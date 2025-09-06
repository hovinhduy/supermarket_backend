package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu danh mục sản phẩm
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Tìm danh mục theo tên
     */
    Optional<Category> findByName(String name);

    /**
     * Kiểm tra sự tồn tại của tên danh mục
     */
    boolean existsByName(String name);
    
    /**
     * Tìm tất cả danh mục đang hoạt động
     */
    List<Category> findByIsActiveTrue();
    
    /**
     * Tìm tất cả danh mục đang hoạt động với phân trang
     */
    Page<Category> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Tìm danh mục con của một danh mục cha
     */
    List<Category> findByParent_CategoryId(Integer parentId);
    
    /**
     * Tìm tất cả danh mục gốc (không có danh mục cha)
     */
    List<Category> findByParentIsNull();
    
    /**
     * Tìm danh mục theo tên chứa từ khóa
     */
    List<Category> findByNameContainingIgnoreCase(String keyword);
    
    /**
     * Tìm danh mục theo tên chứa từ khóa và đang hoạt động
     */
    Page<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword, Pageable pageable);
}