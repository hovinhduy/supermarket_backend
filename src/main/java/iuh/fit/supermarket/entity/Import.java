package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho phiếu nhập hàng trong hệ thống
 */
@Entity
@Table(name = "imports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Import {

    /**
     * ID duy nhất của phiếu nhập
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_id")
    private Integer importId;

    /**
     * Mã phiếu nhập (duy nhất)
     */
    @Column(name = "import_code", length = 50, nullable = false, unique = true)
    private String importCode;

    /**
     * Ngày nhập hàng
     */
    @Column(name = "import_date")
    private LocalDateTime importDate = LocalDateTime.now();

    /**
     * Ghi chú
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Nhà cung cấp
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    /**
     * Nhân viên tạo phiếu nhập
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;

    /**
     * Danh sách chi tiết nhập hàng
     */
    @OneToMany(mappedBy = "importRecord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ImportDetail> importDetails;
}
