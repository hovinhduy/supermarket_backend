package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.dto.sale.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.PaymentMethod;
import iuh.fit.supermarket.exception.InsufficientStockException;
import iuh.fit.supermarket.exception.InvalidSaleDataException;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.InvoiceService;
import iuh.fit.supermarket.service.InvoicePdfService;
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

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleServiceImpl implements SaleService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductUnitRepository productUnitRepository;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final SaleInvoiceDetailRepository saleInvoiceDetailRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final AppliedOrderPromotionRepository appliedOrderPromotionRepository;
    private final WarehouseService warehouseService;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;
    private final InvoicePdfService invoicePdfService;
    private final PromotionDetailRepository promotionDetailRepository;

    @Override
    @Transactional
    public CreateSaleResponseDTO createSale(CreateSaleRequestDTO request) {
        log.info("Bắt đầu tạo bán hàng cho nhân viên ID: {}", request.employeeId());

        // Validate nhân viên và khách hàng
        Employee employee = employeeRepository.findById(request.employeeId())
                .orElseThrow(
                        () -> new InvalidSaleDataException("Không tìm thấy nhân viên với ID: " + request.employeeId()));

        Customer customer = null;
        if (request.customerId() != null) {
            customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy khách hàng với ID: " + request.customerId()));
        }

        // Validate khuyến mãi (kiểm tra usageLimit)
        validatePromotions(request);

        // Kiểm tra tồn kho
        validateAndCheckStock(request.items());

        // Tính toán tổng tiền
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
        boolean isCashPayment = request.paymentMethod() == PaymentMethod.CASH;

        // Tạo Invoice trực tiếp (không cần Order)
        // CASH: invoice PAID, trừ kho ngay
        // ONLINE: invoice UNPAID, trừ kho khi webhook confirm
        String invoiceNumber = generateInvoiceNumber();
        LocalDateTime invoiceDate = LocalDateTime.now();

        SaleInvoiceHeader invoice = new SaleInvoiceHeader();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceDate(invoiceDate);
        invoice.setSubtotal(subtotal);
        invoice.setTotalDiscount(totalDiscount);
        invoice.setTotalTax(BigDecimal.ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(isCashPayment ? InvoiceStatus.PAID : InvoiceStatus.UNPAID);
        invoice.setPaymentMethod(request.paymentMethod());
        invoice.setPaidAmount(isCashPayment ? totalAmount : BigDecimal.ZERO);
        invoice.setOrder(null); // Không cần Order nữa
        invoice.setCustomer(customer);
        invoice.setEmployee(employee);

        invoice = saleInvoiceHeaderRepository.save(invoice);
        log.info("Đã tạo invoice {} với trạng thái: {}", invoiceNumber, invoice.getStatus());

        // Tạo Invoice Details
        List<SaleInvoiceDetail> invoiceDetails = new ArrayList<>();
        List<SaleItemResponseDTO> itemResponses = new ArrayList<>();
        Map<Integer, PromotionAppliedDTO> itemPromotionsByIndex = new HashMap<>();

        for (int i = 0; i < request.items().size(); i++) {
            SaleItemRequestDTO item = request.items().get(i);
            ProductUnit productUnit = productUnitRepository.findById(item.productUnitId())
                    .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy đơn vị sản phẩm với ID: " + item.productUnitId()));

            BigDecimal itemSubtotal = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            BigDecimal itemDiscount = itemSubtotal.subtract(item.lineTotal());

            SaleInvoiceDetail invoiceDetail = new SaleInvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductUnit(productUnit);
            invoiceDetail.setQuantity(item.quantity());
            invoiceDetail.setUnitPrice(item.unitPrice());
            invoiceDetail.setDiscountAmount(itemDiscount);
            invoiceDetail.setLineTotal(item.lineTotal());
            invoiceDetail.setTaxAmount(BigDecimal.ZERO);
            invoiceDetail.setLineTotalWithTax(item.lineTotal());

            invoiceDetails.add(saleInvoiceDetailRepository.save(invoiceDetail));

            // Lưu promotion info
            if (item.promotionApplied() != null) {
                itemPromotionsByIndex.put(i, item.promotionApplied());
            }

            // Tạo item response
            itemResponses.add(new SaleItemResponseDTO(
                    invoiceDetail.getInvoiceDetailId(),
                    productUnit.getId(),
                    productUnit.getProduct().getName(),
                    productUnit.getUnit().getName(),
                    item.quantity(),
                    item.unitPrice(),
                    itemDiscount,
                    item.lineTotal(),
                    item.promotionApplied()));
        }
        invoice.setInvoiceDetails(invoiceDetails);

        // Lưu thông tin khuyến mãi đã áp dụng
        invoiceService.saveAppliedPromotions(invoiceNumber, request.appliedOrderPromotions(), itemPromotionsByIndex);
        log.info("Đã lưu khuyến mãi cho invoice {}", invoiceNumber);

        // Trừ kho ngay cho thanh toán CASH
        if (isCashPayment) {
            for (SaleInvoiceDetail detail : invoiceDetails) {
                warehouseService.stockOut(
                        detail.getProductUnit().getId(),
                        detail.getQuantity(),
                        invoiceNumber,
                        "Bán hàng thanh toán tiền mặt - Invoice: " + invoiceNumber);
            }
            log.info("Đã trừ kho cho invoice {} (thanh toán tiền mặt)", invoiceNumber);

            // Cập nhật usage count cho khuyến mãi (thanh toán tiền mặt)
            updatePromotionUsageCount(request, invoiceNumber);
        }

        BigDecimal changeAmount = isCashPayment ? request.amountPaid().subtract(totalAmount) : BigDecimal.ZERO;

        // Xử lý thanh toán ONLINE
        Long paymentOrderCode = null;
        String paymentUrl = null;
        String qrCode = null;

        if (!isCashPayment) {
             // Sử dụng invoiceId làm orderCode cho payment, thêm prefix 2 tỷ để phân biệt với Order
             // 1xxxxxxxxx: Order
             // 2xxxxxxxxx: Invoice
             paymentOrderCode = 2000000000L + invoice.getInvoiceId();

             List<PaymentService.PaymentItemData> paymentItems = request.items().stream()
                    .map(item -> {
                        ProductUnit productUnit = productUnitRepository.findById(item.productUnitId())
                                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
                        return new PaymentService.PaymentItemData(
                                productUnit.getProduct().getName() + " - " + productUnit.getUnit().getName(),
                                item.quantity(),
                                item.unitPrice().intValue());
                    })
                    .toList();

            CreatePaymentLinkResponse paymentResponse = paymentService.createPaymentLink(
                    paymentOrderCode,
                    totalAmount,
                    "Thanh toan QR",
                    paymentItems);

            paymentUrl = paymentResponse.getCheckoutUrl();
            qrCode = paymentResponse.getQrCode();
            log.info("Đã tạo payment link cho invoice {}: {}", invoiceNumber, paymentUrl);
        }

        log.info("Hoàn thành tạo invoice. Invoice: {}, Tổng tiền: {}, Trạng thái: {}",
                invoiceNumber, totalAmount, invoice.getStatus());

        return new CreateSaleResponseDTO(
                invoice.getInvoiceId(),
                invoiceNumber,
                invoiceDate,
                subtotal,
                totalDiscount,
                totalAmount,
                isCashPayment ? request.amountPaid() : BigDecimal.ZERO,
                changeAmount,
                customer != null ? customer.getUser().getName() : "Khách vãng lai",
                employee.getUser().getName(),
                itemResponses,
                paymentOrderCode,
                paymentUrl,
                qrCode,
                invoice.getStatus().getValue());
    }

    /**
     * Tạo mã hóa đơn tự động
     */
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
                        currentStock);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponseDTO getInvoiceStatus(Long invoiceId) {
        log.info("Kiểm tra trạng thái hóa đơn ID: {}", invoiceId);

        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findById(invoiceId.intValue())
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        return new OrderStatusResponseDTO(
                invoiceId,
                invoice.getStatus(),
                invoice.getPaymentMethod(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoicesListResponseDTO searchAndFilterSalesInvoices(
            String searchKeyword,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            Integer employeeId,
            Integer customerId,
            Integer productUnitId,
            int pageNumber,
            int pageSize) {
        log.info(
                "Tìm kiếm hoá đơn - Keyword: {}, From: {}, To: {}, Status: {}, EmployeeId: {}, CustomerId: {}, ProductUnitId: {}, Page: {}, Size: {}",
                searchKeyword, fromDate, toDate, status, employeeId, customerId, productUnitId, pageNumber, pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SaleInvoiceHeader> invoicesPage = saleInvoiceHeaderRepository.searchAndFilterInvoices(
                searchKeyword,
                fromDate,
                toDate,
                status,
                employeeId,
                customerId,
                productUnitId,
                pageable);

        List<SaleInvoiceFullDTO> invoiceDTOs = invoicesPage.getContent().stream()
                .map(this::convertToSaleInvoiceFullDTO)
                .collect(Collectors.toList());

        return new SaleInvoicesListResponseDTO(
                invoiceDTOs,
                (int) invoicesPage.getTotalElements(),
                pageNumber,
                pageSize);
    }

    /**
     * Chuyển đổi entity SaleInvoiceHeader thành DTO SaleInvoiceFullDTO kèm đầy đủ
     * thông tin khuyến mãi
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
                invoice.getCustomer() != null ? invoice.getCustomer().getUser().getName() : "Khách vãng lai",
                invoice.getEmployee() != null ? invoice.getEmployee().getUser().getName() : "",
                invoice.getPaymentMethod(),
                invoice.getStatus(),
                invoice.getSubtotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalTax(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                items,
                orderPromotions,
                invoice.getCreatedAt());
    }

    /**
     * Chuyển đổi entity SaleInvoiceDetail thành DTO SaleInvoiceItemDetailDTO kèm
     * danh sách khuyến mãi áp dụng
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
                promotions);
    }

    /**
     * Chuyển đổi entity AppliedPromotion thành DTO AppliedPromotionDetailDTO
     */
    private AppliedPromotionDetailDTO convertToAppliedPromotionDetailDTO(AppliedPromotion promotion) {
        return new AppliedPromotionDetailDTO(
                promotion.getPromotionName(),
                promotion.getPromotionLineId(),
                promotion.getPromotionDetailId(),
                promotion.getPromotionSummary(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getSourceLineItemId());
    }

    /**
     * Chuyển đổi entity AppliedOrderPromotion thành DTO
     * AppliedOrderPromotionDetailDTO
     */
    private AppliedOrderPromotionDetailDTO convertToAppliedOrderPromotionDetailDTO(AppliedOrderPromotion promotion) {
        return new AppliedOrderPromotionDetailDTO(
                promotion.getPromotionId(),
                promotion.getPromotionName(),
                promotion.getPromotionDetailId(),
                promotion.getPromotionSummary(),
                promotion.getDiscountType(),
                promotion.getDiscountValue());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceFullDTO getInvoiceDetail(Integer invoiceId) {
        log.info("Lấy thông tin chi tiết hoá đơn ID: {}", invoiceId);

        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hoá đơn với ID: " + invoiceId));

        return convertToSaleInvoiceFullDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Integer invoiceId) {
        log.info("Tạo PDF cho hóa đơn ID: {}", invoiceId);

        SaleInvoiceFullDTO invoice = getInvoiceDetail(invoiceId);

        return invoicePdfService.generateInvoicePdf(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public String generateInvoiceHtml(Integer invoiceId) {
        log.info("Tạo HTML để in cho hóa đơn ID: {}", invoiceId);

        SaleInvoiceFullDTO invoice = getInvoiceDetail(invoiceId);

        try {
            // Load template
            InputStream templateStream = getClass().getResourceAsStream("/templates/invoice-print-template.html");
            if (templateStream == null) {
                throw new RuntimeException("Không tìm thấy template hóa đơn");
            }

            String template = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);

            // Format currency
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            // Build items rows HTML
            StringBuilder itemsRows = new StringBuilder();
            int index = 1;
            for (var item : invoice.items()) {
                itemsRows.append("<tr>")
                        .append("<td class=\"center\">").append(index++).append("</td>")
                        .append("<td>").append(item.productName()).append("</td>")
                        .append("<td class=\"center\">").append(item.unit()).append("</td>")
                        .append("<td class=\"center\">").append(item.quantity()).append("</td>")
                        .append("<td class=\"right\">").append(currencyFormatter.format(item.unitPrice()))
                        .append("</td>")
                        .append("<td class=\"right\">").append(currencyFormatter.format(item.lineTotal()))
                        .append("</td>")
                        .append("</tr>");

                // Add promotions if any
                if (item.appliedPromotions() != null && !item.appliedPromotions().isEmpty()) {
                    itemsRows.append("<tr class=\"promotion-row\">")
                            .append("<td colspan=\"6\" style=\"padding-left: 30px;\">");

                    for (var promo : item.appliedPromotions()) {
                        itemsRows.append("→ ").append(promo.promotionSummary())
                                .append(" (-").append(currencyFormatter.format(promo.discountValue())).append(") ");
                    }

                    itemsRows.append("</td></tr>");
                }
            }

            // Build order promotions HTML
            String orderPromotionsHtml = "";
            if (invoice.appliedOrderPromotions() != null && !invoice.appliedOrderPromotions().isEmpty()) {
                StringBuilder promotions = new StringBuilder();
                promotions.append("<div class=\"order-promotions\">")
                        .append("<h4>KHUYẾN MÃI ĐƠN HÀNG</h4>")
                        .append("<ul>");

                for (var promo : invoice.appliedOrderPromotions()) {
                    promotions.append("<li>• ").append(promo.promotionSummary())
                            .append(": -").append(currencyFormatter.format(promo.discountValue()))
                            .append("</li>");
                }

                promotions.append("</ul></div>");
                orderPromotionsHtml = promotions.toString();
            }

            // Calculate remaining
            BigDecimal remaining = invoice.totalAmount().subtract(invoice.paidAmount());

            // Replace placeholders
            String html = template
                    .replace("{{invoiceNumber}}", invoice.invoiceNumber())
                    .replace("{{invoiceDate}}", invoice.invoiceDate().format(dateFormatter))
                    .replace("{{status}}", getStatusText(invoice.status()))
                    .replace("{{paymentMethod}}", getPaymentMethodText(invoice.paymentMethod()))
                    .replace("{{customerName}}", invoice.customerName() != null ? invoice.customerName() : "Khách lẻ")
                    .replace("{{employeeName}}", invoice.employeeName())
                    .replace("{{itemsRows}}", itemsRows.toString())
                    .replace("{{orderPromotions}}", orderPromotionsHtml)
                    .replace("{{subtotal}}", currencyFormatter.format(invoice.subtotal()))
                    .replace("{{totalDiscount}}", currencyFormatter.format(invoice.totalDiscount()))
                    .replace("{{totalTax}}", currencyFormatter.format(invoice.totalTax()))
                    .replace("{{totalAmount}}", currencyFormatter.format(invoice.totalAmount()))
                    .replace("{{paidAmount}}", currencyFormatter.format(invoice.paidAmount()))
                    .replace("{{remaining}}", currencyFormatter.format(remaining))
                    .replace("{{printTime}}", LocalDateTime.now().format(dateFormatter));

            return html;

        } catch (Exception e) {
            log.error("Lỗi khi tạo HTML hóa đơn: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo HTML hóa đơn", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoicesListResponseDTO getCustomerInvoices(
            String username,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            int pageNumber,
            int pageSize) {
        log.info("Lấy danh sách hóa đơn cho customer: {}, FromDate: {}, ToDate: {}, Status: {}, Page: {}, Size: {}",
                username, fromDate, toDate, status, pageNumber, pageSize);

        // Tìm customer theo username (email hoặc phone)
        User user = userRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy tài khoản khách hàng"));

        if (!user.isCustomer()) {
            throw new InvalidSaleDataException("Tài khoản không phải là khách hàng");
        }

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new InvalidSaleDataException("Không tìm thấy thông tin khách hàng");
        }

        // Mặc định lấy hóa đơn PAID nếu không truyền status
        InvoiceStatus invoiceStatus = status != null ? status : InvoiceStatus.PAID;
        log.info("Sử dụng status: {} (mặc định PAID nếu null)", invoiceStatus);

        // Gọi method searchAndFilterSalesInvoices với customerId
        return searchAndFilterSalesInvoices(
                null, // searchKeyword
                fromDate,
                toDate,
                invoiceStatus,
                null, // employeeId
                customer.getCustomerId(), // customerId
                null, // productUnitId
                pageNumber,
                pageSize
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SaleInvoiceFullDTO getCustomerInvoiceDetail(String username, Integer invoiceId) {
        log.info("Customer {} lấy chi tiết hóa đơn ID: {}", username, invoiceId);

        // Tìm customer theo username (email hoặc phone)
        User user = userRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy tài khoản khách hàng"));

        if (!user.isCustomer()) {
            throw new InvalidSaleDataException("Tài khoản không phải là khách hàng");
        }

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new InvalidSaleDataException("Không tìm thấy thông tin khách hàng");
        }

        // Lấy chi tiết hóa đơn
        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new InvalidSaleDataException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        // Kiểm tra quyền sở hữu: hóa đơn phải thuộc về customer này
        if (invoice.getCustomer() == null || 
            !invoice.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            log.warn("Customer {} cố gắng truy cập hóa đơn {} không thuộc về mình", 
                    username, invoiceId);
            throw new InvalidSaleDataException("Bạn không có quyền xem hóa đơn này");
        }

        log.info("Customer {} được phép xem hóa đơn {}", username, invoiceId);

        // Chuyển đổi sang DTO
        return convertToSaleInvoiceFullDTO(invoice);
    }

    private String getStatusText(InvoiceStatus status) {
        return switch (status) {
            case UNPAID -> "Chưa thanh toán";
            case PAID -> "Đã thanh toán";
            default -> status.name();
        };
    }

    /**
     * Validate các khuyến mãi trong request còn sử dụng được không
     * Kiểm tra usageLimit trước khi áp dụng
     *
     * @param request Thông tin bán hàng
     * @throws InvalidSaleDataException nếu promotion đã hết lượt sử dụng
     */
    private void validatePromotions(CreateSaleRequestDTO request) {
        log.debug("Bắt đầu validate khuyến mãi cho bán hàng");

        java.util.Set<Long> checkedPromotionIds = new java.util.HashSet<>();

        // Validate khuyến mãi từ items
        for (SaleItemRequestDTO item : request.items()) {
            if (item.promotionApplied() != null &&
                item.promotionApplied().promotionDetailId() != null) {

                Long promotionDetailId = item.promotionApplied().promotionDetailId();

                // Tránh kiểm tra trùng
                if (checkedPromotionIds.contains(promotionDetailId)) {
                    continue;
                }

                PromotionDetail detail = promotionDetailRepository.findById(promotionDetailId)
                    .orElseThrow(() -> new InvalidSaleDataException(
                        "Không tìm thấy khuyến mãi với ID: " + promotionDetailId));

                // Kiểm tra usageLimit
                if (!canUsePromotion(detail)) {
                    String promotionName = item.promotionApplied().promotionName();
                    throw new InvalidSaleDataException(
                        String.format("Khuyến mãi '%s' đã hết lượt sử dụng (giới hạn: %d, đã dùng: %d)",
                            promotionName,
                            detail.getUsageLimit(),
                            detail.getUsageCount()));
                }

                checkedPromotionIds.add(promotionDetailId);
            }
        }

        // Validate khuyến mãi order level
        if (request.appliedOrderPromotions() != null) {
            for (var orderPromotion : request.appliedOrderPromotions()) {
                if (orderPromotion.promotionDetailId() != null) {
                    Long promotionDetailId = orderPromotion.promotionDetailId();

                    // Tránh kiểm tra trùng
                    if (checkedPromotionIds.contains(promotionDetailId)) {
                        continue;
                    }

                    PromotionDetail detail = promotionDetailRepository.findById(promotionDetailId)
                        .orElseThrow(() -> new InvalidSaleDataException(
                            "Không tìm thấy khuyến mãi với ID: " + promotionDetailId));

                    // Kiểm tra usageLimit
                    if (!canUsePromotion(detail)) {
                        String promotionName = orderPromotion.promotionName();
                        throw new InvalidSaleDataException(
                            String.format("Khuyến mãi '%s' đã hết lượt sử dụng (giới hạn: %d, đã dùng: %d)",
                                promotionName,
                                detail.getUsageLimit(),
                                detail.getUsageCount()));
                    }

                    checkedPromotionIds.add(promotionDetailId);
                }
            }
        }

        log.info("Đã validate {} khuyến mãi, tất cả còn khả dụng", checkedPromotionIds.size());
    }

    /**
     * Kiểm tra xem promotion detail còn có thể sử dụng không
     *
     * @param detail PromotionDetail cần kiểm tra
     * @return true nếu còn có thể sử dụng, false nếu đã hết lượt
     */
    private boolean canUsePromotion(PromotionDetail detail) {
        // Nếu không có giới hạn (usageLimit = null), luôn có thể sử dụng
        if (detail.getUsageLimit() == null) {
            return true;
        }

        // Nếu có giới hạn, kiểm tra usageCount < usageLimit
        Integer usageCount = detail.getUsageCount() != null ? detail.getUsageCount() : 0;
        return usageCount < detail.getUsageLimit();
    }

    /**
     * Cập nhật số lượng sử dụng khuyến mãi khi bán hàng thành công
     *
     * @param request Thông tin bán hàng
     * @param invoiceNumber Số hóa đơn
     */
    private void updatePromotionUsageCount(CreateSaleRequestDTO request, String invoiceNumber) {
        log.info("Cập nhật usage count cho các khuyến mãi của invoice {}", invoiceNumber);

        java.util.Set<Long> processedDetailIds = new java.util.HashSet<>();

        // Cập nhật usageCount cho khuyến mãi từ items
        for (SaleItemRequestDTO item : request.items()) {
            if (item.promotionApplied() != null &&
                item.promotionApplied().promotionDetailId() != null &&
                !processedDetailIds.contains(item.promotionApplied().promotionDetailId())) {

                promotionDetailRepository.findById(item.promotionApplied().promotionDetailId())
                    .ifPresent(detail -> {
                        Integer currentCount = detail.getUsageCount() != null ? detail.getUsageCount() : 0;
                        detail.setUsageCount(currentCount + 1);
                        promotionDetailRepository.save(detail);
                        log.debug("Đã cập nhật usageCount cho promotion detail ID: {} ({}->{})",
                            detail.getDetailId(), currentCount, currentCount + 1);
                    });

                processedDetailIds.add(item.promotionApplied().promotionDetailId());
            }
        }

        // Cập nhật usageCount cho khuyến mãi đơn hàng
        if (request.appliedOrderPromotions() != null) {
            for (var orderPromotion : request.appliedOrderPromotions()) {
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
        }

        log.info("Hoàn thành cập nhật usage count cho {} promotion details", processedDetailIds.size());
    }

    @Override
    @Transactional
    public void confirmInvoicePayment(Integer invoiceId) {
        log.info("Xác nhận thanh toán cho Invoice ID: {}", invoiceId);

        // Fetch invoice with details to ensure we have everything for stock deduction
        SaleInvoiceHeader invoice = saleInvoiceHeaderRepository.findByIdWithDetails(invoiceId)
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
        if (invoice.getInvoiceDetails() != null) {
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

        // Cập nhật usage count cho khuyến mãi
        updatePromotionUsageCountForInvoice(invoice);
    }

    /**
     * Cập nhật số lượng sử dụng khuyến mãi dựa trên thông tin invoice đã lưu
     */
    private void updatePromotionUsageCountForInvoice(SaleInvoiceHeader invoice) {
        log.info("Cập nhật usage count cho các khuyến mãi của invoice {}", invoice.getInvoiceNumber());
        java.util.Set<Long> processedDetailIds = new java.util.HashSet<>();

        // 1. Cập nhật usageCount cho khuyến mãi từ items
        if (invoice.getInvoiceDetails() != null) {
            for (SaleInvoiceDetail detail : invoice.getInvoiceDetails()) {
                List<AppliedPromotion> appliedPromotions = appliedPromotionRepository
                        .findByInvoiceDetail_InvoiceDetailId(detail.getInvoiceDetailId());
                
                for (AppliedPromotion ap : appliedPromotions) {
                    if (ap.getPromotionDetailId() != null && !processedDetailIds.contains(ap.getPromotionDetailId())) {
                        promotionDetailRepository.findById(ap.getPromotionDetailId()).ifPresent(pDetail -> {
                            Integer currentCount = pDetail.getUsageCount() != null ? pDetail.getUsageCount() : 0;
                            pDetail.setUsageCount(currentCount + 1);
                            promotionDetailRepository.save(pDetail);
                            log.debug("Đã cập nhật usageCount cho promotion detail ID: {} ({}->{})",
                                    pDetail.getDetailId(), currentCount, currentCount + 1);
                        });
                        processedDetailIds.add(ap.getPromotionDetailId());
                    }
                }
            }
        }

        // 2. Cập nhật usageCount cho khuyến mãi đơn hàng
        List<AppliedOrderPromotion> orderPromotions = appliedOrderPromotionRepository
                .findByInvoice_InvoiceId(invoice.getInvoiceId());
        
        for (AppliedOrderPromotion aop : orderPromotions) {
            if (aop.getPromotionDetailId() != null && !processedDetailIds.contains(aop.getPromotionDetailId())) {
                promotionDetailRepository.findById(aop.getPromotionDetailId()).ifPresent(pDetail -> {
                    Integer currentCount = pDetail.getUsageCount() != null ? pDetail.getUsageCount() : 0;
                    pDetail.setUsageCount(currentCount + 1);
                    promotionDetailRepository.save(pDetail);
                    log.debug("Đã cập nhật usageCount cho order promotion detail ID: {} ({}->{})",
                            pDetail.getDetailId(), currentCount, currentCount + 1);
                });
                processedDetailIds.add(aop.getPromotionDetailId());
            }
        }
        
        log.info("Hoàn thành cập nhật usage count cho {} promotion details (từ webhook)", processedDetailIds.size());
    }

    private String getPaymentMethodText(PaymentMethod method) {
        if (method == null) {
            return "Không xác định";
        }
        return switch (method) {
            case CASH -> "Tiền mặt";
            case CARD -> "Thẻ";
            case ONLINE -> "Chuyển khoản";
            default -> method.name();
        };
    }
}
