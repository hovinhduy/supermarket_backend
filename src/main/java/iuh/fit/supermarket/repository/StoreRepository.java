package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Store
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * Tìm cửa hàng theo mã
     */
    Optional<Store> findByStoreCode(String storeCode);

    /**
     * Lấy danh sách cửa hàng đang hoạt động
     */
    List<Store> findByIsActiveTrue();

    /**
     * Kiểm tra cửa hàng tồn tại theo mã
     */
    boolean existsByStoreCode(String storeCode);
}
