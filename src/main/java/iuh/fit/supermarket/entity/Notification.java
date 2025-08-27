package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho thông báo gửi đến khách hàng
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    /**
     * ID duy nhất của thông báo
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    /**
     * Tiêu đề thông báo
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Nội dung thông báo
     */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Ngày gửi thông báo
     */
    @CreationTimestamp
    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    /**
     * Trạng thái đã đọc
     */
    @Column(name = "is_read")
    private Boolean isRead = false;

    /**
     * Khách hàng nhận thông báo
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}
