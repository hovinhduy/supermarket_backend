package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho User entity
 * Chứa các phương thức query cơ bản cho authentication và user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email
     * @param email email của user
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm user theo email và chưa bị xóa
     * @param email email của user
     * @return Optional<User>
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Tìm user theo số điện thoại
     * @param phone số điện thoại
     * @return Optional<User>
     */
    Optional<User> findByPhone(String phone);

    /**
     * Tìm user theo số điện thoại và chưa bị xóa
     * @param phone số điện thoại
     * @return Optional<User>
     */
    Optional<User> findByPhoneAndIsDeletedFalse(String phone);

    /**
     * Tìm user theo email hoặc phone (dùng cho customer login)
     * @param email email của user
     * @param phone số điện thoại
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE (u.email = :email OR u.phone = :phone) AND u.isDeleted = false")
    Optional<User> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    /**
     * Tìm user theo user ID và chưa bị xóa
     * @param userId ID của user
     * @return Optional<User>
     */
    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra phone đã tồn tại chưa
     * @param phone số điện thoại cần kiểm tra
     * @return true nếu phone đã tồn tại
     */
    boolean existsByPhone(String phone);

    /**
     * Kiểm tra email đã tồn tại chưa (ngoại trừ user hiện tại)
     * @param email email cần kiểm tra
     * @param userId ID của user hiện tại (để loại trừ)
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmailAndUserIdNot(String email, Long userId);

    /**
     * Kiểm tra phone đã tồn tại chưa (ngoại trừ user hiện tại)
     * @param phone số điện thoại cần kiểm tra
     * @param userId ID của user hiện tại (để loại trừ)
     * @return true nếu phone đã tồn tại
     */
    boolean existsByPhoneAndUserIdNot(String phone, Long userId);

    /**
     * Lấy danh sách user theo role và chưa bị xóa
     * @param userRole vai trò của user
     * @return List<User>
     */
    List<User> findAllByUserRoleAndIsDeletedFalse(UserRole userRole);

    /**
     * Lấy danh sách tất cả user chưa bị xóa
     * @return List<User>
     */
    List<User> findAllByIsDeletedFalse();

    /**
     * Tìm user theo email và role
     * Dùng để distinguish giữa employee và customer login
     * @param email email của user
     * @param userRole vai trò của user
     * @return Optional<User>
     */
    Optional<User> findByEmailAndUserRoleAndIsDeletedFalse(String email, UserRole userRole);

    /**
     * Tìm user theo email hoặc phone và role = CUSTOMER
     * Dùng cho customer login
     * @param emailOrPhone email hoặc phone
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE " +
           "(u.email = :emailOrPhone OR u.phone = :emailOrPhone) " +
           "AND u.userRole = 'CUSTOMER' " +
           "AND u.isDeleted = false")
    Optional<User> findCustomerByEmailOrPhone(@Param("emailOrPhone") String emailOrPhone);

    /**
     * Tìm employee user theo email
     * Dùng cho employee login
     * @param email email của employee
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.email = :email " +
           "AND u.userRole IN ('ADMIN', 'MANAGER', 'STAFF') " +
           "AND u.isDeleted = false")
    Optional<User> findEmployeeByEmail(@Param("email") String email);

    /**
     * Đếm số lượng user theo role và chưa bị xóa
     * @param userRole vai trò của user
     * @return số lượng user
     */
    long countByUserRoleAndIsDeletedFalse(UserRole userRole);

    /**
     * Tìm kiếm user theo tên (chứa từ khóa)
     * @param name tên cần tìm
     * @return List<User>
     */
    List<User> findByNameContainingAndIsDeletedFalse(String name);
}
