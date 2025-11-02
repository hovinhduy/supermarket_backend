package iuh.fit.supermarket.security;

import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.UserRole;
import iuh.fit.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService để load thông tin user từ database
 * Sau refactoring: load từ bảng users thay vì employees
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username (email hoặc identifier)
     * Hỗ trợ cả employee users (ADMIN, MANAGER, STAFF) và customer users
     * @param username email của employee hoặc "CUSTOMER:email/phone" cho customer
     * @return UserDetails
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Đang load user với identifier: {}", username);

        User user;

        // Kiểm tra xem đây có phải là customer token không
        if (username.startsWith("CUSTOMER:")) {
            // Đây là customer - lấy email/phone sau prefix
            String emailOrPhone = username.substring(9); // Bỏ "CUSTOMER:" prefix
            log.debug("Đang load customer với email/phone: {}", emailOrPhone);

            // Tìm customer user
            user = userRepository.findCustomerByEmailOrPhone(emailOrPhone)
                    .orElseThrow(() -> {
                        log.warn("Không tìm thấy customer với email/phone: {}", emailOrPhone);
                        return new UsernameNotFoundException("Không tìm thấy customer với email/phone: " + emailOrPhone);
                    });
        } else {
            // Đây là employee - chỉ có email
            log.debug("Đang load employee với email: {}", username);

            // Tìm employee user (role là ADMIN, MANAGER, hoặc STAFF)
            user = userRepository.findEmployeeByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("Không tìm thấy employee với email: {}", username);
                        return new UsernameNotFoundException("Không tìm thấy employee với email: " + username);
                    });
        }

        log.debug("Tìm thấy user: {} với role: {}", user.getName(), user.getUserRole());

        return createUserDetails(user);
    }

    /**
     * Tạo UserDetails từ User entity
     * @param user User entity
     * @return UserDetails
     */
    private UserDetails createUserDetails(User user) {
        Collection<GrantedAuthority> authorities = getAuthorities(user);

        // Xác định username: ưu tiên email, nếu không có thì dùng phone
        String username = user.getEmail() != null ? user.getEmail() : user.getPhone();

        // Nếu là customer và có cả email và phone, thêm prefix để phân biệt
        if (user.getUserRole() == UserRole.CUSTOMER && username != null) {
            username = "CUSTOMER:" + username;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getIsDeleted())
                .credentialsExpired(false)
                .disabled(user.getIsDeleted())
                .build();
    }

    /**
     * Lấy danh sách quyền của user dựa trên userRole
     * @param user User entity
     * @return Collection<GrantedAuthority>
     */
    private Collection<GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Thêm role chính
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));

        // Thêm các quyền cụ thể dựa trên role
        switch (user.getUserRole()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_ADMIN_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EMPLOYEE_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_INVENTORY_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_SALES_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REPORTS_VIEW"));
                break;

            case MANAGER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_INVENTORY_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_SALES_MANAGE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_REPORTS_VIEW"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_EMPLOYEE_VIEW"));
                break;

            case STAFF:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_SALES_CREATE"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_INVENTORY_VIEW"));
                break;

            case CUSTOMER:
                // Customer không có quyền truy cập admin panel
                authorities.add(new SimpleGrantedAuthority("PERMISSION_CUSTOMER_ORDER"));
                break;

            default:
                log.warn("Unknown role: {}", user.getUserRole());
                break;
        }

        log.debug("Authorities cho user {}: {}", user.getEmail(), authorities);
        return authorities;
    }

    /**
     * Load user by ID (để sử dụng trong JWT authentication)
     * @param userId ID của user
     * @return UserDetails
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user với ID: " + userId));

        return createUserDetails(user);
    }
}
