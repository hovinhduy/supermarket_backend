package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.address.CreateAddressRequest;
import iuh.fit.supermarket.dto.address.CustomerAddressDto;
import iuh.fit.supermarket.dto.address.UpdateAddressRequest;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.CustomerAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến địa chỉ giao hàng của khách hàng
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Address Management", description = "API quản lý địa chỉ giao hàng của khách hàng")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    // ==================== API CHO KHÁCH HÀNG (MY ADDRESSES) ====================

    /**
     * Khách hàng lấy danh sách địa chỉ của mình
     */
    @Operation(summary = "Lấy danh sách địa chỉ của tôi", 
               description = "API dành cho khách hàng lấy danh sách địa chỉ giao hàng của chính mình")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", 
                    description = "Lấy danh sách địa chỉ thành công",
                    content = @Content(schema = @Schema(implementation = CustomerAddressDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", 
                    description = "Chưa đăng nhập hoặc token không hợp lệ")
    })
    @GetMapping("/my-addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<CustomerAddressDto>>> getMyAddresses() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu lấy danh sách địa chỉ", username);

        List<CustomerAddressDto> addresses = customerAddressService.getMyAddresses(username);

        log.info("Lấy danh sách địa chỉ thành công cho khách hàng {} - {} địa chỉ", username, addresses.size());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách địa chỉ thành công", addresses));
    }

    /**
     * Khách hàng thêm địa chỉ mới
     */
    @Operation(summary = "Thêm địa chỉ mới", 
               description = "API dành cho khách hàng thêm địa chỉ giao hàng mới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", 
                    description = "Thêm địa chỉ thành công",
                    content = @Content(schema = @Schema(implementation = CustomerAddressDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", 
                    description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", 
                    description = "Chưa đăng nhập hoặc token không hợp lệ")
    })
    @PostMapping("/my-addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> createMyAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu thêm địa chỉ mới", username);

        CustomerAddressDto address = customerAddressService.createMyAddress(username, request);

        log.info("Thêm địa chỉ thành công cho khách hàng {} - ID: {}", username, address.getAddressId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm địa chỉ thành công", address));
    }

    /**
     * Khách hàng cập nhật địa chỉ
     */
    @Operation(summary = "Cập nhật địa chỉ", 
               description = "API dành cho khách hàng cập nhật địa chỉ giao hàng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", 
                    description = "Cập nhật địa chỉ thành công",
                    content = @Content(schema = @Schema(implementation = CustomerAddressDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", 
                    description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", 
                    description = "Không tìm thấy địa chỉ")
    })
    @PutMapping("/my-addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> updateMyAddress(
            @Parameter(description = "ID địa chỉ") @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu cập nhật địa chỉ ID: {}", username, addressId);

        CustomerAddressDto address = customerAddressService.updateMyAddress(username, addressId, request);

        log.info("Cập nhật địa chỉ thành công cho khách hàng {} - ID: {}", username, addressId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công", address));
    }

    /**
     * Khách hàng xóa địa chỉ
     */
    @Operation(summary = "Xóa địa chỉ", 
               description = "API dành cho khách hàng xóa địa chỉ giao hàng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", 
                    description = "Xóa địa chỉ thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", 
                    description = "Không tìm thấy địa chỉ")
    })
    @DeleteMapping("/my-addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<String>> deleteMyAddress(
            @Parameter(description = "ID địa chỉ") @PathVariable Long addressId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu xóa địa chỉ ID: {}", username, addressId);

        customerAddressService.deleteMyAddress(username, addressId);

        log.info("Xóa địa chỉ thành công cho khách hàng {} - ID: {}", username, addressId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa địa chỉ thành công"));
    }

    /**
     * Khách hàng đặt địa chỉ mặc định
     */
    @Operation(summary = "Đặt địa chỉ mặc định", 
               description = "API dành cho khách hàng đặt một địa chỉ làm mặc định")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", 
                    description = "Đặt địa chỉ mặc định thành công",
                    content = @Content(schema = @Schema(implementation = CustomerAddressDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", 
                    description = "Không tìm thấy địa chỉ")
    })
    @PostMapping("/my-addresses/{addressId}/set-default")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> setDefaultAddress(
            @Parameter(description = "ID địa chỉ") @PathVariable Long addressId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu đặt địa chỉ mặc định ID: {}", username, addressId);

        CustomerAddressDto address = customerAddressService.setDefaultAddress(username, addressId);

        log.info("Đặt địa chỉ mặc định thành công cho khách hàng {} - ID: {}", username, addressId);
        return ResponseEntity.ok(ApiResponse.success("Đặt địa chỉ mặc định thành công", address));
    }

    /**
     * Khách hàng lấy địa chỉ mặc định
     */
    @Operation(summary = "Lấy địa chỉ mặc định", 
               description = "API dành cho khách hàng lấy địa chỉ giao hàng mặc định")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", 
                    description = "Lấy địa chỉ mặc định thành công",
                    content = @Content(schema = @Schema(implementation = CustomerAddressDto.class)))
    })
    @GetMapping("/my-addresses/default")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerAddressDto>> getDefaultAddress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Khách hàng {} yêu cầu lấy địa chỉ mặc định", username);

        CustomerAddressDto address = customerAddressService.getDefaultAddress(username);

        if (address == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa có địa chỉ mặc định", null));
        }

        log.info("Lấy địa chỉ mặc định thành công cho khách hàng {}", username);
        return ResponseEntity.ok(ApiResponse.success("Lấy địa chỉ mặc định thành công", address));
    }

}
