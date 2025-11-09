package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata bổ sung về AI response
 */
public record ResponseMetadata(

        /**
         * Số lượng kết quả trả về
         */
        @JsonProperty(value = "result_count")
        Integer resultCount,

        /**
         * Có cần tìm kiếm thêm không?
         */
        @JsonProperty(value = "has_more")
        Boolean hasMore,

        /**
         * Độ tin cậy của câu trả lời (0.0 - 1.0)
         */
        @JsonProperty(value = "confidence")
        Double confidence,

        /**
         * Tools đã được sử dụng để tạo response
         */
        @JsonProperty(value = "tools_used")
        String toolsUsed,

        /**
         * Thông tin bổ sung (nếu có)
         */
        @JsonProperty(value = "additional_info")
        String additionalInfo
) {

    /**
     * Factory method tạo metadata cơ bản
     */
    public static ResponseMetadata simple(int resultCount, String toolsUsed) {
        return new ResponseMetadata(resultCount, false, null, toolsUsed, null);
    }

    /**
     * Factory method tạo metadata đầy đủ
     */
    public static ResponseMetadata full(int resultCount, boolean hasMore,
                                       double confidence, String toolsUsed,
                                       String additionalInfo) {
        return new ResponseMetadata(resultCount, hasMore, confidence, toolsUsed, additionalInfo);
    }
}
