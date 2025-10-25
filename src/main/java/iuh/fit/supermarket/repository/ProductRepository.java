package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface cho thao tác với dữ liệu sản phẩm
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        /**
         * Lấy danh sách sản phẩm theo trạng thái hoạt động
         */
        List<Product> findByIsActiveAndIsDeleted(Boolean isActive, Boolean isDeleted);

        /**
         * Lấy danh sách sản phẩm theo danh mục
         */
        @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.isDeleted = :isDeleted")
        List<Product> findByCategoryIdAndIsDeleted(@Param("categoryId") Long categoryId,
                        @Param("isDeleted") Boolean isDeleted);

        /**
         * Tìm sản phẩm theo tên chứa từ khóa
         */
        @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.isDeleted = false")
        List<Product> findByNameContaining(@Param("keyword") String keyword);

        /**
         * Tìm kiếm sản phẩm nâng cao với filtering
         */
        @Query("SELECT p FROM Product p WHERE " +
                        "(:searchTerm = '' OR (p.name LIKE %:searchTerm%)) AND " +
                        "(:isActive IS NULL OR p.isActive = :isActive) AND " +
                        "p.isDeleted = false")
        Page<Product> findProductsAdvanced(@Param("searchTerm") String searchTerm,
                        @Param("isActive") Boolean isActive,
                        Pageable pageable);

        /**
         * Kiểm tra tồn tại mã sản phẩm
         */
        boolean existsByCode(String code);

        /**
         * Kiểm tra tồn tại mã sản phẩm (trừ bản thân)
         */
        boolean existsByCodeAndIdNot(String code, Long id);

        /**
         * Kiểm tra tồn tại tên sản phẩm
         */
        boolean existsByName(String name);

        /**
         * Kiểm tra tồn tại tên sản phẩm (trừ bản thân)
         */
        boolean existsByNameAndIdNot(String name, Long id);
}
