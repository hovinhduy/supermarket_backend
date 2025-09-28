package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết giá sản phẩm trong hệ thống
 * Lưu trữ thông tin giá bán cụ thể cho từng đơn vị sản phẩm trong một bảng giá
 * Mỗi chi tiết giá bao gồm đơn vị sản phẩm và giá bán
 */
@Entity
@Table(name = "price_details", indexes = {
                @Index(name = "idx_price_detail_product_unit_id", columnList = "product_unit_id"),
                @Index(name = "idx_price_detail_price_id", columnList = "price_id"),
                @Index(name = "idx_price_detail_sale_price", columnList = "sale_price"),
                @Index(name = "idx_price_detail_composite", columnList = "price_id, product_unit_id")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_price_detail_price_product_unit", columnNames = { "price_id",
                                "product_unit_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetail {

        /**
         * ID duy nhất của chi tiết giá
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "price_detail_id")
        private Long priceDetailId;

        /**
         * Giá bán
         * Giá bán của sản phẩm theo đơn vị tính
         */
        @NotNull(message = "Giá bán không được để trống")
        @Positive(message = "Giá bán phải lớn hơn 0")
        @Column(name = "sale_price", nullable = false, precision = 12, scale = 2)
        private BigDecimal salePrice;

        /**
         * Thời gian tạo chi tiết giá
         */
        @CreationTimestamp
        @Column(name = "created_at")
        private LocalDateTime createdAt;

        /**
         * Thời gian cập nhật chi tiết giá
         */
        @UpdateTimestamp
        @Column(name = "updated_at")
        private LocalDateTime updatedAt;

        /**
         * Bảng giá chứa chi tiết này
         * Quan hệ Many-to-One với Price
         */
        @NotNull(message = "Bảng giá không được để trống")
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "price_id", nullable = false, foreignKey = @ForeignKey(name = "fk_price_detail_price"))
        private Price price;

        /**
         * Đơn vị sản phẩm tương ứng
         * Quan hệ Many-to-One với ProductUnit
         */
        @NotNull(message = "Đơn vị sản phẩm không được để trống")
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "product_unit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_price_detail_product_unit"))
        private ProductUnit productUnit;
}
