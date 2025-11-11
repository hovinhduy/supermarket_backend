package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.return_invoice.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.ReturnInvoiceService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation của ReturnInvoiceService - xử lý trả hàng toàn bộ hóa đơn
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnInvoiceServiceImpl implements ReturnInvoiceService {

    private final SaleInvoiceHeaderRepository invoiceHeaderRepository;
    private final SaleInvoiceDetailRepository invoiceDetailRepository;
    private final ReturnInvoiceHeaderRepository returnInvoiceHeaderRepository;
    private final ReturnInvoiceDetailRepository returnInvoiceDetailRepository;
    private final WarehouseService warehouseService;

    @Override
    @Transactional(readOnly = true)
    public RefundCalculationResponse calculateRefund(Integer invoiceId) {
        log.info("Bắt đầu tính toán preview trả toàn bộ hóa đơn ID: {}", invoiceId);

        SaleInvoiceHeader invoice = validateInvoice(invoiceId);
        List<SaleInvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoice_InvoiceId(invoiceId);

        if (invoiceDetails.isEmpty()) {
            throw new InvalidSaleDataException("Hóa đơn không có sản phẩm nào để trả");
        }

        // Số tiền hoàn = số tiền khách đã thanh toán
        BigDecimal refundAmount = getRefundAmount(invoice);

        List<RefundLineItemResponse> lineItemResponses = new ArrayList<>();

        for (SaleInvoiceDetail invoiceDetail : invoiceDetails) {
            RefundLineItemResponse lineResponse = new RefundLineItemResponse(
                    invoiceDetail.getInvoiceDetailId(),
                    invoiceDetail.getQuantity(),
                    invoiceDetail.getUnitPrice(),
                    invoiceDetail.getLineTotal(),  // Tổng tiền trước thuế
                    invoiceDetail.getUnitPrice(),
                    invoiceDetail.getUnitPrice(),
                    invoiceDetail.getLineTotal(),  // Số tiền hoàn = thành tiền (lineTotal)
                    invoiceDetail.getQuantity(),
                    BigDecimal.ZERO);

            lineItemResponses.add(lineResponse);
        }

        TransactionInfo transactionInfo = new TransactionInfo(
                invoiceId,
                refundAmount,
                refundAmount);

        log.info("Tính toán preview hoàn tất. Số tiền hoàn: {}", refundAmount);

        return new RefundCalculationResponse(refundAmount, lineItemResponses, transactionInfo);
    }

    @Override
    @Transactional
    public CreateRefundResponse createRefund(CreateRefundRequest request) {
        log.info("Bắt đầu tạo phiếu trả toàn bộ hóa đơn ID: {}", request.invoiceId());

        SaleInvoiceHeader invoice = validateInvoice(request.invoiceId());
        List<SaleInvoiceDetail> invoiceDetails = invoiceDetailRepository.findByInvoice_InvoiceId(request.invoiceId());

        if (invoiceDetails.isEmpty()) {
            throw new InvalidSaleDataException("Hóa đơn không có sản phẩm nào để trả");
        }

        // Số tiền hoàn = số tiền khách đã thanh toán
        BigDecimal refundAmount = getRefundAmount(invoice);

        List<ReturnInvoiceDetail> returnDetails = new ArrayList<>();

        for (SaleInvoiceDetail invoiceDetail : invoiceDetails) {
            ReturnInvoiceDetail returnDetail = new ReturnInvoiceDetail();
            returnDetail.setQuantity(invoiceDetail.getQuantity());
            returnDetail.setPriceAtReturn(invoiceDetail.getUnitPrice());
            // Lấy giá từ lineTotal (thành tiền trước thuế)
            returnDetail.setRefundAmount(invoiceDetail.getLineTotal());
            returnDetail.setProductUnit(invoiceDetail.getProductUnit());
            returnDetails.add(returnDetail);
        }

        String returnCode = generateReturnCode();
        ReturnInvoiceHeader returnHeader = new ReturnInvoiceHeader();
        returnHeader.setReturnCode(returnCode);
        returnHeader.setReturnDate(LocalDateTime.now());
        returnHeader.setTotalRefundAmount(refundAmount);
        returnHeader.setReclaimedDiscountAmount(BigDecimal.ZERO);
        returnHeader.setFinalRefundAmount(refundAmount);
        returnHeader.setReasonNote(request.reasonNote());
        returnHeader.setOriginalInvoice(invoice);
        returnHeader.setCustomer(invoice.getCustomer());
        returnHeader.setEmployee(invoice.getEmployee());

        returnHeader = returnInvoiceHeaderRepository.save(returnHeader);
        log.info("Đã tạo phiếu trả với mã: {}", returnCode);

        for (ReturnInvoiceDetail detail : returnDetails) {
            detail.setReturnInvoice(returnHeader);
            returnInvoiceDetailRepository.save(detail);

            warehouseService.stockIn(
                    detail.getProductUnit().getId(),
                    detail.getQuantity(),
                    returnCode,
                    "Trả hàng toàn bộ - Phiếu: " + returnCode);
            log.info("Đã cộng kho {} sản phẩm {} - Mã: {}", detail.getQuantity(),
                    detail.getProductUnit().getProduct().getName(), returnCode);
        }

        // Cập nhật trạng thái hóa đơn sang RETURNED
        invoice.setStatus(InvoiceStatus.RETURNED);
        invoiceHeaderRepository.save(invoice);
        log.info("Đã cập nhật trạng thái hóa đơn {} sang RETURNED", invoice.getInvoiceNumber());

        log.info("Hoàn tất tạo phiếu trả. Số tiền hoàn: {}", refundAmount);

        return new CreateRefundResponse(
                returnHeader.getReturnId(),
                returnCode,
                refundAmount,
                BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public CreateRefundResponse getReturnInvoice(Integer returnId) {
        ReturnInvoiceHeader returnHeader = returnInvoiceHeaderRepository.findById(returnId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy phiếu trả với ID: " + returnId));

        return new CreateRefundResponse(
                returnHeader.getReturnId(),
                returnHeader.getReturnCode(),
                returnHeader.getFinalRefundAmount(),
                returnHeader.getReclaimedDiscountAmount());
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnInvoiceDetailResponse getReturnInvoiceDetail(Integer returnId) {
        log.info("Lấy chi tiết phiếu trả: {}", returnId);

        ReturnInvoiceHeader returnHeader = returnInvoiceHeaderRepository.findByIdWithDetails(returnId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy phiếu trả với ID: " + returnId));

        ReturnInvoiceDetailResponse.CustomerInfo customerInfo = null;
        if (returnHeader.getCustomer() != null) {
            customerInfo = new ReturnInvoiceDetailResponse.CustomerInfo(
                    returnHeader.getCustomer().getCustomerId(),
                    returnHeader.getCustomer().getUser().getName(),
                    returnHeader.getCustomer().getUser().getPhone(),
                    returnHeader.getCustomer().getUser().getEmail());
        }

        ReturnInvoiceDetailResponse.EmployeeInfo employeeInfo = new ReturnInvoiceDetailResponse.EmployeeInfo(
                returnHeader.getEmployee().getEmployeeId(),
                returnHeader.getEmployee().getUser().getName(),
                returnHeader.getEmployee().getUser().getEmail());

        List<ReturnInvoiceDetailResponse.ReturnItemDetail> returnItemDetails = returnHeader.getReturnDetails()
                .stream()
                .map(detail -> new ReturnInvoiceDetailResponse.ReturnItemDetail(
                        detail.getReturnDetailId(),
                        detail.getQuantity(),
                        detail.getProductUnit().getProduct().getName(),
                        detail.getProductUnit().getUnit().getName(),
                        detail.getPriceAtReturn(),
                        detail.getRefundAmount()))
                .collect(Collectors.toList());

        return new ReturnInvoiceDetailResponse(
                returnHeader.getReturnId(),
                returnHeader.getReturnCode(),
                returnHeader.getReturnDate(),
                returnHeader.getOriginalInvoice().getInvoiceNumber(),
                returnHeader.getOriginalInvoice().getInvoiceId(),
                customerInfo,
                employeeInfo,
                returnHeader.getTotalRefundAmount(),
                returnHeader.getReclaimedDiscountAmount(),
                returnHeader.getFinalRefundAmount(),
                returnHeader.getReasonNote(),
                returnItemDetails,
                returnHeader.getCreatedAt(),
                returnHeader.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReturnInvoiceListResponse> searchAndFilterReturns(
            String searchKeyword,
            LocalDate fromDate,
            LocalDate toDate,
            Integer employeeId,
            Integer customerId,
            Integer productUnitId,
            Pageable pageable) {
        log.info("Tìm kiếm phiếu trả: searchKeyword={}, fromDate={}, toDate={}, employeeId={}, customerId={}, productUnitId={}",
                searchKeyword, fromDate, toDate, employeeId, customerId, productUnitId);

        Page<ReturnInvoiceHeader> returnHeaders = returnInvoiceHeaderRepository.searchAndFilterReturns(
                searchKeyword,
                fromDate,
                toDate,
                employeeId,
                customerId,
                productUnitId,
                pageable);

        return returnHeaders.map(header -> new ReturnInvoiceListResponse(
                header.getReturnId(),
                header.getReturnCode(),
                header.getReturnDate(),
                header.getOriginalInvoice().getInvoiceNumber(),
                header.getCustomer() != null ? header.getCustomer().getUser().getName() : null,
                header.getCustomer() != null ? header.getCustomer().getUser().getPhone() : null,
                header.getEmployee().getUser().getName(),
                header.getTotalRefundAmount(),
                header.getReclaimedDiscountAmount(),
                header.getFinalRefundAmount(),
                header.getReasonNote()));
    }

    /**
     * Validate hóa đơn trước khi trả hàng
     * @param invoiceId ID hóa đơn
     * @return Hóa đơn hợp lệ
     */
    private SaleInvoiceHeader validateInvoice(Integer invoiceId) {
        SaleInvoiceHeader invoice = invoiceHeaderRepository.findById(invoiceId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.RETURNED) {
            throw new InvalidSaleDataException("Hóa đơn này đã được trả hàng rồi");
        }

        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new InvalidSaleDataException("Hóa đơn phải ở trạng thái PAID mới có thể trả hàng");
        }

        return invoice;
    }

    /**
     * Lấy số tiền hoàn lại = số tiền khách đã thanh toán
     * @param invoice Hóa đơn gốc
     * @return Số tiền hoàn lại
     */
    private BigDecimal getRefundAmount(SaleInvoiceHeader invoice) {
        // Ưu tiên lấy paidAmount, nếu không có thì lấy totalAmount
        BigDecimal refundAmount = invoice.getPaidAmount();
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            refundAmount = invoice.getTotalAmount();
        }
        return refundAmount;
    }

    /**
     * Tạo mã phiếu trả hàng duy nhất
     * @return Mã phiếu trả
     */
    private String generateReturnCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RET" + timestamp + random;
    }
}
