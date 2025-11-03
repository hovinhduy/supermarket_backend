package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PriceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho bảng giá trong hệ thống
 * Quản lý các bảng giá khác nhau (giá thường, giá khuyến mãi, giá đặc biệt...)
 * Mỗi bảng giá có thể chứa nhiều giá chi tiết cho các biến thể sản phẩm khác
 * nhau
 */
@Entity
@Table(name = "prices", indexes = {
        @Index(name = "idx_price_code", columnList = "price_code"),
        @Index(name = "idx_price_status", columnList = "status"),
        @Index(name = "idx_price_dates", columnList = "start_date, end_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    /**
     * ID duy nhất của bảng giá
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    /**
     * Tên của bảng giá
     */
    @Column(name = "price_name", nullable = false, length = 255)
    private String priceName;

    /**
     * Mã của bảng giá (duy nhất)
     */
    @Column(name = "price_code", length = 100, nullable = false, unique = true)
    private String priceCode;

    /**
     * Ngày bắt đầu hiệu lực của bảng giá
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Ngày kết thúc hiệu lực của bảng giá
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Mô tả về bảng giá
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái của bảng giá (ACTIVE, PAUSED, EXPIRED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PriceType status = PriceType.ACTIVE;

    /**
     * Thời gian tạo bảng giá
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật bảng giá
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Người tạo bảng giá
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    /**
     * Người cập nhật bảng giá
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Employee updatedBy;

    /**
     * Danh sách các giá chi tiết thuộc bảng giá này
     */
    @OneToMany(mappedBy = "price", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PriceDetail> priceDetails;
}
