package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.CustomerType;
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
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    /**
     * Tìm khách hàng theo email
     * @param email email của khách hàng
     * @return Optional<Customer>
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Tìm khách hàng theo email và chưa bị xóa
     * @param email email của khách hàng
     * @return Optional<Customer>
     */
    Optional<Customer> findByEmailAndIsDeletedFalse(String email);

    /**
     * Tìm khách hàng theo số điện thoại
     * @param phone số điện thoại của khách hàng
     * @return Optional<Customer>
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * Tìm khách hàng theo số điện thoại và chưa bị xóa
     * @param phone số điện thoại của khách hàng
     * @return Optional<Customer>
     */
    Optional<Customer> findByPhoneAndIsDeletedFalse(String phone);

    /**
     * Kiểm tra email đã tồn tại chưa
     * @param email email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra email đã tồn tại chưa (loại trừ khách hàng hiện tại)
     * @param email email cần kiểm tra
     * @param customerId ID khách hàng hiện tại
     * @return true nếu email đã tồn tại
     */
    boolean existsByEmailAndCustomerIdNot(String email, Integer customerId);

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa
     * @param phone số điện thoại cần kiểm tra
     * @return true nếu số điện thoại đã tồn tại
     */
    boolean existsByPhone(String phone);

    /**
     * Kiểm tra số điện thoại đã tồn tại chưa (loại trừ khách hàng hiện tại)
     * @param phone số điện thoại cần kiểm tra
     * @param customerId ID khách hàng hiện tại
     * @return true nếu số điện thoại đã tồn tại
     */
    boolean existsByPhoneAndCustomerIdNot(String phone, Integer customerId);

    /**
     * Tìm tất cả khách hàng chưa bị xóa
     * @return List<Customer>
     */
    List<Customer> findAllByIsDeletedFalse();

    /**
     * Tìm tất cả khách hàng chưa bị xóa với phân trang
     * @param pageable thông tin phân trang
     * @return Page<Customer>
     */
    Page<Customer> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * Tìm khách hàng theo loại khách hàng
     * @param customerType loại khách hàng
     * @return List<Customer>
     */
    List<Customer> findByCustomerTypeAndIsDeletedFalse(CustomerType customerType);

    /**
     * Tìm khách hàng theo loại khách hàng với phân trang
     * @param customerType loại khách hàng
     * @param pageable thông tin phân trang
     * @return Page<Customer>
     */
    Page<Customer> findByCustomerTypeAndIsDeletedFalse(CustomerType customerType, Pageable pageable);

    /**
     * Tìm khách hàng theo ID và chưa bị xóa
     * @param customerId ID khách hàng
     * @return Optional<Customer>
     */
    Optional<Customer> findByCustomerIdAndIsDeletedFalse(Integer customerId);

    /**
     * Tìm khách hàng theo tên (tìm kiếm gần đúng)
     * @param name tên khách hàng
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name% AND c.isDeleted = false")
    List<Customer> findByNameContainingAndIsDeletedFalse(@Param("name") String name);

    /**
     * Tìm khách hàng theo tên với phân trang
     * @param name tên khách hàng
     * @param pageable thông tin phân trang
     * @return Page<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name% AND c.isDeleted = false")
    Page<Customer> findByNameContainingAndIsDeletedFalse(@Param("name") String name, Pageable pageable);

    /**
     * Tìm khách hàng theo email (tìm kiếm gần đúng)
     * @param email email khách hàng
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.email LIKE %:email% AND c.isDeleted = false")
    List<Customer> findByEmailContainingAndIsDeletedFalse(@Param("email") String email);

    /**
     * Tìm khách hàng theo số điện thoại (tìm kiếm gần đúng)
     * @param phone số điện thoại khách hàng
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.phone LIKE %:phone% AND c.isDeleted = false")
    List<Customer> findByPhoneContainingAndIsDeletedFalse(@Param("phone") String phone);

    /**
     * Tìm kiếm khách hàng theo nhiều tiêu chí
     * @param searchTerm từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(c.name LIKE %:searchTerm% OR c.email LIKE %:searchTerm% OR c.phone LIKE %:searchTerm%) " +
           "AND c.isDeleted = false")
    Page<Customer> searchCustomers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Đếm số lượng khách hàng theo loại
     * @param customerType loại khách hàng
     * @return số lượng khách hàng
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.customerType = :customerType AND c.isDeleted = false")
    long countByCustomerTypeAndIsDeletedFalse(@Param("customerType") CustomerType customerType);

    /**
     * Tìm khách hàng sinh nhật trong khoảng thời gian
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.dateOfBirth BETWEEN :startDate AND :endDate AND c.isDeleted = false")
    List<Customer> findCustomersWithBirthdayBetween(@Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Tìm khách hàng theo địa chỉ
     * @param address địa chỉ khách hàng
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.address LIKE %:address% AND c.isDeleted = false")
    List<Customer> findByAddressContainingAndIsDeletedFalse(@Param("address") String address);

    /**
     * Tìm khách hàng VIP
     * @return List<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'VIP' AND c.isDeleted = false")
    List<Customer> findVipCustomers();

    /**
     * Tìm khách hàng VIP với phân trang
     * @param pageable thông tin phân trang
     * @return Page<Customer>
     */
    @Query("SELECT c FROM Customer c WHERE c.customerType = 'VIP' AND c.isDeleted = false")
    Page<Customer> findVipCustomers(Pageable pageable);
}
