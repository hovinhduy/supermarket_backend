package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.sale.CreateSaleRequestDTO;
import iuh.fit.supermarket.dto.sale.CreateSaleResponseDTO;
import iuh.fit.supermarket.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến bán hàng
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;

    /**
     * API tạo bán hàng mới
     * - Kiểm tra tồn kho
     * - Trừ kho và ghi transaction
     * - Tạo order và invoice
     * - Lưu thông tin khuyến mãi
     * 
     * @param request thông tin bán hàng
     * @return thông tin hóa đơn đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSaleResponseDTO>> createSale(
            @Valid @RequestBody CreateSaleRequestDTO request) {
        
        log.info("Nhận yêu cầu bán hàng từ nhân viên ID: {}", request.employeeId());

        CreateSaleResponseDTO response = saleService.createSale(request);
        
        log.info("Tạo bán hàng thành công. Invoice: {}", response.invoiceNumber());
        
        return ResponseEntity.ok(ApiResponse.success("Tạo bán hàng thành công", response));
    }
}
