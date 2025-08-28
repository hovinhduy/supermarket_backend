package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.auth.LoginRequest;
import iuh.fit.supermarket.dto.auth.LoginResponse;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến authentication
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API xác thực và quản lý phiên đăng nhập")
public class AuthController {

    private final AuthService authService;

    /**
     * API đăng nhập nhân viên
     *
     * @param loginRequest thông tin đăng nhập
     * @return JWT token và thông tin nhân viên
     */
    @Operation(summary = "Đăng nhập nhân viên", description = "Xác thực thông tin đăng nhập và trả về JWT token cùng thông tin nhân viên")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không chính xác"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Tài khoản bị khóa")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Nhận yêu cầu đăng nhập từ email: {}", loginRequest.getEmail());

        try {
            LoginResponse loginResponse = authService.login(loginRequest);

            log.info("Đăng nhập thành công cho email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng nhập thành công", loginResponse));

        } catch (BadCredentialsException e) {
            log.warn("Đăng nhập thất bại - sai thông tin: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email hoặc mật khẩu không chính xác"));

        } catch (DisabledException e) {
            log.warn("Đăng nhập thất bại - tài khoản bị khóa: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tài khoản đã bị khóa"));

        } catch (UsernameNotFoundException e) {
            log.warn("Đăng nhập thất bại - không tìm thấy user: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Email hoặc mật khẩu không chính xác"));

        } catch (AuthenticationException e) {
            log.error("Lỗi xác thực cho email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Xác thực thất bại"));

        } catch (Exception e) {
            log.error("Lỗi không xác định khi đăng nhập cho email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra trong quá trình đăng nhập"));
        }
    }

    /**
     * API validate JWT token
     * 
     * @param token JWT token
     * @return kết quả validation
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        log.debug("Nhận yêu cầu validate token");

        try {
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok(
                        ApiResponse.success("Token hợp lệ", true));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không hợp lệ", false));
            }

        } catch (Exception e) {
            log.error("Lỗi khi validate token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi validate token", false));
        }
    }

    /**
     * API logout (client side sẽ xóa token)
     *
     * @return thông báo logout thành công
     */
    @Operation(summary = "Đăng xuất", description = "Đăng xuất khỏi hệ thống (client cần xóa token)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng xuất thành công")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        log.info("Nhận yêu cầu logout");

        // Với JWT, logout chủ yếu được xử lý ở client side bằng cách xóa token
        // Server có thể implement blacklist token nếu cần

        return ResponseEntity.ok(
                ApiResponse.success("Đăng xuất thành công", "Vui lòng xóa token ở client side"));
    }

    /**
     * API lấy thông tin user hiện tại từ token
     *
     * @param authorization Authorization header
     * @return thông tin user
     */
    @Operation(summary = "Lấy thông tin user hiện tại", description = "Lấy thông tin chi tiết của nhân viên đang đăng nhập từ JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(schema = @Schema(implementation = LoginResponse.EmployeeInfo.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token không hợp lệ hoặc đã hết hạn")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse.EmployeeInfo>> getCurrentUser(
            @Parameter(description = "JWT token với prefix 'Bearer '", required = true) @RequestHeader("Authorization") String authorization) {

        log.debug("Nhận yêu cầu lấy thông tin user hiện tại");

        try {
            // Lấy token từ header
            String token = authorization.substring(7); // Bỏ "Bearer " prefix
            String email = authService.getEmailFromToken(token);

            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token không hợp lệ"));
            }

            var employee = authService.getEmployeeByEmail(email);
            var employeeInfo = new LoginResponse.EmployeeInfo(
                    employee.getEmployeeId(),
                    employee.getName(),
                    employee.getEmail(),
                    employee.getRole());

            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin user thành công", employeeInfo));

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user hiện tại", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Có lỗi xảy ra khi lấy thông tin user"));
        }
    }
}
