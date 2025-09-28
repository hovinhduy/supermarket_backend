package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho mục trong giỏ hàng
 */
@Entity
@Table(name = "cart_items")
@IdClass(CartItemId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    /**
     * Số lượng sản phẩm
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;
    
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
     * Giỏ hàng chứa mục này
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private ShoppingCart cart;
    
    /**
     * Đơn vị sản phẩm
     */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id")
    private ProductUnit productUnit;
}
