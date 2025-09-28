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

/**
 * Entity đại diện cho dòng áp dụng khuyến mãi trong hệ thống
 */
@Entity
@Table(name = "promotion_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLine {

    /**
     * ID duy nhất của dòng khuyến mãi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    /**
     * Mã dòng khuyến mãi
     */
    @Column(name = "line_code", nullable = false, unique = true)
    private String lineCode;

    /**
     * Mô tả dòng khuyến mãi
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Loại khuyến mãi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_type", nullable = false)
    private PromotionType promotionType;

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
     * Trạng thái hoạt động
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_status", nullable = false)
    private PromotionStatus status;

    /**
     * Thời gian tạo dòng khuyến mãi
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật dòng khuyến mãi
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Có được áp dụng đồng thời với các dòng khác không
     */
    @Column(name = "is_combinable", nullable = false)
    private Boolean isCombinable = false;

    /**
     * Tổng số lượng áp dụng tối đa cho dòng này (null nếu không giới hạn)
     */
    @Column(name = "max_total_quantity")
    private Integer maxTotalQuantity;

    /**
     * Số lần sử dụng tối đa cho mỗi khách hàng
     */
    @Column(name = "max_per_customer")
    private Integer maxPerCustomer;

    /**
     * Độ ưu tiên khi có nhiều khuyến mãi cùng áp dụng, số lớn hơn ưu tiên cao
     * hơn
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * Chương trình khuyến mãi mà dòng này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private PromotionHeader promotion;

    /**
     * Các chi tiết khuyến mãi thuộc về dòng này
     */
    @OneToMany(mappedBy = "promotionLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PromotionDetail> promotionDetails;

}
