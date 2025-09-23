package iuh.fit.supermarket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key cho entity CartItem
 * Sử dụng cho primary key phức hợp (cart_id, variant_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemId implements Serializable {

    /**
     * ID giỏ hàng
     */
    private Integer cart;

    /**
     * ID biến thể sản phẩm
     */
    private Long variant;
}
