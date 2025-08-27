package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho banner quảng cáo trên trang chủ
 */
@Entity
@Table(name = "banners")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {

    /**
     * ID duy nhất của banner
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Integer bannerId;

    /**
     * Tiêu đề banner
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * URL hình ảnh banner
     */
    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    /**
     * Thứ tự hiển thị
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

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
     * Danh sách sản phẩm được gán cho banner này
     */
    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BannerVariant> bannerVariants;
}
