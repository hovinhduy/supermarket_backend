package iuh.fit.supermarket.dto.chat;

import iuh.fit.supermarket.dto.ChatData;
import iuh.fit.supermarket.enums.SenderType;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin tin nhắn trong lịch sử chat
 */
public record MessageResponse(
        /**
         * ID của message
         */
        Long id,

        /**
         * Loại người gửi (USER hoặc AI)
         */
        SenderType senderType,

        /**
         * Nội dung tin nhắn
         */
        String content,

        /**
         * Dữ liệu có cấu trúc (chỉ có với AI messages)
         */
        ChatData data,

        /**
         * Thời gian gửi
         */
        LocalDateTime timestamp
) {
}
