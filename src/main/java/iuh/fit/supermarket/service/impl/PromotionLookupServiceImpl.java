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
     * Lấy khuyến mãi đang có với thông tin chi tiết Lines và Details
     */
    @Override
    public String getActivePromotions(int limit) {
        List<PromotionHeader> promotions = promotionRepository
                .findActivePromotions(LocalDate.now(), PageRequest.of(0, limit))
                .getContent();

        if (promotions.isEmpty()) {
            return "Hiện tại không có chương trình khuyến mãi nào đang diễn ra.";
        }

        StringBuilder result = new StringBuilder("🎁 KHUYẾN MÃI ĐANG DIỄN RA:\n");
        result.append("═".repeat(50)).append("\n\n");

        for (int i = 0; i < promotions.size(); i++) {
            PromotionHeader promo = promotions.get(i);

            // Header information
            result.append(String.format("📌 %d. %s\n", i + 1, promo.getPromotionName().toUpperCase()));
            result.append("─".repeat(40)).append("\n");

            if (promo.getDescription() != null && !promo.getDescription().isEmpty()) {
                result.append(String.format("📝 Mô tả: %s\n", promo.getDescription()));
            }

            result.append(String.format("📅 Thời gian: %s - %s\n",
                    promo.getStartDate().format(dateFormatter),
                    promo.getEndDate().format(dateFormatter)
            ));

            // Loại khuyến mãi được xác định ở PromotionLine level, không phải Header

            // Get Promotion Lines
            List<PromotionLine> lines = promotionLineRepository.findByPromotionHeaderId(promo.getPromotionId());

            if (!lines.isEmpty()) {
                result.append("\n📋 CHI TIẾT KHUYẾN MÃI:\n");

                for (PromotionLine line : lines) {
                    result.append(String.format("   • Tên: %s", line.getLineName()));

                    // Hiển thị loại khuyến mãi cho từng line
                    if (line.getPromotionType() != null) {
                        result.append(String.format(" [%s]", translatePromotionType(line.getPromotionType())));
                    }
                    result.append("\n");

                    if (line.getDescription() != null && !line.getDescription().isEmpty()) {
                        result.append(String.format("     %s\n", line.getDescription()));
                    }

                    // Lấy thông tin chi tiết của line này
                    String detailInfo = getPromotionDetailInfo(line);
                    if (!detailInfo.isEmpty()) {
                        result.append(detailInfo);
                    }

                    result.append("\n");
                }
            }

            result.append("\n");
            result.append("═".repeat(50)).append("\n\n");
        }

        log.info("🎁 Đã tải {} chương trình khuyến mãi với chi tiết", promotions.size());
        return result.toString();
    }

    /**
     * Lấy thông tin chi tiết của PromotionDetail dựa trên PromotionLine
     */
    private String getPromotionDetailInfo(PromotionLine line) {
        StringBuilder detailInfo = new StringBuilder();

        try {
            // Tìm PromotionDetail liên quan đến line này
            List<PromotionDetail> details = promotionDetailRepository.findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            if (!details.isEmpty()) {
                detailInfo.append("     💰 Ưu đãi:\n");

                for (PromotionDetail detail : details) {
                    // Hiển thị mã khuyến mãi (giờ nằm ở detail level)
                    if (detail.getPromotionCode() != null) {
                        detailInfo.append(String.format("       📍 Mã KM: %s\n", detail.getPromotionCode()));
                    }

                    // Kiểm tra loại detail và format thông tin phù hợp
                    if (detail instanceof OrderDiscountDetail) {
                        OrderDiscountDetail orderDiscount = (OrderDiscountDetail) detail;
                        if (orderDiscount.getOrderDiscountType() == iuh.fit.supermarket.enums.DiscountType.PERCENTAGE) {
                            detailInfo.append(String.format("       - Giảm %s%% cho đơn hàng",
                                    orderDiscount.getOrderDiscountValue()));
                            if (orderDiscount.getOrderDiscountMaxValue() != null) {
                                detailInfo.append(String.format(" (Tối đa: %,.0fđ)",
                                        orderDiscount.getOrderDiscountMaxValue()));
                            }
                            detailInfo.append("\n");
                        } else {
                            detailInfo.append(String.format("       - Giảm trực tiếp: %,.0fđ\n",
                                    orderDiscount.getOrderDiscountValue()));
                        }

                        if (orderDiscount.getOrderMinTotalValue() != null) {
                            detailInfo.append(String.format("       - Áp dụng cho đơn từ: %,.0fđ\n",
                                    orderDiscount.getOrderMinTotalValue()));
                        }
                    } else if (detail instanceof ProductDiscountDetail) {
                        ProductDiscountDetail productDiscount = (ProductDiscountDetail) detail;
                        if (productDiscount.getProductDiscountType() == iuh.fit.supermarket.enums.DiscountType.PERCENTAGE) {
                            detailInfo.append(String.format("       - Giảm %s%% cho sản phẩm\n",
                                    productDiscount.getProductDiscountValue()));
                        } else {
                            detailInfo.append(String.format("       - Giảm %,.0fđ cho sản phẩm\n",
                                    productDiscount.getProductDiscountValue()));
                        }

                        if (productDiscount.getApplyToProduct() != null && productDiscount.getApplyToProduct().getProduct() != null) {
                            detailInfo.append(String.format("       - Sản phẩm: %s\n",
                                    productDiscount.getApplyToProduct().getProduct().getName()));
                        }

                        if (productDiscount.getProductMinOrderValue() != null) {
                            detailInfo.append(String.format("       - Đơn hàng tối thiểu: %,.0fđ\n",
                                    productDiscount.getProductMinOrderValue()));
                        }
                    } else if (detail instanceof BuyXGetYDetail) {
                        BuyXGetYDetail buyXGetY = (BuyXGetYDetail) detail;

                        if (buyXGetY.getBuyMinQuantity() != null && buyXGetY.getGiftQuantity() != null) {
                            detailInfo.append(String.format("       - Mua %d tặng %d\n",
                                    buyXGetY.getBuyMinQuantity(),
                                    buyXGetY.getGiftQuantity()));
                        }

                        if (buyXGetY.getBuyProduct() != null && buyXGetY.getBuyProduct().getProduct() != null) {
                            detailInfo.append(String.format("       - Sản phẩm mua: %s\n",
                                    buyXGetY.getBuyProduct().getProduct().getName()));
                        }

                        if (buyXGetY.getGiftProduct() != null && buyXGetY.getGiftProduct().getProduct() != null) {
                            detailInfo.append(String.format("       - Sản phẩm tặng: %s\n",
                                    buyXGetY.getGiftProduct().getProduct().getName()));
                        }

                        if (buyXGetY.getGiftDiscountType() == iuh.fit.supermarket.enums.DiscountType.FREE) {
                            detailInfo.append("       - Tặng miễn phí\n");
                        } else if (buyXGetY.getGiftDiscountType() == iuh.fit.supermarket.enums.DiscountType.PERCENTAGE) {
                            detailInfo.append(String.format("       - Giảm %s%% cho sản phẩm tặng\n",
                                    buyXGetY.getGiftDiscountValue()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết khuyến mãi cho line {}: {}", line.getLineName(), e.getMessage());
        }

        return detailInfo.toString();
    }

    /**
     * Dịch loại khuyến mãi
     */
    private String translatePromotionType(PromotionType promotionType) {
        if (promotionType == null) return "Chung";

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
                    promo.getEndDate().format(dateFormatter)
            ));
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
            case "EXPIRED" -> "Đã hết hạn";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
