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
     * Tìm thuộc tính sản phẩm theo productId và attributeValueId
     */
    Optional<ProductAttribute> findByProductIdAndAttributeValueId(Long productId, Long attributeValueId);

    /**
     * Tìm thuộc tính sản phẩm theo productId và attribute.id thông qua
     * attributeValue
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.product.id = :productId AND pa.attributeValue.attribute.id = :attributeId")
    List<ProductAttribute> findByProductIdAndAttributeId(@Param("productId") Long productId,
            @Param("attributeId") Long attributeId);

    /**
     * Xóa tất cả thuộc tính theo productId
     */
    void deleteByProductId(Long productId);

    /**
     * Tìm sản phẩm theo thuộc tính và giá trị thông qua AttributeValue
     */
    @Query("SELECT pa FROM ProductAttribute pa WHERE pa.attributeValue.attribute.name = :attributeName AND pa.attributeValue.value = :value")
    List<ProductAttribute> findByAttributeNameAndValue(@Param("attributeName") String attributeName,
            @Param("value") String value);

    /**
     * Tìm thuộc tính sản phẩm theo attributeValue ID
     */
    List<ProductAttribute> findByAttributeValueId(Long attributeValueId);
}
