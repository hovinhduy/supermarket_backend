package iuh.fit.supermarket.dto.cart;

import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response cho thông tin toàn bộ giỏ hàng
 */
public record CartResponse(
        /**
         * ID giỏ hàng
         */
        Integer cartId,

        /**
         * ID khách hàng (null nếu là khách vãng lai)
         */
        Integer customerId,

        /**
         * Danh sách items trong giỏ (bao gồm cả quà tặng từ khuyến mãi)
         */
        List<CartItemResponse> items,

        /**
         * Tổng số lượng items (chỉ tính sản phẩm mua, không tính quà tặng)
         */
        Integer totalItems,

        /**
         * Tổng tiền trước khuyến mãi
         */
        Double subTotal,

        /**
         * Giảm giá từ sản phẩm (PRODUCT_DISCOUNT + BUY_X_GET_Y)
         */
        Double lineItemDiscount,

        /**
         * Giảm giá từ đơn hàng (ORDER_DISCOUNT)
         */
        Double orderDiscount,

        /**
         * Tổng tiền sau khuyến mãi (cần thanh toán)
         */
        Double totalPayable,

        /**
         * Danh sách khuyến mãi đơn hàng đã áp dụng
         */
        List<CheckPromotionResponseDTO.OrderPromotionDTO> appliedOrderPromotions,

        /**
         * Thời gian tạo giỏ hàng
         */
        LocalDateTime createdAt,

        /**
         * Thời gian cập nhật
         */
        LocalDateTime updatedAt
) {
}
