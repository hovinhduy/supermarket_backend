package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.enums.DiscountType;
import iuh.fit.supermarket.repository.PromotionHeaderRepository;
import iuh.fit.supermarket.repository.PromotionLineRepository;
import iuh.fit.supermarket.repository.PromotionDetailRepository;
import iuh.fit.supermarket.service.PromotionLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation của PromotionLookupService
 * Cung cấp thông tin khuyến mãi chi tiết cho AI chat
 * Bao gồm: Header, Lines và Details
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PromotionLookupServiceImpl implements PromotionLookupService {

    private final PromotionHeaderRepository promotionRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final PromotionDetailRepository promotionDetailRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Lấy khuyến mãi đang có với thông tin chi tiết từ Lines và Details
     * Trả về format JSON để AI có thể parse thành structured data
     */
    @Override
    public String getActivePromotions(int limit) {
        LocalDate now = LocalDate.now();

        // Lấy các header còn active và còn hạn
        List<PromotionHeader> activeHeaders = promotionRepository
                .findActivePromotions(now, PageRequest.of(0, limit))
                .getContent();

        if (activeHeaders.isEmpty()) {
            return "Hiện tại không có chương trình khuyến mãi nào đang diễn ra.";
        }

        StringBuilder result = new StringBuilder("[PROMOTIONS]\n");

        for (PromotionHeader header : activeHeaders) {
            // Lấy các line còn active và còn hạn của header này
            List<PromotionLine> activeLines = promotionLineRepository
                    .findActiveLinesByHeaderId(header.getPromotionId(), now);

            // Chỉ xử lý nếu có line active
            if (activeLines.isEmpty()) {
                continue;
            }

            // Mỗi line + detail tạo thành một promotion riêng
            for (PromotionLine line : activeLines) {
                // Lấy các detail của line này
                List<PromotionDetail> details = promotionDetailRepository
                        .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

                for (PromotionDetail detail : details) {
                    result.append(formatPromotionAsJson(line, detail));
                }
            }
        }

        log.info("🎁 Đã tải khuyến mãi từ {} header", activeHeaders.size());
        return result.toString();
    }

    /**
     * Format promotion thành JSON cho AI parse
     * Mỗi PromotionLine + PromotionDetail = 1 promotion entry
     */
    private String formatPromotionAsJson(PromotionLine line, PromotionDetail detail) {
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append(String.format("  \"promotion_line_id\": %d,\n", line.getPromotionLineId()));
        json.append(String.format("  \"promotion_code\": \"%s\",\n",
                detail.getPromotionCode() != null ? detail.getPromotionCode() : ""));
        json.append(String.format("  \"name\": \"%s\",\n", escapejson(line.getLineName())));
        json.append(String.format("  \"description\": \"%s\",\n",
                line.getDescription() != null ? escapejson(line.getDescription()) : ""));
        
        // Thêm mô tả ngắn gọn, dễ hiểu
        String summary = generatePromotionSummary(detail);
        json.append(String.format("  \"summary\": \"%s\",\n", escapejson(summary)));
        
        json.append(String.format("  \"type\": \"%s\",\n", line.getPromotionType().name()));
        json.append(String.format("  \"start_date\": \"%s\",\n", line.getStartDate()));
        json.append(String.format("  \"end_date\": \"%s\",\n", line.getEndDate()));
        json.append(String.format("  \"status\": \"%s\",\n", line.getStatus().name()));
        json.append(String.format("  \"usage_limit\": %s,\n",
                detail.getUsageLimit() != null ? detail.getUsageLimit() : "null"));
        json.append(String.format("  \"usage_count\": %d,\n", detail.getUsageCount()));

        // Thêm thông tin chi tiết theo loại khuyến mãi
        if (detail instanceof BuyXGetYDetail) {
            json.append(formatBuyXGetYDetail((BuyXGetYDetail) detail));
        } else if (detail instanceof OrderDiscountDetail) {
            json.append(formatOrderDiscountDetail((OrderDiscountDetail) detail));
        } else if (detail instanceof ProductDiscountDetail) {
            json.append(formatProductDiscountDetail((ProductDiscountDetail) detail));
        }

        json.append("}\n");
        return json.toString();
    }

    /**
     * Tạo mô tả ngắn gọn cho khuyến mãi
     */
    private String generatePromotionSummary(PromotionDetail detail) {
        if (detail instanceof BuyXGetYDetail) {
            return generateBuyXGetYSummary((BuyXGetYDetail) detail);
        } else if (detail instanceof OrderDiscountDetail) {
            return generateOrderDiscountSummary((OrderDiscountDetail) detail);
        } else if (detail instanceof ProductDiscountDetail) {
            return generateProductDiscountSummary((ProductDiscountDetail) detail);
        }
        return "";
    }

    /**
     * Tạo mô tả cho Mua X Tặng Y
     * Ví dụ: "Mua 5 hộp Sữa tươi Vinamilk tặng 1 hộp miễn phí", "Mua 5 hộp Sữa tươi Vinamilk giảm 10% cho 1 hộp tiếp theo"
     */
    private String generateBuyXGetYSummary(BuyXGetYDetail detail) {
        StringBuilder summary = new StringBuilder();
        
        // Lấy thông tin sản phẩm và đơn vị mua
        String buyProductName = "";
        String buyUnitName = "";
        if (detail.getBuyProduct() != null) {
            if (detail.getBuyProduct().getProduct() != null) {
                buyProductName = detail.getBuyProduct().getProduct().getName();
            }
            if (detail.getBuyProduct().getUnit() != null) {
                buyUnitName = detail.getBuyProduct().getUnit().getName();
            }
        }
        
        // Lấy thông tin sản phẩm và đơn vị tặng/giảm
        String giftProductName = "";
        String giftUnitName = "";
        if (detail.getGiftProduct() != null) {
            if (detail.getGiftProduct().getProduct() != null) {
                giftProductName = detail.getGiftProduct().getProduct().getName();
            }
            if (detail.getGiftProduct().getUnit() != null) {
                giftUnitName = detail.getGiftProduct().getUnit().getName();
            }
        }
        
        // Điều kiện mua
        if (detail.getBuyMinQuantity() != null) {
            summary.append(String.format("Mua %d", detail.getBuyMinQuantity()));
            if (!buyUnitName.isEmpty()) {
                summary.append(" ").append(buyUnitName);
            }
            if (!buyProductName.isEmpty()) {
                summary.append(" ").append(buyProductName);
            }
        } else if (detail.getBuyMinValue() != null) {
            if (!buyProductName.isEmpty()) {
                summary.append(String.format("Mua %s từ %,.0fđ", buyProductName, detail.getBuyMinValue()));
            } else {
                summary.append(String.format("Mua từ %,.0fđ", detail.getBuyMinValue()));
            }
        }
        
        // Ưu đãi nhận được
        if (detail.getGiftDiscountType() == DiscountType.FREE) {
            // Tặng miễn phí
            if (detail.getGiftQuantity() != null) {
                summary.append(String.format(" tặng %d", detail.getGiftQuantity()));
                if (!giftUnitName.isEmpty()) {
                    summary.append(" ").append(giftUnitName);
                }
                // Chỉ hiển thị tên sản phẩm tặng nếu khác với sản phẩm mua
                if (!giftProductName.isEmpty() && !giftProductName.equals(buyProductName)) {
                    summary.append(" ").append(giftProductName);
                }
                summary.append(" miễn phí");
            }
        } else if (detail.getGiftDiscountType() == DiscountType.PERCENTAGE) {
            // Giảm %
            if (detail.getGiftQuantity() != null && detail.getGiftDiscountValue() != null) {
                summary.append(String.format(" giảm %s%% cho %d", 
                    detail.getGiftDiscountValue(), detail.getGiftQuantity()));
                if (!giftUnitName.isEmpty()) {
                    summary.append(" ").append(giftUnitName);
                }
                // Chỉ hiển thị tên sản phẩm giảm nếu khác với sản phẩm mua
                if (!giftProductName.isEmpty() && !giftProductName.equals(buyProductName)) {
                    summary.append(" ").append(giftProductName);
                }
                summary.append(" tiếp theo");
            }
        } else if (detail.getGiftDiscountType() == DiscountType.FIXED_AMOUNT) {
            // Giảm số tiền
            if (detail.getGiftQuantity() != null && detail.getGiftDiscountValue() != null) {
                summary.append(String.format(" giảm %,.0fđ cho %d", 
                    detail.getGiftDiscountValue(), detail.getGiftQuantity()));
                if (!giftUnitName.isEmpty()) {
                    summary.append(" ").append(giftUnitName);
                }
                // Chỉ hiển thị tên sản phẩm giảm nếu khác với sản phẩm mua
                if (!giftProductName.isEmpty() && !giftProductName.equals(buyProductName)) {
                    summary.append(" ").append(giftProductName);
                }
                summary.append(" tiếp theo");
            }
        }
        
        return summary.toString();
    }

    /**
     * Tạo mô tả cho Giảm Giá Đơn Hàng
     * Ví dụ: "Giảm 10% đơn hàng từ 500.000đ (tối đa 50.000đ)"
     */
    private String generateOrderDiscountSummary(OrderDiscountDetail detail) {
        StringBuilder summary = new StringBuilder();
        
        // Giá trị giảm
        if (detail.getOrderDiscountType() == DiscountType.PERCENTAGE) {
            summary.append(String.format("Giảm %s%% đơn hàng", detail.getOrderDiscountValue()));
            
            // Giảm tối đa
            if (detail.getOrderDiscountMaxValue() != null) {
                summary.append(String.format(" (tối đa %,.0fđ)", detail.getOrderDiscountMaxValue()));
            }
        } else if (detail.getOrderDiscountType() == DiscountType.FIXED_AMOUNT) {
            summary.append(String.format("Giảm %,.0fđ cho đơn hàng", detail.getOrderDiscountValue()));
        }
        
        // Điều kiện đơn hàng
        if (detail.getOrderMinTotalValue() != null) {
            summary.append(String.format(" khi mua từ %,.0fđ", detail.getOrderMinTotalValue()));
        } else if (detail.getOrderMinTotalQuantity() != null) {
            summary.append(String.format(" khi mua từ %d sản phẩm", detail.getOrderMinTotalQuantity()));
        }
        
        return summary.toString();
    }

    /**
     * Tạo mô tả cho Giảm Giá Sản Phẩm
     * Ví dụ: "Giảm 15% cho Sữa tươi Vinamilk", "Giảm 10.000đ mỗi hộp Sữa tươi Vinamilk khi mua từ 3"
     */
    private String generateProductDiscountSummary(ProductDiscountDetail detail) {
        StringBuilder summary = new StringBuilder();
        
        // Lấy thông tin sản phẩm và đơn vị
        String productName = "";
        String unitName = "";
        if (detail.getApplyToProduct() != null) {
            if (detail.getApplyToProduct().getProduct() != null) {
                productName = detail.getApplyToProduct().getProduct().getName();
            }
            if (detail.getApplyToProduct().getUnit() != null) {
                unitName = detail.getApplyToProduct().getUnit().getName();
            }
        }
        
        // Giá trị giảm
        if (detail.getProductDiscountType() == DiscountType.PERCENTAGE) {
            summary.append(String.format("Giảm %s%%", detail.getProductDiscountValue()));
        } else if (detail.getProductDiscountType() == DiscountType.FIXED_AMOUNT) {
            summary.append(String.format("Giảm %,.0fđ", detail.getProductDiscountValue()));
        }
        
        // Áp dụng cho
        if (!productName.isEmpty()) {
            if (!unitName.isEmpty()) {
                summary.append(String.format(" mỗi %s %s", unitName, productName));
            } else {
                summary.append(String.format(" cho %s", productName));
            }
        } else {
            summary.append(" cho sản phẩm");
        }
        
        // Điều kiện
        if (detail.getProductMinOrderValue() != null) {
            summary.append(String.format(" khi đơn hàng từ %,.0fđ", detail.getProductMinOrderValue()));
        } else if (detail.getProductMinPromotionQuantity() != null) {
            summary.append(String.format(" khi mua từ %d", detail.getProductMinPromotionQuantity()));
            if (!unitName.isEmpty()) {
                summary.append(" ").append(unitName);
            }
        }
        
        return summary.toString();
    }

    /**
     * Format chi tiết Mua X Tặng Y
     */
    private String formatBuyXGetYDetail(BuyXGetYDetail detail) {
        StringBuilder json = new StringBuilder();
        json.append("  \"buy_x_get_y_detail\": {\n");

        if (detail.getBuyProduct() != null && detail.getBuyProduct().getProduct() != null) {
            json.append(String.format("    \"buy_product_name\": \"%s\",\n",
                    escapejson(detail.getBuyProduct().getProduct().getName())));
        }

        json.append(String.format("    \"buy_min_quantity\": %s,\n",
                detail.getBuyMinQuantity() != null ? detail.getBuyMinQuantity() : "null"));
        json.append(String.format("    \"buy_min_value\": %s,\n",
                detail.getBuyMinValue() != null ? detail.getBuyMinValue() : "null"));

        if (detail.getGiftProduct() != null && detail.getGiftProduct().getProduct() != null) {
            json.append(String.format("    \"gift_product_name\": \"%s\",\n",
                    escapejson(detail.getGiftProduct().getProduct().getName())));
        }

        json.append(String.format("    \"gift_quantity\": %s,\n",
                detail.getGiftQuantity() != null ? detail.getGiftQuantity() : "null"));
        json.append(String.format("    \"gift_discount_type\": \"%s\",\n",
                detail.getGiftDiscountType() != null ? detail.getGiftDiscountType().name() : ""));
        json.append(String.format("    \"gift_discount_value\": %s,\n",
                detail.getGiftDiscountValue() != null ? detail.getGiftDiscountValue() : "null"));
        json.append(String.format("    \"gift_max_quantity\": %s\n",
                detail.getGiftMaxQuantity() != null ? detail.getGiftMaxQuantity() : "null"));

        json.append("  },\n");
        json.append("  \"order_discount_detail\": null,\n");
        json.append("  \"product_discount_detail\": null\n");

        return json.toString();
    }

    /**
     * Format chi tiết Giảm Giá Đơn Hàng
     */
    private String formatOrderDiscountDetail(OrderDiscountDetail detail) {
        StringBuilder json = new StringBuilder();
        json.append("  \"buy_x_get_y_detail\": null,\n");
        json.append("  \"order_discount_detail\": {\n");

        json.append(String.format("    \"discount_type\": \"%s\",\n",
                detail.getOrderDiscountType() != null ? detail.getOrderDiscountType().name() : ""));
        json.append(String.format("    \"discount_value\": %s,\n",
                detail.getOrderDiscountValue() != null ? detail.getOrderDiscountValue() : "null"));
        json.append(String.format("    \"max_discount\": %s,\n",
                detail.getOrderDiscountMaxValue() != null ? detail.getOrderDiscountMaxValue() : "null"));
        json.append(String.format("    \"min_order_value\": %s,\n",
                detail.getOrderMinTotalValue() != null ? detail.getOrderMinTotalValue() : "null"));
        json.append(String.format("    \"min_order_quantity\": %s\n",
                detail.getOrderMinTotalQuantity() != null ? detail.getOrderMinTotalQuantity() : "null"));

        json.append("  },\n");
        json.append("  \"product_discount_detail\": null\n");

        return json.toString();
    }

    /**
     * Format chi tiết Giảm Giá Sản Phẩm
     */
    private String formatProductDiscountDetail(ProductDiscountDetail detail) {
        StringBuilder json = new StringBuilder();
        json.append("  \"buy_x_get_y_detail\": null,\n");
        json.append("  \"order_discount_detail\": null,\n");
        json.append("  \"product_discount_detail\": {\n");

        json.append(String.format("    \"discount_type\": \"%s\",\n",
                detail.getProductDiscountType() != null ? detail.getProductDiscountType().name() : ""));
        json.append(String.format("    \"discount_value\": %s,\n",
                detail.getProductDiscountValue() != null ? detail.getProductDiscountValue() : "null"));
        json.append(String.format("    \"apply_to_type\": \"%s\",\n",
                detail.getApplyToType() != null ? detail.getApplyToType().name() : ""));

        if (detail.getApplyToProduct() != null && detail.getApplyToProduct().getProduct() != null) {
            json.append(String.format("    \"apply_to_product_name\": \"%s\",\n",
                    escapejson(detail.getApplyToProduct().getProduct().getName())));
        } else {
            json.append("    \"apply_to_product_name\": null,\n");
        }

        json.append(String.format("    \"min_order_value\": %s,\n",
                detail.getProductMinOrderValue() != null ? detail.getProductMinOrderValue() : "null"));
        json.append(String.format("    \"min_promotion_value\": %s,\n",
                detail.getProductMinPromotionValue() != null ? detail.getProductMinPromotionValue() : "null"));
        json.append(String.format("    \"min_promotion_quantity\": %s\n",
                detail.getProductMinPromotionQuantity() != null ? detail.getProductMinPromotionQuantity() : "null"));

        json.append("  }\n");

        return json.toString();
    }

    /**
     * Escape JSON string
     */
    private String escapejson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Dịch loại khuyến mãi
     */
    private String translatePromotionType(PromotionType promotionType) {
        if (promotionType == null)
            return "Chung";

        return switch (promotionType) {
            case PRODUCT_DISCOUNT -> "Giảm giá sản phẩm";
            case ORDER_DISCOUNT -> "Giảm giá đơn hàng";
            case BUY_X_GET_Y -> "Mua X tặng Y";
            default -> promotionType.toString();
        };
    }

    /**
     * Tìm kiếm khuyến mãi
     */
    @Override
    public String searchPromotions(String keyword) {
        List<PromotionHeader> promotions = promotionRepository
                .findByKeyword(keyword, PageRequest.of(0, 5))
                .getContent();

        if (promotions.isEmpty()) {
            return "Không tìm thấy chương trình khuyến mãi nào với từ khóa: " + keyword;
        }

        StringBuilder result = new StringBuilder("Kết quả tìm kiếm khuyến mãi:\n\n");
        for (int i = 0; i < promotions.size(); i++) {
            PromotionHeader promo = promotions.get(i);
            result.append(String.format("%d. %s\n", i + 1, promo.getPromotionName()));

            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                result.append(String.format("   %s\n", promo.getDescription()));
            }

            result.append(String.format("   Trạng thái: %s\n", translateStatus(promo.getStatus().name())));
            result.append(String.format("   Thời gian: %s - %s\n",
                    promo.getStartDate().format(dateFormatter),
                    promo.getEndDate().format(dateFormatter)));
            result.append("\n");
        }

        return result.toString();
    }

    /**
     * Dịch trạng thái khuyến mãi sang tiếng Việt
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "ACTIVE" -> "Đang diễn ra";
            case "UPCOMING" -> "Sắp diễn ra";
            case "PAUSED" -> "Đang tạm dừng";
            case "EXPIRED" -> "Đã hết hạn";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
