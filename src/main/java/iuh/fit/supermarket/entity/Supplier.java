package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho nhà cung cấp trong hệ thống
 */
@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    /**
     * ID duy nhất của nhà cung cấp
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Integer supplierId;

    @Column(name = "code_supplier", length = 50, unique = true)
    private String code;

    /**
     * Tên nhà cung cấp
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Địa chỉ nhà cung cấp
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Email nhà cung cấp
     */
    @Column(name = "email", unique = true)
    private String email;

    /**
     * Số điện thoại
     */
    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

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
     * Danh sách phiếu nhập từ nhà cung cấp này
     */
    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Import> imports;

}
