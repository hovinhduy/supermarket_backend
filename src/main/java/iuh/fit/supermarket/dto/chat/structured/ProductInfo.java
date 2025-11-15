package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Thông tin sản phẩm trong structured response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductInfo(

        /**
         * ID sản phẩm
         */
        @JsonProperty(value = "product_id")
        Long productId,

        /**
         * ID đơn vị sản phẩm (QUAN TRỌNG: dùng để thêm vào giỏ hàng)
         */
        @JsonProperty(value = "product_unit_id", required = true)
        Long productUnitId,

        /**
         * Tên sản phẩm
         */
        @JsonProperty(required = true, value = "name")
        String name,

        /**
         * Mã sản phẩm
         */
        @JsonProperty(value = "code")
        String code,

        /**
         * Giá bán
         */
        @JsonProperty(value = "price")
        BigDecimal price,

        /**
         * Đơn vị tính
         */
        @JsonProperty(value = "unit")
        String unit,

        /**
         * Thương hiệu
         */
        @JsonProperty(value = "brand")
        String brand,

        /**
         * Tình trạng tồn kho
         */
        @JsonProperty(value = "stock_status")
        String stockStatus,

        /**
         * URL hình ảnh
         */
        @JsonProperty(value = "image_url")
        String imageUrl,

        /**
         * Mô tả ngắn
         */
        @JsonProperty(value = "description")
        String description,

        /**
         * Đang có khuyến mãi không?
         */
        @JsonProperty(value = "has_promotion")
        Boolean hasPromotion,

        /**
         * Giá sau khuyến mãi (nếu có)
         */
        @JsonProperty(value = "promotion_price")
        BigDecimal promotionPrice
) {
}
