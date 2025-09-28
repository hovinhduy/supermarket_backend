// package iuh.fit.supermarket.entity;

// import jakarta.persistence.*;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// /**
// * Entity đại diện cho mối quan hệ giữa banner và sản phẩm
// */
// @Entity
// @Table(name = "banner_variants")
// @IdClass(BannerVariantId.class)
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class BannerVariant {

// /**
// * Banner
// */
// @Id
// @ManyToOne(fetch = FetchType.LAZY)
// @JoinColumn(name = "banner_id")
// private Banner banner;

// /**
// * Đơn vị sản phẩm được gán cho banner
// */
// @Id
// @ManyToOne(fetch = FetchType.LAZY)
// @JoinColumn(name = "product_unit_id")
// private ProductUnit productUnit;
// }
