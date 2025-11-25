package iuh.fit.supermarket.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.checkout.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.*;
import iuh.fit.supermarket.exception.BadRequestException;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.CheckoutService;
import iuh.fit.supermarket.service.PromotionCheckService;
import iuh.fit.supermarket.service.SaleService;
import iuh.fit.supermarket.exception.UnauthorizedException;
import iuh.fit.supermarket.validator.OrderStatusTransitionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation của CheckoutService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final ProductUnitRepository productUnitRepository;
    private final WarehouseRepository warehouseRepository;
    private final PriceDetailRepository priceDetailRepository;
    private final SaleService saleService;
    private final PromotionCheckService promotionCheckService;
    private final ObjectMapper objectMapper;
    private final iuh.fit.supermarket.service.PaymentService paymentService;
    private final OrderStatusTransitionValidator statusTransitionValidator;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final SaleInvoiceDetailRepository saleInvoiceDetailRepository;
    private final AppliedPromotionRepository appliedPromotionRepository;
    private final AppliedOrderPromotionRepository appliedOrderPromotionRepository;
    private final iuh.fit.supermarket.service.WarehouseService warehouseService;
    private final PromotionDetailRepository promotionDetailRepository;

    /**
     * Thực hiện checkout giỏ hàng cho khách hàng
     */
    @Override
    @Transactional
    public CheckoutResponseDTO checkoutForCustomer(String username, CheckoutRequestDTO request) {
        log.info("Bắt đầu checkout cho khách hàng: {}", username);

        // Bỏ prefix nếu có
        String actualUsername = extractActualUsername(username);
        log.info("Tìm user với email/phone: {}", actualUsername);

        // Tìm customer theo username (email hoặc phone)
        User user = userRepository.findByEmailOrPhone(actualUsername, actualUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản khách hàng"));

        if (!user.isCustomer()) {
            throw new BadRequestException("Tài khoản không phải là khách hàng");
        }

        Customer customer = user.getCustomer();
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy thông tin khách hàng");
        }

        // Lấy giỏ hàng của khách hàng
        ShoppingCart cart = shoppingCartRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giỏ hàng"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // Tạo đơn hàng
        Order order = new Order();
        order.setOrderCode(generateOrderCode()); // Sinh mã đơn hàng tự động
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setEmployee(null); // Không có nhân viên vì khách hàng tự checkout
        order.setStatus(OrderStatus.UNPAID); // Khởi tạo với trạng thái UNPAID
        order.setDeliveryType(request.deliveryType());
        order.setPaymentMethod(request.paymentMethod());
        order.setNote(request.orderNote());

        // Set thông tin giao hàng nếu giao hàng tận nơi
        if (request.deliveryType() == DeliveryType.HOME_DELIVERY) {
            if (request.deliveryAddress() != null) {
                order.setDeliveryAddress(request.deliveryAddress());

                // Tính phí vận chuyển
                BigDecimal shippingFee = calculateShippingFee(request.deliveryAddress());
                order.setShippingFee(shippingFee);
            }
        } else {
            order.setShippingFee(BigDecimal.ZERO);
        }

        // Bước 1: Convert CartItem sang CheckPromotionRequestDTO để tính khuyến mãi
        List<CartItemRequestDTO> cartItemRequests = cartItems.stream()
                .map(item -> new CartItemRequestDTO(item.getProductUnit().getId(), item.getQuantity()))
                .toList();

        CheckPromotionRequestDTO promotionRequest = new CheckPromotionRequestDTO(cartItemRequests);

        // Bước 2: Gọi PromotionCheckService để tính khuyến mãi
        log.info("Kiểm tra khuyến mãi cho {} sản phẩm trong giỏ hàng", cartItemRequests.size());
        CheckPromotionResponseDTO promotionResponse = promotionCheckService.checkAndApplyPromotions(promotionRequest);
        log.info("Áp dụng khuyến mãi: lineItemDiscount={}, orderDiscount={}",
                promotionResponse.summary().lineItemDiscount(),
                promotionResponse.summary().orderDiscount());

        // Bước 3: Kiểm tra tồn kho trước khi tạo đơn hàng
        for (CartItemResponseDTO cartItemResponse : promotionResponse.items()) {
            // Chỉ kiểm tra tồn kho cho item gốc (không phải gift item tự động)
            if (cartItemResponse.promotionApplied() != null &&
                    cartItemResponse.promotionApplied().sourceLineItemId() != null) {
                // Đây là gift item tự động thêm, bỏ qua kiểm tra tồn kho
                continue;
            }

            ProductUnit productUnit = productUnitRepository.findById(cartItemResponse.productUnitId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Không tìm thấy sản phẩm với ID: %d", cartItemResponse.productUnitId())));

            Warehouse warehouse = warehouseRepository.findByProductUnit(productUnit)
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Không tìm thấy thông tin kho cho sản phẩm %s",
                                    productUnit.getProduct().getName())));

            if (warehouse.getQuantityOnHand() < cartItemResponse.quantity()) {
                throw new BadRequestException(
                        String.format("Sản phẩm %s không đủ hàng trong kho (còn %d, yêu cầu %d)",
                                productUnit.getProduct().getName(),
                                warehouse.getQuantityOnHand(),
                                cartItemResponse.quantity()));
            }
        }

        // Bước 4: Map từ CheckPromotionResponseDTO sang OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        Map<Long, Long> lineItemIdToOrderDetailId = new HashMap<>(); // Map lineItemId → OrderDetailId (tạm)

        for (CartItemResponseDTO cartItemResponse : promotionResponse.items()) {
            ProductUnit productUnit = productUnitRepository.findById(cartItemResponse.productUnitId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format("Không tìm thấy sản phẩm với ID: %d", cartItemResponse.productUnitId())));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProductUnit(productUnit);
            orderDetail.setQuantity(cartItemResponse.quantity());
            orderDetail.setPriceAtPurchase(cartItemResponse.unitPrice());

            // Tính discount: giá gốc - giá sau khuyến mãi
            BigDecimal originalTotal = cartItemResponse.unitPrice()
                    .multiply(BigDecimal.valueOf(cartItemResponse.quantity()));
            BigDecimal discount = originalTotal.subtract(cartItemResponse.lineTotal());
            orderDetail.setDiscount(discount);

            // Set thông tin khuyến mãi nếu có
            if (cartItemResponse.promotionApplied() != null) {
                PromotionAppliedDTO promotion = cartItemResponse.promotionApplied();
                orderDetail.setPromotionName(promotion.promotionName());
                orderDetail.setPromotionLineId(promotion.promotionLineId());
                orderDetail.setPromotionDetailId(promotion.promotionDetailId());
                orderDetail.setPromotionSummary(promotion.promotionSummary());
                orderDetail.setDiscountType(promotion.discountType());
                orderDetail.setDiscountValue(promotion.discountValue());

                // Set sourceLineItemId (cho item tặng)
                if (promotion.sourceLineItemId() != null) {
                    // Tìm OrderDetail tương ứng với sourceLineItemId
                    Long sourceOrderDetailId = lineItemIdToOrderDetailId.get(promotion.sourceLineItemId());
                    orderDetail.setSourceLineItemId(sourceOrderDetailId);
                }
            }

            orderDetails.add(orderDetail);

            // Lưu mapping lineItemId → index để xử lý sourceLineItemId sau
            lineItemIdToOrderDetailId.put(cartItemResponse.lineItemId(), (long) (orderDetails.size() - 1));
        }

        // Bước 5: Set thông tin khuyến mãi cho Order
        order.setSubtotal(promotionResponse.summary().subTotal());
        order.setOrderDiscount(promotionResponse.summary().orderDiscount());
        order.setLineItemDiscount(promotionResponse.summary().lineItemDiscount());

        // Serialize appliedOrderPromotions sang JSON
        if (promotionResponse.appliedOrderPromotions() != null
                && !promotionResponse.appliedOrderPromotions().isEmpty()) {
            try {
                order.setAppliedOrderPromotionsJson(
                        objectMapper.writeValueAsString(promotionResponse.appliedOrderPromotions()));
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi serialize appliedOrderPromotions sang JSON", e);
                // Không throw exception, chỉ log lỗi và tiếp tục
            }
        }

        // Tính totalAmount = totalPayable + shippingFee
        // (totalPayable đã bao gồm subTotal - lineItemDiscount - orderDiscount)
        order.setTotalAmount(
                promotionResponse.summary().totalPayable()
                        .add(order.getShippingFee()));

        // Set số tiền khách trả
        // Mặc định amountPaid = totalAmount (sẽ cập nhật sau khi thanh toán online)
        order.setAmountPaid(order.getTotalAmount());

        // Lưu đơn hàng
        order = orderRepository.save(order);

        // Lưu chi tiết đơn hàng
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(order);
            orderDetailRepository.save(detail);
        }

        // Cập nhật tồn kho ngay sau khi tạo đơn hàng
        updateInventory(orderDetails);

        // Xóa giỏ hàng sau khi checkout thành công
        cartItemRepository.deleteAll(cartItems);

        // Tạo payment link nếu thanh toán ONLINE
        String paymentUrl = null;
        String qrCode = null;

        if (order.getPaymentMethod() == PaymentMethod.ONLINE) {
            try {
                // Sử dụng orderId làm orderCode cho payment, thêm prefix 1 tỷ để phân biệt với Invoice
                // 1xxxxxxxxx: Order
                // 2xxxxxxxxx: Invoice
                Long paymentOrderCode = 1000000000L + order.getOrderId();

                // Chuyển đổi orderDetails sang PaymentItemData
                List<iuh.fit.supermarket.service.PaymentService.PaymentItemData> paymentItems = orderDetails.stream()
                        .map(detail -> new iuh.fit.supermarket.service.PaymentService.PaymentItemData(
                                detail.getProductUnit().getProduct().getName() + " - " +
                                detail.getProductUnit().getUnit().getName(),
                                detail.getQuantity(),
                                detail.getPriceAtPurchase().intValue()))
                        .toList();

                vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse paymentResponse = paymentService
                        .createPaymentLink(
                                paymentOrderCode,
                                order.getTotalAmount(),
                                "Thanh toan don hang #" + order.getOrderId(),
                                paymentItems);

                paymentUrl = paymentResponse.getCheckoutUrl();
                qrCode = paymentResponse.getQrCode();
                log.info("Đã tạo payment link cho đơn hàng {}: {}", order.getOrderId(), paymentUrl);
            } catch (Exception e) {
                log.error("Lỗi khi tạo payment link cho đơn hàng {}: {}", order.getOrderId(), e.getMessage(), e);
                // Không throw exception, vẫn trả về response nhưng không có payment link
            }
        }

        // Tạo response
        return buildCheckoutResponse(order, orderDetails, paymentUrl, qrCode);
    }
    @Override
    @Transactional
    @Deprecated
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {
        log.warn("Đang sử dụng phương thức checkout deprecated. Vui lòng sử dụng checkoutForCustomer thay thế.");

        // Phương thức này deprecated, không còn hỗ trợ
        throw new UnsupportedOperationException(
                "Phương thức này đã deprecated. Vui lòng sử dụng checkoutForCustomer với authentication.");
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Override
    @Transactional
    public CheckoutResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Cập nhật trạng thái đơn hàng {} sang {}", orderId, newStatus);

        // Ngăn chặn việc cập nhật thủ công sang COMPLETED
        if (newStatus == OrderStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không thể cập nhật thủ công sang trạng thái COMPLETED. " +
                            "Đơn hàng sẽ tự động hoàn thành sau khi được giao (DELIVERED).");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        OrderStatus currentStatus = order.getStatus();
        DeliveryType deliveryType = order.getDeliveryType();

        // Validate chuyển trạng thái hợp lệ với loại giao hàng
        if (!statusTransitionValidator.isValidTransition(currentStatus, newStatus, deliveryType)) {
            String errorMessage = statusTransitionValidator.getTransitionErrorMessage(
                    currentStatus, newStatus, deliveryType);
            throw new BadRequestException(errorMessage);
        }

        // Kiểm tra trạng thái có phù hợp với loại giao hàng không
        if (!statusTransitionValidator.isStatusValidForDeliveryType(newStatus, deliveryType)) {
            throw new BadRequestException(
                    String.format("Trạng thái %s không phù hợp với loại giao hàng %s",
                            newStatus, deliveryType));
        }

        order.setStatus(newStatus);

        // Nếu chuyển sang DELIVERED -> Tạo hóa đơn bán hàng và tự động hoàn thành
        if (newStatus == OrderStatus.DELIVERED) {
            createSalesInvoice(order);

            // Cập nhật số lượng sử dụng khuyến mãi
            updatePromotionUsageCount(order);

            // Tự động chuyển sang trạng thái COMPLETED
            log.info("Tự động chuyển đơn hàng {} từ DELIVERED sang COMPLETED", orderId);
            order.setStatus(OrderStatus.COMPLETED);
            order = orderRepository.save(order);

            log.info("Đơn hàng {} đã được tự động hoàn thành", orderId);
            return buildCheckoutResponse(order, order.getOrderDetails());
        }

        // Nếu chuyển sang CANCELLED -> Hoàn lại tồn kho
        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventory(order.getOrderDetails());
        }

        order = orderRepository.save(order);

        log.info("Đã cập nhật trạng thái đơn hàng {} từ {} sang {}",
                orderId, currentStatus, newStatus);

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Lấy thông tin chi tiết đơn hàng của khách hàng
     */
    @Override
    @Transactional(readOnly = true)
    public CheckoutResponseDTO getOrderDetailForCustomer(String username, Long orderId) {
        log.info("Khách hàng {} lấy thông tin chi tiết đơn hàng ID: {}", username, orderId);

        // Bỏ prefix nếu có
        String actualUsername = extractActualUsername(username);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền xem đơn hàng
        User user = userRepository.findByEmailOrPhone(actualUsername, actualUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        if (order.getCustomer() == null ||
                !order.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            throw new UnauthorizedException("Bạn không có quyền xem đơn hàng này");
        }

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Lấy thông tin chi tiết đơn hàng
     */
    @Override
    @Transactional(readOnly = true)
    public CheckoutResponseDTO getOrderDetail(Long orderId) {
        log.info("Lấy thông tin chi tiết đơn hàng ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Hủy đơn hàng của khách hàng
     */
    @Override
    @Transactional
    public CheckoutResponseDTO cancelOrderForCustomer(String username, Long orderId, String reason) {
        log.info("Khách hàng {} hủy đơn hàng ID: {} với lý do: {}", username, orderId, reason);

        // Bỏ prefix nếu có
        String actualUsername = extractActualUsername(username);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền hủy đơn hàng
        User user = userRepository.findByEmailOrPhone(actualUsername, actualUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        if (order.getCustomer() == null ||
                !order.getCustomer().getCustomerId().equals(user.getCustomer().getCustomerId())) {
            throw new UnauthorizedException("Bạn không có quyền hủy đơn hàng này");
        }

        // Chỉ cho phép hủy đơn hàng ở trạng thái PENDING hoặc PREPARED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARED) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNote(order.getNote() != null ? order.getNote() + " | Lý do hủy: " + reason : "Lý do hủy: " + reason);

        // Hoàn lại tồn kho
        restoreInventory(order.getOrderDetails());

        order = orderRepository.save(order);

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Hủy đơn hàng
     */
    @Override
    @Transactional
    public CheckoutResponseDTO cancelOrder(Long orderId, String reason) {
        log.info("Hủy đơn hàng ID: {} với lý do: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Không cho phép hủy đơn hàng ở trạng thái COMPLETED hoặc CANCELLED
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNote(order.getNote() != null ? order.getNote() + " | Lý do hủy: " + reason : "Lý do hủy: " + reason);

        // Hoàn lại tồn kho
        restoreInventory(order.getOrderDetails());

        order = orderRepository.save(order);

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Xác nhận thanh toán online thành công
     * Webhook sẽ gọi method này khi thanh toán thành công
     */
    @Override
    @Transactional
    public CheckoutResponseDTO confirmOnlinePayment(Long orderId, String transactionId) {
        log.info("Xác nhận thanh toán online cho đơn hàng ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        if (order.getPaymentMethod() != PaymentMethod.ONLINE && order.getPaymentMethod() != PaymentMethod.CARD) {
            throw new BadRequestException("Đơn hàng không phải thanh toán online/card");
        }

        // Kiểm tra đã thanh toán chưa (trạng thái không phải UNPAID)
        if (order.getStatus() != OrderStatus.UNPAID) {
            log.warn("Đơn hàng {} đã được xử lý rồi (trạng thái: {})", orderId, order.getStatus());
            return buildCheckoutResponse(order, order.getOrderDetails());
        }

        // Cập nhật trạng thái từ UNPAID sang PENDING khi thanh toán thành công
        order.setStatus(OrderStatus.PENDING);
        order.setTransactionId(transactionId);

        // Cập nhật số tiền đã thanh toán
        order.setAmountPaid(order.getTotalAmount());

        // Thêm thông tin giao dịch vào note
        order.setNote(order.getNote() != null ? order.getNote() + " | Transaction ID: " + transactionId
                : "Transaction ID: " + transactionId);

        order = orderRepository.save(order);
        log.info("Đã xác nhận thanh toán cho đơn hàng {}, chuyển sang trạng thái PENDING, transactionId: {}",
                orderId, transactionId);

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Tính phí vận chuyển dựa trên địa chỉ giao hàng
     */
    @Override
    public BigDecimal calculateShippingFee(String deliveryAddress) {
        // Logic tính phí vận chuyển có thể phức tạp hơn
        // Ví dụ: dựa trên khoảng cách, khu vực, trọng lượng...
        // Đây là implementation đơn giản

        if (deliveryAddress == null || deliveryAddress.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Phí vận chuyển cố định 0 VND
        return new BigDecimal("0");
    }

    /**
     * Bỏ prefix "CUSTOMER:" hoặc "EMPLOYEE:" từ username
     */
    private String extractActualUsername(String username) {
        if (username.startsWith("CUSTOMER:")) {
            return username.substring("CUSTOMER:".length());
        } else if (username.startsWith("EMPLOYEE:")) {
            return username.substring("EMPLOYEE:".length());
        }
        return username;
    }

    /**
     * Cập nhật tồn kho sau khi đặt hàng
     * Sử dụng WarehouseService để trừ kho và ghi lại lịch sử giao dịch
     */
    private void updateInventory(List<OrderDetail> orderDetails) {
        Long orderId = orderDetails.isEmpty() ? null : orderDetails.get(0).getOrder().getOrderId();
        String referenceId = orderId != null ? "ORDER-" + orderId : null;

        for (OrderDetail detail : orderDetails) {
            ProductUnit productUnit = detail.getProductUnit();

            try {
                // Sử dụng WarehouseService để xuất kho và ghi lại lịch sử
                // quantityChange = -detail.getQuantity() (số âm để xuất kho)
                warehouseService.stockOut(
                        productUnit.getId(),
                        detail.getQuantity(),
                        referenceId,
                        String.format("Xuất kho cho đơn hàng #%s - %s",
                                orderId,
                                productUnit.getProduct().getName()));

                log.info("Đã trừ {} {} {} khỏi kho cho đơn hàng #{}",
                        detail.getQuantity(),
                        productUnit.getUnit().getName(),
                        productUnit.getProduct().getName(),
                        orderId);

            } catch (Exception e) {
                log.error("Lỗi khi trừ kho cho sản phẩm {}: {}",
                        productUnit.getProduct().getName(), e.getMessage());
                throw new BadRequestException(
                        String.format("Không thể trừ kho cho sản phẩm %s: %s",
                                productUnit.getProduct().getName(), e.getMessage()));
            }
        }
    }

    /**
     * Hoàn lại tồn kho khi hủy đơn hàng
     * Sử dụng WarehouseService để hoàn kho và ghi lại lịch sử giao dịch với
     * TransactionType.RETURN
     */
    private void restoreInventory(List<OrderDetail> orderDetails) {
        Long orderId = orderDetails.isEmpty() ? null : orderDetails.get(0).getOrder().getOrderId();
        String referenceId = orderId != null ? "ORDER-" + orderId : null;

        for (OrderDetail detail : orderDetails) {
            ProductUnit productUnit = detail.getProductUnit();

            try {
                // Sử dụng WarehouseService.updateStock với TransactionType.RETURN để hoàn kho
                // (không dùng stockIn vì nó tạo transaction với type STOCK_IN)
                warehouseService.updateStock(
                        productUnit.getId(),
                        detail.getQuantity(), // số dương để tăng tồn kho
                        WarehouseTransaction.TransactionType.RETURN,
                        referenceId,
                        String.format("Hoàn kho từ đơn hàng hủy #%s - %s",
                                orderId,
                                productUnit.getProduct().getName()));

                log.info("Đã hoàn {} {} {} vào kho từ đơn hàng hủy #{}",
                        detail.getQuantity(),
                        productUnit.getUnit().getName(),
                        productUnit.getProduct().getName(),
                        orderId);

            } catch (Exception e) {
                log.error("Lỗi khi hoàn kho cho sản phẩm {}: {}",
                        productUnit.getProduct().getName(), e.getMessage());
                throw new BadRequestException(
                        String.format("Không thể hoàn kho cho sản phẩm %s: %s",
                                productUnit.getProduct().getName(), e.getMessage()));
            }
        }
    }

    /**
     * Tạo hóa đơn bán hàng khi đơn hàng được giao
     */
    private void createSalesInvoice(Order order) {
        log.info("Bắt đầu tạo hóa đơn bán hàng cho đơn hàng ID: {}", order.getOrderId());

        // Kiểm tra xem đơn hàng đã có hóa đơn chưa
        if (saleInvoiceHeaderRepository.existsByOrderId(order.getOrderId())) {
            log.warn("Đơn hàng {} đã có hóa đơn, bỏ qua việc tạo hóa đơn mới", order.getOrderId());
            return;
        }

        // Tạo SaleInvoiceHeader
        SaleInvoiceHeader invoice = new SaleInvoiceHeader();

        // Tạo số hóa đơn tự động
        String invoiceNumber = generateInvoiceNumber();
        invoice.setInvoiceNumber(invoiceNumber);

        // Set thông tin cơ bản
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setOrder(order);
        invoice.setCustomer(order.getCustomer());

        // Set nhân viên - nếu đơn hàng không có nhân viên thì lấy nhân viên hệ thống
        // (ID = 1)
        if (order.getEmployee() != null) {
            invoice.setEmployee(order.getEmployee());
        } else {
            // Lấy nhân viên hệ thống mặc định (thường là admin với ID = 1)
            Employee systemEmployee = employeeRepository.findById(1)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy nhân viên hệ thống"));
            invoice.setEmployee(systemEmployee);
        }

        // Set thông tin thanh toán
        invoice.setPaymentMethod(order.getPaymentMethod());

        // Tính toán tổng tiền
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // Tạo danh sách chi tiết hóa đơn
        List<SaleInvoiceDetail> invoiceDetails = new ArrayList<>();

        for (OrderDetail orderDetail : order.getOrderDetails()) {
            SaleInvoiceDetail detail = new SaleInvoiceDetail();
            detail.setInvoice(invoice);
            detail.setProductUnit(orderDetail.getProductUnit());
            detail.setQuantity(orderDetail.getQuantity());
            detail.setUnitPrice(orderDetail.getPriceAtPurchase());
            detail.setDiscountAmount(orderDetail.getDiscount());

            // Tính thành tiền trước thuế
            BigDecimal lineTotal = orderDetail.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                    .subtract(orderDetail.getDiscount());
            detail.setLineTotal(lineTotal);

            // Tính thuế (mặc định VAT 10%)
            BigDecimal taxAmount = lineTotal.multiply(BigDecimal.valueOf(0.10));
            detail.setTaxType(TaxType.VAT_10);
            detail.setTaxAmount(taxAmount);

            // Thành tiền sau thuế
            BigDecimal lineTotalWithTax = lineTotal.add(taxAmount);
            detail.setLineTotalWithTax(lineTotalWithTax);

            invoiceDetails.add(detail);

            // Cộng dồn vào tổng
            subtotal = subtotal.add(lineTotal);
            totalDiscount = totalDiscount.add(orderDetail.getDiscount());
            totalTax = totalTax.add(taxAmount);
        }

        // Set tổng tiền cho hóa đơn
        invoice.setSubtotal(subtotal);
        invoice.setTotalDiscount(totalDiscount);
        invoice.setTotalTax(totalTax);

        // Tổng tiền cuối cùng
        BigDecimal totalAmount = subtotal.add(totalTax);
        invoice.setTotalAmount(totalAmount);

        // Set trạng thái hóa đơn dựa trên trạng thái đơn hàng
        // Nếu đơn hàng đã thanh toán online (có transactionId) thì đặt hóa đơn là PAID
        if (order.getTransactionId() != null && !order.getTransactionId().isEmpty()) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAmount(totalAmount);
        } else if (order.getPaymentMethod() == PaymentMethod.CASH) {
            // Thanh toán tiền mặt, đánh dấu là đã thanh toán khi giao hàng
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAmount(totalAmount);
        } else {
            invoice.setStatus(InvoiceStatus.UNPAID);
            invoice.setPaidAmount(BigDecimal.ZERO);
        }

        // Lưu hóa đơn
        invoice = saleInvoiceHeaderRepository.save(invoice);

        // Lưu chi tiết hóa đơn và thông tin khuyến mãi
        int detailIndex = 0;
        for (SaleInvoiceDetail detail : invoiceDetails) {
            detail.setInvoice(invoice);
            SaleInvoiceDetail savedDetail = saleInvoiceDetailRepository.save(detail);

            // Lưu thông tin khuyến mãi từ OrderDetail (nếu có)
            if (detailIndex < order.getOrderDetails().size()) {
                OrderDetail orderDetail = order.getOrderDetails().get(detailIndex);

                if (orderDetail.getPromotionLineId() != null) {
                    AppliedPromotion appliedPromotion = new AppliedPromotion();
                    appliedPromotion.setInvoiceDetail(savedDetail);
                    appliedPromotion.setPromotionName(orderDetail.getPromotionName());
                    appliedPromotion.setPromotionLineId(orderDetail.getPromotionLineId());
                    appliedPromotion.setPromotionDetailId(orderDetail.getPromotionDetailId());
                    appliedPromotion.setPromotionSummary(orderDetail.getPromotionSummary());
                    appliedPromotion.setDiscountType(orderDetail.getDiscountType());
                    appliedPromotion.setDiscountValue(orderDetail.getDiscountValue());

                    appliedPromotionRepository.save(appliedPromotion);
                    log.debug("Đã lưu thông tin khuyến mãi {} cho chi tiết hóa đơn",
                            orderDetail.getPromotionName());
                }
            }
            detailIndex++;
        }

        // Lưu thông tin khuyến mãi toàn đơn từ Order (nếu có)
        if (order.getAppliedOrderPromotionsJson() != null && !order.getAppliedOrderPromotionsJson().isEmpty()) {
            try {
                // Parse JSON thành danh sách OrderPromotionDTO
                List<OrderPromotionDTO> orderPromotions = objectMapper.readValue(
                        order.getAppliedOrderPromotionsJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OrderPromotionDTO.class));

                for (OrderPromotionDTO promotionDTO : orderPromotions) {
                    AppliedOrderPromotion appliedOrderPromotion = new AppliedOrderPromotion();
                    appliedOrderPromotion.setInvoice(invoice);
                    appliedOrderPromotion.setPromotionId(promotionDTO.promotionId());
                    appliedOrderPromotion.setPromotionName(promotionDTO.promotionName());
                    appliedOrderPromotion.setPromotionDetailId(promotionDTO.promotionDetailId());
                    appliedOrderPromotion.setPromotionSummary(promotionDTO.promotionSummary());
                    appliedOrderPromotion.setDiscountType(promotionDTO.discountType());
                    appliedOrderPromotion.setDiscountValue(promotionDTO.discountValue());

                    appliedOrderPromotionRepository.save(appliedOrderPromotion);
                    log.debug("Đã lưu khuyến mãi toàn đơn: {}", promotionDTO.promotionName());
                }
            } catch (Exception e) {
                log.error("Lỗi khi parse thông tin khuyến mãi toàn đơn: {}", e.getMessage());
            }
        }

        log.info("Đã tạo hóa đơn số {} cho đơn hàng {}, tổng tiền: {}",
                invoiceNumber, order.getOrderId(), totalAmount);
    }

    /**
     * Tạo số hóa đơn tự động
     * Format: INV + năm + tháng + số thứ tự (5 chữ số)
     * Ví dụ: INV20251100001
     */
    private String generateInvoiceNumber() {
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

        // Tìm số hóa đơn cuối cùng trong tháng
        List<String> lastInvoiceNumbers = saleInvoiceHeaderRepository
                .findLastInvoiceNumberByMonth(yearMonth);

        int nextNumber = 1;
        if (!lastInvoiceNumbers.isEmpty()) {
            String lastInvoiceNumber = lastInvoiceNumbers.get(0);
            // Lấy 5 chữ số cuối
            String lastNumberStr = lastInvoiceNumber.substring(lastInvoiceNumber.length() - 5);
            nextNumber = Integer.parseInt(lastNumberStr) + 1;
        }

        return String.format("INV%s%05d", yearMonth, nextNumber);
    }

    /**
     * Tạo mã đơn hàng tự động
     * Format: ORD + năm + tháng + ngày + số thứ tự (5 chữ số)
     * Ví dụ: ORD2025111100001
     */
    private String generateOrderCode() {
        String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Tìm mã đơn hàng cuối cùng trong ngày
        org.springframework.data.domain.Page<String> lastOrderCodes = orderRepository
                .findLastOrderCodeByDate(datePattern, org.springframework.data.domain.PageRequest.of(0, 1));

        int nextNumber = 1;
        if (lastOrderCodes.hasContent()) {
            String lastOrderCode = lastOrderCodes.getContent().get(0);
            // Lấy 5 chữ số cuối
            String lastNumberStr = lastOrderCode.substring(lastOrderCode.length() - 5);
            nextNumber = Integer.parseInt(lastNumberStr) + 1;
        }

        return String.format("ORD%s%05d", datePattern, nextNumber);
    }

    /**
     * Xây dựng response cho checkout
     */
    private CheckoutResponseDTO buildCheckoutResponse(Order order, List<OrderDetail> orderDetails) {
        return buildCheckoutResponse(order, orderDetails, null, null);
    }

    /**
     * Xây dựng response cho checkout với thông tin thanh toán online
     */
    private CheckoutResponseDTO buildCheckoutResponse(Order order, List<OrderDetail> orderDetails,
            String paymentUrl, String qrCode) {
        // Build customer info
        CustomerInfoDTO customerInfo = null;
        if (order.getCustomer() != null) {
            Customer customer = order.getCustomer();
            User user = customer.getUser();
            customerInfo = new CustomerInfoDTO(
                    customer.getCustomerId(),
                    user.getName(),
                    user.getPhone(),
                    user.getEmail(),
                    0 // TODO: Lấy điểm tích lũy thực tế
            );
        }

        // Build delivery info - chỉ lưu địa chỉ đơn giản
        DeliveryInfoDTO deliveryInfo = null;
        if (order.getDeliveryType() == DeliveryType.HOME_DELIVERY && order.getDeliveryAddress() != null) {
            deliveryInfo = new DeliveryInfoDTO(
                    null, // recipientName - không dùng nữa
                    null, // deliveryPhone - không dùng nữa
                    order.getDeliveryAddress(),
                    null // deliveryNote - không dùng nữa
            );
        }

        // Build order items
        List<OrderItemDTO> orderItems = orderDetails.stream()
                .map(detail -> {
                    // Xây dựng thông tin khuyến mãi cho từng item
                    String promotionInfo = null;
                    if (detail.getPromotionName() != null) {
                        promotionInfo = detail.getPromotionName();
                        if (detail.getPromotionSummary() != null) {
                            promotionInfo += " - " + detail.getPromotionSummary();
                        }
                    }

                    // Lấy URL hình ảnh chính của ProductUnit
                    String imageUrl = null;
                    ProductUnit productUnit = detail.getProductUnit();
                    if (productUnit.getProductUnitImages() != null && !productUnit.getProductUnitImages().isEmpty()) {
                        // Tìm hình ảnh chính (isPrimary = true)
                        imageUrl = productUnit.getProductUnitImages().stream()
                                .filter(pui -> pui.getIsPrimary() != null && pui.getIsPrimary() && pui.getIsActive())
                                .findFirst()
                                .map(pui -> pui.getProductImage().getImageUrl())
                                .orElse(null);

                        // Nếu không có hình ảnh chính, lấy hình ảnh đầu tiên
                        if (imageUrl == null) {
                            imageUrl = productUnit.getProductUnitImages().stream()
                                    .filter(pui -> pui.getIsActive())
                                    .findFirst()
                                    .map(pui -> pui.getProductImage().getImageUrl())
                                    .orElse(null);
                        }
                    }

                    return new OrderItemDTO(
                            detail.getProductUnit().getId(),
                            detail.getProductUnit().getProduct().getName(),
                            detail.getProductUnit().getUnit().getName(),
                            detail.getProductUnit().getBarcode(),
                            detail.getQuantity(),
                            detail.getPriceAtPurchase(),
                            detail.getPriceAtPurchase().subtract(detail.getDiscount()),
                            detail.getDiscount(),
                            detail.getLineTotal(),
                            promotionInfo,
                            imageUrl);
                })
                .collect(Collectors.toList());

        // Build online payment info (if applicable)
        OnlinePaymentInfoDTO onlinePaymentInfo = null;
        if (order.getPaymentMethod() == PaymentMethod.ONLINE || order.getPaymentMethod() == PaymentMethod.CARD) {
            // Xác định trạng thái thanh toán dựa trên transactionId và orderStatus
            String paymentStatus = "UNPAID"; // Mặc định
            if (order.getTransactionId() != null && !order.getTransactionId().isEmpty()) {
                paymentStatus = "PAID"; // Đã có transactionId nghĩa là đã thanh toán
            } else if (order.getStatus() == OrderStatus.CANCELLED) {
                paymentStatus = "FAILED"; // Đơn hàng bị hủy
            }

            onlinePaymentInfo = new OnlinePaymentInfoDTO(
                    order.getTransactionId(), // transactionId từ webhook
                    "PayOS", // payment provider
                    paymentStatus, // Trạng thái thanh toán
                    paymentUrl, // URL thanh toán
                    qrCode, // QR code
                    LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // expiration time
            );
        }

        // Calculate change amount - không còn tính changeAmount vì không có amountPaid
        // trong request
        BigDecimal changeAmount = BigDecimal.ZERO;

        // Build danh sách khuyến mãi đã áp dụng
        List<PromotionAppliedDTO> appliedPromotions = new ArrayList<>();

        // Lấy khuyến mãi từ OrderDetail (PRODUCT_DISCOUNT, BUY_X_GET_Y)
        for (OrderDetail detail : orderDetails) {
            if (detail.getPromotionLineId() != null) {
                appliedPromotions.add(new PromotionAppliedDTO(
                        detail.getPromotionName(),
                        detail.getPromotionLineId(),
                        detail.getPromotionDetailId(),
                        detail.getPromotionSummary(),
                        detail.getDiscountType(),
                        detail.getDiscountValue(),
                        detail.getSourceLineItemId()));
            }
        }

        // Lấy khuyến mãi toàn đơn từ JSON (ORDER_DISCOUNT)
        if (order.getAppliedOrderPromotionsJson() != null && !order.getAppliedOrderPromotionsJson().isEmpty()) {
            try {
                List<CheckPromotionResponseDTO.OrderPromotionDTO> orderPromotions = objectMapper.readValue(
                        order.getAppliedOrderPromotionsJson(),
                        objectMapper.getTypeFactory().constructCollectionType(
                                List.class,
                                CheckPromotionResponseDTO.OrderPromotionDTO.class));

                // Convert sang PromotionAppliedDTO
                for (CheckPromotionResponseDTO.OrderPromotionDTO orderPromotion : orderPromotions) {
                    appliedPromotions.add(new PromotionAppliedDTO(
                            orderPromotion.promotionName(),
                            orderPromotion.promotionLineId(),
                            orderPromotion.promotionDetailId(),
                            orderPromotion.promotionSummary(),
                            orderPromotion.discountType(),
                            orderPromotion.discountValue(),
                            null // ORDER_DISCOUNT không có sourceLineItemId
                    ));
                }
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi deserialize appliedOrderPromotionsJson", e);
                // Không throw exception, chỉ log lỗi
            }
        }

        // Tính tổng giảm giá
        BigDecimal totalDiscount = order.getLineItemDiscount().add(order.getOrderDiscount());

        return new CheckoutResponseDTO(
                order.getOrderId(),
                order.getOrderCode(), // Sử dụng orderCode từ database
                order.getStatus(),
                order.getDeliveryType(),
                order.getPaymentMethod(),
                order.getTransactionId(),
                customerInfo,
                deliveryInfo,
                orderItems,
                order.getSubtotal(),
                totalDiscount,
                order.getShippingFee(),
                0, // loyaltyPointsUsed
                BigDecimal.ZERO, // loyaltyPointsDiscount
                order.getTotalAmount(),
                order.getAmountPaid(),
                changeAmount,
                onlinePaymentInfo,
                appliedPromotions,
                0, // TODO: Điểm tích lũy nhận được
                order.getCreatedAt(),
                "Đặt hàng thành công");
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng với khả năng lọc theo trạng thái và
     * phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CheckoutResponseDTO> getCustomerOrders(
            String username,
            OrderStatus status,
            org.springframework.data.domain.Pageable pageable) {

        log.info("Lấy danh sách đơn hàng cho khách hàng: {}, trạng thái: {}, page: {}, size: {}",
                username, status, pageable.getPageNumber(), pageable.getPageSize());

        // Bỏ prefix nếu có
        String actualUsername = extractActualUsername(username);

        // Lấy danh sách đơn hàng với phân trang
        org.springframework.data.domain.Page<Order> orderPage;
        if (status == null) {
            orderPage = orderRepository.findByCustomerUsername(actualUsername, pageable);
        } else {
            orderPage = orderRepository.findByCustomerUsernameAndStatus(actualUsername, status, pageable);
        }

        // Convert sang DTO
        return orderPage.map(order -> {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
            return buildCheckoutResponse(order, orderDetails);
        });
    }

    /**
     * Lấy danh sách tất cả đơn hàng trong hệ thống (dành cho Admin)
     * Có khả năng lọc theo trạng thái và phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CheckoutResponseDTO> getAllOrders(
            OrderStatus status,
            org.springframework.data.domain.Pageable pageable) {
        // Gọi method overload với deliveryType = null
        return getAllOrders(status, null, pageable);
    }

    /**
     * Lấy danh sách tất cả đơn hàng trong hệ thống với lọc theo loại hình nhận hàng
     * (dành cho Admin)
     * Có khả năng lọc theo trạng thái, loại hình nhận hàng và phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<CheckoutResponseDTO> getAllOrders(
            OrderStatus status,
            DeliveryType deliveryType,
            org.springframework.data.domain.Pageable pageable) {

        log.info("Admin lấy danh sách tất cả đơn hàng, trạng thái: {}, loại hình nhận hàng: {}, page: {}, size: {}",
                status, deliveryType, pageable.getPageNumber(), pageable.getPageSize());

        // Lấy danh sách tất cả đơn hàng với phân trang
        org.springframework.data.domain.Page<Order> orderPage;

        // Xác định cách lấy dữ liệu dựa trên các tham số
        if (status == null && deliveryType == null) {
            // Lấy tất cả đơn hàng trừ UNPAID (chưa thanh toán)
            orderPage = orderRepository.findByStatusNot(OrderStatus.UNPAID, pageable);
        } else if (status != null && deliveryType == null) {
            // Lọc theo trạng thái
            orderPage = orderRepository.findByStatus(status, pageable);
        } else if (status == null && deliveryType != null) {
            // Lọc theo loại hình nhận hàng, loại trừ UNPAID
            orderPage = orderRepository.findByStatusNotAndDeliveryType(OrderStatus.UNPAID, deliveryType, pageable);
        } else {
            // Lọc theo cả trạng thái và loại hình nhận hàng
            orderPage = orderRepository.findByStatusAndDeliveryType(status, deliveryType, pageable);
        }

        log.info("Tìm thấy {} đơn hàng, tổng số trang: {}",
                orderPage.getNumberOfElements(), orderPage.getTotalPages());

        // Convert sang DTO
        return orderPage.map(order -> {
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
            return buildCheckoutResponse(order, orderDetails);
        });
    }

    /**
     * Cập nhật số lượng sử dụng khuyến mãi khi đơn hàng thành công
     *
     * @param order Đơn hàng đã hoàn thành
     */
    private void updatePromotionUsageCount(Order order) {
        log.info("Cập nhật usage count cho các khuyến mãi của đơn hàng {}", order.getOrderId());

        Set<Long> processedDetailIds = new HashSet<>();

        // Cập nhật usageCount cho khuyến mãi từ OrderDetails (PRODUCT_DISCOUNT,
        // BUY_X_GET_Y)
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
                                CheckPromotionResponseDTO.OrderPromotionDTO.class));

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
}