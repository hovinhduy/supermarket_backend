package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho Customer entity
 * Các query liên quan đến email, phone, name hiện join với bảng User
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

        /**
         * Tìm khách hàng theo user_id
         *
         * @param userId ID của user
         * @return Optional<Customer>
         */
        Optional<Customer> findByUser_UserId(Long userId);

        /**
         * Tìm khách hàng theo user_id và user chưa bị xóa
         *
         * @param userId ID của user
         * @return Optional<Customer>
         */
        Optional<Customer> findByUser_UserIdAndUser_IsDeletedFalse(Long userId);

        /**
         * Kiểm tra mã khách hàng đã tồn tại chưa
         * 
         * @param customerCode mã khách hàng cần kiểm tra
         * @return true nếu mã khách hàng đã tồn tại
         */
        boolean existsByCustomerCode(String customerCode);

        /**
         * Tìm khách hàng có mã lớn nhất để sinh mã mới
         * 
         * @return Optional<Customer>
         */
        /**
         * Tìm khách hàng có mã lớn nhất để sinh mã mới
         * 
         * @return Optional<Customer>
         */
        Optional<Customer> findTopByCustomerCodeIsNotNullOrderByCustomerCodeDesc();

        /**
         * Tìm tất cả khách hàng mà user chưa bị xóa
         *
         * @return List<Customer>
         */
        List<Customer> findAllByUser_IsDeletedFalse();

        /**
         * Tìm tất cả khách hàng mà user chưa bị xóa với phân trang
         *
         * @param pageable thông tin phân trang
         * @return Page<Customer>
         */
        Page<Customer> findAllByUser_IsDeletedFalse(Pageable pageable);

        /**
         * Tìm khách hàng theo loại khách hàng và user chưa bị xóa
         *
         * @param customerType loại khách hàng
         * @return List<Customer>
         */
        List<Customer> findByCustomerTypeAndUser_IsDeletedFalse(CustomerType customerType);

        /**
         * Tìm khách hàng theo loại khách hàng với phân trang và user chưa bị xóa
         *
         * @param customerType loại khách hàng
         * @param pageable     thông tin phân trang
         * @return Page<Customer>
         */
        Page<Customer> findByCustomerTypeAndUser_IsDeletedFalse(CustomerType customerType, Pageable pageable);

        /**
         * Tìm khách hàng theo ID và user chưa bị xóa
         *
         * @param customerId ID khách hàng
         * @return Optional<Customer>
         */
        Optional<Customer> findByCustomerIdAndUser_IsDeletedFalse(Integer customerId);

        /**
         * Tìm khách hàng theo tên (join với User)
         *
         * @param name tên khách hàng
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE u.name LIKE %:name% AND u.isDeleted = false")
        List<Customer> findByNameContainingAndIsDeletedFalse(@Param("name") String name);

        /**
         * Tìm khách hàng theo tên với phân trang (join với User)
         *
         * @param name     tên khách hàng
         * @param pageable thông tin phân trang
         * @return Page<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE u.name LIKE %:name% AND u.isDeleted = false")
        Page<Customer> findByNameContainingAndIsDeletedFalse(@Param("name") String name, Pageable pageable);

        /**
         * Tìm khách hàng theo email (join với User)
         *
         * @param email email khách hàng
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE u.email LIKE %:email% AND u.isDeleted = false")
        List<Customer> findByEmailContainingAndIsDeletedFalse(@Param("email") String email);

        /**
         * Tìm khách hàng theo số điện thoại (join với User)
         *
         * @param phone số điện thoại khách hàng
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE u.phone LIKE %:phone% AND u.isDeleted = false")
        List<Customer> findByPhoneContainingAndIsDeletedFalse(@Param("phone") String phone);

        /**
         * Tìm kiếm khách hàng theo nhiều tiêu chí (join với User)
         *
         * @param searchTerm từ khóa tìm kiếm
         * @param pageable   thông tin phân trang
         * @return Page<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE " +
                        "(u.name LIKE %:searchTerm% OR u.email LIKE %:searchTerm% OR u.phone LIKE %:searchTerm%) " +
                        "AND u.isDeleted = false")
        Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

        /**
         * Đếm số lượng khách hàng theo loại (join với User)
         *
         * @param customerType loại khách hàng
         * @return số lượng khách hàng
         */
        @Query("SELECT COUNT(c) FROM Customer c JOIN c.user u WHERE c.customerType = :customerType AND u.isDeleted = false")
        long countByCustomerTypeAndIsDeletedFalse(@Param("customerType") CustomerType customerType);

        /**
         * Tìm khách hàng sinh nhật trong khoảng thời gian (join với User)
         *
         * @param startDate ngày bắt đầu
         * @param endDate   ngày kết thúc
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE u.dateOfBirth BETWEEN :startDate AND :endDate AND u.isDeleted = false")
        List<Customer> findCustomersWithBirthdayBetween(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        /**
         * Tìm khách hàng theo địa chỉ
         *
         * @param address địa chỉ khách hàng
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE c.address LIKE %:address% AND u.isDeleted = false")
        List<Customer> findByAddressContainingAndIsDeletedFalse(@Param("address") String address);

        /**
         * Tìm khách hàng VIP
         *
         * @return List<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE c.customerType = 'VIP' AND u.isDeleted = false")
        List<Customer> findVipCustomers();

        /**
         * Tìm khách hàng VIP với phân trang
         *
         * @param pageable thông tin phân trang
         * @return Page<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE c.customerType = 'VIP' AND u.isDeleted = false")
        Page<Customer> findVipCustomers(Pageable pageable);

        /**
         * Tìm kiếm khách hàng nâng cao với nhiều tiêu chí tùy chọn (join với User)
         *
         * @param searchTerm   từ khóa tìm kiếm (tìm trong tên, email, số điện thoại từ
         *                     User)
         * @param gender       giới tính (có thể null - từ User)
         * @param customerType loại khách hàng (có thể null)
         * @param pageable     thông tin phân trang
         * @return Page<Customer>
         */
        @Query("SELECT c FROM Customer c JOIN c.user u WHERE " +
                        "u.isDeleted = false " +
                        "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
                        "     LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "     u.phone LIKE CONCAT('%', :searchTerm, '%')) " +
                        "AND (:gender IS NULL OR u.gender = :gender) " +
                        "AND (:customerType IS NULL OR c.customerType = :customerType)")
        Page<Customer> searchCustomersAdvanced(
                        @Param("searchTerm") String searchTerm,
                        @Param("gender") Gender gender,
                        @Param("customerType") CustomerType customerType,
                        Pageable pageable);
}
