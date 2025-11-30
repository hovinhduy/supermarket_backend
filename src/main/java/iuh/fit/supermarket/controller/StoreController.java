package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.store.StoreDTO;
import iuh.fit.supermarket.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller cho quản lý thông tin cửa hàng
 */
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
@Tag(name = "Store", description = "API quản lý thông tin cửa hàng")
public class StoreController {

    private final StoreService storeService;

    /**
     * Lấy danh sách cửa hàng đang hoạt động
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách cửa hàng", description = "Lấy danh sách tất cả cửa hàng đang hoạt động để khách hàng chọn khi checkout")
    public ResponseEntity<ApiResponse<List<StoreDTO>>> getActiveStores() {
        List<StoreDTO> stores = storeService.getActiveStores();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cửa hàng thành công", stores));
    }

    /**
     * Lấy thông tin cửa hàng theo ID
     */
    @GetMapping("/{storeId}")
    @Operation(summary = "Lấy thông tin cửa hàng", description = "Lấy thông tin chi tiết của một cửa hàng theo ID")
    public ResponseEntity<ApiResponse<StoreDTO>> getStoreById(@PathVariable Long storeId) {
        StoreDTO store = storeService.getStoreById(storeId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin cửa hàng thành công", store));
    }
}
