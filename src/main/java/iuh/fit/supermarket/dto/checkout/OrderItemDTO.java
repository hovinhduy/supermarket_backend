package iuh.fit.supermarket.dto.checkout;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin chi tiết sản phẩm trong đơn hàng
 */
public record OrderItemDTO(
        /**
         * ID của product unit
         */
        Long productUnitId,

        /**
         * Tên sản phẩm
         */
        String productName,

        /**
         * Tên đơn vị tính
         */
        String unitName,

        /**
         * Barcode
         */
        String barcode,

        /**
         * Số lượng
         */
        Integer quantity,

        /**
         * Giá gốc (giá tại thời điểm mua)
         */
        BigDecimal originalPrice,

        /**
         * Giá sau giảm (nếu có khuyến mãi)
         */
        BigDecimal discountedPrice,

        /**
         * Số tiền giảm giá
         */
        BigDecimal discountAmount,

        /**
         * Thành tiền (quantity * discountedPrice)
         */
        BigDecimal lineTotal,

        /**
         * Thông tin khuyến mãi áp dụng (nếu có)
         */
        String promotionInfo,

        /**
         * URL hình ảnh chính của product unit
         */
        String imageUrl
) {}