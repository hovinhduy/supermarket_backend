package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.config.PayOSConfig;
import iuh.fit.supermarket.entity.SaleInvoiceHeader;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
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
    private final WarehouseService warehouseService;

    @Override
    public CreatePaymentLinkResponse createPaymentLink(Long orderCode, BigDecimal amount, String description,
            List<PaymentItemData> items) {
        try {
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

        } catch (PayOSException e) {
            log.error("Lỗi PayOS khi tạo payment link cho order code {}: {}", orderCode, e.getMessage(), e);
            throw new InvalidSaleDataException("Không thể tạo link thanh toán: " + e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi tạo payment link cho order code {}: {}", orderCode, e.getMessage(), e);
            throw new InvalidSaleDataException("Không thể tạo link thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(Long invoiceId) {
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
