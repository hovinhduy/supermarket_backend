package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.BaseUnitInventoryService;
import iuh.fit.supermarket.service.BaseUnitInventoryService.VariantInventoryInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý tồn kho dựa trên đơn vị cơ bản
 * Chỉ lưu tồn kho cho biến thể có đơn vị cơ bản, tính toán cho các biến thể khác
 */
@RestController
@RequestMapping("/api/inventory/base-unit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Base Unit Inventory Management", description = "APIs cho quản lý tồn kho dựa trên đơn vị cơ bản")
public class BaseUnitInventoryController {

    private final BaseUnitInventoryService baseUnitInventoryService;

    /**
     * Lấy số lượng tồn kho cho biến thể tại kho cụ thể
     */
    @GetMapping("/variants/{variantId}/warehouses/{warehouseId}/quantity")
    @Operation(summary = "Lấy số lượng tồn kho", description = "Lấy số lượng tồn kho cho biến thể tại kho cụ thể")
    public ResponseEntity<ApiResponse<Integer>> getQuantityOnHandForVariant(
            @PathVariable Long variantId,
            @PathVariable Integer warehouseId) {

        log.info("API lấy số lượng tồn kho cho biến thể {} tại kho {}", variantId, warehouseId);

        try {
            Integer quantity = baseUnitInventoryService.getQuantityOnHandForVariant(variantId, warehouseId);

            ApiResponse<Integer> response = ApiResponse.success(quantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy số lượng tồn kho: ", e);

            ApiResponse<Integer> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy số lượng có thể bán cho biến thể tại kho cụ thể
     */
    @GetMapping("/variants/{variantId}/warehouses/{warehouseId}/available")
    @Operation(summary = "Lấy số lượng có thể bán", description = "Lấy số lượng có thể bán cho biến thể tại kho cụ thể")
    public ResponseEntity<ApiResponse<Integer>> getAvailableQuantityForVariant(
            @PathVariable Long variantId,
            @PathVariable Integer warehouseId) {

        log.info("API lấy số lượng có thể bán cho biến thể {} tại kho {}", variantId, warehouseId);

        try {
            Integer quantity = baseUnitInventoryService.getAvailableQuantityForVariant(variantId, warehouseId);

            ApiResponse<Integer> response = ApiResponse.success(quantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy số lượng có thể bán: ", e);

            ApiResponse<Integer> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy tổng số lượng tồn kho cho biến thể trên tất cả kho
     */
    @GetMapping("/variants/{variantId}/total-quantity")
    @Operation(summary = "Lấy tổng số lượng tồn kho", description = "Lấy tổng số lượng tồn kho cho biến thể trên tất cả kho")
    public ResponseEntity<ApiResponse<Integer>> getTotalQuantityOnHandForVariant(
            @PathVariable Long variantId) {

        log.info("API lấy tổng số lượng tồn kho cho biến thể {}", variantId);

        try {
            Integer quantity = baseUnitInventoryService.getTotalQuantityOnHandForVariant(variantId);

            ApiResponse<Integer> response = ApiResponse.success(quantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng số lượng tồn kho: ", e);

            ApiResponse<Integer> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy tổng số lượng có thể bán cho biến thể trên tất cả kho
     */
    @GetMapping("/variants/{variantId}/total-available")
    @Operation(summary = "Lấy tổng số lượng có thể bán", description = "Lấy tổng số lượng có thể bán cho biến thể trên tất cả kho")
    public ResponseEntity<ApiResponse<Integer>> getTotalAvailableQuantityForVariant(
            @PathVariable Long variantId) {

        log.info("API lấy tổng số lượng có thể bán cho biến thể {}", variantId);

        try {
            Integer quantity = baseUnitInventoryService.getTotalAvailableQuantityForVariant(variantId);

            ApiResponse<Integer> response = ApiResponse.success(quantity);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng số lượng có thể bán: ", e);

            ApiResponse<Integer> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Kiểm tra biến thể có cần đặt hàng lại không
     */
    @GetMapping("/variants/{variantId}/needs-reorder")
    @Operation(summary = "Kiểm tra cần đặt hàng lại", description = "Kiểm tra biến thể có cần đặt hàng lại không")
    public ResponseEntity<ApiResponse<Boolean>> needsReorderForVariant(
            @PathVariable Long variantId) {

        log.info("API kiểm tra cần đặt hàng lại cho biến thể {}", variantId);

        try {
            Boolean needsReorder = baseUnitInventoryService.needsReorderForVariant(variantId);

            ApiResponse<Boolean> response = ApiResponse.success(needsReorder);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra cần đặt hàng lại: ", e);

            ApiResponse<Boolean> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách tồn kho của tất cả biến thể trong một sản phẩm
     */
    @GetMapping("/products/{productId}/variants")
    @Operation(summary = "Lấy tồn kho tất cả biến thể", description = "Lấy danh sách tồn kho của tất cả biến thể trong một sản phẩm")
    public ResponseEntity<ApiResponse<List<VariantInventoryInfo>>> getVariantInventoriesForProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer warehouseId) {

        log.info("API lấy tồn kho tất cả biến thể cho sản phẩm {} tại kho {}", productId, warehouseId);

        try {
            List<VariantInventoryInfo> inventories = baseUnitInventoryService
                    .getVariantInventoriesForProduct(productId, warehouseId);

            ApiResponse<List<VariantInventoryInfo>> response = ApiResponse.success(inventories);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tồn kho tất cả biến thể: ", e);

            ApiResponse<List<VariantInventoryInfo>> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Kiểm tra có thể tạo tồn kho cho biến thể này không
     */
    @GetMapping("/variants/{variantId}/can-create-inventory")
    @Operation(summary = "Kiểm tra có thể tạo tồn kho", description = "Kiểm tra có thể tạo tồn kho cho biến thể này không")
    public ResponseEntity<ApiResponse<Boolean>> canCreateInventoryForVariant(
            @PathVariable Long variantId) {

        log.info("API kiểm tra có thể tạo tồn kho cho biến thể {}", variantId);

        try {
            Boolean canCreate = baseUnitInventoryService.canCreateInventoryForVariant(variantId);

            ApiResponse<Boolean> response = ApiResponse.success(canCreate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra có thể tạo tồn kho: ", e);

            ApiResponse<Boolean> response = ApiResponse.error(e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
