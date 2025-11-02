package iuh.fit.supermarket.dto.cart;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;

import java.time.LocalDateTime;

/**
 * DTO response cho thông tin một item trong giỏ hàng
 */
public record CartItemResponse(
        /**
         * ID dòng item (để reference từ quà tặng)
         */
        Long lineItemId,

        /**
         * ID của product unit
         */
        Long productUnitId,

        /**
         * Tên sản phẩm
         */
        String productName,

        /**
         * Tên đơn vị
         */
        String unitName,

        /**
         * Số lượng
         */
        Integer quantity,

        /**
         * Giá tại thời điểm thêm vào giỏ
         */
        Double unitPrice,

        /**
         * Tổng giá trước khuyến mãi (quantity * unitPrice)
         */
        Double originalTotal,

        /**
         * Tổng giá sau khuyến mãi
         */
        Double finalTotal,

        /**
         * URL hình ảnh sản phẩm
         */
        String imageUrl,

        /**
         * Số lượng tồn kho
         */
        Integer stockQuantity,

        /**
         * Có khuyến mãi không
         */
        Boolean hasPromotion,

        /**
         * Thông tin khuyến mãi được áp dụng (PRODUCT_DISCOUNT hoặc BUY_X_GET_Y)
         */
        PromotionAppliedDTO promotionApplied,

        /**
         * Thời gian thêm vào giỏ
         */
        LocalDateTime createdAt,

        /**
         * Thời gian cập nhật
         */
        LocalDateTime updatedAt
) {
}
