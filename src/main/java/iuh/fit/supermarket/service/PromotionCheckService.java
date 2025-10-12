package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.checkout.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.exception.ProductNotFoundException;
import iuh.fit.supermarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service để kiểm tra và áp dụng các chương trình khuyến mãi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionCheckService {

    private final ProductUnitRepository productUnitRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final PriceDetailRepository priceDetailRepository;

    /**
     * Kiểm tra và áp dụng khuyến mãi cho giỏ hàng
     * Hỗ trợ BUY_X_GET_Y, PRODUCT_DISCOUNT và ORDER_DISCOUNT
     * 
     * Thứ tự áp dụng:
     * 1. PRODUCT_DISCOUNT - giảm giá từng sản phẩm
     * 2. BUY_X_GET_Y - tạo dòng quà tặng
     * 3. ORDER_DISCOUNT - giảm giá toàn đơn (tính sau 2 bước trên)
     * 
     * @param request danh sách sản phẩm trong giỏ hàng
     * @return response với các sản phẩm và khuyến mãi được áp dụng
     */
    @Transactional(readOnly = true)
    public CheckPromotionResponseDTO checkAndApplyPromotions(CheckPromotionRequestDTO request) {
        log.info("Bắt đầu kiểm tra khuyến mãi cho {} sản phẩm", request.items().size());

        Map<Long, ProductUnit> productUnitMap = loadProductUnits(request.items());
        Map<Long, BigDecimal> priceMap = loadPrices(productUnitMap.keySet());

        // Tải tất cả khuyến mãi giảm giá sản phẩm đang active
        List<ProductDiscountDetail> productDiscounts = findApplicableProductDiscounts();

        List<CartItemResponseDTO> resultItems = new ArrayList<>();
        Long lineItemId = 1L;

        for (CartItemRequestDTO item : request.items()) {
            ProductUnit productUnit = productUnitMap.get(item.productUnitId());
            if (productUnit == null) {
                throw new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + item.productUnitId());
            }

            BigDecimal unitPrice = priceMap.getOrDefault(item.productUnitId(), BigDecimal.ZERO);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));

            // 1. Kiểm tra PRODUCT_DISCOUNT (giảm giá trực tiếp)
            ProductDiscountDetail applicableDiscount = findBestProductDiscount(
                    productDiscounts, 
                    productUnit, 
                    item.quantity(), 
                    lineTotal
            );

            PromotionAppliedDTO productDiscountApplied = null;
            if (applicableDiscount != null) {
                BigDecimal discountAmount = calculateProductDiscountAmount(
                        applicableDiscount,
                        lineTotal
                );
                lineTotal = lineTotal.subtract(discountAmount);
                
                productDiscountApplied = new PromotionAppliedDTO(
                        applicableDiscount.getPromotionLine().getPromotionCode(),
                        applicableDiscount.getPromotionLine().getDescription() != null 
                            ? applicableDiscount.getPromotionLine().getDescription()
                            : "Giảm giá sản phẩm",
                        applicableDiscount.getDetailId(),
                        buildProductDiscountSummary(applicableDiscount, productUnit),
                        mapDiscountType(applicableDiscount.getProductDiscountType()),
                        applicableDiscount.getProductDiscountValue(),
                        null
                );
            }

            // 2. Kiểm tra BUY_X_GET_Y (mua X tặng Y)
            List<BuyXGetYDetail> applicableBuyXGetY = findApplicableBuyXGetY(
                    productUnit.getId(),
                    item.quantity()
            );

            boolean hasBuyXGetYPromotion = !applicableBuyXGetY.isEmpty();

            CartItemResponseDTO cartItem = new CartItemResponseDTO(
                    lineItemId++,
                    productUnit.getId(),
                    productUnit.getUnit().getName(),
                    productUnit.getProduct().getName(),
                    item.quantity(),
                    unitPrice,
                    lineTotal,
                    hasBuyXGetYPromotion,
                    productDiscountApplied
            );
            resultItems.add(cartItem);

            // Thêm dòng quà tặng nếu có BUY_X_GET_Y
            if (hasBuyXGetYPromotion) {
                for (BuyXGetYDetail promotion : applicableBuyXGetY) {
                    CartItemResponseDTO giftItem = createGiftItem(
                            lineItemId++,
                            promotion,
                            item.quantity(),
                            cartItem.lineItemId(),
                            priceMap
                    );
                    resultItems.add(giftItem);
                }
            }
        }

        // Tính summary và áp dụng ORDER_DISCOUNT (sau cùng)
        SummaryResult summaryResult = calculateSummaryWithOrderDiscount(resultItems);

        log.info("Hoàn thành kiểm tra khuyến mãi. Tổng cộng {} dòng sản phẩm (bao gồm khuyến mãi)", 
                resultItems.size());

        return new CheckPromotionResponseDTO(
                resultItems, 
                summaryResult.summary(),
                summaryResult.appliedOrderPromotions()
        );
    }

    /**
     * Tải thông tin ProductUnit từ database
     */
    private Map<Long, ProductUnit> loadProductUnits(List<CartItemRequestDTO> items) {
        Set<Long> productUnitIds = items.stream()
                .map(CartItemRequestDTO::productUnitId)
                .collect(Collectors.toSet());

        return productUnitRepository.findAllById(productUnitIds).stream()
                .collect(Collectors.toMap(ProductUnit::getId, pu -> pu));
    }

    /**
     * Tải giá hiện tại của các sản phẩm
     */
    private Map<Long, BigDecimal> loadPrices(Set<Long> productUnitIds) {
        Map<Long, BigDecimal> priceMap = new HashMap<>();

        for (Long productUnitId : productUnitIds) {
            priceDetailRepository.findCurrentPriceByProductUnitId(productUnitId, iuh.fit.supermarket.enums.PriceType.CURRENT)
                    .ifPresent(priceDetail -> priceMap.put(productUnitId, priceDetail.getSalePrice()));
        }

        return priceMap;
    }

    /**
     * Tìm các khuyến mãi Mua X Tặng Y áp dụng được cho sản phẩm
     */
    private List<BuyXGetYDetail> findApplicableBuyXGetY(Long productUnitId, Integer quantity) {
        LocalDateTime now = LocalDateTime.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.BUY_X_GET_Y)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) && 
                               !now.isAfter(line.getHeader().getEndDate()))
                .filter(line -> isUsageLimitValid(line))
                .toList();

        List<BuyXGetYDetail> applicablePromotions = new ArrayList<>();

        for (PromotionLine line : activeLines) {
            List<PromotionDetail> details = promotionDetailRepository
                    .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            for (PromotionDetail detail : details) {
                if (detail instanceof BuyXGetYDetail buyXGetYDetail) {
                    if (isBuyXGetYApplicable(buyXGetYDetail, productUnitId, quantity)) {
                        applicablePromotions.add(buyXGetYDetail);
                    }
                }
            }
        }

        return applicablePromotions;
    }

    /**
     * Kiểm tra giới hạn sử dụng của promotion line
     */
    private boolean isUsageLimitValid(PromotionLine line) {
        if (line.getMaxUsageTotal() != null && 
            line.getCurrentUsageCount() >= line.getMaxUsageTotal()) {
            return false;
        }
        return true;
    }

    /**
     * Kiểm tra xem khuyến mãi Mua X Tặng Y có áp dụng được không
     */
    private boolean isBuyXGetYApplicable(BuyXGetYDetail detail, Long productUnitId, Integer quantity) {
        if (detail.getBuyProduct() == null || !detail.getBuyProduct().getId().equals(productUnitId)) {
            return false;
        }

        if (detail.getBuyMinQuantity() != null && quantity < detail.getBuyMinQuantity()) {
            return false;
        }

        return true;
    }

    /**
     * Tạo dòng sản phẩm quà tặng
     */
    private CartItemResponseDTO createGiftItem(
            Long lineItemId,
            BuyXGetYDetail promotion,
            Integer buyQuantity,
            Long sourceLineItemId,
            Map<Long, BigDecimal> priceMap
    ) {
        ProductUnit giftProduct = promotion.getGiftProduct();
        BigDecimal giftPrice = priceMap.getOrDefault(giftProduct.getId(), BigDecimal.ZERO);

        int giftQuantity = calculateGiftQuantity(promotion, buyQuantity);

        BigDecimal discountValue = calculateGiftDiscount(
                promotion.getGiftDiscountType(),
                promotion.getGiftDiscountValue(),
                giftPrice
        );

        BigDecimal finalGiftPrice = giftPrice.subtract(discountValue);
        if (finalGiftPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalGiftPrice = BigDecimal.ZERO;
        }

        BigDecimal lineTotal = finalGiftPrice.multiply(BigDecimal.valueOf(giftQuantity));

        String discountTypeStr = mapDiscountType(promotion.getGiftDiscountType());
        // Với PERCENTAGE: hiển thị phần trăm
        // Với FIXED_AMOUNT/FREE: hiển thị tổng giá trị giảm (discountValue × số lượng)
        BigDecimal displayDiscountValue = promotion.getGiftDiscountType() == DiscountType.PERCENTAGE
                ? promotion.getGiftDiscountValue()
                : discountValue.multiply(BigDecimal.valueOf(giftQuantity));

        // Tạo mô tả ngắn gọn cho promotion
        String defaultDescription = "Mua " + promotion.getBuyMinQuantity() + " tặng " 
                + (promotion.getGiftQuantity() != null ? promotion.getGiftQuantity() : 1);

        PromotionAppliedDTO promotionApplied = new PromotionAppliedDTO(
                promotion.getPromotionLine().getPromotionCode(),
                promotion.getPromotionLine().getDescription() != null 
                    ? promotion.getPromotionLine().getDescription() 
                    : defaultDescription,
                promotion.getDetailId(),
                buildBuyXGetYSummary(promotion, giftQuantity),
                discountTypeStr,
                displayDiscountValue,
                sourceLineItemId
        );

        return new CartItemResponseDTO(
                lineItemId,
                giftProduct.getId(),
                giftProduct.getUnit().getName(),
                giftProduct.getProduct().getName(),
                giftQuantity,
                giftPrice,
                lineTotal,
                null,
                promotionApplied
        );
    }

    /**
     * Tính số lượng quà tặng
     * Công thức: MIN(số lần đủ điều kiện, số lần áp dụng tối đa) × số lượng tặng mỗi lần
     * 
     * @param promotion chi tiết khuyến mãi
     * @param buyQuantity số lượng sản phẩm khách mua
     * @return tổng số lượng sản phẩm tặng
     */
    private int calculateGiftQuantity(BuyXGetYDetail promotion, Integer buyQuantity) {
        if (promotion.getBuyMinQuantity() == null) {
            // Nếu không có điều kiện số lượng tối thiểu, trả về số lượng tặng mặc định
            return promotion.getGiftQuantity() != null ? promotion.getGiftQuantity() : 1;
        }

        // Tính số lần mua đủ điều kiện
        int eligibleSets = buyQuantity / promotion.getBuyMinQuantity();

        // Giới hạn số lần áp dụng (nếu có cấu hình)
        if (promotion.getGiftMaxQuantity() != null) {
            eligibleSets = Math.min(eligibleSets, promotion.getGiftMaxQuantity());
        }

        // Số lượng tặng cho mỗi lần đủ điều kiện (mặc định là 1)
        int giftQuantityPerSet = promotion.getGiftQuantity() != null ? promotion.getGiftQuantity() : 1;

        // Tổng số lượng tặng = số lần áp dụng × số lượng tặng mỗi lần
        return eligibleSets * giftQuantityPerSet;
    }

    /**
     * Tính giá trị giảm giá cho quà tặng
     */
    private BigDecimal calculateGiftDiscount(
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal originalPrice
    ) {
        if (discountType == DiscountType.FREE) {
            return originalPrice;
        }

        if (discountType == DiscountType.PERCENTAGE) {
            return originalPrice.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        if (discountType == DiscountType.FIXED_AMOUNT) {
            return discountValue;
        }

        return BigDecimal.ZERO;
    }

    /**
     * Map DiscountType sang string cho response
     */
    private String mapDiscountType(DiscountType discountType) {
        return switch (discountType) {
            case PERCENTAGE -> "percentage";
            case FIXED_AMOUNT -> "fixed";
            case FREE -> "percentage";
        };
    }

    /**
     * Tính tổng hợp giỏ hàng VÀ áp dụng ORDER_DISCOUNT (giảm giá toàn đơn)
     * ORDER_DISCOUNT áp dụng SAU khi đã trừ PRODUCT_DISCOUNT và BUY_X_GET_Y
     */
    private SummaryResult calculateSummaryWithOrderDiscount(List<CartItemResponseDTO> items) {
        // Bước 1: Tính tổng và giảm giá từ PRODUCT_DISCOUNT + BUY_X_GET_Y
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal lineItemDiscount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (CartItemResponseDTO item : items) {
            // Tính tổng số lượng
            if (item.promotionApplied() == null || item.promotionApplied().sourceLineItemId() == null) {
                totalQuantity += item.quantity();
            }

            // Dòng có promotionApplied và sourceLineItemId = null là dòng gốc có PRODUCT_DISCOUNT
            // Dòng có promotionApplied và sourceLineItemId != null là dòng quà tặng BUY_X_GET_Y
            if (item.promotionApplied() != null && item.promotionApplied().sourceLineItemId() == null) {
                // PRODUCT_DISCOUNT: giảm giá trên dòng gốc
                BigDecimal originalPrice = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                BigDecimal discountAmount = originalPrice.subtract(item.lineTotal());
                lineItemDiscount = lineItemDiscount.add(discountAmount);
                subTotal = subTotal.add(originalPrice);
            } else if (item.promotionApplied() != null && item.promotionApplied().sourceLineItemId() != null) {
                // BUY_X_GET_Y: dòng quà tặng
                BigDecimal originalPrice = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                BigDecimal discountAmount = originalPrice.subtract(item.lineTotal());
                lineItemDiscount = lineItemDiscount.add(discountAmount);
                subTotal = subTotal.add(originalPrice);
            } else {
                // Không có KM
                subTotal = subTotal.add(item.lineTotal());
            }
        }

        // Tổng sau khi trừ giảm giá dòng sản phẩm (PRODUCT_DISCOUNT + BUY_X_GET_Y)
        BigDecimal totalAfterLineDiscount = subTotal.subtract(lineItemDiscount);

        // Bước 2: Tìm và áp dụng ORDER_DISCOUNT (giảm giá toàn đơn)
        BigDecimal orderDiscount = BigDecimal.ZERO;
        List<CheckPromotionResponseDTO.OrderPromotionDTO> appliedOrderPromotions = new ArrayList<>();
        
        OrderDiscountDetail applicableOrderDiscount = findBestOrderDiscount(
                totalAfterLineDiscount,
                totalQuantity
        );

        if (applicableOrderDiscount != null) {
            orderDiscount = calculateOrderDiscountAmount(
                    applicableOrderDiscount,
                    totalAfterLineDiscount
            );
            
            log.info("Áp dụng ORDER_DISCOUNT: {} - Giảm {}đ", 
                    applicableOrderDiscount.getPromotionLine().getPromotionCode(),
                    orderDiscount);
            
            // Thêm thông tin ORDER_DISCOUNT vào danh sách
            CheckPromotionResponseDTO.OrderPromotionDTO orderPromotion = 
                    new CheckPromotionResponseDTO.OrderPromotionDTO(
                            applicableOrderDiscount.getPromotionLine().getPromotionCode(),
                            applicableOrderDiscount.getPromotionLine().getDescription() != null
                                ? applicableOrderDiscount.getPromotionLine().getDescription()
                                : "Giảm giá đơn hàng",
                            applicableOrderDiscount.getDetailId(),
                            buildOrderDiscountSummary(applicableOrderDiscount),
                            mapDiscountType(applicableOrderDiscount.getOrderDiscountType()),
                            applicableOrderDiscount.getOrderDiscountValue()
                    );
            appliedOrderPromotions.add(orderPromotion);
        }

        BigDecimal totalPayable = totalAfterLineDiscount.subtract(orderDiscount);

        CheckPromotionResponseDTO.SummaryDTO summary = new CheckPromotionResponseDTO.SummaryDTO(
                subTotal,
                orderDiscount,
                lineItemDiscount,
                totalPayable
        );

        return new SummaryResult(summary, appliedOrderPromotions);
    }

    /**
     * Record để trả về cả summary và danh sách ORDER_DISCOUNT đã áp dụng
     */
    private record SummaryResult(
            CheckPromotionResponseDTO.SummaryDTO summary,
            List<CheckPromotionResponseDTO.OrderPromotionDTO> appliedOrderPromotions
    ) {
    }

    // ============= PRODUCT_DISCOUNT METHODS =============

    /**
     * Tìm tất cả các khuyến mãi giảm giá sản phẩm đang active
     */
    private List<ProductDiscountDetail> findApplicableProductDiscounts() {
        LocalDateTime now = LocalDateTime.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.PRODUCT_DISCOUNT)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) && 
                               !now.isAfter(line.getHeader().getEndDate()))
                .filter(line -> isUsageLimitValid(line))
                .toList();

        List<ProductDiscountDetail> productDiscounts = new ArrayList<>();

        for (PromotionLine line : activeLines) {
            List<PromotionDetail> details = promotionDetailRepository
                    .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            for (PromotionDetail detail : details) {
                if (detail instanceof ProductDiscountDetail productDiscountDetail) {
                    productDiscounts.add(productDiscountDetail);
                }
            }
        }

        return productDiscounts;
    }

    /**
     * Tìm khuyến mãi giảm giá sản phẩm tốt nhất cho một sản phẩm
     * 
     * @param productDiscounts danh sách tất cả KM giảm giá đang active
     * @param productUnit sản phẩm cần kiểm tra
     * @param quantity số lượng
     * @param lineTotal tổng tiền dòng sản phẩm
     * @return KM tốt nhất hoặc null
     */
    private ProductDiscountDetail findBestProductDiscount(
            List<ProductDiscountDetail> productDiscounts,
            ProductUnit productUnit,
            Integer quantity,
            BigDecimal lineTotal
    ) {
        ProductDiscountDetail bestDiscount = null;
        BigDecimal maxDiscountAmount = BigDecimal.ZERO;

        for (ProductDiscountDetail discount : productDiscounts) {
            if (isProductDiscountApplicable(discount, productUnit, quantity, lineTotal)) {
                BigDecimal discountAmount = calculateProductDiscountAmount(discount, lineTotal);
                
                if (discountAmount.compareTo(maxDiscountAmount) > 0) {
                    maxDiscountAmount = discountAmount;
                    bestDiscount = discount;
                }
            }
        }

        return bestDiscount;
    }

    /**
     * Kiểm tra khuyến mãi giảm giá sản phẩm có áp dụng được không
     */
    private boolean isProductDiscountApplicable(
            ProductDiscountDetail discount,
            ProductUnit productUnit,
            Integer quantity,
            BigDecimal lineTotal
    ) {
        // Kiểm tra loại áp dụng
        switch (discount.getApplyToType()) {
            case ALL:
                // Áp dụng cho tất cả sản phẩm
                break;
            case PRODUCT:
                // Áp dụng cho sản phẩm cụ thể
                if (discount.getApplyToProduct() == null || 
                    !discount.getApplyToProduct().getId().equals(productUnit.getId())) {
                    return false;
                }
                break;
            default:
                return false;
        }

        // Kiểm tra số lượng tối thiểu
        if (discount.getProductMinPromotionQuantity() != null && 
            quantity < discount.getProductMinPromotionQuantity()) {
            return false;
        }

        // Kiểm tra giá trị sản phẩm tối thiểu
        if (discount.getProductMinPromotionValue() != null && 
            lineTotal.compareTo(discount.getProductMinPromotionValue()) < 0) {
            return false;
        }

        return true;
    }

    /**
     * Tính giá trị giảm giá cho sản phẩm
     * 
     * @param discount chi tiết KM giảm giá
     * @param lineTotal tổng tiền dòng sản phẩm
     * @return giá trị giảm giá
     */
    private BigDecimal calculateProductDiscountAmount(
            ProductDiscountDetail discount,
            BigDecimal lineTotal
    ) {
        if (discount.getProductDiscountType() == DiscountType.PERCENTAGE) {
            return lineTotal.multiply(discount.getProductDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (discount.getProductDiscountType() == DiscountType.FIXED_AMOUNT) {
            BigDecimal discountAmount = discount.getProductDiscountValue();
            // Giảm tối đa bằng giá trị sản phẩm
            return discountAmount.min(lineTotal);
        }
        
        return BigDecimal.ZERO;
    }

    // ============= ORDER_DISCOUNT METHODS =============

    /**
     * Tìm khuyến mãi giảm giá đơn hàng tốt nhất
     * 
     * @param totalAfterLineDiscount tổng đơn hàng sau khi trừ PRODUCT_DISCOUNT và BUY_X_GET_Y
     * @param totalQuantity tổng số lượng sản phẩm
     * @return KM tốt nhất hoặc null
     */
    private OrderDiscountDetail findBestOrderDiscount(
            BigDecimal totalAfterLineDiscount,
            Integer totalQuantity
    ) {
        LocalDateTime now = LocalDateTime.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.ORDER_DISCOUNT)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) && 
                               !now.isAfter(line.getHeader().getEndDate()))
                .filter(line -> isUsageLimitValid(line))
                .toList();

        OrderDiscountDetail bestDiscount = null;
        BigDecimal maxDiscountAmount = BigDecimal.ZERO;

        for (PromotionLine line : activeLines) {
            List<PromotionDetail> details = promotionDetailRepository
                    .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            for (PromotionDetail detail : details) {
                if (detail instanceof OrderDiscountDetail orderDiscountDetail) {
                    if (isOrderDiscountApplicable(orderDiscountDetail, totalAfterLineDiscount, totalQuantity)) {
                        BigDecimal discountAmount = calculateOrderDiscountAmount(
                                orderDiscountDetail,
                                totalAfterLineDiscount
                        );
                        
                        if (discountAmount.compareTo(maxDiscountAmount) > 0) {
                            maxDiscountAmount = discountAmount;
                            bestDiscount = orderDiscountDetail;
                        }
                    }
                }
            }
        }

        return bestDiscount;
    }

    /**
     * Kiểm tra khuyến mãi giảm giá đơn hàng có áp dụng được không
     */
    private boolean isOrderDiscountApplicable(
            OrderDiscountDetail discount,
            BigDecimal totalAfterLineDiscount,
            Integer totalQuantity
    ) {
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (discount.getOrderMinTotalValue() != null && 
            totalAfterLineDiscount.compareTo(discount.getOrderMinTotalValue()) < 0) {
            return false;
        }

        // Kiểm tra số lượng sản phẩm tối thiểu
        if (discount.getOrderMinTotalQuantity() != null && 
            totalQuantity < discount.getOrderMinTotalQuantity()) {
            return false;
        }

        return true;
    }

    /**
     * Tính giá trị giảm giá cho đơn hàng
     * 
     * @param discount chi tiết KM giảm giá đơn hàng
     * @param totalAfterLineDiscount tổng đơn hàng sau khi trừ giảm giá dòng
     * @return giá trị giảm giá
     */
    private BigDecimal calculateOrderDiscountAmount(
            OrderDiscountDetail discount,
            BigDecimal totalAfterLineDiscount
    ) {
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (discount.getOrderDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = totalAfterLineDiscount.multiply(discount.getOrderDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            // Giới hạn giảm tối đa (nếu có)
            if (discount.getOrderDiscountMaxValue() != null && 
                discountAmount.compareTo(discount.getOrderDiscountMaxValue()) > 0) {
                discountAmount = discount.getOrderDiscountMaxValue();
            }
        } else if (discount.getOrderDiscountType() == DiscountType.FIXED_AMOUNT) {
            discountAmount = discount.getOrderDiscountValue();
        }

        // Giảm tối đa bằng tổng đơn hàng (không âm)
        return discountAmount.min(totalAfterLineDiscount);
    }

    // ============= SUMMARY BUILDER METHODS =============

    /**
     * Tạo thông tin tóm tắt cho PRODUCT_DISCOUNT
     * 
     * @param discount chi tiết khuyến mãi giảm giá sản phẩm
     * @param productUnit sản phẩm được áp dụng
     * @return thông tin tóm tắt chi tiết
     */
    private String buildProductDiscountSummary(ProductDiscountDetail discount, ProductUnit productUnit) {
        StringBuilder summary = new StringBuilder();
        
        // Loại giảm giá
        if (discount.getProductDiscountType() == DiscountType.PERCENTAGE) {
            summary.append("Giảm ").append(discount.getProductDiscountValue()).append("%");
        } else {
            summary.append("Giảm ").append(String.format("%,.0f", discount.getProductDiscountValue())).append("đ");
        }
        
        // Áp dụng cho
        switch (discount.getApplyToType()) {
            case ALL:
                summary.append(" cho tất cả sản phẩm");
                break;
            case PRODUCT:
                if (discount.getApplyToProduct() != null) {
                    summary.append(" cho ").append(discount.getApplyToProduct().getProduct().getName());
                }
                break;
            default:
                summary.append(" cho sản phẩm");
                break;
        }
        
        // Điều kiện tối thiểu
        if (discount.getProductMinPromotionQuantity() != null) {
            summary.append(" (tối thiểu ").append(discount.getProductMinPromotionQuantity()).append(" sản phẩm)");
        } else if (discount.getProductMinPromotionValue() != null) {
            summary.append(" (tối thiểu ").append(String.format("%,.0f", discount.getProductMinPromotionValue())).append("đ)");
        }
        
        return summary.toString();
    }

    /**
     * Tạo thông tin tóm tắt cho BUY_X_GET_Y
     * 
     * @param promotion chi tiết khuyến mãi mua X tặng Y
     * @param actualGiftQuantity số lượng quà tặng thực tế được áp dụng
     * @return thông tin tóm tắt chi tiết
     */
    private String buildBuyXGetYSummary(BuyXGetYDetail promotion, int actualGiftQuantity) {
        StringBuilder summary = new StringBuilder();
        
        // Điều kiện mua
        summary.append("Mua ");
        if (promotion.getBuyMinQuantity() != null) {
            summary.append(promotion.getBuyMinQuantity()).append(" ");
        }
        if (promotion.getBuyProduct() != null) {
            summary.append(promotion.getBuyProduct().getProduct().getName());
        }
        
        // Quà tặng - hiển thị số lượng theo cấu hình
        summary.append(" tặng ");
        if (promotion.getGiftQuantity() != null && promotion.getGiftQuantity() > 1) {
            // Hiển thị số lượng tặng theo cấu hình nếu > 1
            summary.append(promotion.getGiftQuantity()).append(" ");
        }
        if (promotion.getGiftProduct() != null) {
            summary.append(promotion.getGiftProduct().getProduct().getName());
        }
        
        // Hiển thị số lượng thực tế nếu khác với cấu hình cơ bản
        if (actualGiftQuantity != promotion.getGiftQuantity()) {
            summary.append(" (").append(actualGiftQuantity).append(" sản phẩm)");
        }
        
        // Loại giảm giá quà tặng
        if (promotion.getGiftDiscountType() == DiscountType.FREE) {
            summary.append(" (miễn phí)");
        } else if (promotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
            summary.append(" (giảm ").append(promotion.getGiftDiscountValue()).append("%)");
        } else if (promotion.getGiftDiscountType() == DiscountType.FIXED_AMOUNT) {
            summary.append(" (giảm ").append(String.format("%,.0f", promotion.getGiftDiscountValue())).append("đ)");
        }
        
        return summary.toString();
    }

    /**
     * Tạo thông tin tóm tắt cho ORDER_DISCOUNT
     * 
     * @param discount chi tiết khuyến mãi giảm giá đơn hàng
     * @return thông tin tóm tắt chi tiết
     */
    private String buildOrderDiscountSummary(OrderDiscountDetail discount) {
        StringBuilder summary = new StringBuilder();
        
        // Loại giảm giá
        if (discount.getOrderDiscountType() == DiscountType.PERCENTAGE) {
            summary.append("Giảm ").append(discount.getOrderDiscountValue()).append("%");
            if (discount.getOrderDiscountMaxValue() != null) {
                summary.append(" (tối đa ").append(String.format("%,.0f", discount.getOrderDiscountMaxValue())).append("đ)");
            }
        } else {
            summary.append("Giảm ").append(String.format("%,.0f", discount.getOrderDiscountValue())).append("đ");
        }
        
        summary.append(" cho đơn hàng");
        
        // Điều kiện tối thiểu
        List<String> conditions = new ArrayList<>();
        if (discount.getOrderMinTotalValue() != null) {
            conditions.add("từ " + String.format("%,.0f", discount.getOrderMinTotalValue()) + "đ");
        }
        if (discount.getOrderMinTotalQuantity() != null) {
            conditions.add("từ " + discount.getOrderMinTotalQuantity() + " sản phẩm");
        }
        
        if (!conditions.isEmpty()) {
            summary.append(" (").append(String.join(" và ", conditions)).append(")");
        }
        
        return summary.toString();
    }
}
