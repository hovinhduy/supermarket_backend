package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho mối quan hệ giữa khuyến mãi và khách hàng
 */
@Entity
@Table(name = "promotion_customers")
@IdClass(PromotionCustomerId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCustomer {

    /**
     * Chương trình khuyến mãi
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    /**
     * Khách hàng được áp dụng khuyến mãi
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
