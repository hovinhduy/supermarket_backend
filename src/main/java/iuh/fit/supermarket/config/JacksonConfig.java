package iuh.fit.supermarket.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Cấu hình Jackson ObjectMapper để hỗ trợ Java 8 Date/Time API (LocalDateTime, LocalDate, etc.)
 */
@Configuration
public class JacksonConfig {

    /**
     * Tạo ObjectMapper bean với JSR310 module để serialize/deserialize LocalDateTime
     * 
     * @return ObjectMapper đã được config
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Đăng ký JSR310 module cho Java 8 Date/Time support
        mapper.registerModule(new JavaTimeModule());
        
        // Disable write dates as timestamps (dùng ISO-8601 string thay vì timestamp)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Ignore unknown properties khi deserialize (backup nếu AI tự ý thêm fields)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return mapper;
    }
}
