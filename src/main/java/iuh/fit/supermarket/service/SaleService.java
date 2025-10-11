package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.sale.CreateSaleRequestDTO;
import iuh.fit.supermarket.dto.sale.CreateSaleResponseDTO;
import iuh.fit.supermarket.dto.sale.OrderStatusResponseDTO;

/**
 * Service interface cho quản lý bán hàng
 */
public interface SaleService {

    /**
     * Tạo bán hàng mới
     * - Kiểm tra tồn kho
     * - Trừ kho và ghi transaction (nếu không phải ONLINE)
     * - Tạo order
     * - Tạo invoice (chỉ khi order COMPLETED)
     * - Lưu thông tin khuyến mãi đã áp dụng
     *
     * @param request thông tin bán hàng
     * @return thông tin hóa đơn đã tạo
     */
    CreateSaleResponseDTO createSale(CreateSaleRequestDTO request);

    /**
     * Lấy trạng thái đơn hàng
     * 
     * @param orderId ID của đơn hàng
     * @return thông tin trạng thái đơn hàng
     */
    OrderStatusResponseDTO getOrderStatus(Long orderId);

}
