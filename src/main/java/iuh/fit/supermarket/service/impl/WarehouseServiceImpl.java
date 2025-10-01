package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.warehouse.WarehouseDto;
import iuh.fit.supermarket.dto.warehouse.WarehouseTransactionDto;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.exception.InsufficientStockException;
import iuh.fit.supermarket.exception.WarehouseException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation của WarehouseService
 * Xử lý logic nghiệp vụ cho quản lý tồn kho và giao dịch kho
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseTransactionRepository warehouseTransactionRepository;
    private final ProductUnitRepository productUnitRepository;

    /**
     * Cập nhật tồn kho cho một đơn vị sản phẩm
     * Đây là method core xử lý tất cả các thay đổi tồn kho
     */
    @Override
    @Transactional
    public WarehouseDto updateStock(Long productUnitId, Integer quantityChange,
            WarehouseTransaction.TransactionType transactionType,
            String referenceId, String notes) {
        log.info("Cập nhật tồn kho cho đơn vị sản phẩm ID: {}, thay đổi: {}, loại: {}",
                productUnitId, quantityChange, transactionType);

        try {
            // 1. Validate đầu vào
            validateStockUpdateRequest(productUnitId, quantityChange, transactionType);

            // 2. Kiểm tra đơn vị sản phẩm có tồn tại và active
            ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy đơn vị sản phẩm với ID: " + productUnitId));

            if (!productUnit.getIsActive() || productUnit.getIsDeleted()) {
                throw new IllegalArgumentException(
                        "Đơn vị sản phẩm không hoạt động hoặc đã bị xóa với ID: " + productUnitId);
            }

            // 3. Lấy hoặc tạo mới bản ghi tồn kho
            Warehouse warehouse = getOrCreateWarehouse(productUnit);
            Integer currentQuantity = warehouse.getQuantityOnHand();

            // 4. Kiểm tra số lượng xuất không vượt quá tồn kho
            if (quantityChange < 0 && Math.abs(quantityChange) > currentQuantity) {
                throw new InsufficientStockException(productUnitId, Math.abs(quantityChange), currentQuantity);
            }

            // 5. Cập nhật số lượng tồn kho
            Integer newQuantity = currentQuantity + quantityChange;
            warehouse.setQuantityOnHand(newQuantity);
            warehouse = warehouseRepository.save(warehouse);

            // 6. Ghi nhận giao dịch
            WarehouseTransaction transaction = createWarehouseTransaction(
                    productUnit, currentQuantity, quantityChange, newQuantity, transactionType, referenceId, notes);
            warehouseTransactionRepository.save(transaction);

            log.info("Đã cập nhật tồn kho cho đơn vị sản phẩm {}: {} -> {}",
                    productUnit.getCode(), currentQuantity, newQuantity);

            return convertWarehouseToDto(warehouse);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật tồn kho: ", e);
            throw new RuntimeException("Không thể cập nhật tồn kho: " + e.getMessage(), e);
        }
    }

    /**
     * Validate dữ liệu đầu vào cho việc cập nhật tồn kho
     */
    private void validateStockUpdateRequest(Long productUnitId, Integer quantityChange,
            WarehouseTransaction.TransactionType transactionType) {
        if (productUnitId == null) {
            throw new IllegalArgumentException("ID đơn vị sản phẩm không được null");
        }
        if (quantityChange == null || quantityChange == 0) {
            throw new IllegalArgumentException("Số lượng thay đổi phải khác 0");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("Loại giao dịch không được null");
        }
    }

    /**
     * Lấy hoặc tạo mới bản ghi tồn kho cho đơn vị sản phẩm
     */
    private Warehouse getOrCreateWarehouse(ProductUnit productUnit) {
        Optional<Warehouse> existingWarehouse = warehouseRepository.findByProductUnitId(productUnit.getId());

        if (existingWarehouse.isPresent()) {
            return existingWarehouse.get();
        } else {
            // Tạo mới bản ghi tồn kho
            Warehouse newWarehouse = new Warehouse();
            newWarehouse.setProductUnit(productUnit);
            newWarehouse.setQuantityOnHand(0);
            return warehouseRepository.save(newWarehouse);
        }
    }

    /**
     * Tạo bản ghi giao dịch kho
     *
     * @param productUnit     đơn vị sản phẩm
     * @param beforeQuantity  số lượng tồn kho trước giao dịch
     * @param quantityChange  số lượng thay đổi
     * @param newQuantity     số lượng tồn kho sau giao dịch
     * @param transactionType loại giao dịch
     * @param referenceId     mã tham chiếu
     * @param notes           ghi chú
     * @return WarehouseTransaction
     */
    private WarehouseTransaction createWarehouseTransaction(ProductUnit productUnit, Integer beforeQuantity,
            Integer quantityChange, Integer newQuantity, WarehouseTransaction.TransactionType transactionType,
            String referenceId, String notes) {
        WarehouseTransaction transaction = new WarehouseTransaction();
        transaction.setProductUnit(productUnit);
        transaction.setBeforeQuantity(beforeQuantity);
        transaction.setQuantityChange(quantityChange);
        transaction.setNewQuantity(newQuantity);
        transaction.setTransactionType(transactionType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        return transaction;
    }

    /**
     * Nhập hàng cho một đơn vị sản phẩm
     */
    @Override
    @Transactional
    public WarehouseDto stockIn(Long productUnitId, Integer quantity, String referenceId, String notes) {
        log.info("Nhập hàng cho đơn vị sản phẩm ID: {}, số lượng: {}", productUnitId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        return updateStock(productUnitId, quantity, WarehouseTransaction.TransactionType.STOCK_IN, referenceId, notes);
    }

    /**
     * Xuất hàng cho một đơn vị sản phẩm
     */
    @Override
    @Transactional
    public WarehouseDto stockOut(Long productUnitId, Integer quantity, String referenceId, String notes) {
        log.info("Xuất hàng cho đơn vị sản phẩm ID: {}, số lượng: {}", productUnitId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }

        return updateStock(productUnitId, -quantity, WarehouseTransaction.TransactionType.SALE, referenceId, notes);
    }

    /**
     * Lấy thông tin tồn kho theo đơn vị sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public WarehouseDto getWarehouseByProductUnitId(Long productUnitId) {
        log.debug("Lấy thông tin tồn kho cho đơn vị sản phẩm ID: {}", productUnitId);

        Warehouse warehouse = warehouseRepository.findByProductUnitId(productUnitId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Không tìm thấy tồn kho cho đơn vị sản phẩm ID: " + productUnitId));

        return convertWarehouseToDto(warehouse);
    }

    /**
     * Kiểm tra tồn kho có đủ để xuất không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isStockAvailable(Long productUnitId, Integer requiredQuantity) {
        Integer currentStock = getCurrentStock(productUnitId);
        return currentStock >= requiredQuantity;
    }

    /**
     * Lấy số lượng tồn kho hiện tại
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentStock(Long productUnitId) {
        Optional<Warehouse> warehouse = warehouseRepository.findByProductUnitId(productUnitId);
        return warehouse.map(Warehouse::getQuantityOnHand).orElse(0);
    }

    /**
     * Lấy danh sách tồn kho có số lượng thấp
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> getLowStockWarehouses(Integer minQuantity, Pageable pageable) {
        log.debug("Lấy danh sách tồn kho thấp với ngưỡng: {}", minQuantity);

        Page<Warehouse> warehouses = warehouseRepository.findLowStockWarehouses(minQuantity, pageable);
        return warehouses.map(this::convertWarehouseToDto);
    }

    /**
     * Lấy danh sách tồn kho hết hàng
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> getOutOfStockWarehouses(Pageable pageable) {
        log.debug("Lấy danh sách tồn kho hết hàng");

        Page<Warehouse> warehouses = warehouseRepository.findOutOfStockWarehouses(pageable);
        return warehouses.map(this::convertWarehouseToDto);
    }

    /**
     * Lấy danh sách tất cả tồn kho
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> getAllWarehouses(Pageable pageable) {
        log.debug("Lấy danh sách tất cả tồn kho");

        Page<Warehouse> warehouses = warehouseRepository.findAllWithProductUnitDetails(pageable);
        return warehouses.map(this::convertWarehouseToDto);
    }

    /**
     * Tìm kiếm tồn kho theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseDto> searchWarehouses(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm tồn kho với từ khóa: {}", keyword);

        Page<Warehouse> warehouses = warehouseRepository.findByKeyword(keyword, pageable);
        return warehouses.map(this::convertWarehouseToDto);
    }

    /**
     * Lấy danh sách tồn kho theo sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseDto> getWarehousesByProductId(Long productId) {
        log.debug("Lấy danh sách tồn kho theo sản phẩm ID: {}", productId);

        List<Warehouse> warehouses = warehouseRepository.findByProductId(productId);
        return warehouses.stream()
                .map(this::convertWarehouseToDto)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi Warehouse entity sang DTO
     */
    private WarehouseDto convertWarehouseToDto(Warehouse warehouse) {
        WarehouseDto dto = new WarehouseDto();
        dto.setWarehouseId(warehouse.getWarehouseId());
        dto.setQuantityOnHand(warehouse.getQuantityOnHand());
        dto.setUpdatedAt(warehouse.getUpdatedAt());

        if (warehouse.getProductUnit() != null) {
            WarehouseDto.ProductUnitInfo productUnitInfo = new WarehouseDto.ProductUnitInfo();
            productUnitInfo.setProductUnitId(warehouse.getProductUnit().getId());
            productUnitInfo.setCode(warehouse.getProductUnit().getCode());
            productUnitInfo.setBarcode(warehouse.getProductUnit().getBarcode());
            productUnitInfo.setConversionValue(warehouse.getProductUnit().getConversionValue());
            productUnitInfo.setIsBaseUnit(warehouse.getProductUnit().getIsBaseUnit());

            if (warehouse.getProductUnit().getProduct() != null) {
                productUnitInfo.setProductName(warehouse.getProductUnit().getProduct().getName());
            }

            if (warehouse.getProductUnit().getUnit() != null) {
                productUnitInfo.setUnit(warehouse.getProductUnit().getUnit().getName());
            }

            dto.setProductUnit(productUnitInfo);
        }

        return dto;
    }

    /**
     * Lấy lịch sử giao dịch kho theo đơn vị sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseTransactionDto> getTransactionsByProductUnitId(Long productUnitId, Pageable pageable) {
        log.debug("Lấy lịch sử giao dịch kho cho đơn vị sản phẩm ID: {}", productUnitId);

        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByProductUnitId(productUnitId,
                pageable);
        return transactions.map(this::convertTransactionToDto);
    }

    /**
     * Lấy lịch sử giao dịch kho theo loại giao dịch
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseTransactionDto> getTransactionsByType(WarehouseTransaction.TransactionType transactionType,
            Pageable pageable) {
        log.debug("Lấy lịch sử giao dịch kho theo loại: {}", transactionType);

        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByTransactionType(transactionType,
                pageable);
        return transactions.map(this::convertTransactionToDto);
    }

    /**
     * Lấy lịch sử giao dịch kho theo mã tham chiếu
     */
    @Override
    @Transactional(readOnly = true)
    public List<WarehouseTransactionDto> getTransactionsByReferenceId(String referenceId) {
        log.debug("Lấy lịch sử giao dịch kho theo mã tham chiếu: {}", referenceId);

        List<WarehouseTransaction> transactions = warehouseTransactionRepository.findByReferenceId(referenceId);
        return transactions.stream()
                .map(this::convertTransactionToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử giao dịch kho trong khoảng thời gian
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseTransactionDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        log.debug("Lấy lịch sử giao dịch kho từ {} đến {}", startDate, endDate);

        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByDateRange(startDate, endDate,
                pageable);
        return transactions.map(this::convertTransactionToDto);
    }

    /**
     * Lấy tất cả giao dịch kho
     */
    @Override
    @Transactional(readOnly = true)
    public Page<WarehouseTransactionDto> getAllTransactions(Pageable pageable) {
        log.debug("Lấy tất cả giao dịch kho");

        Page<WarehouseTransaction> transactions = warehouseTransactionRepository
                .findAllWithProductUnitDetails(pageable);
        return transactions.map(this::convertTransactionToDto);
    }

    /**
     * Chuyển đổi WarehouseTransaction entity sang DTO
     */
    private WarehouseTransactionDto convertTransactionToDto(WarehouseTransaction transaction) {
        WarehouseTransactionDto dto = new WarehouseTransactionDto();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setBeforeQuantity(transaction.getBeforeQuantity());
        dto.setQuantityChange(transaction.getQuantityChange());
        dto.setNewQuantity(transaction.getNewQuantity());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setReferenceId(transaction.getReferenceId());
        dto.setNotes(transaction.getNotes());
        dto.setTransactionDate(transaction.getTransactionDate());

        if (transaction.getProductUnit() != null) {
            WarehouseTransactionDto.ProductUnitInfo productUnitInfo = new WarehouseTransactionDto.ProductUnitInfo();
            productUnitInfo.setProductUnitId(transaction.getProductUnit().getId());
            productUnitInfo.setCode(transaction.getProductUnit().getCode());
            productUnitInfo.setBarcode(transaction.getProductUnit().getBarcode());
            productUnitInfo.setConversionValue(transaction.getProductUnit().getConversionValue());
            productUnitInfo.setIsBaseUnit(transaction.getProductUnit().getIsBaseUnit());

            if (transaction.getProductUnit().getProduct() != null) {
                productUnitInfo.setProductName(transaction.getProductUnit().getProduct().getName());
            }

            if (transaction.getProductUnit().getUnit() != null) {
                productUnitInfo.setUnit(transaction.getProductUnit().getUnit().getName());
            }

            dto.setProductUnit(productUnitInfo);
        }

        return dto;
    }
}
