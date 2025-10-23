package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.sale.*;

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

    /**
     * Tìm kiếm và lọc danh sách hoá đơn bán có đầy đủ thông tin khuyến mãi
     * 
     * @param searchKeyword từ khóa tìm kiếm (tìm trong mã hoá đơn và số điện thoại khách hàng)
     * @param fromDate từ ngày
     * @param toDate đến ngày
     * @param status trạng thái hoá đơn
     * @param employeeId ID nhân viên
     * @param customerId ID khách hàng
     * @param productUnitId ID sản phẩm đơn vị
     * @param pageNumber số trang (từ 0)
     * @param pageSize kích thước trang
     * @return danh sách hoá đơn với chi tiết khuyến mãi
     */
    SaleInvoicesListResponseDTO searchAndFilterSalesInvoices(
            String searchKeyword,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            Integer employeeId,
            Integer customerId,
            Integer productUnitId,
            int pageNumber,
            int pageSize
    );

    /**
     * Lấy thông tin chi tiết hoá đơn bán theo ID
     * 
     * @param invoiceId ID của hoá đơn
     * @return hoá đơn với đầy đủ thông tin khuyến mãi
     */
    SaleInvoiceFullDTO getInvoiceDetail(Integer invoiceId);

}
