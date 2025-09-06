package iuh.fit.supermarket.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO chuẩn cho tất cả API response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    /**
     * Trạng thái thành công
     */
    private Boolean success;

    /**
     * Thông báo
     */
    private String message;

    /**
     * Dữ liệu trả về
     */
    private T data;

    /**
     * Thời gian phản hồi
     */
    private LocalDateTime timestamp;

    /**
     * Constructor cho response thành công với data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Thành công", data, LocalDateTime.now());
    }

    /**
     * Constructor cho response thành công với message
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /**
     * Constructor cho response lỗi
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }

    /**
     * Constructor cho response lỗi với data
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, LocalDateTime.now());
    }
}
