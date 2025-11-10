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
     * Header chỉ dùng để kiểm tra còn active và còn hạn
     * Data thực tế được lấy từ Line và Detail
     */
    @Override
    public String getActivePromotions(int limit) {
        LocalDate now = LocalDate.now();

        // Lấy các header còn active và còn hạn (chỉ để kiểm tra)
        List<PromotionHeader> activeHeaders = promotionRepository
                .findActivePromotions(now, PageRequest.of(0, limit))
                .getContent();

        if (activeHeaders.isEmpty()) {
            return "Hiện tại không có chương trình khuyến mãi nào đang diễn ra.";
        }

        StringBuilder result = new StringBuilder("🎁 KHUYẾN MÃI ĐANG DIỄN RA:\n");
        result.append("═".repeat(50)).append("\n\n");

        int promotionCount = 0;

        for (PromotionHeader header : activeHeaders) {
            // Lấy các line còn active và còn hạn của header này
            List<PromotionLine> activeLines = promotionLineRepository
                    .findActiveLinesByHeaderId(header.getPromotionId(), now);

            // Chỉ hiển thị header nếu có line active
            if (activeLines.isEmpty()) {
                continue;
            }

            promotionCount++;

            // Hiển thị tên header ngắn gọn (chỉ để group các line)
            result.append(String.format("📌 %d. CHƯƠNG TRÌNH: %s\n", promotionCount, header.getPromotionName().toUpperCase()));
            result.append("─".repeat(40)).append("\n\n");

            // Focus vào Line và Detail
            for (PromotionLine line : activeLines) {
                result.append(String.format("   🏷️  %s", line.getLineName()));

                // Hiển thị loại khuyến mãi
                if (line.getPromotionType() != null) {
                    result.append(String.format(" [%s]", translatePromotionType(line.getPromotionType())));
                }
                result.append("\n");

                // Thời gian của line
                result.append(String.format("       📅 %s - %s\n",
                        line.getStartDate().format(dateFormatter),
                        line.getEndDate().format(dateFormatter)
                ));

                // Mô tả line
                if (line.getDescription() != null && !line.getDescription().isEmpty()) {
                    result.append(String.format("       📝 %s\n", line.getDescription()));
                }

                // Lấy thông tin chi tiết từ Detail
                String detailInfo = getPromotionDetailInfo(line);
                if (!detailInfo.isEmpty()) {
                    result.append(detailInfo);
                }

                result.append("\n");
            }

            result.append("═".repeat(50)).append("\n\n");
        }

        if (promotionCount == 0) {
            return "Hiện tại không có chương trình khuyến mãi nào đang diễn ra.";
        }

        log.info("🎁 Đã tải {} chương trình khuyến mãi với {} line đang active",
                activeHeaders.size(), promotionCount);
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

                        // Hiển thị sản phẩm mua trước
                        if (buyXGetY.getBuyProduct() != null && buyXGetY.getBuyProduct().getProduct() != null) {
                            detailInfo.append(String.format("       - Sản phẩm áp dụng: %s\n",
                                    buyXGetY.getBuyProduct().getProduct().getName()));
                        }

                        // Hiển thị điều kiện và ưu đãi tùy theo giftDiscountType
                        if (buyXGetY.getBuyMinQuantity() != null && buyXGetY.getGiftQuantity() != null) {
                            if (buyXGetY.getGiftDiscountType() == iuh.fit.supermarket.enums.DiscountType.FREE) {
                                // Tặng miễn phí
                                detailInfo.append(String.format("       - Mua %d tặng %d miễn phí\n",
                                        buyXGetY.getBuyMinQuantity(),
                                        buyXGetY.getGiftQuantity()));
                            } else if (buyXGetY.getGiftDiscountType() == iuh.fit.supermarket.enums.DiscountType.PERCENTAGE) {
                                // Giảm % cho sản phẩm tiếp theo
                                detailInfo.append(String.format("       - Mua %d giảm %s%% cho %d sản phẩm tiếp theo\n",
                                        buyXGetY.getBuyMinQuantity(),
                                        buyXGetY.getGiftDiscountValue(),
                                        buyXGetY.getGiftQuantity()));
                            } else if (buyXGetY.getGiftDiscountType() == iuh.fit.supermarket.enums.DiscountType.FIXED_AMOUNT) {
                                // Giảm số tiền cố định cho sản phẩm tiếp theo
                                detailInfo.append(String.format("       - Mua %d giảm %,.0fđ cho %d sản phẩm tiếp theo\n",
                                        buyXGetY.getBuyMinQuantity(),
                                        buyXGetY.getGiftDiscountValue(),
                                        buyXGetY.getGiftQuantity()));
                            }
                        }

                        // Chỉ hiển thị sản phẩm được giảm nếu khác với sản phẩm mua
                        if (buyXGetY.getGiftProduct() != null &&
                            buyXGetY.getGiftProduct().getProduct() != null &&
                            buyXGetY.getBuyProduct() != null &&
                            !buyXGetY.getGiftProduct().getProduct().getId().equals(
                                buyXGetY.getBuyProduct().getProduct().getId())) {
                            detailInfo.append(String.format("       - Sản phẩm được giảm: %s\n",
                                    buyXGetY.getGiftProduct().getProduct().getName()));
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
