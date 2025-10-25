package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.imports.ImportCreateRequest;
import iuh.fit.supermarket.dto.imports.ImportResponse;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.exception.DuplicateImportCodeException;
import iuh.fit.supermarket.exception.ImportCodeOverflowException;
import iuh.fit.supermarket.exception.ImportException;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.ImportService;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ImportService
 * Xử lý logic nghiệp vụ cho chức năng nhập hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImportServiceImpl implements ImportService {

    private final ImportRepository importRepository;
    private final ImportDetailRepository importDetailRepository;
    private final ProductUnitRepository productUnitRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;
    private final WarehouseService warehouseService;

    /**
     * Tạo phiếu nhập hàng mới
     * Xử lý nhập hàng theo từng biến thể sản phẩm với transaction đảm bảo tính
     * nhất
     * quán
     */
    @Override
    @Transactional
    public ImportResponse createImport(ImportCreateRequest request, Integer employeeId) {
        log.info("Bắt đầu tạo phiếu nhập hàng cho nhà cung cấp ID: {}, nhân viên ID: {}",
                request.getSupplierId(), employeeId);

        try {
            // 1. Validate dữ liệu đầu vào
            validateImportRequest(request, employeeId);

            // 2. Xử lý mã phiếu nhập
            String importCode = processImportCode(request.getImportCode());

            // 3. Lấy thông tin nhà cung cấp và nhân viên
            Supplier supplier = supplierRepository.findBySupplierIdAndIsDeletedFalse(request.getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy nhà cung cấp với ID: " + request.getSupplierId()));

            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + employeeId));

            // 4. Tạo phiếu nhập chính
            Import importRecord = new Import();
            importRecord.setImportCode(importCode);
            importRecord.setImportDate(request.getImportDate() != null ? request.getImportDate() : LocalDateTime.now());
            importRecord.setNotes(request.getNotes());
            importRecord.setSupplier(supplier);
            importRecord.setCreatedBy(employee);

            // 5. Lưu phiếu nhập để có ID
            importRecord = importRepository.save(importRecord);
            log.info("Đã tạo phiếu nhập với mã: {}", importCode);

            // 6. Xử lý từng chi tiết nhập hàng
            List<ImportDetail> importDetails = processImportDetails(request.getImportDetails(), importRecord);

            // 7. Lưu chi tiết nhập hàng
            importDetailRepository.saveAll(importDetails);
            importRecord.setImportDetails(importDetails);

            log.info("Hoàn thành tạo phiếu nhập {} với {} sản phẩm", importCode,
                    importDetails.size());

            // 8. Chuyển đổi sang DTO và trả về
            return convertToResponse(importRecord);

        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu nhập hàng: ", e);
            throw new ImportException("Không thể tạo phiếu nhập hàng: " + e.getMessage(),
                    e);
        }
    }

    /**
     * Validate dữ liệu đầu vào cho yêu cầu tạo phiếu nhập
     */
    private void validateImportRequest(ImportCreateRequest request, Integer employeeId) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu tạo phiếu nhập không được null");
        }

        if (request.getSupplierId() == null) {
            throw new IllegalArgumentException("ID nhà cung cấp không được để trống");
        }

        if (employeeId == null) {
            throw new IllegalArgumentException("ID nhân viên không được để trống");
        }

        if (request.getImportDetails() == null ||
                request.getImportDetails().isEmpty()) {
            throw new IllegalArgumentException("Danh sách sản phẩm nhập không được để trống");
        }

        // Validate từng chi tiết
        for (ImportCreateRequest.ImportDetailRequest detail : request.getImportDetails()) {
            if (detail.getProductUnitId() == null) {
                throw new IllegalArgumentException("ID đơn vị sản phẩm không được để trống");
            }
            if (detail.getQuantity() == null || detail.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
            }
        }
    }

    /**
     * Xử lý từng chi tiết nhập hàng
     * - Kiểm tra đơn vị sản phẩm có tồn tại và active
     * - Cập nhật tồn kho
     * - Ghi nhận giao dịch
     */
    private List<ImportDetail> processImportDetails(List<ImportCreateRequest.ImportDetailRequest> detailRequests,
            Import importRecord) {
        return detailRequests.stream().map(detailRequest -> {
            // 1. Kiểm tra đơn vị sản phẩm
            ProductUnit productUnit = productUnitRepository.findById(detailRequest.getProductUnitId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy đơn vị sản phẩm với ID: " + detailRequest.getProductUnitId()));

            // Kiểm tra đơn vị sản phẩm có hoạt động và chưa bị xóa
            if (!productUnit.getIsActive() || productUnit.getIsDeleted()) {
                throw new IllegalArgumentException(
                        "Đơn vị sản phẩm không hoạt động hoặc đã bị xóa với ID: " +
                                detailRequest.getProductUnitId());
            }

            log.debug("Xử lý nhập hàng cho đơn vị sản phẩm: {} - {} - Số lượng: {}",
                    productUnit.getProduct().getName(), productUnit.getUnit().getName(), detailRequest.getQuantity());

            // 2. Cập nhật tồn kho thông qua WarehouseService
            warehouseService.stockIn(
                    productUnit.getId(),
                    detailRequest.getQuantity(),
                    importRecord.getImportCode(),
                    "Nhập hàng từ " + importRecord.getSupplier().getName());

            // 3. Tạo chi tiết nhập hàng
            ImportDetail importDetail = new ImportDetail();
            importDetail.setImportRecord(importRecord);
            importDetail.setProductUnit(productUnit);
            importDetail.setQuantity(detailRequest.getQuantity());

            return importDetail;

        }).collect(Collectors.toList());
    }

    /**
     * Xử lý mã phiếu nhập: sử dụng mã truyền vào hoặc tự động tạo mới
     *
     * @param requestedCode mã phiếu nhập được yêu cầu (có thể null)
     * @return mã phiếu nhập hợp lệ
     * @throws DuplicateImportCodeException nếu mã đã tồn tại
     */
    private String processImportCode(String requestedCode) {
        if (requestedCode != null && !requestedCode.trim().isEmpty()) {
            // Kiểm tra mã đã tồn tại hay chưa
            if (importRepository.existsByImportCode(requestedCode)) {
                throw new DuplicateImportCodeException(requestedCode);
            }
            log.info("Sử dụng mã phiếu nhập được yêu cầu: {}", requestedCode);
            return requestedCode;
        } else {
            // Tự động tạo mã mới
            return generateImportCode();
        }
    }

    /**
     * Tạo mã phiếu nhập tự động
     * Format mới: PN + 6 chữ số (PN000001 đến PN999999)
     *
     * @return mã phiếu nhập mới
     * @throws ImportCodeOverflowException nếu đạt tới giới hạn tối đa
     */
    @Override
    public String generateImportCode() {
        // Tìm mã phiếu nhập lớn nhất hiện có
        List<String> maxCodes = importRepository.findMaxImportCodeWithPNFormat();

        int nextSequence = 1; // Mặc định bắt đầu từ 1

        if (!maxCodes.isEmpty()) {
            String maxCode = maxCodes.get(0); // Lấy mã lớn nhất
            if (maxCode != null && maxCode.startsWith("PN") && maxCode.length() == 8) {
                try {
                    // Lấy phần số từ mã (6 chữ số cuối)
                    String numberPart = maxCode.substring(2);
                    int currentMax = Integer.parseInt(numberPart);

                    // Kiểm tra overflow
                    if (currentMax >= 999999) {
                        throw new ImportCodeOverflowException();
                    }

                    nextSequence = currentMax + 1;
                } catch (NumberFormatException e) {
                    log.warn("Không thể parse mã phiếu nhập: {}. Sử dụng sequence mặc định.",
                            maxCode);
                    nextSequence = 1;
                }
            }
        }

        // Tạo mã mới với format PN + 6 chữ số
        String newCode = String.format("PN%06d", nextSequence);

        // Kiểm tra trùng lặp (phòng trường hợp race condition)
        if (importRepository.existsByImportCode(newCode)) {
            log.warn("Mã phiếu nhập {} đã tồn tại, thử tạo mã khác", newCode);
            // Thử tìm mã tiếp theo có sẵn
            return findNextAvailableCode(nextSequence);
        }

        log.info("Tạo mã phiếu nhập mới: {}", newCode);
        return newCode;
    }

    /**
     * Tìm mã phiếu nhập tiếp theo có sẵn (xử lý race condition)
     *
     * @param startSequence sequence bắt đầu tìm kiếm
     * @return mã phiếu nhập có sẵn
     * @throws ImportCodeOverflowException nếu không tìm được mã có sẵn
     */
    private String findNextAvailableCode(int startSequence) {
        for (int i = startSequence; i <= 999999; i++) {
            String candidateCode = String.format("PN%06d", i);
            if (!importRepository.existsByImportCode(candidateCode)) {
                log.info("Tìm thấy mã phiếu nhập có sẵn: {}", candidateCode);
                return candidateCode;
            }
        }

        // Nếu không tìm được mã nào có sẵn
        throw new ImportCodeOverflowException();
    }

    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private ImportResponse convertToResponse(Import importRecord) {
        ImportResponse response = new ImportResponse();
        response.setImportId(importRecord.getImportId());
        response.setImportCode(importRecord.getImportCode());
        response.setImportDate(importRecord.getImportDate());
        response.setNotes(importRecord.getNotes());
        response.setCreatedAt(importRecord.getCreatedAt());

        // Convert supplier
        if (importRecord.getSupplier() != null) {
            response.setSupplier(convertSupplierToDto(importRecord.getSupplier()));
        }

        // Convert employee
        if (importRecord.getCreatedBy() != null) {
            response.setCreatedBy(convertEmployeeToDto(importRecord.getCreatedBy()));
        }

        // Convert import details
        if (importRecord.getImportDetails() != null) {
            List<ImportResponse.ImportDetailResponse> detailResponses = importRecord.getImportDetails().stream()
                    .map(this::convertImportDetailToResponse)
                    .collect(Collectors.toList());
            response.setImportDetails(detailResponses);

            // Calculate totals
            response.setTotalQuantity(detailResponses.stream()
                    .mapToInt(ImportResponse.ImportDetailResponse::getQuantity)
                    .sum());
            response.setTotalUnits(detailResponses.size());
        }

        return response;
    }

    /**
     * Helper methods để chuyển đổi Entity sang DTO
     */
    private iuh.fit.supermarket.dto.supplier.SupplierDto convertSupplierToDto(Supplier supplier) {
        iuh.fit.supermarket.dto.supplier.SupplierDto dto = new iuh.fit.supermarket.dto.supplier.SupplierDto();
        dto.setSupplierId(supplier.getSupplierId());
        dto.setCode(supplier.getCode());
        dto.setName(supplier.getName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setIsActive(supplier.getIsActive());
        return dto;
    }

    private iuh.fit.supermarket.dto.employee.EmployeeDto convertEmployeeToDto(Employee employee) {
        return iuh.fit.supermarket.dto.employee.EmployeeDto.fromEntity(employee);
    }

    private ImportResponse.ImportDetailResponse convertImportDetailToResponse(ImportDetail detail) {
        ImportResponse.ImportDetailResponse response = new ImportResponse.ImportDetailResponse();
        response.setImportDetailId(detail.getImportDetailId());
        response.setQuantity(detail.getQuantity());
        response.setCreatedAt(detail.getCreatedAt());

        if (detail.getProductUnit() != null) {
            ImportResponse.ImportDetailResponse.ProductUnitInfo productUnitInfo = new ImportResponse.ImportDetailResponse.ProductUnitInfo();
            productUnitInfo.setProductUnitId(detail.getProductUnit().getId());
            productUnitInfo.setBarcode(detail.getProductUnit().getBarcode());
            productUnitInfo.setConversionValue(detail.getProductUnit().getConversionValue());

            if (detail.getProductUnit().getProduct() != null) {
                productUnitInfo.setProductName(detail.getProductUnit().getProduct().getName());
            }

            if (detail.getProductUnit().getUnit() != null) {
                productUnitInfo.setUnit(detail.getProductUnit().getUnit().getName());
            }

            response.setProductUnit(productUnitInfo);
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ImportResponse getImportById(Integer importId) {
        log.debug("Lấy thông tin phiếu nhập ID: {}", importId);

        Import importRecord = importRepository.findById(importId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu nhập với ID: " + importId));

        return convertToResponse(importRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportResponse> getAllImports(Pageable pageable) {
        log.debug("Lấy danh sách tất cả phiếu nhập với phân trang");

        Page<Import> imports = importRepository.findAllWithDetails(pageable);
        return imports.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportResponse> getImportsBySupplier(Integer supplierId, Pageable pageable) {
        log.debug("Lấy danh sách phiếu nhập theo nhà cung cấp ID: {}", supplierId);

        Page<Import> imports = importRepository.findBySupplier(supplierId, pageable);
        return imports.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportResponse> getImportsByDateRange(LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        log.debug("Lấy danh sách phiếu nhập từ {} đến {}", startDate, endDate);

        Page<Import> imports = importRepository.findByDateRange(startDate, endDate,
                pageable);
        return imports.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportResponse> searchImports(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm phiếu nhập với từ khóa: {}", keyword);

        Page<Import> imports = importRepository.findByKeyword(keyword, pageable);
        return imports.map(this::convertToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportResponse> getImportsByEmployee(Integer employeeId) {
        log.debug("Lấy danh sách phiếu nhập theo nhân viên ID: {}", employeeId);

        List<Import> imports = importRepository.findByCreatedBy(employeeId);
        return imports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer importId) {
        return importRepository.existsById(importId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByImportCode(String importCode) {
        return importRepository.existsByImportCode(importCode);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportStatistics getImportStatisticsBySupplier(Integer supplierId) {
        // Implementation đơn giản - có thể cải thiện với native query
        List<Import> imports = importRepository.findBySupplier(supplierId);
        return calculateStatistics(imports);
    }

    @Override
    @Transactional(readOnly = true)
    public ImportStatistics getImportStatisticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Import> imports = importRepository.findByDateRange(startDate, endDate);
        return calculateStatistics(imports);
    }

    private ImportStatistics calculateStatistics(List<Import> imports) {
        ImportStatistics stats = new ImportStatistics();
        stats.setTotalImports((long) imports.size());

        int totalQuantity = 0;
        int totalVariants = 0;

        for (Import imp : imports) {
            if (imp.getImportDetails() != null) {
                totalQuantity += imp.getImportDetails().stream()
                        .mapToInt(ImportDetail::getQuantity)
                        .sum();
                totalVariants += imp.getImportDetails().size();
            }
        }

        stats.setTotalQuantity(totalQuantity);
        stats.setTotalVariants(totalVariants);
        stats.setTotalSuppliers(1); // Simplified

        return stats;
    }
}
