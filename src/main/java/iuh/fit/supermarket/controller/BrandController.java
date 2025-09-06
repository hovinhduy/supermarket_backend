package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.brand.BrandCreateRequest;
import iuh.fit.supermarket.dto.brand.BrandResponse;
import iuh.fit.supermarket.dto.brand.BrandUpdateRequest;
import iuh.fit.supermarket.dto.common.ApiResponse;

import iuh.fit.supermarket.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý thương hiệu
 */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brand Management", description = "APIs cho quản lý thương hiệu sản phẩm")
@SecurityRequirement(name = "Bearer Authentication")
public class BrandController {

    private final BrandService brandService;

    /**
     * Tạo mới thương hiệu
     */
    @PostMapping
    @Operation(summary = "Tạo mới thương hiệu", description = "Tạo mới một thương hiệu trong hệ thống")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thương hiệu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Thương hiệu đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
            @Valid @RequestBody BrandCreateRequest request) {

        log.info("API tạo mới thương hiệu với tên: {}", request.getName());

        try {
            BrandResponse brandResponse = brandService.createBrand(request);

            ApiResponse<BrandResponse> response = ApiResponse.success(
                    "Tạo thương hiệu thành công", brandResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi tạo thương hiệu: {}", e.getMessage());

            ApiResponse<BrandResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi tạo thương hiệu: ", e);

            ApiResponse<BrandResponse> response = ApiResponse.error(
                    "Có lỗi xảy ra khi tạo thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật thông tin thương hiệu
     */
    @PutMapping("/{brandId}")
    @Operation(summary = "Cập nhật thương hiệu", description = "Cập nhật thông tin thương hiệu theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thương hiệu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Thông tin thương hiệu đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
            @Parameter(description = "ID của thương hiệu", required = true) @PathVariable Integer brandId,
            @Valid @RequestBody BrandUpdateRequest request) {

        log.info("API cập nhật thương hiệu ID: {} với tên: {}", brandId, request.getName());

        try {
            BrandResponse brandResponse = brandService.updateBrand(brandId, request);

            ApiResponse<BrandResponse> response = ApiResponse.success(
                    "Cập nhật thương hiệu thành công", brandResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi cập nhật thương hiệu: {}", e.getMessage());

            ApiResponse<BrandResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi cập nhật thương hiệu: ", e);

            ApiResponse<BrandResponse> response = ApiResponse.error(
                    "Có lỗi xảy ra khi cập nhật thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa thương hiệu
     */
    @DeleteMapping("/{brandId}")
    @Operation(summary = "Xóa thương hiệu", description = "Xóa thương hiệu theo ID (soft delete)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thương hiệu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Không thể xóa thương hiệu do có ràng buộc"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<Void>> deleteBrand(
            @Parameter(description = "ID của thương hiệu", required = true) @PathVariable Integer brandId) {

        log.info("API xóa thương hiệu ID: {}", brandId);

        try {
            brandService.deleteBrand(brandId);

            ApiResponse<Void> response = ApiResponse.success(
                    "Xóa thương hiệu thành công", null);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy thương hiệu: {}", e.getMessage());

            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalStateException e) {
            log.error("Lỗi business logic khi xóa thương hiệu: {}", e.getMessage());

            ApiResponse<Void> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi xóa thương hiệu: ", e);

            ApiResponse<Void> response = ApiResponse.error(
                    "Có lỗi xảy ra khi xóa thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin thương hiệu theo ID
     */
    @GetMapping("/{brandId}")
    @Operation(summary = "Lấy thông tin thương hiệu", description = "Lấy thông tin chi tiết thương hiệu theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(
            @Parameter(description = "ID của thương hiệu", required = true) @PathVariable Integer brandId) {

        log.info("API lấy thông tin thương hiệu ID: {}", brandId);

        try {
            BrandResponse brandResponse = brandService.getBrandById(brandId);

            ApiResponse<BrandResponse> response = ApiResponse.success(
                    "Lấy thông tin thương hiệu thành công", brandResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy thương hiệu: {}", e.getMessage());

            ApiResponse<BrandResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi lấy thông tin thương hiệu: ", e);

            ApiResponse<BrandResponse> response = ApiResponse.error(
                    "Có lỗi xảy ra khi lấy thông tin thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả thương hiệu đang hoạt động
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách thương hiệu", description = "Lấy danh sách tất cả thương hiệu đang hoạt động")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllActiveBrands() {

        log.info("API lấy danh sách tất cả thương hiệu đang hoạt động");

        try {
            List<BrandResponse> brands = brandService.getAllActiveBrands();

            ApiResponse<List<BrandResponse>> response = ApiResponse.success(
                    "Lấy danh sách thương hiệu thành công", brands);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi lấy danh sách thương hiệu: ", e);

            ApiResponse<List<BrandResponse>> response = ApiResponse.error(
                    "Có lỗi xảy ra khi lấy danh sách thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách thương hiệu với phân trang
     */
    @GetMapping("/paged")
    @Operation(summary = "Lấy danh sách thương hiệu có phân trang", description = "Lấy danh sách thương hiệu với phân trang và sắp xếp")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tham số phân trang không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<Page<BrandResponse>>> getAllBrandsWithPaging(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("API lấy danh sách thương hiệu có phân trang - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        try {
            // Validate tham số phân trang
            if (page < 0) {
                throw new IllegalArgumentException("Số trang không được nhỏ hơn 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Kích thước trang phải từ 1 đến 100");
            }

            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<BrandResponse> brandPage = brandService.getAllBrands(pageable);

            ApiResponse<Page<BrandResponse>> response = ApiResponse.success(
                    "Lấy danh sách thương hiệu thành công", brandPage);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi tham số phân trang: {}", e.getMessage());

            ApiResponse<Page<BrandResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi lấy danh sách thương hiệu có phân trang: ", e);

            ApiResponse<Page<BrandResponse>> response = ApiResponse.error(
                    "Có lỗi xảy ra khi lấy danh sách thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tìm kiếm thương hiệu
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm thương hiệu", description = "Tìm kiếm thương hiệu theo từ khóa")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Từ khóa tìm kiếm không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<List<BrandResponse>>> searchBrands(
            @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword) {

        log.info("API tìm kiếm thương hiệu với từ khóa: {}", keyword);

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống");
            }

            List<BrandResponse> brands = brandService.searchBrands(keyword);

            ApiResponse<List<BrandResponse>> response = ApiResponse.success(
                    "Tìm kiếm thương hiệu thành công", brands);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi tham số tìm kiếm: {}", e.getMessage());

            ApiResponse<List<BrandResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi tìm kiếm thương hiệu: ", e);

            ApiResponse<List<BrandResponse>> response = ApiResponse.error(
                    "Có lỗi xảy ra khi tìm kiếm thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tìm kiếm thương hiệu với phân trang
     */
    @GetMapping("/search/paged")
    @Operation(summary = "Tìm kiếm thương hiệu có phân trang", description = "Tìm kiếm thương hiệu theo từ khóa với phân trang")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Tham số không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<Page<BrandResponse>>> searchBrandsWithPaging(
            @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp", example = "name") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("API tìm kiếm thương hiệu có phân trang - keyword: {}, page: {}, size: {}",
                keyword, page, size);

        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống");
            }

            if (page < 0) {
                throw new IllegalArgumentException("Số trang không được nhỏ hơn 0");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Kích thước trang phải từ 1 đến 100");
            }

            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<BrandResponse> brandPage = brandService.searchBrands(keyword, pageable);

            ApiResponse<Page<BrandResponse>> response = ApiResponse.success(
                    "Tìm kiếm thương hiệu thành công", brandPage);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi tham số: {}", e.getMessage());

            ApiResponse<Page<BrandResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi tìm kiếm thương hiệu có phân trang: ", e);

            ApiResponse<Page<BrandResponse>> response = ApiResponse.error(
                    "Có lỗi xảy ra khi tìm kiếm thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Kích hoạt/vô hiệu hóa thương hiệu
     */
    @PatchMapping("/{brandId}/status")
    @Operation(summary = "Thay đổi trạng thái thương hiệu", description = "Kích hoạt hoặc vô hiệu hóa thương hiệu")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thay đổi trạng thái thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy thương hiệu"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    public ResponseEntity<ApiResponse<BrandResponse>> toggleBrandStatus(
            @Parameter(description = "ID của thương hiệu", required = true) @PathVariable Integer brandId,
            @Parameter(description = "Trạng thái mới (true: kích hoạt, false: vô hiệu hóa)", required = true) @RequestParam Boolean isActive) {

        log.info("API thay đổi trạng thái thương hiệu ID: {} thành: {}", brandId, isActive);

        try {
            BrandResponse brandResponse = brandService.toggleBrandStatus(brandId, isActive);

            String message = isActive ? "Kích hoạt thương hiệu thành công" : "Vô hiệu hóa thương hiệu thành công";
            ApiResponse<BrandResponse> response = ApiResponse.success(message, brandResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy thương hiệu: {}", e.getMessage());

            ApiResponse<BrandResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi thay đổi trạng thái thương hiệu: ", e);

            ApiResponse<BrandResponse> response = ApiResponse.error(
                    "Có lỗi xảy ra khi thay đổi trạng thái thương hiệu");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}