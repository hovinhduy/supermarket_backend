package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.EmployeeRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho nhân viên trong hệ thống
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * ID duy nhất của nhân viên
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Integer employeeId;

    /**
     * Tên nhân viên
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Email nhân viên (duy nhất)
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Mật khẩu đã được hash
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Vai trò của nhân viên (Admin/Manager/Staff)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private EmployeeRole role;

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
     * Danh sách đơn hàng được xử lý bởi nhân viên này
     */
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;

    /**
     * Danh sách lịch sử giá được tạo bởi nhân viên này
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PriceHistory> priceHistories;

    /**
     * Danh sách phiếu nhập được tạo bởi nhân viên này
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Import> imports;

    /**
     * Danh sách hóa đơn bán hàng được tạo bởi nhân viên này
     */
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SaleInvoiceHeader> saleInvoices;

    /**
     * Danh sách phiếu trả hàng được xử lý bởi nhân viên này
     */
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReturnInvoiceHeader> returnInvoices;

    /**
     * Danh sách phiếu kiểm kê được tạo bởi nhân viên này
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Stocktake> createdStocktakes;

    /**
     * Danh sách phiếu kiểm kê được hoàn thành bởi nhân viên này
     */
    @OneToMany(mappedBy = "completedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Stocktake> completedStocktakes;
}
