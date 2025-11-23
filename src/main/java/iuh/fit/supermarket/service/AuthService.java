package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.auth.CustomerLoginRequest;
import iuh.fit.supermarket.dto.auth.CustomerLoginResponse;
import iuh.fit.supermarket.dto.auth.LoginRequest;
import iuh.fit.supermarket.dto.auth.LoginResponse;
import iuh.fit.supermarket.dto.auth.UserInfoResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.UserRole;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.UserRepository;
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
 * Sau refactoring: sử dụng UserRepository thống nhất cho cả Employee và Customer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Xử lý đăng nhập nhân viên
     * Sau refactoring: load user từ UserRepository, sau đó load Employee
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

            // Lấy thông tin user từ database (chỉ employee users)
            User user = userRepository.findEmployeeByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy employee với email: {}", loginRequest.getEmail());
                    return new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + loginRequest.getEmail());
                });

            // Kiểm tra tài khoản có bị khóa không
            if (user.getIsDeleted()) {
                log.warn("Tài khoản nhân viên {} đã bị khóa", loginRequest.getEmail());
                throw new DisabledException("Tài khoản đã bị khóa");
            }

            // Lấy Employee entity từ user_id
            Employee employee = employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy employee record"));

            // Tạo JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Tạo response với thông tin từ User và Employee
            LoginResponse.EmployeeInfo employeeInfo = new LoginResponse.EmployeeInfo(
                employee.getEmployeeId(),
                user.getName(),
                user.getEmail(),
                user.getUserRole()
            );

            LoginResponse response = new LoginResponse(
                token,
                jwtUtil.getExpirationTime(),
                employeeInfo
            );

            log.info("Đăng nhập thành công cho nhân viên: {} ({})", user.getName(), user.getEmail());
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
        // Tìm User từ email (chỉ employee users)
        User user = userRepository.findEmployeeByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + email));

        // Lấy Employee entity từ user_id
        return employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy employee record"));
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
            // Tìm User từ email (chỉ employee users)
            User user = userRepository.findEmployeeByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + email));

            String userRole = "ROLE_" + user.getUserRole().name();
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
            // Tìm User với role CUSTOMER bằng email hoặc số điện thoại
            User user = userRepository.findCustomerByEmailOrPhone(loginRequest.getEmailOrPhone())
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy khách hàng: {}", loginRequest.getEmailOrPhone());
                    return new BadCredentialsException("Email/Số điện thoại hoặc mật khẩu không chính xác");
                });

            // Kiểm tra tài khoản có bị xóa không
            if (user.getIsDeleted()) {
                log.warn("Tài khoản khách hàng {} đã bị khóa", loginRequest.getEmailOrPhone());
                throw new DisabledException("Tài khoản đã bị khóa");
            }

            // Kiểm tra mật khẩu
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
                log.warn("Sai mật khẩu cho khách hàng: {}", loginRequest.getEmailOrPhone());
                throw new BadCredentialsException("Email/Số điện thoại hoặc mật khẩu không chính xác");
            }

            // Lấy Customer entity từ user_id
            Customer customer = customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy customer record"));

            // Tạo JWT token với identifier (email hoặc phone)
            String identifier = user.getEmail() != null ? user.getEmail() : user.getPhone();
            String token = jwtUtil.generateToken("CUSTOMER:" + identifier);

            // Tạo response với thông tin từ User và Customer
            CustomerLoginResponse.CustomerInfo customerInfo = new CustomerLoginResponse.CustomerInfo(
                customer.getCustomerId(),
                user.getName(),      // Từ User entity
                user.getEmail(),     // Từ User entity
                user.getPhone(),     // Từ User entity
                customer.getCustomerType()
            );

            CustomerLoginResponse response = new CustomerLoginResponse(
                token,
                jwtUtil.getExpirationTime(),
                customerInfo
            );

            log.info("Đăng nhập thành công cho khách hàng: {} ({})", user.getName(), loginRequest.getEmailOrPhone());
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
     * Lấy thông tin khách hàng từ email
     * @param email email của khách hàng
     * @return Customer entity
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByEmail(String email) {
        // Tìm User với role CUSTOMER theo email
        User user = userRepository.findCustomerByEmailOrPhone(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với email: " + email));

        // Lấy Customer entity từ user_id
        return customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy customer record"));
    }

    /**
     * Lấy thông tin khách hàng từ số điện thoại
     * @param phone số điện thoại của khách hàng
     * @return Customer entity
     */
    @Transactional(readOnly = true)
    public Customer getCustomerByPhone(String phone) {
        // Tìm User với role CUSTOMER theo phone
        User user = userRepository.findCustomerByEmailOrPhone(phone)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng với số điện thoại: " + phone));

        // Lấy Customer entity từ user_id
        return customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy customer record"));
    }

    /**
     * Lấy thông tin user hiện tại từ JWT token
     * Hỗ trợ cả nhân viên (Employee) và khách hàng (Customer)
     * @param token JWT token
     * @return UserInfoResponse chứa thông tin user
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserByToken(String token) {
        log.debug("Đang lấy thông tin user từ token");

        try {
            // Lấy identifier từ token
            String identifier = jwtUtil.getUsernameFromToken(token);

            if (identifier == null) {
                throw new UsernameNotFoundException("Token không hợp lệ");
            }

            return getUserByUsername(identifier);

        } catch (UsernameNotFoundException e) {
            log.warn("Không tìm thấy user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user từ token", e);
            throw new RuntimeException("Có lỗi xảy ra khi lấy thông tin user");
        }
    }

    /**
     * Lấy thông tin user hiện tại từ username (identifier)
     * Hỗ trợ cả nhân viên (Employee) và khách hàng (Customer)
     * @param username email của employee hoặc "CUSTOMER:email/phone" cho customer
     * @return UserInfoResponse chứa thông tin user
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserByUsername(String username) {
        log.debug("Đang lấy thông tin user với username: {}", username);

        try {
            // Kiểm tra xem đây là customer hay employee
            if (username.startsWith("CUSTOMER:")) {
                // Đây là customer token
                String emailOrPhone = username.substring(9); // Bỏ "CUSTOMER:" prefix
                return getUserInfoForCustomer(emailOrPhone);
            } else {
                // Đây là employee token (chỉ có email)
                return getUserInfoForEmployee(username);
            }

        } catch (UsernameNotFoundException e) {
            log.warn("Không tìm thấy user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user", e);
            throw new RuntimeException("Có lỗi xảy ra khi lấy thông tin user");
        }
    }

    /**
     * Lấy thông tin cho nhân viên
     * @param email email của nhân viên
     * @return UserInfoResponse
     */
    private UserInfoResponse getUserInfoForEmployee(String email) {
        // Tìm User từ email (chỉ employee users)
        User user = userRepository.findEmployeeByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + email));

        // Kiểm tra tài khoản có bị khóa không
        if (user.getIsDeleted()) {
            throw new DisabledException("Tài khoản đã bị khóa");
        }

        // Lấy Employee entity từ user_id
        Employee employee = employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy employee record"));

        return UserInfoResponse.forEmployee(
            employee.getEmployeeId(),
            user.getName(),
            user.getEmail(),
            user.getUserRole()
        );
    }

    /**
     * Lấy thông tin cho khách hàng
     * @param emailOrPhone email hoặc số điện thoại của khách hàng
     * @return UserInfoResponse
     */
    private UserInfoResponse getUserInfoForCustomer(String emailOrPhone) {
        // Tìm User với role CUSTOMER theo email hoặc phone
        User user = userRepository.findCustomerByEmailOrPhone(emailOrPhone)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy khách hàng: " + emailOrPhone));

        // Kiểm tra tài khoản có bị khóa không
        if (user.getIsDeleted()) {
            throw new DisabledException("Tài khoản đã bị khóa");
        }

        // Lấy Customer entity từ user_id
        Customer customer = customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy customer record"));

        return UserInfoResponse.forCustomer(
            customer.getCustomerId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            customer.getCustomerType(),
            customer.getAddress()
        );
    }
}
