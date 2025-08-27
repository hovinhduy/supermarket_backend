package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho chương trình khuyến mãi trong hệ thống
 */
@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    
    /**
     * ID duy nhất của chương trình khuyến mãi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer promotionId;
    
    /**
     * Tên chương trình khuyến mãi
     */
    @Column(name = "name", nullable = false)
    private String name;
    
    /**
     * Mô tả chương trình
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * Mã coupon (duy nhất)
     */
    @Column(name = "coupon_code", length = 50, unique = true)
    private String couponCode;
    
    /**
     * Ngày bắt đầu
     */
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    /**
     * Ngày kết thúc
     */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    /**
     * Danh sách chi tiết khuyến mãi
     */
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromotionDetail> promotionDetails;
    
    /**
     * Danh sách khách hàng áp dụng khuyến mãi
     */
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromotionCustomer> promotionCustomers;
    
    /**
     * Kiểm tra khuyến mãi có đang hoạt động không
     * @return true nếu đang hoạt động
     */
    public Boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && 
               (startDate == null || !now.isBefore(startDate)) && 
               (endDate == null || !now.isAfter(endDate));
    }
}
