package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.PaymentMethod;
import iuh.fit.supermarket.enums.DeliveryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho đơn hàng trong hệ thống
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * ID duy nhất của đơn hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    /**
     * Ngày đặt hàng
     */
    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();

    /**
     * Tổng tiền đơn hàng
     */
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /**
     * Thành tiền
     */
    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    /**
     * Tiền khách trả
     */
    @Column(name = "amount_paid", precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid;

    /**
     * Trạng thái đơn hàng
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.UNPAID;

    /**
     * Phương thức thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    /**
     * Mã giao dịch thanh toán (từ payment gateway)
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * Ghi chú đơn hàng
     */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    /**
     * Loại hình nhận hàng
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type")
    private DeliveryType deliveryType;

    /**
     * Địa chỉ giao hàng (nếu giao hàng tận nơi)
     */
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    /**
     * Số điện thoại nhận hàng
     */
    @Column(name = "delivery_phone", length = 20)
    private String deliveryPhone;

    /**
     * Tên người nhận hàng
     */
    @Column(name = "recipient_name", length = 255)
    private String recipientName;

    /**
     * Ghi chú giao hàng
     */
    @Column(name = "delivery_note", columnDefinition = "TEXT")
    private String deliveryNote;

    /**
     * Phí vận chuyển
     */
    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /**
     * Giảm giá toàn đơn (ORDER_DISCOUNT)
     */
    @Column(name = "order_discount", precision = 12, scale = 2)
    private BigDecimal orderDiscount = BigDecimal.ZERO;

    /**
     * Giảm giá dòng sản phẩm (PRODUCT_DISCOUNT + BUY_X_GET_Y)
     */
    @Column(name = "line_item_discount", precision = 12, scale = 2)
    private BigDecimal lineItemDiscount = BigDecimal.ZERO;

    /**
     * JSON string lưu danh sách khuyến mãi toàn đơn đã áp dụng
     * Format: List<OrderPromotionDTO>
     */
    @Column(name = "applied_order_promotions_json", columnDefinition = "TEXT")
    private String appliedOrderPromotionsJson;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Khách hàng đặt hàng (NULL cho khách vãng lai)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Nhân viên xử lý đơn hàng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /**
     * Danh sách chi tiết đơn hàng
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

    /**
     * Hóa đơn bán hàng tương ứng
     */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<SaleInvoiceHeader> saleInvoices;

    /**
     * Điểm tích lũy từ đơn hàng này
     */
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<LoyaltyPoint> loyaltyPoints;
}
