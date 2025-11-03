package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import iuh.fit.supermarket.enums.PromotionStatus;

import java.time.LocalDate;
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
     * Tên chương trình khuyến mãi
     */
    @Column(name = "promotion_name", length = 200, nullable = false)
    private String promotionName;

    /**
     * Mô tả chương trình
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Ngày bắt đầu
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Ngày kết thúc
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Trạng thái khuyến mãi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PromotionStatus status;

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
     * Các line khuyến mãi thuộc về chương trình này
     */
    @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PromotionLine> promotionLines;
}
