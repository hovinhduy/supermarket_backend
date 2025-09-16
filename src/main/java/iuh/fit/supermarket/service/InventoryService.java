package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.inventory.InventoryHistoryDto;
import iuh.fit.supermarket.dto.inventory.InventoryHistoryRequest;
import iuh.fit.supermarket.dto.inventory.InventoryTransactionDto;
import iuh.fit.supermarket.entity.Inventory;
import iuh.fit.supermarket.entity.InventoryTransaction;
import iuh.fit.supermarket.entity.ProductVariant;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho quản lý tồn kho sản phẩm
 */
public interface InventoryService {

        /**
         * Tạo tồn kho mới cho biến thể sản phẩm tại kho mặc định
         * 
         * @param variant         biến thể sản phẩm
         * @param initialQuantity số lượng ban đầu
         * @param unitCostPrice   giá vốn đơn vị
         * @param notes           ghi chú
         * @return thông tin tồn kho đã tạo
         */
        Inventory createInventoryForVariant(ProductVariant variant, Integer initialQuantity,
                        BigDecimal unitCostPrice, String notes);

        /**
         * Tạo tồn kho mới cho biến thể sản phẩm tại kho cụ thể
         * 
         * @param variant         biến thể sản phẩm
         * @param warehouseId     ID kho hàng
         * @param initialQuantity số lượng ban đầu
         * @param unitCostPrice   giá vốn đơn vị
         * @param notes           ghi chú
         * @return thông tin tồn kho đã tạo
         */
        Inventory createInventoryForVariant(ProductVariant variant, Integer warehouseId,
                        Integer initialQuantity, BigDecimal unitCostPrice, String notes);

        /**
         * Lấy thông tin tồn kho theo biến thể và kho hàng
         * 
         * @param variantId   ID biến thể sản phẩm
         * @param warehouseId ID kho hàng
         * @return thông tin tồn kho
         */
        Optional<Inventory> getInventoryByVariantAndWarehouse(Long variantId, Integer warehouseId);

        /**
         * Lấy danh sách tồn kho theo biến thể sản phẩm
         * 
         * @param variantId ID biến thể sản phẩm
         * @return danh sách tồn kho tại các kho khác nhau
         */
        List<Inventory> getInventoriesByVariant(Long variantId);

        /**
         * Tính tổng số lượng tồn kho của một biến thể trên tất cả kho
         * 
         * @param variantId ID biến thể sản phẩm
         * @return tổng số lượng tồn kho
         */
        Integer getTotalQuantityByVariant(Long variantId);

        /**
         * Tính tổng số lượng có thể bán của một biến thể trên tất cả kho
         * 
         * @param variantId ID biến thể sản phẩm
         * @return tổng số lượng có thể bán
         */
        Integer getTotalAvailableQuantityByVariant(Long variantId);

        /**
         * Kiểm tra biến thể có cần đặt hàng lại không
         * 
         * @param variantId ID biến thể sản phẩm
         * @return true nếu cần đặt hàng lại
         */
        Boolean needsReorderByVariant(Long variantId);

        /**
         * Cập nhật số lượng tồn kho
         * 
         * @param variantId       ID biến thể sản phẩm
         * @param warehouseId     ID kho hàng
         * @param quantityChange  thay đổi số lượng (dương = nhập, âm = xuất)
         * @param transactionType loại giao dịch
         * @param unitCostPrice   giá vốn đơn vị
         * @param referenceId     mã tham chiếu (mã đơn hàng, phiếu nhập...)
         * @param notes           ghi chú
         * @return thông tin tồn kho sau cập nhật
         */
        Inventory updateInventory(Long variantId, Integer warehouseId, Integer quantityChange,
                        InventoryTransaction.TransactionType transactionType,
                        BigDecimal unitCostPrice, String referenceId, String notes);

        /**
         * Lấy danh sách sản phẩm có tồn kho thấp
         * 
         * @return danh sách tồn kho cần đặt hàng lại
         */
        List<Inventory> getLowStockInventories();

        /**
         * Lấy lịch sử giao dịch theo biến thể sản phẩm
         *
         * @param variantId ID biến thể sản phẩm
         * @return danh sách giao dịch DTO
         */
        List<InventoryTransactionDto> getTransactionHistoryByVariant(Long variantId);

        /**
         * Lấy lịch sử thay đổi kho hàng với phân trang, tìm kiếm và sắp xếp
         *
         * @param request thông tin phân trang, tìm kiếm và sắp xếp
         * @return danh sách lịch sử thay đổi kho hàng với phân trang
         */
        Page<InventoryHistoryDto> getInventoryHistory(InventoryHistoryRequest request);

        /**
         * Lấy ID kho mặc định
         *
         * @return ID kho mặc định
         */
        Integer getDefaultWarehouseId();
}
