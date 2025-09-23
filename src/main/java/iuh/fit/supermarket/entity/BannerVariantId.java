package iuh.fit.supermarket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key cho entity BannerVariant
 * Sử dụng cho primary key phức hợp (banner_id, variant_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerVariantId implements Serializable {

    /**
     * ID banner
     */
    private Integer banner;

    /**
     * ID biến thể sản phẩm
     */
    private Long variant;
}
