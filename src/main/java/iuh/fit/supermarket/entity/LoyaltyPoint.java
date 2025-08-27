package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho điểm tích lũy của khách hàng
 */
@Entity
@Table(name = "loyalty_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoyaltyPoint {

    /**
     * ID duy nhất của bản ghi điểm
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Integer pointId;

    /**
     * Số điểm (có thể âm nếu là sử dụng điểm)
     */
    @Column(name = "points")
    private Integer points = 0;

    /**
     * Ngày tích/sử dụng điểm
     */
    @CreationTimestamp
    @Column(name = "earned_date")
    private LocalDateTime earnedDate;

    /**
     * Mô tả giao dịch điểm
     */
    @Column(name = "description")
    private String description;

    /**
     * Khách hàng sở hữu điểm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Đơn hàng liên quan (nếu có)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
