package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.dto.sale.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.exception.InsufficientStockException;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.SaleService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implementation của SaleService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleServiceImpl implements SaleService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductUnitRepository productUnitRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final SaleInvoiceDetailRepository saleInvoiceDetailRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final AppliedOrderPromotionRepository appliedOrderPromotionRepository;
    private final WarehouseService warehouseService;

    @Override
    @Transactional
    public CreateSaleResponseDTO createSale(CreateSaleRequestDTO request) {
        log.info("Bắt đầu tạo bán hàng cho nhân viên ID: {}", request.employeeId());

        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy nhân viên với ID: " + request.employeeId()));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy khách hàng với ID: " + request.customerId()));
        }

        validateAndCheckStock(request.items());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal lineItemDiscount = BigDecimal.ZERO;

        for (SaleItemRequestDTO item : request.items()) {
            BigDecimal itemSubtotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            subtotal = subtotal.add(itemSubtotal);
            
            BigDecimal itemDiscount = itemSubtotal.subtract(item.lineTotal());
            lineItemDiscount = lineItemDiscount.add(itemDiscount);
        }

        // Tính order discount từ appliedOrderPromotions
        BigDecimal orderDiscount = BigDecimal.ZERO;
        if (request.appliedOrderPromotions() != null && !request.appliedOrderPromotions().isEmpty()) {
            for (var orderPromotion : request.appliedOrderPromotions()) {
                orderDiscount = orderDiscount.add(orderPromotion.discountValue());
            }
        }

        BigDecimal totalDiscount = lineItemDiscount.add(orderDiscount);
        BigDecimal totalAmount = subtotal.subtract(totalDiscount);

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setSubtotal(subtotal);
        order.setTotalAmount(totalAmount);
        order.setAmountPaid(request.amountPaid());
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentMethod(request.paymentMethod());
        order.setNote(request.note());
        order.setEmployee(employee);
        order.setCustomer(customer);

        order = orderRepository.save(order);
        log.info("Đã tạo order với ID: {}", order.getOrderId());

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (SaleItemRequestDTO item : request.items()) {
            ProductUnit productUnit = productUnitRepository.findById(item.productUnitId())
                    .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy đơn vị sản phẩm với ID: " + item.productUnitId()));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProductUnit(productUnit);
            orderDetail.setQuantity(item.quantity());
            orderDetail.setPriceAtPurchase(item.unitPrice());
            
            BigDecimal itemSubtotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            BigDecimal itemDiscount = itemSubtotal.subtract(item.lineTotal());
            orderDetail.setDiscount(itemDiscount);

            orderDetails.add(orderDetailRepository.save(orderDetail));
        }
        order.setOrderDetails(orderDetails);

        String invoiceNumber = generateInvoiceNumber();
        
        for (SaleItemRequestDTO item : request.items()) {
            warehouseService.stockOut(
                    item.productUnitId(),
                    item.quantity(),
                    invoiceNumber,
                    "Bán hàng - Invoice: " + invoiceNumber
            );
        }
        log.info("Đã trừ kho và ghi transaction cho {} sản phẩm", request.items().size());

        SaleInvoiceHeader invoice = new SaleInvoiceHeader();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setSubtotal(subtotal);
        invoice.setTotalDiscount(totalDiscount);
        invoice.setTotalTax(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(request.amountPaid());
        invoice.setOrder(order);
        invoice.setCustomer(customer);
        invoice.setEmployee(employee);

        invoice = saleInvoiceHeaderRepository.save(invoice);
        log.info("Đã tạo invoice với số: {}", invoiceNumber);

        List<SaleItemResponseDTO> itemResponses = new ArrayList<>();
        
        for (int i = 0; i < request.items().size(); i++) {
            SaleItemRequestDTO itemRequest = request.items().get(i);
            ProductUnit productUnit = productUnitRepository.findById(itemRequest.productUnitId())
                    .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy đơn vị sản phẩm với ID: " + itemRequest.productUnitId()));

            SaleInvoiceDetail invoiceDetail = new SaleInvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductUnit(productUnit);
            invoiceDetail.setQuantity(itemRequest.quantity());
            invoiceDetail.setUnitPrice(itemRequest.unitPrice());
            
            BigDecimal itemSubtotal = itemRequest.unitPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            BigDecimal itemDiscount = itemSubtotal.subtract(itemRequest.lineTotal());
            invoiceDetail.setDiscountAmount(itemDiscount);
            invoiceDetail.setLineTotal(itemRequest.lineTotal());
            invoiceDetail.setTaxAmount(BigDecimal.ZERO);
            invoiceDetail.setLineTotalWithTax(itemRequest.lineTotal());

            invoiceDetail = saleInvoiceDetailRepository.save(invoiceDetail);

            if (itemRequest.promotionApplied() != null) {
                AppliedPromotion appliedPromotion = new AppliedPromotion();
                appliedPromotion.setInvoiceDetail(invoiceDetail);
                appliedPromotion.setPromotionId(itemRequest.promotionApplied().promotionId());
                appliedPromotion.setPromotionName(itemRequest.promotionApplied().promotionName());
                appliedPromotion.setPromotionDetailId(itemRequest.promotionApplied().promotionDetailId());
                appliedPromotion.setPromotionSummary(itemRequest.promotionApplied().promotionSummary());
                appliedPromotion.setDiscountType(itemRequest.promotionApplied().discountType());
                appliedPromotion.setDiscountValue(itemRequest.promotionApplied().discountValue());
                appliedPromotion.setSourceLineItemId(itemRequest.promotionApplied().sourceLineItemId());

                appliedPromotionRepository.save(appliedPromotion);
                log.debug("Đã lưu khuyến mãi áp dụng: {}", itemRequest.promotionApplied().promotionSummary());
            }

            itemResponses.add(new SaleItemResponseDTO(
                    invoiceDetail.getInvoiceDetailId(),
                    productUnit.getId(),
                    productUnit.getProduct().getName(),
                    productUnit.getUnit().getName(),
                    itemRequest.quantity(),
                    itemRequest.unitPrice(),
                    itemDiscount,
                    itemRequest.lineTotal(),
                    itemRequest.promotionApplied()
            ));
        }

        // Lưu order promotions nếu có
        if (request.appliedOrderPromotions() != null && !request.appliedOrderPromotions().isEmpty()) {
            for (var orderPromotionRequest : request.appliedOrderPromotions()) {
                AppliedOrderPromotion appliedOrderPromotion = new AppliedOrderPromotion();
                appliedOrderPromotion.setInvoice(invoice);
                appliedOrderPromotion.setPromotionId(orderPromotionRequest.promotionId());
                appliedOrderPromotion.setPromotionName(orderPromotionRequest.promotionName());
                appliedOrderPromotion.setPromotionDetailId(orderPromotionRequest.promotionDetailId());
                appliedOrderPromotion.setPromotionSummary(orderPromotionRequest.promotionSummary());
                appliedOrderPromotion.setDiscountType(orderPromotionRequest.discountType());
                appliedOrderPromotion.setDiscountValue(orderPromotionRequest.discountValue());

                appliedOrderPromotionRepository.save(appliedOrderPromotion);
                log.debug("Đã lưu khuyến mãi order: {}", orderPromotionRequest.promotionSummary());
            }
            log.info("Đã lưu {} khuyến mãi order level", request.appliedOrderPromotions().size());
        }

        BigDecimal changeAmount = request.amountPaid().subtract(totalAmount);

        log.info("Hoàn thành bán hàng. Invoice: {}, Tổng tiền: {}", invoiceNumber, totalAmount);

        return new CreateSaleResponseDTO(
                invoiceNumber,
                invoice.getInvoiceDate(),
                subtotal,
                totalDiscount,
                totalAmount,
                request.amountPaid(),
                changeAmount,
                customer != null ? customer.getName() : "Khách vãng lai",
                employee.getName(),
                itemResponses
        );
    }

    private void validateAndCheckStock(List<SaleItemRequestDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidSaleDataException("Danh sách sản phẩm không được rỗng");
        }

        for (SaleItemRequestDTO item : items) {
            if (item.quantity() <= 0) {
                throw new InvalidSaleDataException("Số lượng phải lớn hơn 0 cho sản phẩm ID: " + item.productUnitId());
            }

            if (item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidSaleDataException("Đơn giá phải lớn hơn 0 cho sản phẩm ID: " + item.productUnitId());
            }

            if (!warehouseService.isStockAvailable(item.productUnitId(), item.quantity())) {
                Integer currentStock = warehouseService.getCurrentStock(item.productUnitId());
                throw new InsufficientStockException(
                        item.productUnitId(), 
                        item.quantity(), 
                        currentStock
                );
            }
        }
    }

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "INV" + timestamp + random;
    }
}
