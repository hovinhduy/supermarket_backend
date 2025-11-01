package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.sale.*;

/**
 * Service interface cho quản lý bán hàng
 */
public interface SaleService {

    /**
     * Tạo bán hàng mới
     * - Kiểm tra tồn kho
     * - Tạo invoice trực tiếp
     * - CASH: invoice PAID, trừ kho ngay
     * - ONLINE: invoice UNPAID, trừ kho khi webhook confirm
     * - Lưu thông tin khuyến mãi đã áp dụng
     *
     * @param request thông tin bán hàng
     * @return thông tin hóa đơn đã tạo
     */
    CreateSaleResponseDTO createSale(CreateSaleRequestDTO request);

    /**
     * Lấy trạng thái hóa đơn theo invoiceId
     * - Dùng để polling kiểm tra invoice đã PAID chưa
     * - Dùng cho thanh toán ONLINE
     *
     * @param invoiceId ID của hóa đơn (từ paymentOrderCode)
     * @return thông tin trạng thái hóa đơn
     */
    OrderStatusResponseDTO getInvoiceStatus(Long invoiceId);

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

    /**
     * Tạo file PDF cho hóa đơn bán hàng
     * 
     * @param invoiceId ID của hoá đơn
     * @return byte array của file PDF
     */
    byte[] generateInvoicePdf(Integer invoiceId);

    /**
     * Tạo HTML để in hóa đơn bán hàng
     * 
     * @param invoiceId ID của hoá đơn
     * @return HTML content có thể in trực tiếp
     */
    String generateInvoiceHtml(Integer invoiceId);

}
