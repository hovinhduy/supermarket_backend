package iuh.fit.supermarket.util;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Utility class để lấy thông tin nhân viên hiện tại từ SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final EmployeeRepository employeeRepository;
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
}
