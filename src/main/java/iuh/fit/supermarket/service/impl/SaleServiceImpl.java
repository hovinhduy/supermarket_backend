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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Tạo Invoice cho cả ONLINE và CASH/CARD
        // CASH/CARD: invoice PAID, trừ kho ngay
        // ONLINE: invoice ISSUED, trừ kho khi webhook confirm
        String invoiceNumber = null;
        LocalDateTime invoiceDate = null;
        List<SaleItemResponseDTO> itemResponses = new ArrayList<>();
        
        if (isOnlinePayment) {
            // Tạo invoice với trạng thái ISSUED cho ONLINE payment
            invoiceNumber = createInvoiceForPendingOrder(order);
            log.info("Đã tạo invoice {} với trạng thái ISSUED cho thanh toán ONLINE", invoiceNumber);
        } else {
            // Tạo invoice và trừ kho ngay cho CASH/CARD
            invoiceNumber = invoiceService.createInvoiceForCompletedOrder(order.getOrderId());
            log.info("Đã tạo invoice {} và trừ kho cho thanh toán CASH/CARD", invoiceNumber);
        }
        
        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy invoice vừa tạo"));
        invoiceDate = invoice.getInvoiceDate();
        
        // Lưu thông tin khuyến mãi đã áp dụng (map theo index)
        Map<Integer, PromotionAppliedDTO> itemPromotionsByIndex = new HashMap<>();
        for (int i = 0; i < request.items().size(); i++) {
            SaleItemRequestDTO item = request.items().get(i);
            if (item.promotionApplied() != null) {
                itemPromotionsByIndex.put(i, item.promotionApplied());
            }
        }
        invoiceService.saveAppliedPromotions(invoiceNumber, request.appliedOrderPromotions(), itemPromotionsByIndex);
        log.info("Đã lưu khuyến mãi cho invoice {}", invoiceNumber);
        
        // Tạo item responses từ order details (match theo index)
        for (int i = 0; i < order.getOrderDetails().size(); i++) {
            OrderDetail orderDetail = order.getOrderDetails().get(i);
            PromotionAppliedDTO promotion = itemPromotionsByIndex.get(i);
            
            itemResponses.add(new SaleItemResponseDTO(
                    null,
                    orderDetail.getProductUnit().getId(),
                    orderDetail.getProductUnit().getProduct().getName(),
                    orderDetail.getProductUnit().getUnit().getName(),
                    orderDetail.getQuantity(),
                    orderDetail.getPriceAtPurchase(),
                    orderDetail.getDiscount(),
                    orderDetail.getPriceAtPurchase()
                            .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                            .subtract(orderDetail.getDiscount()),
                    promotion
            ));
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



    /**
     * Tạo invoice với trạng thái ISSUED cho order PENDING (thanh toán ONLINE)
     * Không trừ kho, chờ webhook xác nhận thanh toán
     */
    private String createInvoiceForPendingOrder(Order order) {
        String invoiceNumber = generateInvoiceNumber();
        
        // Tạo Invoice Header với trạng thái ISSUED
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
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());
        invoice.setEmployee(order.getEmployee());

        invoice = saleInvoiceHeaderRepository.save(invoice);
        log.info("Đã tạo invoice {} với trạng thái ISSUED", invoiceNumber);

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

    private String generateInvoiceNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new java.util.Random().nextInt(10000));
        return "INV" + timestamp + random;
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

    @Override
    @Transactional(readOnly = true)
    public SaleInvoicesListResponseDTO getSalesInvoicesWithPromotions(int pageNumber, int pageSize) {
        log.info("Lấy danh sách hoá đơn bán với trang {}, kích thước {}", pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SaleInvoiceHeader> invoicesPage = saleInvoiceHeaderRepository.findAllWithDetails(pageable);

        List<SaleInvoiceFullDTO> invoiceDTOs = invoicesPage.getContent().stream()
                .map(this::convertToSaleInvoiceFullDTO)
                .collect(Collectors.toList());

        return new SaleInvoicesListResponseDTO(
                invoiceDTOs,
                (int) invoicesPage.getTotalElements(),
                pageNumber,
                pageSize
        );
    }

    /**
     * Chuyển đổi entity SaleInvoiceHeader thành DTO SaleInvoiceFullDTO kèm đầy đủ thông tin khuyến mãi
     */
    private SaleInvoiceFullDTO convertToSaleInvoiceFullDTO(SaleInvoiceHeader invoice) {
        // Lấy danh sách items của hóa đơn
        List<SaleInvoiceItemDetailDTO> items = invoice.getInvoiceDetails().stream()
                .map(detail -> convertToSaleInvoiceItemDetailDTO(detail))
                .collect(Collectors.toList());

        // Lấy danh sách khuyến mãi áp dụng cho toàn order
        List<AppliedOrderPromotionDetailDTO> orderPromotions = appliedOrderPromotionRepository
                .findByInvoice_InvoiceId(invoice.getInvoiceId()).stream()
                .map(this::convertToAppliedOrderPromotionDetailDTO)
                .collect(Collectors.toList());

        return new SaleInvoiceFullDTO(
                invoice.getInvoiceId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getOrder() != null ? invoice.getOrder().getOrderId() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getName() : "Khách vãng lai",
                invoice.getEmployee() != null ? invoice.getEmployee().getName() : "",
                invoice.getOrder() != null ? invoice.getOrder().getPaymentMethod() : null,
                invoice.getStatus(),
                invoice.getSubtotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalTax(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                items,
                orderPromotions,
                invoice.getCreatedAt()
        );
    }

    /**
     * Chuyển đổi entity SaleInvoiceDetail thành DTO SaleInvoiceItemDetailDTO kèm danh sách khuyến mãi áp dụng
     */
    private SaleInvoiceItemDetailDTO convertToSaleInvoiceItemDetailDTO(SaleInvoiceDetail detail) {
        // Lấy danh sách khuyến mãi áp dụng cho item này
        List<AppliedPromotionDetailDTO> promotions = appliedPromotionRepository
                .findByInvoiceDetail_InvoiceDetailId(detail.getInvoiceDetailId()).stream()
                .map(this::convertToAppliedPromotionDetailDTO)
                .collect(Collectors.toList());

        return new SaleInvoiceItemDetailDTO(
                detail.getInvoiceDetailId(),
                detail.getProductUnit().getId(),
                detail.getProductUnit().getProduct().getName(),
                detail.getProductUnit().getUnit().getName(),
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getDiscountAmount(),
                detail.getLineTotal(),
                promotions
        );
    }

    /**
     * Chuyển đổi entity AppliedPromotion thành DTO AppliedPromotionDetailDTO
     */
    private AppliedPromotionDetailDTO convertToAppliedPromotionDetailDTO(AppliedPromotion promotion) {
        return new AppliedPromotionDetailDTO(
                promotion.getPromotionId(),
                promotion.getPromotionName(),
                promotion.getPromotionDetailId(),
                promotion.getPromotionSummary(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getSourceLineItemId()
        );
    }

    /**
     * Chuyển đổi entity AppliedOrderPromotion thành DTO AppliedOrderPromotionDetailDTO
     */
    private AppliedOrderPromotionDetailDTO convertToAppliedOrderPromotionDetailDTO(AppliedOrderPromotion promotion) {
        return new AppliedOrderPromotionDetailDTO(
                promotion.getPromotionId(),
                promotion.getPromotionName(),
                promotion.getPromotionDetailId(),
                promotion.getPromotionSummary(),
                promotion.getDiscountType(),
                promotion.getDiscountValue()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoicesListResponseDTO searchSalesInvoices(SaleInvoiceSearchRequestDTO searchRequest) {
        log.info("Tìm kiếm hoá đơn với tiêu chí - Invoice: {}, Customer: {}, From: {}, To: {}, Status: {}",
                searchRequest.invoiceNumber(),
                searchRequest.customerName(),
                searchRequest.fromDate(),
                searchRequest.toDate(),
                searchRequest.status());

        Pageable pageable = PageRequest.of(
                searchRequest.pageNumber() != null ? searchRequest.pageNumber() : 0,
                searchRequest.pageSize() != null ? searchRequest.pageSize() : 10
        );

        Page<SaleInvoiceHeader> invoicesPage = saleInvoiceHeaderRepository.searchAndFilterInvoices(
                searchRequest.invoiceNumber(),
                searchRequest.customerName(),
                searchRequest.fromDate(),
                searchRequest.toDate(),
                searchRequest.status(),
                pageable
        );

        List<SaleInvoiceFullDTO> invoiceDTOs = invoicesPage.getContent().stream()
                .map(this::convertToSaleInvoiceFullDTO)
                .collect(Collectors.toList());

        return new SaleInvoicesListResponseDTO(
                invoiceDTOs,
                (int) invoicesPage.getTotalElements(),
                searchRequest.pageNumber() != null ? searchRequest.pageNumber() : 0,
                searchRequest.pageSize() != null ? searchRequest.pageSize() : 10
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceFullDTO getInvoiceDetail(Integer invoiceId) {
        log.info("Lấy thông tin chi tiết hoá đơn ID: {}", invoiceId);

        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hoá đơn với ID: " + invoiceId));

        return convertToSaleInvoiceFullDTO(invoice);
    }
}
