package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho khách hàng trong hệ thống
 */
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /**
     * ID duy nhất của khách hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    /**
     * Tên khách hàng
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Email khách hàng (duy nhất)
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Số điện thoại khách hàng
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Mật khẩu đã được hash
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Loại khách hàng (Regular/VIP)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType = CustomerType.REGULAR;

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
     * Danh sách địa chỉ của khách hàng
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerAddress> addresses;

    /**
     * Danh sách đơn hàng của khách hàng
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    /**
     * Giỏ hàng của khách hàng
     */
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ShoppingCart shoppingCart;

    /**
     * Danh sách sản phẩm yêu thích
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerFavorite> favorites;

    /**
     * Danh sách điểm tích lũy
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoyaltyPoint> loyaltyPoints;

    /**
     * Danh sách thông báo
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications;
}
