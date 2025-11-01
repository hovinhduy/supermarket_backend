package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.auth.CustomerLoginRequest;
import iuh.fit.supermarket.dto.auth.CustomerLoginResponse;
import iuh.fit.supermarket.dto.auth.LoginRequest;
import iuh.fit.supermarket.dto.auth.LoginResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service xử lý authentication và authorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Xử lý đăng nhập nhân viên
     * @param loginRequest thông tin đăng nhập
     * @return LoginResponse chứa JWT token và thông tin nhân viên
     * @throws AuthenticationException nếu đăng nhập thất bại
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        log.info("Đang xử lý đăng nhập cho email: {}", loginRequest.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Lấy thông tin nhân viên từ database
            Employee employee = employeeRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy nhân viên với email: {}", loginRequest.getEmail());
                    return new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + loginRequest.getEmail());
                });

            // Kiểm tra tài khoản có bị khóa không
            if (employee.getIsDeleted()) {
                log.warn("Tài khoản nhân viên {} đã bị khóa", loginRequest.getEmail());
                throw new DisabledException("Tài khoản đã bị khóa");
            }

            // Tạo JWT token
            String token = jwtUtil.generateToken(employee.getEmail());
            
            // Tạo response
            LoginResponse.EmployeeInfo employeeInfo = new LoginResponse.EmployeeInfo(
                employee.getEmployeeId(),
                employee.getName(),
                employee.getEmail(),
                employee.getRole()
            );

            LoginResponse response = new LoginResponse(
                token,
                jwtUtil.getExpirationTime(),
                employeeInfo
            );

            log.info("Đăng nhập thành công cho nhân viên: {} ({})", employee.getName(), employee.getEmail());
            return response;

        } catch (BadCredentialsException e) {
            log.warn("Sai thông tin đăng nhập cho email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác");
        } catch (DisabledException e) {
            log.warn("Tài khoản bị khóa cho email: {}", loginRequest.getEmail());
            throw new DisabledException("Tài khoản đã bị khóa");
        } catch (AuthenticationException e) {
            log.error("Lỗi xác thực cho email: {}", loginRequest.getEmail(), e);
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không xác định khi đăng nhập cho email: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("Có lỗi xảy ra trong quá trình đăng nhập");
        }
    }

    /**
     * Validate JWT token
     * @param token JWT token
     * @return true nếu token hợp lệ
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("Lỗi khi validate token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy email từ JWT token
     * @param token JWT token
     * @return email của nhân viên
     */
    public String getEmailFromToken(String token) {
        try {
            return jwtUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("Lỗi khi lấy email từ token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Lấy thông tin nhân viên từ email
     * @param email email của nhân viên
     * @return Employee entity
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmailAndIsDeletedFalse(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + email));
    }

    /**
     * Kiểm tra nhân viên có quyền truy cập không
     * @param email email của nhân viên
     * @param requiredRole role yêu cầu
     * @return true nếu có quyền
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String email, String requiredRole) {
        try {
            Employee employee = getEmployeeByEmail(email);
            String userRole = "ROLE_" + employee.getRole().name();
            return userRole.equals(requiredRole);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra quyền cho email: {}", email, e);
            return false;
        }
    }

    /**
     * Xử lý đăng nhập khách hàng bằng email hoặc số điện thoại
     * @param loginRequest thông tin đăng nhập (email hoặc số điện thoại và mật khẩu)
     * @return CustomerLoginResponse chứa JWT token và thông tin khách hàng
     * @throws AuthenticationException nếu đăng nhập thất bại
     */
    @Transactional(readOnly = true)
    public CustomerLoginResponse customerLogin(CustomerLoginRequest loginRequest) throws AuthenticationException {
        log.info("Đang xử lý đăng nhập khách hàng với: {}", loginRequest.getEmailOrPhone());

        try {
            // Tìm khách hàng bằng email hoặc số điện thoại
            Customer customer = findCustomerByEmailOrPhone(loginRequest.getEmailOrPhone());

            // Kiểm tra tài khoản có bị xóa không
            if (customer.getIsDeleted()) {
                log.warn("Tài khoản khách hàng {} đã bị khóa", loginRequest.getEmailOrPhone());
                throw new DisabledException("Tài khoản đã bị khóa");
            }

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getPasswordHash())) {
                log.warn("Sai mật khẩu cho khách hàng: {}", loginRequest.getEmailOrPhone());
                throw new BadCredentialsException("Email/Số điện thoại hoặc mật khẩu không chính xác");
            }

            // Tạo JWT token với identifier (email hoặc phone)
            String identifier = customer.getEmail() != null ? customer.getEmail() : customer.getPhone();
            String token = jwtUtil.generateToken("CUSTOMER:" + identifier);

            // Tạo response
            CustomerLoginResponse.CustomerInfo customerInfo = new CustomerLoginResponse.CustomerInfo(
                customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCustomerType()
            );

            CustomerLoginResponse response = new CustomerLoginResponse(
                token,
                jwtUtil.getExpirationTime(),
                customerInfo
            );

            log.info("Đăng nhập thành công cho khách hàng: {} ({})", customer.getName(), loginRequest.getEmailOrPhone());
            return response;

        } catch (BadCredentialsException e) {
            log.warn("Sai thông tin đăng nhập cho: {}", loginRequest.getEmailOrPhone());
            throw new BadCredentialsException("Email/Số điện thoại hoặc mật khẩu không chính xác");
        } catch (DisabledException e) {
            log.warn("Tài khoản bị khóa cho: {}", loginRequest.getEmailOrPhone());
            throw new DisabledException("Tài khoản đã bị khóa");
        } catch (UsernameNotFoundException e) {
            log.warn("Không tìm thấy khách hàng: {}", loginRequest.getEmailOrPhone());
            throw new BadCredentialsException("Email/Số điện thoại hoặc mật khẩu không chính xác");
        } catch (Exception e) {
            log.error("Lỗi không xác định khi đăng nhập khách hàng: {}", loginRequest.getEmailOrPhone(), e);
            throw new RuntimeException("Có lỗi xảy ra trong quá trình đăng nhập");
        }
    }

    /**
     * Tìm khách hàng theo email hoặc số điện thoại
     * @param emailOrPhone email hoặc số điện thoại
     * @return Customer entity
     * @throws UsernameNotFoundException nếu không tìm thấy
     */
    private Customer findCustomerByEmailOrPhone(String emailOrPhone) throws UsernameNotFoundException {
        // Kiểm tra xem input có phải là email không (chứa @)
        if (emailOrPhone.contains("@")) {
            return customerRepository.findByEmailAndIsDeletedFalse(emailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với email: " + emailOrPhone));
        } else {
            return customerRepository.findByPhoneAndIsDeletedFalse(emailOrPhone)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với số điện thoại: " + emailOrPhone));
        }
    }

    /**
     * Lấy thông tin khách hàng từ email
     * @param email email của khách hàng
     * @return Customer entity
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        return customerRepository.findByEmailAndIsDeletedFalse(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với email: " + email));
    }

    /**
     * Lấy thông tin khách hàng từ số điện thoại
     * @param phone số điện thoại của khách hàng
     * @return Customer entity
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByPhone(String phone) {
        return customerRepository.findByPhoneAndIsDeletedFalse(phone)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với số điện thoại: " + phone));
    }
}
