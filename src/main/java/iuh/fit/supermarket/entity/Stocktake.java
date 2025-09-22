package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.StocktakeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho phiếu kiểm kê kho trong hệ thống
 */
@Entity
@Table(name = "stocktakes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stocktake {

    /**
     * ID duy nhất của phiếu kiểm kê
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stocktake_id")
    private Integer stocktakeId;

    /**
     * Mã phiếu kiểm kê (duy nhất)
     */
    @Column(name = "stocktake_code", length = 50, nullable = false, unique = true)
    private String stocktakeCode;

    /**
     * Trạng thái kiểm kê
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StocktakeStatus status = StocktakeStatus.PENDING;

    /**
     * Ghi chú chung cho đợt kiểm kê
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Thời điểm hoàn tất kiểm kê
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Nhân viên tạo phiếu kiểm kê
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    /**
     * Nhân viên hoàn tất phiếu kiểm kê
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private Employee completedBy;

    /**
     * Danh sách chi tiết kiểm kê
     */
    @OneToMany(mappedBy = "stocktake", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StocktakeDetail> stocktakeDetails;
}
