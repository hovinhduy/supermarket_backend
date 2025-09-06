package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.category.CategoryCreateRequest;
import iuh.fit.supermarket.dto.category.CategoryDto;
import iuh.fit.supermarket.dto.category.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho quản lý danh mục sản phẩm
 */
public interface CategoryService {

    /**
     * Tạo danh mục mới
     * 
     * @param request thông tin danh mục cần tạo
     * @return thông tin danh mục đã tạo
     */
    CategoryDto createCategory(CategoryCreateRequest request);

    /**
     * Lấy thông tin danh mục theo ID
     * 
     * @param id ID danh mục
     * @return thông tin danh mục
     */
    CategoryDto getCategoryById(Integer id);

    /**
     * Cập nhật thông tin danh mục
     * 
     * @param id      ID danh mục
     * @param request thông tin cập nhật
     * @return thông tin danh mục đã cập nhật
     */
    CategoryDto updateCategory(Integer id, CategoryUpdateRequest request);

    /**
     * Xóa danh mục (đánh dấu không hoạt động)
     * 
     * @param id ID danh mục
     */
    void deleteCategory(Integer id);

    /**
     * Lấy danh sách danh mục với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return danh sách danh mục
     */
    Page<CategoryDto> getCategories(Pageable pageable);
    
    /**
     * Lấy tất cả danh mục đang hoạt động
     * 
     * @return danh sách danh mục
     */
    List<CategoryDto> getAllActiveCategories();
    
    /**
     * Lấy danh sách danh mục gốc (không có danh mục cha)
     * 
     * @return danh sách danh mục gốc
     */
    List<CategoryDto> getRootCategories();
    
    /**
     * Lấy danh sách danh mục con của một danh mục
     * 
     * @param parentId ID danh mục cha
     * @return danh sách danh mục con
     */
    List<CategoryDto> getSubcategories(Integer parentId);
    
    /**
     * Tìm kiếm danh mục theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return danh sách danh mục tìm được
     */
    Page<CategoryDto> searchCategories(String keyword, Pageable pageable);
}