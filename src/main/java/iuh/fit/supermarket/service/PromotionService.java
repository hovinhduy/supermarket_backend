package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.*;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.factory.PromotionDetailFactory;
import iuh.fit.supermarket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class cho quản lý chương trình khuyến mãi
 * Xử lý tất cả business logic liên quan đến promotion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionService {

    private final PromotionHeaderRepository promotionHeaderRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final ProductUnitRepository productUnitRepository;
    private final PromotionDetailFactory promotionDetailFactory;

    /**
     * Tạo mới chỉ promotion header (không bao gồm lines)
     * 
     * @param requestDTO thông tin header cần tạo
     * @return PromotionHeaderResponseDTO chứa thông tin header đã tạo
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     * @throws DuplicatePromotionException  nếu tên chương trình đã tồn tại
     */
    @Transactional
    public PromotionHeaderResponseDTO createPromotionHeaderOnly(PromotionHeaderOnlyRequestDTO requestDTO) {
        log.info("Bắt đầu tạo promotion header: {}", requestDTO.getPromotionName());

        // Validation cơ bản
        validatePromotionHeaderOnlyRequest(requestDTO);

        // Kiểm tra trùng lặp tên chương trình
        if (promotionHeaderRepository.findByPromotionNameIgnoreCase(requestDTO.getPromotionName()).isPresent()) {
            throw new DuplicatePromotionException(
                    "Tên chương trình khuyến mãi đã tồn tại: " + requestDTO.getPromotionName());
        }

        // Tạo PromotionHeader
        PromotionHeader header = new PromotionHeader();
        header.setPromotionName(requestDTO.getPromotionName());
        header.setDescription(requestDTO.getDescription());
        header.setStartDate(requestDTO.getStartDate());
        header.setEndDate(requestDTO.getEndDate());
        header.setStatus(requestDTO.getStatus());

        header = promotionHeaderRepository.save(header);

        log.info("Đã tạo thành công promotion header ID: {}", header.getPromotionId());
        return convertToHeaderResponseDTO(header);
    }

    /**
     * Tạo mới promotion line cho một header đã tồn tại (bao gồm cả detail trong
     * request)
     * Note: Để sử dụng API này, cần thêm trường promotionHeaderId vào
     * PromotionLineRequestDTO
     * Hoặc tạo một DTO riêng. Hiện tại method này chưa được sử dụng.
     *
     * @param requestDTO    thông tin line cần tạo (bao gồm cả detail)
     * @param headerIdthông tin line cần tạo (bao gồm cả detail)
     * @return PromotionLineResponseDTO chứa thông tin line đã tạo
     * @throws PromotionNotFoundException   nếu không tìm thấy header
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public PromotionLineResponseDTO createPromotionLineWithDetail(Long headerId, PromotionLineRequestDTO requestDTO) {
        log.info("Bắt đầu tạo promotion line với tên: {}", requestDTO.getLineName());

        // Kiểm tra header tồn tại
        PromotionHeader header = promotionHeaderRepository.findById(headerId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion header với ID: " + headerId));

        // Validation
        validatePromotionLineRequest(requestDTO);

        // Validation ngày tháng của line phải nằm trong khoảng của header
        validatePromotionLineDateRangeWithinHeader(requestDTO.getStartDate(), requestDTO.getEndDate(), header);

        // Kiểm tra trùng mã khuyến mãi (giờ kiểm tra ở detail level)
        if (requestDTO.getDetail() != null && requestDTO.getDetail().getPromotionCode() != null) {
            if (promotionDetailRepository.existsByPromotionCodeIgnoreCase(requestDTO.getDetail().getPromotionCode())) {
                throw new DuplicatePromotionException(
                        "Mã khuyến mãi đã tồn tại: " + requestDTO.getDetail().getPromotionCode());
            }
        }

        // Tạo line
        PromotionLine line = new PromotionLine();
        line.setLineName(requestDTO.getLineName());
        line.setPromotionType(requestDTO.getPromotionType());
        line.setDescription(requestDTO.getDescription());
        line.setStartDate(requestDTO.getStartDate());
        line.setEndDate(requestDTO.getEndDate());
        line.setStatus(requestDTO.getStatus());
        line.setHeader(header);

        line = promotionLineRepository.save(line);

        // Tạo detail nếu có
        if (requestDTO.getDetail() != null) {
            PromotionDetail detail = createPromotionDetailFromDTO(requestDTO.getDetail(), line);
            detail = promotionDetailRepository.save(detail);

            // Khởi tạo list nếu chưa có
            if (line.getDetails() == null) {
                line.setDetails(new ArrayList<>());
            }
            line.getDetails().add(detail);
        }

        log.info("Đã tạo thành công promotion line ID: {}", line.getPromotionLineId());
        return convertToLineResponseDTO(line);
    }

    /**
     * Tạo mới promotion line (không bao gồm detail)
     * Detail phải được tạo riêng thông qua endpoint POST /lines/{lineId}/details
     *
     * @param headerId   ID của header (từ URL path)
     * @param requestDTO thông tin line cần tạo (không bao gồm detail)
     * @return PromotionLineResponseDTO chứa thông tin line đã tạo
     * @throws PromotionNotFoundException   nếu không tìm thấy header
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public PromotionLineResponseDTO createPromotionLineOnly(Long headerId, PromotionLineOnlyRequestDTO requestDTO) {
        log.info("Bắt đầu tạo promotion line với tên: {}", requestDTO.getLineName());

        // Kiểm tra header tồn tại
        PromotionHeader header = promotionHeaderRepository.findById(headerId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion header với ID: " + headerId));

        // Validation cơ bản
        validatePromotionLineOnlyRequest(requestDTO);

        // Validation ngày tháng của line phải nằm trong khoảng của header
        validatePromotionLineDateRangeWithinHeader(requestDTO.getStartDate(), requestDTO.getEndDate(), header);

        // Tạo line
        PromotionLine line = new PromotionLine();
        line.setLineName(requestDTO.getLineName());
        line.setPromotionType(requestDTO.getPromotionType());
        line.setDescription(requestDTO.getDescription());
        line.setStartDate(requestDTO.getStartDate());
        line.setEndDate(requestDTO.getEndDate());
        line.setStatus(requestDTO.getStatus());
        line.setHeader(header);

        line = promotionLineRepository.save(line);

        log.info("Đã tạo thành công promotion line ID: {} (không có detail - detail phải được tạo riêng)",
                line.getPromotionLineId());
        return convertToLineResponseDTO(line);
    }

    /**
     * Tạo mới promotion detail cho một line đã tồn tại
     * Một line có thể có nhiều details
     * 
     * @param lineId     ID của line (từ URL path)
     * @param requestDTO thông tin detail cần tạo
     * @return PromotionDetailResponseDTO chứa thông tin detail đã tạo
     * @throws PromotionNotFoundException   nếu không tìm thấy line
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public PromotionDetailResponseDTO createPromotionDetail(Long lineId, PromotionDetailWithLineRequestDTO requestDTO) {
        log.info("Bắt đầu tạo promotion detail cho line ID: {}", lineId);

        // Kiểm tra line tồn tại
        PromotionLine line = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion line với ID: " + lineId));

        // Validation theo loại khuyến mãi
        validatePromotionDetailRequest(requestDTO, line.getPromotionType());

        // Tạo detail
        PromotionDetail detail = createPromotionDetailFromDTO(requestDTO, line);
        detail = promotionDetailRepository.save(detail);

        // Khởi tạo list nếu chưa có và thêm detail mới
        if (line.getDetails() == null) {
            line.setDetails(new ArrayList<>());
        }
        line.getDetails().add(detail);
        promotionLineRepository.save(line);

        log.info("Đã tạo thành công promotion detail ID: {}", detail.getDetailId());
        return convertToDetailResponseDTO(detail);
    }

    /**
     * Xóa chương trình khuyến mãi
     * 
     * @param promotionId ID của chương trình cần xóa
     * @throws PromotionNotFoundException   nếu không tìm thấy chương trình
     * @throws PromotionValidationException nếu chương trình không thể xóa
     */
    @Transactional
    public void deletePromotion(Long promotionId) {
        log.info("Bắt đầu xóa chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader header = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        // Xóa tất cả details và lines trước
        deleteExistingLinesAndDetails(promotionId);

        // Xóa header
        promotionHeaderRepository.delete(header);

        log.info("Đã xóa thành công chương trình khuyến mãi ID: {}", promotionId);
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi
     * 
     * @param promotionId ID của chương trình
     * @return PromotionHeaderResponseDTO chứa thông tin chi tiết
     * @throws PromotionNotFoundException nếu không tìm thấy chương trình
     */
    @Transactional(readOnly = true)
    public PromotionHeaderResponseDTO getPromotionById(Long promotionId) {
        log.debug("Lấy thông tin chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader header = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        return convertToHeaderResponseDTO(header);
    }

    /**
     * Lấy thông tin chi tiết promotion line và detail của nó
     * 
     * @param lineId ID của promotion line
     * @return PromotionLineResponseDTO chứa thông tin chi tiết line và detail
     * @throws PromotionNotFoundException nếu không tìm thấy promotion line
     */
    @Transactional(readOnly = true)
    public PromotionLineResponseDTO getPromotionLineById(Long lineId) {
        log.debug("Lấy thông tin promotion line ID: {}", lineId);

        PromotionLine line = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion line với ID: " + lineId));

        return convertToLineResponseDTO(line);
    }

    /**
     * Lấy thông tin chi tiết promotion detail theo ID
     * 
     * @param detailId ID của promotion detail
     * @return PromotionDetailResponseDTO chứa thông tin chi tiết
     * @throws PromotionNotFoundException nếu không tìm thấy promotion detail
     */
    @Transactional(readOnly = true)
    public PromotionDetailResponseDTO getPromotionDetailById(Long detailId) {
        log.debug("Lấy thông tin promotion detail ID: {}", detailId);

        PromotionDetail detail = promotionDetailRepository.findById(detailId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion detail với ID: " + detailId));

        return convertToDetailResponseDTO(detail);
    }

    /**
     * Xóa promotion line và tất cả details của nó
     * 
     * @param lineId ID của promotion line cần xóa
     * @throws PromotionNotFoundException   nếu không tìm thấy promotion line
     * @throws PromotionValidationException nếu promotion line không thể xóa
     */
    @Transactional
    public void deletePromotionLine(Long lineId) {
        log.info("Bắt đầu xóa promotion line ID: {}", lineId);

        // Tìm promotion line
        PromotionLine line = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion line với ID: " + lineId));

        // Kiểm tra xem có thể xóa không (dựa vào header và trạng thái line)
        validatePromotionLineCanBeDeleted(line);

        // Xóa tất cả details trước nếu có
        if (line.getDetails() != null && !line.getDetails().isEmpty()) {
            log.info("Xóa {} promotion details của line ID: {}", line.getDetails().size(), lineId);
            promotionDetailRepository.deleteAll(line.getDetails());
        }

        // Xóa line
        promotionLineRepository.delete(line);

        log.info("Đã xóa thành công promotion line ID: {}", lineId);
    }

    /**
     * Xóa một promotion detail cụ thể
     * 
     * @param detailId ID của promotion detail cần xóa
     * @throws PromotionNotFoundException   nếu không tìm thấy promotion detail
     * @throws PromotionValidationException nếu promotion detail không thể xóa
     */
    @Transactional
    public void deletePromotionDetail(Long detailId) {
        log.info("Bắt đầu xóa promotion detail ID: {}", detailId);

        // Tìm promotion detail
        PromotionDetail detail = promotionDetailRepository.findById(detailId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion detail với ID: " + detailId));

        // Xóa detail
        promotionDetailRepository.delete(detail);

        log.info("Đã xóa thành công promotion detail ID: {}", detailId);
    }

    /**
     * Kiểm tra xem promotion line có thể xóa không
     *
     * @param line promotion line cần kiểm tra
     * @throws PromotionValidationException nếu không thể xóa
     */
    private void validatePromotionLineCanBeDeleted(PromotionLine line) {
        LocalDate now = LocalDate.now();

        // Không cho phép xóa nếu line đang hoạt động
        if (line.getStatus() == PromotionStatus.ACTIVE &&
                now.isAfter(line.getStartDate()) &&
                now.isBefore(line.getEndDate())) {
            throw new PromotionValidationException("Không thể xóa promotion line đang hoạt động");
        }
    }

    /**
     * Lấy danh sách promotion lines theo header ID với lọc
     *
     * @param promotionId   ID của promotion header
     * @param promotionType Loại khuyến mãi để lọc (optional)
     * @param startDateFrom Ngày bắt đầu từ (optional)
     * @param startDateTo   Ngày bắt đầu đến (optional)
     * @param endDateFrom   Ngày kết thúc từ (optional)
     * @param endDateTo     Ngày kết thúc đến (optional)
     * @return List của PromotionLineResponseDTO
     * @throws PromotionNotFoundException nếu không tìm thấy promotion header
     */
    @Transactional(readOnly = true)
    public List<PromotionLineResponseDTO> getPromotionLinesByHeaderId(
            Long promotionId,
            PromotionType promotionType,
            LocalDate startDateFrom,
            LocalDate startDateTo,
            LocalDate endDateFrom,
            LocalDate endDateTo) {

        log.debug("Lấy danh sách promotion lines cho header ID: {} với bộ lọc", promotionId);

        // Kiểm tra promotion header có tồn tại không
        if (!promotionHeaderRepository.existsById(promotionId)) {
            throw new PromotionNotFoundException(
                    "Không tìm thấy promotion header với ID: " + promotionId);
        }

        // Lấy tất cả lines của header
        List<PromotionLine> lines = promotionLineRepository.findByPromotionHeaderId(promotionId);

        // Áp dụng bộ lọc
        List<PromotionLine> filteredLines = lines.stream()
                .filter(line -> {
                    // Lọc theo loại khuyến mãi
                    if (promotionType != null && !line.getPromotionType().equals(promotionType)) {
                        return false;
                    }

                    // Lọc theo ngày bắt đầu
                    if (startDateFrom != null && line.getStartDate().isBefore(startDateFrom)) {
                        return false;
                    }
                    if (startDateTo != null && line.getStartDate().isAfter(startDateTo)) {
                        return false;
                    }

                    // Lọc theo ngày kết thúc
                    if (endDateFrom != null && line.getEndDate().isBefore(endDateFrom)) {
                        return false;
                    }
                    if (endDateTo != null && line.getEndDate().isAfter(endDateTo)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        log.debug("Tìm thấy {} promotion lines sau khi lọc", filteredLines.size());

        // Chuyển đổi sang DTO
        return filteredLines.stream()
                .map(this::convertToLineResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và lọc chương trình khuyến mãi
     * 
     * @param searchDTO điều kiện tìm kiếm
     * @return Page chứa danh sách chương trình khuyến mãi
     */
    @Transactional(readOnly = true)
    public Page<PromotionHeaderResponseDTO> searchPromotions(PromotionSearchDTO searchDTO) {
        log.debug("Tìm kiếm chương trình khuyến mãi với điều kiện: {}", searchDTO);

        // Validate và chuẩn hóa tham số tìm kiếm
        searchDTO.validate();

        // Tạo Pageable
        Sort sort = Sort.by(
                searchDTO.getSortDirection().equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                searchDTO.getSortBy());
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);

        Page<PromotionHeader> headerPage;

        // Xử lý các trường hợp tìm kiếm đặc biệt
        LocalDate now = LocalDate.now();

        if (Boolean.TRUE.equals(searchDTO.getActiveOnly())) {
            headerPage = promotionHeaderRepository.findActivePromotions(now, pageable);
        } else if (Boolean.TRUE.equals(searchDTO.getUpcomingOnly())) {
            headerPage = promotionHeaderRepository.findUpcomingPromotions(now, pageable);
        } else if (Boolean.TRUE.equals(searchDTO.getExpiredOnly())) {
            headerPage = promotionHeaderRepository.findExpiredPromotions(now, pageable);
        } else if (searchDTO.hasSearchCriteria()) {
            // Tìm kiếm với nhiều điều kiện
            headerPage = promotionHeaderRepository.findWithCriteria(
                    searchDTO.getKeyword(),
                    searchDTO.getStatus(),
                    searchDTO.getStartDateFrom(),
                    searchDTO.getStartDateTo(),
                    searchDTO.getEndDateFrom(),
                    searchDTO.getEndDateTo(),
                    pageable);
        } else {
            // Lấy tất cả
            headerPage = promotionHeaderRepository.findAll(pageable);
        }

        return headerPage.map(this::convertToHeaderResponseDTO);
    }

    // =====================================================
    // PRIVATE HELPER METHODS
    // =====================================================

    /**
     * Validation chung cho ngày tháng và trạng thái (được tái sử dụng bởi các
     * validation khác)
     *
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @param status    trạng thái
     * @param context   ngữ cảnh để custom error message (vd: "header", "line")
     * @throws PromotionValidationException nếu validation không hợp lệ
     */
    private void validateDateRangeAndStatus(LocalDate startDate, LocalDate endDate,
            PromotionStatus status, String context) {
        // Validation 1: startDate < endDate
        if (startDate.isAfter(endDate)) {
            String message = context.isEmpty()
                    ? "Ngày bắt đầu phải trước ngày kết thúc"
                    : String.format("Ngày bắt đầu %s phải trước ngày kết thúc %s", context, context);
            throw new PromotionValidationException(message);
        }

        // Validation 2: endDate > now
        LocalDate now = LocalDate.now();
        if (!endDate.isAfter(now)) {
            String message = context.isEmpty()
                    ? "Ngày kết thúc phải lớn hơn ngày hiện tại"
                    : String.format("Ngày kết thúc %s phải lớn hơn ngày hiện tại", context);
            throw new PromotionValidationException(message);
        }

        // Validation 3: status chỉ được ACTIVE hoặc PAUSED
        if (status != PromotionStatus.ACTIVE && status != PromotionStatus.PAUSED) {
            String message = context.isEmpty()
                    ? "Trạng thái chỉ được phép là ACTIVE hoặc PAUSED"
                    : String.format("Trạng thái %s chỉ được phép là ACTIVE hoặc PAUSED", context);
            throw new PromotionValidationException(message);
        }
    }

    /**
     * Validation cho PromotionHeaderOnlyRequestDTO (chỉ header, không có lines)
     */
    private void validatePromotionHeaderOnlyRequest(PromotionHeaderOnlyRequestDTO requestDTO) {
        validateDateRangeAndStatus(requestDTO.getStartDate(), requestDTO.getEndDate(),
                requestDTO.getStatus(), "");
    }

    /**
     * Validation cho PromotionLineOnlyRequestDTO (chỉ line, không có detail)
     */
    private void validatePromotionLineOnlyRequest(PromotionLineOnlyRequestDTO requestDTO) {
        validateDateRangeAndStatus(requestDTO.getStartDate(), requestDTO.getEndDate(),
                requestDTO.getStatus(), "line");
    }

    /**
     * Validation cho PromotionLineRequestDTO (có cả detail)
     */
    private void validatePromotionLineRequest(PromotionLineRequestDTO lineDTO) {
        validateDateRangeAndStatus(lineDTO.getStartDate(), lineDTO.getEndDate(),
                lineDTO.getStatus(), "line");

        if (lineDTO.getDetail() == null) {
            throw new PromotionValidationException("Chi tiết khuyến mãi không được để trống");
        }

        // Validate chi tiết theo loại khuyến mãi
        validatePromotionDetailRequest(lineDTO.getDetail(), lineDTO.getPromotionType());
    }

    /**
     * Validation ngày tháng của line phải nằm trong khoảng của header
     *
     * @param lineStartDate ngày bắt đầu của line
     * @param lineEndDate   ngày kết thúc của line
     * @param header        promotion header cha
     * @throws PromotionValidationException nếu ngày tháng của line nằm ngoài khoảng
     *                                      của header
     */
    private void validatePromotionLineDateRangeWithinHeader(LocalDate lineStartDate, LocalDate lineEndDate,
            PromotionHeader header) {
        if (lineStartDate.isBefore(header.getStartDate())) {
            throw new PromotionValidationException(
                    String.format(
                            "Ngày bắt đầu của line (%s) phải lớn hơn hoặc bằng ngày bắt đầu của chương trình khuyến mãi (%s)",
                            lineStartDate, header.getStartDate()));
        }

        if (lineEndDate.isAfter(header.getEndDate())) {
            throw new PromotionValidationException(
                    String.format(
                            "Ngày kết thúc của line (%s) phải nhỏ hơn hoặc bằng ngày kết thúc của chương trình khuyến mãi (%s)",
                            lineEndDate, header.getEndDate()));
        }
    }

    /**
     * Validation cho PromotionDetailRequestDTO dựa trên loại khuyến mãi
     */
    private void validatePromotionDetailRequest(PromotionDetailRequestDTO detailDTO, PromotionType promotionType) {
        switch (promotionType) {
            case BUY_X_GET_Y:
                validateBuyXGetYDetail(detailDTO);
                break;
            case ORDER_DISCOUNT:
                validateOrderDiscountDetail(detailDTO);
                break;
            case PRODUCT_DISCOUNT:
                validateProductDiscountDetail(detailDTO);
                break;
            default:
                throw new PromotionValidationException("Loại khuyến mãi không hợp lệ: " + promotionType);
        }
    }

    /**
     * Validation cho khuyến mãi loại BUY_X_GET_Y
     */
    private void validateBuyXGetYDetail(PromotionDetailRequestDTO detailDTO) {
        if (detailDTO.getBuyProductId() == null) {
            throw new PromotionValidationException("Sản phẩm phải mua không được để trống cho khuyến mãi Mua X Tặng Y");
        }

        if (detailDTO.getGiftProductId() == null) {
            throw new PromotionValidationException("Sản phẩm tặng không được để trống cho khuyến mãi Mua X Tặng Y");
        }

        if (detailDTO.getGiftDiscountType() == null) {
            throw new PromotionValidationException("Loại giảm giá quà tặng không được để trống");
        }

        // Kiểm tra sản phẩm tồn tại
        if (!productUnitRepository.existsById(detailDTO.getBuyProductId())) {
            throw new PromotionValidationException("Sản phẩm phải mua không tồn tại");
        }

        if (!productUnitRepository.existsById(detailDTO.getGiftProductId())) {
            throw new PromotionValidationException("Sản phẩm tặng không tồn tại");
        }

        // Validate điều kiện mua
        if (detailDTO.getBuyMinQuantity() == null && detailDTO.getBuyMinValue() == null) {
            throw new PromotionValidationException(
                    "Phải có ít nhất một điều kiện: số lượng tối thiểu hoặc giá trị tối thiểu");
        }
    }

    /**
     * Validation cho khuyến mãi loại ORDER_DISCOUNT
     */
    private void validateOrderDiscountDetail(PromotionDetailRequestDTO detailDTO) {
        if (detailDTO.getOrderDiscountType() == null) {
            throw new PromotionValidationException("Loại giảm giá đơn hàng không được để trống");
        }

        if (detailDTO.getOrderDiscountValue() == null) {
            throw new PromotionValidationException("Giá trị giảm giá đơn hàng không được để trống");
        }

        // Validate điều kiện đơn hàng tối thiểu
        if (detailDTO.getOrderMinTotalValue() == null && detailDTO.getOrderMinTotalQuantity() == null) {
            throw new PromotionValidationException(
                    "Phải có ít nhất một điều kiện: giá trị tối thiểu hoặc số lượng tối thiểu");
        }
    }

    /**
     * Validation cho khuyến mãi loại PRODUCT_DISCOUNT
     */
    private void validateProductDiscountDetail(PromotionDetailRequestDTO detailDTO) {
        if (detailDTO.getProductDiscountType() == null) {
            throw new PromotionValidationException("Loại giảm giá sản phẩm không được để trống");
        }

        if (detailDTO.getProductDiscountValue() == null) {
            throw new PromotionValidationException("Giá trị giảm giá sản phẩm không được để trống");
        }

        if (detailDTO.getApplyToType() == null) {
            throw new PromotionValidationException("Loại áp dụng không được để trống");
        }

        // Validate theo loại áp dụng
        switch (detailDTO.getApplyToType()) {
            case PRODUCT:
                if (detailDTO.getApplyToProductId() == null) {
                    throw new PromotionValidationException("ID sản phẩm áp dụng không được để trống");
                }
                if (!productUnitRepository.existsById(detailDTO.getApplyToProductId())) {
                    throw new PromotionValidationException("Sản phẩm áp dụng không tồn tại");
                }
                break;
            case ALL:
                // Không cần validate thêm
                break;
        }
    }


    /**
     * Tạo PromotionDetail từ DTO sử dụng Factory Pattern
     */
    private PromotionDetail createPromotionDetailFromDTO(PromotionDetailRequestDTO detailDTO, PromotionLine line) {
        PromotionDetail detail = promotionDetailFactory.createDetail(line.getPromotionType(), detailDTO);
        detail.setPromotionLine(line);

        // Set các trường mới: promotionCode, usageLimit, usageCount
        detail.setPromotionCode(detailDTO.getPromotionCode());
        detail.setUsageLimit(detailDTO.getUsageLimit());
        detail.setUsageCount(0); // Tự động set = 0 khi tạo mới, không cho client truyền

        return detail;
    }

    /**
     * Xóa tất cả lines và details của một chương trình
     */
    private void deleteExistingLinesAndDetails(Long promotionId) {
        List<PromotionLine> existingLines = promotionLineRepository.findByPromotionHeaderId(promotionId);

        for (PromotionLine line : existingLines) {
            // Xóa tất cả details của line trước
            if (line.getDetails() != null && !line.getDetails().isEmpty()) {
                promotionDetailRepository.deleteAll(line.getDetails());
            }
        }

        // Xóa tất cả lines
        promotionLineRepository.deleteByHeader_PromotionId(promotionId);
    }

    /**
     * Chuyển đổi PromotionHeader entity sang PromotionHeaderResponseDTO
     */
    private PromotionHeaderResponseDTO convertToHeaderResponseDTO(PromotionHeader header) {
        PromotionHeaderResponseDTO responseDTO = new PromotionHeaderResponseDTO();

        // Thiết lập thông tin cơ bản
        responseDTO.setPromotionId(header.getPromotionId());
        responseDTO.setPromotionName(header.getPromotionName());
        responseDTO.setDescription(header.getDescription());
        responseDTO.setStartDate(header.getStartDate());
        responseDTO.setEndDate(header.getEndDate());
        responseDTO.setStatus(header.getStatus());
        responseDTO.setCreatedAt(header.getCreatedAt());
        responseDTO.setUpdatedAt(header.getUpdatedAt());

        // Chuyển đổi promotion lines nếu có
        if (header.getPromotionLines() != null && !header.getPromotionLines().isEmpty()) {
            List<PromotionLineResponseDTO> lineResponseDTOs = header.getPromotionLines().stream()
                    .map(this::convertToLineResponseDTO)
                    .collect(Collectors.toList());
            responseDTO.setPromotionLines(lineResponseDTOs);
        }

        // Tính toán các trường computed
        responseDTO.calculateOverallStatus();
        responseDTO.calculateLineStatistics();

        return responseDTO;
    }

    /**
     * Chuyển đổi PromotionLine entity sang PromotionLineResponseDTO
     */
    private PromotionLineResponseDTO convertToLineResponseDTO(PromotionLine line) {
        PromotionLineResponseDTO responseDTO = new PromotionLineResponseDTO();

        // Thiết lập thông tin cơ bản
        responseDTO.setPromotionLineId(line.getPromotionLineId());
        responseDTO.setLineName(line.getLineName());
        responseDTO.setPromotionType(line.getPromotionType());
        responseDTO.setDescription(line.getDescription());
        responseDTO.setStartDate(line.getStartDate());
        responseDTO.setEndDate(line.getEndDate());
        responseDTO.setStatus(line.getStatus());
        responseDTO.setCreatedAt(line.getCreatedAt());
        responseDTO.setUpdatedAt(line.getUpdatedAt());

        // Chuyển đổi tất cả details nếu có
        if (line.getDetails() != null && !line.getDetails().isEmpty()) {
            List<PromotionDetailResponseDTO> detailResponseDTOs = line.getDetails().stream()
                    .map(this::convertToDetailResponseDTO)
                    .collect(Collectors.toList());
            responseDTO.setDetails(detailResponseDTOs);
        }

        // Tính toán trạng thái hoạt động
        responseDTO.calculateActiveStatus();

        return responseDTO;
    }

    /**
     * Chuyển đổi PromotionDetail entity sang PromotionDetailResponseDTO
     */
    private PromotionDetailResponseDTO convertToDetailResponseDTO(PromotionDetail detail) {
        PromotionDetailResponseDTO responseDTO = new PromotionDetailResponseDTO();
        responseDTO.setDetailId(detail.getDetailId());
        responseDTO.setPromotionCode(detail.getPromotionCode());
        responseDTO.setUsageLimit(detail.getUsageLimit());
        responseDTO.setUsageCount(detail.getUsageCount());

        if (detail instanceof BuyXGetYDetail buyXGetY) {
            // Thiết lập thông tin BUY_X_GET_Y
            if (buyXGetY.getBuyProduct() != null) {
                responseDTO.setBuyProduct(convertToProductUnitInfo(buyXGetY.getBuyProduct()));
            }
            responseDTO.setBuyMinQuantity(buyXGetY.getBuyMinQuantity());
            responseDTO.setBuyMinValue(buyXGetY.getBuyMinValue());

            if (buyXGetY.getGiftProduct() != null) {
                responseDTO.setGiftProduct(convertToProductUnitInfo(buyXGetY.getGiftProduct()));
            }
            responseDTO.setGiftQuantity(buyXGetY.getGiftQuantity());
            responseDTO.setGiftDiscountType(buyXGetY.getGiftDiscountType());
            responseDTO.setGiftDiscountValue(buyXGetY.getGiftDiscountValue());
            responseDTO.setGiftMaxQuantity(buyXGetY.getGiftMaxQuantity());
        } else if (detail instanceof OrderDiscountDetail orderDiscount) {
            // Thiết lập thông tin ORDER_DISCOUNT
            responseDTO.setOrderDiscountType(orderDiscount.getOrderDiscountType());
            responseDTO.setOrderDiscountValue(orderDiscount.getOrderDiscountValue());
            responseDTO.setOrderDiscountMaxValue(orderDiscount.getOrderDiscountMaxValue());
            responseDTO.setOrderMinTotalValue(orderDiscount.getOrderMinTotalValue());
            responseDTO.setOrderMinTotalQuantity(orderDiscount.getOrderMinTotalQuantity());
        } else if (detail instanceof ProductDiscountDetail productDiscount) {
            // Thiết lập thông tin PRODUCT_DISCOUNT
            responseDTO.setProductDiscountType(productDiscount.getProductDiscountType());
            responseDTO.setProductDiscountValue(productDiscount.getProductDiscountValue());
            responseDTO.setApplyToType(productDiscount.getApplyToType());

            if (productDiscount.getApplyToProduct() != null) {
                responseDTO.setApplyToProduct(convertToProductUnitInfo(productDiscount.getApplyToProduct()));
            }

            responseDTO.setProductMinOrderValue(productDiscount.getProductMinOrderValue());
            responseDTO.setProductMinPromotionValue(productDiscount.getProductMinPromotionValue());
            responseDTO.setProductMinPromotionQuantity(productDiscount.getProductMinPromotionQuantity());
        }

        return responseDTO;
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitInfo
     */
    private PromotionDetailResponseDTO.ProductUnitInfo convertToProductUnitInfo(ProductUnit productUnit) {
        PromotionDetailResponseDTO.ProductUnitInfo info = new PromotionDetailResponseDTO.ProductUnitInfo();
        info.setProductUnitId(productUnit.getId());
        info.setProductName(productUnit.getProduct().getName());
        info.setUnitName(productUnit.getUnit().getName());
        info.setVariantCode(String.valueOf(productUnit.getId())); // Sử dụng ID làm variant code

        // Lấy giá hiện tại (có thể cần implement logic lấy giá từ PriceDetail)
        // Tạm thời để null, sẽ implement sau
        info.setCurrentPrice(null);
        info.setImageUrl(null); // Tương tự cho image URL

        return info;
    }

    // =====================================================
    // NEW UPDATE METHODS - SEPARATED BY ENTITY
    // =====================================================

    /**
     * Cập nhật chỉ promotion header (không cập nhật lines)
     * 
     * @param promotionId ID của promotion header cần cập nhật
     * @param requestDTO  thông tin cập nhật
     * @return PromotionHeaderResponseDTO chứa thông tin đã cập nhật
     * @throws PromotionNotFoundException   nếu không tìm thấy promotion header
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public PromotionHeaderResponseDTO updatePromotionHeaderOnly(Long promotionId,
            PromotionHeaderOnlyRequestDTO requestDTO) {
        log.info("Bắt đầu cập nhật promotion header ID: {}", promotionId);

        // Tìm promotion header hiện tại
        PromotionHeader existingHeader = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion header với ID: " + promotionId));

        // Validation cơ bản
        validatePromotionHeaderOnlyRequest(requestDTO);

        // Kiểm tra trùng lặp tên (trừ chương trình hiện tại)
        if (promotionHeaderRepository.existsByPromotionNameIgnoreCaseAndPromotionIdNot(
                requestDTO.getPromotionName(), promotionId)) {
            throw new DuplicatePromotionException(
                    "Tên chương trình khuyến mãi đã tồn tại: " + requestDTO.getPromotionName());
        }

        // Cập nhật thông tin header (không động chạm đến lines)
        existingHeader.setPromotionName(requestDTO.getPromotionName());
        existingHeader.setDescription(requestDTO.getDescription());
        existingHeader.setStartDate(requestDTO.getStartDate());
        existingHeader.setEndDate(requestDTO.getEndDate());
        existingHeader.setStatus(requestDTO.getStatus());

        existingHeader = promotionHeaderRepository.save(existingHeader);

        log.info("Đã cập nhật thành công promotion header ID: {}", promotionId);
        return convertToHeaderResponseDTO(existingHeader);
    }

    /**
     * Cập nhật promotion line (không bao gồm detail)
     * Detail phải được cập nhật riêng thông qua endpoint PUT /details/{detailId}
     *
     * @param lineId     ID của promotion line cần cập nhật
     * @param requestDTO thông tin cập nhật (không bao gồm detail)
     * @return PromotionLineResponseDTO chứa thông tin đã cập nhật
     * @throws PromotionNotFoundException   nếu không tìm thấy promotion line
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     */
    @Transactional
    public PromotionLineResponseDTO updatePromotionLine(Long lineId, PromotionLineOnlyRequestDTO requestDTO) {
        log.info("Bắt đầu cập nhật promotion line ID: {}", lineId);

        // Tìm promotion line hiện tại
        PromotionLine existingLine = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion line với ID: " + lineId));

        // Validation cơ bản
        validatePromotionLineOnlyRequest(requestDTO);

        // Validation ngày tháng của line phải nằm trong khoảng của header
        validatePromotionLineDateRangeWithinHeader(requestDTO.getStartDate(), requestDTO.getEndDate(),
                existingLine.getHeader());

        // Cập nhật thông tin line (không động chạm đến details)
        existingLine.setLineName(requestDTO.getLineName());
        existingLine.setPromotionType(requestDTO.getPromotionType());
        existingLine.setDescription(requestDTO.getDescription());
        existingLine.setStartDate(requestDTO.getStartDate());
        existingLine.setEndDate(requestDTO.getEndDate());
        existingLine.setStatus(requestDTO.getStatus());

        existingLine = promotionLineRepository.save(existingLine);

        log.info("Đã cập nhật thành công promotion line ID: {} (không cập nhật details)", lineId);
        return convertToLineResponseDTO(existingLine);
    }

    /**
     * Cập nhật promotion detail
     *
     * @param detailId   ID của promotion detail cần cập nhật
     * @param requestDTO thông tin detail cập nhật
     * @return PromotionDetailResponseDTO chứa thông tin đã cập nhật
     * @throws PromotionNotFoundException   nếu không tìm thấy promotion detail
     * @throws PromotionValidationException nếu dữ liệu không hợp lệ
     * @throws DuplicatePromotionException  nếu mã khuyến mãi đã tồn tại
     */
    @Transactional
    public PromotionDetailResponseDTO updatePromotionDetail(Long detailId, PromotionDetailRequestDTO requestDTO) {
        log.info("Bắt đầu cập nhật promotion detail ID: {}", detailId);

        // Tìm promotion detail hiện tại
        PromotionDetail existingDetail = promotionDetailRepository.findById(detailId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy promotion detail với ID: " + detailId));

        // Lấy promotion line để kiểm tra loại khuyến mãi
        PromotionLine line = existingDetail.getPromotionLine();

        // Validation theo loại khuyến mãi
        validatePromotionDetailRequest(requestDTO, line.getPromotionType());

        // Kiểm tra và cập nhật promotionCode nếu có thay đổi
        if (requestDTO.getPromotionCode() != null &&
            !requestDTO.getPromotionCode().equals(existingDetail.getPromotionCode())) {
            // Kiểm tra trùng lặp mã khuyến mãi (trừ detail hiện tại)
            if (promotionDetailRepository.existsByPromotionCodeIgnoreCaseAndDetailIdNot(
                    requestDTO.getPromotionCode(), detailId)) {
                throw new DuplicatePromotionException(
                        "Mã khuyến mãi đã tồn tại: " + requestDTO.getPromotionCode());
            }
            existingDetail.setPromotionCode(requestDTO.getPromotionCode());
            log.info("Đã cập nhật promotionCode từ '{}' sang '{}'",
                    existingDetail.getPromotionCode(), requestDTO.getPromotionCode());
        }

        // Cập nhật usageLimit (cho phép null - không giới hạn)
        existingDetail.setUsageLimit(requestDTO.getUsageLimit());
        log.info("Đã cập nhật usageLimit sang: {}", requestDTO.getUsageLimit());

        // LƯU Ý: KHÔNG cập nhật usageCount - đây là counter chỉ được system quản lý

        // Cập nhật các thông tin detail khác dựa trên loại khuyến mãi
        updateDetailFromDTO(existingDetail, requestDTO, line.getPromotionType());

        existingDetail = promotionDetailRepository.save(existingDetail);

        log.info("Đã cập nhật thành công promotion detail ID: {}", detailId);
        return convertToDetailResponseDTO(existingDetail);
    }

    /**
     * Cập nhật thông tin detail từ DTO
     * 
     * @param detail        detail cần cập nhật
     * @param detailDTO     thông tin cập nhật
     * @param promotionType loại khuyến mãi để validation
     */
    private void updateDetailFromDTO(PromotionDetail detail, PromotionDetailRequestDTO detailDTO,
            PromotionType promotionType) {
        promotionDetailFactory.updateDetail(detail, detailDTO);
    }

}
