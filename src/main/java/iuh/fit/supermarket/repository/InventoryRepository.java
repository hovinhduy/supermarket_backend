package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Inventory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho quản lý tồn kho sản phẩm
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    /**
     * Tìm tồn kho theo biến thể sản phẩm và kho hàng
     * 
     * @param variantId   ID biến thể sản phẩm
     * @param warehouseId ID kho hàng
     * @return thông tin tồn kho
     */
    Optional<Inventory> findByVariantVariantIdAndWarehouseWarehouseId(Long variantId, Integer warehouseId);

    /**
     * Lấy danh sách tồn kho theo biến thể sản phẩm
     * Sử dụng @EntityGraph để eager load ProductVariant và Warehouse
     *
     * @param variantId ID biến thể sản phẩm
     * @return danh sách tồn kho tại các kho khác nhau
     */
    @EntityGraph(attributePaths = { "variant", "warehouse" })
    List<Inventory> findByVariantVariantId(Long variantId);

    /**
     * Lấy danh sách tồn kho theo kho hàng
     * 
     * @param warehouseId ID kho hàng
     * @return danh sách tồn kho tại kho
     */
    List<Inventory> findByWarehouseWarehouseId(Integer warehouseId);

    /**
     * Tìm các sản phẩm có tồn kho thấp (cần đặt hàng lại)
     * Sử dụng @EntityGraph để eager load ProductVariant và Warehouse
     *
     * @return danh sách tồn kho cần đặt hàng lại
     */
    @EntityGraph(attributePaths = { "variant", "warehouse" })
    @Query("SELECT i FROM Inventory i WHERE i.quantityOnHand <= i.reorderPoint")
    List<Inventory> findLowStockInventories();

    /**
     * Tính tổng số lượng tồn kho của một biến thể trên tất cả kho
     * 
     * @param variantId ID biến thể sản phẩm
     * @return tổng số lượng tồn kho
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand), 0) FROM Inventory i WHERE i.variant.variantId = :variantId")
    Integer getTotalQuantityByVariantId(@Param("variantId") Long variantId);

    /**
     * Tính tổng số lượng có thể bán của một biến thể trên tất cả kho
     * 
     * @param variantId ID biến thể sản phẩm
     * @return tổng số lượng có thể bán
     */
    @Query("SELECT COALESCE(SUM(i.quantityOnHand - i.quantityReserved), 0) FROM Inventory i WHERE i.variant.variantId = :variantId")
    Integer getTotalAvailableQuantityByVariantId(@Param("variantId") Long variantId);

    /**
     * Kiểm tra xem biến thể có tồn tại trong kho nào không
     * 
     * @param variantId ID biến thể sản phẩm
     * @return true nếu có tồn kho
     */
    boolean existsByVariantVariantId(Long variantId);
}
