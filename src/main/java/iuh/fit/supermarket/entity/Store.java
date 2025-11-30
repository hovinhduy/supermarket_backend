package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity đại diện cho cửa hàng/chi nhánh trong hệ thống
 */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    /**
     * ID duy nhất của cửa hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    /**
     * Mã cửa hàng
     */
    @Column(name = "store_code", unique = true, nullable = false, length = 50)
    private String storeCode;

    /**
     * Tên cửa hàng
     */
    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;

    /**
     * Địa chỉ cửa hàng
     */
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    /**
     * Số điện thoại cửa hàng
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Giờ mở cửa
     */
    @Column(name = "opening_time")
    private LocalTime openingTime;

    /**
     * Giờ đóng cửa
     */
    @Column(name = "closing_time")
    private LocalTime closingTime;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
}
