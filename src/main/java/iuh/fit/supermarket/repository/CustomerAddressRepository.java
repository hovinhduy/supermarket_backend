package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho CustomerAddress entity
 */
@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

    /**
     * Tìm tất cả địa chỉ của khách hàng chưa bị xóa
     *
     * @param customerId ID khách hàng
     * @return danh sách địa chỉ
     */
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customer.customerId = :customerId AND ca.isDeleted = false ORDER BY ca.isDefault DESC, ca.createdAt DESC")
    List<CustomerAddress> findAllByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Tìm địa chỉ theo ID và chưa bị xóa
     *
     * @param addressId ID địa chỉ
     * @return địa chỉ
     */
    Optional<CustomerAddress> findByAddressIdAndIsDeletedFalse(Long addressId);

    /**
     * Tìm địa chỉ theo ID, customer ID và chưa bị xóa
     *
     * @param addressId  ID địa chỉ
     * @param customerId ID khách hàng
     * @return địa chỉ
     */
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.addressId = :addressId AND ca.customer.customerId = :customerId AND ca.isDeleted = false")
    Optional<CustomerAddress> findByAddressIdAndCustomerId(@Param("addressId") Long addressId, @Param("customerId") Integer customerId);

    /**
     * Tìm địa chỉ mặc định của khách hàng
     *
     * @param customerId ID khách hàng
     * @return địa chỉ mặc định
     */
    @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customer.customerId = :customerId AND ca.isDefault = true AND ca.isDeleted = false")
    Optional<CustomerAddress> findDefaultAddressByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Đếm số địa chỉ của khách hàng chưa bị xóa
     *
     * @param customerId ID khách hàng
     * @return số địa chỉ
     */
    @Query("SELECT COUNT(ca) FROM CustomerAddress ca WHERE ca.customer.customerId = :customerId AND ca.isDeleted = false")
    long countByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Bỏ đánh dấu mặc định cho tất cả địa chỉ của khách hàng
     *
     * @param customerId ID khách hàng
     */
    @Modifying
    @Query("UPDATE CustomerAddress ca SET ca.isDefault = false WHERE ca.customer.customerId = :customerId")
    void clearDefaultAddresses(@Param("customerId") Integer customerId);
}
