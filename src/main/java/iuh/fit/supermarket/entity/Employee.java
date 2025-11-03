package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Entity đại diện cho nhân viên trong hệ thống
 * Thông tin cơ bản (name, email, password, role) được lưu trong bảng User
 */
@Entity
@Table(name = "employees")
@Data
@EqualsAndHashCode(exclude = {"user", "orders", "imports", "saleInvoices", "returnInvoices", "createdStocktakes", "completedStocktakes", "createdPrices", "updatedPrices"})
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
     * Foreign key tới bảng users
     * Mỗi employee phải có một user tương ứng
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Mã nhân viên (tự động sinh: NV000001 - NV999999, hoặc tùy chỉnh)
     */
    @Column(name = "employee_code", length = 50, unique = true)
    private String employeeCode;

    /**
     * Danh sách đơn hàng được xử lý bởi nhân viên này
     */
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> orders;

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

    /**
     * Danh sách bảng giá được tạo bởi nhân viên này
     */
    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Price> createdPrices;

    /**
     * Danh sách bảng giá được cập nhật bởi nhân viên này
     */
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Price> updatedPrices;
}
