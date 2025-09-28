package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import iuh.fit.supermarket.enums.PromotionStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho chiến dịch khuyến mãi trong hệ thống
 */
@Entity
@Table(name = "promotions")
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
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Mô tả chương trình
     */
    @Column(name = "description", columnDefinition = "TEXT")
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
     * Trang thái hoạt động
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "promotion_status", nullable = false)
    private PromotionStatus status;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Các dòng áp dụng khuyến mãi thuộc về chương trình này
     */
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PromotionLine> promotionLines;
}
