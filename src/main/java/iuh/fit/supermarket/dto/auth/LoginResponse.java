package iuh.fit.supermarket.dto.auth;

import iuh.fit.supermarket.enums.EmployeeRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho phản hồi đăng nhập thành công
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT access token
     */
    private String accessToken;

    /**
     * Loại token (Bearer)
     */
    private String tokenType = "Bearer";

    /**
     * Thời gian hết hạn token (milliseconds)
     */
    private Long expiresIn;

    /**
     * Thông tin nhân viên đăng nhập
     */
    private EmployeeInfo employee;

    /**
     * Constructor với token và employee info
     */
    public LoginResponse(String accessToken, Long expiresIn, EmployeeInfo employee) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.employee = employee;
    }

    /**
     * Thông tin cơ bản của nhân viên
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Integer employeeId;
        private String name;
        private String email;
        private EmployeeRole role;
    }
}
