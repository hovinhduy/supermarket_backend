//package iuh.fit.supermarket.service.impl;
//
//import iuh.fit.supermarket.dto.warehouse.WarehouseDto;
//import iuh.fit.supermarket.dto.warehouse.WarehouseTransactionDto;
//import iuh.fit.supermarket.entity.*;
//import iuh.fit.supermarket.exception.InsufficientStockException;
//import iuh.fit.supermarket.exception.WarehouseException;
//import iuh.fit.supermarket.repository.*;
//import iuh.fit.supermarket.service.WarehouseService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * Implementation của WarehouseService
// * Xử lý logic nghiệp vụ cho quản lý tồn kho và giao dịch kho
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class WarehouseServiceImpl implements WarehouseService {
//
//    private final WarehouseRepository warehouseRepository;
//    private final WarehouseTransactionRepository warehouseTransactionRepository;
//    private final ProductVariantRepository productVariantRepository;
//
//    /**
//     * Cập nhật tồn kho cho một biến thể sản phẩm
//     * Đây là method core xử lý tất cả các thay đổi tồn kho
//     */
//    @Override
//    @Transactional
//    public WarehouseDto updateStock(Long variantId, Integer quantityChange,
//            WarehouseTransaction.TransactionType transactionType,
//            String referenceId, String notes) {
//        log.info("Cập nhật tồn kho cho biến thể ID: {}, thay đổi: {}, loại: {}",
//                variantId, quantityChange, transactionType);
//
//        try {
//            // 1. Validate đầu vào
//            validateStockUpdateRequest(variantId, quantityChange, transactionType);
//
//            // 2. Kiểm tra biến thể có tồn tại và active
//            ProductVariant variant = productVariantRepository.findById(variantId)
//                    .orElseThrow(() -> new IllegalArgumentException(
//                            "Không tìm thấy biến thể sản phẩm với ID: " + variantId));
//
//            if (!variant.getIsActive() || variant.getIsDeleted()) {
//                throw new IllegalArgumentException(
//                        "Biến thể sản phẩm không hoạt động hoặc đã bị xóa với ID: " + variantId);
//            }
//
//            // 3. Lấy hoặc tạo mới bản ghi tồn kho
//            Warehouse warehouse = getOrCreateWarehouse(variant);
//            Integer currentQuantity = warehouse.getQuantityOnHand();
//
//            // 4. Kiểm tra số lượng xuất không vượt quá tồn kho
//            if (quantityChange < 0 && Math.abs(quantityChange) > currentQuantity) {
//                throw new InsufficientStockException(variantId, Math.abs(quantityChange), currentQuantity);
//            }
//
//            // 5. Cập nhật số lượng tồn kho
//            Integer newQuantity = currentQuantity + quantityChange;
//            warehouse.setQuantityOnHand(newQuantity);
//            warehouse = warehouseRepository.save(warehouse);
//
//            // 6. Ghi nhận giao dịch
//            WarehouseTransaction transaction = createWarehouseTransaction(
//                    variant, currentQuantity, quantityChange, newQuantity, transactionType, referenceId, notes);
//            warehouseTransactionRepository.save(transaction);
//
//            log.info("Đã cập nhật tồn kho cho biến thể {}: {} -> {}",
//                    variant.getVariantName(), currentQuantity, newQuantity);
//
//            return convertWarehouseToDto(warehouse);
//
//        } catch (Exception e) {
//            log.error("Lỗi khi cập nhật tồn kho: ", e);
//            throw new RuntimeException("Không thể cập nhật tồn kho: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * Validate dữ liệu đầu vào cho việc cập nhật tồn kho
//     */
//    private void validateStockUpdateRequest(Long variantId, Integer quantityChange,
//            WarehouseTransaction.TransactionType transactionType) {
//        if (variantId == null) {
//            throw new IllegalArgumentException("ID biến thể sản phẩm không được null");
//        }
//        if (quantityChange == null || quantityChange == 0) {
//            throw new IllegalArgumentException("Số lượng thay đổi phải khác 0");
//        }
//        if (transactionType == null) {
//            throw new IllegalArgumentException("Loại giao dịch không được null");
//        }
//    }
//
//    /**
//     * Lấy hoặc tạo mới bản ghi tồn kho cho biến thể
//     */
//    private Warehouse getOrCreateWarehouse(ProductVariant variant) {
//        Optional<Warehouse> existingWarehouse = warehouseRepository.findByVariantId(variant.getVariantId());
//
//        if (existingWarehouse.isPresent()) {
//            return existingWarehouse.get();
//        } else {
//            // Tạo mới bản ghi tồn kho
//            Warehouse newWarehouse = new Warehouse();
//            newWarehouse.setVariant(variant);
//            newWarehouse.setQuantityOnHand(0);
//            return warehouseRepository.save(newWarehouse);
//        }
//    }
//
//    /**
//     * Tạo bản ghi giao dịch kho
//     *
//     * @param variant         biến thể sản phẩm
//     * @param beforeQuantity  số lượng tồn kho trước giao dịch
//     * @param quantityChange  số lượng thay đổi
//     * @param newQuantity     số lượng tồn kho sau giao dịch
//     * @param transactionType loại giao dịch
//     * @param referenceId     mã tham chiếu
//     * @param notes           ghi chú
//     * @return WarehouseTransaction
//     */
//    private WarehouseTransaction createWarehouseTransaction(ProductVariant variant, Integer beforeQuantity,
//            Integer quantityChange, Integer newQuantity, WarehouseTransaction.TransactionType transactionType,
//            String referenceId, String notes) {
//        WarehouseTransaction transaction = new WarehouseTransaction();
//        transaction.setVariant(variant);
//        transaction.setBeforeQuantity(beforeQuantity);
//        transaction.setQuantityChange(quantityChange);
//        transaction.setNewQuantity(newQuantity);
//        transaction.setTransactionType(transactionType);
//        transaction.setReferenceId(referenceId);
//        transaction.setNotes(notes);
//        return transaction;
//    }
//
//    /**
//     * Nhập hàng cho một biến thể sản phẩm
//     */
//    @Override
//    @Transactional
//    public WarehouseDto stockIn(Long variantId, Integer quantity, String referenceId, String notes) {
//        log.info("Nhập hàng cho biến thể ID: {}, số lượng: {}", variantId, quantity);
//
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
//        }
//
//        return updateStock(variantId, quantity, WarehouseTransaction.TransactionType.STOCK_IN, referenceId, notes);
//    }
//
//    /**
//     * Xuất hàng cho một biến thể sản phẩm
//     */
//    @Override
//    @Transactional
//    public WarehouseDto stockOut(Long variantId, Integer quantity, String referenceId, String notes) {
//        log.info("Xuất hàng cho biến thể ID: {}, số lượng: {}", variantId, quantity);
//
//        if (quantity <= 0) {
//            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
//        }
//
//        return updateStock(variantId, -quantity, WarehouseTransaction.TransactionType.SALE, referenceId, notes);
//    }
//
//    /**
//     * Lấy thông tin tồn kho theo biến thể sản phẩm
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public WarehouseDto getWarehouseByVariantId(Long variantId) {
//        log.debug("Lấy thông tin tồn kho cho biến thể ID: {}", variantId);
//
//        Warehouse warehouse = warehouseRepository.findByVariantId(variantId)
//                .orElseThrow(
//                        () -> new IllegalArgumentException("Không tìm thấy tồn kho cho biến thể ID: " + variantId));
//
//        return convertWarehouseToDto(warehouse);
//    }
//
//    /**
//     * Kiểm tra tồn kho có đủ để xuất không
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public boolean isStockAvailable(Long variantId, Integer requiredQuantity) {
//        Integer currentStock = getCurrentStock(variantId);
//        return currentStock >= requiredQuantity;
//    }
//
//    /**
//     * Lấy số lượng tồn kho hiện tại
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Integer getCurrentStock(Long variantId) {
//        Optional<Warehouse> warehouse = warehouseRepository.findByVariantId(variantId);
//        return warehouse.map(Warehouse::getQuantityOnHand).orElse(0);
//    }
//
//    /**
//     * Lấy danh sách tồn kho có số lượng thấp
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseDto> getLowStockWarehouses(Integer minQuantity, Pageable pageable) {
//        log.debug("Lấy danh sách tồn kho thấp với ngưỡng: {}", minQuantity);
//
//        Page<Warehouse> warehouses = warehouseRepository.findLowStockWarehouses(minQuantity, pageable);
//        return warehouses.map(this::convertWarehouseToDto);
//    }
//
//    /**
//     * Lấy danh sách tồn kho hết hàng
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseDto> getOutOfStockWarehouses(Pageable pageable) {
//        log.debug("Lấy danh sách tồn kho hết hàng");
//
//        Page<Warehouse> warehouses = warehouseRepository.findOutOfStockWarehouses(pageable);
//        return warehouses.map(this::convertWarehouseToDto);
//    }
//
//    /**
//     * Lấy danh sách tất cả tồn kho
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseDto> getAllWarehouses(Pageable pageable) {
//        log.debug("Lấy danh sách tất cả tồn kho");
//
//        Page<Warehouse> warehouses = warehouseRepository.findAllWithVariantDetails(pageable);
//        return warehouses.map(this::convertWarehouseToDto);
//    }
//
//    /**
//     * Tìm kiếm tồn kho theo từ khóa
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseDto> searchWarehouses(String keyword, Pageable pageable) {
//        log.debug("Tìm kiếm tồn kho với từ khóa: {}", keyword);
//
//        Page<Warehouse> warehouses = warehouseRepository.findByKeyword(keyword, pageable);
//        return warehouses.map(this::convertWarehouseToDto);
//    }
//
//    /**
//     * Lấy danh sách tồn kho theo sản phẩm
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public List<WarehouseDto> getWarehousesByProductId(Long productId) {
//        log.debug("Lấy danh sách tồn kho theo sản phẩm ID: {}", productId);
//
//        List<Warehouse> warehouses = warehouseRepository.findByProductId(productId);
//        return warehouses.stream()
//                .map(this::convertWarehouseToDto)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Chuyển đổi Warehouse entity sang DTO
//     */
//    private WarehouseDto convertWarehouseToDto(Warehouse warehouse) {
//        WarehouseDto dto = new WarehouseDto();
//        dto.setWarehouseId(warehouse.getWarehouseId());
//        dto.setQuantityOnHand(warehouse.getQuantityOnHand());
//        dto.setUpdatedAt(warehouse.getUpdatedAt());
//
//        if (warehouse.getVariant() != null) {
//            WarehouseDto.ProductVariantInfo variantInfo = new WarehouseDto.ProductVariantInfo();
//            variantInfo.setVariantId(warehouse.getVariant().getVariantId());
//            variantInfo.setVariantName(warehouse.getVariant().getVariantName());
//            variantInfo.setVariantCode(warehouse.getVariant().getVariantCode());
//            variantInfo.setBarcode(warehouse.getVariant().getBarcode());
//
//            if (warehouse.getVariant().getProduct() != null) {
//                variantInfo.setProductName(warehouse.getVariant().getProduct().getName());
//            }
//
//            if (warehouse.getVariant().getUnit() != null) {
//                variantInfo.setUnit(warehouse.getVariant().getUnit().getUnit());
//            }
//
//            dto.setVariant(variantInfo);
//        }
//
//        return dto;
//    }
//
//    /**
//     * Lấy lịch sử giao dịch kho theo biến thể
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseTransactionDto> getTransactionsByVariantId(Long variantId, Pageable pageable) {
//        log.debug("Lấy lịch sử giao dịch kho cho biến thể ID: {}", variantId);
//
//        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByVariantId(variantId, pageable);
//        return transactions.map(this::convertTransactionToDto);
//    }
//
//    /**
//     * Lấy lịch sử giao dịch kho theo loại giao dịch
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseTransactionDto> getTransactionsByType(WarehouseTransaction.TransactionType transactionType,
//            Pageable pageable) {
//        log.debug("Lấy lịch sử giao dịch kho theo loại: {}", transactionType);
//
//        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByTransactionType(transactionType,
//                pageable);
//        return transactions.map(this::convertTransactionToDto);
//    }
//
//    /**
//     * Lấy lịch sử giao dịch kho theo mã tham chiếu
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public List<WarehouseTransactionDto> getTransactionsByReferenceId(String referenceId) {
//        log.debug("Lấy lịch sử giao dịch kho theo mã tham chiếu: {}", referenceId);
//
//        List<WarehouseTransaction> transactions = warehouseTransactionRepository.findByReferenceId(referenceId);
//        return transactions.stream()
//                .map(this::convertTransactionToDto)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Lấy lịch sử giao dịch kho trong khoảng thời gian
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseTransactionDto> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
//            Pageable pageable) {
//        log.debug("Lấy lịch sử giao dịch kho từ {} đến {}", startDate, endDate);
//
//        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findByDateRange(startDate, endDate,
//                pageable);
//        return transactions.map(this::convertTransactionToDto);
//    }
//
//    /**
//     * Lấy tất cả giao dịch kho
//     */
//    @Override
//    @Transactional(readOnly = true)
//    public Page<WarehouseTransactionDto> getAllTransactions(Pageable pageable) {
//        log.debug("Lấy tất cả giao dịch kho");
//
//        Page<WarehouseTransaction> transactions = warehouseTransactionRepository.findAllWithVariantDetails(pageable);
//        return transactions.map(this::convertTransactionToDto);
//    }
//
//    /**
//     * Chuyển đổi WarehouseTransaction entity sang DTO
//     */
//    private WarehouseTransactionDto convertTransactionToDto(WarehouseTransaction transaction) {
//        WarehouseTransactionDto dto = new WarehouseTransactionDto();
//        dto.setTransactionId(transaction.getTransactionId());
//        dto.setBeforeQuantity(transaction.getBeforeQuantity());
//        dto.setQuantityChange(transaction.getQuantityChange());
//        dto.setNewQuantity(transaction.getNewQuantity());
//        dto.setTransactionType(transaction.getTransactionType());
//        dto.setReferenceId(transaction.getReferenceId());
//        dto.setNotes(transaction.getNotes());
//        dto.setTransactionDate(transaction.getTransactionDate());
//
//        if (transaction.getVariant() != null) {
//            WarehouseTransactionDto.ProductVariantInfo variantInfo = new WarehouseTransactionDto.ProductVariantInfo();
//            variantInfo.setVariantId(transaction.getVariant().getVariantId());
//            variantInfo.setVariantName(transaction.getVariant().getVariantName());
//            variantInfo.setVariantCode(transaction.getVariant().getVariantCode());
//            variantInfo.setBarcode(transaction.getVariant().getBarcode());
//
//            if (transaction.getVariant().getProduct() != null) {
//                variantInfo.setProductName(transaction.getVariant().getProduct().getName());
//            }
//
//            if (transaction.getVariant().getUnit() != null) {
//                variantInfo.setUnit(transaction.getVariant().getUnit().getUnit());
//            }
//
//            dto.setVariant(variantInfo);
//        }
//
//        return dto;
//    }
//}
