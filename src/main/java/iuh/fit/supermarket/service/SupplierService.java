package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteRequest;
import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteResponse;
import iuh.fit.supermarket.dto.supplier.SupplierCreateRequest;
import iuh.fit.supermarket.dto.supplier.SupplierPageableRequest;
import iuh.fit.supermarket.dto.supplier.SupplierResponse;
import iuh.fit.supermarket.dto.supplier.SupplierUpdateRequest;
import iuh.fit.supermarket.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho quản lý nhà cung cấp
 */
public interface SupplierService {

    /**
     * Tạo mới nhà cung cấp
     * 
     * @param request thông tin nhà cung cấp cần tạo
     * @return SupplierResponse thông tin nhà cung cấp đã tạo
     */
    SupplierResponse createSupplier(SupplierCreateRequest request);

    /**
     * Cập nhật thông tin nhà cung cấp
     * 
     * @param supplierId ID nhà cung cấp cần cập nhật
     * @param request    thông tin cập nhật
     * @return SupplierResponse thông tin nhà cung cấp đã cập nhật
     */
    SupplierResponse updateSupplier(Integer supplierId, SupplierUpdateRequest request);

    /**
     * Xóa nhà cung cấp (soft delete)
     * 
     * @param supplierId ID nhà cung cấp cần xóa
     */
    void deleteSupplier(Integer supplierId);

    /**
     * Lấy thông tin nhà cung cấp theo ID
     * 
     * @param supplierId ID nhà cung cấp
     * @return SupplierResponse thông tin nhà cung cấp
     */
    SupplierResponse getSupplierById(Integer supplierId);

    /**
     * Lấy danh sách tất cả nhà cung cấp đang hoạt động
     * 
     * @return List<SupplierResponse> danh sách nhà cung cấp
     */
    List<SupplierResponse> getAllActiveSuppliers();

    /**
     * Lấy danh sách nhà cung cấp với phân trang
     *
     * @param pageable thông tin phân trang
     * @return Page<SupplierResponse> danh sách nhà cung cấp có phân trang
     */
    Page<SupplierResponse> getAllSuppliers(Pageable pageable);

    /**
     * Lấy danh sách nhà cung cấp với filtering, searching và sorting nâng cao
     * 
     * @param request thông tin phân trang, filtering và sorting
     * @return Page<SupplierResponse> danh sách nhà cung cấp
     */
    Page<SupplierResponse> getSuppliersAdvanced(SupplierPageableRequest request);

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @return List<SupplierResponse> danh sách nhà cung cấp tìm được
     */
    List<SupplierResponse> searchSuppliers(String keyword);

    /**
     * Tìm kiếm nhà cung cấp theo từ khóa với phân trang
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<SupplierResponse> danh sách nhà cung cấp tìm được có phân trang
     */
    Page<SupplierResponse> searchSuppliers(String keyword, Pageable pageable);

    /**
     * Kiểm tra nhà cung cấp có tồn tại không
     * 
     * @param supplierId ID nhà cung cấp
     * @return true nếu tồn tại
     */
    boolean existsById(Integer supplierId);

    /**
     * Lấy entity Supplier theo ID (dùng nội bộ)
     *
     * @param supplierId ID nhà cung cấp
     * @return Supplier entity
     */
    Supplier findSupplierEntityById(Integer supplierId);

    /**
     * Xóa nhiều nhà cung cấp cùng lúc (soft delete)
     *
     * @param request danh sách ID nhà cung cấp cần xóa
     * @return SupplierBatchDeleteResponse kết quả xóa
     */
    SupplierBatchDeleteResponse batchDeleteSuppliers(SupplierBatchDeleteRequest request);
}
