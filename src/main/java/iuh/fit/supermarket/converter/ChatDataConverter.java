package iuh.fit.supermarket.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iuh.fit.supermarket.dto.ChatData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA Converter để chuyển đổi giữa ChatData object và JSON string
 * Sử dụng Jackson ObjectMapper để serialize/deserialize
 * Hỗ trợ Java 8 Date/Time types (LocalDateTime, LocalDate, etc.)
 */
@Converter(autoApply = false)
@Slf4j
public class ChatDataConverter implements AttributeConverter<ChatData, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // Hỗ trợ Java 8 Date/Time
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Serialize dates as ISO-8601 strings

    /**
     * Chuyển đổi ChatData object sang JSON string để lưu vào database
     *
     * @param chatData đối tượng ChatData cần chuyển đổi
     * @return JSON string hoặc null nếu input là null
     */
    @Override
    public String convertToDatabaseColumn(ChatData chatData) {
        if (chatData == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(chatData);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi chuyển đổi ChatData sang JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Chuyển đổi JSON string từ database sang ChatData object
     *
     * @param jsonString JSON string từ database
     * @return đối tượng ChatData hoặc null nếu input là null hoặc rỗng
     */
    @Override
    public ChatData convertToEntityAttribute(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(jsonString, ChatData.class);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi chuyển đổi JSON sang ChatData: {}", e.getMessage());
            return null;
        }
    }
}
