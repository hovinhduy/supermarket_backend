package iuh.fit.supermarket.dto.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO cho request gửi tin nhắn chat
 * Customer ID sẽ được lấy tự động từ SecurityContext
 */
public record ChatRequest(
        /**
         * ID của conversation (null nếu bắt đầu conversation mới)
         */
        String conversationId,

        /**
         * Nội dung tin nhắn từ user
         */
        @NotBlank(message = "Tin nhắn không được để trống")
        String message
) {
}
