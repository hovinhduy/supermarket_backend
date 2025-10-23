package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Employee entity
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    /**
     * Tìm nhân viên theo email
     * @param email email của nhân viên
     * @return Optional<Employee>
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Tìm nhân viên theo email và chưa bị xóa
     * @param email email của nhân viên
     * @return Optional<Employee>
     */
    Optional<Employee> findByEmailAndIsDeletedFalse(String email);

    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại chưa (loại trừ nhân viên hiện tại)
     * @param email email cần kiểm tra
     * @param employeeId ID nhân viên hiện tại
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmailAndEmployeeIdNot(String email, Integer employeeId);

    /**
     * Kiểm tra mã nhân viên đã tồn tại chưa
     * @param employeeCode mã nhân viên cần kiểm tra
     * @return true nếu mã nhân viên đã tồn tại
     */
    boolean existsByEmployeeCode(String employeeCode);

    /**
     * Tìm nhân viên có mã lớn nhất để sinh mã mới
     * @return Optional<Employee>
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeCode IS NOT NULL ORDER BY e.employeeCode DESC")
    Optional<Employee> findTopByOrderByEmployeeCodeDesc();

    /**
     * Tìm tất cả nhân viên chưa bị xóa
     * @return List<Employee>
     */
    List<Employee> findAllByIsDeletedFalse();

    /**
     * Tìm nhân viên theo role
     * @param role vai trò nhân viên
     * @return List<Employee>
     */
    List<Employee> findByRoleAndIsDeletedFalse(EmployeeRole role);

    /**
     * Tìm nhân viên theo ID và chưa bị xóa
     * @param employeeId ID nhân viên
     * @return Optional<Employee>
     */
    Optional<Employee> findByEmployeeIdAndIsDeletedFalse(Integer employeeId);

    /**
     * Đếm số lượng nhân viên theo role
     * @param role vai trò nhân viên
     * @return số lượng nhân viên
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.role = :role AND e.isDeleted = false")
    long countByRoleAndIsDeletedFalse(@Param("role") EmployeeRole role);

    /**
     * Tìm nhân viên theo tên (tìm kiếm gần đúng)
     * @param name tên nhân viên
     * @return List<Employee>
     */
    @Query("SELECT e FROM Employee e WHERE e.name LIKE %:name% AND e.isDeleted = false")
    List<Employee> findByNameContainingAndIsDeletedFalse(@Param("name") String name);
}
