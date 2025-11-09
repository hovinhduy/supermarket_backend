package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * DTO cho structured output từ AI
 *
 * Thay vì trả về text đơn thuần, AI sẽ trả về dữ liệu có cấu trúc này
 * để frontend dễ dàng parse và hiển thị
 */
@JsonPropertyOrder({"responseType", "message", "data", "suggestions", "metadata"})
public record AIStructuredResponse(

        /**
         * Loại response: PRODUCT_INFO, ORDER_INFO, PROMOTION_INFO, GENERAL_ANSWER, ERROR
         */
        @JsonProperty(required = true, value = "response_type")
        ResponseType responseType,

        /**
         * Câu trả lời dạng văn bản cho người dùng (để hiển thị)
         */
        @JsonProperty(required = true, value = "message")
        String message,

        /**
         * Dữ liệu có cấu trúc (tùy theo responseType)
         */
        @JsonProperty(value = "data")
        ResponseData data,

        /**
         * Các gợi ý câu hỏi tiếp theo cho người dùng
         */
        @JsonProperty(value = "suggestions")
        List<String> suggestions,

        /**
         * Metadata bổ sung về response
         */
        @JsonProperty(value = "metadata")
        ResponseMetadata metadata
) {

    /**
     * Enum định nghĩa các loại response
     */
    public enum ResponseType {
        PRODUCT_INFO,       // Thông tin về sản phẩm
        ORDER_INFO,         // Thông tin đơn hàng
        PROMOTION_INFO,     // Thông tin khuyến mãi
        STOCK_INFO,         // Thông tin tồn kho
        GENERAL_ANSWER,     // Câu trả lời chung (chính sách, giờ mở cửa, etc.)
        ERROR              // Lỗi hoặc không tìm thấy thông tin
    }
}
