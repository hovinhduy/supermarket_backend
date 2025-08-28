package iuh.fit.supermarket.security;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    /**
     * Load user by username (email trong trường hợp này)
     * @param username email của nhân viên
     * @return UserDetails
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Đang load user với email: {}", username);
        
        Employee employee = employeeRepository.findByEmailAndIsDeletedFalse(username)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy nhân viên với email: {}", username);
                    return new UsernameNotFoundException("Không tìm thấy nhân viên với email: " + username);
                });

        log.debug("Tìm thấy nhân viên: {} với role: {}", employee.getName(), employee.getRole());
        
        return createUserDetails(employee);
    }

    /**
     * Tạo UserDetails từ Employee entity
     * @param employee Employee entity
     * @return UserDetails
     */
    private UserDetails createUserDetails(Employee employee) {
        Collection<GrantedAuthority> authorities = getAuthorities(employee);
        
        return User.builder()
                .username(employee.getEmail())
                .password(employee.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(employee.getIsDeleted())
                .credentialsExpired(false)
                .disabled(employee.getIsDeleted())
                .build();
    }

    /**
     * Lấy danh sách quyền của nhân viên
     * @param employee Employee entity
     * @return Collection<GrantedAuthority>
     */
    private Collection<GrantedAuthority> getAuthorities(Employee employee) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Thêm role chính
        authorities.add(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
        
        // Thêm các quyền cụ thể dựa trên role
        switch (employee.getRole()) {
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
                
            default:
                log.warn("Unknown role: {}", employee.getRole());
                break;
        }
        
        log.debug("Authorities cho user {}: {}", employee.getEmail(), authorities);
        return authorities;
    }

    /**
     * Load user by ID (để sử dụng trong JWT authentication)
     * @param employeeId ID của nhân viên
     * @return UserDetails
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Integer employeeId) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        return createUserDetails(employee);
    }
}
