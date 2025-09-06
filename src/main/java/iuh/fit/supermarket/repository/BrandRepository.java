package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu thương hiệu
 */
@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    /**
     * Tìm thương hiệu theo tên
     * @param name tên thương hiệu
     * @return Optional<Brand>
     */
    Optional<Brand> findByName(String name);

    /**
     * Tìm thương hiệu theo mã thương hiệu
     * @param brandCode mã thương hiệu
     * @return Optional<Brand>
     */
    Optional<Brand> findByBrandCode(String brandCode);

    /**
     * Kiểm tra sự tồn tại của tên thương hiệu
     * @param name tên thương hiệu
     * @return true nếu tên đã tồn tại
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra sự tồn tại của mã thương hiệu
     * @param brandCode mã thương hiệu
     * @return true nếu mã đã tồn tại
     */
    boolean existsByBrandCode(String brandCode);

    /**
     * Kiểm tra sự tồn tại của tên thương hiệu (loại trừ thương hiệu hiện tại)
     * @param name tên thương hiệu
     * @param brandId ID thương hiệu hiện tại
     * @return true nếu tên đã tồn tại
     */
    boolean existsByNameAndBrandIdNot(String name, Integer brandId);

    /**
     * Kiểm tra sự tồn tại của mã thương hiệu (loại trừ thương hiệu hiện tại)
     * @param brandCode mã thương hiệu
     * @param brandId ID thương hiệu hiện tại
     * @return true nếu mã đã tồn tại
     */
    boolean existsByBrandCodeAndBrandIdNot(String brandCode, Integer brandId);

    /**
     * Lấy tất cả thương hiệu đang hoạt động
     * @return List<Brand>
     */
    List<Brand> findByIsActiveTrue();

    /**
     * Lấy tất cả thương hiệu đang hoạt động với phân trang
     * @param pageable thông tin phân trang
     * @return Page<Brand>
     */
    Page<Brand> findByIsActiveTrue(Pageable pageable);

    /**
     * Tìm thương hiệu theo tên chứa từ khóa
     * @param keyword từ khóa tìm kiếm
     * @return List<Brand>
     */
    @Query("SELECT b FROM Brand b WHERE b.name LIKE %:keyword% AND b.isActive = true")
    List<Brand> findByNameContaining(@Param("keyword") String keyword);

    /**
     * Tìm thương hiệu theo tên chứa từ khóa với phân trang
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<Brand>
     */
    @Query("SELECT b FROM Brand b WHERE b.name LIKE %:keyword% AND b.isActive = true")
    Page<Brand> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm mã thương hiệu lớn nhất có định dạng BR + 4 chữ số
     * @return List<String>
     */
    @Query("SELECT b.brandCode FROM Brand b WHERE b.brandCode LIKE 'BR%' ORDER BY b.brandCode DESC")
    List<String> findMaxBrandCode();
}