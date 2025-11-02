package iuh.fit.supermarket.service;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.UserRole;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service xử lý business logic cho Employee
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy tất cả nhân viên chưa bị xóa
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        log.debug("Lấy danh sách tất cả nhân viên");
        return employeeRepository.findAllByUser_IsDeletedFalse();
    }

    /**
     * Lấy nhân viên theo ID
     * @param employeeId ID nhân viên
     * @return Optional<Employee>
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Integer employeeId) {
        log.debug("Lấy nhân viên với ID: {}", employeeId);
        return employeeRepository.findByEmployeeIdAndUser_IsDeletedFalse(employeeId);
    }

    /**
     * Lấy nhân viên theo email
     * @param email email nhân viên
     * @return Optional<Employee>
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmail(String email) {
        log.debug("Lấy nhân viên với email: {}", email);
        // Tìm User trước, sau đó lấy Employee từ user_id
        Optional<User> user = userRepository.findByEmailAndIsDeletedFalse(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.get().getUserId());
    }

    /**
     * Lấy nhân viên theo role
     * @param role vai trò nhân viên
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByRole(UserRole role) {
        log.debug("Lấy danh sách nhân viên với role: {}", role);
        return employeeRepository.findByUser_UserRoleAndUser_IsDeletedFalse(role);
    }

    /**
     * Tự động sinh mã nhân viên mới theo định dạng NV000001 đến NV999999
     * @return mã nhân viên mới
     */
    @Transactional(readOnly = true)
    public String generateEmployeeCode() {
        Optional<Employee> lastEmployee = employeeRepository.findTopByOrderByEmployeeCodeDesc();

        if (lastEmployee.isPresent() && lastEmployee.get().getEmployeeCode() != null) {
            String lastCode = lastEmployee.get().getEmployeeCode();
            // Trích xuất số từ mã (NV000001 -> 1)
            int lastNumber = Integer.parseInt(lastCode.substring(2));
            
            // Kiểm tra giới hạn
            if (lastNumber >= 999999) {
                throw new IllegalArgumentException("Đã hết mã nhân viên có thể tạo (NV999999)");
            }
            
            // Tạo mã mới
            int newNumber = lastNumber + 1;
            return String.format("NV%06d", newNumber);
        }

        // Nếu chưa có mã nào, bắt đầu từ NV000001
        return "NV000001";
    }

    /**
     * Tạo nhân viên mới
     * @param employee thông tin nhân viên (phải có User object đã set sẵn)
     * @return Employee đã được tạo
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        // Validate User object
        if (employee.getUser() == null) {
            throw new IllegalArgumentException("User object không được null");
        }

        User user = employee.getUser();
        log.info("Tạo nhân viên mới với email: {}", user.getEmail());

        // Xử lý mã nhân viên
        String employeeCode = employee.getEmployeeCode();
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            employeeCode = generateEmployeeCode();
            employee.setEmployeeCode(employeeCode);
            log.info("Tự động sinh mã nhân viên: {}", employeeCode);
        } else {
            employeeCode = employeeCode.trim();
            employee.setEmployeeCode(employeeCode);
            // Kiểm tra mã đã tồn tại chưa
            if (employeeRepository.existsByEmployeeCode(employeeCode)) {
                throw new IllegalArgumentException("Mã nhân viên đã tồn tại: " + employeeCode);
            }
        }

        // Kiểm tra email đã tồn tại chưa (qua UserRepository)
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + user.getEmail());
        }

        // Mã hóa password
        if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        // Set default values cho User
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(false);
        }

        // Đảm bảo role là employee role (ADMIN, MANAGER, STAFF)
        if (user.getUserRole() == UserRole.CUSTOMER) {
            throw new IllegalArgumentException("Không thể tạo Employee với role CUSTOMER");
        }

        // Tạo User trước
        User savedUser = userRepository.save(user);
        log.info("Đã tạo User mới với ID: {}", savedUser.getUserId());

        // Tạo Employee với user_id FK
        employee.setUser(savedUser);
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Đã tạo nhân viên mới với ID: {}", savedEmployee.getEmployeeId());

        return savedEmployee;
    }

    /**
     * Cập nhật thông tin nhân viên
     * @param employeeId ID nhân viên
     * @param updatedEmployee thông tin cập nhật (phải có User object đã set sẵn)
     * @return Employee đã được cập nhật
     */
    @Transactional
    public Employee updateEmployee(Integer employeeId, Employee updatedEmployee) {
        log.info("Cập nhật nhân viên với ID: {}", employeeId);

        // Validate updated User object
        if (updatedEmployee.getUser() == null) {
            throw new IllegalArgumentException("User object không được null");
        }

        Employee existingEmployee = employeeRepository.findByEmployeeIdAndUser_IsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

        User existingUser = existingEmployee.getUser();
        User updatedUser = updatedEmployee.getUser();

        // Kiểm tra email đã tồn tại chưa (ngoại trừ user hiện tại)
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
            userRepository.existsByEmailAndUserIdNot(updatedUser.getEmail(), existingUser.getUserId())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + updatedUser.getEmail());
        }

        // Cập nhật thông tin User
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setUserRole(updatedUser.getUserRole());
        if (updatedUser.getPhone() != null) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(updatedUser.getDateOfBirth());
        }
        if (updatedUser.getGender() != null) {
            existingUser.setGender(updatedUser.getGender());
        }

        // Cập nhật password nếu có
        if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
        }

        // Save User
        userRepository.save(existingUser);

        // Cập nhật Employee code nếu cần
        if (updatedEmployee.getEmployeeCode() != null && !updatedEmployee.getEmployeeCode().isEmpty()) {
            existingEmployee.setEmployeeCode(updatedEmployee.getEmployeeCode());
        }

        Employee savedEmployee = employeeRepository.save(existingEmployee);
        log.info("Đã cập nhật nhân viên với ID: {}", savedEmployee.getEmployeeId());

        return savedEmployee;
    }

    /**
     * Xóa mềm nhân viên (soft delete)
     * @param employeeId ID nhân viên
     */
    @Transactional
    public void deleteEmployee(Integer employeeId) {
        log.info("Xóa nhân viên với ID: {}", employeeId);

        Employee employee = employeeRepository.findByEmployeeIdAndUser_IsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Soft delete User (cascades logically to Employee via queries)
        User user = employee.getUser();
        user.setIsDeleted(true);
        userRepository.save(user);

        log.info("Đã xóa nhân viên với ID: {}", employeeId);
    }

    /**
     * Khôi phục nhân viên đã bị xóa
     * @param employeeId ID nhân viên
     */
    @Transactional
    public void restoreEmployee(Integer employeeId) {
        log.info("Khôi phục nhân viên với ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Restore User
        User user = employee.getUser();
        user.setIsDeleted(false);
        userRepository.save(user);

        log.info("Đã khôi phục nhân viên với ID: {}", employeeId);
    }

    /**
     * Đếm số lượng nhân viên theo role
     * @param role vai trò nhân viên
     * @return số lượng nhân viên
     */
    @Transactional(readOnly = true)
    public long countEmployeesByRole(UserRole role) {
        log.debug("Đếm số lượng nhân viên với role: {}", role);
        return employeeRepository.countByRoleAndIsDeletedFalse(role);
    }

    /**
     * Tìm kiếm nhân viên theo tên
     * @param name tên nhân viên
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        log.debug("Tìm kiếm nhân viên với tên: {}", name);
        return employeeRepository.findByNameContainingAndIsDeletedFalse(name);
    }

    /**
     * Đổi mật khẩu nhân viên
     * @param employeeId ID nhân viên
     * @param newPassword mật khẩu mới
     */
    @Transactional
    public void changePassword(Integer employeeId, String newPassword) {
        log.info("Đổi mật khẩu cho nhân viên với ID: {}", employeeId);

        Employee employee = employeeRepository.findByEmployeeIdAndUser_IsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Update password in User
        User user = employee.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Đã đổi mật khẩu cho nhân viên với ID: {}", employeeId);
    }
}
