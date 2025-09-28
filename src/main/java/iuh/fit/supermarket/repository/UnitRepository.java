package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu đơn vị tính
 * Cung cấp các phương thức CRUD và truy vấn cho entity Unit
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /**
     * Tìm đơn vị tính theo tên
     *
     * @param name tên đơn vị tính
     * @return đơn vị tính nếu tìm thấy
     */
    Optional<Unit> findByName(String name);

    /**
     * Tìm đơn vị tính theo tên (không phân biệt chữ hoa/thường)
     *
     * @param name tên đơn vị tính
     * @return đơn vị tính nếu tìm thấy
     */
    Optional<Unit> findByNameIgnoreCase(String name);

    /**
     * Kiểm tra sự tồn tại của tên đơn vị tính
     *
     * @param name tên đơn vị tính
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByName(String name);

    /**
     * Lấy danh sách đơn vị tính hoạt động
     *
     * @return danh sách đơn vị tính hoạt động
     */
    @Query("SELECT u FROM Unit u WHERE u.isActive = true AND u.isDeleted = false ORDER BY u.name ASC")
    List<Unit> findActiveUnits();

    /**
     * Tìm kiếm đơn vị tính theo từ khóa
     *
     * @param keyword  từ khóa tìm kiếm (tên)
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    @Query("SELECT u FROM Unit u WHERE u.name LIKE %:keyword% AND u.isDeleted = false ORDER BY u.name ASC")
    Page<Unit> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy danh sách tất cả đơn vị tính (không bao gồm đã xóa)
     *
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    @Query("SELECT u FROM Unit u WHERE u.isDeleted = false ORDER BY u.name ASC")
    Page<Unit> findAllNotDeleted(Pageable pageable);

    /**
     * Lấy danh sách đơn vị tính theo trạng thái hoạt động
     *
     * @param isActive trạng thái hoạt động
     * @param pageable thông tin phân trang
     * @return danh sách đơn vị tính phân trang
     */
    @Query("SELECT u FROM Unit u WHERE u.isActive = :isActive AND u.isDeleted = false ORDER BY u.name ASC")
    Page<Unit> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);

    /**
     * Đếm số lượng đơn vị tính hoạt động
     *
     * @return số lượng đơn vị tính hoạt động
     */
    @Query("SELECT COUNT(u) FROM Unit u WHERE u.isActive = true AND u.isDeleted = false")
    Long countActiveUnits();

    /**
     * Tìm đơn vị tính có thể xóa (không được sử dụng bởi ProductUnit nào)
     * 
     * @return danh sách đơn vị tính có thể xóa
     */
    @Query("SELECT u FROM Unit u WHERE u.id NOT IN " +
            "(SELECT DISTINCT pu.unit.id FROM ProductUnit pu WHERE pu.unit.id IS NOT NULL) " +
            "AND u.isDeleted = false")
    List<Unit> findDeletableUnits();

    /**
     * Kiểm tra đơn vị tính có đang được sử dụng không
     * 
     * @param unitId ID đơn vị tính
     * @return true nếu đang được sử dụng, false nếu không
     */
    @Query("SELECT COUNT(pu) > 0 FROM ProductUnit pu WHERE pu.unit.id = :unitId")
    boolean isUnitInUse(@Param("unitId") Long unitId);
}
