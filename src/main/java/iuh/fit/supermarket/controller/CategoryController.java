package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.category.CategoryCreateRequest;
import iuh.fit.supermarket.dto.category.CategoryDto;
import iuh.fit.supermarket.dto.category.CategoryUpdateRequest;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý danh mục sản phẩm
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs cho quản lý danh mục sản phẩm")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API tạo danh mục mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo danh mục mới", description = "Tạo một danh mục sản phẩm mới trong hệ thống")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo danh mục thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Danh mục đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request) {
        log.info("API tạo danh mục mới: {}", request.getName());
        
        try {
            CategoryDto createdCategory = categoryService.createCategory(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo danh mục thành công", createdCategory));
        } catch (Exception e) {
            log.error("Lỗi khi tạo danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy thông tin danh mục theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin danh mục theo ID", description = "Trả về thông tin chi tiết của danh mục theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> getCategoryById(
            @Parameter(description = "ID của danh mục") @PathVariable Integer id) {
        log.info("API lấy thông tin danh mục với ID: {}", id);
        
        try {
            CategoryDto category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin danh mục thành công", category));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API cập nhật thông tin danh mục
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin danh mục", description = "Cập nhật thông tin của danh mục theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Tên danh mục đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<CategoryDto>> updateCategory(
            @Parameter(description = "ID của danh mục") @PathVariable Integer id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        log.info("API cập nhật danh mục với ID: {}", id);
        
        try {
            CategoryDto updatedCategory = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", updatedCategory));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API xóa danh mục
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa danh mục", description = "Đánh dấu danh mục là không hoạt động")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể xóa danh mục đang chứa sản phẩm hoặc danh mục con"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "ID của danh mục") @PathVariable Integer id) {
        log.info("API xóa danh mục với ID: {}", id);
        
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", (Void) null));
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy danh sách danh mục với phân trang
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách danh mục", description = "Trả về danh sách danh mục với phân trang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> getCategories(
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        log.info("API lấy danh sách danh mục với phân trang");
        
        try {
            Page<CategoryDto> categories = categoryService.getCategories(pageable);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục thành công", categories));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy tất cả danh mục đang hoạt động
     */
    @GetMapping("/active")
    @Operation(summary = "Lấy tất cả danh mục đang hoạt động", description = "Trả về danh sách tất cả danh mục đang hoạt động")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllActiveCategories() {
        log.info("API lấy tất cả danh mục đang hoạt động");
        
        try {
            List<CategoryDto> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục đang hoạt động thành công", categories));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách danh mục đang hoạt động: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy danh sách danh mục gốc
     */
    @GetMapping("/roots")
    @Operation(summary = "Lấy danh sách danh mục gốc", description = "Trả về danh sách các danh mục không có danh mục cha")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getRootCategories() {
        log.info("API lấy danh sách danh mục gốc");
        
        try {
            List<CategoryDto> rootCategories = categoryService.getRootCategories();
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục gốc thành công", rootCategories));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách danh mục gốc: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy danh sách danh mục con
     */
    @GetMapping("/{parentId}/subcategories")
    @Operation(summary = "Lấy danh sách danh mục con", description = "Trả về danh sách các danh mục con của một danh mục cha")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục cha")
    })
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getSubcategories(
            @Parameter(description = "ID của danh mục cha") @PathVariable Integer parentId) {
        log.info("API lấy danh sách danh mục con của danh mục với ID: {}", parentId);
        
        try {
            List<CategoryDto> subcategories = categoryService.getSubcategories(parentId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách danh mục con thành công", subcategories));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách danh mục con: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API tìm kiếm danh mục theo từ khóa
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm danh mục", description = "Tìm kiếm danh mục theo từ khóa trong tên")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công")
    })
    public ResponseEntity<ApiResponse<Page<CategoryDto>>> searchCategories(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        log.info("API tìm kiếm danh mục với từ khóa: {}", keyword);
        
        try {
            Page<CategoryDto> categories = categoryService.searchCategories(keyword, pageable);
            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm danh mục thành công", categories));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

}