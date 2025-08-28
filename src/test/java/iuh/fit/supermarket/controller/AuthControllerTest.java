package iuh.fit.supermarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.auth.LoginRequest;
import iuh.fit.supermarket.dto.auth.LoginResponse;
import iuh.fit.supermarket.enums.EmployeeRole;
import iuh.fit.supermarket.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test cho AuthController
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest validLoginRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        // Tạo valid login request
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("password123");

        // Tạo login response
        LoginResponse.EmployeeInfo employeeInfo = new LoginResponse.EmployeeInfo(
                1, "Test Employee", "test@example.com", EmployeeRole.STAFF);
        loginResponse = new LoginResponse("test-jwt-token", 86400000L, employeeInfo);
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.accessToken").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(86400000))
                .andExpect(jsonPath("$.data.employee.employeeId").value(1))
                .andExpect(jsonPath("$.data.employee.name").value("Test Employee"))
                .andExpect(jsonPath("$.data.employee.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.employee.role").value("STAFF"));
    }

    @Test
    void testLoginWithBadCredentials() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không chính xác"));
    }

    @Test
    void testLoginWithDisabledAccount() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new DisabledException("Account disabled"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tài khoản đã bị khóa"));
    }

    @Test
    void testLoginWithUserNotFound() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email hoặc mật khẩu không chính xác"));
    }

    @Test
    void testLoginWithInvalidInput() throws Exception {
        // Given - invalid login request
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("123"); // Too short

        // When & Then
        mockMvc.perform(post("/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Dữ liệu không hợp lệ"));
    }

    @Test
    void testValidateTokenSuccess() throws Exception {
        // Given
        when(authService.validateToken("valid-token")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .with(csrf())
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token hợp lệ"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void testValidateTokenFailure() throws Exception {
        // Given
        when(authService.validateToken("invalid-token")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/validate")
                .with(csrf())
                .param("token", "invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token không hợp lệ"))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithMockUser
    void testLogout() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/logout")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công"));
    }
}
