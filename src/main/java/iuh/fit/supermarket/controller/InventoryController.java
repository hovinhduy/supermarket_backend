package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.inventory.InventoryDto;
import iuh.fit.supermarket.dto.inventory.InventoryHistoryDto;
import iuh.fit.supermarket.dto.inventory.InventoryHistoryRequest;
import iuh.fit.supermarket.dto.inventory.InventoryTransactionDto;
import iuh.fit.supermarket.dto.inventory.InventoryUpdateRequest;
import iuh.fit.supermarket.entity.Inventory;
import iuh.fit.supermarket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller xử lý các API liên quan đến quản lý tồn kho
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "APIs cho quản lý tồn kho sản phẩm")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Lấy thông tin tồn kho theo biến thể sản phẩm
     */
    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Lấy tồn kho theo biến thể", description = "Lấy danh sách tồn kho của một biến thể sản phẩm tại các kho khác nhau")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getInventoryByVariant(
            @PathVariable Long variantId) {

        log.info("API lấy tồn kho cho biến thể ID: {}", variantId);

        try {
            List<Inventory> inventories = inventoryService.getInventoriesByVariant(variantId);
            List<InventoryDto> inventoryDtos = inventories.stream()
                    .map(this::mapToInventoryDto)
                    .collect(Collectors.toList());

            ApiResponse<List<InventoryDto>> response = ApiResponse.success(inventoryDtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin tồn kho: ", e);
            ApiResponse<List<InventoryDto>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy tổng số lượng tồn kho của một biến thể
     */
    @GetMapping("/variant/{variantId}/total")
    @Operation(summary = "Lấy tổng tồn kho", description = "Lấy tổng số lượng tồn kho của một biến thể trên tất cả kho")
    public ResponseEntity<ApiResponse<Integer>> getTotalQuantityByVariant(
            @PathVariable Long variantId) {

        log.info("API lấy tổng tồn kho cho biến thể ID: {}", variantId);

        try {
            Integer totalQuantity = inventoryService.getTotalQuantityByVariant(variantId);
            ApiResponse<Integer> response = ApiResponse.success(totalQuantity);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tổng tồn kho: ", e);
            ApiResponse<Integer> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật số lượng tồn kho
     */
    @PostMapping("/update")
    @Operation(summary = "Cập nhật tồn kho", description = "Cập nhật số lượng tồn kho cho một biến thể tại kho cụ thể")
    public ResponseEntity<ApiResponse<InventoryDto>> updateInventory(
            @RequestBody InventoryUpdateRequest request) {

        log.info("API cập nhật tồn kho cho biến thể {} tại kho {} với thay đổi {}",
                request.getVariantId(), request.getWarehouseId(), request.getQuantityChange());

        try {
            Inventory inventory = inventoryService.updateInventory(
                    request.getVariantId(),
                    request.getWarehouseId(),
                    request.getQuantityChange(),
                    request.getTransactionType(),
                    request.getUnitCostPrice(),
                    request.getReferenceId(),
                    request.getNotes());

            InventoryDto inventoryDto = mapToInventoryDto(inventory);
            ApiResponse<InventoryDto> response = ApiResponse.success("Cập nhật tồn kho thành công", inventoryDto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tồn kho: ", e);
            ApiResponse<InventoryDto> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Lấy tồn kho thấp", description = "Lấy danh sách sản phẩm có tồn kho thấp cần đặt hàng lại")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> getLowStockInventories() {

        log.info("API lấy danh sách tồn kho thấp");

        try {
            List<Inventory> inventories = inventoryService.getLowStockInventories();
            List<InventoryDto> inventoryDtos = inventories.stream()
                    .map(this::mapToInventoryDto)
                    .collect(Collectors.toList());

            ApiResponse<List<InventoryDto>> response = ApiResponse.success(inventoryDtos);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tồn kho thấp: ", e);
            ApiResponse<List<InventoryDto>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy lịch sử giao dịch kho theo biến thể
     */
    @GetMapping("/variant/{variantId}/transactions")
    @Operation(summary = "Lấy lịch sử giao dịch", description = "Lấy lịch sử giao dịch kho của một biến thể sản phẩm")
    public ResponseEntity<ApiResponse<List<InventoryTransactionDto>>> getTransactionHistory(
            @PathVariable Long variantId) {

        log.info("API lấy lịch sử giao dịch cho biến thể ID: {}", variantId);

        try {
            List<InventoryTransactionDto> transactions = inventoryService.getTransactionHistoryByVariant(variantId);
            ApiResponse<List<InventoryTransactionDto>> response = ApiResponse.success(transactions);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử giao dịch: ", e);
            ApiResponse<List<InventoryTransactionDto>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách lịch sử thay đổi kho hàng với phân trang, tìm kiếm và sắp xếp
     */
    @GetMapping("/history")
    @Operation(summary = "Lấy lịch sử thay đổi kho hàng", description = "Lấy danh sách lịch sử thay đổi kho hàng với hỗ trợ phân trang, tìm kiếm theo mã/tên sản phẩm và sắp xếp đa trường")
    public ResponseEntity<ApiResponse<Page<InventoryHistoryDto>>> getInventoryHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String sortField,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        log.info("API lấy lịch sử thay đổi kho hàng: page={}, limit={}, search={}", page, limit, search);

        try {
            // Tạo request object từ parameters
            InventoryHistoryRequest request = new InventoryHistoryRequest();
            request.setPage(page);
            request.setLimit(limit);
            request.setSearch(search);

            // Thêm sort criteria nếu có
            if (sortField != null && !sortField.trim().isEmpty()) {
                InventoryHistoryRequest.SortCriteria sortCriteria = new InventoryHistoryRequest.SortCriteria();
                sortCriteria.setField(sortField);
                sortCriteria.setOrder(sortOrder);
                request.setSorts(List.of(sortCriteria));
            }

            Page<InventoryHistoryDto> historyPage = inventoryService.getInventoryHistory(request);
            ApiResponse<Page<InventoryHistoryDto>> response = ApiResponse.success(historyPage);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử thay đổi kho hàng: ", e);
            ApiResponse<Page<InventoryHistoryDto>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Map Inventory entity thành InventoryDto
     */
    private InventoryDto mapToInventoryDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setInventoryId(inventory.getInventoryId());
        dto.setVariantId(inventory.getVariant().getVariantId());
        dto.setVariantCode(inventory.getVariant().getVariantCode());
        dto.setVariantName(inventory.getVariant().getVariantName());
        dto.setWarehouseId(inventory.getWarehouse().getWarehouseId());
        dto.setWarehouseName(inventory.getWarehouse().getName());
        dto.setQuantityOnHand(inventory.getQuantityOnHand());
        dto.setQuantityReserved(inventory.getQuantityReserved());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        dto.setReorderPoint(inventory.getReorderPoint());
        dto.setNeedsReorder(inventory.needsReorder());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        return dto;
    }
}
