package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity đại diện cho bảng giá trong hệ thống
 * Dành cho hệ thống cần nhiều bảng giá (giá bán lẻ, giá sỉ, giá khuyến mãi)
 */
@Entity
@Table(name = "PriceLists")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceList {

    /**
     * ID duy nhất của bảng giá
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_list_id")
    private Integer priceListId;

    /**
     * Tên bảng giá
     * Ví dụ: "Bảng giá bán lẻ", "Bảng giá VIP"
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Đánh dấu bảng giá mặc định
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;

    /**
     * Danh sách giá trong bảng giá này
     */
    @OneToMany(mappedBy = "priceList", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Price> prices;
}
