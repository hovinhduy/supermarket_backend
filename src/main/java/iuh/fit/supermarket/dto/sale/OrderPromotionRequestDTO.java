package iuh.fit.supermarket.dto.sale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO cho thông tin khuyến mãi order level trong request bán hàng
 */
public record OrderPromotionRequestDTO(
        @NotBlank(message = "Mã khuyến mãi không được rỗng")
        String promotionId,

        @NotBlank(message = "Tên khuyến mãi không được rỗng")
        String promotionName,

        @NotNull(message = "ID chi tiết khuyến mãi không được null")
        Long promotionDetailId,

        String promotionSummary,

        @NotBlank(message = "Loại giảm giá không được rỗng")
        String discountType,

        @NotNull(message = "Giá trị giảm giá không được null")
        BigDecimal discountValue
) {
}
