package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho từng line khuyến mãi thuộc một PromotionHeader
 */
@Entity
@Table(name = "promotion_line")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_line_id")
    private Long promotionLineId;

    /**
     * Mã chương trình khuyến mãi
     */
    @Column(name = "promotion_code", length = 50, nullable = false, unique = true)
    private String promotionCode;

    /**
     * Loại khuyến mãi của line (được chuyển từ PromotionHeader)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", length = 20, nullable = false)
    private PromotionType promotionType;

    /**
     * Mô tả ngắn về line khuyến mãi
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Ngày bắt đầu cho từng line
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc cho từng line
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /**
     * Trạng thái khuyến mãi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionStatus status;

    /**
     * Giới hạn tổng số lần sử dụng
     */
    @Column(name = "max_usage_total")
    private Integer maxUsageTotal;

    /**
     * Giới hạn số lần sử dụng mỗi khách hàng
     */
    @Column(name = "max_usage_per_customer")
    private Integer maxUsagePerCustomer;

    /**
     * Số lần đã sử dụng hiện tại
     */
    @Column(name = "current_usage_count", nullable = false)
    private Integer currentUsageCount = 0;

    /**
     * Header cha mà line này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private PromotionHeader header;

    /**
     * Các chi tiết cho line này (một line có thể có nhiều detail)
     */
    @OneToMany(mappedBy = "promotionLine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromotionDetail> details;

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
}
