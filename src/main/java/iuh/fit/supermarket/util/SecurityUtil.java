package iuh.fit.supermarket.util;

import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility class để lấy thông tin nhân viên hoặc khách hàng hiện tại từ SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin nhân viên hiện tại từ SecurityContext
     * 
     * @return Employee hiện tại
     * @throws IllegalStateException nếu không thể xác định nhân viên hiện tại
     */
    public Employee getCurrentEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có thông tin xác thực");
        }

        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Principal không phải là UserDetails");
        }

        UserDetails userDetails = (UserDetails) principal;
        String email = userDetails.getUsername(); // Email được sử dụng làm username

        log.debug("Lấy thông tin nhân viên với email: {}", email);

        // Tìm User trước, sau đó lấy Employee từ user_id
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với email: {}", email);
                    return new IllegalStateException("Không tìm thấy user với email: " + email);
                });

        return employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy nhân viên với user_id: {}", user.getUserId());
                    return new IllegalStateException("Không tìm thấy nhân viên với email: " + email);
                });
    }

    /**
     * Lấy ID nhân viên hiện tại từ SecurityContext
     * 
     * @return ID nhân viên hiện tại
     * @throws IllegalStateException nếu không thể xác định nhân viên hiện tại
     */
    public Integer getCurrentEmployeeId() {
        return getCurrentEmployee().getEmployeeId();
    }

    /**
     * Lấy email nhân viên hiện tại từ SecurityContext
     * 
     * @return Email nhân viên hiện tại
     * @throws IllegalStateException nếu không thể xác định nhân viên hiện tại
     */
    public String getCurrentEmployeeEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có thông tin xác thực");
        }

        Object principal = authentication.getPrincipal();
        
        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Principal không phải là UserDetails");
        }

        return ((UserDetails) principal).getUsername();
    }

    /**
     * Kiểm tra xem có nhân viên nào đang đăng nhập không
     *
     * @return true nếu có nhân viên đang đăng nhập
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UserDetails;
    }

    /**
     * Lấy thông tin khách hàng hiện tại từ SecurityContext
     *
     * @return Customer hiện tại
     * @throws IllegalStateException nếu không thể xác định khách hàng hiện tại
     */
    public Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có thông tin xác thực");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Principal không phải là UserDetails");
        }

        UserDetails userDetails = (UserDetails) principal;
        String username = userDetails.getUsername(); // Username có thể có prefix "CUSTOMER:"

        log.debug("Lấy thông tin khách hàng với username: {}", username);

        // Strip "CUSTOMER:" prefix nếu có
        String emailOrPhone = username.startsWith("CUSTOMER:") ? username.substring(9) : username;

        log.debug("Email/Phone sau khi strip prefix: {}", emailOrPhone);

        // Tìm User trước (có thể là email hoặc phone)
        User user = userRepository.findByEmailAndIsDeletedFalse(emailOrPhone)
                .or(() -> userRepository.findByPhoneAndIsDeletedFalse(emailOrPhone))
                .orElseThrow(() -> {
                    log.error("Không tìm thấy user với email/phone: {}", emailOrPhone);
                    return new IllegalStateException("Không tìm thấy user với email/phone: " + emailOrPhone);
                });

        return customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> {
                    log.error("Không tìm thấy khách hàng với user_id: {}", user.getUserId());
                    return new IllegalStateException("Không tìm thấy khách hàng với email/phone: " + emailOrPhone);
                });
    }

    /**
     * Lấy ID khách hàng hiện tại từ SecurityContext
     *
     * @return ID khách hàng hiện tại
     * @throws IllegalStateException nếu không thể xác định khách hàng hiện tại
     */
    public Integer getCurrentCustomerId() {
        return getCurrentCustomer().getCustomerId();
    }

    /**
     * Lấy email user hiện tại từ SecurityContext (dùng chung cho cả Employee và Customer)
     *
     * @return Email user hiện tại
     * @throws IllegalStateException nếu không thể xác định user hiện tại
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có thông tin xác thực");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Principal không phải là UserDetails");
        }

        return ((UserDetails) principal).getUsername();
    }
}
