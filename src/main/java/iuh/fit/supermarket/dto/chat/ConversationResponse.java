package iuh.fit.supermarket.dto.chat;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin conversation
 */
public record ConversationResponse(
        /**
         * ID của conversation
         */
        String id,

        /**
         * ID của khách hàng
         */
        Integer customerId,

        /**
         * Tiêu đề conversation
         */
        String title,

        /**
         * Thời gian tạo
         */
        LocalDateTime createdAt,

        /**
         * Thời gian cập nhật gần nhất
         */
        LocalDateTime updatedAt,

        /**
         * Preview của tin nhắn cuối cùng
         */
        String lastMessage
) {
}
