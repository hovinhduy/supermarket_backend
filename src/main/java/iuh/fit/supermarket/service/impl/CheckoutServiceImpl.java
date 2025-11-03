package iuh.fit.supermarket.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.checkout.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.*;
import iuh.fit.supermarket.enums.PaymentStatus;
import iuh.fit.supermarket.exception.BadRequestException;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.CheckoutService;
import iuh.fit.supermarket.service.PromotionCheckService;
import iuh.fit.supermarket.service.SaleService;
import iuh.fit.supermarket.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setEmployee(null); // Không có nhân viên vì khách hàng tự checkout
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryType(request.deliveryType());
        order.setPaymentMethod(request.paymentMethod());
        order.setNote(request.orderNote());
        
        // Set trạng thái thanh toán
        // - CASH: mặc định UNPAID, sẽ chuyển thành PAID khi giao hàng
        // - ONLINE/CARD: UNPAID, chờ webhook xác nhận thanh toán
        order.setPaymentStatus(PaymentStatus.UNPAID);

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
                        String.format("Không tìm thấy sản phẩm với ID: %d", cartItemResponse.productUnitId())
                    ));

            Warehouse warehouse = warehouseRepository.findByProductUnit(productUnit)
                    .orElseThrow(() -> new NotFoundException(
                        String.format("Không tìm thấy thông tin kho cho sản phẩm %s",
                        productUnit.getProduct().getName())
                    ));

            if (warehouse.getQuantityOnHand() < cartItemResponse.quantity()) {
                throw new BadRequestException(
                    String.format("Sản phẩm %s không đủ hàng trong kho (còn %d, yêu cầu %d)",
                    productUnit.getProduct().getName(),
                    warehouse.getQuantityOnHand(),
                    cartItemResponse.quantity())
                );
            }
        }

        // Bước 4: Map từ CheckPromotionResponseDTO sang OrderDetail
        List<OrderDetail> orderDetails = new ArrayList<>();
        Map<Long, Long> lineItemIdToOrderDetailId = new HashMap<>(); // Map lineItemId → OrderDetailId (tạm)

        for (CartItemResponseDTO cartItemResponse : promotionResponse.items()) {
            ProductUnit productUnit = productUnitRepository.findById(cartItemResponse.productUnitId())
                    .orElseThrow(() -> new NotFoundException(
                        String.format("Không tìm thấy sản phẩm với ID: %d", cartItemResponse.productUnitId())
                    ));

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProductUnit(productUnit);
            orderDetail.setQuantity(cartItemResponse.quantity());
            orderDetail.setPriceAtPurchase(cartItemResponse.unitPrice());

            // Tính discount: giá gốc - giá sau khuyến mãi
            BigDecimal originalTotal = cartItemResponse.unitPrice().multiply(BigDecimal.valueOf(cartItemResponse.quantity()));
            BigDecimal discount = originalTotal.subtract(cartItemResponse.lineTotal());
            orderDetail.setDiscount(discount);

            // Set thông tin khuyến mãi nếu có
            if (cartItemResponse.promotionApplied() != null) {
                PromotionAppliedDTO promotion = cartItemResponse.promotionApplied();
                orderDetail.setPromotionId(promotion.promotionId());
                orderDetail.setPromotionName(promotion.promotionName());
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
        if (promotionResponse.appliedOrderPromotions() != null && !promotionResponse.appliedOrderPromotions().isEmpty()) {
            try {
                order.setAppliedOrderPromotionsJson(objectMapper.writeValueAsString(promotionResponse.appliedOrderPromotions()));
            } catch (JsonProcessingException e) {
                log.error("Lỗi khi serialize appliedOrderPromotions sang JSON", e);
                // Không throw exception, chỉ log lỗi và tiếp tục
            }
        }

        // Tính totalAmount = subTotal - lineItemDiscount - orderDiscount + shippingFee
        order.setTotalAmount(
                promotionResponse.summary().subTotal()
                        .subtract(promotionResponse.summary().lineItemDiscount())
                        .subtract(promotionResponse.summary().orderDiscount())
                        .add(order.getShippingFee())
        );

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
                // Sử dụng orderId làm orderCode cho payment
                Long paymentOrderCode = order.getOrderId();

                // Chuyển đổi orderDetails sang PaymentItemData
                List<iuh.fit.supermarket.service.PaymentService.PaymentItemData> paymentItems = orderDetails.stream()
                        .map(detail -> new iuh.fit.supermarket.service.PaymentService.PaymentItemData(
                                detail.getProductUnit().getProduct().getName() + " - " + 
                                    detail.getProductUnit().getUnit().getName(),
                                detail.getQuantity(),
                                detail.getPriceAtPurchase().intValue()))
                        .toList();

                vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse paymentResponse = 
                    paymentService.createPaymentLink(
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

    /**
     * Thực hiện checkout giỏ hàng và tạo đơn hàng (deprecated)
     */
    @Override
    @Transactional
    @Deprecated
    public CheckoutResponseDTO checkout(CheckoutRequestDTO request) {
        log.warn("Đang sử dụng phương thức checkout deprecated. Vui lòng sử dụng checkoutForCustomer thay thế.");

        // Phương thức này deprecated, không còn hỗ trợ
        throw new UnsupportedOperationException(
            "Phương thức này đã deprecated. Vui lòng sử dụng checkoutForCustomer với authentication."
        );
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @Override
    @Transactional
    public CheckoutResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Cập nhật trạng thái đơn hàng {} sang {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        OrderStatus currentStatus = order.getStatus();

        // Validate chuyển trạng thái hợp lệ
        validateStatusTransition(currentStatus, newStatus);

        order.setStatus(newStatus);

        // Nếu chuyển sang DELIVERED -> Tạo hóa đơn bán hàng
        if (newStatus == OrderStatus.DELIVERED) {
            createSalesInvoice(order);
        }

        // Nếu chuyển sang CANCELLED -> Hoàn lại tồn kho
        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventory(order.getOrderDetails());
        }

        order = orderRepository.save(order);

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
        order.setNote(order.getNote() != null ?
                order.getNote() + " | Lý do hủy: " + reason :
                "Lý do hủy: " + reason);

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

        // Chỉ cho phép hủy đơn hàng ở trạng thái PENDING hoặc PREPARED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PREPARED) {
            throw new BadRequestException("Không thể hủy đơn hàng ở trạng thái: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNote(order.getNote() != null ?
                order.getNote() + " | Lý do hủy: " + reason :
                "Lý do hủy: " + reason);

        // Hoàn lại tồn kho
        restoreInventory(order.getOrderDetails());

        order = orderRepository.save(order);

        return buildCheckoutResponse(order, order.getOrderDetails());
    }

    /**
     * Xác nhận thanh toán online thành công
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

        // Kiểm tra đã thanh toán chưa
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.warn("Đơn hàng {} đã được thanh toán rồi", orderId);
            return buildCheckoutResponse(order, order.getOrderDetails());
        }

        // Cập nhật trạng thái thanh toán
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setTransactionId(transactionId);
        
        // Cập nhật số tiền đã thanh toán
        order.setAmountPaid(order.getTotalAmount());

        // Thêm thông tin giao dịch vào note
        order.setNote(order.getNote() != null ?
                order.getNote() + " | Transaction ID: " + transactionId :
                "Transaction ID: " + transactionId);

        order = orderRepository.save(order);
        log.info("Đã xác nhận thanh toán cho đơn hàng {}, transactionId: {}", orderId, transactionId);

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

        // Phí vận chuyển cố định 30,000 VND
        return new BigDecimal("30000");
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
     * Validate chuyển trạng thái đơn hàng
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValid = false;

        switch (currentStatus) {
            case PENDING:
                isValid = (newStatus == OrderStatus.PREPARED ||
                          newStatus == OrderStatus.CANCELLED);
                break;
            case PREPARED:
                isValid = (newStatus == OrderStatus.SHIPPING ||
                          newStatus == OrderStatus.DELIVERED ||  // Nhận tại cửa hàng
                          newStatus == OrderStatus.CANCELLED);
                break;
            case SHIPPING:
                isValid = (newStatus == OrderStatus.DELIVERED);
                break;
            case DELIVERED:
                isValid = (newStatus == OrderStatus.COMPLETED);
                break;
            default:
                isValid = false;
        }

        if (!isValid) {
            throw new BadRequestException(
                String.format("Không thể chuyển từ trạng thái %s sang %s",
                currentStatus, newStatus)
            );
        }
    }

    /**
     * Cập nhật tồn kho sau khi đặt hàng
     */
    private void updateInventory(List<OrderDetail> orderDetails) {
        for (OrderDetail detail : orderDetails) {
            ProductUnit productUnit = detail.getProductUnit();

            Warehouse warehouse = warehouseRepository.findByProductUnit(productUnit)
                    .orElseThrow(() -> new NotFoundException(
                        String.format("Không tìm thấy thông tin kho cho sản phẩm: %s",
                        productUnit.getProduct().getName())
                    ));

            int newQuantity = warehouse.getQuantityOnHand() - detail.getQuantity();

            if (newQuantity < 0) {
                throw new BadRequestException(
                    String.format("Không đủ hàng cho sản phẩm: %s",
                    productUnit.getProduct().getName())
                );
            }

            warehouse.setQuantityOnHand(newQuantity);
            warehouseRepository.save(warehouse);
        }
    }

    /**
     * Hoàn lại tồn kho khi hủy đơn hàng
     */
    private void restoreInventory(List<OrderDetail> orderDetails) {
        for (OrderDetail detail : orderDetails) {
            ProductUnit productUnit = detail.getProductUnit();

            Warehouse warehouse = warehouseRepository.findByProductUnit(productUnit)
                    .orElseThrow(() -> new NotFoundException(
                        String.format("Không tìm thấy thông tin kho cho sản phẩm: %s",
                        productUnit.getProduct().getName())
                    ));

            int newQuantity = warehouse.getQuantityOnHand() + detail.getQuantity();
            warehouse.setQuantityOnHand(newQuantity);
            warehouseRepository.save(warehouse);
        }
    }

    /**
     * Tạo hóa đơn bán hàng khi đơn hàng được giao
     */
    private void createSalesInvoice(Order order) {
        // TODO: Tích hợp với SaleService để tạo hóa đơn
        log.info("Tạo hóa đơn bán hàng cho đơn hàng ID: {}", order.getOrderId());
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
                null,  // recipientName - không dùng nữa
                null,  // deliveryPhone - không dùng nữa
                order.getDeliveryAddress(),
                null   // deliveryNote - không dùng nữa
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
                    promotionInfo
                );
            })
            .collect(Collectors.toList());

        // Build online payment info (if applicable)
        OnlinePaymentInfoDTO onlinePaymentInfo = null;
        if (order.getPaymentMethod() == PaymentMethod.ONLINE || order.getPaymentMethod() == PaymentMethod.CARD) {
            onlinePaymentInfo = new OnlinePaymentInfoDTO(
                order.getTransactionId(), // transactionId từ webhook
                "PayOS", // payment provider
                order.getPaymentStatus().name(), // UNPAID, PAID, FAILED, REFUNDED
                paymentUrl, // URL thanh toán
                qrCode, // QR code
                LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // expiration time
            );
        }

        // Calculate change amount - không còn tính changeAmount vì không có amountPaid trong request
        BigDecimal changeAmount = BigDecimal.ZERO;

        // Build danh sách khuyến mãi đã áp dụng
        List<PromotionAppliedDTO> appliedPromotions = new ArrayList<>();
        
        // Lấy khuyến mãi từ OrderDetail (PRODUCT_DISCOUNT, BUY_X_GET_Y)
        for (OrderDetail detail : orderDetails) {
            if (detail.getPromotionId() != null) {
                appliedPromotions.add(new PromotionAppliedDTO(
                    detail.getPromotionId(),
                    detail.getPromotionName(),
                    detail.getPromotionDetailId(),
                    detail.getPromotionSummary(),
                    detail.getDiscountType(),
                    detail.getDiscountValue(),
                    detail.getSourceLineItemId()
                ));
            }
        }

        // Lấy khuyến mãi toàn đơn từ JSON (ORDER_DISCOUNT)
        if (order.getAppliedOrderPromotionsJson() != null && !order.getAppliedOrderPromotionsJson().isEmpty()) {
            try {
                List<CheckPromotionResponseDTO.OrderPromotionDTO> orderPromotions = 
                    objectMapper.readValue(
                        order.getAppliedOrderPromotionsJson(), 
                        objectMapper.getTypeFactory().constructCollectionType(
                            List.class, 
                            CheckPromotionResponseDTO.OrderPromotionDTO.class
                        )
                    );
                
                // Convert sang PromotionAppliedDTO
                for (CheckPromotionResponseDTO.OrderPromotionDTO orderPromotion : orderPromotions) {
                    appliedPromotions.add(new PromotionAppliedDTO(
                        orderPromotion.promotionId(),
                        orderPromotion.promotionName(),
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
            "ORD" + String.format("%08d", order.getOrderId()),
            order.getStatus(),
            order.getDeliveryType(),
            order.getPaymentMethod(),
            order.getPaymentStatus(),
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
            "Đặt hàng thành công"
        );
    }

    /**
     * Lấy danh sách đơn hàng của khách hàng với khả năng lọc theo trạng thái và phân trang
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
}