package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho đơn vị tính trong hệ thống
 * Lưu trữ danh mục các đơn vị tính như Kilogram, Gram, Lít, Chai, Hộp, v.v.
 * Đây là bảng master data đơn giản để quản lý tập trung các đơn vị tính
 */
@Entity
@Table(name = "units", uniqueConstraints = {
        @UniqueConstraint(name = "uk_unit_name", columnNames = {"name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    /**
     * ID duy nhất của đơn vị tính
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Tên đơn vị tính (duy nhất)
     * Ví dụ: Kilogram, Gram, Lít, Chai, Hộp
     */
    @NotBlank(message = "Tên đơn vị tính không được để trống")
    @Size(max = 50, message = "Tên đơn vị tính không được vượt quá 50 ký tự")
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Trạng thái xóa mềm
     */
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
     * Danh sách ProductUnit sử dụng đơn vị tính này
     */
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY)
    private List<ProductUnit> productUnits;
}
