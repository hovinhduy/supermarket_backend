package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các API liên quan đến quản lý sản phẩm
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "APIs cho quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    /**
     * Tạo sản phẩm mới
     */
    @PostMapping
    @Operation(summary = "Tạo sản phẩm mới", description = "Tạo sản phẩm mới với thông tin đầy đủ")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProduct(
            @RequestBody ProductCreateRequest request) {

        log.info("API tạo sản phẩm mới được gọi: {}", request.getName());

        try {
            ProductResponse product = productService.createProduct(request);

            Map<String, Object> responseData = Map.of(
                    "id", product.getId(),
                    "code", product.getCode(),
                    "message", "Tạo sản phẩm thành công");

            ApiResponse<Map<String, Object>> response = ApiResponse.success(responseData);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Lỗi khi tạo sản phẩm: ", e);

            ApiResponse<Map<String, Object>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin sản phẩm", description = "Lấy thông tin chi tiết sản phẩm theo ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {

        log.info("API lấy thông tin sản phẩm ID: {}", id);

        try {
            ProductResponse product = productService.getProductById(id);

            ApiResponse<ProductResponse> response = ApiResponse.success(product);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin sản phẩm: ", e);

            ApiResponse<ProductResponse> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật sản phẩm", description = "Cập nhật thông tin sản phẩm")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductUpdateRequest request) {

        log.info("API cập nhật sản phẩm ID: {}", id);

        try {
            ProductResponse product = productService.updateProduct(id, request);

            ApiResponse<ProductResponse> response = ApiResponse.success("Cập nhật sản phẩm thành công", product);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm: ", e);

            ApiResponse<ProductResponse> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm", description = "Xóa sản phẩm (soft delete)")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @PathVariable Long id) {

        log.info("API xóa sản phẩm ID: {}", id);

        try {
            productService.deleteProduct(id);

            ApiResponse<String> response = ApiResponse.success("Xóa sản phẩm thành công", (String) null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm: ", e);

            ApiResponse<String> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách sản phẩm với phân trang
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy danh sách sản phẩm với phân trang")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getProducts(
            Pageable pageable) {

        log.info("API lấy danh sách sản phẩm với phân trang");

        try {
            Page<ProductResponse> products = productService.getProducts(pageable);

            ApiResponse<Page<ProductResponse>> response = ApiResponse.success(products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm: ", e);

            ApiResponse<Page<ProductResponse>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tìm kiếm sản phẩm
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm sản phẩm theo từ khóa")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam String keyword) {

        log.info("API tìm kiếm sản phẩm với từ khóa: {}", keyword);

        try {
            List<ProductResponse> products = productService.searchProducts(keyword);

            ApiResponse<List<ProductResponse>> response = ApiResponse.success(products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm sản phẩm: ", e);

            ApiResponse<List<ProductResponse>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo danh mục", description = "Lấy danh sách sản phẩm thuộc một danh mục")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId) {

        log.info("API lấy sản phẩm theo danh mục: {}", categoryId);

        try {
            List<ProductResponse> products = productService.getProductsByCategory(categoryId);

            ApiResponse<List<ProductResponse>> response = ApiResponse.success(products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo danh mục: ", e);

            ApiResponse<List<ProductResponse>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tạo biến thể sản phẩm
     */
    @PostMapping("/{id}/variants")
    @Operation(summary = "Tạo biến thể sản phẩm", description = "Tạo biến thể cho sản phẩm")
    public ResponseEntity<ApiResponse<ProductResponse>> createProductVariant(
            @PathVariable Long id,
            @RequestBody ProductVariantCreateRequest request) {

        log.info("API tạo biến thể cho sản phẩm ID: {}", id);

        try {
            ProductResponse variant = productService.createProductVariant(id, request);

            ApiResponse<ProductResponse> response = ApiResponse.success("Tạo biến thể sản phẩm thành công", variant);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Lỗi khi tạo biến thể sản phẩm: ", e);

            ApiResponse<ProductResponse> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy sản phẩm tồn kho thấp
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Lấy sản phẩm tồn kho thấp", description = "Lấy danh sách sản phẩm có tồn kho thấp")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getLowStockProducts() {

        log.info("API lấy sản phẩm tồn kho thấp");

        try {
            List<ProductResponse> products = productService.getLowStockProducts();

            ApiResponse<List<ProductResponse>> response = ApiResponse.success(products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm tồn kho thấp: ", e);

            ApiResponse<List<ProductResponse>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
