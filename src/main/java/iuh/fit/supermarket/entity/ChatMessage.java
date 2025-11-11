package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.converter.ChatDataConverter;
import iuh.fit.supermarket.dto.ChatData;
import iuh.fit.supermarket.enums.SenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho tin nhắn trong cuộc hội thoại chat
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * ID duy nhất của message
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Cuộc hội thoại chứa message này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    /**
     * Loại người gửi (USER hoặc AI)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    /**
     * Nội dung tin nhắn
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Dữ liệu bổ sung trong cuộc trò chuyện
     * Lưu trữ thông tin về sản phẩm, đơn hàng, khuyến mãi, kho hàng và chính sách
     */
    @Convert(converter = ChatDataConverter.class)
    @Column(name = "data", columnDefinition = "JSON")
    private ChatData data;

    /**
     * Thời gian gửi tin nhắn
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
