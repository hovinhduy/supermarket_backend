package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.ProductCreateRequest;
import iuh.fit.supermarket.dto.product.ProductListResponse;
import iuh.fit.supermarket.dto.product.ProductPageableRequest;
import iuh.fit.supermarket.dto.product.ProductResponse;
import iuh.fit.supermarket.dto.product.ProductUpdateRequest;
import iuh.fit.supermarket.dto.product.ProductUnitDetailResponse;
import iuh.fit.supermarket.dto.product.ProductUnitRequest;
import iuh.fit.supermarket.dto.product.ProductUnitUpdateRequest;
import iuh.fit.supermarket.dto.product.ProductUnitResponse;
import iuh.fit.supermarket.service.ProductService;
import iuh.fit.supermarket.service.ProductExcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý sản phẩm
 */
@RestController
@RequestMapping("/api/products")
@Slf4j
@Tag(name = "Product Management", description = "APIs cho quản lý sản phẩm")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductExcelService productExcelService;
    private final iuh.fit.supermarket.repository.UserRepository userRepository;
    private final iuh.fit.supermarket.repository.CustomerRepository customerRepository;

    /**
     * API tạo sản phẩm mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo sản phẩm mới", description = "Tạo một sản phẩm mới trong hệ thống")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo sản phẩm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Sản phẩm đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        log.info("API tạo sản phẩm mới: {}", request.getName());

        try {
            ProductResponse createdProduct = productService.createProduct(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo sản phẩm thành công", createdProduct));
        } catch (Exception e) {
            log.error("Lỗi khi tạo sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy thông tin sản phẩm theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin sản phẩm theo ID", description = "Trả về thông tin chi tiết của sản phẩm theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long id) {
        log.info("API lấy thông tin sản phẩm với ID: {}", id);

        try {
            ProductResponse product = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API tìm kiếm sản phẩm theo barcode
     */
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Tìm kiếm sản phẩm theo barcode", description = "Tìm kiếm và trả về thông tin sản phẩm dựa trên mã vạch của đơn vị sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Mã vạch không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm với mã vạch này")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByBarcode(
            @Parameter(description = "Mã vạch sản phẩm", example = "1234567890123") @PathVariable String barcode) {
        log.info("API tìm kiếm sản phẩm với barcode: {}", barcode);

        try {
            ProductResponse product = productService.getProductByBarcode(barcode);
            return ResponseEntity.ok(ApiResponse.success("Tìm thấy sản phẩm", product));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm sản phẩm theo barcode: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }


    /**
     * API cập nhật thông tin sản phẩm
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin sản phẩm", description = "Cập nhật thông tin sản phẩm theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        log.info("API cập nhật sản phẩm với ID: {}", id);

        try {
            ProductResponse updatedProduct = productService.updateProduct(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", updatedProduct));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API xóa sản phẩm
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa sản phẩm", description = "Xóa sản phẩm khỏi hệ thống (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @Parameter(description = "ID của sản phẩm") @PathVariable Long id) {
        log.info("API xóa sản phẩm với ID: {}", id);

        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API xóa nhiều sản phẩm
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa nhiều sản phẩm", description = "Xóa nhiều sản phẩm khỏi hệ thống (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<String>> deleteMultipleProducts(
            @Parameter(description = "Danh sách ID sản phẩm cần xóa") @RequestBody List<Long> ids) {
        log.info("API xóa nhiều sản phẩm: {} sản phẩm", ids != null ? ids.size() : 0);

        try {
            productService.deleteMultipleProducts(ids);
            int deletedCount = (ids != null) ? ids.size() : 0;
            return ResponseEntity.ok(ApiResponse.success("Xóa " + deletedCount + " sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi xóa nhiều sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy danh sách sản phẩm với phân trang và lọc
     * Hỗ trợ tìm kiếm theo tên sản phẩm hoặc mã sản phẩm
     */
    @PostMapping("/search")
    @Operation(summary = "Lấy danh sách sản phẩm", description = "Lấy danh sách sản phẩm với phân trang, tìm kiếm theo tên hoặc mã sản phẩm, và lọc theo các tiêu chí")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<ApiResponse<ProductListResponse>> getProducts(
            @Valid @RequestBody ProductPageableRequest request) {
        log.info(
                "API lấy danh sách sản phẩm với filter: searchTerm={}, categoryId={}, brandId={}, isActive={}, isRewardPoint={}, hasPrice={}, hasStock={}, page={}, size={}",
                request.getSearchTerm(), request.getCategoryId(), request.getBrandId(),
                request.getIsActive(), request.getIsRewardPoint(), request.getHasPrice(), request.getHasStock(),
                request.getPage(), request.getSize());

        try {
            // Lấy customerId nếu user là customer (để check favorite)
            Integer customerId = getCustomerIdIfAuthenticated();

            // Tạo Pageable từ request
            Pageable pageable = createPageableFromRequest(request);

            ProductListResponse products = productService.getProducts(
                    request.getSearchTermTrimmed(),
                    request.getCategoryId(),
                    request.getBrandId(),
                    request.getIsActive(),
                    request.getIsRewardPoint(),
                    request.getHasPrice(),
                    request.getHasStock(),
                    customerId,
                    pageable);
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy tất cả sản phẩm đang hoạt động
     */
    @GetMapping("/active")
    @Operation(summary = "Lấy tất cả sản phẩm đang hoạt động", description = "Lấy danh sách tất cả sản phẩm đang hoạt động (không phân trang)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công")
    })
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllActiveProducts() {
        log.info("API lấy tất cả sản phẩm đang hoạt động");

        try {
            List<ProductResponse> products = productService.getAllActiveProducts();
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sản phẩm hoạt động: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy sản phẩm theo danh mục
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Lấy sản phẩm theo danh mục", description = "Lấy danh sách sản phẩm theo danh mục với phân trang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    public ResponseEntity<ApiResponse<ProductListResponse>> getProductsByCategory(
            @Parameter(description = "ID danh mục") @PathVariable Integer categoryId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("API lấy sản phẩm theo danh mục ID: {}", categoryId);

        try {
            ProductListResponse products = productService.getProductsByCategory(categoryId, pageable);
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo danh mục: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy sản phẩm theo thương hiệu
     */
    @GetMapping("/brand/{brandId}")
    @Operation(summary = "Lấy sản phẩm theo thương hiệu", description = "Lấy danh sách sản phẩm theo thương hiệu với phân trang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu")
    })
    public ResponseEntity<ApiResponse<ProductListResponse>> getProductsByBrand(
            @Parameter(description = "ID thương hiệu") @PathVariable Integer brandId,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("API lấy sản phẩm theo thương hiệu ID: {}", brandId);

        try {
            ProductListResponse products = productService.getProductsByBrand(brandId, pageable);
            return ResponseEntity.ok(ApiResponse.success(products));
        } catch (Exception e) {
            log.error("Lỗi khi lấy sản phẩm theo thương hiệu: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API tìm kiếm sản phẩm
     */
    // @GetMapping("/search")
    // @Operation(summary = "Tìm kiếm sản phẩm", description = "Tìm kiếm sản phẩm
    // theo từ khóa với phân trang")
    // @ApiResponses(value = {
    // @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
    // description = "Tìm kiếm thành công"),
    // @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
    // description = "Từ khóa không hợp lệ")
    // })
    // public ResponseEntity<ApiResponse<ProductListResponse>> searchProducts(
    // @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
    // @PageableDefault(size = 10) Pageable pageable) {
    // log.info("API tìm kiếm sản phẩm với từ khóa: {}", keyword);

    // try {
    // ProductListResponse products = productService.searchProducts(keyword,
    // pageable);
    // return ResponseEntity.ok(ApiResponse.success(products));
    // } catch (Exception e) {
    // log.error("Lỗi khi tìm kiếm sản phẩm: ", e);
    // return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    // }
    // }

    /**
     * Tạo Pageable từ ProductPageableRequest
     */
    private Pageable createPageableFromRequest(ProductPageableRequest request) {
        // Xử lý sort
        Sort sort = Sort.unsorted();
        if (request.getSort() != null && !request.getSort().isEmpty()) {
            Sort.Order[] orders = request.getSort().stream()
                    .map(this::parseSortString)
                    .toArray(Sort.Order[]::new);
            sort = Sort.by(orders);
        }

        return PageRequest.of(request.getValidPage(), request.getValidSize(), sort);
    }

    /**
     * Phân tích chuỗi sort thành Sort.Order
     * Format: "fieldName" hoặc "fieldName,asc" hoặc "fieldName,desc"
     */
    private Sort.Order parseSortString(String sortStr) {
        if (sortStr == null || sortStr.trim().isEmpty()) {
            return Sort.Order.asc("createdAt"); // default sort
        }

        String[] parts = sortStr.split(",");
        String property = parts[0].trim();

        // Validate property name để tránh lỗi bảo mật
        if (!isValidSortProperty(property)) {
            property = "createdAt"; // fallback to safe default
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            String directionStr = parts[1].trim().toLowerCase();
            if ("desc".equals(directionStr)) {
                direction = Sort.Direction.DESC;
            }
        }

        return new Sort.Order(direction, property);
    }

    /**
     * Kiểm tra tên trường sort có hợp lệ không
     */
    private boolean isValidSortProperty(String property) {
        // Chỉ cho phép các trường an toàn
        return property != null && ("id".equals(property) ||
                "name".equals(property) ||
                "productCode".equals(property) ||
                "description".equals(property) ||
                "isActive".equals(property) ||
                "isRewardPoint".equals(property) ||
                "createdAt".equals(property) ||
                "updatedAt".equals(property));
    }

    // ==================== QUẢN LÝ ĐƠN VỊ SẢN PHẨM ====================

    /**
     * API thêm đơn vị mới vào sản phẩm
     */
    @PostMapping("/{productId}/units")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Thêm đơn vị mới vào sản phẩm", description = "Thêm một đơn vị tính mới cho sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Thêm đơn vị thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Đơn vị đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<ProductUnitResponse>> addProductUnit(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Valid @RequestBody ProductUnitRequest request) {
        log.info("API thêm đơn vị mới cho sản phẩm ID: {}, unit: {}", productId, request.unitName());

        try {
            ProductUnitResponse productUnit = productService.addProductUnit(productId, request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Thêm đơn vị sản phẩm thành công", productUnit));
        } catch (Exception e) {
            log.error("Lỗi khi thêm đơn vị sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API cập nhật thông tin đơn vị sản phẩm
     */
    @PutMapping("/{productId}/units/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin đơn vị sản phẩm", description = "Cập nhật thông tin đơn vị tính của sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc đơn vị"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<ProductUnitResponse>> updateProductUnit(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Parameter(description = "ID đơn vị sản phẩm") @PathVariable Long unitId,
            @Valid @RequestBody ProductUnitUpdateRequest request) {
        log.info("API cập nhật đơn vị sản phẩm ID: {} của sản phẩm ID: {}", unitId, productId);

        try {
            ProductUnitResponse productUnit = productService.updateProductUnit(productId, unitId, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn vị sản phẩm thành công", productUnit));
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật đơn vị sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API xóa đơn vị khỏi sản phẩm
     */
    @DeleteMapping("/{productId}/units/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xóa đơn vị khỏi sản phẩm", description = "Xóa đơn vị tính khỏi sản phẩm (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc đơn vị"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể xóa đơn vị cơ bản"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<String>> deleteProductUnit(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Parameter(description = "ID đơn vị sản phẩm") @PathVariable Long unitId) {
        log.info("API xóa đơn vị sản phẩm ID: {} khỏi sản phẩm ID: {}", unitId, productId);

        try {
            productService.deleteProductUnit(productId, unitId);
            return ResponseEntity.ok(ApiResponse.success("Xóa đơn vị sản phẩm thành công"));
        } catch (Exception e) {
            log.error("Lỗi khi xóa đơn vị sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy danh sách đơn vị của sản phẩm
     */
    @GetMapping("/{productId}/units")
    @Operation(summary = "Lấy danh sách đơn vị của sản phẩm", description = "Lấy tất cả đơn vị tính của sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    })
    public ResponseEntity<ApiResponse<List<ProductUnitResponse>>> getProductUnits(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {
        log.info("API lấy danh sách đơn vị của sản phẩm ID: {}", productId);

        try {
            List<ProductUnitResponse> units = productService.getProductUnits(productId);
            return ResponseEntity.ok(ApiResponse.success(units));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách đơn vị sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy thông tin đơn vị sản phẩm theo ID
     */
    @GetMapping("/{productId}/units/{unitId}")
    @Operation(summary = "Lấy thông tin đơn vị sản phẩm", description = "Lấy thông tin chi tiết của một đơn vị sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc đơn vị")
    })
    public ResponseEntity<ApiResponse<ProductUnitResponse>> getProductUnit(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId,
            @Parameter(description = "ID đơn vị sản phẩm") @PathVariable Long unitId) {
        log.info("API lấy thông tin đơn vị sản phẩm ID: {} của sản phẩm ID: {}", unitId, productId);

        try {
            ProductUnitResponse unit = productService.getProductUnit(productId, unitId);
            return ResponseEntity.ok(ApiResponse.success(unit));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin đơn vị sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy đơn vị cơ bản của sản phẩm
     */
    @GetMapping("/{productId}/units/base")
    @Operation(summary = "Lấy đơn vị cơ bản của sản phẩm", description = "Lấy thông tin đơn vị cơ bản (có conversionValue = 1) của sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm hoặc đơn vị cơ bản")
    })
    public ResponseEntity<ApiResponse<ProductUnitResponse>> getBaseProductUnit(
            @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {
        log.info("API lấy đơn vị cơ bản của sản phẩm ID: {}", productId);

        try {
            ProductUnitResponse baseUnit = productService.getBaseProductUnit(productId);
            return ResponseEntity.ok(ApiResponse.success(baseUnit));
        } catch (Exception e) {
            log.error("Lỗi khi lấy đơn vị cơ bản của sản phẩm: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API tìm kiếm ProductUnit theo tên sản phẩm, mã code hoặc barcode
     */
    @GetMapping("/units/search")
    @Operation(summary = "Tìm kiếm ProductUnit", description = "Tìm kiếm ProductUnit theo tên sản phẩm, mã code hoặc barcode")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Từ khóa tìm kiếm không hợp lệ")
    })
    public ResponseEntity<ApiResponse<List<ProductUnitResponse>>> searchProductUnits(
            @Parameter(description = "Từ khóa tìm kiếm (tên sản phẩm, code, barcode)") @RequestParam String searchTerm) {
        log.info("API tìm kiếm ProductUnit với từ khóa: {}", searchTerm);

        try {
            List<ProductUnitResponse> productUnits = productService.searchProductUnits(searchTerm);
            String message = String.format("Tìm thấy %d kết quả", productUnits.size());
            return ResponseEntity.ok(ApiResponse.success(message, productUnits));
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm ProductUnit: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * API lấy thông tin chi tiết đầy đủ của ProductUnit
     */
    @GetMapping("/units/{productUnitId}/details")
    @Operation(summary = "Lấy thông tin chi tiết ProductUnit", description = "Lấy thông tin chi tiết đầy đủ của ProductUnit bao gồm tên sản phẩm, tên đơn vị, số lượng tồn kho và giá hiện tại")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy ProductUnit")
    })
    public ResponseEntity<ApiResponse<ProductUnitDetailResponse>> getProductUnitDetails(
            @Parameter(description = "ID của ProductUnit") @PathVariable Long productUnitId) {
        log.info("API lấy thông tin chi tiết ProductUnit ID: {}", productUnitId);

        try {
            ProductUnitDetailResponse details = productService.getProductUnitDetails(productUnitId);
            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết thành công", details));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin chi tiết ProductUnit: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== EXCEL IMPORT/EXPORT ====================

    /**
     * API export danh sách sản phẩm ra file Excel
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Export sản phẩm ra Excel", description = "Export danh sách sản phẩm ra file Excel")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Export thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<byte[]> exportProductsToExcel() {
        log.info("API export danh sách sản phẩm ra Excel");

        try {
            // Lấy tất cả sản phẩm đang hoạt động
            List<ProductResponse> products = productService.getAllActiveProducts();

            // Export ra Excel
            byte[] excelData = productExcelService.exportProductsToExcel(products);

            // Tạo tên file với timestamp
            String fileName = "danh_sach_san_pham_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelData.length);

            log.info("Export Excel thành công với {} sản phẩm", products.size());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (IOException e) {
            log.error("Lỗi khi export Excel: ", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Lỗi khi export sản phẩm: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API import sản phẩm từ file Excel
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Import sản phẩm từ Excel", description = "Import danh sách sản phẩm từ file Excel")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Import thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc dữ liệu lỗi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<ApiResponse<String>> importProductsFromExcel(
            @Parameter(description = "File Excel chứa danh sách sản phẩm") @RequestParam("file") MultipartFile file) {
        log.info("API import sản phẩm từ file Excel: {}", file.getOriginalFilename());

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File không được để trống"));
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File phải có định dạng Excel (.xlsx hoặc .xls)"));
            }

            // Parse Excel file
            List<ProductCreateRequest> products = productExcelService.importProductsFromExcel(file);

            if (products.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File Excel không chứa dữ liệu sản phẩm hợp lệ"));
            }

            // Import products
            int successCount = 0;
            int errorCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (int i = 0; i < products.size(); i++) {
                try {
                    ProductCreateRequest product = products.get(i);
                    productService.createProduct(product);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    errorMessages.append("Dòng ").append(i + 2).append(": ").append(e.getMessage()).append("; ");
                    log.warn("Lỗi khi import sản phẩm dòng {}: {}", i + 2, e.getMessage());
                }
            }

            String message = String.format("Import hoàn tất: %d thành công, %d lỗi", successCount, errorCount);
            if (errorCount > 0) {
                message += ". Chi tiết lỗi: " + errorMessages.toString();
            }

            log.info("Import Excel hoàn tất: {} thành công, {} lỗi", successCount, errorCount);
            return ResponseEntity.ok(ApiResponse.success(message));

        } catch (IOException e) {
            log.error("Lỗi khi đọc file Excel: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi đọc file Excel: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi import sản phẩm: ", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi import: " + e.getMessage()));
        }
    }

    /**
     * API tải template Excel để import sản phẩm
     */
    @GetMapping("/import/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tải template Excel", description = "Tải file template Excel để import sản phẩm")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tải template thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền thực hiện")
    })
    public ResponseEntity<byte[]> downloadImportTemplate() {
        log.info("API tải template Excel import sản phẩm");

        try {
            byte[] templateData = productExcelService.createImportTemplate();

            String fileName = "template_import_san_pham.xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(templateData.length);

            log.info("Tải template Excel thành công");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateData);

        } catch (IOException e) {
            log.error("Lỗi khi tạo template Excel: ", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Lỗi khi tạo template: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy customer ID nếu user hiện tại là customer (đã authenticated)
     * Trả về null nếu không phải customer hoặc chưa đăng nhập
     *
     * @return Customer ID hoặc null
     */
    private Integer getCustomerIdIfAuthenticated() {
        try {
            org.springframework.security.core.Authentication authentication =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails)) {
                return null;
            }

            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) principal;
            String username = userDetails.getUsername();

            // Chỉ xử lý nếu là customer (có prefix "CUSTOMER:")
            if (!username.startsWith("CUSTOMER:")) {
                return null;
            }

            // Loại bỏ prefix "CUSTOMER:"
            final String emailOrPhone = username.substring(9);

            // Tìm User từ email hoặc phone
            iuh.fit.supermarket.entity.User user = userRepository.findByEmailAndIsDeletedFalse(emailOrPhone)
                    .or(() -> userRepository.findByPhoneAndIsDeletedFalse(emailOrPhone))
                    .orElse(null);

            if (user == null) {
                return null;
            }

            // Tìm Customer từ user_id
            iuh.fit.supermarket.entity.Customer customer = customerRepository.findByUser_UserId(user.getUserId())
                    .orElse(null);

            return customer != null ? customer.getCustomerId() : null;
        } catch (Exception e) {
            log.warn("Không thể lấy customer ID: {}", e.getMessage());
            return null;
        }
    }
}
