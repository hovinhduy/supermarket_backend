package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.inventory.InventoryHistoryDto;
import iuh.fit.supermarket.dto.inventory.InventoryHistoryRequest;
import iuh.fit.supermarket.dto.inventory.InventoryTransactionDto;
import iuh.fit.supermarket.entity.Inventory;
import iuh.fit.supermarket.entity.InventoryTransaction;
import iuh.fit.supermarket.entity.ProductVariant;
import iuh.fit.supermarket.entity.Warehouse;
import iuh.fit.supermarket.repository.InventoryRepository;
import iuh.fit.supermarket.repository.InventoryTransactionRepository;
import iuh.fit.supermarket.repository.WarehouseRepository;
import iuh.fit.supermarket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation của InventoryService
 * Quản lý tồn kho sản phẩm và giao dịch kho
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;

    // ID kho mặc định - có thể cấu hình từ properties
    private static final Integer DEFAULT_WAREHOUSE_ID = 1;

    /**
     * Tạo tồn kho mới cho biến thể sản phẩm tại kho mặc định
     */
    @Override
    @Transactional
    public Inventory createInventoryForVariant(ProductVariant variant, Integer initialQuantity,
            BigDecimal unitCostPrice, String notes) {
        return createInventoryForVariant(variant, DEFAULT_WAREHOUSE_ID, initialQuantity, unitCostPrice, notes);
    }

    /**
     * Tạo tồn kho mới cho biến thể sản phẩm tại kho cụ thể
     * CHỈ CHO PHÉP TẠO TỒN KHO CHO BIẾN THỂ CÓ ĐƠN VỊ CƠ BẢN (isBaseUnit = true)
     */
    @Override
    @Transactional
    public Inventory createInventoryForVariant(ProductVariant variant, Integer warehouseId,
            Integer initialQuantity, BigDecimal unitCostPrice, String notes) {
        log.info("Tạo tồn kho cho biến thể {} tại kho {} với số lượng {}",
                variant.getVariantCode(), warehouseId, initialQuantity);

        // Kiểm tra chỉ cho phép tạo tồn kho cho biến thể có đơn vị cơ bản
        if (!Boolean.TRUE.equals(variant.getUnit().getIsBaseUnit())) {
            throw new RuntimeException("Chỉ có thể tạo tồn kho cho biến thể có đơn vị cơ bản. " +
                    "Biến thể " + variant.getVariantCode() + " có đơn vị: " + variant.getUnit().getUnit());
        }

        // Kiểm tra kho hàng tồn tại
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho hàng với ID: " + warehouseId));

        // Kiểm tra xem đã có tồn kho cho biến thể này tại kho này chưa
        Optional<Inventory> existingInventory = inventoryRepository
                .findByVariantVariantIdAndWarehouseWarehouseId(variant.getVariantId(), warehouseId);

        if (existingInventory.isPresent()) {
            throw new RuntimeException("Đã tồn tại tồn kho cho biến thể " + variant.getVariantCode() +
                    " tại kho " + warehouse.getName());
        }

        // Tạo bản ghi tồn kho mới
        Inventory inventory = new Inventory();
        inventory.setVariant(variant);
        inventory.setWarehouse(warehouse);
        inventory.setQuantityOnHand(initialQuantity != null ? initialQuantity : 0);
        inventory.setQuantityReserved(0);
        inventory.setReorderPoint(0); // Có thể cấu hình sau

        inventory = inventoryRepository.save(inventory);

        // Tạo giao dịch nhập kho ban đầu nếu có số lượng
        if (initialQuantity != null && initialQuantity > 0) {
            createInventoryTransaction(variant, warehouse, initialQuantity, initialQuantity,
                    InventoryTransaction.TransactionType.STOCK_IN, unitCostPrice,
                    "INITIAL_STOCK", notes != null ? notes : "Nhập kho ban đầu khi tạo sản phẩm");
        }

        log.info("Đã tạo tồn kho thành công cho biến thể {} tại kho {}",
                variant.getVariantCode(), warehouse.getName());

        return inventory;
    }

    /**
     * Lấy thông tin tồn kho theo biến thể và kho hàng
     */
    @Override
    public Optional<Inventory> getInventoryByVariantAndWarehouse(Long variantId, Integer warehouseId) {
        return inventoryRepository.findByVariantVariantIdAndWarehouseWarehouseId(variantId, warehouseId);
    }

    /**
     * Lấy danh sách tồn kho theo biến thể sản phẩm
     */
    @Override
    public List<Inventory> getInventoriesByVariant(Long variantId) {
        return inventoryRepository.findByVariantVariantId(variantId);
    }

    /**
     * Tính tổng số lượng tồn kho của một biến thể trên tất cả kho
     */
    @Override
    public Integer getTotalQuantityByVariant(Long variantId) {
        return inventoryRepository.getTotalQuantityByVariantId(variantId);
    }

    /**
     * Tính tổng số lượng có thể bán của một biến thể trên tất cả kho
     */
    @Override
    public Integer getTotalAvailableQuantityByVariant(Long variantId) {
        return inventoryRepository.getTotalAvailableQuantityByVariantId(variantId);
    }

    /**
     * Kiểm tra biến thể có cần đặt hàng lại không
     */
    @Override
    public Boolean needsReorderByVariant(Long variantId) {
        List<Inventory> inventories = getInventoriesByVariant(variantId);
        return inventories.stream().anyMatch(Inventory::needsReorder);
    }

    /**
     * Cập nhật số lượng tồn kho
     */
    @Override
    @Transactional
    public Inventory updateInventory(Long variantId, Integer warehouseId, Integer quantityChange,
            InventoryTransaction.TransactionType transactionType,
            BigDecimal unitCostPrice, String referenceId, String notes) {
        log.info("Cập nhật tồn kho cho biến thể {} tại kho {} với thay đổi {}",
                variantId, warehouseId, quantityChange);

        // Lấy thông tin tồn kho hiện tại
        Inventory inventory = inventoryRepository
                .findByVariantVariantIdAndWarehouseWarehouseId(variantId, warehouseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tồn kho cho biến thể " + variantId +
                        " tại kho " + warehouseId));

        // Tính số lượng mới
        Integer newQuantity = inventory.getQuantityOnHand() + quantityChange;

        if (newQuantity < 0) {
            throw new RuntimeException("Số lượng tồn kho không thể âm. Hiện tại: " +
                    inventory.getQuantityOnHand() + ", Thay đổi: " + quantityChange);
        }

        // Cập nhật số lượng tồn kho
        inventory.setQuantityOnHand(newQuantity);
        inventory = inventoryRepository.save(inventory);

        // Tạo giao dịch kho
        createInventoryTransaction(inventory.getVariant(), inventory.getWarehouse(),
                quantityChange, newQuantity, transactionType, unitCostPrice, referenceId, notes);

        log.info("Đã cập nhật tồn kho thành công. Số lượng mới: {}", newQuantity);

        return inventory;
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp
     */
    @Override
    public List<Inventory> getLowStockInventories() {
        return inventoryRepository.findLowStockInventories();
    }

    /**
     * Lấy lịch sử giao dịch theo biến thể sản phẩm
     */
    @Override
    public List<InventoryTransactionDto> getTransactionHistoryByVariant(Long variantId) {
        List<InventoryTransaction> transactions = inventoryTransactionRepository
                .findByVariantVariantIdOrderByTransactionDateDesc(variantId);

        return transactions.stream()
                .map(this::mapToInventoryTransactionDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử thay đổi kho hàng với phân trang, tìm kiếm và sắp xếp
     */
    @Override
    @Transactional(readOnly = true)
    public Page<InventoryHistoryDto> getInventoryHistory(InventoryHistoryRequest request) {
        log.info("Lấy lịch sử thay đổi kho hàng: page={}, limit={}, search={}",
                request.getPage(), request.getLimit(), request.getSearchTerm());

        // Tạo Pageable object từ request
        Pageable pageable = createPageableFromRequest(request);

        // Gọi repository để lấy dữ liệu
        Page<InventoryTransaction> transactions = inventoryTransactionRepository
                .findInventoryHistoryWithSearch(request.getSearchTerm(), pageable);

        // Map sang InventoryHistoryDto
        return transactions.map(this::mapToInventoryHistoryDto);
    }

    /**
     * Lấy ID kho mặc định
     */
    @Override
    public Integer getDefaultWarehouseId() {
        return DEFAULT_WAREHOUSE_ID;
    }

    /**
     * Tạo giao dịch tồn kho
     */
    private void createInventoryTransaction(ProductVariant variant, Warehouse warehouse,
            Integer quantityChange, Integer newQuantity,
            InventoryTransaction.TransactionType transactionType,
            BigDecimal unitCostPrice, String referenceId, String notes) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setVariant(variant);
        transaction.setWarehouse(warehouse);
        transaction.setQuantityChange(quantityChange);
        transaction.setNewQuantity(newQuantity);
        transaction.setTransactionType(transactionType);
        transaction.setUnitCostPrice(unitCostPrice);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);

        inventoryTransactionRepository.save(transaction);

        log.debug("Đã tạo giao dịch kho: {} {} cho biến thể {}",
                transactionType, quantityChange, variant.getVariantCode());
    }

    /**
     * Tạo Pageable object từ InventoryHistoryRequest
     */
    private Pageable createPageableFromRequest(InventoryHistoryRequest request) {
        // Tạo Sort object từ sorts trong request
        Sort sort = Sort.unsorted();

        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            List<Sort.Order> orders = request.getSorts().stream()
                    .map(sortCriteria -> {
                        Sort.Direction direction = "DESC".equalsIgnoreCase(sortCriteria.getOrder())
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, mapSortField(sortCriteria.getField()));
                    })
                    .collect(Collectors.toList());
            sort = Sort.by(orders);
        } else {
            // Mặc định sắp xếp theo thời gian giảm dần
            sort = Sort.by(Sort.Direction.DESC, "transactionDate");
        }

        // Tạo PageRequest với page index, size và sort
        return PageRequest.of(
                request.getPageIndex(),
                request.getValidLimit(),
                sort);
    }

    /**
     * Map tên trường từ DTO sang tên trường entity
     */
    private String mapSortField(String field) {
        return switch (field) {
            case "time" -> "transactionDate";
            case "actualQuantity" -> "newQuantity";
            case "totalDifference" -> "quantityChange";
            case "increaseQuantity" -> "quantityChange";
            case "decreaseQuantity" -> "quantityChange";
            case "variantCode" -> "variant.variantCode";
            case "variantName" -> "variant.variantName";
            default -> "transactionDate";
        };
    }

    /**
     * Chuyển đổi InventoryTransaction entity thành InventoryHistoryDto
     */
    private InventoryHistoryDto mapToInventoryHistoryDto(InventoryTransaction transaction) {
        // Tính toán số lượng tăng và giảm
        Integer increaseQuantity = transaction.getQuantityChange() > 0 ? transaction.getQuantityChange() : 0;
        Integer decreaseQuantity = transaction.getQuantityChange() < 0 ? Math.abs(transaction.getQuantityChange()) : 0;

        return InventoryHistoryDto.builder()
                .time(transaction.getTransactionDate())
                .actualQuantity(transaction.getNewQuantity())
                .totalDifference(transaction.getQuantityChange())
                .increaseQuantity(increaseQuantity)
                .decreaseQuantity(decreaseQuantity)
                .note(transaction.getNotes())
                // Thông tin sản phẩm
                .variantId(transaction.getVariant().getVariantId())
                .variantCode(transaction.getVariant().getVariantCode())
                .variantName(transaction.getVariant().getVariantName())
                // Thông tin kho hàng
                .warehouseId(transaction.getWarehouse().getWarehouseId())
                .warehouseName(transaction.getWarehouse().getName())
                // Thông tin giao dịch
                .transactionId(transaction.getTransactionId())
                .referenceId(transaction.getReferenceId())
                .build();
    }

    /**
     * Chuyển đổi InventoryTransaction entity thành InventoryTransactionDto
     * Phương thức này đảm bảo không có LazyInitializationException
     * vì các quan hệ đã được eager load bằng @EntityGraph
     */
    private InventoryTransactionDto mapToInventoryTransactionDto(InventoryTransaction transaction) {
        return InventoryTransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .quantityChange(transaction.getQuantityChange())
                .newQuantity(transaction.getNewQuantity())
                .unitCostPrice(transaction.getUnitCostPrice())
                .transactionType(transaction.getTransactionType())
                .referenceId(transaction.getReferenceId())
                .notes(transaction.getNotes())
                .transactionDate(transaction.getTransactionDate())
                // Thông tin biến thể sản phẩm (eager loaded)
                .variantId(transaction.getVariant().getVariantId())
                .variantCode(transaction.getVariant().getVariantCode())
                .variantName(transaction.getVariant().getVariantName())
                // Thông tin kho hàng (eager loaded)
                .warehouseId(transaction.getWarehouse().getWarehouseId())
                .warehouseName(transaction.getWarehouse().getName())
                .build();
    }
}
