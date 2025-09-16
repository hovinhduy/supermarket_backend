package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.entity.Inventory;
import iuh.fit.supermarket.entity.InventoryTransaction;
import iuh.fit.supermarket.entity.ProductVariant;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.Warehouse;
import iuh.fit.supermarket.repository.InventoryRepository;
import iuh.fit.supermarket.repository.ProductVariantRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.repository.WarehouseRepository;
import iuh.fit.supermarket.service.BaseUnitInventoryService;
import iuh.fit.supermarket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation của BaseUnitInventoryService
 * Quản lý tồn kho dựa trên đơn vị cơ bản
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BaseUnitInventoryServiceImpl implements BaseUnitInventoryService {

    private final ProductVariantRepository productVariantRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductUnitRepository productUnitRepository;
    private final InventoryService inventoryService;

    /**
     * Tìm biến thể có đơn vị cơ bản của một sản phẩm
     */
    @Override
    public Optional<ProductVariant> findBaseUnitVariant(Long productId) {
        log.debug("Tìm biến thể có đơn vị cơ bản cho sản phẩm ID: {}", productId);

        List<ProductVariant> baseVariants = productVariantRepository
                .findByProductIdAndUnitIsBaseUnitTrueAndIsDeletedFalse(productId);

        if (baseVariants.isEmpty()) {
            return Optional.empty();
        }

        // Lấy biến thể đầu tiên nếu có nhiều base unit (không nên xảy ra nhưng để tránh
        // lỗi)
        if (baseVariants.size() > 1) {
            log.warn("Tìm thấy {} biến thể có đơn vị cơ bản cho sản phẩm ID: {}. Lấy biến thể đầu tiên.",
                    baseVariants.size(), productId);

            // Tự động sửa dữ liệu: chỉ giữ lại base unit đầu tiên, các base unit khác
            // chuyển thành non-base
            fixMultipleBaseUnits(productId, baseVariants);
        }

        return Optional.of(baseVariants.get(0));
    }

    /**
     * Lấy thông tin tồn kho của biến thể có đơn vị cơ bản
     */
    @Override
    public Optional<Inventory> getBaseUnitInventory(Long productId, Integer warehouseId) {
        log.debug("Lấy tồn kho đơn vị cơ bản cho sản phẩm {} tại kho {}", productId, warehouseId);

        Optional<ProductVariant> baseVariant = findBaseUnitVariant(productId);
        if (baseVariant.isEmpty()) {
            log.warn("Không tìm thấy biến thể có đơn vị cơ bản cho sản phẩm ID: {}", productId);
            return Optional.empty();
        }

        return inventoryRepository.findByVariantVariantIdAndWarehouseWarehouseId(
                baseVariant.get().getVariantId(), warehouseId);
    }

    /**
     * Tính số lượng tồn kho cho biến thể cụ thể dựa trên đơn vị cơ bản
     */
    @Override
    public Integer getQuantityOnHandForVariant(Long variantId, Integer warehouseId) {
        log.debug("Tính số lượng tồn kho cho biến thể {} tại kho {}", variantId, warehouseId);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Nếu là đơn vị cơ bản, trả về trực tiếp
        if (Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            Optional<Inventory> inventory = inventoryRepository
                    .findByVariantVariantIdAndWarehouseWarehouseId(variantId, warehouseId);
            return inventory.map(Inventory::getQuantityOnHand).orElse(0);
        }

        // Nếu không phải đơn vị cơ bản, tính dựa trên đơn vị cơ bản
        Optional<Inventory> baseInventory = getBaseUnitInventory(variant.getProduct().getId(), warehouseId);
        if (baseInventory.isEmpty()) {
            return 0;
        }

        return baseInventory.get().getQuantityOnHandForUnit(variant.getUnit().getConversionValue());
    }

    /**
     * Tính số lượng có thể bán cho biến thể cụ thể dựa trên đơn vị cơ bản
     */
    @Override
    public Integer getAvailableQuantityForVariant(Long variantId, Integer warehouseId) {
        log.debug("Tính số lượng có thể bán cho biến thể {} tại kho {}", variantId, warehouseId);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Nếu là đơn vị cơ bản, trả về trực tiếp
        if (Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            Optional<Inventory> inventory = inventoryRepository
                    .findByVariantVariantIdAndWarehouseWarehouseId(variantId, warehouseId);
            return inventory.map(Inventory::getAvailableQuantity).orElse(0);
        }

        // Nếu không phải đơn vị cơ bản, tính dựa trên đơn vị cơ bản
        Optional<Inventory> baseInventory = getBaseUnitInventory(variant.getProduct().getId(), warehouseId);
        if (baseInventory.isEmpty()) {
            return 0;
        }

        return baseInventory.get().getAvailableQuantityForUnit(variant.getUnit().getConversionValue());
    }

    /**
     * Tính tổng số lượng tồn kho cho biến thể trên tất cả kho
     */
    @Override
    public Integer getTotalQuantityOnHandForVariant(Long variantId) {
        log.debug("Tính tổng số lượng tồn kho cho biến thể {} trên tất cả kho", variantId);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Nếu là đơn vị cơ bản, trả về trực tiếp
        if (Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            return inventoryRepository.getTotalQuantityByVariantId(variantId);
        }

        // Nếu không phải đơn vị cơ bản, tính dựa trên đơn vị cơ bản
        Optional<ProductVariant> baseVariant = findBaseUnitVariant(variant.getProduct().getId());
        if (baseVariant.isEmpty()) {
            return 0;
        }

        Integer baseQuantity = inventoryRepository.getTotalQuantityByVariantId(baseVariant.get().getVariantId());
        return baseQuantity / variant.getUnit().getConversionValue();
    }

    /**
     * Tính tổng số lượng có thể bán cho biến thể trên tất cả kho
     */
    @Override
    public Integer getTotalAvailableQuantityForVariant(Long variantId) {
        log.debug("Tính tổng số lượng có thể bán cho biến thể {} trên tất cả kho", variantId);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Nếu là đơn vị cơ bản, trả về trực tiếp
        if (Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            return inventoryRepository.getTotalAvailableQuantityByVariantId(variantId);
        }

        // Nếu không phải đơn vị cơ bản, tính dựa trên đơn vị cơ bản
        Optional<ProductVariant> baseVariant = findBaseUnitVariant(variant.getProduct().getId());
        if (baseVariant.isEmpty()) {
            return 0;
        }

        Integer baseAvailable = inventoryRepository
                .getTotalAvailableQuantityByVariantId(baseVariant.get().getVariantId());
        return baseAvailable / variant.getUnit().getConversionValue();
    }

    /**
     * Kiểm tra biến thể có cần đặt hàng lại không
     */
    @Override
    public Boolean needsReorderForVariant(Long variantId) {
        log.debug("Kiểm tra cần đặt hàng lại cho biến thể {}", variantId);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Nếu là đơn vị cơ bản, kiểm tra trực tiếp
        if (Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            List<Inventory> inventories = inventoryRepository.findByVariantVariantId(variantId);
            return inventories.stream().anyMatch(Inventory::needsReorder);
        }

        // Nếu không phải đơn vị cơ bản, kiểm tra dựa trên đơn vị cơ bản
        Optional<ProductVariant> baseVariant = findBaseUnitVariant(variant.getProduct().getId());
        if (baseVariant.isEmpty()) {
            return false;
        }

        List<Inventory> baseInventories = inventoryRepository.findByVariantVariantId(baseVariant.get().getVariantId());
        return baseInventories.stream().anyMatch(Inventory::needsReorder);
    }

    /**
     * Kiểm tra xem có thể tạo tồn kho cho biến thể này không
     */
    @Override
    public Boolean canCreateInventoryForVariant(Long variantId) {
        log.debug("Kiểm tra có thể tạo tồn kho cho biến thể {}", variantId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Chỉ cho phép tạo tồn kho cho biến thể có đơn vị cơ bản
        return Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit());
    }

    /**
     * Cập nhật số lượng tồn kho dựa trên đơn vị cơ bản
     */
    @Override
    @Transactional
    public Inventory updateInventoryFromVariant(Long variantId, Integer warehouseId, Integer quantityChange,
            String transactionType, BigDecimal unitCostPrice,
            String referenceId, String notes) {
        log.info("Cập nhật tồn kho từ biến thể {} tại kho {} với thay đổi {}",
                variantId, warehouseId, quantityChange);

        // Lấy thông tin biến thể
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Tìm biến thể có đơn vị cơ bản
        Optional<ProductVariant> baseVariant = findBaseUnitVariant(variant.getProduct().getId());
        if (baseVariant.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể có đơn vị cơ bản cho sản phẩm ID: " +
                    variant.getProduct().getId());
        }

        // Tính toán số lượng thay đổi theo đơn vị cơ bản
        Integer baseQuantityChange = quantityChange * variant.getUnit().getConversionValue();

        // Cập nhật tồn kho cho biến thể có đơn vị cơ bản
        InventoryTransaction.TransactionType transType = InventoryTransaction.TransactionType.valueOf(transactionType);

        return inventoryService.updateInventory(
                baseVariant.get().getVariantId(),
                warehouseId,
                baseQuantityChange,
                transType,
                unitCostPrice,
                referenceId,
                notes);
    }

    /**
     * Lấy danh sách tất cả biến thể của sản phẩm có tồn kho thấp
     */
    @Override
    public List<ProductVariant> getLowStockVariants() {
        log.debug("Lấy danh sách biến thể có tồn kho thấp");

        List<Inventory> lowStockInventories = inventoryService.getLowStockInventories();
        List<ProductVariant> lowStockVariants = new ArrayList<>();

        for (Inventory inventory : lowStockInventories) {
            // Lấy tất cả biến thể của sản phẩm
            Long productId = inventory.getVariant().getProduct().getId();
            List<ProductVariant> productVariants = productVariantRepository
                    .findByProductIdAndIsDeletedFalse(productId);

            lowStockVariants.addAll(productVariants);
        }

        return lowStockVariants.stream().distinct().toList();
    }

    /**
     * Lấy danh sách tồn kho của tất cả biến thể trong một sản phẩm
     */
    @Override
    public List<VariantInventoryInfo> getVariantInventoriesForProduct(Long productId, Integer warehouseId) {
        log.debug("Lấy danh sách tồn kho cho tất cả biến thể của sản phẩm {} tại kho {}",
                productId, warehouseId);

        List<VariantInventoryInfo> result = new ArrayList<>();

        // Lấy tất cả biến thể của sản phẩm
        List<ProductVariant> variants = productVariantRepository.findByProductIdAndIsDeletedFalse(productId);

        if (warehouseId != null) {
            // Lấy thông tin cho kho cụ thể
            for (ProductVariant variant : variants) {
                VariantInventoryInfo info = createVariantInventoryInfo(variant, warehouseId);
                result.add(info);
            }
        } else {
            // Lấy thông tin cho tất cả kho
            List<Warehouse> warehouses = warehouseRepository.findAll();
            for (ProductVariant variant : variants) {
                for (Warehouse warehouse : warehouses) {
                    VariantInventoryInfo info = createVariantInventoryInfo(variant, warehouse.getWarehouseId());
                    result.add(info);
                }
            }
        }

        return result;
    }

    /**
     * Tạo thông tin tồn kho cho biến thể tại kho cụ thể
     */
    private VariantInventoryInfo createVariantInventoryInfo(ProductVariant variant, Integer warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho với ID: " + warehouseId));

        Integer quantityOnHand = getQuantityOnHandForVariant(variant.getVariantId(), warehouseId);
        Integer availableQuantity = getAvailableQuantityForVariant(variant.getVariantId(), warehouseId);
        Boolean needsReorder = needsReorderForVariant(variant.getVariantId());

        // Tính quantityReserved dựa trên tỷ lệ
        Integer quantityReserved = quantityOnHand - availableQuantity;

        return new VariantInventoryInfo(
                variant.getVariantId(),
                variant.getVariantCode(),
                variant.getVariantName(),
                variant.getUnit().getUnit(),
                variant.getUnit().getConversionValue(),
                variant.getUnit().getIsBaseUnit(),
                quantityOnHand,
                quantityReserved,
                availableQuantity,
                needsReorder,
                warehouseId,
                warehouse.getName());
    }

    /**
     * Sửa lỗi có nhiều base unit cho cùng một sản phẩm
     * Chỉ giữ lại base unit đầu tiên, các base unit khác chuyển thành non-base
     */
    @Transactional
    private void fixMultipleBaseUnits(Long productId, List<ProductVariant> baseVariants) {
        log.warn("Đang sửa lỗi có {} base unit cho sản phẩm ID: {}", baseVariants.size(), productId);

        // Giữ lại base unit đầu tiên
        ProductVariant keepAsBase = baseVariants.get(0);
        log.info("Giữ lại base unit: {} (ID: {})", keepAsBase.getVariantName(), keepAsBase.getVariantId());

        // Chuyển các base unit khác thành non-base
        for (int i = 1; i < baseVariants.size(); i++) {
            ProductVariant variant = baseVariants.get(i);
            ProductUnit unit = variant.getUnit();

            log.warn("Chuyển biến thể '{}' (ID: {}) từ base unit thành non-base unit",
                    variant.getVariantName(), variant.getVariantId());

            // Cập nhật unit thành non-base và set conversion value phù hợp
            unit.setIsBaseUnit(false);
            if (unit.getConversionValue() == null || unit.getConversionValue() <= 1) {
                unit.setConversionValue(1); // Set conversion value mặc định
            }

            // Lưu thay đổi
            productUnitRepository.save(unit);
        }

        log.info("Đã sửa xong lỗi multiple base units cho sản phẩm ID: {}", productId);
    }
}
