package iuh.fit.supermarket.dto.chat;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;

import java.time.LocalDateTime;

/**
 * DTO cho response của tin nhắn chat
 *
 * Hỗ trợ cả text response (legacy) và structured response (mới)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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
         * Nội dung response từ AI dạng text (legacy, để tương thích ngược)
         */
        String message,

        /**
         * Nội dung response từ AI dạng structured (mới)
         * Nếu không null, frontend nên ưu tiên sử dụng structured data
         */
        AIStructuredResponse structuredData,

        /**
         * Thời gian tạo message
         */
        LocalDateTime timestamp
) {

    /**
     * Constructor cho text response (legacy)
     */
    public ChatResponse(String conversationId, Long messageId, String message, LocalDateTime timestamp) {
        this(conversationId, messageId, message, null, timestamp);
    }

    /**
     * Constructor cho structured response (mới)
     */
    public static ChatResponse withStructuredData(
            String conversationId,
            Long messageId,
            AIStructuredResponse structuredData,
            LocalDateTime timestamp) {
        // Vẫn lưu message dạng text từ structuredData.message() để tương thích ngược
        String textMessage = structuredData != null ? structuredData.message() : "";
        return new ChatResponse(conversationId, messageId, textMessage, structuredData, timestamp);
    }
}
