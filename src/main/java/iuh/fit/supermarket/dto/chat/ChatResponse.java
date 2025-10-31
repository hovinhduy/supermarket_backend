package iuh.fit.supermarket.dto.chat;

import java.time.LocalDateTime;

/**
 * DTO cho response của tin nhắn chat
 */
public record ChatResponse(
        /**
         * ID của conversation
         */
        String conversationId,

        /**
         * ID của message vừa tạo
         */
        Long messageId,

        /**
         * Nội dung response từ AI
         */
        String message,

        /**
         * Thời gian tạo message
         */
        LocalDateTime timestamp
) {
}
