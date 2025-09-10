package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu giá trị thuộc tính
 */
@Repository
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

    /**
     * Tìm giá trị thuộc tính theo giá trị và ID thuộc tính
     */
    Optional<AttributeValue> findByValueAndAttributeId(String value, Long attributeId);

    /**
     * Kiểm tra sự tồn tại của giá trị thuộc tính theo giá trị và ID thuộc tính
     */
    boolean existsByValueAndAttributeId(String value, Long attributeId);

    /**
     * Lấy danh sách giá trị thuộc tính theo ID thuộc tính
     */
    List<AttributeValue> findByAttributeId(Long attributeId);

    /**
     * Lấy danh sách giá trị thuộc tính theo tên thuộc tính
     */
    @Query("SELECT av FROM AttributeValue av WHERE av.attribute.name = :attributeName")
    List<AttributeValue> findByAttributeName(@Param("attributeName") String attributeName);

    /**
     * Tìm kiếm giá trị thuộc tính theo từ khóa
     */
    @Query("SELECT av FROM AttributeValue av WHERE " +
            "LOWER(av.value) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(av.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<AttributeValue> searchByKeyword(@Param("keyword") String keyword);
}
