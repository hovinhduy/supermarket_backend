package iuh.fit.supermarket.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request gửi tin nhắn chat
 */
public record ChatRequest(
        /**
         * ID của conversation (null nếu bắt đầu conversation mới)
         */
        String conversationId,

        /**
         * ID của khách hàng
         */
        @NotNull(message = "Customer ID không được để trống")
        Integer customerId,

        /**
         * Nội dung tin nhắn từ user
         */
        @NotBlank(message = "Tin nhắn không được để trống")
        String message
) {
}
