package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Abstract base class cho chi tiết khuyến mãi
 * Sử dụng Single Table Inheritance để quản lý 3 loại khuyến mãi
 * Strategy Pattern được áp dụng qua abstract method calculateDiscount()
 */
@Entity
@Table(name = "promotion_details")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "promotion_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PromotionDetail {

    /**
     * ID duy nhất của chi tiết khuyến mãi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    /**
     * Promotion line mà chi tiết này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_line_id", nullable = false)
    private PromotionLine promotionLine;

    /**
     * Lấy loại khuyến mãi từ subclass
     */
    public abstract PromotionType getPromotionType();

    /**
     * Validate business rules cho từng loại khuyến mãi
     */
    public abstract void validate();
}
