package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.structured.BuyXGetYInfo;
import iuh.fit.supermarket.dto.chat.structured.OrderDiscountInfo;
import iuh.fit.supermarket.dto.chat.structured.ProductDiscountInfo;
import iuh.fit.supermarket.dto.chat.structured.PromotionInfo;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.repository.PromotionLineRepository;
import iuh.fit.supermarket.service.PromotionSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của PromotionSearchService
 * Tìm kiếm và lấy thông tin khuyến mãi đang hoạt động
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionSearchServiceImpl implements PromotionSearchService {

    private final PromotionLineRepository promotionLineRepository;

    /**
     * Lấy danh sách khuyến mãi đang hoạt động (ACTIVE và còn hạn)
     * 
     * @param limit số lượng tối đa
     * @return danh sách thông tin khuyến mãi
     */
    @Override
    @Transactional(readOnly = true)
    public List<PromotionInfo> getActivePromotions(Integer limit) {
        log.info("Tìm kiếm khuyến mãi đang hoạt động với limit: {}", limit);

        // Lấy tất cả promotion lines đang ACTIVE
        List<PromotionLine> promotionLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> {
                    LocalDate now = LocalDate.now();
                    return !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate());
                })
                .limit(limit != null && limit > 0 ? limit : 10)
                .collect(Collectors.toList());

        log.debug("Tìm thấy {} promotion lines đang hoạt động", promotionLines.size());

        // Convert sang PromotionInfo
        List<PromotionInfo> result = new ArrayList<>();
        for (PromotionLine line : promotionLines) {
            // Mỗi line có thể có nhiều details
            if (line.getDetails() != null && !line.getDetails().isEmpty()) {
                for (PromotionDetail detail : line.getDetails()) {
                    PromotionInfo info = mapToPromotionInfo(line, detail);
                    if (info != null) {
                        result.add(info);
                    }
                }
            }
        }

        log.info("Trả về {} khuyến mãi", result.size());
        return result;
    }

    /**
     * Map PromotionLine và PromotionDetail sang PromotionInfo
     */
    private PromotionInfo mapToPromotionInfo(PromotionLine line, PromotionDetail detail) {
        try {
            // Build summary dựa trên type
            String summary = buildSummary(line, detail);

            // Map detail specific fields
            BuyXGetYInfo buyXGetYInfo = null;
            OrderDiscountInfo orderDiscountInfo = null;
            ProductDiscountInfo productDiscountInfo = null;

            if (detail instanceof BuyXGetYDetail buyXGetY) {
                buyXGetYInfo = mapToBuyXGetYInfo(buyXGetY);
            } else if (detail instanceof OrderDiscountDetail orderDiscount) {
                orderDiscountInfo = mapToOrderDiscountInfo(orderDiscount);
            } else if (detail instanceof ProductDiscountDetail productDiscount) {
                productDiscountInfo = mapToProductDiscountInfo(productDiscount);
            }

            return new PromotionInfo(
                    line.getPromotionLineId(),
                    detail.getPromotionCode(),
                    line.getLineName(),
                    line.getDescription(),
                    summary,
                    line.getPromotionType().name(),
                    line.getStartDate(),
                    line.getEndDate(),
                    line.getStatus().name(),
                    detail.getUsageLimit(),
                    detail.getUsageCount(),
                    buyXGetYInfo,
                    orderDiscountInfo,
                    productDiscountInfo
            );
        } catch (Exception e) {
            log.error("Lỗi khi map promotion line {} detail {}: {}", 
                    line.getPromotionLineId(), detail.getDetailId(), e.getMessage());
            return null;
        }
    }

    /**
     * Tạo summary ngắn gọn cho khuyến mãi
     */
    private String buildSummary(PromotionLine line, PromotionDetail detail) {
        if (detail instanceof BuyXGetYDetail buyXGetY) {
            String buyProduct = buyXGetY.getBuyProduct() != null && buyXGetY.getBuyProduct().getProduct() != null
                    ? buyXGetY.getBuyProduct().getProduct().getName() 
                    : "sản phẩm";
            String giftProduct = buyXGetY.getGiftProduct() != null && buyXGetY.getGiftProduct().getProduct() != null
                    ? buyXGetY.getGiftProduct().getProduct().getName() 
                    : "quà tặng";
            
            if (buyXGetY.getBuyMinQuantity() != null && buyXGetY.getGiftQuantity() != null) {
                return String.format("Mua %d %s tặng %d %s", 
                        buyXGetY.getBuyMinQuantity(), buyProduct,
                        buyXGetY.getGiftQuantity(), giftProduct);
            }
            return String.format("Mua %s tặng %s", buyProduct, giftProduct);
            
        } else if (detail instanceof OrderDiscountDetail orderDiscount) {
            String discountStr;
            if (orderDiscount.getOrderDiscountType() != null) {
                switch (orderDiscount.getOrderDiscountType()) {
                    case PERCENTAGE:
                        discountStr = String.format("Giảm %s%%", orderDiscount.getOrderDiscountValue());
                        break;
                    case FIXED_AMOUNT:
                        discountStr = String.format("Giảm %sk", orderDiscount.getOrderDiscountValue().divide(java.math.BigDecimal.valueOf(1000)));
                        break;
                    default:
                        discountStr = "Giảm giá";
                }
            } else {
                discountStr = "Giảm giá";
            }
            
            if (orderDiscount.getOrderMinTotalValue() != null) {
                return String.format("%s đơn từ %sk", 
                        discountStr, 
                        orderDiscount.getOrderMinTotalValue().divide(java.math.BigDecimal.valueOf(1000)));
            }
            return discountStr + " đơn hàng";
            
        } else if (detail instanceof ProductDiscountDetail productDiscount) {
            String discountStr;
            if (productDiscount.getProductDiscountType() != null) {
                switch (productDiscount.getProductDiscountType()) {
                    case PERCENTAGE:
                        discountStr = String.format("Giảm %s%%", productDiscount.getProductDiscountValue());
                        break;
                    case FIXED_AMOUNT:
                        discountStr = String.format("Giảm %sk", productDiscount.getProductDiscountValue().divide(java.math.BigDecimal.valueOf(1000)));
                        break;
                    default:
                        discountStr = "Giảm giá";
                }
            } else {
                discountStr = "Giảm giá";
            }
            
            if (productDiscount.getApplyToProduct() != null && productDiscount.getApplyToProduct().getProduct() != null) {
                return discountStr + " " + productDiscount.getApplyToProduct().getProduct().getName();
            }
            return discountStr + " sản phẩm";
        }
        
        return line.getLineName();
    }

    /**
     * Map BuyXGetYDetail sang BuyXGetYInfo
     */
    private BuyXGetYInfo mapToBuyXGetYInfo(BuyXGetYDetail detail) {
        String buyProductName = null;
        if (detail.getBuyProduct() != null && detail.getBuyProduct().getProduct() != null) {
            buyProductName = detail.getBuyProduct().getProduct().getName();
        }
        
        String giftProductName = null;
        if (detail.getGiftProduct() != null && detail.getGiftProduct().getProduct() != null) {
            giftProductName = detail.getGiftProduct().getProduct().getName();
        }
        
        return new BuyXGetYInfo(
                buyProductName,
                detail.getBuyMinQuantity(),
                detail.getBuyMinValue(),
                giftProductName,
                detail.getGiftQuantity(),
                detail.getGiftDiscountType() != null ? detail.getGiftDiscountType().name() : null,
                detail.getGiftDiscountValue(),
                detail.getGiftMaxQuantity()
        );
    }

    /**
     * Map OrderDiscountDetail sang OrderDiscountInfo
     */
    private OrderDiscountInfo mapToOrderDiscountInfo(OrderDiscountDetail detail) {
        return new OrderDiscountInfo(
                detail.getOrderDiscountType() != null ? detail.getOrderDiscountType().name() : null,
                detail.getOrderDiscountValue(),
                detail.getOrderDiscountMaxValue(),
                detail.getOrderMinTotalValue(),
                detail.getOrderMinTotalQuantity()
        );
    }

    /**
     * Map ProductDiscountDetail sang ProductDiscountInfo
     */
    private ProductDiscountInfo mapToProductDiscountInfo(ProductDiscountDetail detail) {
        String applyToProductName = null;
        if (detail.getApplyToProduct() != null && detail.getApplyToProduct().getProduct() != null) {
            applyToProductName = detail.getApplyToProduct().getProduct().getName();
        }
        
        return new ProductDiscountInfo(
                detail.getProductDiscountType() != null ? detail.getProductDiscountType().name() : null,
                detail.getProductDiscountValue(),
                detail.getApplyToType() != null ? detail.getApplyToType().name() : null,
                applyToProductName,
                detail.getProductMinOrderValue(),
                detail.getProductMinPromotionValue(),
                detail.getProductMinPromotionQuantity()
        );
    }
}
