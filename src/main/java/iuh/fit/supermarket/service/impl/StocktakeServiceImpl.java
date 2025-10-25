package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.stocktake.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.StocktakeStatus;
import iuh.fit.supermarket.exception.DuplicateStocktakeCodeException;
import iuh.fit.supermarket.exception.StocktakeException;
import iuh.fit.supermarket.exception.StocktakeNotFoundException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.StocktakeService;
import iuh.fit.supermarket.service.WarehouseService;
import iuh.fit.supermarket.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation của StocktakeService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StocktakeServiceImpl implements StocktakeService {

    private final StocktakeRepository stocktakeRepository;
    private final StocktakeDetailRepository stocktakeDetailRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseTransactionRepository warehouseTransactionRepository;
    private final EmployeeRepository employeeRepository;
    private final ProductUnitRepository productUnitRepository;
    private final WarehouseService warehouseService;
    private final SecurityUtil securityUtil;

    /**
     * Tạo phiếu kiểm kê mới
     */
    @Override
    @Transactional
    public StocktakeDto createStocktake(StocktakeCreateRequest request) {
        log.info("Bắt đầu tạo phiếu kiểm kê mới với {} chi tiết",
                request.getStocktakeDetails() != null ? request.getStocktakeDetails().size() : 0);

        // Lấy thông tin nhân viên hiện tại từ SecurityContext
        Employee createdBy = securityUtil.getCurrentEmployee();

        // Xử lý mã phiếu kiểm kê: sử dụng mã truyền vào hoặc tự động tạo mới
        String stocktakeCode = processStocktakeCode(request.getStocktakeCode());

        // Tạo entity Stocktake
        Stocktake stocktake = new Stocktake();
        stocktake.setStocktakeCode(stocktakeCode);
        stocktake.setNotes(request.getNotes());
        stocktake.setCreatedBy(createdBy);

        // Xử lý trạng thái - mặc định là PENDING nếu không được chỉ định
        StocktakeStatus status = request.getStatus() != null ? request.getStatus() : StocktakeStatus.PENDING;
        stocktake.setStatus(status);

        // Nếu tạo trực tiếp với trạng thái COMPLETED, set thông tin hoàn thành
        if (status == StocktakeStatus.COMPLETED) {
            stocktake.setCompletedBy(createdBy);
            stocktake.setCompletedAt(LocalDateTime.now());
        }

        // Lưu phiếu kiểm kê
        stocktake = stocktakeRepository.save(stocktake);
        log.info("Đã tạo phiếu kiểm kê với ID: {} và mã: {}", stocktake.getStocktakeId(), stocktakeCode);

        // Tạo chi tiết kiểm kê
        if (request.getStocktakeDetails() != null && !request.getStocktakeDetails().isEmpty()) {
            createStocktakeDetails(stocktake, request.getStocktakeDetails());
        }

        // Nếu tạo trực tiếp với trạng thái COMPLETED, cập nhật tồn kho
        if (status == StocktakeStatus.COMPLETED) {
            log.info("Xử lý hoàn thành phiếu kiểm kê được tạo trực tiếp với trạng thái COMPLETED");
            processStocktakeCompletion(stocktake);
            // Lưu lại sau khi xử lý hoàn thành
            stocktake = stocktakeRepository.save(stocktake);
        }

        return mapToStocktakeDto(stocktake);
    }

    /**
     * Lấy thông tin phiếu kiểm kê theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public StocktakeDto getStocktakeById(Integer stocktakeId) {
        log.info("Lấy thông tin phiếu kiểm kê với ID: {}", stocktakeId);

        Stocktake stocktake = stocktakeRepository.findByIdWithDetails(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        return mapToStocktakeDto(stocktake);
    }

    /**
     * Lấy thông tin phiếu kiểm kê theo mã phiếu
     */
    @Override
    @Transactional(readOnly = true)
    public StocktakeDto getStocktakeByCode(String stocktakeCode) {
        log.info("Lấy thông tin phiếu kiểm kê với mã: {}", stocktakeCode);

        Stocktake stocktake = stocktakeRepository.findByStocktakeCode(stocktakeCode)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeCode));

        return mapToStocktakeDto(stocktake);
    }

    /**
     * Cập nhật thông tin phiếu kiểm kê
     */
    @Override
    @Transactional
    public StocktakeDto updateStocktake(Integer stocktakeId, StocktakeUpdateRequest request) {
        log.info("Cập nhật phiếu kiểm kê với ID: {}", stocktakeId);

        Stocktake stocktake = stocktakeRepository.findById(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        // Kiểm tra trạng thái hiện tại
        if (stocktake.getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể cập nhật phiếu kiểm kê đã hoàn thành");
        }

        // Cập nhật thông tin cơ bản
        if (request.getNotes() != null) {
            stocktake.setNotes(request.getNotes());
        }

        // Xử lý thay đổi trạng thái
        if (request.getStatus() != null && request.getStatus() != stocktake.getStatus()) {
            if (request.getStatus() == StocktakeStatus.COMPLETED) {
                // Hoàn thành phiếu kiểm kê
                return completeStocktake(stocktakeId);
            }
            stocktake.setStatus(request.getStatus());
        }

        // Cập nhật chi tiết nếu có
        if (request.getStocktakeDetails() != null && !request.getStocktakeDetails().isEmpty()) {
            updateStocktakeDetails(stocktake, request.getStocktakeDetails());
        }

        stocktake = stocktakeRepository.save(stocktake);
        log.info("Đã cập nhật phiếu kiểm kê với ID: {}", stocktakeId);

        return mapToStocktakeDto(stocktake);
    }

    /**
     * Hoàn thành phiếu kiểm kê
     */
    @Override
    @Transactional
    public StocktakeDto completeStocktake(Integer stocktakeId) {
        log.info("Hoàn thành phiếu kiểm kê với ID: {}", stocktakeId);

        Stocktake stocktake = stocktakeRepository.findByIdWithDetails(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        // Kiểm tra trạng thái hiện tại
        if (stocktake.getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Phiếu kiểm kê đã được hoàn thành trước đó");
        }

        // Lấy thông tin nhân viên hiện tại từ SecurityContext
        Employee completedByEmployee = securityUtil.getCurrentEmployee();

        // Cập nhật số lượng tồn kho mới nhất trước khi hoàn thành
        updateExpectedQuantitiesWithCurrentStock(stocktakeId);

        // Cập nhật tồn kho và tạo WarehouseTransaction cho từng chi tiết có chênh lệch
        processStocktakeCompletion(stocktake);

        // Cập nhật trạng thái phiếu kiểm kê
        stocktake.setStatus(StocktakeStatus.COMPLETED);
        stocktake.setCompletedBy(completedByEmployee);
        stocktake.setCompletedAt(LocalDateTime.now());

        stocktake = stocktakeRepository.save(stocktake);
        log.info("Đã hoàn thành phiếu kiểm kê với ID: {}", stocktakeId);

        return mapToStocktakeDto(stocktake);
    }

    /**
     * Xóa phiếu kiểm kê
     */
    @Override
    @Transactional
    public void deleteStocktake(Integer stocktakeId) {
        log.info("Xóa phiếu kiểm kê với ID: {}", stocktakeId);

        Stocktake stocktake = stocktakeRepository.findById(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        // Chỉ cho phép xóa phiếu kiểm kê PENDING
        if (stocktake.getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể xóa phiếu kiểm kê đã hoàn thành");
        }

        // Xóa chi tiết trước
        stocktakeDetailRepository.deleteByStocktakeId(stocktakeId);

        // Xóa phiếu kiểm kê
        stocktakeRepository.delete(stocktake);

        log.info("Đã xóa phiếu kiểm kê với ID: {}", stocktakeId);
    }

    /**
     * Lấy danh sách phiếu kiểm kê với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StocktakeDto> getAllStocktakes(Pageable pageable) {
        log.info("Lấy danh sách phiếu kiểm kê với phân trang: {}", pageable);

        Page<Stocktake> stocktakes = stocktakeRepository.findAllWithEmployeeDetails(pageable);
        return stocktakes.map(this::mapToStocktakeDto);
    }

    /**
     * Lấy danh sách phiếu kiểm kê theo trạng thái
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StocktakeDto> getStocktakesByStatus(StocktakeStatus status, Pageable pageable) {
        log.info("Lấy danh sách phiếu kiểm kê theo trạng thái: {} với phân trang: {}", status, pageable);

        Page<Stocktake> stocktakes = stocktakeRepository.findByStatus(status, pageable);
        return stocktakes.map(this::mapToStocktakeDto);
    }

    /**
     * Tìm kiếm phiếu kiểm kê theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StocktakeDto> searchStocktakes(String keyword, Pageable pageable) {
        log.info("Tìm kiếm phiếu kiểm kê theo từ khóa: {} với phân trang: {}", keyword, pageable);

        Page<Stocktake> stocktakes = stocktakeRepository.findByKeyword(keyword, pageable);
        return stocktakes.map(this::mapToStocktakeDto);
    }

    /**
     * Lấy danh sách phiếu kiểm kê trong khoảng thời gian
     */
    @Override
    @Transactional(readOnly = true)
    public Page<StocktakeDto> getStocktakesByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        log.info("Lấy danh sách phiếu kiểm kê từ {} đến {} với phân trang: {}", startDate, endDate, pageable);

        Page<Stocktake> stocktakes = stocktakeRepository.findByCreatedAtBetween(startDate, endDate, pageable);
        return stocktakes.map(this::mapToStocktakeDto);
    }

    /**
     * Lấy danh sách chi tiết kiểm kê theo phiếu kiểm kê
     */
    @Override
    @Transactional(readOnly = true)
    public List<StocktakeDetailDto> getStocktakeDetails(Integer stocktakeId) {
        log.info("Lấy danh sách chi tiết kiểm kê cho phiếu ID: {}", stocktakeId);

        List<StocktakeDetail> details = stocktakeDetailRepository.findByStocktakeIdWithProductUnitDetails(stocktakeId);
        return details.stream()
                .map(this::mapToStocktakeDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Thêm chi tiết kiểm kê vào phiếu kiểm kê
     */
    @Override
    @Transactional
    public StocktakeDetailDto addStocktakeDetail(StocktakeDetailCreateRequest request) {
        log.info("Thêm chi tiết kiểm kê cho phiếu ID: {} và đơn vị sản phẩm ID: {}",
                request.getStocktakeId(), request.getProductUnitId());

        // Kiểm tra phiếu kiểm kê
        Stocktake stocktake = stocktakeRepository.findById(request.getStocktakeId())
                .orElseThrow(() -> new StocktakeNotFoundException(request.getStocktakeId()));

        if (stocktake.getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể thêm chi tiết vào phiếu kiểm kê đã hoàn thành");
        }

        // Kiểm tra đơn vị sản phẩm
        ProductUnit productUnit = productUnitRepository.findById(request.getProductUnitId())
                .orElseThrow(() -> new StocktakeException(
                        "Không tìm thấy đơn vị sản phẩm với ID: " + request.getProductUnitId()));

        // Kiểm tra trùng lặp
        if (stocktakeDetailRepository.existsByStocktakeIdAndProductUnitId(request.getStocktakeId(),
                request.getProductUnitId())) {
            throw new StocktakeException("Chi tiết kiểm kê cho đơn vị sản phẩm này đã tồn tại trong phiếu");
        }

        // Lấy số lượng tồn kho hiện tại
        Integer currentStock = warehouseRepository.findByProductUnitId(request.getProductUnitId())
                .map(Warehouse::getQuantityOnHand)
                .orElse(0);

        // Tạo chi tiết kiểm kê
        StocktakeDetail detail = new StocktakeDetail();
        detail.setStocktake(stocktake);
        detail.setProductUnit(productUnit);
        detail.setQuantityExpected(currentStock);
        detail.setQuantityCounted(request.getQuantityCounted());
        detail.setReason(request.getReason());

        // Tính toán chênh lệch sẽ được thực hiện tự động qua @PrePersist

        detail = stocktakeDetailRepository.save(detail);
        log.info("Đã thêm chi tiết kiểm kê với ID: {}", detail.getStocktakeDetailId());

        return mapToStocktakeDetailDto(detail);
    }

    /**
     * Cập nhật chi tiết kiểm kê
     */
    @Override
    @Transactional
    public StocktakeDetailDto updateStocktakeDetail(Integer detailId, Integer quantityCounted, String reason) {
        log.info("Cập nhật chi tiết kiểm kê với ID: {}", detailId);

        StocktakeDetail detail = stocktakeDetailRepository.findById(detailId)
                .orElseThrow(() -> new StocktakeException("Không tìm thấy chi tiết kiểm kê với ID: " + detailId));

        // Kiểm tra trạng thái phiếu kiểm kê
        if (detail.getStocktake().getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể cập nhật chi tiết của phiếu kiểm kê đã hoàn thành");
        }

        // Cập nhật thông tin
        if (quantityCounted != null) {
            detail.setQuantityCounted(quantityCounted);
        }
        if (reason != null) {
            detail.setReason(reason);
        }

        detail = stocktakeDetailRepository.save(detail);
        log.info("Đã cập nhật chi tiết kiểm kê với ID: {}", detailId);

        return mapToStocktakeDetailDto(detail);
    }

    /**
     * Xóa chi tiết kiểm kê
     */
    @Override
    @Transactional
    public void deleteStocktakeDetail(Integer detailId) {
        log.info("Xóa chi tiết kiểm kê với ID: {}", detailId);

        StocktakeDetail detail = stocktakeDetailRepository.findById(detailId)
                .orElseThrow(() -> new StocktakeException("Không tìm thấy chi tiết kiểm kê với ID: " + detailId));

        // Kiểm tra trạng thái phiếu kiểm kê
        if (detail.getStocktake().getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể xóa chi tiết của phiếu kiểm kê đã hoàn thành");
        }

        stocktakeDetailRepository.delete(detail);
        log.info("Đã xóa chi tiết kiểm kê với ID: {}", detailId);
    }

    /**
     * Lấy danh sách chi tiết có chênh lệch
     */
    @Override
    @Transactional(readOnly = true)
    public List<StocktakeDetailDto> getStocktakeDifferences(Integer stocktakeId) {
        log.info("Lấy danh sách chi tiết có chênh lệch cho phiếu ID: {}", stocktakeId);

        List<StocktakeDetail> differences = stocktakeDetailRepository.findDifferencesByStocktakeId(stocktakeId);
        return differences.stream()
                .map(this::mapToStocktakeDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Tạo phiếu kiểm kê từ danh sách tồn kho hiện tại
     */
    @Override
    @Transactional
    public StocktakeDto createStocktakeFromCurrentStock(String notes, List<Long> productUnitIds,
            StocktakeStatus status) {
        log.info("Tạo phiếu kiểm kê từ tồn kho hiện tại cho {} đơn vị sản phẩm với trạng thái {}",
                productUnitIds != null ? productUnitIds.size() : "tất cả", status);

        // Lấy thông tin nhân viên hiện tại từ SecurityContext
        Employee createdByEmployee = securityUtil.getCurrentEmployee();

        // Tạo mã phiếu kiểm kê
        String stocktakeCode = generateStocktakeCode();

        // Tạo entity Stocktake
        Stocktake stocktake = new Stocktake();
        stocktake.setStocktakeCode(stocktakeCode);
        stocktake.setNotes(notes);
        stocktake.setCreatedBy(createdByEmployee);

        // Xử lý trạng thái - mặc định là PENDING nếu không được chỉ định
        StocktakeStatus finalStatus = status != null ? status : StocktakeStatus.PENDING;
        stocktake.setStatus(finalStatus);

        // Nếu tạo trực tiếp với trạng thái COMPLETED, set thông tin hoàn thành
        if (finalStatus == StocktakeStatus.COMPLETED) {
            stocktake.setCompletedBy(createdByEmployee);
            stocktake.setCompletedAt(LocalDateTime.now());
        }

        // Lưu phiếu kiểm kê
        stocktake = stocktakeRepository.save(stocktake);

        // Lấy danh sách tồn kho
        List<Warehouse> warehouses;
        if (productUnitIds != null && !productUnitIds.isEmpty()) {
            warehouses = productUnitIds.stream()
                    .map(warehouseRepository::findByProductUnitId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else {
            warehouses = warehouseRepository.findAll();
        }

        // Tạo chi tiết kiểm kê từ tồn kho hiện tại
        for (Warehouse warehouse : warehouses) {
            StocktakeDetail detail = new StocktakeDetail();
            detail.setStocktake(stocktake);
            detail.setProductUnit(warehouse.getProductUnit());
            detail.setQuantityExpected(warehouse.getQuantityOnHand());
            detail.setQuantityCounted(warehouse.getQuantityOnHand()); // Mặc định = số lượng hiện tại
            detail.setReason("");

            stocktakeDetailRepository.save(detail);
        }

        // Nếu tạo trực tiếp với trạng thái COMPLETED, cập nhật tồn kho
        if (finalStatus == StocktakeStatus.COMPLETED) {
            log.info("Xử lý hoàn thành phiếu kiểm kê từ tồn kho với trạng thái COMPLETED");
            processStocktakeCompletion(stocktake);
            // Lưu lại sau khi xử lý hoàn thành
            stocktake = stocktakeRepository.save(stocktake);
        }

        log.info("Đã tạo phiếu kiểm kê từ tồn kho với {} chi tiết", warehouses.size());
        return mapToStocktakeDto(stocktake);
    }

    /**
     * Kiểm tra xem có phiếu kiểm kê nào đang PENDING không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingStocktake() {
        return stocktakeRepository.existsPendingStocktake();
    }

    /**
     * Lấy thống kê tổng quan về kiểm kê
     */
    @Override
    @Transactional(readOnly = true)
    public StocktakeDto.StocktakeSummary getStocktakeSummary(Integer stocktakeId) {
        log.info("Lấy thống kê tổng quan cho phiếu kiểm kê ID: {}", stocktakeId);

        // Kiểm tra phiếu kiểm kê tồn tại
        if (!stocktakeRepository.existsById(stocktakeId)) {
            throw new StocktakeNotFoundException(stocktakeId);
        }

        Long totalItems = stocktakeDetailRepository.countByStocktakeId(stocktakeId);
        Long itemsWithDifference = stocktakeDetailRepository.countDifferencesByStocktakeId(stocktakeId);
        Integer totalPositiveDifference = stocktakeDetailRepository.sumPositiveDifferencesByStocktakeId(stocktakeId);
        Integer totalNegativeDifference = stocktakeDetailRepository.sumNegativeDifferencesByStocktakeId(stocktakeId);
        Integer netDifference = totalPositiveDifference + totalNegativeDifference;

        return new StocktakeDto.StocktakeSummary(
                totalItems,
                itemsWithDifference,
                totalPositiveDifference,
                totalNegativeDifference,
                netDifference);
    }

    /**
     * Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê
     */
    @Override
    @Transactional
    public StocktakeDto refreshExpectedQuantities(Integer stocktakeId) {
        log.info("Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê ID: {}", stocktakeId);

        // Kiểm tra phiếu kiểm kê tồn tại
        Stocktake stocktake = stocktakeRepository.findById(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        // Kiểm tra trạng thái - chỉ cho phép cập nhật khi PENDING
        if (stocktake.getStatus() == StocktakeStatus.COMPLETED) {
            throw new StocktakeException("Không thể cập nhật số lượng tồn kho cho phiếu kiểm kê đã hoàn thành");
        }

        // Cập nhật số lượng tồn kho mới nhất
        updateExpectedQuantitiesWithCurrentStock(stocktakeId);

        // Lấy lại thông tin phiếu kiểm kê sau khi cập nhật
        stocktake = stocktakeRepository.findByIdWithDetails(stocktakeId)
                .orElseThrow(() -> new StocktakeNotFoundException(stocktakeId));

        log.info("Đã cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê ID: {}", stocktakeId);
        return mapToStocktakeDto(stocktake);
    }

    /**
     * Xử lý mã phiếu kiểm kê: sử dụng mã truyền vào hoặc tự động tạo mới
     *
     * @param requestedCode mã phiếu kiểm kê được yêu cầu (có thể null hoặc empty)
     * @return mã phiếu kiểm kê hợp lệ
     * @throws DuplicateStocktakeCodeException nếu mã đã tồn tại
     */
    private String processStocktakeCode(String requestedCode) {
        if (requestedCode != null && !requestedCode.trim().isEmpty()) {
            // Kiểm tra mã đã tồn tại hay chưa
            if (stocktakeRepository.existsByStocktakeCode(requestedCode.trim())) {
                throw new DuplicateStocktakeCodeException(requestedCode.trim());
            }
            log.info("Sử dụng mã phiếu kiểm kê được yêu cầu: {}", requestedCode.trim());
            return requestedCode.trim();
        } else {
            // Tự động tạo mã mới
            return generateStocktakeCode();
        }
    }

    /**
     * Tạo mã phiếu kiểm kê tự động
     */
    private String generateStocktakeCode() {
        String prefix = "KK";
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Tìm số thứ tự trong ngày
        String pattern = prefix + dateStr + "%";
        long count = stocktakeRepository.count(); // Simplified - có thể cải thiện để đếm theo ngày

        String sequence = String.format("%03d", count + 1);
        return prefix + dateStr + sequence;
    }

    /**
     * Tạo chi tiết kiểm kê từ danh sách request
     */
    private void createStocktakeDetails(Stocktake stocktake,
            List<StocktakeCreateRequest.StocktakeDetailCreateRequest> detailRequests) {
        for (StocktakeCreateRequest.StocktakeDetailCreateRequest detailRequest : detailRequests) {
            // Kiểm tra đơn vị sản phẩm
            ProductUnit productUnit = productUnitRepository.findById(detailRequest.getProductUnitId())
                    .orElseThrow(() -> new StocktakeException(
                            "Không tìm thấy đơn vị sản phẩm với ID: " + detailRequest.getProductUnitId()));

            // Lấy số lượng tồn kho hiện tại
            Integer currentStock = warehouseRepository.findByProductUnitId(detailRequest.getProductUnitId())
                    .map(Warehouse::getQuantityOnHand)
                    .orElse(0);

            // Tạo chi tiết kiểm kê
            StocktakeDetail detail = new StocktakeDetail();
            detail.setStocktake(stocktake);
            detail.setProductUnit(productUnit);
            detail.setQuantityExpected(currentStock);
            detail.setQuantityCounted(detailRequest.getQuantityCounted());
            detail.setReason(detailRequest.getReason());

            stocktakeDetailRepository.save(detail);
        }
    }

    /**
     * Cập nhật chi tiết kiểm kê từ danh sách request
     */
    private void updateStocktakeDetails(Stocktake stocktake,
            List<StocktakeUpdateRequest.StocktakeDetailUpdateRequest> detailRequests) {
        for (StocktakeUpdateRequest.StocktakeDetailUpdateRequest detailRequest : detailRequests) {
            StocktakeDetail detail = stocktakeDetailRepository.findById(detailRequest.getStocktakeDetailId())
                    .orElseThrow(() -> new StocktakeException(
                            "Không tìm thấy chi tiết kiểm kê với ID: " + detailRequest.getStocktakeDetailId()));

            // Cập nhật thông tin
            if (detailRequest.getQuantityCounted() != null) {
                detail.setQuantityCounted(detailRequest.getQuantityCounted());
            }
            if (detailRequest.getReason() != null) {
                detail.setReason(detailRequest.getReason());
            }

            stocktakeDetailRepository.save(detail);
        }
    }

    /**
     * Cập nhật số lượng tồn kho mới nhất cho tất cả chi tiết kiểm kê
     * Đảm bảo quantityExpected phản ánh đúng số lượng tồn kho hiện tại
     */
    private void updateExpectedQuantitiesWithCurrentStock(Integer stocktakeId) {
        log.info("Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê ID: {}", stocktakeId);

        List<StocktakeDetail> details = stocktakeDetailRepository
                .findByStocktakeIdWithProductUnitDetails(stocktakeId);

        for (StocktakeDetail detail : details) {
            // Lấy số lượng tồn kho hiện tại từ bảng Warehouse
            Integer currentStock = warehouseService.getCurrentStock(detail.getProductUnit().getId());

            // Cập nhật quantityExpected với số lượng tồn kho mới nhất
            Integer oldExpected = detail.getQuantityExpected();
            detail.setQuantityExpected(currentStock);

            // Tính toán lại chênh lệch (sẽ được tự động tính trong @PreUpdate)
            detail.calculateDifference();

            log.debug(
                    "Cập nhật chi tiết kiểm kê ID: {} - ProductUnit ID: {} - Expected: {} -> {} - Counted: {} - Difference: {}",
                    detail.getStocktakeDetailId(),
                    detail.getProductUnit().getId(),
                    oldExpected,
                    currentStock,
                    detail.getQuantityCounted(),
                    detail.getQuantityDifference());
        }

        // Lưu tất cả thay đổi
        stocktakeDetailRepository.saveAll(details);
        log.info("Đã cập nhật {} chi tiết kiểm kê với số lượng tồn kho mới nhất", details.size());
    }

    /**
     * Xử lý hoàn thành kiểm kê - cập nhật tồn kho và tạo WarehouseTransaction
     */
    private void processStocktakeCompletion(Stocktake stocktake) {
        log.info("Xử lý hoàn thành kiểm kê cho phiếu ID: {}", stocktake.getStocktakeId());

        List<StocktakeDetail> details = stocktakeDetailRepository
                .findByStocktakeIdWithProductUnitDetails(stocktake.getStocktakeId());

        for (StocktakeDetail detail : details) {
            if (detail.getQuantityDifference() != 0) {
                // Cập nhật tồn kho
                updateWarehouseStock(detail);

                // Tạo WarehouseTransaction
                createWarehouseTransaction(detail, stocktake.getStocktakeCode());
            }
        }
    }

    /**
     * Cập nhật tồn kho
     */
    private void updateWarehouseStock(StocktakeDetail detail) {
        Warehouse warehouse = warehouseRepository.findByProductUnitId(detail.getProductUnit().getId())
                .orElseThrow(() -> new StocktakeException(
                        "Không tìm thấy tồn kho cho đơn vị sản phẩm ID: " + detail.getProductUnit().getId()));

        // Cập nhật số lượng tồn kho
        warehouse.setQuantityOnHand(detail.getQuantityCounted());
        warehouseRepository.save(warehouse);

        log.info("Đã cập nhật tồn kho cho đơn vị sản phẩm ID: {} từ {} thành {}",
                detail.getProductUnit().getId(),
                detail.getQuantityExpected(),
                detail.getQuantityCounted());
    }

    /**
     * Tạo WarehouseTransaction cho kiểm kê
     */
    private void createWarehouseTransaction(StocktakeDetail detail, String stocktakeCode) {
        WarehouseTransaction transaction = new WarehouseTransaction();
        transaction.setProductUnit(detail.getProductUnit());
        transaction.setBeforeQuantity(detail.getQuantityExpected());
        transaction.setQuantityChange(detail.getQuantityDifference());
        transaction.setNewQuantity(detail.getQuantityCounted());
        transaction.setTransactionType(WarehouseTransaction.TransactionType.ADJUSTMENT);
        transaction.setReferenceId(stocktakeCode);
        transaction
                .setNotes("Điều chỉnh tồn kho từ kiểm kê: " + (detail.getReason() != null ? detail.getReason() : ""));

        warehouseTransactionRepository.save(transaction);

        log.info("Đã tạo WarehouseTransaction cho đơn vị sản phẩm ID: {} với chênh lệch: {}",
                detail.getProductUnit().getId(),
                detail.getQuantityDifference());
    }

    /**
     * Mapping từ Stocktake entity sang StocktakeDto
     */
    private StocktakeDto mapToStocktakeDto(Stocktake stocktake) {
        StocktakeDto dto = new StocktakeDto();
        dto.setStocktakeId(stocktake.getStocktakeId());
        dto.setStocktakeCode(stocktake.getStocktakeCode());
        dto.setStatus(stocktake.getStatus());
        dto.setNotes(stocktake.getNotes());
        dto.setCompletedAt(stocktake.getCompletedAt());
        dto.setCreatedAt(stocktake.getCreatedAt());
        dto.setUpdatedAt(stocktake.getUpdatedAt());

        // Map thông tin nhân viên tạo
        if (stocktake.getCreatedBy() != null) {
            dto.setCreatedBy(new StocktakeDto.EmployeeInfo(
                    stocktake.getCreatedBy().getEmployeeId(),
                    stocktake.getCreatedBy().getName(),
                    stocktake.getCreatedBy().getEmail()));
        }

        // Map thông tin nhân viên hoàn thành
        if (stocktake.getCompletedBy() != null) {
            dto.setCompletedBy(new StocktakeDto.EmployeeInfo(
                    stocktake.getCompletedBy().getEmployeeId(),
                    stocktake.getCompletedBy().getName(),
                    stocktake.getCompletedBy().getEmail()));
        }

        // Map chi tiết kiểm kê
        if (stocktake.getStocktakeDetails() != null) {
            dto.setStocktakeDetails(stocktake.getStocktakeDetails().stream()
                    .map(this::mapToStocktakeDetailDto)
                    .collect(Collectors.toList()));
        }

        // Tính thống kê tổng quan
        if (stocktake.getStocktakeId() != null) {
            dto.setSummary(getStocktakeSummary(stocktake.getStocktakeId()));
        }

        return dto;
    }

    /**
     * Mapping từ StocktakeDetail entity sang StocktakeDetailDto
     */
    private StocktakeDetailDto mapToStocktakeDetailDto(StocktakeDetail detail) {
        StocktakeDetailDto dto = new StocktakeDetailDto();
        dto.setStocktakeDetailId(detail.getStocktakeDetailId());
        dto.setQuantityExpected(detail.getQuantityExpected());
        dto.setQuantityCounted(detail.getQuantityCounted());
        dto.setQuantityDifference(detail.getQuantityDifference());
        dto.setQuantityIncrease(detail.getQuantityIncrease());
        dto.setQuantityDecrease(detail.getQuantityDecrease());
        dto.setReason(detail.getReason());
        dto.setCreatedAt(detail.getCreatedAt());

        // Map thông tin đơn vị sản phẩm
        if (detail.getProductUnit() != null) {
            ProductUnit productUnit = detail.getProductUnit();
            dto.setProductUnit(new StocktakeDetailDto.ProductUnitInfo(
                    productUnit.getId(),
                    productUnit.getBarcode(),
                    productUnit.getConversionValue(),
                    productUnit.getProduct() != null ? productUnit.getProduct().getName() : null,
                    productUnit.getUnit() != null ? productUnit.getUnit().getName() : null));
        }

        return dto;
    }
}
