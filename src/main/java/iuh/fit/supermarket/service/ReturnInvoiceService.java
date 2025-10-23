package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.return_invoice.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface cho quản lý trả hàng
 */
public interface ReturnInvoiceService {

    /**
     * Tính toán preview số tiền hoàn (không lưu DB)
     *
     * @param invoiceId ID hóa đơn gốc
     * @param refundLineItems Danh sách sản phẩm muốn trả
     * @return Thông tin tính toán
     */
    RefundCalculationResponse calculateRefund(Integer invoiceId, List<RefundLineItemRequest> refundLineItems);

    /**
     * Tạo phiếu trả hàng thực tế (lưu DB + cộng kho + hoàn tiền)
     *
     * @param request Request tạo phiếu trả
     * @return Thông tin phiếu trả đã tạo
     */
    CreateRefundResponse createRefund(CreateRefundRequest request);

    /**
     * Lấy thông tin chi tiết phiếu trả
     *
     * @param returnId ID phiếu trả
     * @return Thông tin phiếu trả
     */
    CreateRefundResponse getReturnInvoice(Integer returnId);

    /**
     * Lấy thông tin chi tiết đầy đủ của phiếu trả
     *
     * @param returnId ID phiếu trả
     * @return Thông tin chi tiết phiếu trả
     */
    ReturnInvoiceDetailResponse getReturnInvoiceDetail(Integer returnId);

    /**
     * Tìm kiếm và lọc danh sách hóa đơn trả hàng
     *
     * @param searchKeyword Từ khóa tìm kiếm (tìm trong mã trả hàng, mã hóa đơn gốc, tên khách hàng, số điện thoại)
     * @param fromDate Từ ngày
     * @param toDate Đến ngày
     * @param employeeId ID nhân viên
     * @param customerId ID khách hàng
     * @param productUnitId ID sản phẩm đơn vị
     * @param pageable Thông tin phân trang
     * @return Danh sách hóa đơn trả hàng
     */
    Page<ReturnInvoiceListResponse> searchAndFilterReturns(
            String searchKeyword,
            LocalDate fromDate,
            LocalDate toDate,
            Integer employeeId,
            Integer customerId,
            Integer productUnitId,
            Pageable pageable
    );

    /**
     * Kiểm tra số lượng còn lại có thể trả của mỗi hóa đơn bán
     *
     * @param invoiceId ID hóa đơn
     * @return Thông tin chi tiết số lượng có thể trả
     */
    AvailableReturnQuantityResponse getAvailableReturnQuantity(Integer invoiceId);
}
