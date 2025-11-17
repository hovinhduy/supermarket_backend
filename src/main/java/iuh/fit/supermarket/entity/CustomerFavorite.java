package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho sản phẩm yêu thích của khách hàng
 */
@Entity
@Table(name = "customer_favorites")
@IdClass(CustomerFavoriteId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFavorite {

  /**
   * Thời gian thêm vào danh sách yêu thích
   */
  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /**
   * Thời gian cập nhật gần nhất
   *
   */

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  /**
   * Khách hàng
   */
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id")
  private Customer customer;

  /**
   * Đơn vị sản phẩm yêu thích
   */
  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_unit_id")
  private ProductUnit productUnit;
}
