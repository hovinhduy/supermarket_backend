package iuh.fit.supermarket.dto.favorite;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request thêm sản phẩm vào danh sách yêu thích
 */
public record AddFavoriteRequest(
        /**
         * ID của đơn vị sản phẩm (ProductUnit)
         */
        @NotNull(message = "ID đơn vị sản phẩm không được để trống")
        Long productUnitId
) {
    /**
     * Constructor có validation
     */
    public AddFavoriteRequest {
        if (productUnitId == null || productUnitId <= 0) {
            throw new IllegalArgumentException("ID đơn vị sản phẩm phải lớn hơn 0");
        }
    }
}
