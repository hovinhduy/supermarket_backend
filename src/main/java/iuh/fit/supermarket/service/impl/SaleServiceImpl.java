package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.dto.sale.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.PaymentMethod;
import iuh.fit.supermarket.exception.InsufficientStockException;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.InvoiceService;
import iuh.fit.supermarket.service.PaymentService;
import iuh.fit.supermarket.service.SaleService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

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

        BigDecimal orderDiscount = BigDecimal.ZERO;
        if (request.appliedOrderPromotions() != null && !request.appliedOrderPromotions().isEmpty()) {
            for (var orderPromotion : request.appliedOrderPromotions()) {
                orderDiscount = orderDiscount.add(orderPromotion.discountValue());
            }
        }

        BigDecimal totalDiscount = lineItemDiscount.add(orderDiscount);
        BigDecimal totalAmount = subtotal.subtract(totalDiscount);

        boolean isOnlinePayment = request.paymentMethod() == PaymentMethod.ONLINE;
        
        // Tạo Order
        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setSubtotal(subtotal);
        order.setTotalAmount(totalAmount);
        order.setAmountPaid(isOnlinePayment ? BigDecimal.ZERO : request.amountPaid());
        order.setStatus(isOnlinePayment ? OrderStatus.PENDING : OrderStatus.COMPLETED);
        order.setPaymentMethod(request.paymentMethod());
        order.setNote(request.note());
        order.setEmployee(employee);
        order.setCustomer(customer);

        order = orderRepository.save(order);
        log.info("Đã tạo order {} với trạng thái: {}", order.getOrderId(), order.getStatus());

        // Tạo OrderDetail
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

        // Chỉ tạo Invoice nếu KHÔNG phải ONLINE payment
        String invoiceNumber = null;
        LocalDateTime invoiceDate = null;
        List<SaleItemResponseDTO> itemResponses = new ArrayList<>();
        
        if (!isOnlinePayment) {
            invoiceNumber = invoiceService.createInvoiceForCompletedOrder(order.getOrderId());
            SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByInvoiceNumber(invoiceNumber)
                    .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy invoice vừa tạo"));
            invoiceDate = invoice.getInvoiceDate();
            
            // Tạo item responses từ order details (đã có sẵn)
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                BigDecimal itemSubtotal = orderDetail.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(orderDetail.getQuantity()));
                BigDecimal itemDiscount = itemSubtotal.subtract(
                        orderDetail.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                                .subtract(orderDetail.getDiscount())
                );
                
                itemResponses.add(new SaleItemResponseDTO(
                        null, // invoice detail ID chưa có
                        orderDetail.getProductUnit().getId(),
                        orderDetail.getProductUnit().getProduct().getName(),
                        orderDetail.getProductUnit().getUnit().getName(),
                        orderDetail.getQuantity(),
                        orderDetail.getPriceAtPurchase(),
                        orderDetail.getDiscount(),
                        orderDetail.getPriceAtPurchase()
                                .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                                .subtract(orderDetail.getDiscount()),
                        findPromotionAppliedFromRequest(orderDetail.getProductUnit().getId(), request.items())
                ));
            }
        } else {
            log.info("Thanh toán ONLINE - Chờ xác nhận thanh toán mới tạo invoice");
        }

        BigDecimal changeAmount = isOnlinePayment ? BigDecimal.ZERO : request.amountPaid().subtract(totalAmount);

        // Tạo payment link nếu ONLINE
        String paymentUrl = null;
        String qrCode = null;
        
        if (isOnlinePayment) {
            List<PaymentService.PaymentItemData> paymentItems = request.items().stream()
                    .map(item -> {
                        ProductUnit productUnit = productUnitRepository.findById(item.productUnitId())
                                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy sản phẩm"));
                        return new PaymentService.PaymentItemData(
                                productUnit.getProduct().getName() + " - " + productUnit.getUnit().getName(),
                                item.quantity(),
                                item.unitPrice().intValue()
                        );
                    })
                    .toList();

            CreatePaymentLinkResponse paymentResponse = paymentService.createPaymentLink(
                    order.getOrderId(),
                    totalAmount,
                    "Thanh toan don hang",
                    paymentItems
            );
            
            paymentUrl = paymentResponse.getCheckoutUrl();
            qrCode = paymentResponse.getQrCode();
            log.info("Đã tạo payment link: {}", paymentUrl);
        }

        log.info("Hoàn thành tạo order. Invoice: {}, Tổng tiền: {}, Trạng thái: {}", 
                invoiceNumber, totalAmount, order.getStatus());

        return new CreateSaleResponseDTO(
                invoiceNumber,
                invoiceDate,
                subtotal,
                totalDiscount,
                totalAmount,
                isOnlinePayment ? BigDecimal.ZERO : request.amountPaid(),
                changeAmount,
                customer != null ? customer.getName() : "Khách vãng lai",
                employee.getName(),
                itemResponses,
                order.getOrderId(),
                paymentUrl,
                qrCode,
                order.getStatus().getValue()
        );
    }



    private PromotionAppliedDTO findPromotionAppliedFromRequest(Long productUnitId, List<SaleItemRequestDTO> items) {
        for (SaleItemRequestDTO item : items) {
            if (item.productUnitId().longValue() == productUnitId && item.promotionApplied() != null) {
                return item.promotionApplied();
            }
        }
        return null;
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

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponseDTO getOrderStatus(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Tìm invoice nếu có
        String invoiceNumber = null;
        LocalDateTime invoiceDate = null;
        
        List<SaleInvoiceHeader> invoices = saleInvoiceHeaderRepository.findByOrder_OrderId(orderId);
        if (!invoices.isEmpty()) {
            SaleInvoiceHeader invoice = invoices.get(0);
            invoiceNumber = invoice.getInvoiceNumber();
            invoiceDate = invoice.getInvoiceDate();
        }

        return new OrderStatusResponseDTO(
                order.getOrderId(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getTotalAmount(),
                order.getAmountPaid(),
                invoiceNumber,
                invoiceDate,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
