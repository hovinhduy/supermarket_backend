package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.MovementReason;
import iuh.fit.supermarket.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho chuyển động kho trong hệ thống
 */
@Entity
@Table(name = "stock_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    /**
     * ID duy nhất của chuyển động kho
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movement_id")
    private Integer movementId;

    /**
     * Loại chuyển động (In/Out/Adjustment/Transfer)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    /**
     * Lý do chuyển động
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_reason", nullable = false)
    private MovementReason movementReason;

    /**
     * Số lượng thay đổi (Số dương = nhập, Số âm = xuất)
     */
    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    /**
     * Số lượng trước khi thay đổi
     */
    @Column(name = "quantity_before", nullable = false)
    private Integer quantityBefore;

    /**
     * Số lượng sau khi thay đổi
     */
    @Column(name = "quantity_after", nullable = false)
    private Integer quantityAfter;

    /**
     * Loại chứng từ (order, import, ...)
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    /**
     * ID của chứng từ
     */
    @Column(name = "reference_id")
    private Integer referenceId;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Nhân viên tạo chuyển động kho
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;
}
