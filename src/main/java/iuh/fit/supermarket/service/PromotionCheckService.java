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
import java.time.LocalDate;
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
        // Tải giá ban đầu cho các sản phẩm trong request
        Map<Long, BigDecimal> priceMap = loadPrices(productUnitMap.keySet());

        // Tải tất cả khuyến mãi giảm giá sản phẩm đang active
        List<ProductDiscountDetail> productDiscounts = findApplicableProductDiscounts();

        // Tải tất cả khuyến mãi BuyXGetY đang active (để kiểm tra gift product)
        List<BuyXGetYDetail> allBuyXGetYPromotions = findAllActiveBuyXGetYPromotions();

        List<CartItemResponseDTO> resultItems = new ArrayList<>();
        Long lineItemId = 1L;

        // Map để lưu productUnitId → lineItemId (dùng để tìm sourceLineItemId cho gift product)
        Map<Long, Long> productUnitIdToLineItemId = new HashMap<>();

        for (CartItemRequestDTO item : request.items()) {
            ProductUnit productUnit = productUnitMap.get(item.productUnitId());
            if (productUnit == null) {
                throw new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + item.productUnitId());
            }

            BigDecimal unitPrice = priceMap.getOrDefault(item.productUnitId(), BigDecimal.ZERO);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));

            // Lưu lineItemId hiện tại cho productUnitId này
            Long currentLineItemId = lineItemId;
            productUnitIdToLineItemId.put(item.productUnitId(), currentLineItemId);

            // 1. Kiểm tra PRODUCT_DISCOUNT (giảm giá trực tiếp)
            ProductDiscountDetail applicableDiscount = findBestProductDiscount(
                    productDiscounts,
                    productUnit,
                    item.quantity(),
                    lineTotal
            );

            PromotionAppliedDTO promotionApplied = null;
            if (applicableDiscount != null) {
                BigDecimal discountAmount = calculateProductDiscountAmount(
                        applicableDiscount,
                        lineTotal
                );
                lineTotal = lineTotal.subtract(discountAmount);

                promotionApplied = new PromotionAppliedDTO(
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

            // 2. Kiểm tra xem item hiện tại có phải là GIFT PRODUCT của promotion giảm giá không
            // (PERCENTAGE/FIXED_AMOUNT - khách tự thêm vào giỏ)
            boolean hasGiftDiscountPromotion = false;
            if (promotionApplied == null) {
                BuyXGetYDetail giftPromotion = findApplicableGiftDiscount(
                        allBuyXGetYPromotions,
                        productUnit.getId(),
                        item.quantity(),
                        request.items(),
                        productUnitMap
                );

                if (giftPromotion != null) {
                    hasGiftDiscountPromotion = true;

                    // Tìm sourceLineItemId từ buy product
                    Long buyProductId = giftPromotion.getBuyProduct().getId();
                    Long sourceLineItemId = productUnitIdToLineItemId.get(buyProductId);

                    // Kiểm tra xem gift product có GIỐNG buy product không
                    boolean isSameProduct = giftPromotion.getBuyProduct().getId()
                            .equals(giftPromotion.getGiftProduct().getId());

                    if (isSameProduct) {
                        // Trường hợp đặc biệt: Gift product GIỐNG buy product
                        // Chỉ áp dụng giảm giá cho phần vượt quá buyMinQuantity
                        int buyMinQty = giftPromotion.getBuyMinQuantity() != null
                                ? giftPromotion.getBuyMinQuantity() : 0;

                        // Tính số lượng gift có thể nhận (phần vượt quá buyMinQuantity)
                        int giftQty = calculateGiftQuantityForSameProduct(
                                item.quantity(),
                                buyMinQty,
                                giftPromotion.getGiftQuantity(),
                                giftPromotion.getGiftMaxQuantity()
                        );

                        if (giftQty > 0) {
                            // Tính số lượng tối đa được áp dụng khuyến mãi
                            int giftQtyPerSet = (giftPromotion.getGiftQuantity() != null && giftPromotion.getGiftQuantity() > 0)
                                    ? giftPromotion.getGiftQuantity() : 1;

                            // Tính số lượng tối đa có thể được giảm giá dựa trên giftMaxQuantity (số lần)
                            // Ví dụ: Mua 5 tặng 2, tối đa 3 lần
                            // → maxPromotionQuantity = (5 + 2) × 3 = 21 sản phẩm
                            int maxSets = (giftPromotion.getGiftMaxQuantity() != null && giftPromotion.getGiftMaxQuantity() > 0)
                                    ? giftPromotion.getGiftMaxQuantity()
                                    : Integer.MAX_VALUE;

                            int maxPromotionQuantity;
                            if (maxSets == Integer.MAX_VALUE) {
                                maxPromotionQuantity = Integer.MAX_VALUE;
                            } else {
                                maxPromotionQuantity = (buyMinQty + giftQtyPerSet) * maxSets;
                            }

                            int maxGiftQuantity = maxSets == Integer.MAX_VALUE ? Integer.MAX_VALUE : maxSets * giftQtyPerSet;

                            // Kiểm tra xem có vượt quá giới hạn không
                            if (item.quantity() > maxPromotionQuantity) {
                                // VƯỢT QUÁ GIỚI HẠN: Tách thành 2 line items
                                int promotionQuantity = maxPromotionQuantity;  // Số lượng được KM
                                int excessQuantity = item.quantity() - maxPromotionQuantity;  // Số lượng vượt quá

                                // Tính discount cho từng gift product
                                BigDecimal discountAmount = calculateGiftDiscount(
                                        giftPromotion.getGiftDiscountType(),
                                        giftPromotion.getGiftDiscountValue(),
                                        unitPrice
                                );

                                BigDecimal discountedPrice = unitPrice.subtract(discountAmount);
                                if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
                                    discountedPrice = BigDecimal.ZERO;
                                }

                                // Line 1: Phần được khuyến mãi
                                int buyQtyWithPromotion = promotionQuantity - maxGiftQuantity;
                                BigDecimal buyTotal = unitPrice.multiply(BigDecimal.valueOf(buyQtyWithPromotion));
                                BigDecimal giftTotal = discountedPrice.multiply(BigDecimal.valueOf(maxGiftQuantity));
                                BigDecimal lineTotalWithPromotion = buyTotal.add(giftTotal);

                                // Tính tổng discount amount cho display
                                BigDecimal totalDiscountAmount = discountAmount.multiply(BigDecimal.valueOf(maxGiftQuantity));

                                // Hiển thị discount value
                                BigDecimal displayDiscountValue;
                                if (giftPromotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
                                    displayDiscountValue = giftPromotion.getGiftDiscountValue();
                                } else {
                                    displayDiscountValue = totalDiscountAmount;
                                }

                                PromotionAppliedDTO promotionAppliedDTO = new PromotionAppliedDTO(
                                        giftPromotion.getPromotionLine().getPromotionCode(),
                                        giftPromotion.getPromotionLine().getDescription() != null
                                            ? giftPromotion.getPromotionLine().getDescription()
                                            : "Mua " + giftPromotion.getBuyMinQuantity() + " tặng " +
                                              (giftPromotion.getGiftQuantity() != null ? giftPromotion.getGiftQuantity() : 1),
                                        giftPromotion.getDetailId(),
                                        buildBuyXGetYDiscountSummary(giftPromotion),
                                        mapDiscountType(giftPromotion.getGiftDiscountType()),
                                        displayDiscountValue,
                                        sourceLineItemId
                                );

                                CartItemResponseDTO cartItemWithPromotion = new CartItemResponseDTO(
                                        lineItemId++,
                                        productUnit.getId(),
                                        productUnit.getUnit().getName(),
                                        productUnit.getProduct().getName(),
                                        promotionQuantity,
                                        unitPrice,
                                        lineTotalWithPromotion,
                                        true,
                                        promotionAppliedDTO
                                );
                                resultItems.add(cartItemWithPromotion);

                                // Line 2: Phần vượt quá không có khuyến mãi
                                BigDecimal lineTotalExcess = unitPrice.multiply(BigDecimal.valueOf(excessQuantity));

                                CartItemResponseDTO cartItemExcess = new CartItemResponseDTO(
                                        lineItemId++,
                                        productUnit.getId(),
                                        productUnit.getUnit().getName(),
                                        productUnit.getProduct().getName(),
                                        excessQuantity,
                                        unitPrice,
                                        lineTotalExcess,
                                        false,
                                        null
                                );
                                resultItems.add(cartItemExcess);

                                // Bỏ qua logic bên dưới vì đã xử lý xong - tiếp tục với item tiếp theo
                                continue;
                            } else {
                                // KHÔNG VƯỢT QUÁ: Logic cũ
                                // Tính discount cho từng gift product
                                BigDecimal discountAmount = calculateGiftDiscount(
                                        giftPromotion.getGiftDiscountType(),
                                        giftPromotion.getGiftDiscountValue(),
                                        unitPrice
                                );

                                BigDecimal discountedPrice = unitPrice.subtract(discountAmount);
                                if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
                                    discountedPrice = BigDecimal.ZERO;
                                }

                                // Tính lineTotal: (buy quantity * unitPrice) + (gift quantity * discountedPrice)
                                int buyQty = item.quantity() - giftQty;
                                BigDecimal buyTotal = unitPrice.multiply(BigDecimal.valueOf(buyQty));
                                BigDecimal giftTotal = discountedPrice.multiply(BigDecimal.valueOf(giftQty));
                                lineTotal = buyTotal.add(giftTotal);

                                // Tính tổng discount amount cho display
                                BigDecimal totalDiscountAmount = discountAmount.multiply(BigDecimal.valueOf(giftQty));

                                // Hiển thị discount value
                                BigDecimal displayDiscountValue;
                                if (giftPromotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
                                    displayDiscountValue = giftPromotion.getGiftDiscountValue();
                                } else {
                                    displayDiscountValue = totalDiscountAmount;
                                }

                                promotionApplied = new PromotionAppliedDTO(
                                        giftPromotion.getPromotionLine().getPromotionCode(),
                                        giftPromotion.getPromotionLine().getDescription() != null
                                            ? giftPromotion.getPromotionLine().getDescription()
                                            : "Mua " + giftPromotion.getBuyMinQuantity() + " tặng " +
                                              (giftPromotion.getGiftQuantity() != null ? giftPromotion.getGiftQuantity() : 1),
                                        giftPromotion.getDetailId(),
                                        buildBuyXGetYDiscountSummary(giftPromotion),
                                        mapDiscountType(giftPromotion.getGiftDiscountType()),
                                        displayDiscountValue,
                                        sourceLineItemId  // Gán sourceLineItemId cho gift product
                                );
                            }
                        }
                    } else {
                        // Trường hợp bình thường: Gift product KHÁC buy product
                        // Tính số lượng tối đa được áp dụng khuyến mãi
                        int giftQtyPerSet = (giftPromotion.getGiftQuantity() != null && giftPromotion.getGiftQuantity() > 0)
                                ? giftPromotion.getGiftQuantity() : 1;
                        int maxGiftQuantity = (giftPromotion.getGiftMaxQuantity() != null && giftPromotion.getGiftMaxQuantity() > 0)
                                ? giftPromotion.getGiftMaxQuantity() * giftQtyPerSet
                                : Integer.MAX_VALUE;

                        // Kiểm tra xem có vượt quá giới hạn không
                        if (item.quantity() > maxGiftQuantity) {
                            // VƯỢT QUÁ GIỚI HẠN: Tách thành 2 line items
                            int promotionQuantity = maxGiftQuantity;  // Số lượng được KM
                            int excessQuantity = item.quantity() - maxGiftQuantity;  // Số lượng vượt quá

                            // Tính discount
                            BigDecimal discountAmount = calculateGiftDiscount(
                                    giftPromotion.getGiftDiscountType(),
                                    giftPromotion.getGiftDiscountValue(),
                                    unitPrice
                            );

                            BigDecimal discountedPrice = unitPrice.subtract(discountAmount);
                            if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
                                discountedPrice = BigDecimal.ZERO;
                            }

                            // Line 1: Phần được khuyến mãi
                            BigDecimal lineTotalWithPromotion = discountedPrice.multiply(BigDecimal.valueOf(promotionQuantity));

                            // Hiển thị discount value
                            BigDecimal displayDiscountValue;
                            if (giftPromotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
                                displayDiscountValue = giftPromotion.getGiftDiscountValue();
                            } else {
                                displayDiscountValue = discountAmount.multiply(BigDecimal.valueOf(promotionQuantity));
                            }

                            PromotionAppliedDTO promotionAppliedDTO = new PromotionAppliedDTO(
                                    giftPromotion.getPromotionLine().getPromotionCode(),
                                    giftPromotion.getPromotionLine().getDescription() != null
                                        ? giftPromotion.getPromotionLine().getDescription()
                                        : "Mua " + giftPromotion.getBuyMinQuantity() + " tặng " +
                                          (giftPromotion.getGiftQuantity() != null ? giftPromotion.getGiftQuantity() : 1),
                                    giftPromotion.getDetailId(),
                                    buildBuyXGetYDiscountSummary(giftPromotion),
                                    mapDiscountType(giftPromotion.getGiftDiscountType()),
                                    displayDiscountValue,
                                    sourceLineItemId
                            );

                            CartItemResponseDTO cartItemWithPromotion = new CartItemResponseDTO(
                                    lineItemId++,
                                    productUnit.getId(),
                                    productUnit.getUnit().getName(),
                                    productUnit.getProduct().getName(),
                                    promotionQuantity,
                                    unitPrice,
                                    lineTotalWithPromotion,
                                    true,
                                    promotionAppliedDTO
                            );
                            resultItems.add(cartItemWithPromotion);

                            // Line 2: Phần vượt quá không có khuyến mãi
                            BigDecimal lineTotalExcess = unitPrice.multiply(BigDecimal.valueOf(excessQuantity));

                            CartItemResponseDTO cartItemExcess = new CartItemResponseDTO(
                                    lineItemId++,
                                    productUnit.getId(),
                                    productUnit.getUnit().getName(),
                                    productUnit.getProduct().getName(),
                                    excessQuantity,
                                    unitPrice,
                                    lineTotalExcess,
                                    false,
                                    null
                            );
                            resultItems.add(cartItemExcess);

                            // Bỏ qua logic bên dưới vì đã xử lý xong - tiếp tục với item tiếp theo
                            continue;
                        } else {
                            // KHÔNG VƯỢT QUÁ: Áp dụng giảm giá cho TẤT CẢ quantity
                            BigDecimal discountAmount = calculateGiftDiscount(
                                    giftPromotion.getGiftDiscountType(),
                                    giftPromotion.getGiftDiscountValue(),
                                    unitPrice
                            );

                            BigDecimal discountedPrice = unitPrice.subtract(discountAmount);
                            if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
                                discountedPrice = BigDecimal.ZERO;
                            }

                            lineTotal = discountedPrice.multiply(BigDecimal.valueOf(item.quantity()));

                            // Hiển thị discount value
                            BigDecimal displayDiscountValue;
                            if (giftPromotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
                                displayDiscountValue = giftPromotion.getGiftDiscountValue();
                            } else {
                                displayDiscountValue = discountAmount.multiply(BigDecimal.valueOf(item.quantity()));
                            }

                            promotionApplied = new PromotionAppliedDTO(
                                    giftPromotion.getPromotionLine().getPromotionCode(),
                                    giftPromotion.getPromotionLine().getDescription() != null
                                        ? giftPromotion.getPromotionLine().getDescription()
                                        : "Mua " + giftPromotion.getBuyMinQuantity() + " tặng " +
                                          (giftPromotion.getGiftQuantity() != null ? giftPromotion.getGiftQuantity() : 1),
                                    giftPromotion.getDetailId(),
                                    buildBuyXGetYDiscountSummary(giftPromotion),
                                    mapDiscountType(giftPromotion.getGiftDiscountType()),
                                    displayDiscountValue,
                                    sourceLineItemId  // Gán sourceLineItemId cho gift product
                            );
                        }
                    }
                }
            }

            // 3. Kiểm tra BUY_X_GET_Y (mua X tặng Y - CHỈ FREE GIFTS)
            List<BuyXGetYDetail> applicableFreeGifts = findApplicableFreeGifts(
                    productUnit.getId(),
                    item.quantity()
            );

            boolean hasFreeGiftPromotion = !applicableFreeGifts.isEmpty();

            // hasPromotion = true nếu có FREE gift hoặc có gift discount promotion
            boolean hasPromotion = hasFreeGiftPromotion || hasGiftDiscountPromotion;

            CartItemResponseDTO cartItem = new CartItemResponseDTO(
                    lineItemId++,
                    productUnit.getId(),
                    productUnit.getUnit().getName(),
                    productUnit.getProduct().getName(),
                    item.quantity(),
                    unitPrice,
                    lineTotal,
                    hasPromotion,
                    promotionApplied
            );
            resultItems.add(cartItem);

            // Thêm dòng quà tặng MIỄN PHÍ (tự động)
            if (hasFreeGiftPromotion) {
                for (BuyXGetYDetail promotion : applicableFreeGifts) {
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
     * Nếu có nhiều giá ACTIVE, lấy giá từ bảng giá có ngày tạo mới nhất
     */
    private Map<Long, BigDecimal> loadPrices(Set<Long> productUnitIds) {
        Map<Long, BigDecimal> priceMap = new HashMap<>();

        for (Long productUnitId : productUnitIds) {
            // Sử dụng phương thức trả về List để xử lý trường hợp có nhiều giá
            List<PriceDetail> priceDetails = priceDetailRepository.findByProductUnitIdAndPriceStatus(
                    productUnitId, iuh.fit.supermarket.enums.PriceType.ACTIVE);
            
            if (!priceDetails.isEmpty()) {
                // Lấy giá từ bảng giá có ngày tạo mới nhất
                PriceDetail latestPriceDetail = priceDetails.stream()
                        .max((pd1, pd2) -> pd1.getPrice().getCreatedAt().compareTo(pd2.getPrice().getCreatedAt()))
                        .orElse(priceDetails.get(0));
                
                priceMap.put(productUnitId, latestPriceDetail.getSalePrice());
            }
        }

        return priceMap;
    }

    /**
     * Tính số lượng gift product khi gift product GIỐNG buy product
     * Ví dụ: Mua 5 tặng 2, giftMaxQuantity = 3 (giới hạn 3 lần)
     * - quantity = 5 → giftQty = 0 (chưa có gift)
     * - quantity = 7 → giftQty = 2 (5 buy + 2 gift, 1 lần)
     * - quantity = 14 → giftQty = 4 (10 buy + 4 gift, 2 lần)
     * - quantity = 35 → giftQty = 6 (25 buy + 6 gift, giới hạn 3 lần × 2 = 6 sản phẩm)
     *
     * @param totalQuantity tổng số lượng sản phẩm trong giỏ
     * @param buyMinQuantity số lượng tối thiểu phải mua
     * @param giftQuantity số lượng gift nhận được mỗi lần
     * @param giftMaxQuantity giới hạn SỐ LẦN áp dụng tối đa (null = không giới hạn)
     * @return số lượng gift product
     */
    private int calculateGiftQuantityForSameProduct(int totalQuantity, int buyMinQuantity,
                                                     Integer giftQuantity, Integer giftMaxQuantity) {
        if (buyMinQuantity <= 0) {
            return 0;
        }

        int giftQty = (giftQuantity != null && giftQuantity > 0) ? giftQuantity : 1;

        // Nếu tổng số lượng <= số lượng mua tối thiểu → chưa có gift
        if (totalQuantity <= buyMinQuantity) {
            return 0;
        }

        // Tính số lượng gift dựa trên phần vượt quá buyMinQuantity
        // Công thức: số lần nhận gift = (totalQuantity) / (buyMinQuantity + giftQty)
        int cycleSize = buyMinQuantity + giftQty;
        int completeCycles = totalQuantity / cycleSize;
        int remainder = totalQuantity % cycleSize;

        // Tính số lần áp dụng
        int eligibleSets = completeCycles;

        // Nếu phần dư > buyMinQuantity → có thêm 1 lần áp dụng nữa
        if (remainder > buyMinQuantity) {
            eligibleSets++;
        }

        // Áp dụng giới hạn SỐ LẦN (giftMaxQuantity) nếu có
        if (giftMaxQuantity != null && giftMaxQuantity > 0) {
            eligibleSets = Math.min(eligibleSets, giftMaxQuantity);
        }

        // Tổng số lượng gift = số lần áp dụng × số lượng gift mỗi lần
        return eligibleSets * giftQty;
    }

    /**
     * Tìm các khuyến mãi Mua X Tặng Y (chỉ FREE gifts - tự động thêm vào giỏ)
     * Chỉ trả về các promotion có giftDiscountType = FREE
     */
    private List<BuyXGetYDetail> findApplicableFreeGifts(Long productUnitId, Integer quantity) {
        LocalDate now = LocalDate.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.BUY_X_GET_Y)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) &&
                               !now.isAfter(line.getHeader().getEndDate()))
                .toList();

        List<BuyXGetYDetail> applicablePromotions = new ArrayList<>();

        for (PromotionLine line : activeLines) {
            List<PromotionDetail> details = promotionDetailRepository
                    .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            for (PromotionDetail detail : details) {
                if (detail instanceof BuyXGetYDetail buyXGetYDetail) {
                    // Chỉ lấy promotion có giftDiscountType = FREE (tự động tặng)
                    if (buyXGetYDetail.getGiftDiscountType() == DiscountType.FREE &&
                        isBuyXGetYApplicable(buyXGetYDetail, productUnitId, quantity)) {
                        applicablePromotions.add(buyXGetYDetail);
                    }
                }
            }
        }

        return applicablePromotions;
    }

    /**
     * Tìm tất cả các khuyến mãi Mua X Tặng Y đang active (bao gồm cả FREE và DISCOUNTED)
     * Dùng để kiểm tra xem item hiện tại có phải là gift product không
     */
    private List<BuyXGetYDetail> findAllActiveBuyXGetYPromotions() {
        LocalDate now = LocalDate.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.BUY_X_GET_Y)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) &&
                               !now.isAfter(line.getHeader().getEndDate()))
                .toList();

        List<BuyXGetYDetail> allPromotions = new ArrayList<>();

        for (PromotionLine line : activeLines) {
            List<PromotionDetail> details = promotionDetailRepository
                    .findByPromotionLine_PromotionLineId(line.getPromotionLineId());

            for (PromotionDetail detail : details) {
                if (detail instanceof BuyXGetYDetail buyXGetYDetail) {
                    allPromotions.add(buyXGetYDetail);
                }
            }
        }

        return allPromotions;
    }

    /**
     * Tìm promotion giảm giá áp dụng cho gift product (khi khách tự thêm vào giỏ)
     * Chỉ áp dụng cho promotion có giftDiscountType != FREE (PERCENTAGE/FIXED_AMOUNT)
     *
     * @param allPromotions danh sách tất cả promotion BuyXGetY đang active
     * @param currentProductUnitId ID của sản phẩm hiện tại (có thể là gift product)
     * @param currentQuantity số lượng sản phẩm hiện tại
     * @param allItems tất cả items trong giỏ hàng
     * @param productUnitMap map chứa thông tin ProductUnit
     * @return promotion tốt nhất hoặc null
     */
    private BuyXGetYDetail findApplicableGiftDiscount(
            List<BuyXGetYDetail> allPromotions,
            Long currentProductUnitId,
            Integer currentQuantity,
            List<CartItemRequestDTO> allItems,
            Map<Long, ProductUnit> productUnitMap
    ) {
        BuyXGetYDetail bestPromotion = null;
        BigDecimal maxDiscountAmount = BigDecimal.ZERO;

        for (BuyXGetYDetail promotion : allPromotions) {
            // Chỉ xét promotion có giảm giá (không phải FREE)
            if (promotion.getGiftDiscountType() == DiscountType.FREE) {
                continue;
            }

            // Kiểm tra xem currentProductUnitId có phải là giftProduct không
            if (promotion.getGiftProduct() == null ||
                !promotion.getGiftProduct().getId().equals(currentProductUnitId)) {
                continue;
            }

            Long buyProductId = promotion.getBuyProduct().getId();
            Integer buyMinQuantity = promotion.getBuyMinQuantity() != null ? promotion.getBuyMinQuantity() : 0;

            // Kiểm tra xem gift product có GIỐNG buy product không
            boolean isSameProduct = buyProductId.equals(currentProductUnitId);

            boolean hasValidPromotion = false;

            if (isSameProduct) {
                // Trường hợp ĐẶC BIỆT: Gift product GIỐNG buy product
                // Chỉ áp dụng khi currentQuantity > buyMinQuantity
                // Ví dụ: Mua 5 tặng 1 giảm 10%
                // - quantity = 5 → KHÔNG áp dụng (chỉ có buy, chưa có gift)
                // - quantity = 6 → ÁP DỤNG (5 buy + 1 gift)
                if (currentQuantity > buyMinQuantity) {
                    hasValidPromotion = true;
                }
            } else {
                // Trường hợp BÌNH THƯỜNG: Gift product KHÁC buy product
                // Kiểm tra trong giỏ hàng có buyProduct đủ số lượng không
                boolean hasSufficientBuyProduct = allItems.stream()
                        .anyMatch(item ->
                                item.productUnitId().equals(buyProductId) &&
                                item.quantity() >= buyMinQuantity
                        );

                if (hasSufficientBuyProduct) {
                    hasValidPromotion = true;
                }
            }

            if (!hasValidPromotion) {
                continue;
            }

            // Tính discount amount để so sánh
            // Tải giá của gift product
            List<PriceDetail> priceDetails = priceDetailRepository.findByProductUnitIdAndPriceStatus(
                    currentProductUnitId, iuh.fit.supermarket.enums.PriceType.ACTIVE);

            if (!priceDetails.isEmpty()) {
                BigDecimal giftPrice = priceDetails.stream()
                        .max((pd1, pd2) -> pd1.getPrice().getCreatedAt().compareTo(pd2.getPrice().getCreatedAt()))
                        .orElse(priceDetails.get(0))
                        .getSalePrice();

                BigDecimal discountAmount = calculateGiftDiscount(
                        promotion.getGiftDiscountType(),
                        promotion.getGiftDiscountValue(),
                        giftPrice
                );

                // Chọn promotion có discount lớn nhất
                if (discountAmount.compareTo(maxDiscountAmount) > 0) {
                    maxDiscountAmount = discountAmount;
                    bestPromotion = promotion;
                }
            }
        }

        return bestPromotion;
    }



    /**
     * Kiểm tra xem khuyến mãi Mua X Tặng Y có áp dụng được không
     * Lưu ý: quantity là số lượng mua thực tế, không bao gồm quà tặng
     * Ví dụ: Mua 5 tặng 1 thì quantity chỉ cần >= 5
     */
    private boolean isBuyXGetYApplicable(BuyXGetYDetail detail, Long productUnitId, Integer quantity) {
        if (detail.getBuyProduct() == null || !detail.getBuyProduct().getId().equals(productUnitId)) {
            return false;
        }

        if (detail.getBuyMinQuantity() != null) {
            // Chỉ cần kiểm tra số lượng mua tối thiểu, không cần cộng thêm số lượng tặng
            if (quantity < detail.getBuyMinQuantity()) {
                return false;
            }
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
        // Đảm bảo có giá cho quà tặng, nếu chưa có thì tải từ database
        BigDecimal giftPrice = priceMap.get(giftProduct.getId());
        if (giftPrice == null) {
            // Tải giá cho quà tặng nếu chưa có trong priceMap
            List<PriceDetail> giftPriceDetails = priceDetailRepository.findByProductUnitIdAndPriceStatus(
                    giftProduct.getId(), iuh.fit.supermarket.enums.PriceType.ACTIVE);
            
            if (!giftPriceDetails.isEmpty()) {
                // Lấy giá từ bảng giá có ngày tạo mới nhất
                giftPrice = giftPriceDetails.stream()
                        .max((pd1, pd2) -> pd1.getPrice().getCreatedAt().compareTo(pd2.getPrice().getCreatedAt()))
                        .orElse(giftPriceDetails.get(0))
                        .getSalePrice();
                
                // Thêm vào priceMap để dùng lại sau
                priceMap.put(giftProduct.getId(), giftPrice);
            } else {
                giftPrice = BigDecimal.ZERO;
            }
        }

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
        // Với FIXED_AMOUNT: hiển thị tổng giá trị giảm (discountValue × số lượng)
        // Với FREE: hiển thị 100 (miễn phí 100%)
        BigDecimal displayDiscountValue;
        if (promotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
            displayDiscountValue = promotion.getGiftDiscountValue();
        } else if (promotion.getGiftDiscountType() == DiscountType.FREE) {
            displayDiscountValue = BigDecimal.valueOf(100); // Miễn phí 100%
        } else {
            displayDiscountValue = discountValue.multiply(BigDecimal.valueOf(giftQuantity));
        }

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
     * Tính số lượng gift product cho FREE gifts (tự động thêm)
     * Ví dụ: Mua 5 tặng 2, giftMaxQuantity = 3 (giới hạn 3 lần)
     * - Mua 5 → Tặng 2 (1 lần)
     * - Mua 10 → Tặng 4 (2 lần)
     * - Mua 20 → Tặng 6 (giới hạn 3 lần × 2 = 6 sản phẩm)
     *
     * @param promotion chi tiết khuyến mãi
     * @param buyQuantity số lượng sản phẩm khách mua (không bao gồm quà tặng)
     * @return tổng số lượng sản phẩm tặng
     */
    private int calculateGiftQuantity(BuyXGetYDetail promotion, Integer buyQuantity) {
        if (promotion.getBuyMinQuantity() == null) {
            // Nếu không có điều kiện số lượng tối thiểu, trả về số lượng tặng mặc định
            int defaultQty = promotion.getGiftQuantity() != null ? promotion.getGiftQuantity() : 1;
            // Áp dụng giới hạn nếu có (trong trường hợp này giftMaxQuantity = số lần = 1)
            if (promotion.getGiftMaxQuantity() != null && promotion.getGiftMaxQuantity() > 0) {
                // Nếu giftMaxQuantity = 0 thì không tặng
                return promotion.getGiftMaxQuantity() >= 1 ? defaultQty : 0;
            }
            return defaultQty;
        }

        // Số lượng tặng cho mỗi lần đủ điều kiện (mặc định là 1)
        int giftQuantityPerSet = promotion.getGiftQuantity() != null ? promotion.getGiftQuantity() : 1;

        // Tính số lần mua đủ điều kiện dựa trên số lượng mua thực tế
        int eligibleSets = buyQuantity / promotion.getBuyMinQuantity();

        // Áp dụng giới hạn SỐ LẦN (giftMaxQuantity) nếu có
        if (promotion.getGiftMaxQuantity() != null && promotion.getGiftMaxQuantity() > 0) {
            eligibleSets = Math.min(eligibleSets, promotion.getGiftMaxQuantity());
        }

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
            // Tính tổng số lượng (chỉ tính sản phẩm mua, không tính quà tặng)
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
                // BUY_X_GET_Y: dòng quà tặng - chỉ tính vào giảm giá, không tính vào subTotal
                BigDecimal originalPrice = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
                BigDecimal discountAmount = originalPrice.subtract(item.lineTotal());
                lineItemDiscount = lineItemDiscount.add(discountAmount);
                // Không cộng vào subTotal vì quà tặng không phải là sản phẩm mua
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
        LocalDate now = LocalDate.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.PRODUCT_DISCOUNT)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) &&
                               !now.isAfter(line.getHeader().getEndDate()))
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
        LocalDate now = LocalDate.now();

        List<PromotionLine> activeLines = promotionLineRepository.findAll().stream()
                .filter(line -> line.getPromotionType() == PromotionType.ORDER_DISCOUNT)
                .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getStartDate()) && !now.isAfter(line.getEndDate()))
                .filter(line -> line.getHeader().getStatus() == PromotionStatus.ACTIVE)
                .filter(line -> !now.isBefore(line.getHeader().getStartDate()) &&
                               !now.isAfter(line.getHeader().getEndDate()))
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
     * Tạo thông tin tóm tắt cho BUY_X_GET_Y Discount (khi khách tự thêm gift product)
     *
     * @param promotion chi tiết khuyến mãi mua X tặng Y
     * @return thông tin tóm tắt chi tiết
     */
    private String buildBuyXGetYDiscountSummary(BuyXGetYDetail promotion) {
        StringBuilder summary = new StringBuilder();

        // Loại giảm giá
        if (promotion.getGiftDiscountType() == DiscountType.PERCENTAGE) {
            summary.append("Giảm ").append(promotion.getGiftDiscountValue()).append("%");
        } else if (promotion.getGiftDiscountType() == DiscountType.FIXED_AMOUNT) {
            summary.append("Giảm ").append(String.format("%,.0f", promotion.getGiftDiscountValue())).append("đ");
        }

        // Điều kiện
        summary.append(" khi mua ");
        if (promotion.getBuyMinQuantity() != null) {
            summary.append(promotion.getBuyMinQuantity()).append(" ");
        }
        if (promotion.getBuyProduct() != null) {
            summary.append(promotion.getBuyProduct().getProduct().getName());
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
