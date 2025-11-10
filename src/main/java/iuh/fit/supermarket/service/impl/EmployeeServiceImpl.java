package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.employee.EmployeeDto;
import iuh.fit.supermarket.dto.employee.EmployeeSearchRequest;
import iuh.fit.supermarket.dto.employee.EmployeeSearchResponse;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.UserRole;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.UserRepository;
import iuh.fit.supermarket.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation của EmployeeService
 * Service layer trả về DTO và thực hiện conversion trong transaction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Lấy nhân viên theo ID
     * @param employeeId ID nhân viên
     * @return Optional<EmployeeDto>
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeById(Integer employeeId) {
        log.debug("Lấy nhân viên với ID: {}", employeeId);
        return employeeRepository.findByEmployeeIdAndUser_IsDeletedFalse(employeeId)
                .map(EmployeeDto::fromEntity);
    }

    /**
     * Lấy nhân viên theo email
     * @param email email nhân viên
     * @return Optional<EmployeeDto>
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeDto> getEmployeeByEmail(String email) {
        log.debug("Lấy nhân viên với email: {}", email);
        // Tìm User trước, sau đó lấy Employee từ user_id
        Optional<User> user = userRepository.findByEmailAndIsDeletedFalse(email);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        return employeeRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.get().getUserId())
                .map(EmployeeDto::fromEntity);
    }

    /**
     * Lấy nhân viên theo role
     * @param role vai trò nhân viên
     * @return List<EmployeeDto>
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getEmployeesByRole(UserRole role) {
        log.debug("Lấy danh sách nhân viên với role: {}", role);
        List<Employee> employees = employeeRepository.findByUser_UserRoleAndUser_IsDeletedFalse(role);
        return employees.stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tự động sinh mã nhân viên mới theo định dạng NV000001 đến NV999999
     * @return mã nhân viên mới
     */
    @Override
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
     * @return EmployeeDto đã được tạo
     */
    @Override
    @Transactional
    public EmployeeDto createEmployee(Employee employee) {
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

        // Convert sang DTO trong transaction
        return EmployeeDto.fromEntity(savedEmployee);
    }

    /**
     * Cập nhật thông tin nhân viên
     * @param employeeId ID nhân viên
     * @param updatedEmployee thông tin cập nhật (phải có User object đã set sẵn)
     * @return EmployeeDto đã được cập nhật
     */
    @Override
    @Transactional
    public EmployeeDto updateEmployee(Integer employeeId, Employee updatedEmployee) {
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

        // Convert sang DTO trong transaction
        return EmployeeDto.fromEntity(savedEmployee);
    }

    /**
     * Xóa mềm nhân viên (soft delete)
     * @param employeeId ID nhân viên
     */
    @Override
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
    @Override
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
    @Override
    @Transactional(readOnly = true)
    public long countEmployeesByRole(UserRole role) {
        log.debug("Đếm số lượng nhân viên với role: {}", role);
        return employeeRepository.countByRoleAndIsDeletedFalse(role);
    }

    /**
     * Tìm kiếm nhân viên theo nhiều tiêu chí với phân trang
     * Hỗ trợ tìm theo: tên, email, mã nhân viên và lọc theo role
     * @param searchRequest yêu cầu tìm kiếm chứa các tiêu chí
     * @return EmployeeSearchResponse chứa danh sách nhân viên và thông tin phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeSearchResponse searchEmployees(EmployeeSearchRequest searchRequest) {
        log.debug("Tìm kiếm nhân viên với tiêu chí: keyword={}, role={}, page={}, size={}",
                searchRequest.getKeyword(), searchRequest.getRole(),
                searchRequest.getPage(), searchRequest.getSize());

        // Xử lý sort field mapping (entity field vs user field)
        String sortField = mapSortField(searchRequest.getSortBy());

        // Tạo Pageable với sorting
        Sort.Direction direction = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort
        );

        // Thực hiện search với pagination
        Page<Employee> employeePage = employeeRepository.searchEmployees(
                searchRequest.getKeyword(),
                searchRequest.getRole(),
                pageable
        );

        // Convert sang DTO trong transaction
        List<EmployeeDto> employeeDtos = employeePage.getContent().stream()
                .map(EmployeeDto::fromEntity)
                .collect(Collectors.toList());

        // Tạo response với pagination metadata
        return new EmployeeSearchResponse(
                employeeDtos,
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.hasNext(),
                employeePage.hasPrevious()
        );
    }

    /**
     * Map sort field từ DTO sang entity field
     * Một số field thuộc User entity, một số thuộc Employee entity
     */
    private String mapSortField(String sortBy) {
        if (sortBy == null) {
            return "user.createdAt";
        }

        return switch (sortBy.toLowerCase()) {
            case "name" -> "user.name";
            case "email" -> "user.email";
            case "employeecode" -> "employeeCode";
            case "createdat" -> "user.createdAt";
            case "updatedat" -> "user.updatedAt";
            default -> "user.createdAt";
        };
    }

    /**
     * Đổi mật khẩu nhân viên
     * @param employeeId ID nhân viên
     * @param newPassword mật khẩu mới
     */
    @Override
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
