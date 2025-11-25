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

    /**
     * Lấy danh sách hóa đơn của customer theo username
     * - Customer chỉ có thể xem hóa đơn của chính mình
     * - Có thể lọc theo trạng thái, khoảng ngày và phân trang
     * - Mặc định lấy hóa đơn đã thanh toán (PAID)
     * 
     * @param username username của customer (email hoặc phone)
     * @param fromDate từ ngày (null để không lọc)
     * @param toDate đến ngày (null để không lọc)
     * @param status trạng thái hóa đơn cần lọc (null thì mặc định PAID)
     * @param pageNumber số trang (từ 0)
     * @param pageSize kích thước trang
     * @return danh sách hóa đơn của customer với chi tiết khuyến mãi
     */
    SaleInvoicesListResponseDTO getCustomerInvoices(
            String username,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            int pageNumber,
            int pageSize
    );

    /**
     * Lấy chi tiết hóa đơn của customer theo username và invoiceId
     * - Customer chỉ có thể xem chi tiết hóa đơn của chính mình
     * - Kiểm tra quyền sở hữu hóa đơn
     * 
     * @param username username của customer (email hoặc phone)
     * @param invoiceId ID của hóa đơn
     * @return chi tiết hóa đơn với đầy đủ thông tin khuyến mãi
     */
    SaleInvoiceFullDTO getCustomerInvoiceDetail(String username, Integer invoiceId);

    /**
     * Xác nhận thanh toán cho hóa đơn
     * - Cập nhật trạng thái sang PAID
     * - Trừ kho
     * - Cập nhật số lượng sử dụng khuyến mãi
     * 
     * @param invoiceId ID của hóa đơn
     */
    void confirmInvoicePayment(Integer invoiceId);

}
