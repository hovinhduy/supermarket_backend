package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.employee.EmployeeDto;
import iuh.fit.supermarket.dto.employee.EmployeeSearchRequest;
import iuh.fit.supermarket.dto.employee.EmployeeSearchResponse;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Service interface để xử lý business logic cho Employee
 * Service layer trả về DTO, không trả về Entity
 */
public interface EmployeeService {

    /**
     * Lấy nhân viên theo ID
     * @param employeeId ID nhân viên
     * @return Optional<EmployeeDto>
     */
    Optional<EmployeeDto> getEmployeeById(Integer employeeId);

    /**
     * Lấy nhân viên theo email
     * @param email email nhân viên
     * @return Optional<EmployeeDto>
     */
    Optional<EmployeeDto> getEmployeeByEmail(String email);

    /**
     * Lấy nhân viên theo role
     * @param role vai trò nhân viên
     * @return List<EmployeeDto>
     */
    List<EmployeeDto> getEmployeesByRole(UserRole role);

    /**
     * Tự động sinh mã nhân viên mới theo định dạng NV000001 đến NV999999
     * @return mã nhân viên mới
     */
    String generateEmployeeCode();

    /**
     * Tạo nhân viên mới
     * @param employee thông tin nhân viên (phải có User object đã set sẵn)
     * @return EmployeeDto đã được tạo
     */
    EmployeeDto createEmployee(Employee employee);

    /**
     * Cập nhật thông tin nhân viên
     * @param employeeId ID nhân viên
     * @param updatedEmployee thông tin cập nhật (phải có User object đã set sẵn)
     * @return EmployeeDto đã được cập nhật
     */
    EmployeeDto updateEmployee(Integer employeeId, Employee updatedEmployee);

    /**
     * Xóa mềm nhân viên (soft delete)
     * @param employeeId ID nhân viên
     */
    void deleteEmployee(Integer employeeId);

    /**
     * Khôi phục nhân viên đã bị xóa
     * @param employeeId ID nhân viên
     */
    void restoreEmployee(Integer employeeId);

    /**
     * Đếm số lượng nhân viên theo role
     * @param role vai trò nhân viên
     * @return số lượng nhân viên
     */
    long countEmployeesByRole(UserRole role);

    /**
     * Tìm kiếm nhân viên theo nhiều tiêu chí với phân trang
     * Hỗ trợ tìm theo: tên, email, mã nhân viên và lọc theo role
     * @param searchRequest yêu cầu tìm kiếm chứa các tiêu chí
     * @return EmployeeSearchResponse chứa danh sách nhân viên và thông tin phân trang
     */
    EmployeeSearchResponse searchEmployees(EmployeeSearchRequest searchRequest);

    /**
     * Đổi mật khẩu nhân viên
     * @param employeeId ID nhân viên
     * @param newPassword mật khẩu mới
     */
    void changePassword(Integer employeeId, String newPassword);
}
