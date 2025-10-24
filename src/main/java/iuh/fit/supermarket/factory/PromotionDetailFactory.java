package iuh.fit.supermarket.factory;

import iuh.fit.supermarket.dto.promotion.PromotionDetailRequestDTO;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.exception.PromotionValidationException;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory class để tạo các concrete PromotionDetail subclass
 * Áp dụng Factory Pattern và Strategy Pattern
 */
@Component
@RequiredArgsConstructor
public class PromotionDetailFactory {

    private final ProductUnitRepository productUnitRepository;

    /**
     * Tạo PromotionDetail instance dựa trên PromotionType
     */
    public PromotionDetail createDetail(PromotionType promotionType, PromotionDetailRequestDTO dto) {
        PromotionDetail detail = switch (promotionType) {
            case BUY_X_GET_Y -> createBuyXGetYDetail(dto);
            case ORDER_DISCOUNT -> createOrderDiscountDetail(dto);
            case PRODUCT_DISCOUNT -> createProductDiscountDetail(dto);
        };

        detail.validate();
        return detail;
    }

    /**
     * Tạo BuyXGetYDetail từ DTO
     */
    private BuyXGetYDetail createBuyXGetYDetail(PromotionDetailRequestDTO dto) {
        BuyXGetYDetail detail = new BuyXGetYDetail();

        if (dto.getBuyProductId() != null) {
            ProductUnit buyProduct = productUnitRepository.findById(dto.getBuyProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm phải mua không tồn tại"));
            detail.setBuyProduct(buyProduct);
        }

        detail.setBuyMinQuantity(dto.getBuyMinQuantity());
        detail.setBuyMinValue(dto.getBuyMinValue());

        if (dto.getGiftProductId() != null) {
            ProductUnit giftProduct = productUnitRepository.findById(dto.getGiftProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm tặng không tồn tại"));
            detail.setGiftProduct(giftProduct);
        }

        detail.setGiftQuantity(dto.getGiftQuantity());
        detail.setGiftDiscountType(dto.getGiftDiscountType());
        detail.setGiftDiscountValue(dto.getGiftDiscountValue());
        detail.setGiftMaxQuantity(dto.getGiftMaxQuantity());

        return detail;
    }

    /**
     * Tạo OrderDiscountDetail từ DTO
     */
    private OrderDiscountDetail createOrderDiscountDetail(PromotionDetailRequestDTO dto) {
        OrderDiscountDetail detail = new OrderDiscountDetail();

        detail.setOrderDiscountType(dto.getOrderDiscountType());
        detail.setOrderDiscountValue(dto.getOrderDiscountValue());
        detail.setOrderDiscountMaxValue(dto.getOrderDiscountMaxValue());
        detail.setOrderMinTotalValue(dto.getOrderMinTotalValue());
        detail.setOrderMinTotalQuantity(dto.getOrderMinTotalQuantity());

        return detail;
    }

    /**
     * Tạo ProductDiscountDetail từ DTO
     */
    private ProductDiscountDetail createProductDiscountDetail(PromotionDetailRequestDTO dto) {
        ProductDiscountDetail detail = new ProductDiscountDetail();

        detail.setProductDiscountType(dto.getProductDiscountType());
        detail.setProductDiscountValue(dto.getProductDiscountValue());
        detail.setApplyToType(dto.getApplyToType());

        if (dto.getApplyToProductId() != null) {
            ProductUnit applyToProduct = productUnitRepository.findById(dto.getApplyToProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm áp dụng không tồn tại"));
            detail.setApplyToProduct(applyToProduct);
        }

        detail.setProductMinOrderValue(dto.getProductMinOrderValue());
        detail.setProductMinPromotionValue(dto.getProductMinPromotionValue());
        detail.setProductMinPromotionQuantity(dto.getProductMinPromotionQuantity());

        return detail;
    }

    /**
     * Update PromotionDetail từ DTO (giữ nguyên type)
     */
    public void updateDetail(PromotionDetail detail, PromotionDetailRequestDTO dto) {
        if (detail instanceof BuyXGetYDetail buyXGetY) {
            updateBuyXGetYDetail(buyXGetY, dto);
        } else if (detail instanceof OrderDiscountDetail orderDiscount) {
            updateOrderDiscountDetail(orderDiscount, dto);
        } else if (detail instanceof ProductDiscountDetail productDiscount) {
            updateProductDiscountDetail(productDiscount, dto);
        } else {
            throw new PromotionValidationException("Loại promotion detail không xác định");
        }

        detail.validate();
    }

    /**
     * Update BuyXGetYDetail
     */
    private void updateBuyXGetYDetail(BuyXGetYDetail detail, PromotionDetailRequestDTO dto) {
        if (dto.getBuyProductId() != null) {
            ProductUnit buyProduct = productUnitRepository.findById(dto.getBuyProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm phải mua không tồn tại"));
            detail.setBuyProduct(buyProduct);
        } else {
            detail.setBuyProduct(null);
        }

        detail.setBuyMinQuantity(dto.getBuyMinQuantity());
        detail.setBuyMinValue(dto.getBuyMinValue());

        if (dto.getGiftProductId() != null) {
            ProductUnit giftProduct = productUnitRepository.findById(dto.getGiftProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm tặng không tồn tại"));
            detail.setGiftProduct(giftProduct);
        }

        detail.setGiftQuantity(dto.getGiftQuantity());
        detail.setGiftDiscountType(dto.getGiftDiscountType());
        detail.setGiftDiscountValue(dto.getGiftDiscountValue());
        detail.setGiftMaxQuantity(dto.getGiftMaxQuantity());
    }

    /**
     * Update OrderDiscountDetail
     */
    private void updateOrderDiscountDetail(OrderDiscountDetail detail, PromotionDetailRequestDTO dto) {
        detail.setOrderDiscountType(dto.getOrderDiscountType());
        detail.setOrderDiscountValue(dto.getOrderDiscountValue());
        detail.setOrderDiscountMaxValue(dto.getOrderDiscountMaxValue());
        detail.setOrderMinTotalValue(dto.getOrderMinTotalValue());
        detail.setOrderMinTotalQuantity(dto.getOrderMinTotalQuantity());
    }

    /**
     * Update ProductDiscountDetail
     */
    private void updateProductDiscountDetail(ProductDiscountDetail detail, PromotionDetailRequestDTO dto) {
        detail.setProductDiscountType(dto.getProductDiscountType());
        detail.setProductDiscountValue(dto.getProductDiscountValue());
        detail.setApplyToType(dto.getApplyToType());

        if (dto.getApplyToProductId() != null) {
            ProductUnit applyToProduct = productUnitRepository.findById(dto.getApplyToProductId())
                    .orElseThrow(() -> new PromotionValidationException("Sản phẩm áp dụng không tồn tại"));
            detail.setApplyToProduct(applyToProduct);
        } else {
            detail.setApplyToProduct(null);
        }

        detail.setProductMinOrderValue(dto.getProductMinOrderValue());
        detail.setProductMinPromotionValue(dto.getProductMinPromotionValue());
        detail.setProductMinPromotionQuantity(dto.getProductMinPromotionQuantity());
    }
}
