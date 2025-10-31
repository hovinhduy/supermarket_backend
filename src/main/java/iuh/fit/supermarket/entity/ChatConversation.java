package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho cuộc hội thoại chat với AI
 */
@Entity
@Table(name = "chat_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversation {

    /**
     * ID duy nhất của conversation (UUID)
     */
    @Id
    @Column(name = "id", length = 36)
    private String id;

    /**
     * Khách hàng sở hữu conversation
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Tiêu đề conversation (tự động sinh hoặc tùy chỉnh)
     */
    @Column(name = "title", length = 255)
    private String title = "Cuộc trò chuyện mới";

    /**
     * Thời gian tạo conversation
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Danh sách tin nhắn trong conversation
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ChatMessage> messages;
}
