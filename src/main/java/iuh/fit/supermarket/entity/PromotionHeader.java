package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho chiến dịch khuyến mãi trong hệ thống
 */
@Entity
@Table(name = "promotion_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionHeader {

    /**
     * ID duy nhất của chương trình khuyến mãi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;

    /**
     * Mã chương trình khuyến mãi
     */
    @Column(name = "promotion_code", length = 50, nullable = false, unique = true)
    private String promotionCode;

    /**
     * Tên chương trình khuyến mãi
     */
    @Column(name = "promotion_name", length = 200, nullable = false)
    private String promotionName;

    /**
     * Loại khuyến mãi (BUY_X_GET_Y, ORDER_DISCOUNT, PRODUCT_DISCOUNT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", length = 20, nullable = false)
    private PromotionType promotionType;

    /**
     * Mô tả chương trình
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Ngày bắt đầu
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Trạng thái khuyến mãi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionStatus status;

    // /**
    // * Độ ưu tiên khi áp dụng nhiều khuyến mãi (số lớn hơn = ưu tiên cao hơn)
    // */
    // @Column(name = "priority", nullable = false)
    // private Integer priority = 0;

    /**
     * Giới hạn số lần sử dụng mỗi khách hàng
     */
    @Column(name = "max_usage_per_customer")
    private Integer maxUsagePerCustomer;

    /**
     * Giới hạn tổng số lần sử dụng
     */
    @Column(name = "max_usage_total")
    private Integer maxUsageTotal;

    /**
     * Số lần đã sử dụng hiện tại
     */
    @Column(name = "current_usage_count", nullable = false)
    private Integer currentUsageCount = 0;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Các chi tiết khuyến mãi thuộc về chương trình này
     */
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromotionDetail> promotionDetails;
}
