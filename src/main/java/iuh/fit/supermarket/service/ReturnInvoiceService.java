package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.return_invoice.CreateRefundRequest;
import iuh.fit.supermarket.dto.return_invoice.CreateRefundResponse;
import iuh.fit.supermarket.dto.return_invoice.RefundCalculationResponse;
import iuh.fit.supermarket.dto.return_invoice.RefundLineItemRequest;

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
}
