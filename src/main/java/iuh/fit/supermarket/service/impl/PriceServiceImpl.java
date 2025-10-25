package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.price.*;
import iuh.fit.supermarket.entity.Employee;
import iuh.fit.supermarket.entity.Price;
import iuh.fit.supermarket.entity.PriceDetail;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.enums.PriceType;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.repository.EmployeeRepository;
import iuh.fit.supermarket.repository.PriceDetailRepository;
import iuh.fit.supermarket.repository.PriceRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.service.PriceService;
import iuh.fit.supermarket.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của PriceService
 * Quản lý tất cả business logic liên quan đến bảng giá
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final PriceDetailRepository priceDetailRepository;
    private final ProductUnitRepository productUnitRepository;
    private final EmployeeRepository employeeRepository;
    private final SecurityUtil securityUtil;

    /**
     * Tạo bảng giá mới
     */
    @Override
    public PriceResponse createPrice(PriceCreateRequest request) {
        // Tự động tạo mã bảng giá nếu không được cung cấp
        String priceCode = request.getPriceCode();
        if (priceCode == null || priceCode.trim().isEmpty()) {
            priceCode = generatePriceCode();
            log.info("Tạo bảng giá mới với mã tự động: {}", priceCode);
        } else {
            log.info("Tạo bảng giá mới với mã được cung cấp: {}", priceCode);
            // Kiểm tra mã bảng giá đã tồn tại chưa
            if (priceRepository.existsByPriceCode(priceCode)) {
                throw new DuplicatePriceCodeException(priceCode, false);
            }
        }

        // Validate business rules
        validatePriceBusinessRules(request, false, null);

        // Lấy thông tin nhân viên hiện tại
        Employee currentEmployee = getCurrentEmployee();

        // Tạo entity Price
        Price price = new Price();
        price.setPriceName(request.getPriceName());
        price.setPriceCode(priceCode);
        price.setStartDate(request.getStartDate());
        price.setEndDate(request.getEndDate());
        price.setDescription(request.getDescription());
        price.setStatus(PriceType.UPCOMING);
        price.setCreatedBy(currentEmployee);
        price.setUpdatedBy(currentEmployee);

        // Lưu bảng giá
        price = priceRepository.save(price);

        // Tạo chi tiết giá nếu có
        List<PriceDetail> priceDetails = new ArrayList<>();
        if (request.getPriceDetails() != null &&
                !request.getPriceDetails().isEmpty()) {
            priceDetails = createPriceDetails(price, request.getPriceDetails());
            price.setPriceDetails(priceDetails);
        }

        log.info("Đã tạo bảng giá thành công: {} với {} chi tiết giá",
                price.getPriceCode(), priceDetails.size());

        return mapToPriceResponse(price, true);
    }

    /**
     * Cập nhật bảng giá
     */
    @Override
    public PriceResponse updatePrice(Long priceId, PriceUpdateRequest request) {
        log.info("Cập nhật bảng giá ID: {}", priceId);

        // Tìm bảng giá
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Kiểm tra quyền chỉnh sửa
        validatePriceEditable(price);

        // Lấy thông tin nhân viên hiện tại
        Employee currentEmployee = getCurrentEmployee();

        // Cập nhật thông tin cơ bản
        if (request.getPriceName() != null) {
            price.setPriceName(request.getPriceName());
        }
        if (request.getDescription() != null) {
            price.setDescription(request.getDescription());
        }
        if (request.getEndDate() != null) {
            price.setEndDate(request.getEndDate());
        }

        // Cập nhật ngày bắt đầu (chỉ cho phép nếu chưa active)
        if (request.getStartDate() != null && price.getStatus() == PriceType.UPCOMING) {
            validateStartDate(request.getStartDate());
            price.setStartDate(request.getStartDate());
        }

        // Cập nhật trạng thái (chỉ cho phép một số trạng thái)
        if (request.getStatus() != null) {
            validateStatusTransition(price.getStatus(), request.getStatus());
            price.setStatus(request.getStatus());
        }

        price.setUpdatedBy(currentEmployee);

        // Cập nhật chi tiết giá nếu có
        if (request.getPriceDetails() != null) {
            updatePriceDetails(price, request.getPriceDetails());
        }

        // Lưu bảng giá
        price = priceRepository.save(price);

        log.info("Đã cập nhật bảng giá thành công: {}", price.getPriceCode());

        return mapToPriceResponse(price, true);
    }

    /**
     * Lấy thông tin bảng giá theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public PriceResponse getPriceById(Long priceId, boolean includeDetails) {
        log.debug("Lấy thông tin bảng giá ID: {}, includeDetails: {}", priceId,
                includeDetails);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        return mapToPriceResponse(price, includeDetails);
    }

    /**
     * Lấy thông tin bảng giá theo mã
     */
    @Override
    @Transactional(readOnly = true)
    public PriceResponse getPriceByCode(String priceCode, boolean includeDetails) {
        log.debug("Lấy thông tin bảng giá mã: {}, includeDetails: {}", priceCode,
                includeDetails);

        Price price = priceRepository.findByPriceCode(priceCode)
                .orElseThrow(() -> PriceNotFoundException.byPriceCode(priceCode));

        return mapToPriceResponse(price, includeDetails);
    }

    /**
     * Lấy danh sách bảng giá với phân trang và lọc
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PriceResponse> getPricesAdvanced(PricePageableRequest request) {
        log.debug("Lấy danh sách bảng giá nâng cao: page={}, limit={}, search={}",
                request.getPage(), request.getLimit(), request.getSearchTerm());

        // Validate sort parameters
        if (!request.isValidSortBy()) {
            throw new PriceValidationException("Trường sắp xếp không hợp lệ: " +
                    request.getSortBy());
        }
        if (!request.isValidSortDirection()) {
            throw new PriceValidationException("Hướng sắp xếp không hợp lệ: " +
                    request.getSortDirection());
        }

        // Tạo Pageable
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(request.getSortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getLimit(),
                sort);

        // Gọi repository
        Page<Price> pricePage = priceRepository.findPricesAdvanced(
                request.getSearchTerm(),
                request.getStatus(),
                request.getStartDateFrom(),
                request.getStartDateTo(),
                request.getEndDateFrom(),
                request.getEndDateTo(),
                request.getCreatedBy(),
                request.getCreatedFrom(),
                request.getCreatedTo(),
                pageable);

        // Map sang PriceResponse
        return pricePage.map(price -> mapToPriceResponse(price,
                request.getIncludeDetails()));
    }

    /**
     * Xóa bảng giá
     */
    @Override
    public void deletePrice(Long priceId) {
        log.info("Xóa bảng giá ID: {}", priceId);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Chỉ cho phép xóa bảng giá UPCOMING hoặc PAUSED
        if (price.getStatus() != PriceType.UPCOMING && price.getStatus() != PriceType.PAUSED) {
            throw PriceConflictException.cannotEditPrice(price.getStatus().name());
        }

        priceRepository.delete(price);

        log.info("Đã xóa bảng giá thành công: {}", price.getPriceCode());
    }

    /**
     * Cập nhật trạng thái bảng giá
     */
    @Override
    public PriceResponse updatePriceStatus(Long priceId, PriceStatusUpdateRequest request) {
        log.info("Cập nhật trạng thái bảng giá ID: {} sang {}", priceId,
                request.getStatus());

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Validate chuyển đổi trạng thái
        validateStatusTransition(price.getStatus(), request.getStatus());

        // Xử lý logic đặc biệt cho từng trạng thái
        handleStatusTransition(price, request.getStatus());

        price.setStatus(request.getStatus());
        price.setUpdatedBy(getCurrentEmployee());

        price = priceRepository.save(price);

        log.info("Đã cập nhật trạng thái bảng giá thành công: {} -> {}",
                price.getPriceCode(), request.getStatus());

        return mapToPriceResponse(price, false);
    }

    /**
     * Kích hoạt bảng giá
     */
    @Override
    public PriceResponse activatePrice(Long priceId) {
        log.info("Kích hoạt bảng giá ID: {}", priceId);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Chỉ cho phép kích hoạt từ UPCOMING hoặc PAUSED
        if (price.getStatus() != PriceType.UPCOMING && price.getStatus() != PriceType.PAUSED) {
            throw PriceConflictException.invalidStatusTransition(
                    price.getStatus().name(), PriceType.CURRENT.name());
        }

        // Kiểm tra thời gian bắt đầu
        if (price.getStartDate().isAfter(LocalDateTime.now())) {
            throw new PriceValidationException("Chưa đến thời gian bắt đầu của bảng giá");
        }

        // Kiểm tra bảng giá phải có ít nhất một chi tiết giá
        long priceDetailCount = priceDetailRepository.countByPricePriceId(price.getPriceId());
        if (priceDetailCount == 0) {
            throw new PriceValidationException(
                    "Không thể kích hoạt bảng giá trống. Vui lòng thêm ít nhất một chi tiết giá trước khi kích hoạt");
        }

        // Xử lý logic kích hoạt
        handleStatusTransition(price, PriceType.CURRENT);

        price.setStatus(PriceType.CURRENT);
        price.setUpdatedBy(getCurrentEmployee());

        price = priceRepository.save(price);

        log.info("Đã kích hoạt bảng giá thành công: {}", price.getPriceCode());

        return mapToPriceResponse(price, false);
    }

    /**
     * Tạm dừng bảng giá
     */
    @Override
    public PriceResponse pausePrice(Long priceId) {
        log.info("Tạm dừng bảng giá ID: {}", priceId);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Chỉ cho phép tạm dừng từ CURRENT
        if (price.getStatus() != PriceType.CURRENT) {
            throw PriceConflictException.invalidStatusTransition(
                    price.getStatus().name(), PriceType.PAUSED.name());
        }

        price.setStatus(PriceType.PAUSED);
        price.setUpdatedBy(getCurrentEmployee());

        price = priceRepository.save(price);

        log.info("Đã tạm dừng bảng giá thành công: {}", price.getPriceCode());

        return mapToPriceResponse(price, false);
    }

    /**
     * Lấy danh sách bảng giá theo trạng thái
     */
    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> getPricesByStatus(PriceType status) {
        log.debug("Lấy danh sách bảng giá theo trạng thái: {}", status);

        List<Price> prices = priceRepository.findByStatus(status);
        return prices.stream()
                .map(price -> mapToPriceResponse(price, false))
                .collect(Collectors.toList());
    }

    /**
     * Lấy bảng giá hiện tại đang áp dụng
     */
    @Override
    @Transactional(readOnly = true)
    public List<PriceResponse> getCurrentPrices() {
        return getPricesByStatus(PriceType.CURRENT);
    }

    /**
     * Tự động cập nhật trạng thái bảng giá
     */
    @Override
    public void autoUpdatePriceStatus() {
        log.info("Bắt đầu tự động cập nhật trạng thái bảng giá");

        LocalDateTime now = LocalDateTime.now();

        // Chuyển UPCOMING sang CURRENT
        List<Price> pricesToActivate = priceRepository.findPricesToActivate(PriceType.UPCOMING, now);
        for (Price price : pricesToActivate) {
            try {
                log.info("Tự động kích hoạt bảng giá: {}", price.getPriceCode());

                // Kiểm tra bảng giá phải có ít nhất một chi tiết giá
                long priceDetailCount = priceDetailRepository.countByPricePriceId(price.getPriceId());
                if (priceDetailCount == 0) {
                    log.warn("Bỏ qua tự động kích hoạt bảng giá trống: {}", price.getPriceCode());
                    continue;
                }

                handleStatusTransition(price, PriceType.CURRENT);
                price.setStatus(PriceType.CURRENT);
                priceRepository.save(price);
            } catch (Exception e) {
                log.error("Lỗi khi tự động kích hoạt bảng giá {}: {}", price.getPriceCode(), e.getMessage());
            }
        }

        // Chuyển CURRENT sang EXPIRED
        List<Price> pricesToExpire = priceRepository.findPricesToExpire(PriceType.CURRENT, now);
        for (Price price : pricesToExpire) {
            try {
                log.info("Tự động hết hạn bảng giá: {}", price.getPriceCode());
                price.setStatus(PriceType.EXPIRED);
                priceRepository.save(price);
            } catch (Exception e) {
                log.error("Lỗi khi tự động hết hạn bảng giá {}: {}", price.getPriceCode(), e.getMessage());
            }
        }

        log.info("Hoàn thành tự động cập nhật trạng thái bảng giá: {} kích hoạt, {} hết hạn",
                pricesToActivate.size(), pricesToExpire.size());
    }

    /**
     * Kiểm tra và validate business rules cho bảng giá
     */
    @Override
    public void validatePriceBusinessRules(PriceCreateRequest request, boolean isUpdate, Long currentPriceId) {
        // Validate ngày bắt đầu
        validateStartDate(request.getStartDate());

        // Validate ngày kết thúc
        if (request.getEndDate() != null) {
            validateEndDate(request.getStartDate(), request.getEndDate());
        }

        // Validate chi tiết giá
        validatePriceDetails(request.getPriceDetails(), isUpdate, currentPriceId);
    }

    /**
     * Lấy danh sách chi tiết giá theo ID bảng giá
     */
    @Override
    @Transactional(readOnly = true)
    public List<PriceDetailDto> getPriceDetailsByPriceId(Long priceId) {
        log.debug("Lấy danh sách chi tiết giá của bảng giá ID: {}", priceId);

        List<PriceDetail> priceDetails = priceDetailRepository.findByPriceIdWithProductUnit(priceId);
        return priceDetails.stream()
                .map(this::mapToPriceDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Thêm chi tiết giá vào bảng giá
     */
    @Override
    public PriceResponse addPriceDetails(Long priceId,
            List<PriceCreateRequest.PriceDetailCreateRequest> priceDetails) {
        log.info("Thêm {} chi tiết giá vào bảng giá ID: {}", priceDetails.size(),
                priceId);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Kiểm tra quyền chỉnh sửa
        validatePriceEditable(price);

        // Tạo chi tiết giá mới
        List<PriceDetail> newPriceDetails = createPriceDetails(price, priceDetails);

        // Cập nhật thông tin
        price.setUpdatedBy(getCurrentEmployee());
        priceRepository.save(price);

        log.info("Đã thêm {} chi tiết giá vào bảng giá: {}", newPriceDetails.size(),
                price.getPriceCode());

        return mapToPriceResponse(price, true);
    }

    /**
     * Xóa chi tiết giá khỏi bảng giá
     */
    @Override
    public PriceResponse removePriceDetails(Long priceId, List<Long> priceDetailIds) {
        log.info("Xóa {} chi tiết giá khỏi bảng giá ID: {}", priceDetailIds.size(),
                priceId);

        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new PriceNotFoundException(priceId));

        // Kiểm tra quyền chỉnh sửa
        validatePriceEditable(price);

        // Xóa chi tiết giá
        priceDetailRepository.deleteByIds(priceDetailIds);

        // Cập nhật thông tin
        price.setUpdatedBy(getCurrentEmployee());
        priceRepository.save(price);

        log.info("Đã xóa {} chi tiết giá khỏi bảng giá: {}", priceDetailIds.size(),
                price.getPriceCode());

        return mapToPriceResponse(price, true);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Lấy thông tin nhân viên hiện tại
     */
    private Employee getCurrentEmployee() {
        Integer employeeId = securityUtil.getCurrentEmployeeId();
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên hiện tại"));
    }

    /**
     * Tự động tạo mã bảng giá theo format BG + 6 chữ số
     *
     * @return mã bảng giá duy nhất
     */
    private String generatePriceCode() {
        String prefix = "BG";
        int maxAttempts = 1000; // Giới hạn số lần thử để tránh vòng lặp vô hạn

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Tạo số ngẫu nhiên từ 1 đến 999999
            int randomNumber = (int) (Math.random() * 999999) + 1;
            String priceCode = prefix + String.format("%06d", randomNumber);

            // Kiểm tra mã đã tồn tại chưa
            if (!priceRepository.existsByPriceCode(priceCode)) {
                log.debug("Đã tạo mã bảng giá tự động: {}", priceCode);
                return priceCode;
            }
        }

        // Nếu không tìm được mã khả dụng sau maxAttempts lần thử
        throw new RuntimeException("Không thể tạo mã bảng giá duy nhất sau " +
                maxAttempts + " lần thử");
    }

    /**
     * Kiểm tra bảng giá có thể chỉnh sửa không
     */
    private void validatePriceEditable(Price price) {
        if (price.getStatus() == PriceType.CURRENT || price.getStatus() == PriceType.EXPIRED) {
            throw PriceConflictException.cannotEditPrice(price.getStatus().name());
        }
    }

    /**
     * Validate ngày bắt đầu
     */
    private void validateStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            throw new PriceValidationException("Ngày bắt đầu không được để trống");
        }
    }

    /**
     * Validate ngày kết thúc
     */
    private void validateEndDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate != null && !endDate.isAfter(startDate.plusDays(1))) {
            throw new PriceValidationException("Ngày kết thúc phải lớn hơn ngày bắt đầu ít nhất 1 ngày");
        }
    }

    /**
     * Validate chi tiết giá
     */
    private void validatePriceDetails(List<PriceCreateRequest.PriceDetailCreateRequest> priceDetails,
            boolean isUpdate, Long currentPriceId) {
        // Cho phép priceDetails null hoặc empty khi tạo bảng giá mới
        if (priceDetails == null || priceDetails.isEmpty()) {
            return; // Không validate nếu không có chi tiết giá
        }

        // Kiểm tra trùng lặp đơn vị sản phẩm trong request (sử dụng productUnitId từ
        // DTO)
        List<Long> productUnitIds = priceDetails.stream()
                .map(PriceCreateRequest.PriceDetailCreateRequest::getProductUnitId)
                .collect(Collectors.toList());

        if (productUnitIds.size() != productUnitIds.stream().distinct().count()) {
            throw new PriceValidationException("Không được có đơn vị sản phẩm trùng lặp trong bảng giá");
        }

        // Kiểm tra đơn vị sản phẩm có tồn tại trong bảng giá CURRENT khác không
        for (PriceCreateRequest.PriceDetailCreateRequest detail : priceDetails) {
            if (isUpdate && currentPriceId != null) {
                // Tìm bảng giá CURRENT khác chứa đơn vị sản phẩm này
                List<Price> currentPricesWithProductUnit = priceRepository.findCurrentPricesByProductUnitId(
                        PriceType.CURRENT, detail.getProductUnitId());

                // Loại bỏ bảng giá hiện tại khỏi danh sách kiểm tra
                currentPricesWithProductUnit = currentPricesWithProductUnit.stream()
                        .filter(p -> !p.getPriceId().equals(currentPriceId))
                        .collect(Collectors.toList());

                if (!currentPricesWithProductUnit.isEmpty()) {
                    ProductUnit productUnit = productUnitRepository.findById(detail.getProductUnitId())
                            .orElse(null);
                    String productUnitName = detail.getProductUnitId().toString();
                    Price conflictPrice = currentPricesWithProductUnit.get(0);
                    throw PriceConflictException.variantAlreadyInCurrentPrice(
                            productUnitName, conflictPrice.getPriceCode());
                }
            } else {
                List<Price> currentPrices = priceRepository.findCurrentPricesByProductUnitId(
                        PriceType.CURRENT, detail.getProductUnitId());
                if (!currentPrices.isEmpty()) {
                    String productUnitName = detail.getProductUnitId().toString();
                    throw PriceConflictException.variantAlreadyInCurrentPrice(
                            productUnitName, currentPrices.get(0).getPriceCode());
                }
            }
        }
    }

    /**
     * Validate chuyển đổi trạng thái
     */
    private void validateStatusTransition(PriceType currentStatus, PriceType newStatus) {
        if (currentStatus == newStatus) {
            return; // Không thay đổi
        }

        boolean isValidTransition = false;

        switch (currentStatus) {
            case UPCOMING:
                isValidTransition = newStatus == PriceType.CURRENT || newStatus == PriceType.PAUSED;
                break;
            case CURRENT:
                isValidTransition = newStatus == PriceType.PAUSED || newStatus == PriceType.EXPIRED;
                break;
            case PAUSED:
                isValidTransition = newStatus == PriceType.CURRENT || newStatus == PriceType.EXPIRED;
                break;
            case EXPIRED:
                // Không cho phép chuyển từ EXPIRED sang trạng thái khác
                isValidTransition = false;
                break;
        }

        if (!isValidTransition) {
            throw PriceConflictException.invalidStatusTransition(currentStatus.name(),
                    newStatus.name());
        }
    }

    /**
     * Xử lý logic đặc biệt khi chuyển đổi trạng thái
     */
    private void handleStatusTransition(Price price, PriceType newStatus) {
        if (newStatus == PriceType.CURRENT) {
            // Khi kích hoạt bảng giá, kiểm tra xung đột với bảng giá CURRENT khác
            validateNoConflictWithCurrentPrices(price);
        }
    }

    /**
     * Kiểm tra không có xung đột với bảng giá CURRENT khác
     */
    private void validateNoConflictWithCurrentPrices(Price price) {
        List<PriceDetail> priceDetails = priceDetailRepository.findByPricePriceId(price.getPriceId());

        for (PriceDetail detail : priceDetails) {
            // Tìm bảng giá CURRENT khác chứa đơn vị sản phẩm này
            List<Price> currentPricesWithProductUnit = priceRepository.findCurrentPricesByProductUnitId(
                    PriceType.CURRENT, detail.getProductUnit().getId());

            // Loại bỏ bảng giá hiện tại khỏi danh sách kiểm tra
            currentPricesWithProductUnit = currentPricesWithProductUnit.stream()
                    .filter(p -> !p.getPriceId().equals(price.getPriceId()))
                    .collect(Collectors.toList());

            if (!currentPricesWithProductUnit.isEmpty()) {
                Price conflictPrice = currentPricesWithProductUnit.get(0);
                throw PriceConflictException.variantAlreadyInCurrentPrice(
                        detail.getProductUnit().getId().toString(), conflictPrice.getPriceCode());
            }
        }
    }

    /**
     * Tạo danh sách chi tiết giá
     */
    private List<PriceDetail> createPriceDetails(Price price,
            List<PriceCreateRequest.PriceDetailCreateRequest> requests) {
        List<PriceDetail> priceDetails = new ArrayList<>();

        // Kiểm tra danh sách requests không null và không rỗng
        if (requests == null || requests.isEmpty()) {
            return priceDetails;
        }

        for (PriceCreateRequest.PriceDetailCreateRequest request : requests) {
            // Kiểm tra đơn vị sản phẩm tồn tại
            ProductUnit productUnit = productUnitRepository.findById(request.getProductUnitId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy đơn vị sản phẩm ID: " + request.getProductUnitId()));

            // Kiểm tra đơn vị sản phẩm đã tồn tại trong bảng giá này chưa
            if (priceDetailRepository.existsByPricePriceIdAndProductUnitId(price.getPriceId(),
                    request.getProductUnitId())) {
                throw new PriceConflictException(
                        "Đơn vị sản phẩm " + productUnit.getId() + " đã tồn tại trong bảng giá");
            }

            // Kiểm tra đơn vị sản phẩm có tồn tại trong bảng giá CURRENT khác không
            List<Price> currentPricesWithProductUnit = priceRepository.findCurrentPricesByProductUnitId(
                    PriceType.CURRENT, request.getProductUnitId());

            // Loại bỏ bảng giá hiện tại khỏi danh sách kiểm tra
            currentPricesWithProductUnit = currentPricesWithProductUnit.stream()
                    .filter(p -> !p.getPriceId().equals(price.getPriceId()))
                    .collect(Collectors.toList());

            if (!currentPricesWithProductUnit.isEmpty()) {
                Price conflictPrice = currentPricesWithProductUnit.get(0);
                throw PriceConflictException.variantAlreadyInCurrentPrice(
                        productUnit.getId().toString(), conflictPrice.getPriceCode());
            }

            // Tạo chi tiết giá
            PriceDetail priceDetail = new PriceDetail();
            priceDetail.setPrice(price);
            priceDetail.setProductUnit(productUnit);
            priceDetail.setSalePrice(request.getSalePrice());

            priceDetails.add(priceDetail);
        }

        return priceDetailRepository.saveAll(priceDetails);
    }

    /**
     * Cập nhật chi tiết giá
     */
    private void updatePriceDetails(Price price,
            List<PriceUpdateRequest.PriceDetailUpdateRequest> requests) {
        for (PriceUpdateRequest.PriceDetailUpdateRequest request : requests) {
            if (request.getDeleted() != null && request.getDeleted()) {
                // Xóa chi tiết giá
                if (request.getPriceDetailId() != null) {
                    priceDetailRepository.deleteById(request.getPriceDetailId());
                }
            } else if (request.getPriceDetailId() == null) {
                // Tạo mới chi tiết giá
                ProductUnit productUnit = productUnitRepository.findById(request.getProductUnitId())
                        .orElseThrow(() -> new RuntimeException(
                                "Không tìm thấy đơn vị sản phẩm ID: " + request.getProductUnitId()));

                PriceDetail priceDetail = new PriceDetail();
                priceDetail.setPrice(price);
                priceDetail.setProductUnit(productUnit);
                priceDetail.setSalePrice(request.getSalePrice());

                priceDetailRepository.save(priceDetail);
            } else {
                // Cập nhật chi tiết giá
                PriceDetail priceDetail = priceDetailRepository.findById(request.getPriceDetailId())
                        .orElseThrow(() -> new RuntimeException(
                                "Không tìm thấy chi tiết giá ID: " + request.getPriceDetailId()));

                priceDetail.setSalePrice(request.getSalePrice());
                priceDetailRepository.save(priceDetail);
            }
        }
    }

    /**
     * Map Price entity sang PriceResponse DTO
     */
    private PriceResponse mapToPriceResponse(Price price, boolean includeDetails) {
        PriceResponse response = new PriceResponse();
        response.setPriceId(price.getPriceId());
        response.setPriceName(price.getPriceName());
        response.setPriceCode(price.getPriceCode());
        response.setStartDate(price.getStartDate());
        response.setEndDate(price.getEndDate());
        response.setDescription(price.getDescription());
        response.setStatus(price.getStatus());
        response.setCreatedAt(price.getCreatedAt());
        response.setUpdatedAt(price.getUpdatedAt());

        // Map thông tin người tạo
        if (price.getCreatedBy() != null) {
            PriceResponse.EmployeeInfo createdBy = new PriceResponse.EmployeeInfo();
            createdBy.setEmployeeId(price.getCreatedBy().getEmployeeId());
            createdBy.setName(price.getCreatedBy().getName());
            createdBy.setEmail(price.getCreatedBy().getEmail());
            response.setCreatedBy(createdBy);
        }

        // Map thông tin người cập nhật
        if (price.getUpdatedBy() != null) {
            PriceResponse.EmployeeInfo updatedBy = new PriceResponse.EmployeeInfo();
            updatedBy.setEmployeeId(price.getUpdatedBy().getEmployeeId());
            updatedBy.setName(price.getUpdatedBy().getName());
            updatedBy.setEmail(price.getUpdatedBy().getEmail());
            response.setUpdatedBy(updatedBy);
        }

        // Đếm số lượng chi tiết giá
        if (price.getPriceDetails() != null) {
            response.setPriceDetailCount(price.getPriceDetails().size());

            // Bao gồm chi tiết giá nếu được yêu cầu
            if (includeDetails) {
                List<PriceDetailDto> priceDetailDtos = price.getPriceDetails().stream()
                        .map(this::mapToPriceDetailDto)
                        .collect(Collectors.toList());
                response.setPriceDetails(priceDetailDtos);
            }
        } else {
            // Nếu không có lazy loading, đếm từ repository
            long count = priceDetailRepository.countByPricePriceId(price.getPriceId());
            response.setPriceDetailCount((int) count);

            if (includeDetails) {
                List<PriceDetailDto> priceDetailDtos = getPriceDetailsByPriceId(price.getPriceId());
                response.setPriceDetails(priceDetailDtos);
            }
        }

        return response;
    }

    /**
     * Map PriceDetail entity sang PriceDetailDto
     */
    private PriceDetailDto mapToPriceDetailDto(PriceDetail priceDetail) {
        PriceDetailDto dto = new PriceDetailDto();
        dto.setPriceDetailId(priceDetail.getPriceDetailId());
        dto.setSalePrice(priceDetail.getSalePrice());
        dto.setCreatedAt(priceDetail.getCreatedAt());
        dto.setUpdatedAt(priceDetail.getUpdatedAt());

        // Map thông tin đơn vị sản phẩm
        if (priceDetail.getProductUnit() != null) {
            dto.setProductUnitId(priceDetail.getProductUnit().getId());
            dto.setBarcode(priceDetail.getProductUnit().getBarcode());

            // Lấy tên đơn vị từ Unit entity
            if (priceDetail.getProductUnit().getUnit() != null) {
                dto.setProductUnitName(priceDetail.getProductUnit().getUnit().getName());
            }
        }

        return dto;
    }

    /**
     * Lấy giá hiện tại của đơn vị sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public PriceDetailDto getCurrentPriceByProductUnitId(Long productUnitId) {
        Optional<PriceDetail> priceDetailOpt = priceDetailRepository
                .findCurrentPriceByProductUnitId(productUnitId, PriceType.CURRENT);

        if (priceDetailOpt.isEmpty()) {
            return null;
        }

        PriceDetail priceDetail = priceDetailOpt.get();
        return mapToPriceDetailDto(priceDetail);
    }
}
