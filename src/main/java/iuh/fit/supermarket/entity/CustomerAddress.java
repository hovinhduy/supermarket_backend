package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.AddressLabel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho địa chỉ giao hàng của khách hàng
 * Mỗi khách hàng có thể có nhiều địa chỉ giao hàng
 */
@Entity
@Table(name = "customer_addresses")
@Data
@EqualsAndHashCode(exclude = {"customer"})
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {

    /**
     * ID duy nhất của địa chỉ
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    /**
     * Khách hàng sở hữu địa chỉ này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Tên người nhận hàng
     */
    @Column(name = "recipient_name", length = 100, nullable = false)
    private String recipientName;

    /**
     * Số điện thoại người nhận
     */
    @Column(name = "recipient_phone", length = 20, nullable = false)
    private String recipientPhone;

    /**
     * Địa chỉ chi tiết (số nhà, tên đường)
     */
    @Column(name = "address_line", length = 255, nullable = false)
    private String addressLine;

    /**
     * Phường/Xã
     */
    @Column(name = "ward", length = 100)
    private String ward;

    /**
     * Tỉnh/Thành phố
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Đánh dấu địa chỉ mặc định
     * Mỗi khách hàng chỉ có 1 địa chỉ mặc định
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * Nhãn địa chỉ (Nhà, Văn phòng, Tòa nhà)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "label", length = 20)
    private AddressLabel label;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Trạng thái xóa mềm
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Lấy địa chỉ đầy đủ (nối các thành phần)
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine != null && !addressLine.isEmpty()) {
            sb.append(addressLine);
        }
        if (ward != null && !ward.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }
}
