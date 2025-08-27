package iuh.fit.supermarket.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Composite key cho entity PromotionCustomer
 * Sử dụng cho primary key phức hợp (promotion_id, customer_id)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCustomerId implements Serializable {
    
    /**
     * ID chương trình khuyến mãi
     */
    private Integer promotion;
    
    /**
     * ID khách hàng
     */
    private Integer customer;
}
