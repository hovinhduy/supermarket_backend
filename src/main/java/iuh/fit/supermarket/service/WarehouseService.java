package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.warehouse.WarehouseDto;
import iuh.fit.supermarket.dto.warehouse.WarehouseTransactionDto;
import iuh.fit.supermarket.entity.WarehouseTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho quản lý tồn kho và giao dịch kho
 */
public interface WarehouseService {

    /**
     * Cập nhật tồn kho cho một biến thể sản phẩm
     * - Nếu chưa có tồn kho: tạo mới
     * - Nếu đã có: cập nhật số lượng
     * - Ghi nhận giao dịch vào WarehouseTransaction
     * 
     * @param variantId ID biến thể sản phẩm
     * @param quantityChange số lượng thay đổi (dương = nhập, âm = xuất)
     * @param transactionType loại giao dịch
     * @param referenceId mã tham chiếu (mã phiếu nhập, mã đơn hàng...)
     * @param notes ghi chú
     * @return WarehouseDto thông tin tồn kho sau khi cập nhật
     * @throws IllegalArgumentException nếu biến thể không tồn tại hoặc không active
     * @throws IllegalStateException nếu số lượng xuất lớn hơn tồn kho hiện tại
     */
    WarehouseDto updateStock(Long variantId, Integer quantityChange, 
                           WarehouseTransaction.TransactionType transactionType, 
                           String referenceId, String notes);

    /**
     * Nhập hàng cho một biến thể sản phẩm
     * Wrapper method cho updateStock với STOCK_IN
     * 
     * @param variantId ID biến thể sản phẩm
     * @param quantity số lượng nhập
     * @param referenceId mã phiếu nhập
     * @param notes ghi chú
     * @return WarehouseDto thông tin tồn kho sau khi nhập
     */
    WarehouseDto stockIn(Long variantId, Integer quantity, String referenceId, String notes);

    /**
     * Xuất hàng cho một biến thể sản phẩm
     * Wrapper method cho updateStock với SALE
     * 
     * @param variantId ID biến thể sản phẩm
     * @param quantity số lượng xuất
     * @param referenceId mã đơn hàng
     * @param notes ghi chú
     * @return WarehouseDto thông tin tồn kho sau khi xuất
     */
    WarehouseDto stockOut(Long variantId, Integer quantity, String referenceId, String notes);

    /**
     * Lấy thông tin tồn kho theo biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return WarehouseDto thông tin tồn kho
     * @throws IllegalArgumentException nếu không tìm thấy tồn kho
     */
    WarehouseDto getWarehouseByVariantId(Long variantId);

    /**
     * Kiểm tra tồn kho có đủ để xuất không
     * 
     * @param variantId ID biến thể sản phẩm
     * @param requiredQuantity số lượng cần xuất
     * @return true nếu đủ tồn kho
     */
    boolean isStockAvailable(Long variantId, Integer requiredQuantity);

    /**
     * Lấy số lượng tồn kho hiện tại
     * 
     * @param variantId ID biến thể sản phẩm
     * @return số lượng tồn kho (0 nếu chưa có)
     */
    Integer getCurrentStock(Long variantId);

    /**
     * Lấy danh sách tồn kho có số lượng thấp
     * 
     * @param minQuantity ngưỡng số lượng tối thiểu
     * @param pageable thông tin phân trang
     * @return Page<WarehouseDto> danh sách tồn kho thấp
     */
    Page<WarehouseDto> getLowStockWarehouses(Integer minQuantity, Pageable pageable);

    /**
     * Lấy danh sách tồn kho hết hàng
     * 
     * @param pageable thông tin phân trang
     * @return Page<WarehouseDto> danh sách hết hàng
     */
    Page<WarehouseDto> getOutOfStockWarehouses(Pageable pageable);

    /**
     * Lấy danh sách tất cả tồn kho
     * 
     * @param pageable thông tin phân trang
     * @return Page<WarehouseDto> danh sách tồn kho
     */
    Page<WarehouseDto> getAllWarehouses(Pageable pageable);

    /**
     * Tìm kiếm tồn kho theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<WarehouseDto> danh sách tồn kho
     */
    Page<WarehouseDto> searchWarehouses(String keyword, Pageable pageable);

    /**
     * Lấy danh sách tồn kho theo sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return List<WarehouseDto> danh sách tồn kho các biến thể
     */
    List<WarehouseDto> getWarehousesByProductId(Long productId);

    /**
     * Lấy lịch sử giao dịch kho theo biến thể
     * 
     * @param variantId ID biến thể sản phẩm
     * @param pageable thông tin phân trang
     * @return Page<WarehouseTransactionDto> lịch sử giao dịch
     */
    Page<WarehouseTransactionDto> getTransactionsByVariantId(Long variantId, Pageable pageable);

    /**
     * Lấy lịch sử giao dịch kho theo loại giao dịch
     * 
     * @param transactionType loại giao dịch
     * @param pageable thông tin phân trang
     * @return Page<WarehouseTransactionDto> lịch sử giao dịch
     */
    Page<WarehouseTransactionDto> getTransactionsByType(WarehouseTransaction.TransactionType transactionType, Pageable pageable);

    /**
     * Lấy lịch sử giao dịch kho theo mã tham chiếu
     * 
     * @param referenceId mã tham chiếu
     * @return List<WarehouseTransactionDto> lịch sử giao dịch
     */
    List<WarehouseTransactionDto> getTransactionsByReferenceId(String referenceId);

    /**
     * Lấy lịch sử giao dịch kho trong khoảng thời gian
     * 
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @param pageable thông tin phân trang
     * @return Page<WarehouseTransactionDto> lịch sử giao dịch
     */
    Page<WarehouseTransactionDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Lấy tất cả giao dịch kho
     * 
     * @param pageable thông tin phân trang
     * @return Page<WarehouseTransactionDto> lịch sử giao dịch
     */
    Page<WarehouseTransactionDto> getAllTransactions(Pageable pageable);
}
