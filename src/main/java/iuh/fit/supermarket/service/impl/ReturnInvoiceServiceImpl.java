package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.return_invoice.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.ReturnStatus;
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
 * Implementation của ReturnInvoiceService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnInvoiceServiceImpl implements ReturnInvoiceService {

    private final SaleInvoiceHeaderRepository invoiceHeaderRepository;
    private final SaleInvoiceDetailRepository invoiceDetailRepository;
    private final ReturnInvoiceHeaderRepository returnInvoiceHeaderRepository;
    private final ReturnInvoiceDetailRepository returnInvoiceDetailRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final AppliedOrderPromotionRepository appliedOrderPromotionRepository;
    private final WarehouseService warehouseService;

    @Override
    @Transactional(readOnly = true)
    public RefundCalculationResponse calculateRefund(Integer invoiceId, List<RefundLineItemRequest> refundLineItems) {
        log.info("Bắt đầu tính toán preview trả hàng cho invoice ID: {}", invoiceId);

        SaleInvoiceHeader invoice = validateInvoice(invoiceId);
        Map<Integer, Integer> alreadyReturnedQuantities = getAlreadyReturnedQuantities(invoiceId);

        List<RefundLineItemResponse> lineItemResponses = new ArrayList<>();
        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (RefundLineItemRequest refundRequest : refundLineItems) {
            SaleInvoiceDetail invoiceDetail = invoiceDetailRepository.findById(refundRequest.lineItemId())
                    .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy chi tiết hóa đơn với ID: " + refundRequest.lineItemId()));

            if (!invoiceDetail.getInvoice().getInvoiceId().equals(invoiceId)) {
                throw new InvalidSaleDataException("Dòng sản phẩm không thuộc hóa đơn này");
            }

            int alreadyReturned = alreadyReturnedQuantities.getOrDefault(refundRequest.lineItemId(), 0);
            int maxRefundable = invoiceDetail.getQuantity() - alreadyReturned;

            if (refundRequest.quantity() > maxRefundable) {
                throw new InvalidSaleDataException(
                        "Số lượng trả vượt quá số lượng có thể trả. Tối đa: " + maxRefundable);
            }

            List<AppliedPromotion> appliedPromotions = appliedPromotionRepository
                    .findByInvoiceDetail_InvoiceDetailId(refundRequest.lineItemId());

            BigDecimal refundForLine = calculateLineItemRefund(invoiceDetail, refundRequest.quantity(),
                    appliedPromotions);

            RefundLineItemResponse lineResponse = new RefundLineItemResponse(
                    refundRequest.lineItemId(),
                    refundRequest.quantity(),
                    invoiceDetail.getUnitPrice(),
                    invoiceDetail.getUnitPrice().multiply(BigDecimal.valueOf(refundRequest.quantity())),
                    invoiceDetail.getUnitPrice(),
                    invoiceDetail.getUnitPrice().subtract(
                            invoiceDetail.getDiscountAmount().divide(BigDecimal.valueOf(invoiceDetail.getQuantity()),
                                    2, java.math.RoundingMode.HALF_UP)),
                    refundForLine,
                    maxRefundable,
                    BigDecimal.ZERO);

            lineItemResponses.add(lineResponse);
            totalRefundAmount = totalRefundAmount.add(refundForLine);
        }

        BigDecimal reclaimedDiscount = calculateReclaimedOrderDiscount(invoice, refundLineItems);
        BigDecimal finalRefundAmount = totalRefundAmount.subtract(reclaimedDiscount);

        TransactionInfo transactionInfo = new TransactionInfo(
                invoiceId,
                finalRefundAmount,
                finalRefundAmount);

        log.info("Tính toán preview hoàn tất. Tổng hoàn: {}, Thu hồi KM: {}, Thực hoàn: {}",
                totalRefundAmount, reclaimedDiscount, finalRefundAmount);

        return new RefundCalculationResponse(finalRefundAmount, lineItemResponses, transactionInfo);
    }

    @Override
    @Transactional
    public CreateRefundResponse createRefund(CreateRefundRequest request) {
        log.info("Bắt đầu tạo phiếu trả hàng cho invoice ID: {}", request.invoiceId());

        SaleInvoiceHeader invoice = validateInvoice(request.invoiceId());
        Map<Integer, Integer> alreadyReturnedQuantities = getAlreadyReturnedQuantities(request.invoiceId());

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<ReturnInvoiceDetail> returnDetails = new ArrayList<>();

        for (RefundLineItemRequest refundRequest : request.refundLineItems()) {
            SaleInvoiceDetail invoiceDetail = invoiceDetailRepository.findById(refundRequest.lineItemId())
                    .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy chi tiết hóa đơn với ID: " + refundRequest.lineItemId()));

            int alreadyReturned = alreadyReturnedQuantities.getOrDefault(refundRequest.lineItemId(), 0);
            int maxRefundable = invoiceDetail.getQuantity() - alreadyReturned;

            if (refundRequest.quantity() > maxRefundable) {
                throw new InvalidSaleDataException(
                        "Số lượng trả vượt quá số lượng có thể trả. Tối đa: " + maxRefundable);
            }

            List<AppliedPromotion> appliedPromotions = appliedPromotionRepository
                    .findByInvoiceDetail_InvoiceDetailId(refundRequest.lineItemId());

            BigDecimal refundForLine = calculateLineItemRefund(invoiceDetail, refundRequest.quantity(),
                    appliedPromotions);
            totalRefundAmount = totalRefundAmount.add(refundForLine);

            ReturnInvoiceDetail returnDetail = new ReturnInvoiceDetail();
            returnDetail.setQuantity(refundRequest.quantity());
            returnDetail.setPriceAtReturn(invoiceDetail.getUnitPrice());
            returnDetail.setRefundAmount(refundForLine);
            returnDetail.setProductUnit(invoiceDetail.getProductUnit());
            returnDetails.add(returnDetail);
        }

        BigDecimal reclaimedDiscount = calculateReclaimedOrderDiscount(invoice, request.refundLineItems());
        BigDecimal finalRefundAmount = totalRefundAmount.subtract(reclaimedDiscount);

        String returnCode = generateReturnCode();
        ReturnInvoiceHeader returnHeader = new ReturnInvoiceHeader();
        returnHeader.setReturnCode(returnCode);
        returnHeader.setReturnDate(LocalDateTime.now());
        returnHeader.setTotalRefundAmount(totalRefundAmount);
        returnHeader.setReclaimedDiscountAmount(reclaimedDiscount);
        returnHeader.setFinalRefundAmount(finalRefundAmount);
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
                    "Trả hàng - Phiếu: " + returnCode);
            log.info("Đã cộng kho {} sản phẩm {} - Mã: {}", detail.getQuantity(),
                    detail.getProductUnit().getProduct().getName(), returnCode);
        }

        log.info("Hoàn tất tạo phiếu trả. Tổng hoàn: {}, Thu hồi KM: {}, Thực hoàn: {}",
                totalRefundAmount, reclaimedDiscount, finalRefundAmount);

        return new CreateRefundResponse(
                returnHeader.getReturnId(),
                returnCode,
                finalRefundAmount,
                reclaimedDiscount);
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
                    returnHeader.getCustomer().getName(),
                    returnHeader.getCustomer().getPhone(),
                    returnHeader.getCustomer().getEmail());
        }

        ReturnInvoiceDetailResponse.EmployeeInfo employeeInfo = new ReturnInvoiceDetailResponse.EmployeeInfo(
                returnHeader.getEmployee().getEmployeeId(),
                returnHeader.getEmployee().getName(),
                returnHeader.getEmployee().getEmail());

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
            String returnCode,
            String invoiceNumber,
            String customerName,
            String customerPhone,
            LocalDate fromDate,
            LocalDate toDate,
            Integer employeeId,
            Integer customerId,
            Pageable pageable) {
        log.info("Tìm kiếm phiếu trả: returnCode={}, invoiceNumber={}, customerName={}, customerPhone={}",
                returnCode, invoiceNumber, customerName, customerPhone);

        Page<ReturnInvoiceHeader> returnHeaders = returnInvoiceHeaderRepository.searchAndFilterReturns(
                returnCode,
                invoiceNumber,
                customerName,
                customerPhone,
                fromDate,
                toDate,
                employeeId,
                customerId,
                pageable);

        return returnHeaders.map(header -> new ReturnInvoiceListResponse(
                header.getReturnId(),
                header.getReturnCode(),
                header.getReturnDate(),
                header.getOriginalInvoice().getInvoiceNumber(),
                header.getCustomer() != null ? header.getCustomer().getName() : null,
                header.getCustomer() != null ? header.getCustomer().getPhone() : null,
                header.getEmployee().getName(),
                header.getTotalRefundAmount(),
                header.getReclaimedDiscountAmount(),
                header.getFinalRefundAmount(),
                header.getReasonNote()));
    }

    private SaleInvoiceHeader validateInvoice(Integer invoiceId) {
        SaleInvoiceHeader invoice = invoiceHeaderRepository.findById(invoiceId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new InvalidSaleDataException("Hóa đơn phải ở trạng thái PAID mới có thể trả hàng");
        }

        return invoice;
    }

    private Map<Integer, Integer> getAlreadyReturnedQuantities(Integer invoiceId) {
        List<ReturnInvoiceHeader> existingReturns = returnInvoiceHeaderRepository
                .findByOriginalInvoice_InvoiceId(invoiceId);

        Map<Integer, Integer> returnedQuantities = new HashMap<>();
        for (ReturnInvoiceHeader returnHeader : existingReturns) {
            List<ReturnInvoiceDetail> returnDetails = returnInvoiceDetailRepository
                    .findByReturnInvoice_ReturnId(returnHeader.getReturnId());

            for (ReturnInvoiceDetail returnDetail : returnDetails) {
                List<SaleInvoiceDetail> invoiceDetails = invoiceDetailRepository
                        .findByInvoice_InvoiceId(invoiceId);
                for (SaleInvoiceDetail invoiceDetail : invoiceDetails) {
                    if (invoiceDetail.getProductUnit().getId().equals(returnDetail.getProductUnit().getId())) {
                        returnedQuantities.merge(invoiceDetail.getInvoiceDetailId(),
                                returnDetail.getQuantity(), Integer::sum);
                    }
                }
            }
        }

        return returnedQuantities;
    }

    private BigDecimal calculateLineItemRefund(SaleInvoiceDetail invoiceDetail, Integer quantity,
            List<AppliedPromotion> appliedPromotions) {
        BigDecimal pricePerItem = invoiceDetail.getUnitPrice();

        if (!appliedPromotions.isEmpty()) {
            for (AppliedPromotion promotion : appliedPromotions) {
                if ("FREE".equalsIgnoreCase(promotion.getDiscountType())) {
                    return BigDecimal.ZERO;
                }

                if (promotion.getDiscountValue() != null) {
                    BigDecimal discountPerItem = promotion.getDiscountValue()
                            .divide(BigDecimal.valueOf(invoiceDetail.getQuantity()), 2,
                                    java.math.RoundingMode.HALF_UP);
                    pricePerItem = pricePerItem.subtract(discountPerItem);
                }
            }
        } else {
            BigDecimal discountPerItem = invoiceDetail.getDiscountAmount()
                    .divide(BigDecimal.valueOf(invoiceDetail.getQuantity()), 2, java.math.RoundingMode.HALF_UP);
            pricePerItem = pricePerItem.subtract(discountPerItem);
        }

        return pricePerItem.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal calculateReclaimedOrderDiscount(SaleInvoiceHeader invoice,
            List<RefundLineItemRequest> refundLineItems) {
        List<AppliedOrderPromotion> orderPromotions = appliedOrderPromotionRepository
                .findByInvoice_InvoiceId(invoice.getInvoiceId());

        if (orderPromotions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal originalTotal = invoice.getSubtotal();
        BigDecimal returningTotal = BigDecimal.ZERO;

        for (RefundLineItemRequest refundRequest : refundLineItems) {
            SaleInvoiceDetail detail = invoiceDetailRepository.findById(refundRequest.lineItemId())
                    .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy chi tiết hóa đơn: " + refundRequest.lineItemId()));
            returningTotal = returningTotal
                    .add(detail.getUnitPrice().multiply(BigDecimal.valueOf(refundRequest.quantity())));
        }

        BigDecimal remainingTotal = originalTotal.subtract(returningTotal);

        BigDecimal totalOrderDiscount = BigDecimal.ZERO;
        for (AppliedOrderPromotion orderPromotion : orderPromotions) {
            if (orderPromotion.getDiscountValue() != null) {
                totalOrderDiscount = totalOrderDiscount.add(orderPromotion.getDiscountValue());
            }
        }

        BigDecimal minOrderValue = invoice.getTotalAmount().add(invoice.getTotalDiscount());
        if (remainingTotal.compareTo(minOrderValue) < 0 && totalOrderDiscount.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Thu hồi khuyến mãi order: {} vì giá trị còn lại {} < điều kiện tối thiểu {}",
                    totalOrderDiscount, remainingTotal, minOrderValue);
            return totalOrderDiscount;
        }

        return BigDecimal.ZERO;
    }

    private String generateReturnCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RET" + timestamp + random;
    }
}
