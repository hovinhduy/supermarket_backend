package iuh.fit.supermarket.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
    private final PromotionDetailRepository promotionDetailRepository;
    private final ObjectMapper objectMapper;

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

        // Cập nhật usage count cho các khuyến mãi đã sử dụng
        updatePromotionUsageCount(order);
        log.info("Đã cập nhật usage count cho các khuyến mãi của order {}", orderId);

        return invoiceNumber;
    }

    /**
     * Cập nhật số lượng sử dụng khuyến mãi khi tạo hóa đơn
     *
     * @param order Đơn hàng đã hoàn thành
     */
    private void updatePromotionUsageCount(Order order) {
        log.info("Cập nhật usage count cho các khuyến mãi của đơn hàng {}", order.getOrderId());

        Set<Long> processedDetailIds = new HashSet<>();

        // Cập nhật usageCount cho khuyến mãi từ OrderDetails (PRODUCT_DISCOUNT, BUY_X_GET_Y)
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            if (orderDetail.getPromotionDetailId() != null &&
                !processedDetailIds.contains(orderDetail.getPromotionDetailId())) {

                promotionDetailRepository.findById(orderDetail.getPromotionDetailId())
                    .ifPresent(detail -> {
                        Integer currentCount = detail.getUsageCount() != null ? detail.getUsageCount() : 0;
                        detail.setUsageCount(currentCount + 1);
                        promotionDetailRepository.save(detail);
                        log.debug("Đã cập nhật usageCount cho promotion detail ID: {} ({}->{})",
                            detail.getDetailId(), currentCount, currentCount + 1);
                    });

                processedDetailIds.add(orderDetail.getPromotionDetailId());
            }
        }

        // Cập nhật usageCount cho khuyến mãi đơn hàng (ORDER_DISCOUNT)
        if (order.getAppliedOrderPromotionsJson() != null && !order.getAppliedOrderPromotionsJson().isEmpty()) {
            try {
                List<CheckPromotionResponseDTO.OrderPromotionDTO> orderPromotions = objectMapper.readValue(
                    order.getAppliedOrderPromotionsJson(),
                    objectMapper.getTypeFactory().constructCollectionType(
                        List.class,
                        CheckPromotionResponseDTO.OrderPromotionDTO.class
                    )
                );

                for (CheckPromotionResponseDTO.OrderPromotionDTO orderPromotion : orderPromotions) {
                    if (orderPromotion.promotionDetailId() != null &&
                        !processedDetailIds.contains(orderPromotion.promotionDetailId())) {

                        promotionDetailRepository.findById(orderPromotion.promotionDetailId())
                            .ifPresent(detail -> {
                                Integer currentCount = detail.getUsageCount() != null ? detail.getUsageCount() : 0;
                                detail.setUsageCount(currentCount + 1);
                                promotionDetailRepository.save(detail);
                                log.debug("Đã cập nhật usageCount cho order promotion detail ID: {} ({}->{})",
                                    detail.getDetailId(), currentCount, currentCount + 1);
                            });

                        processedDetailIds.add(orderPromotion.promotionDetailId());
                    }
                }
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi parse appliedOrderPromotionsJson để cập nhật usageCount", e);
            }
        }

        log.info("Hoàn thành cập nhật usage count cho {} promotion details", processedDetailIds.size());
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
                    appliedPromotion.setPromotionName(promotion.promotionName());
                    appliedPromotion.setPromotionLineId(promotion.promotionLineId());
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
