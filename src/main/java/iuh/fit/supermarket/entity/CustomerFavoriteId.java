package iuh.fit.supermarket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key cho entity CustomerFavorite
 * Sử dụng cho primary key phức hợp (customer_id, product_unit_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFavoriteId implements Serializable {

    /**
     * ID khách hàng
     */
    private Integer customer;

    /**
     * ID đơn vị sản phẩm
     */
    private Long productUnit;
}
