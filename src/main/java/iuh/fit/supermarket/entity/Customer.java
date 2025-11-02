package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.CustomerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity đại diện cho khách hàng trong hệ thống
 * Thông tin cơ bản (name, email, phone, password, gender, dateOfBirth) được lưu trong bảng User
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
     * Foreign key tới bảng users
     * Mỗi customer phải có một user tương ứng
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Mã khách hàng (tự động sinh: KH000001 - KH999999, hoặc tùy chỉnh)
     */
    @Column(name = "customer_code", length = 50, unique = true)
    private String customerCode;

    /**
     * Địa chỉ khách hàng (thông tin riêng của customer)
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * Loại khách hàng (Regular/VIP)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type")
    private CustomerType customerType = CustomerType.REGULAR;

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

    // /**
    // * Danh sách sản phẩm yêu thích
    // */
    // @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch =
    // FetchType.LAZY)
    // private List<CustomerFavorite> favorites;

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
