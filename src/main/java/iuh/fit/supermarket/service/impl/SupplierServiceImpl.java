package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteRequest;
import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteResponse;
import iuh.fit.supermarket.dto.supplier.SupplierCreateRequest;
import iuh.fit.supermarket.dto.supplier.SupplierPageableRequest;
import iuh.fit.supermarket.dto.supplier.SupplierResponse;
import iuh.fit.supermarket.dto.supplier.SupplierUpdateRequest;
import iuh.fit.supermarket.entity.Supplier;
import iuh.fit.supermarket.repository.SupplierRepository;
import iuh.fit.supermarket.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của SupplierService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    /**
     * Tạo mới nhà cung cấp
     */
    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        log.info("Tạo nhà cung cấp mới với tên: {}", request.getName());

        try {
            // Validate và tạo mã nhà cung cấp nếu chưa có
            String supplierCode = request.getCode();
            if (!StringUtils.hasText(supplierCode)) {
                supplierCode = generateSupplierCode();
            } else {
                // Kiểm tra mã đã tồn tại chưa
                if (supplierRepository.existsByCode(supplierCode)) {
                    throw new IllegalArgumentException("Mã nhà cung cấp đã tồn tại: " + supplierCode);
                }
            }

            // Tạo entity Supplier
            Supplier supplier = new Supplier();
            supplier.setCode(supplierCode);
            supplier.setName(request.getName());
            supplier.setAddress(request.getAddress());
            supplier.setEmail(request.getEmail());
            supplier.setPhone(request.getPhone());
            supplier.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
            supplier.setIsDeleted(false);

            // Lưu vào database
            Supplier savedSupplier = supplierRepository.save(supplier);

            log.info("Tạo nhà cung cấp thành công với ID: {}", savedSupplier.getSupplierId());
            return convertToResponse(savedSupplier);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi tạo nhà cung cấp: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi tạo nhà cung cấp: ", e);
            throw new RuntimeException("Không thể tạo nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin nhà cung cấp
     */
    @Override
    @Transactional
    public SupplierResponse updateSupplier(Integer supplierId, SupplierUpdateRequest request) {
        log.info("Cập nhật nhà cung cấp ID: {} với tên: {}", supplierId, request.getName());

        try {
            Supplier supplier = findSupplierEntityById(supplierId);

            // Cập nhật thông tin
            supplier.setName(request.getName());
            supplier.setAddress(request.getAddress());
            supplier.setEmail(request.getEmail());
            supplier.setPhone(request.getPhone());
            if (request.getIsActive() != null) {
                supplier.setIsActive(request.getIsActive());
            }

            // Lưu vào database
            Supplier updatedSupplier = supplierRepository.save(supplier);

            log.info("Cập nhật nhà cung cấp thành công với ID: {}", updatedSupplier.getSupplierId());
            return convertToResponse(updatedSupplier);

        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi cập nhật nhà cung cấp: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi cập nhật nhà cung cấp: ", e);
            throw new RuntimeException("Không thể cập nhật nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa nhà cung cấp (soft delete)
     */
    @Override
    @Transactional
    public void deleteSupplier(Integer supplierId) {
        log.info("Xóa nhà cung cấp ID: {}", supplierId);

        try {
            Supplier supplier = findSupplierEntityById(supplierId);

            // Kiểm tra có phiếu nhập nào không
            Integer importCount = supplierRepository.countImportsBySupplier(supplierId);
            if (importCount > 0) {
                throw new IllegalStateException("Không thể xóa nhà cung cấp vì đã có " + importCount + " phiếu nhập");
            }

            // Soft delete
            supplier.setIsDeleted(true);
            supplierRepository.save(supplier);

            log.info("Xóa nhà cung cấp thành công với ID: {}", supplierId);

        } catch (IllegalStateException e) {
            log.error("Lỗi business logic khi xóa nhà cung cấp: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi xóa nhà cung cấp: ", e);
            throw new RuntimeException("Không thể xóa nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin nhà cung cấp theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Integer supplierId) {
        log.debug("Lấy thông tin nhà cung cấp ID: {}", supplierId);

        try {
            Supplier supplier = findSupplierEntityById(supplierId);
            return convertToResponse(supplier);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin nhà cung cấp: ", e);
            throw new RuntimeException("Không thể lấy thông tin nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tất cả nhà cung cấp đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllActiveSuppliers() {
        log.debug("Lấy danh sách tất cả nhà cung cấp đang hoạt động");

        try {
            List<Supplier> suppliers = supplierRepository.findByIsActiveTrueAndIsDeletedFalse();
            return suppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhà cung cấp đang hoạt động: ", e);
            throw new RuntimeException("Không thể lấy danh sách nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách nhà cung cấp với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAllSuppliers(Pageable pageable) {
        log.debug("Lấy danh sách nhà cung cấp với phân trang: {}", pageable);

        try {
            Page<Supplier> supplierPage = supplierRepository.findByIsDeletedFalse(pageable);
            return supplierPage.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhà cung cấp có phân trang: ", e);
            throw new RuntimeException("Không thể lấy danh sách nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách nhà cung cấp với filtering, searching và sorting nâng cao
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getSuppliersAdvanced(SupplierPageableRequest request) {
        log.info("Lấy danh sách nhà cung cấp nâng cao: page={}, limit={}, search={}, isActive={}",
                request.getPage(), request.getLimit(), request.getSearchTerm(), request.getIsActive());

        try {
            // Tạo Pageable object từ request
            Pageable pageable = createPageableFromRequest(request);

            // Gọi repository để lấy dữ liệu
            Page<Supplier> suppliers = supplierRepository.findSuppliersAdvanced(
                    request.getSearchTerm(),
                    request.getActiveValue(),
                    pageable);

            // Map sang SupplierResponse
            return suppliers.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhà cung cấp nâng cao: ", e);
            throw new RuntimeException("Không thể lấy danh sách nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm nhà cung cấp theo tên
     */
    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String keyword) {
        log.debug("Tìm kiếm nhà cung cấp với từ khóa: {}", keyword);

        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllActiveSuppliers();
            }

            List<Supplier> suppliers = supplierRepository.findByKeyword(keyword.trim());
            return suppliers.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm nhà cung cấp: ", e);
            throw new RuntimeException("Không thể tìm kiếm nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm nhà cung cấp theo tên với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> searchSuppliers(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm nhà cung cấp với từ khóa: {} và phân trang: {}", keyword, pageable);

        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllSuppliers(pageable);
            }

            Page<Supplier> supplierPage = supplierRepository.findByKeywordWithPaging(keyword.trim(), pageable);
            return supplierPage.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm nhà cung cấp có phân trang: ", e);
            throw new RuntimeException("Không thể tìm kiếm nhà cung cấp: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra nhà cung cấp có tồn tại không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer supplierId) {
        return supplierRepository.existsById(supplierId);
    }

    /**
     * Lấy entity Supplier theo ID (dùng nội bộ)
     */
    @Override
    @Transactional(readOnly = true)
    public Supplier findSupplierEntityById(Integer supplierId) {
        return supplierRepository.findBySupplierIdAndIsDeletedFalse(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp với ID: " + supplierId));
    }

    /**
     * Chuyển đổi Entity sang Response DTO
     */
    private SupplierResponse convertToResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setSupplierId(supplier.getSupplierId());
        response.setCode(supplier.getCode());
        response.setName(supplier.getName());
        response.setAddress(supplier.getAddress());
        response.setEmail(supplier.getEmail());
        response.setPhone(supplier.getPhone());
        response.setIsActive(supplier.getIsActive());
        response.setIsDeleted(supplier.getIsDeleted());
        response.setCreatedAt(supplier.getCreatedAt());
        response.setUpdatedAt(supplier.getUpdatedAt());

        // Đếm số lượng phiếu nhập
        Integer importCount = supplierRepository.countImportsBySupplier(supplier.getSupplierId());
        response.setImportCount(importCount != null ? importCount : 0);

        return response;
    }

    /**
     * Tạo mã nhà cung cấp tự động
     */
    private String generateSupplierCode() {
        String prefix = "SUP";
        int maxAttempts = 1000;

        for (int i = 1; i <= maxAttempts; i++) {
            String code = String.format("%s%04d", prefix, i);
            if (!supplierRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new RuntimeException("Không thể tạo mã nhà cung cấp duy nhất");
    }

    /**
     * Tạo Pageable object từ SupplierPageableRequest
     */
    private Pageable createPageableFromRequest(SupplierPageableRequest request) {
        // Tạo Sort object từ sorts
        Sort sort = Sort.unsorted();
        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            Sort.Order[] orders = request.getSorts().stream()
                    .map(sortRequest -> {
                        Sort.Direction direction = "DESC".equalsIgnoreCase(sortRequest.getOrder())
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, sortRequest.getField());
                    })
                    .toArray(Sort.Order[]::new);
            sort = Sort.by(orders);
        } else {
            // Default sort by name ASC
            sort = Sort.by(Sort.Direction.ASC, "name");
        }

        // Tạo PageRequest
        return PageRequest.of(
                request.getPageForSpringData(),
                request.getLimit() != null ? request.getLimit() : 10,
                sort);
    }

    /**
     * Xóa nhiều nhà cung cấp cùng lúc (soft delete)
     */
    @Override
    @Transactional
    public SupplierBatchDeleteResponse batchDeleteSuppliers(SupplierBatchDeleteRequest request) {
        log.info("Bắt đầu xóa batch {} nhà cung cấp: {}", request.getCount(), request.getSupplierIds());

        try {
            // Validate request
            if (request == null || request.getSupplierIds() == null || request.getSupplierIds().isEmpty()) {
                throw new IllegalArgumentException("Danh sách ID nhà cung cấp không được rỗng");
            }

            List<Integer> validIds = request.getValidIds();
            List<Integer> failedIds = new ArrayList<>();
            List<String> failedReasons = new ArrayList<>();
            int deletedCount = 0;

            log.debug("Xử lý {} ID hợp lệ", validIds.size());

            // Xử lý từng ID
            for (Integer supplierId : validIds) {
                try {
                    // Kiểm tra supplier có tồn tại và chưa bị xóa
                    Supplier supplier = supplierRepository.findBySupplierIdAndIsDeletedFalse(supplierId)
                            .orElse(null);
                    if (supplier == null) {
                        failedIds.add(supplierId);
                        failedReasons.add("Nhà cung cấp không tồn tại hoặc đã bị xóa");
                        log.warn("Supplier ID {} không tồn tại hoặc đã bị xóa", supplierId);
                        continue;
                    }

                    // Kiểm tra ràng buộc dữ liệu (có phiếu nhập không)
                    Integer importCount = supplierRepository.countImportsBySupplier(supplierId);
                    if (importCount != null && importCount > 0) {
                        failedIds.add(supplierId);
                        failedReasons.add(String.format("Nhà cung cấp có %d phiếu nhập, không thể xóa", importCount));
                        log.warn("Supplier ID {} có {} phiếu nhập, không thể xóa", supplierId, importCount);
                        continue;
                    }

                    // Thực hiện soft delete
                    supplier.setIsDeleted(true);
                    supplier.setUpdatedAt(java.time.LocalDateTime.now());
                    supplierRepository.save(supplier);

                    deletedCount++;
                    log.debug("Đã xóa thành công supplier ID {}: {}", supplierId, supplier.getName());

                } catch (Exception e) {
                    failedIds.add(supplierId);
                    failedReasons.add("Lỗi hệ thống: " + e.getMessage());
                    log.error("Lỗi khi xóa supplier ID {}: ", supplierId, e);
                }
            }

            // Tạo response
            SupplierBatchDeleteResponse response = new SupplierBatchDeleteResponse(
                    deletedCount, failedIds, failedReasons, validIds.size());

            log.info("Hoàn thành batch delete: {}/{} thành công, {} thất bại",
                    deletedCount, validIds.size(), failedIds.size());

            return response;

        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi batch delete suppliers: ", e);
            throw new RuntimeException("Có lỗi xảy ra khi xóa nhà cung cấp: " + e.getMessage(), e);
        }
    }
}
