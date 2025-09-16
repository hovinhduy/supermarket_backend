package iuh.fit.supermarket.service;

import iuh.fit.supermarket.entity.Inventory;
import iuh.fit.supermarket.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho quản lý tồn kho dựa trên đơn vị cơ bản
 * Chỉ lưu tồn kho cho biến thể có đơn vị cơ bản (isBaseUnit = true)
 * Tính toán tồn kho cho các biến thể khác dựa trên conversionValue
 */
public interface BaseUnitInventoryService {

    /**
     * Tìm biến thể có đơn vị cơ bản của một sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return biến thể có đơn vị cơ bản
     */
    Optional<ProductVariant> findBaseUnitVariant(Long productId);

    /**
     * Lấy thông tin tồn kho của biến thể có đơn vị cơ bản
     * 
     * @param productId   ID sản phẩm
     * @param warehouseId ID kho hàng
     * @return thông tin tồn kho của biến thể có đơn vị cơ bản
     */
    Optional<Inventory> getBaseUnitInventory(Long productId, Integer warehouseId);

    /**
     * Tính số lượng tồn kho cho biến thể cụ thể dựa trên đơn vị cơ bản
     * 
     * @param variantId   ID biến thể cần tính
     * @param warehouseId ID kho hàng
     * @return số lượng tồn kho theo đơn vị của biến thể đó
     */
    Integer getQuantityOnHandForVariant(Long variantId, Integer warehouseId);

    /**
     * Tính số lượng có thể bán cho biến thể cụ thể dựa trên đơn vị cơ bản
     * 
     * @param variantId   ID biến thể cần tính
     * @param warehouseId ID kho hàng
     * @return số lượng có thể bán theo đơn vị của biến thể đó
     */
    Integer getAvailableQuantityForVariant(Long variantId, Integer warehouseId);

    /**
     * Tính tổng số lượng tồn kho cho biến thể trên tất cả kho
     * 
     * @param variantId ID biến thể cần tính
     * @return tổng số lượng tồn kho theo đơn vị của biến thể đó
     */
    Integer getTotalQuantityOnHandForVariant(Long variantId);

    /**
     * Tính tổng số lượng có thể bán cho biến thể trên tất cả kho
     * 
     * @param variantId ID biến thể cần tính
     * @return tổng số lượng có thể bán theo đơn vị của biến thể đó
     */
    Integer getTotalAvailableQuantityForVariant(Long variantId);

    /**
     * Kiểm tra biến thể có cần đặt hàng lại không
     * 
     * @param variantId ID biến thể cần kiểm tra
     * @return true nếu cần đặt hàng lại
     */
    Boolean needsReorderForVariant(Long variantId);

    /**
     * Cập nhật số lượng tồn kho dựa trên đơn vị cơ bản
     * Chuyển đổi số lượng từ đơn vị của biến thể về đơn vị cơ bản trước khi cập nhật
     * 
     * @param variantId       ID biến thể (có thể không phải đơn vị cơ bản)
     * @param warehouseId     ID kho hàng
     * @param quantityChange  thay đổi số lượng theo đơn vị của biến thể
     * @param transactionType loại giao dịch
     * @param unitCostPrice   giá vốn đơn vị
     * @param referenceId     mã tham chiếu
     * @param notes           ghi chú
     * @return thông tin tồn kho sau cập nhật
     */
    Inventory updateInventoryFromVariant(Long variantId, Integer warehouseId, Integer quantityChange,
                                       String transactionType, java.math.BigDecimal unitCostPrice, 
                                       String referenceId, String notes);

    /**
     * Lấy danh sách tất cả biến thể của sản phẩm có tồn kho thấp
     * 
     * @return danh sách biến thể cần đặt hàng lại
     */
    List<ProductVariant> getLowStockVariants();

    /**
     * Kiểm tra xem có thể tạo tồn kho cho biến thể này không
     * Chỉ cho phép tạo tồn kho cho biến thể có đơn vị cơ bản
     * 
     * @param variantId ID biến thể
     * @return true nếu có thể tạo tồn kho
     */
    Boolean canCreateInventoryForVariant(Long variantId);

    /**
     * Lấy danh sách tồn kho của tất cả biến thể trong một sản phẩm
     * Tính toán dựa trên tồn kho của đơn vị cơ bản
     * 
     * @param productId   ID sản phẩm
     * @param warehouseId ID kho hàng (optional, nếu null thì lấy tất cả kho)
     * @return danh sách thông tin tồn kho của các biến thể
     */
    List<VariantInventoryInfo> getVariantInventoriesForProduct(Long productId, Integer warehouseId);

    /**
     * DTO chứa thông tin tồn kho của biến thể
     */
    record VariantInventoryInfo(
            Long variantId,
            String variantCode,
            String variantName,
            String unitName,
            Integer conversionValue,
            Boolean isBaseUnit,
            Integer quantityOnHand,
            Integer quantityReserved,
            Integer availableQuantity,
            Boolean needsReorder,
            Integer warehouseId,
            String warehouseName
    ) {}
}
