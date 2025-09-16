package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho quản lý kho hàng
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    /**
     * Tìm kho hàng theo tên
     * 
     * @param name tên kho hàng
     * @return thông tin kho hàng
     */
    Optional<Warehouse> findByName(String name);

    /**
     * Lấy danh sách kho hàng đang hoạt động
     * Tạm thời trả về tất cả vì entity chưa có trường isActive
     * 
     * @return danh sách kho hàng
     */
    List<Warehouse> findAll();
}
