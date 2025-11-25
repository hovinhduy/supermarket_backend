package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.config.PayOSConfig;
import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.OrderRepository;
import iuh.fit.supermarket.repository.SaleInvoiceHeaderRepository;
import iuh.fit.supermarket.service.PaymentService;
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
    
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private iuh.fit.supermarket.service.SaleService saleService;

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
        
        // Xác định loại đơn hàng dựa trên prefix
        if (orderCode >= 2000000000L) {
            // Invoice
            Long invoiceId = orderCode - 2000000000L;
            log.info("OrderCode {} là Invoice (ID thực tế: {}), xử lý thanh toán hóa đơn", orderCode, invoiceId);
            handleInvoicePayment(invoiceId);
            return;
        } else if (orderCode >= 1000000000L) {
            // Order
            Long orderId = orderCode - 1000000000L;
            log.info("OrderCode {} là Order (ID thực tế: {}), xử lý thanh toán đơn hàng", orderCode, orderId);
            handleOrderPayment(orderId, transactionId);
            return;
        }
        
        // Fallback cho code cũ (không có prefix)
        log.warn("OrderCode {} không có prefix chuẩn, thử tìm trong cả 2 bảng", orderCode);
        
        // Kiểm tra xem orderCode có phải là Invoice không
        if (saleInvoiceHeaderRepository.existsById(orderCode.intValue())) {
             log.info("OrderCode {} tìm thấy trong Invoice, xử lý thanh toán hóa đơn", orderCode);
             handleInvoicePayment(orderCode);
             return;
        }
        
        // Kiểm tra xem orderCode có phải là Order không
        if (orderRepository.existsById(orderCode)) {
            log.info("OrderCode {} tìm thấy trong Order, xử lý thanh toán đơn hàng", orderCode);
            handleOrderPayment(orderCode, transactionId);
            return;
        }
        
        // Nếu không tìm thấy cả Order và Invoice
        log.error("Không tìm thấy Order hoặc Invoice với orderCode: {}", orderCode);
        throw new NotFoundException("Không tìm thấy đơn hàng hoặc hóa đơn với mã: " + orderCode);
    }



    /**
     * Xử lý thanh toán cho Order
     * Chuyển trạng thái từ UNPAID sang PENDING khi thanh toán thành công
     */
    private void handleOrderPayment(Long orderId, String transactionId) {
        log.info("Xử lý thanh toán cho đơn hàng Order ID: {}, Transaction ID: {}", orderId, transactionId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Kiểm tra đã thanh toán chưa (trạng thái không phải UNPAID)
        if (order.getStatus() != OrderStatus.UNPAID) {
            log.warn("Đơn hàng {} đã được xử lý (trạng thái hiện tại: {})", orderId, order.getStatus());
            return;
        }

        // Cập nhật trạng thái từ UNPAID sang PENDING khi thanh toán thành công
        order.setStatus(OrderStatus.PENDING);
        order.setTransactionId(transactionId);
        order.setAmountPaid(order.getTotalAmount());

        // Thêm thông tin giao dịch vào note
        String currentNote = order.getNote() != null ? order.getNote() : "";
        order.setNote(currentNote.isEmpty() ?
            "Transaction ID: " + transactionId :
            currentNote + " | Transaction ID: " + transactionId);

        orderRepository.save(order);

        log.info("Đã xử lý thành công thanh toán cho đơn hàng {}, chuyển sang trạng thái PENDING, transactionId: {}",
                orderId, transactionId);
    }

    /**
     * Xử lý thanh toán cho Invoice
     */
    private void handleInvoicePayment(Long invoiceId) {
        log.info("Xử lý thanh toán cho Invoice ID: {}", invoiceId);
        
        // Delegate to SaleService to handle business logic (update status, stock, promotions)
        saleService.confirmInvoicePayment(invoiceId.intValue());
    }
}
