package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.sale.CreateSaleRequestDTO;
import iuh.fit.supermarket.dto.sale.CreateSaleResponseDTO;

/**
 * Service interface cho quản lý bán hàng
 */
public interface SaleService {

    /**
     * Tạo bán hàng mới
     * - Kiểm tra tồn kho
     * - Trừ kho và ghi transaction
     * - Tạo order
     * - Tạo invoice
     * - Lưu thông tin khuyến mãi đã áp dụng
     *
     * @param request thông tin bán hàng
     * @return thông tin hóa đơn đã tạo
     */
    CreateSaleResponseDTO createSale(CreateSaleRequestDTO request);
}
