package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Employee entity
 * Các query liên quan đến email, name, role hiện join với bảng User
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    /**
     * Tìm nhân viên theo user_id
     *
     * @param userId ID của user
     * @return Optional<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findByUser_UserId(Long userId);

    /**
     * Tìm nhân viên theo user_id và user chưa bị xóa
     *
     * @param userId ID của user
     * @return Optional<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findByUser_UserIdAndUser_IsDeletedFalse(Long userId);

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
     * Tìm tất cả nhân viên mà user chưa bị xóa
     *
     * @return List<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    List<Employee> findAllByUser_IsDeletedFalse();

    /**
     * Tìm nhân viên theo role (từ User.userRole)
     *
     * @param userRole vai trò nhân viên (ADMIN, MANAGER, STAFF)
     * @return List<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    List<Employee> findByUser_UserRoleAndUser_IsDeletedFalse(UserRole userRole);

    /**
     * Tìm nhân viên theo ID và user chưa bị xóa
     *
     * @param employeeId ID nhân viên
     * @return Optional<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<Employee> findByEmployeeIdAndUser_IsDeletedFalse(Integer employeeId);

    /**
     * Đếm số lượng nhân viên theo role (từ User.userRole)
     *
     * @param userRole vai trò nhân viên
     * @return số lượng nhân viên
     */
    @Query("SELECT COUNT(e) FROM Employee e JOIN e.user u WHERE u.userRole = :userRole AND u.isDeleted = false")
    long countByRoleAndIsDeletedFalse(@Param("userRole") UserRole userRole);

    /**
     * Tìm nhân viên theo tên (join với User)
     *
     * @param name tên nhân viên
     * @return List<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT e FROM Employee e JOIN e.user u WHERE u.name LIKE %:name% AND u.isDeleted = false")
    List<Employee> findByNameContainingAndIsDeletedFalse(@Param("name") String name);

    /**
     * Tìm kiếm nhân viên động với nhiều tiêu chí và phân trang
     * Tìm theo keyword (trong name, email, employeeCode) và lọc theo role
     * CHỈ tìm nhân viên có role: ADMIN, MANAGER, STAFF (loại bỏ CUSTOMER)
     *
     * @param keyword từ khóa tìm kiếm (có thể null)
     * @param role vai trò nhân viên (có thể null)
     * @param pageable thông tin phân trang và sắp xếp
     * @return Page<Employee>
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT e FROM Employee e JOIN e.user u " +
           "WHERE u.isDeleted = false " +
           "AND u.userRole IN ('ADMIN', 'MANAGER', 'STAFF') " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "    LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "    LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "    LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.userRole = :role)")
    Page<Employee> searchEmployees(@Param("keyword") String keyword,
                                    @Param("role") UserRole role,
                                    Pageable pageable);
}
