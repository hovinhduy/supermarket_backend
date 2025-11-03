package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.Gender;
import iuh.fit.supermarket.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho user trong hệ thống (chung cho cả Customer và Employee)
 * Chứa thông tin cơ bản như tên, email, phone, password và role
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = {"customer", "employee"})
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * ID duy nhất của user
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * Tên đầy đủ của user
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Email của user (duy nhất trong hệ thống)
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Số điện thoại (duy nhất, nullable cho employee)
     */
    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    /**
     * Mật khẩu đã được hash
     * Nullable cho trường hợp admin tạo customer chưa có password
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /**
     * Vai trò của user trong hệ thống
     * ADMIN, MANAGER, STAFF cho employee
     * CUSTOMER cho khách hàng
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    /**
     * Ngày sinh của user
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * Giới tính
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

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
     * Relationship với Customer (OneToOne)
     * Một user có thể là một customer
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customer customer;

    /**
     * Relationship với Employee (OneToOne)
     * Một user có thể là một employee
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee employee;

    /**
     * Kiểm tra xem user có phải là customer không
     * @return true nếu user_role là CUSTOMER
     */
    public boolean isCustomer() {
        return this.userRole == UserRole.CUSTOMER;
    }

    /**
     * Kiểm tra xem user có phải là employee không
     * @return true nếu user_role là ADMIN, MANAGER hoặc STAFF
     */
    public boolean isEmployee() {
        return this.userRole == UserRole.ADMIN ||
               this.userRole == UserRole.MANAGER ||
               this.userRole == UserRole.STAFF;
    }

    /**
     * Kiểm tra xem user có phải là admin không
     * @return true nếu user_role là ADMIN
     */
    public boolean isAdmin() {
        return this.userRole == UserRole.ADMIN;
    }

    /**
     * Kiểm tra xem user có phải là manager không
     * @return true nếu user_role là MANAGER
     */
    public boolean isManager() {
        return this.userRole == UserRole.MANAGER;
    }

    /**
     * Lấy display name cho role
     * @return tên role dạng văn bản
     */
    public String getRoleDisplayName() {
        return switch (this.userRole) {
            case ADMIN -> "Quản trị viên";
            case MANAGER -> "Quản lý";
            case STAFF -> "Nhân viên";
            case CUSTOMER -> "Khách hàng";
        };
    }
}
