package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu thuộc tính sản phẩm
 */
@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    /**
     * Tìm thuộc tính sản phẩm theo productId
     */
    List<ProductAttribute> findByProductId(Long productId);

    /**
     * Tìm thuộc tính sản phẩm theo productId và attributeId
     */
    Optional<ProductAttribute> findByProductIdAndAttributeId(Long productId, Long attributeId);

    /**
     * Xóa tất cả thuộc tính theo productId
     */
    void deleteByProductId(Long productId);

    /**
     * Tìm sản phẩm theo thuộc tính và giá trị
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.attribute.name = :attributeName AND pa.value = :value")
    List<ProductAttribute> findByAttributeNameAndValue(@Param("attributeName") String attributeName,
            @Param("value") String value);
}
