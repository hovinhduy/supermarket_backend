package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.category.CategoryCreateRequest;
import iuh.fit.supermarket.dto.category.CategoryDto;
import iuh.fit.supermarket.dto.category.CategoryUpdateRequest;
import iuh.fit.supermarket.entity.Category;
import iuh.fit.supermarket.exception.CategoryException;
import iuh.fit.supermarket.repository.CategoryRepository;
import iuh.fit.supermarket.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của CategoryService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Tạo danh mục mới
     */
    @Override
    public CategoryDto createCategory(CategoryCreateRequest request) {
        log.info("Bắt đầu tạo danh mục mới: {}", request.getName());

        // Kiểm tra trùng lặp tên danh mục
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryException("Danh mục với tên '" + request.getName() + "' đã tồn tại");
        }
        // Kiểm tra nếu là danh mục con thì không được đặt làm danh mục cha
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryException(
                            "Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            if (parent.getParent() != null) {
                throw new CategoryException("Không thể đặt danh mục con làm cha");
            }
        }

        // Tạo entity Category
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);

        // Thiết lập danh mục cha và level nếu có
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryException(
                            "Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            category.setParent(parent);
            // Tính toán level dựa trên danh mục cha
            // category.setLevel(parent.getLevel() + 1);
        } else {
            // Danh mục gốc có level = 0
            // category.setLevel(0);
        }

        // Lưu danh mục
        category = categoryRepository.save(category);
        log.info("Đã tạo danh mục với ID: {}", category.getCategoryId());

        return mapToCategoryDto(category);
    }

    /**
     * Lấy thông tin danh mục theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Integer id) {
        log.info("Lấy thông tin danh mục với ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryException("Không tìm thấy danh mục với ID: " + id));

        return mapToCategoryDtoWithChildren(category);
    }

    /**
     * Cập nhật thông tin danh mục
     */
    @Override
    public CategoryDto updateCategory(Integer id, CategoryUpdateRequest request) {
        log.info("Cập nhật danh mục với ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra trùng lặp tên nếu tên được cập nhật
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new CategoryException("Danh mục với tên '" + request.getName() + "' đã tồn tại");
            }
            category.setName(request.getName());
        }
        // Kiểm tra nếu là danh mục con thì không được đặt làm danh mục cha
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryException(
                            "Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            if (parent.getParent() != null) {
                throw new CategoryException("Không thể đặt danh mục con làm cha");
            }
        }

        // Cập nhật các trường khác
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        // Cập nhật danh mục cha và level nếu có thay đổi
        if (request.getParentId() != null) {
            // Kiểm tra không cho phép đặt chính nó làm cha
            if (request.getParentId().equals(id)) {
                throw new CategoryException("Không thể đặt danh mục làm cha của chính nó");
            }

            // Kiểm tra không cho phép đặt con của nó làm cha (tránh vòng lặp)
            checkCyclicReference(id, request.getParentId());

            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryException(
                            "Không tìm thấy danh mục cha với ID: " + request.getParentId()));
            category.setParent(parent);
            // Cập nhật level dựa trên danh mục cha mới
            // category.setLevel(parent.getLevel() + 1);
            // Cập nhật level cho tất cả danh mục con_
            updateChildrenLevels(category);
        } else if (request.getParentId() == null && category.getParent() != null) {
            // Nếu parentId = null và hiện tại có parent, thì gỡ bỏ parent và đặt level = 0
            category.setParent(null);
            // category.setLevel(0);
            // Cập nhật level cho tất cả danh mục con
            updateChildrenLevels(category);
        }

        // Lưu danh mục
        category = categoryRepository.save(category);
        log.info("Đã cập nhật danh mục với ID: {}", category.getCategoryId());

        return mapToCategoryDto(category);
    }

    /**
     * Xóa danh mục (đánh dấu không hoạt động)
     */
    @Override
    public void deleteCategory(Integer id) {
        log.info("Xóa danh mục với ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra xem danh mục có sản phẩm không
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new CategoryException("Không thể xóa danh mục đang chứa sản phẩm");
        }

        // Kiểm tra xem danh mục có danh mục con không
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            throw new CategoryException("Không thể xóa danh mục đang chứa danh mục con");
        }

        // Đánh dấu không hoạt động thay vì xóa
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Đã đánh dấu không hoạt động cho danh mục với ID: {}", id);
    }

    /**
     * Lấy danh sách danh mục với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> getCategories(Pageable pageable) {
        log.info("Lấy danh sách danh mục với phân trang");

        return categoryRepository.findAll(pageable)
                .map(this::mapToCategoryDto);
    }

    /**
     * Lấy tất cả danh mục đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllActiveCategories() {
        log.info("Lấy tất cả danh mục đang hoạt động");

        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách danh mục gốc (không có danh mục cha)
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        log.info("Lấy danh sách danh mục gốc");

        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToCategoryDtoWithChildren)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách danh mục con của một danh mục
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getSubcategories(Integer parentId) {
        log.info("Lấy danh sách danh mục con của danh mục với ID: {}", parentId);

        // Kiểm tra danh mục cha tồn tại
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryException("Không tìm thấy danh mục cha với ID: " + parentId);
        }

        return categoryRepository.findByParent_CategoryId(parentId).stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm danh mục theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDto> searchCategories(String keyword, Pageable pageable) {
        log.info("Tìm kiếm danh mục với từ khóa: {}", keyword);

        return categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable)
                .map(this::mapToCategoryDto);
    }

    /**
     * Chuyển đổi từ entity Category sang DTO
     */
    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getCategoryId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIsActive(category.getIsActive());
        // dto.setLevel(category.getLevel());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());

        // Thiết lập thông tin danh mục cha nếu có
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getCategoryId());
            dto.setParentName(category.getParent().getName());
        }

        return dto;
    }

    /**
     * Chuyển đổi từ entity Category sang DTO kèm danh sách con
     */
    private CategoryDto mapToCategoryDtoWithChildren(Category category) {
        CategoryDto dto = mapToCategoryDto(category);

        // Thiết lập danh sách danh mục con nếu có
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<CategoryDto> childrenDtos = category.getChildren().stream()
                    .map(this::mapToCategoryDto)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDtos);
        } else {
            dto.setChildren(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Cập nhật level cho tất cả danh mục con một cách đệ quy
     */
    private void updateChildrenLevels(Category parentCategory) {
        List<Category> children = categoryRepository.findByParent_CategoryId(parentCategory.getCategoryId());
        for (Category child : children) {
            // child.setLevel(parentCategory.getLevel() + 1);
            categoryRepository.save(child);
            // Đệ quy cập nhật cho danh mục con của con
            updateChildrenLevels(child);
        }
    }

    /**
     * Kiểm tra tham chiếu vòng tròn trong cấu trúc cây danh mục
     */
    private void checkCyclicReference(Integer categoryId, Integer newParentId) {
        // Kiểm tra xem newParentId có phải là con cháu của categoryId không
        Category parent = categoryRepository.findById(newParentId).orElse(null);
        while (parent != null) {
            if (parent.getCategoryId().equals(categoryId)) {
                throw new CategoryException("Không thể đặt danh mục con làm cha (tham chiếu vòng tròn)");
            }
            parent = parent.getParent();
        }
    }
}