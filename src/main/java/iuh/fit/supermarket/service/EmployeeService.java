package iuh.fit.supermarket.service;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.EmployeeRole;
import iuh.fit.supermarket.repository.EmployeeRepository;
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
    private final PasswordEncoder passwordEncoder;

    /**
     * Lấy tất cả nhân viên chưa bị xóa
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        log.debug("Lấy danh sách tất cả nhân viên");
        return employeeRepository.findAllByIsDeletedFalse();
    }

    /**
     * Lấy nhân viên theo ID
     * @param employeeId ID nhân viên
     * @return Optional<Employee>
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Integer employeeId) {
        log.debug("Lấy nhân viên với ID: {}", employeeId);
        return employeeRepository.findByEmployeeIdAndIsDeletedFalse(employeeId);
    }

    /**
     * Lấy nhân viên theo email
     * @param email email nhân viên
     * @return Optional<Employee>
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmail(String email) {
        log.debug("Lấy nhân viên với email: {}", email);
        return employeeRepository.findByEmailAndIsDeletedFalse(email);
    }

    /**
     * Lấy nhân viên theo role
     * @param role vai trò nhân viên
     * @return List<Employee>
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByRole(EmployeeRole role) {
        log.debug("Lấy danh sách nhân viên với role: {}", role);
        return employeeRepository.findByRoleAndIsDeletedFalse(role);
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
     * @param employee thông tin nhân viên
     * @return Employee đã được tạo
     */
    @Transactional
    public Employee createEmployee(Employee employee) {
        log.info("Tạo nhân viên mới với email: {}", employee.getEmail());
        
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
        
        // Kiểm tra email đã tồn tại chưa
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + employee.getEmail());
        }
        
        // Mã hóa password
        if (employee.getPasswordHash() != null) {
            employee.setPasswordHash(passwordEncoder.encode(employee.getPasswordHash()));
        }
        
        // Set default values
        if (employee.getIsDeleted() == null) {
            employee.setIsDeleted(false);
        }
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Đã tạo nhân viên mới với ID: {}", savedEmployee.getEmployeeId());
        
        return savedEmployee;
    }

    /**
     * Cập nhật thông tin nhân viên
     * @param employeeId ID nhân viên
     * @param updatedEmployee thông tin cập nhật
     * @return Employee đã được cập nhật
     */
    @Transactional
    public Employee updateEmployee(Integer employeeId, Employee updatedEmployee) {
        log.info("Cập nhật nhân viên với ID: {}", employeeId);
        
        Employee existingEmployee = employeeRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        // Kiểm tra email đã tồn tại chưa (ngoại trừ nhân viên hiện tại)
        if (!existingEmployee.getEmail().equals(updatedEmployee.getEmail()) &&
            employeeRepository.existsByEmailAndEmployeeIdNot(updatedEmployee.getEmail(), employeeId)) {
            throw new IllegalArgumentException("Email đã tồn tại: " + updatedEmployee.getEmail());
        }
        
        // Cập nhật thông tin
        existingEmployee.setName(updatedEmployee.getName());
        existingEmployee.setEmail(updatedEmployee.getEmail());
        existingEmployee.setRole(updatedEmployee.getRole());
        
        // Cập nhật password nếu có
        if (updatedEmployee.getPasswordHash() != null && !updatedEmployee.getPasswordHash().isEmpty()) {
            existingEmployee.setPasswordHash(passwordEncoder.encode(updatedEmployee.getPasswordHash()));
        }
        
        Employee savedEmployee = employeeRepository.save(existingEmployee);
        log.info("Đã cập nhật nhân viên với ID: {}", savedEmployee.getEmployeeId());
        
        return savedEmployee;
    }

    /**
     * Xóa mềm nhân viên
     * @param employeeId ID nhân viên
     */
    @Transactional
    public void deleteEmployee(Integer employeeId) {
        log.info("Xóa nhân viên với ID: {}", employeeId);
        
        Employee employee = employeeRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        employee.setIsDeleted(true);
        employeeRepository.save(employee);
        
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
        
        employee.setIsDeleted(false);
        employeeRepository.save(employee);
        
        log.info("Đã khôi phục nhân viên với ID: {}", employeeId);
    }

    /**
     * Đếm số lượng nhân viên theo role
     * @param role vai trò nhân viên
     * @return số lượng nhân viên
     */
    @Transactional(readOnly = true)
    public long countEmployeesByRole(EmployeeRole role) {
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
        
        Employee employee = employeeRepository.findByEmployeeIdAndIsDeletedFalse(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));
        
        employee.setPasswordHash(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
        
        log.info("Đã đổi mật khẩu cho nhân viên với ID: {}", employeeId);
    }
}
