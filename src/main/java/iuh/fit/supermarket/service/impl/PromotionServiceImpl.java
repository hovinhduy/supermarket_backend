package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.PromotionService;
import iuh.fit.supermarket.util.PromotionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của PromotionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private final PromotionHeaderRepository promotionHeaderRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final ProductUnitRepository productUnitRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionValidator promotionValidator;

    // ==================== PROMOTION HEADER OPERATIONS ====================

    @Override
    public PromotionHeaderDTO createPromotion(PromotionCreateRequest request) {
        log.info("Tạo chương trình khuyến mãi mới: {}", request.getName());

        // Validate dữ liệu
        validatePromotionData(request);

        // Kiểm tra xung đột
        validatePromotionConflicts(request);

        // Tạo entity
        PromotionHeader promotion = new PromotionHeader();
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus());

        // Lưu promotion header
        promotion = promotionHeaderRepository.save(promotion);

        // Tạo promotion lines nếu có
        if (request.getPromotionLines() != null && !request.getPromotionLines().isEmpty()) {
            List<PromotionLine> promotionLines = new ArrayList<>();
            for (PromotionLineCreateRequest lineRequest : request.getPromotionLines()) {
                PromotionLine line = createPromotionLineEntity(promotion, lineRequest);
                promotionLines.add(line);
            }
            promotion.setPromotionLines(promotionLines);
        }

        log.info("Đã tạo thành công chương trình khuyến mãi: {}", promotion.getName());
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    public PromotionHeaderDTO updatePromotion(Long promotionId, PromotionCreateRequest request) {
        log.info("Cập nhật chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        // Validate dữ liệu
        validatePromotionData(request);

        // Cập nhật thông tin
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus());

        promotion = promotionHeaderRepository.save(promotion);

        log.info("Đã cập nhật thành công chương trình khuyến mãi: {}", promotion.getName());
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    public void deletePromotion(Long promotionId) {
        log.info("Xóa chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        // Kiểm tra có thể xóa không (business rules)
        if (promotion.getStatus() == PromotionStatus.ACTIVE) {
            throw new PromotionValidationException("Không thể xóa chương trình khuyến mãi đang hoạt động");
        }

        promotionHeaderRepository.delete(promotion);
        log.info("Đã xóa thành công chương trình khuyến mãi ID: {}", promotionId);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionHeaderDTO getPromotionById(Long promotionId) {
        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> getAllPromotions(Pageable pageable) {
        Page<PromotionHeader> promotions = promotionHeaderRepository.findAll(pageable);
        return promotions.map(this::convertToPromotionHeaderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> searchPromotionsByName(String keyword, Pageable pageable) {
        Page<PromotionHeader> promotions = promotionHeaderRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return promotions.map(this::convertToPromotionHeaderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> getPromotionsByStatus(PromotionStatus status, Pageable pageable) {
        Page<PromotionHeader> promotions = promotionHeaderRepository.findByStatus(status, pageable);
        return promotions.map(this::convertToPromotionHeaderDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionHeaderDTO> getCurrentActivePromotions() {
        List<PromotionHeader> promotions = promotionHeaderRepository.findCurrentActivePromotions(LocalDateTime.now());
        return promotions.stream()
                .map(this::convertToPromotionHeaderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionHeaderDTO> getPromotionsExpiringWithin(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(days);

        List<PromotionHeader> promotions = promotionHeaderRepository.findPromotionsExpiringWithin(now, futureDate);
        return promotions.stream()
                .map(this::convertToPromotionHeaderDTO)
                .collect(Collectors.toList());
    }

    // ==================== PROMOTION LINE OPERATIONS ====================

    @Override
    public PromotionLineDTO createPromotionLine(Long promotionId, PromotionLineCreateRequest request) {
        log.info("Tạo dòng khuyến mãi mới cho promotion ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        // Validate dữ liệu
        validatePromotionLineData(request);

        // Kiểm tra xung đột
        validatePromotionLineConflicts(promotionId, request);

        PromotionLine promotionLine = createPromotionLineEntity(promotion, request);

        log.info("Đã tạo thành công dòng khuyến mãi: {}", promotionLine.getLineCode());
        return convertToPromotionLineDTO(promotionLine);
    }

    @Override
    public PromotionLineDTO updatePromotionLine(Long lineId, PromotionLineCreateRequest request) {
        log.info("Cập nhật dòng khuyến mãi ID: {}", lineId);

        PromotionLine promotionLine = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> PromotionNotFoundException.forLineId(lineId));

        // Validate dữ liệu
        validatePromotionLineData(request);

        // Cập nhật thông tin
        promotionLine.setDescription(request.getDescription());
        promotionLine.setPromotionType(request.getPromotionType());
        promotionLine.setStartDate(request.getStartDate());
        promotionLine.setEndDate(request.getEndDate());
        promotionLine.setStatus(request.getStatus());
        promotionLine.setIsCombinable(request.getIsCombinable());
        promotionLine.setMaxTotalQuantity(request.getMaxTotalQuantity());
        promotionLine.setMaxPerCustomer(request.getMaxPerCustomer());
        promotionLine.setPriority(request.getPriority());

        promotionLine = promotionLineRepository.save(promotionLine);

        log.info("Đã cập nhật thành công dòng khuyến mãi: {}", promotionLine.getLineCode());
        return convertToPromotionLineDTO(promotionLine);
    }

    @Override
    public void deletePromotionLine(Long lineId) {
        log.info("Xóa dòng khuyến mãi ID: {}", lineId);

        PromotionLine promotionLine = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> PromotionNotFoundException.forLineId(lineId));

        // Kiểm tra có thể xóa không
        if (promotionLine.getStatus() == PromotionStatus.ACTIVE) {
            throw new PromotionValidationException("Không thể xóa dòng khuyến mãi đang hoạt động");
        }

        promotionLineRepository.delete(promotionLine);
        log.info("Đã xóa thành công dòng khuyến mãi ID: {}", lineId);
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionLineDTO getPromotionLineById(Long lineId) {
        PromotionLine promotionLine = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> PromotionNotFoundException.forLineId(lineId));
        return convertToPromotionLineDTO(promotionLine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionLineDTO> getPromotionLinesByPromotionId(Long promotionId) {
        List<PromotionLine> promotionLines = promotionLineRepository.findByPromotionPromotionId(promotionId);
        return promotionLines.stream()
                .map(this::convertToPromotionLineDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionLineDTO> getPromotionLinesByType(PromotionType promotionType) {
        List<PromotionLine> promotionLines = promotionLineRepository.findByPromotionType(promotionType);
        return promotionLines.stream()
                .map(this::convertToPromotionLineDTO)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private PromotionLine createPromotionLineEntity(PromotionHeader promotion, PromotionLineCreateRequest request) {
        // Kiểm tra mã dòng khuyến mãi đã tồn tại
        if (promotionLineRepository.existsByLineCode(request.getLineCode())) {
            throw DuplicatePromotionException.forLineCode(request.getLineCode());
        }

        PromotionLine promotionLine = new PromotionLine();
        promotionLine.setLineCode(request.getLineCode());
        promotionLine.setDescription(request.getDescription());
        promotionLine.setPromotionType(request.getPromotionType());
        promotionLine.setStartDate(request.getStartDate());
        promotionLine.setEndDate(request.getEndDate());
        promotionLine.setStatus(request.getStatus());
        promotionLine.setIsCombinable(request.getIsCombinable());
        promotionLine.setMaxTotalQuantity(request.getMaxTotalQuantity());
        promotionLine.setMaxPerCustomer(request.getMaxPerCustomer());
        promotionLine.setPriority(request.getPriority());
        promotionLine.setPromotion(promotion);

        promotionLine = promotionLineRepository.save(promotionLine);

        // Tạo promotion details nếu có
        if (request.getPromotionDetails() != null && !request.getPromotionDetails().isEmpty()) {
            List<PromotionDetail> promotionDetails = new ArrayList<>();
            for (PromotionDetailCreateRequest detailRequest : request.getPromotionDetails()) {
                PromotionDetail detail = createPromotionDetailEntity(promotionLine, detailRequest);
                promotionDetails.add(detail);
            }
            promotionLine.setPromotionDetails(promotionDetails);
        }

        return promotionLine;
    }

    private PromotionDetail createPromotionDetailEntity(PromotionLine promotionLine,
            PromotionDetailCreateRequest request) {
        // Validate promotion detail data
        validatePromotionDetailData(request);

        PromotionDetail promotionDetail = new PromotionDetail();
        promotionDetail.setValue(request.getValue());
        promotionDetail.setMinOrderValue(request.getMinOrderValue());
        promotionDetail.setMaxDiscountValue(request.getMaxDiscountValue());
        promotionDetail.setConditionBuyQuantity(request.getConditionBuyQuantity());
        promotionDetail.setGiftQuantity(request.getGiftQuantity());
        promotionDetail.setPromotionLine(promotionLine);

        // Set condition product unit
        if (request.getConditionProductUnitId() != null) {
            ProductUnit conditionProductUnit = productUnitRepository.findById(request.getConditionProductUnitId())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy product unit điều kiện"));
            promotionDetail.setConditionProductUnit(conditionProductUnit);
        }

        // Set condition category
        if (request.getConditionCategoryId() != null) {
            Category conditionCategory = categoryRepository.findById(request.getConditionCategoryId().intValue())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy category điều kiện"));
            promotionDetail.setConditionCategory(conditionCategory);
        }

        // Set gift product unit
        if (request.getGiftProductUnitId() != null) {
            ProductUnit giftProductUnit = productUnitRepository.findById(request.getGiftProductUnitId())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy product unit tặng"));
            promotionDetail.setGiftProductUnit(giftProductUnit);
        }

        return promotionDetailRepository.save(promotionDetail);
    }

    private PromotionHeaderDTO convertToPromotionHeaderDTO(PromotionHeader promotion) {
        PromotionHeaderDTO dto = new PromotionHeaderDTO();
        dto.setPromotionId(promotion.getPromotionId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setStatus(promotion.getStatus());
        dto.setCreatedAt(promotion.getCreatedAt());

        if (promotion.getPromotionLines() != null) {
            List<PromotionLineDTO> lineDTOs = promotion.getPromotionLines().stream()
                    .map(this::convertToPromotionLineDTO)
                    .collect(Collectors.toList());
            dto.setPromotionLines(lineDTOs);
        }

        return dto;
    }

    private PromotionLineDTO convertToPromotionLineDTO(PromotionLine promotionLine) {
        PromotionLineDTO dto = new PromotionLineDTO();
        dto.setLineId(promotionLine.getLineId());
        dto.setLineCode(promotionLine.getLineCode());
        dto.setDescription(promotionLine.getDescription());
        dto.setPromotionType(promotionLine.getPromotionType());
        dto.setStartDate(promotionLine.getStartDate());
        dto.setEndDate(promotionLine.getEndDate());
        dto.setStatus(promotionLine.getStatus());
        dto.setCreatedAt(promotionLine.getCreatedAt());
        dto.setUpdatedAt(promotionLine.getUpdatedAt());
        dto.setIsCombinable(promotionLine.getIsCombinable());
        dto.setMaxTotalQuantity(promotionLine.getMaxTotalQuantity());
        dto.setMaxPerCustomer(promotionLine.getMaxPerCustomer());
        dto.setPriority(promotionLine.getPriority());
        dto.setPromotionId(promotionLine.getPromotion().getPromotionId());

        if (promotionLine.getPromotionDetails() != null) {
            List<PromotionDetailDTO> detailDTOs = promotionLine.getPromotionDetails().stream()
                    .map(this::convertToPromotionDetailDTO)
                    .collect(Collectors.toList());
            dto.setPromotionDetails(detailDTOs);
        }

        return dto;
    }

    private PromotionDetailDTO convertToPromotionDetailDTO(PromotionDetail promotionDetail) {
        PromotionDetailDTO dto = new PromotionDetailDTO();
        dto.setDetailId(promotionDetail.getDetailId());
        dto.setValue(promotionDetail.getValue());
        dto.setMinOrderValue(promotionDetail.getMinOrderValue());
        dto.setMaxDiscountValue(promotionDetail.getMaxDiscountValue());
        dto.setConditionBuyQuantity(promotionDetail.getConditionBuyQuantity());
        dto.setGiftQuantity(promotionDetail.getGiftQuantity());
        dto.setLineId(promotionDetail.getPromotionLine().getLineId());

        if (promotionDetail.getConditionProductUnit() != null) {
            dto.setConditionProductUnitId(promotionDetail.getConditionProductUnit().getId());
            dto.setConditionProductUnitName(promotionDetail.getConditionProductUnit().getCode());
        }

        if (promotionDetail.getConditionCategory() != null) {
            dto.setConditionCategoryId(promotionDetail.getConditionCategory().getCategoryId().longValue());
            dto.setConditionCategoryName(promotionDetail.getConditionCategory().getName());
        }

        if (promotionDetail.getGiftProductUnit() != null) {
            dto.setGiftProductUnitId(promotionDetail.getGiftProductUnit().getId());
            dto.setGiftProductUnitName(promotionDetail.getGiftProductUnit().getCode());
        }

        return dto;
    }

    // ==================== PROMOTION DETAIL OPERATIONS ====================

    @Override
    public PromotionDetailDTO createPromotionDetail(Long lineId, PromotionDetailCreateRequest request) {
        log.info("Tạo chi tiết khuyến mãi mới cho line ID: {}", lineId);

        PromotionLine promotionLine = promotionLineRepository.findById(lineId)
                .orElseThrow(() -> PromotionNotFoundException.forLineId(lineId));

        PromotionDetail promotionDetail = createPromotionDetailEntity(promotionLine, request);

        log.info("Đã tạo thành công chi tiết khuyến mãi cho line: {}", promotionLine.getLineCode());
        return convertToPromotionDetailDTO(promotionDetail);
    }

    @Override
    public PromotionDetailDTO updatePromotionDetail(Long detailId, PromotionDetailCreateRequest request) {
        log.info("Cập nhật chi tiết khuyến mãi ID: {}", detailId);

        PromotionDetail promotionDetail = promotionDetailRepository.findById(detailId)
                .orElseThrow(() -> PromotionNotFoundException.forDetailId(detailId));

        // Validate dữ liệu
        validatePromotionDetailData(request);

        // Cập nhật thông tin
        promotionDetail.setValue(request.getValue());
        promotionDetail.setMinOrderValue(request.getMinOrderValue());
        promotionDetail.setMaxDiscountValue(request.getMaxDiscountValue());
        promotionDetail.setConditionBuyQuantity(request.getConditionBuyQuantity());
        promotionDetail.setGiftQuantity(request.getGiftQuantity());

        // Update references
        if (request.getConditionProductUnitId() != null) {
            ProductUnit conditionProductUnit = productUnitRepository.findById(request.getConditionProductUnitId())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy product unit điều kiện"));
            promotionDetail.setConditionProductUnit(conditionProductUnit);
        } else {
            promotionDetail.setConditionProductUnit(null);
        }

        if (request.getConditionCategoryId() != null) {
            Category conditionCategory = categoryRepository.findById(request.getConditionCategoryId().intValue())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy category điều kiện"));
            promotionDetail.setConditionCategory(conditionCategory);
        } else {
            promotionDetail.setConditionCategory(null);
        }

        if (request.getGiftProductUnitId() != null) {
            ProductUnit giftProductUnit = productUnitRepository.findById(request.getGiftProductUnitId())
                    .orElseThrow(() -> new PromotionValidationException("Không tìm thấy product unit tặng"));
            promotionDetail.setGiftProductUnit(giftProductUnit);
        } else {
            promotionDetail.setGiftProductUnit(null);
        }

        promotionDetail = promotionDetailRepository.save(promotionDetail);

        log.info("Đã cập nhật thành công chi tiết khuyến mãi ID: {}", detailId);
        return convertToPromotionDetailDTO(promotionDetail);
    }

    @Override
    public void deletePromotionDetail(Long detailId) {
        log.info("Xóa chi tiết khuyến mãi ID: {}", detailId);

        PromotionDetail promotionDetail = promotionDetailRepository.findById(detailId)
                .orElseThrow(() -> PromotionNotFoundException.forDetailId(detailId));

        promotionDetailRepository.delete(promotionDetail);
        log.info("Đã xóa thành công chi tiết khuyến mãi ID: {}", detailId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionDetailDTO> getPromotionDetailsByLineId(Long lineId) {
        List<PromotionDetail> promotionDetails = promotionDetailRepository.findByPromotionLineLineId(lineId);
        return promotionDetails.stream()
                .map(this::convertToPromotionDetailDTO)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION OPERATIONS ====================

    @Override
    public void validatePromotionData(PromotionCreateRequest request) {
        promotionValidator.validatePromotionRequest(request);
    }

    @Override
    public void validatePromotionLineData(PromotionLineCreateRequest request) {
        promotionValidator.validatePromotionLine(request);
    }

    @Override
    public void validatePromotionDetailData(PromotionDetailCreateRequest request) {
        // Sử dụng PromotionValidator với promotion type mặc định
        promotionValidator.validatePromotionDetail(request, PromotionType.PERCENT_PRODUCT);
    }

    @Override
    public void validatePromotionConflicts(PromotionCreateRequest request) {
        // Kiểm tra overlap về thời gian
        List<PromotionHeader> overlappingPromotions = promotionHeaderRepository.findOverlappingPromotions(
                0L, request.getStartDate(), request.getEndDate());

        if (!overlappingPromotions.isEmpty()) {
            log.warn("Tìm thấy {} chương trình khuyến mãi có thời gian overlap", overlappingPromotions.size());
            // Có thể cho phép overlap nhưng cảnh báo
        }
    }

    @Override
    public void validatePromotionLineConflicts(Long promotionId, PromotionLineCreateRequest request) {
        // Kiểm tra overlap về thời gian với các promotion line khác
        List<PromotionLine> overlappingLines = promotionLineRepository.findOverlappingPromotionLines(
                0L, request.getStartDate(), request.getEndDate());

        if (!overlappingLines.isEmpty()) {
            log.warn("Tìm thấy {} dòng khuyến mãi có thời gian overlap", overlappingLines.size());
            // Business logic có thể cho phép hoặc không cho phép overlap
        }
    }

    // ==================== BUSINESS LOGIC OPERATIONS ====================

    @Override
    @Transactional(readOnly = true)
    public PromotionDiscountResult calculateDiscount(BigDecimal orderAmount, List<OrderItemDTO> orderItems) {
        log.info("Tính toán discount cho đơn hàng với giá trị: {}", orderAmount);

        PromotionDiscountResult result = new PromotionDiscountResult();
        result.setFinalAmount(orderAmount);
        result.setAppliedPromotions(new ArrayList<>());
        result.setGiftItems(new ArrayList<>());

        // Lấy danh sách promotion lines đang hoạt động theo độ ưu tiên
        List<PromotionLine> activePromotionLines = promotionLineRepository
                .findActivePromotionLinesByPriority(LocalDateTime.now());

        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (PromotionLine promotionLine : activePromotionLines) {
            if (promotionLine.getPromotionDetails() != null) {
                for (PromotionDetail detail : promotionLine.getPromotionDetails()) {
                    // Kiểm tra điều kiện áp dụng
                    if (isPromotionApplicable(detail, orderAmount, orderItems)) {
                        BigDecimal discount = calculatePromotionDiscount(detail, orderAmount, orderItems);

                        if (discount.compareTo(BigDecimal.ZERO) > 0) {
                            totalDiscount = totalDiscount.add(discount);

                            // Thêm vào danh sách promotion đã áp dụng
                            PromotionDiscountResult.AppliedPromotionDTO appliedPromotion = new PromotionDiscountResult.AppliedPromotionDTO();
                            appliedPromotion.setPromotionLineId(promotionLine.getLineId());
                            appliedPromotion.setPromotionLineName(promotionLine.getLineCode());
                            appliedPromotion.setPromotionType(promotionLine.getPromotionType().getValue());
                            appliedPromotion.setDiscountAmount(discount);
                            appliedPromotion.setDescription(promotionLine.getDescription());

                            result.getAppliedPromotions().add(appliedPromotion);

                            // Xử lý gift items cho BUY_X_GET_Y
                            if (promotionLine.getPromotionType() == PromotionType.BUY_X_GET_Y) {
                                addGiftItems(result, detail, orderItems);
                            }

                            // Nếu không thể kết hợp, dừng lại
                            if (!promotionLine.getIsCombinable()) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        result.setTotalDiscount(totalDiscount);
        result.setFinalAmount(orderAmount.subtract(totalDiscount));

        log.info("Tổng discount tính được: {}, Giá trị cuối: {}", totalDiscount, result.getFinalAmount());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDiscountResult calculateProductDiscount(Long productUnitId, Integer quantity,
            BigDecimal unitPrice) {
        log.info("Tính toán discount cho sản phẩm ID: {}, số lượng: {}", productUnitId, quantity);

        // Tạo order item để sử dụng logic chung
        OrderItemDTO orderItem = new OrderItemDTO();
        orderItem.setProductUnitId(productUnitId);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);

        // Lấy category của product unit
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new PromotionValidationException("Không tìm thấy product unit"));
        orderItem.setCategoryId(productUnit.getProduct().getCategory().getCategoryId().longValue());

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return calculateDiscount(totalAmount, List.of(orderItem));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionLineDTO> getApplicablePromotionsForProduct(Long productUnitId) {
        List<PromotionDetail> applicableDetails = promotionDetailRepository
                .findApplicablePromotionDetailsForProductUnit(productUnitId);

        return applicableDetails.stream()
                .map(detail -> detail.getPromotionLine())
                .distinct()
                .map(this::convertToPromotionLineDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromotionLineDTO> getApplicablePromotionsForCategory(Long categoryId) {
        List<PromotionDetail> applicableDetails = promotionDetailRepository
                .findApplicablePromotionDetailsForCategory(categoryId);

        return applicableDetails.stream()
                .map(detail -> detail.getPromotionLine())
                .distinct()
                .map(this::convertToPromotionLineDTO)
                .collect(Collectors.toList());
    }

    // ==================== UTILITY OPERATIONS ====================

    @Override
    public PromotionHeaderDTO activatePromotion(Long promotionId) {
        log.info("Kích hoạt chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        // Kiểm tra điều kiện kích hoạt
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getEndDate().isBefore(now)) {
            throw new PromotionValidationException("Không thể kích hoạt chương trình khuyến mãi đã hết hạn");
        }

        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion = promotionHeaderRepository.save(promotion);

        log.info("Đã kích hoạt thành công chương trình khuyến mãi: {}", promotion.getName());
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    public PromotionHeaderDTO pausePromotion(Long promotionId) {
        log.info("Tạm dừng chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        promotion.setStatus(PromotionStatus.PAUSED);
        promotion = promotionHeaderRepository.save(promotion);

        log.info("Đã tạm dừng thành công chương trình khuyến mãi: {}", promotion.getName());
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    public PromotionHeaderDTO expirePromotion(Long promotionId) {
        log.info("Kết thúc chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

        promotion.setStatus(PromotionStatus.EXPIRED);
        promotion = promotionHeaderRepository.save(promotion);

        log.info("Đã kết thúc thành công chương trình khuyến mãi: {}", promotion.getName());
        return convertToPromotionHeaderDTO(promotion);
    }

    @Override
    public List<PromotionHeaderDTO> createBulkPromotions(List<PromotionCreateRequest> requests) {
        log.info("Tạo bulk {} chương trình khuyến mãi", requests.size());

        List<PromotionHeaderDTO> results = new ArrayList<>();

        for (PromotionCreateRequest request : requests) {
            try {
                PromotionHeaderDTO promotion = createPromotion(request);
                results.add(promotion);
            } catch (Exception e) {
                log.error("Lỗi khi tạo promotion: {}", request.getName(), e);
                // Có thể throw exception hoặc tiếp tục tùy business logic
            }
        }

        log.info("Đã tạo thành công {} chương trình khuyến mãi", results.size());
        return results;
    }

    @Override
    public void updateBulkPromotionStatus(List<Long> promotionIds, PromotionStatus status) {
        log.info("Cập nhật trạng thái {} cho {} chương trình khuyến mãi", status, promotionIds.size());

        for (Long promotionId : promotionIds) {
            try {
                PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                        .orElseThrow(() -> PromotionNotFoundException.forPromotionId(promotionId));

                promotion.setStatus(status);
                promotionHeaderRepository.save(promotion);
            } catch (Exception e) {
                log.error("Lỗi khi cập nhật trạng thái promotion ID: {}", promotionId, e);
            }
        }

        log.info("Đã cập nhật trạng thái thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionStatisticsDTO getPromotionStatistics() {
        log.info("Lấy thống kê promotion");

        PromotionStatisticsDTO stats = new PromotionStatisticsDTO();

        // Đếm promotion theo trạng thái
        stats.setTotalPromotions(promotionHeaderRepository.count());
        stats.setActivePromotions(promotionHeaderRepository.countByStatus(PromotionStatus.ACTIVE));
        stats.setPausedPromotions(promotionHeaderRepository.countByStatus(PromotionStatus.PAUSED));
        stats.setUpcomingPromotions(promotionHeaderRepository.countByStatus(PromotionStatus.UPCOMING));
        stats.setExpiredPromotions(promotionHeaderRepository.countByStatus(PromotionStatus.EXPIRED));

        // Đếm promotion lines
        stats.setTotalPromotionLines(promotionLineRepository.count());
        stats.setActivePromotionLines(promotionLineRepository.countByStatus(PromotionStatus.ACTIVE));

        // Đếm promotion details
        stats.setTotalPromotionDetails(promotionDetailRepository.count());

        // Đếm promotion sắp hết hạn
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusWeeks(1);
        LocalDateTime nextMonth = now.plusMonths(1);

        stats.setPromotionsExpiringThisWeek(
                promotionHeaderRepository.findPromotionsExpiringWithin(now, nextWeek).size());
        stats.setPromotionsExpiringThisMonth(
                promotionHeaderRepository.findPromotionsExpiringWithin(now, nextMonth).size());

        // Đếm theo loại promotion
        stats.setBuyXGetYPromotions(promotionLineRepository.findByPromotionType(PromotionType.BUY_X_GET_Y).size());
        stats.setPercentDiscountPromotions(
                promotionLineRepository.findByPromotionType(PromotionType.PERCENT_PRODUCT).size() +
                        promotionLineRepository.findByPromotionType(PromotionType.PERCENT_ORDER).size());
        stats.setFixedDiscountPromotions(
                promotionLineRepository.findByPromotionType(PromotionType.FIXED_PRODUCT).size() +
                        promotionLineRepository.findByPromotionType(PromotionType.FIXED_ORDER).size());

        return stats;
    }

    // ==================== HELPER METHODS ====================

    private boolean isPromotionApplicable(PromotionDetail detail, BigDecimal orderAmount,
            List<OrderItemDTO> orderItems) {
        // Kiểm tra giá trị đơn hàng tối thiểu
        if (detail.getMinOrderValue() != null && orderAmount.compareTo(detail.getMinOrderValue()) < 0) {
            return false;
        }

        // Kiểm tra điều kiện sản phẩm hoặc category
        if (detail.getConditionProductUnit() != null) {
            return orderItems.stream()
                    .anyMatch(item -> item.getProductUnitId().equals(detail.getConditionProductUnit().getId()));
        }

        if (detail.getConditionCategory() != null) {
            return orderItems.stream().anyMatch(
                    item -> item.getCategoryId().equals(detail.getConditionCategory().getCategoryId().longValue()));
        }

        return true; // Áp dụng cho tất cả nếu không có điều kiện cụ thể
    }

    private BigDecimal calculatePromotionDiscount(PromotionDetail detail, BigDecimal orderAmount,
            List<OrderItemDTO> orderItems) {
        PromotionType promotionType = detail.getPromotionLine().getPromotionType();

        switch (promotionType) {
            case PERCENT_ORDER:
                return calculatePercentOrderDiscount(detail, orderAmount);
            case FIXED_ORDER:
                return calculateFixedOrderDiscount(detail, orderAmount);
            case PERCENT_PRODUCT:
                return calculatePercentProductDiscount(detail, orderItems);
            case FIXED_PRODUCT:
                return calculateFixedProductDiscount(detail, orderItems);
            case BUY_X_GET_Y:
                return BigDecimal.ZERO; // BUY_X_GET_Y không có discount tiền mặt
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculatePercentOrderDiscount(PromotionDetail detail, BigDecimal orderAmount) {
        BigDecimal discount = orderAmount.multiply(detail.getValue()).divide(BigDecimal.valueOf(100));

        // Áp dụng giới hạn discount tối đa nếu có
        if (detail.getMaxDiscountValue() != null && discount.compareTo(detail.getMaxDiscountValue()) > 0) {
            discount = detail.getMaxDiscountValue();
        }

        return discount;
    }

    private BigDecimal calculateFixedOrderDiscount(PromotionDetail detail, BigDecimal orderAmount) {
        BigDecimal discount = detail.getValue();

        // Không cho phép discount lớn hơn giá trị đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    private BigDecimal calculatePercentProductDiscount(PromotionDetail detail, List<OrderItemDTO> orderItems) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (OrderItemDTO item : orderItems) {
            if (isItemApplicableForDetail(item, detail)) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                BigDecimal itemDiscount = itemTotal.multiply(detail.getValue()).divide(BigDecimal.valueOf(100));
                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }

        // Áp dụng giới hạn discount tối đa nếu có
        if (detail.getMaxDiscountValue() != null && totalDiscount.compareTo(detail.getMaxDiscountValue()) > 0) {
            totalDiscount = detail.getMaxDiscountValue();
        }

        return totalDiscount;
    }

    private BigDecimal calculateFixedProductDiscount(PromotionDetail detail, List<OrderItemDTO> orderItems) {
        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (OrderItemDTO item : orderItems) {
            if (isItemApplicableForDetail(item, detail)) {
                BigDecimal itemDiscount = detail.getValue().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalDiscount = totalDiscount.add(itemDiscount);
            }
        }

        return totalDiscount;
    }

    private boolean isItemApplicableForDetail(OrderItemDTO item, PromotionDetail detail) {
        if (detail.getConditionProductUnit() != null) {
            return item.getProductUnitId().equals(detail.getConditionProductUnit().getId());
        }

        if (detail.getConditionCategory() != null) {
            return item.getCategoryId().equals(detail.getConditionCategory().getCategoryId().longValue());
        }

        return false;
    }

    private void addGiftItems(PromotionDiscountResult result, PromotionDetail detail, List<OrderItemDTO> orderItems) {
        if (detail.getConditionBuyQuantity() == null || detail.getGiftQuantity() == null ||
                detail.getGiftProductUnit() == null) {
            return;
        }

        // Tính số lượng gift items dựa trên điều kiện mua
        int totalEligibleQuantity = 0;

        for (OrderItemDTO item : orderItems) {
            if (isItemApplicableForDetail(item, detail)) {
                totalEligibleQuantity += item.getQuantity();
            }
        }

        int giftSets = totalEligibleQuantity / detail.getConditionBuyQuantity();
        int totalGiftQuantity = giftSets * detail.getGiftQuantity();

        if (totalGiftQuantity > 0) {
            PromotionDiscountResult.GiftItemDTO giftItem = new PromotionDiscountResult.GiftItemDTO();
            giftItem.setProductUnitId(detail.getGiftProductUnit().getId());
            giftItem.setProductUnitName(detail.getGiftProductUnit().getCode());
            giftItem.setQuantity(totalGiftQuantity);
            giftItem.setPromotionLineName(detail.getPromotionLine().getLineCode());

            result.getGiftItems().add(giftItem);
        }
    }
}
