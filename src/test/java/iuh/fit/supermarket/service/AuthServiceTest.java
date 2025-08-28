package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.auth.LoginRequest;
import iuh.fit.supermarket.dto.auth.LoginResponse;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.EmployeeRole;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit test cho AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Employee testEmployee;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Tạo test employee
        testEmployee = new Employee();
        testEmployee.setEmployeeId(1);
        testEmployee.setName("Test Employee");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPasswordHash("$2a$10$hashedPassword");
        testEmployee.setRole(EmployeeRole.STAFF);
        testEmployee.setIsDeleted(false);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());

        // Tạo login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void testLoginSuccess() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmailAndIsDeletedFalse(anyString()))
            .thenReturn(Optional.of(testEmployee));
        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("test-jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        
        assertNotNull(response.getEmployee());
        assertEquals(testEmployee.getEmployeeId(), response.getEmployee().getEmployeeId());
        assertEquals(testEmployee.getName(), response.getEmployee().getName());
        assertEquals(testEmployee.getEmail(), response.getEmployee().getEmail());
        assertEquals(testEmployee.getRole(), response.getEmployee().getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmailAndIsDeletedFalse(loginRequest.getEmail());
        verify(jwtUtil).generateToken(testEmployee.getEmail());
    }

    @Test
    void testLoginWithBadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository, never()).findByEmailAndIsDeletedFalse(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLoginWithDeletedEmployee() {
        // Given
        testEmployee.setIsDeleted(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmailAndIsDeletedFalse(anyString()))
            .thenReturn(Optional.of(testEmployee));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmailAndIsDeletedFalse(loginRequest.getEmail());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testLoginWithNonExistentEmployee() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(employeeRepository.findByEmailAndIsDeletedFalse(anyString()))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(employeeRepository).findByEmailAndIsDeletedFalse(loginRequest.getEmail());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    void testValidateTokenSuccess() {
        // Given
        String token = "valid-token";
        when(jwtUtil.validateToken(token)).thenReturn(true);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertTrue(result);
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void testValidateTokenFailure() {
        // Given
        String token = "invalid-token";
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertFalse(result);
        verify(jwtUtil).validateToken(token);
    }

    @Test
    void testGetEmailFromToken() {
        // Given
        String token = "test-token";
        String expectedEmail = "test@example.com";
        when(jwtUtil.getUsernameFromToken(token)).thenReturn(expectedEmail);

        // When
        String result = authService.getEmailFromToken(token);

        // Then
        assertEquals(expectedEmail, result);
        verify(jwtUtil).getUsernameFromToken(token);
    }

    @Test
    void testGetEmployeeByEmail() {
        // Given
        when(employeeRepository.findByEmailAndIsDeletedFalse(testEmployee.getEmail()))
            .thenReturn(Optional.of(testEmployee));

        // When
        Employee result = authService.getEmployeeByEmail(testEmployee.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(testEmployee.getEmployeeId(), result.getEmployeeId());
        assertEquals(testEmployee.getEmail(), result.getEmail());
        verify(employeeRepository).findByEmailAndIsDeletedFalse(testEmployee.getEmail());
    }

    @Test
    void testGetEmployeeByEmailNotFound() {
        // Given
        String email = "notfound@example.com";
        when(employeeRepository.findByEmailAndIsDeletedFalse(email))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authService.getEmployeeByEmail(email);
        });

        verify(employeeRepository).findByEmailAndIsDeletedFalse(email);
    }
}
