package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho quản lý phiếu nhập hàng
 */
@Repository
public interface ImportRepository extends JpaRepository<Import, Integer> {

       /**
        * Tìm phiếu nhập theo mã phiếu nhập
        * 
        * @param importCode mã phiếu nhập
        * @return Optional<Import>
        */
       Optional<Import> findByImportCode(String importCode);

       /**
        * Kiểm tra tồn tại theo mã phiếu nhập
        * 
        * @param importCode mã phiếu nhập
        * @return true nếu tồn tại
        */
       boolean existsByImportCode(String importCode);

       /**
        * Lấy danh sách phiếu nhập theo nhà cung cấp
        * 
        * @param supplierId ID nhà cung cấp
        * @return List<Import>
        */
       @Query("SELECT i FROM Import i WHERE i.supplier.supplierId = :supplierId ORDER BY i.importDate DESC")
       List<Import> findBySupplier(@Param("supplierId") Integer supplierId);

       /**
        * Lấy danh sách phiếu nhập theo nhà cung cấp với phân trang
        * 
        * @param supplierId ID nhà cung cấp
        * @param pageable   thông tin phân trang
        * @return Page<Import>
        */
       @Query("SELECT i FROM Import i WHERE i.supplier.supplierId = :supplierId ORDER BY i.importDate DESC")
       Page<Import> findBySupplier(@Param("supplierId") Integer supplierId, Pageable pageable);

       /**
        * Lấy danh sách phiếu nhập theo khoảng thời gian
        * 
        * @param startDate ngày bắt đầu
        * @param endDate   ngày kết thúc
        * @return List<Import>
        */
       @Query("SELECT i FROM Import i WHERE i.importDate BETWEEN :startDate AND :endDate ORDER BY i.importDate DESC")
       List<Import> findByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Lấy danh sách phiếu nhập theo khoảng thời gian với phân trang
        * 
        * @param startDate ngày bắt đầu
        * @param endDate   ngày kết thúc
        * @param pageable  thông tin phân trang
        * @return Page<Import>
        */
       @Query("SELECT i FROM Import i WHERE i.importDate BETWEEN :startDate AND :endDate ORDER BY i.importDate DESC")
       Page<Import> findByDateRange(@Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate, Pageable pageable);

       /**
        * Tìm kiếm phiếu nhập theo từ khóa (mã phiếu, tên nhà cung cấp, ghi chú)
        * 
        * @param keyword từ khóa tìm kiếm
        * @return List<Import>
        */
       @Query("SELECT i FROM Import i WHERE " +
                     "LOWER(i.importCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.supplier.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       List<Import> findByKeyword(@Param("keyword") String keyword);

       /**
        * Tìm kiếm phiếu nhập theo từ khóa với phân trang
        * 
        * @param keyword  từ khóa tìm kiếm
        * @param pageable thông tin phân trang
        * @return Page<Import>
        */
       @Query("SELECT i FROM Import i WHERE " +
                     "LOWER(i.importCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.supplier.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
       Page<Import> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

       /**
        * Lấy danh sách phiếu nhập theo nhân viên tạo
        * 
        * @param employeeId ID nhân viên
        * @return List<Import>
        */
       @Query("SELECT i FROM Import i WHERE i.createdBy.employeeId = :employeeId ORDER BY i.importDate DESC")
       List<Import> findByCreatedBy(@Param("employeeId") Integer employeeId);

       /**
        * Đếm số lượng phiếu nhập theo nhà cung cấp
        * 
        * @param supplierId ID nhà cung cấp
        * @return số lượng phiếu nhập
        */
       @Query("SELECT COUNT(i) FROM Import i WHERE i.supplier.supplierId = :supplierId")
       Long countBySupplier(@Param("supplierId") Integer supplierId);

       /**
        * Lấy phiếu nhập gần nhất
        * 
        * @return Optional<Import>
        */
       @Query("SELECT i FROM Import i ORDER BY i.createdAt DESC")
       Optional<Import> findLatestImport();

       /**
        * Lấy tất cả phiếu nhập với thông tin chi tiết (để tránh N+1 query)
        *
        * @param pageable thông tin phân trang
        * @return Page<Import>
        */
       @Query("SELECT DISTINCT i FROM Import i " +
                     "LEFT JOIN FETCH i.supplier " +
                     "LEFT JOIN FETCH i.createdBy " +
                     "LEFT JOIN FETCH i.importDetails")
       Page<Import> findAllWithDetails(Pageable pageable);

       /**
        * Tìm mã phiếu nhập lớn nhất có định dạng PN + 6 chữ số
        *
        * @return mã phiếu nhập lớn nhất hoặc null nếu không có
        */
       @Query("SELECT i.importCode FROM Import i " +
                     "WHERE i.importCode LIKE 'PN%' " +
                     "AND LENGTH(i.importCode) = 8 " +
                     "ORDER BY i.importCode DESC")
       List<String> findMaxImportCodeWithPNFormat();

}
