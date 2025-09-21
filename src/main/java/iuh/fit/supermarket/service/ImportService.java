package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.imports.ImportCreateRequest;
import iuh.fit.supermarket.dto.imports.ImportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho quản lý phiếu nhập hàng
 */
public interface ImportService {

    /**
     * Tạo phiếu nhập hàng mới
     * Xử lý nhập hàng theo từng biến thể sản phẩm:
     * - Kiểm tra biến thể có tồn tại và active không
     * - Cập nhật tồn kho trong Warehouse (tạo mới hoặc cập nhật)
     * - Ghi nhận giao dịch vào WarehouseTransaction
     * 
     * @param request    thông tin phiếu nhập hàng
     * @param employeeId ID nhân viên tạo phiếu
     * @return ImportResponse thông tin phiếu nhập đã tạo
     * @throws IllegalArgumentException nếu dữ liệu đầu vào không hợp lệ
     * @throws RuntimeException         nếu có lỗi trong quá trình xử lý
     */
    ImportResponse createImport(ImportCreateRequest request, Integer employeeId);

    /**
     * Lấy thông tin phiếu nhập theo ID
     * 
     * @param importId ID phiếu nhập
     * @return ImportResponse thông tin phiếu nhập
     * @throws IllegalArgumentException nếu không tìm thấy phiếu nhập
     */
    ImportResponse getImportById(Integer importId);

    /**
     * Lấy danh sách tất cả phiếu nhập với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return Page<ImportResponse> danh sách phiếu nhập
     */
    Page<ImportResponse> getAllImports(Pageable pageable);

    /**
     * Lấy danh sách phiếu nhập theo nhà cung cấp
     * 
     * @param supplierId ID nhà cung cấp
     * @param pageable   thông tin phân trang
     * @return Page<ImportResponse> danh sách phiếu nhập
     */
    Page<ImportResponse> getImportsBySupplier(Integer supplierId, Pageable pageable);

    /**
     * Lấy danh sách phiếu nhập theo khoảng thời gian
     * 
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @param pageable  thông tin phân trang
     * @return Page<ImportResponse> danh sách phiếu nhập
     */
    Page<ImportResponse> getImportsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Tìm kiếm phiếu nhập theo từ khóa
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<ImportResponse> danh sách phiếu nhập
     */
    Page<ImportResponse> searchImports(String keyword, Pageable pageable);

    /**
     * Lấy danh sách phiếu nhập theo nhân viên tạo
     * 
     * @param employeeId ID nhân viên
     * @return List<ImportResponse> danh sách phiếu nhập
     */
    List<ImportResponse> getImportsByEmployee(Integer employeeId);

    /**
     * Kiểm tra phiếu nhập có tồn tại không
     * 
     * @param importId ID phiếu nhập
     * @return true nếu tồn tại
     */
    boolean existsById(Integer importId);

    /**
     * Kiểm tra mã phiếu nhập có tồn tại không
     * 
     * @param importCode mã phiếu nhập
     * @return true nếu tồn tại
     */
    boolean existsByImportCode(String importCode);

    /**
     * Tạo mã phiếu nhập tự động
     * Format mới: PN + 6 chữ số (PN000001 đến PN999999)
     * Ví dụ: PN000001, PN000002, ..., PN999999
     *
     * @return mã phiếu nhập mới
     * @throws ImportCodeOverflowException nếu đạt tới giới hạn tối đa
     */
    String generateImportCode();

    /**
     * Lấy thống kê nhập hàng theo nhà cung cấp
     * 
     * @param supplierId ID nhà cung cấp
     * @return thống kê nhập hàng
     */
    ImportStatistics getImportStatisticsBySupplier(Integer supplierId);

    /**
     * Lấy thống kê nhập hàng theo khoảng thời gian
     * 
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @return thống kê nhập hàng
     */
    ImportStatistics getImportStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * DTO cho thống kê nhập hàng
     */
    class ImportStatistics {
        private Long totalImports;
        private Integer totalQuantity;
        private Integer totalVariants;
        private Integer totalSuppliers;

        // Constructors, getters, setters
        public ImportStatistics() {
        }

        public ImportStatistics(Long totalImports, Integer totalQuantity, Integer totalVariants,
                Integer totalSuppliers) {
            this.totalImports = totalImports;
            this.totalQuantity = totalQuantity;
            this.totalVariants = totalVariants;
            this.totalSuppliers = totalSuppliers;
        }

        // Getters and Setters
        public Long getTotalImports() {
            return totalImports;
        }

        public void setTotalImports(Long totalImports) {
            this.totalImports = totalImports;
        }

        public Integer getTotalQuantity() {
            return totalQuantity;
        }

        public void setTotalQuantity(Integer totalQuantity) {
            this.totalQuantity = totalQuantity;
        }

        public Integer getTotalVariants() {
            return totalVariants;
        }

        public void setTotalVariants(Integer totalVariants) {
            this.totalVariants = totalVariants;
        }

        public Integer getTotalSuppliers() {
            return totalSuppliers;
        }

        public void setTotalSuppliers(Integer totalSuppliers) {
            this.totalSuppliers = totalSuppliers;
        }
    }
}
