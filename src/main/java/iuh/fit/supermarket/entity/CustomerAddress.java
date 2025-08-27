package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho địa chỉ khách hàng trong hệ thống
 */
@Entity
@Table(name = "customer_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    /**
     * ID duy nhất của địa chỉ
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    /**
     * Tên người nhận
     */
    @Column(name = "recipient_name")
    private String recipientName;

    /**
     * Số điện thoại người nhận
     */
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    /**
     * Địa chỉ đường phố
     */
    @Column(name = "street_address", columnDefinition = "TEXT")
    private String streetAddress;

    /**
     * Phường/Xã
     */
    @Column(name = "ward", length = 100)
    private String ward;

    /**
     * Quận/Huyện
     */
    @Column(name = "district", length = 100)
    private String district;

    /**
     * Thành phố/Tỉnh
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Địa chỉ mặc định
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Khách hàng sở hữu địa chỉ này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
