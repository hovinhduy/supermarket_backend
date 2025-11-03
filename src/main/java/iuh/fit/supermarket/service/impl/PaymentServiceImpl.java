package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.config.PayOSConfig;
import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.entity.SaleInvoiceHeader;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.PaymentStatus;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.OrderRepository;
import iuh.fit.supermarket.repository.SaleInvoiceHeaderRepository;
import iuh.fit.supermarket.service.PaymentService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation của PaymentService với PayOS SDK
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PayOS payOS;
    private final PayOSConfig payOSConfig;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final OrderRepository orderRepository;
    private final WarehouseService warehouseService;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(Long orderCode, BigDecimal amount, String description,
            List<PaymentItemData> items) {
        try {
            return attemptCreatePaymentLink(orderCode, amount, description, items);

        } catch (PayOSException e) {
            // Nếu lỗi "Đơn thanh toán đã tồn tại", tạo orderCode mới với timestamp suffix
            if (e.getMessage() != null && e.getMessage().contains("đã tồn tại")) {
                log.warn("Payment link với orderCode {} đã tồn tại, tạo orderCode mới với timestamp", orderCode);
                
                Long newOrderCode = generateUniqueOrderCode(orderCode);
                log.info("Retry tạo payment link với orderCode mới: {} (gốc: {})", newOrderCode, orderCode);
                
                try {
                    return attemptCreatePaymentLink(newOrderCode, amount, description, items);
                } catch (Exception retryEx) {
                    log.error("Lỗi khi retry tạo payment link với orderCode {}: {}", newOrderCode, retryEx.getMessage());
                    throw new InvalidSaleDataException("Không thể tạo link thanh toán sau khi retry: " + retryEx.getMessage());
                }
            }
            
            log.error("Lỗi PayOS khi tạo payment link cho order code {}: {}", orderCode, e.getMessage(), e);
            throw new InvalidSaleDataException("Không thể tạo link thanh toán: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo payment link cho order code {}: {}", orderCode, e.getMessage(), e);
            throw new InvalidSaleDataException("Không thể tạo link thanh toán: " + e.getMessage());
        }
    }

    /**
     * Thực hiện tạo payment link với orderCode cho trước
     */
    private CreatePaymentLinkResponse attemptCreatePaymentLink(Long orderCode, BigDecimal amount, 
            String description, List<PaymentItemData> items) throws PayOSException {
        
        CreatePaymentLinkRequest.CreatePaymentLinkRequestBuilder builder = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount.longValue())
                .description(description)
                .returnUrl(payOSConfig.getReturnUrl())
                .cancelUrl(payOSConfig.getCancelUrl());

        for (PaymentItemData item : items) {
            builder.item(PaymentLinkItem.builder()
                    .name(item.name())
                    .quantity(item.quantity())
                    .price((long) item.price())
                    .build());
        }

        CreatePaymentLinkRequest request = builder.build();
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);
        
        log.info("Đã tạo payment link cho order code: {}, URL: {}", orderCode, response.getCheckoutUrl());
        return response;
    }

    /**
     * Tạo orderCode unique bằng cách thêm timestamp suffix
     */
    private Long generateUniqueOrderCode(Long originalOrderCode) {
        // Thêm 6 chữ số cuối của timestamp để tránh conflict
        long timestamp = System.currentTimeMillis() % 1000000;
        return originalOrderCode * 1000000 + timestamp;
    }

    @Override
    @Transactional
    public void handlePaymentWebhook(Long orderCode, String transactionId) {
        log.info("Xử lý webhook thanh toán cho orderCode: {}, transactionId: {}", orderCode, transactionId);
        
        // Bước 0: Extract ID gốc nếu orderCode có timestamp suffix (>= 1000000)
        Long originalOrderCode = extractOriginalOrderCode(orderCode);
        if (!originalOrderCode.equals(orderCode)) {
            log.info("OrderCode {} được extract từ orderCode có timestamp suffix: {}", originalOrderCode, orderCode);
        }
        
        // Bước 1: Kiểm tra xem orderCode có phải là Order không (thử cả original và modified)
        boolean isOrder = orderRepository.existsById(orderCode) || 
                          orderRepository.existsById(originalOrderCode);
        
        if (isOrder) {
            // Xử lý như Order - ưu tiên dùng originalOrderCode
            Long actualOrderId = orderRepository.existsById(originalOrderCode) ? originalOrderCode : orderCode;
            log.info("OrderCode {} là Order (ID thực tế: {}), xử lý thanh toán đơn hàng", orderCode, actualOrderId);
            handleOrderPayment(actualOrderId, transactionId);
            return;
        }
        
        // Bước 2: Kiểm tra xem orderCode có phải là Invoice không (thử cả original và modified)
        boolean isInvoice = saleInvoiceHeaderRepository.existsById(orderCode.intValue()) || 
                            saleInvoiceHeaderRepository.existsById(originalOrderCode.intValue());
        
        if (isInvoice) {
            // Xử lý như Invoice - ưu tiên dùng originalOrderCode
            Long actualInvoiceId = saleInvoiceHeaderRepository.existsById(originalOrderCode.intValue()) ? 
                                   originalOrderCode : orderCode;
            log.info("OrderCode {} là Invoice (ID thực tế: {}), xử lý thanh toán hóa đơn", orderCode, actualInvoiceId);
            handleInvoicePayment(actualInvoiceId);
            return;
        }
        
        // Nếu không tìm thấy cả Order và Invoice
        log.error("Không tìm thấy Order hoặc Invoice với orderCode: {} (original: {})", orderCode, originalOrderCode);
        throw new NotFoundException("Không tìm thấy đơn hàng hoặc hóa đơn với mã: " + orderCode);
    }

    /**
     * Extract ID gốc từ orderCode có timestamp suffix
     * Logic:
     * - Nếu orderCode tồn tại trực tiếp trong DB => không có timestamp, trả về orderCode
     * - Nếu không, thử extract: orderCode / 1000000 (chỉ khi orderCode > 1000000)
     */
    private Long extractOriginalOrderCode(Long orderCode) {
        // Kiểm tra xem orderCode có tồn tại trực tiếp không
        boolean existsAsOrder = orderRepository.existsById(orderCode);
        boolean existsAsInvoice = saleInvoiceHeaderRepository.existsById(orderCode.intValue());
        
        if (existsAsOrder || existsAsInvoice) {
            // OrderCode tồn tại trực tiếp => không có timestamp suffix
            return orderCode;
        }
        
        // Nếu không tồn tại và orderCode > 1000000, thử extract ID gốc
        if (orderCode > 1000000) {
            return orderCode / 1000000;
        }
        
        // OrderCode nhỏ và không tồn tại => trả về chính nó (sẽ fail ở bước sau)
        return orderCode;
    }

    /**
     * Xử lý thanh toán cho Order
     */
    private void handleOrderPayment(Long orderId, String transactionId) {
        log.info("Xử lý thanh toán cho đơn hàng Order ID: {}, Transaction ID: {}", orderId, transactionId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra đã thanh toán chưa
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Đơn hàng {} đã được thanh toán rồi", orderId);
            return;
        }

        // Cập nhật trạng thái thanh toán
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setTransactionId(transactionId);
        order.setAmountPaid(order.getTotalAmount());
        
        // Thêm thông tin giao dịch vào note
        String currentNote = order.getNote() != null ? order.getNote() : "";
        order.setNote(currentNote.isEmpty() ? 
            "Transaction ID: " + transactionId : 
            currentNote + " | Transaction ID: " + transactionId);

        orderRepository.save(order);
        
        log.info("Đã xử lý thành công thanh toán cho đơn hàng {}, transactionId: {}", orderId, transactionId);
    }

    /**
     * Xử lý thanh toán cho Invoice
     */
    private void handleInvoicePayment(Long invoiceId) {
        log.info("Xử lý thanh toán cho Invoice ID: {}", invoiceId);
        
        // invoiceId được dùng làm orderCode trong payment link
        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findById(invoiceId.intValue())
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy invoice với ID: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            log.warn("Invoice {} đã ở trạng thái PAID, bỏ qua cập nhật", invoice.getInvoiceNumber());
            return;
        }

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            log.warn("Invoice {} không ở trạng thái UNPAID (hiện tại: {}), bỏ qua cập nhật",
                    invoice.getInvoiceNumber(), invoice.getStatus());
            return;
        }

        // Cập nhật trạng thái invoice sang PAID
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(invoice.getTotalAmount());
        saleInvoiceHeaderRepository.save(invoice);
        log.info("Đã cập nhật invoice {} sang PAID", invoice.getInvoiceNumber());

        // Trừ kho cho các sản phẩm trong invoice
        invoice.getInvoiceDetails().forEach(detail -> {
            try {
                warehouseService.stockOut(
                        detail.getProductUnit().getId(),
                        detail.getQuantity(),
                        invoice.getInvoiceNumber(),
                        "Thanh toán chuyển khoản thành công - Invoice: " + invoice.getInvoiceNumber());
            } catch (Exception e) {
                log.error("Lỗi khi trừ kho cho product unit {}: {}",
                        detail.getProductUnit().getId(), e.getMessage());
                throw new InvalidSaleDataException("Không thể trừ kho cho sản phẩm: " + e.getMessage());
            }
        });
        log.info("Đã trừ kho cho invoice {}", invoice.getInvoiceNumber());
    }
}
