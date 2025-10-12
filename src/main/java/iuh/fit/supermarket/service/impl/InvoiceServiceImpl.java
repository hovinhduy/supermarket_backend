package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.dto.sale.OrderPromotionRequestDTO;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.InvoiceService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implementation của InvoiceService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final OrderRepository orderRepository;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final SaleInvoiceDetailRepository saleInvoiceDetailRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final AppliedOrderPromotionRepository appliedOrderPromotionRepository;
    private final WarehouseService warehouseService;

    @Override
    @Transactional
    public String createInvoiceForCompletedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy order với ID: " + orderId));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidSaleDataException("Order phải ở trạng thái COMPLETED mới tạo được invoice");
        }

        // Kiểm tra đã có invoice chưa
        List<SaleInvoiceHeader> existingInvoices = saleInvoiceHeaderRepository.findByOrder_OrderId(orderId);
        if (!existingInvoices.isEmpty()) {
            log.warn("Order {} đã có invoice: {}", orderId, existingInvoices.get(0).getInvoiceNumber());
            return existingInvoices.get(0).getInvoiceNumber();
        }

        String invoiceNumber = generateInvoiceNumber();
        
        // Trừ kho
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            warehouseService.stockOut(
                    orderDetail.getProductUnit().getId(),
                    orderDetail.getQuantity(),
                    invoiceNumber,
                    "Bán hàng - Invoice: " + invoiceNumber
            );
        }
        log.info("Đã trừ kho cho {} sản phẩm", order.getOrderDetails().size());

        // Tạo Invoice Header
        SaleInvoiceHeader invoice = new SaleInvoiceHeader();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setSubtotal(order.getSubtotal());
        
        BigDecimal totalDiscount = order.getOrderDetails().stream()
                .map(OrderDetail::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        invoice.setTotalDiscount(totalDiscount);
        
        invoice.setTotalTax(BigDecimal.ZERO);
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(order.getTotalAmount());
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setEmployee(order.getEmployee());

        invoice = saleInvoiceHeaderRepository.save(invoice);
        log.info("Đã tạo invoice với số: {}", invoiceNumber);

        // Tạo Invoice Details
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            SaleInvoiceDetail invoiceDetail = new SaleInvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductUnit(orderDetail.getProductUnit());
            invoiceDetail.setQuantity(orderDetail.getQuantity());
            invoiceDetail.setUnitPrice(orderDetail.getPriceAtPurchase());
            invoiceDetail.setDiscountAmount(orderDetail.getDiscount());
            
            BigDecimal lineTotal = orderDetail.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                    .subtract(orderDetail.getDiscount());
            invoiceDetail.setLineTotal(lineTotal);
            invoiceDetail.setTaxAmount(BigDecimal.ZERO);
            invoiceDetail.setLineTotalWithTax(lineTotal);

            saleInvoiceDetailRepository.save(invoiceDetail);
        }

        return invoiceNumber;
    }

    @Override
    @Transactional
    public void saveAppliedPromotions(
            String invoiceNumber,
            List<OrderPromotionRequestDTO> orderPromotions,
            Map<Integer, PromotionAppliedDTO> itemPromotionsByIndex
    ) {
        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy invoice: " + invoiceNumber));

        // Lưu khuyến mãi order level
        if (orderPromotions != null && !orderPromotions.isEmpty()) {
            for (OrderPromotionRequestDTO orderPromotion : orderPromotions) {
                AppliedOrderPromotion appliedOrderPromotion = new AppliedOrderPromotion();
                appliedOrderPromotion.setPromotionId(orderPromotion.promotionId());
                appliedOrderPromotion.setPromotionName(orderPromotion.promotionName());
                appliedOrderPromotion.setPromotionDetailId(orderPromotion.promotionDetailId());
                appliedOrderPromotion.setPromotionSummary(orderPromotion.promotionSummary());
                appliedOrderPromotion.setDiscountType(orderPromotion.discountType());
                appliedOrderPromotion.setDiscountValue(orderPromotion.discountValue());
                appliedOrderPromotion.setInvoice(invoice);

                appliedOrderPromotionRepository.save(appliedOrderPromotion);
            }
            log.info("Đã lưu {} khuyến mãi order level cho invoice {}", orderPromotions.size(), invoiceNumber);
        }

        // Lưu khuyến mãi item level (match theo index)
        if (itemPromotionsByIndex != null && !itemPromotionsByIndex.isEmpty()) {
            List<SaleInvoiceDetail> invoiceDetails = saleInvoiceDetailRepository.findByInvoice_InvoiceId(invoice.getInvoiceId());
            
            for (int i = 0; i < invoiceDetails.size(); i++) {
                SaleInvoiceDetail invoiceDetail = invoiceDetails.get(i);
                PromotionAppliedDTO promotion = itemPromotionsByIndex.get(i);
                
                if (promotion != null) {
                    AppliedPromotion appliedPromotion = new AppliedPromotion();
                    appliedPromotion.setPromotionId(promotion.promotionId());
                    appliedPromotion.setPromotionName(promotion.promotionName());
                    appliedPromotion.setPromotionDetailId(promotion.promotionDetailId());
                    appliedPromotion.setPromotionSummary(promotion.promotionSummary());
                    appliedPromotion.setDiscountType(promotion.discountType());
                    appliedPromotion.setDiscountValue(promotion.discountValue());
                    appliedPromotion.setSourceLineItemId(promotion.sourceLineItemId());
                    appliedPromotion.setInvoiceDetail(invoiceDetail);

                    appliedPromotionRepository.save(appliedPromotion);
                }
            }
            log.info("Đã lưu {} khuyến mãi item level cho invoice {}", itemPromotionsByIndex.size(), invoiceNumber);
        }
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "INV" + timestamp + random;
    }
}
