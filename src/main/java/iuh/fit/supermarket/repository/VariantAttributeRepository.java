package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.VariantAttribute;
import iuh.fit.supermarket.entity.ProductVariant;
import iuh.fit.supermarket.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository để xử lý các thao tác CRUD với VariantAttribute
 */
@Repository
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {

    /**
     * Tìm tất cả thuộc tính của một biến thể
     * 
     * @param variant biến thể sản phẩm
     * @return danh sách thuộc tính của biến thể
     */
    List<VariantAttribute> findByVariant(ProductVariant variant);

    /**
     * Tìm tất cả thuộc tính của một biến thể theo ID
     * 
     * @param variantId ID của biến thể
     * @return danh sách thuộc tính của biến thể
     */
    @Query("SELECT va FROM VariantAttribute va WHERE va.variant.variantId = :variantId")
    List<VariantAttribute> findByVariantId(@Param("variantId") Long variantId);

    /**
     * Tìm tất cả biến thể có chứa giá trị thuộc tính cụ thể
     * 
     * @param attributeValue giá trị thuộc tính
     * @return danh sách thuộc tính biến thể
     */
    List<VariantAttribute> findByAttributeValue(AttributeValue attributeValue);

    /**
     * Tìm tất cả biến thể có chứa giá trị thuộc tính theo ID
     * 
     * @param attributeValueId ID của giá trị thuộc tính
     * @return danh sách thuộc tính biến thể
     */
    @Query("SELECT va FROM VariantAttribute va WHERE va.attributeValue.id = :attributeValueId")
    List<VariantAttribute> findByAttributeValueId(@Param("attributeValueId") Long attributeValueId);

    /**
     * Xóa tất cả thuộc tính của một biến thể
     * 
     * @param variant biến thể sản phẩm
     */
    void deleteByVariant(ProductVariant variant);

    /**
     * Xóa tất cả thuộc tính của một biến thể theo ID
     * 
     * @param variantId ID của biến thể
     */
    @Query("DELETE FROM VariantAttribute va WHERE va.variant.variantId = :variantId")
    void deleteByVariantId(@Param("variantId") Long variantId);

    /**
     * Kiểm tra biến thể có thuộc tính cụ thể hay không
     * 
     * @param variantId        ID của biến thể
     * @param attributeValueId ID của giá trị thuộc tính
     * @return true nếu biến thể có thuộc tính này
     */
    @Query("SELECT COUNT(va) > 0 FROM VariantAttribute va WHERE va.variant.variantId = :variantId AND va.attributeValue.id = :attributeValueId")
    boolean existsByVariantIdAndAttributeValueId(@Param("variantId") Long variantId,
            @Param("attributeValueId") Long attributeValueId);
}
