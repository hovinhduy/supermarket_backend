package iuh.fit.supermarket.dto.auth;

import iuh.fit.supermarket.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho phản hồi đăng nhập thành công của khách hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLoginResponse {

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
     * Thông tin khách hàng đăng nhập
     */
    private CustomerInfo customer;

    /**
     * Constructor với token và customer info
     */
    public CustomerLoginResponse(String accessToken, Long expiresIn, CustomerInfo customer) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.customer = customer;
    }

    /**
     * Thông tin cơ bản của khách hàng
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private Integer customerId;
        private String name;
        private String email;
        private String phone;
        private CustomerType customerType;
    }
}
