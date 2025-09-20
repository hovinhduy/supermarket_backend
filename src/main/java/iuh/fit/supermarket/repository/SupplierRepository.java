package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho quản lý nhà cung cấp
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

       /**
        * Tìm nhà cung cấp theo mã code
        * 
        * @param code mã nhà cung cấp
        * @return Optional<Supplier>
        */
       Optional<Supplier> findByCode(String code);

       /**
        * Kiểm tra tồn tại theo mã code
        * 
        * @param code mã nhà cung cấp
        * @return true nếu tồn tại
        */
       boolean existsByCode(String code);

       /**
        * Kiểm tra tồn tại theo mã code và loại trừ ID cụ thể (dùng cho update)
        * 
        * @param code       mã nhà cung cấp
        * @param supplierId ID nhà cung cấp cần loại trừ
        * @return true nếu tồn tại
        */
       boolean existsByCodeAndSupplierIdNot(String code, Integer supplierId);

       /**
        * Lấy tất cả nhà cung cấp đang hoạt động và chưa bị xóa
        * 
        * @return List<Supplier>
        */
       List<Supplier> findByIsActiveTrueAndIsDeletedFalse();

       /**
        * Lấy tất cả nhà cung cấp chưa bị xóa với phân trang
        * 
        * @param pageable thông tin phân trang
        * @return Page<Supplier>
        */
       Page<Supplier> findByIsDeletedFalse(Pageable pageable);

       /**
        * Tìm kiếm nhà cung cấp theo từ khóa (tên, email, phone) với phân trang
        * 
        * @param keyword  từ khóa tìm kiếm
        * @param pageable thông tin phân trang
        * @return Page<Supplier>
        */
       @Query("SELECT s FROM Supplier s WHERE s.isDeleted = false AND " +
                     "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       Page<Supplier> findByKeywordWithPaging(@Param("keyword") String keyword, Pageable pageable);

       /**
        * Tìm kiếm nhà cung cấp theo từ khóa (tên, email, phone)
        * 
        * @param keyword từ khóa tìm kiếm
        * @return List<Supplier>
        */
       @Query("SELECT s FROM Supplier s WHERE s.isDeleted = false AND " +
                     "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       List<Supplier> findByKeyword(@Param("keyword") String keyword);

       /**
        * Tìm nhà cung cấp theo ID và chưa bị xóa
        * 
        * @param supplierId ID nhà cung cấp
        * @return Optional<Supplier>
        */
       Optional<Supplier> findBySupplierIdAndIsDeletedFalse(Integer supplierId);

       /**
        * Đếm số lượng phiếu nhập của nhà cung cấp
        * 
        * @param supplierId ID nhà cung cấp
        * @return số lượng phiếu nhập
        */
       @Query("SELECT COUNT(i) FROM Import i WHERE i.supplier.supplierId = :supplierId")
       Integer countImportsBySupplier(@Param("supplierId") Integer supplierId);

       /**
        * Lấy danh sách nhà cung cấp với số lượng phiếu nhập
        * 
        * @param pageable thông tin phân trang
        * @return Page<Object[]> [Supplier, importCount]
        */
       @Query("SELECT s, COUNT(i) as importCount FROM Supplier s " +
                     "LEFT JOIN s.imports i " +
                     "WHERE s.isDeleted = false " +
                     "GROUP BY s " +
                     "ORDER BY s.name")
       Page<Object[]> findSuppliersWithImportCount(Pageable pageable);

       /**
        * Tìm kiếm nhà cung cấp nâng cao với filtering
        * 
        * @param searchTerm từ khóa tìm kiếm
        * @param isActive   trạng thái hoạt động (null = tất cả)
        * @param pageable   thông tin phân trang
        * @return Page<Supplier>
        */
       @Query("SELECT s FROM Supplier s WHERE s.isDeleted = false " +
                     "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
                     "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(s.phone) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(s.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
                     "AND (:isActive IS NULL OR s.isActive = :isActive)")
       Page<Supplier> findSuppliersAdvanced(@Param("searchTerm") String searchTerm,
                     @Param("isActive") Boolean isActive,
                     Pageable pageable);
}
