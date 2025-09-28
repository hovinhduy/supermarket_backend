package iuh.fit.supermarket.util;

import iuh.fit.supermarket.dto.promotion.PromotionCreateRequest;
import iuh.fit.supermarket.dto.promotion.PromotionDetailCreateRequest;
import iuh.fit.supermarket.dto.promotion.PromotionLineCreateRequest;
import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.entity.PromotionLine;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.exception.PromotionValidationException;
import iuh.fit.supermarket.repository.PromotionHeaderRepository;
import iuh.fit.supermarket.repository.PromotionLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Validator cho promotion với các business rules phức tạp
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionValidator {

    private final PromotionHeaderRepository promotionHeaderRepository;
    private final PromotionLineRepository promotionLineRepository;

    /**
     * Validate toàn bộ promotion request
     */
    public void validatePromotionRequest(PromotionCreateRequest request) {
        validatePromotionHeader(request);
        
        if (request.getPromotionLines() != null) {
            for (PromotionLineCreateRequest lineRequest : request.getPromotionLines()) {
                validatePromotionLine(lineRequest);
                
                if (lineRequest.getPromotionDetails() != null) {
                    for (PromotionDetailCreateRequest detailRequest : lineRequest.getPromotionDetails()) {
                        validatePromotionDetail(detailRequest, lineRequest.getPromotionType());
                    }
                }
            }
        }
        
        // Validate business rules giữa các components
        validatePromotionBusinessRules(request);
    }

    /**
     * Validate promotion header
     */
    public void validatePromotionHeader(PromotionCreateRequest request) {
        // Kiểm tra ngày tháng
        validateDateRange(request.getStartDate(), request.getEndDate());
        
        // Kiểm tra trạng thái và ngày bắt đầu
        validateStatusAndStartDate(request.getStatus(), request.getStartDate());
        
        // Kiểm tra tên không trùng lặp
        validatePromotionName(request.getName());
    }

    /**
     * Validate promotion line
     */
    public void validatePromotionLine(PromotionLineCreateRequest request) {
        // Kiểm tra ngày tháng
        validateDateRange(request.getStartDate(), request.getEndDate());
        
        // Kiểm tra các giá trị số
        validateNumericValues(request);
        
        // Kiểm tra loại promotion và các field liên quan
        validatePromotionTypeConsistency(request);
    }

    /**
     * Validate promotion detail
     */
    public void validatePromotionDetail(PromotionDetailCreateRequest request, PromotionType promotionType) {
        // Validate theo loại promotion
        switch (promotionType) {
            case PERCENT_ORDER:
            case PERCENT_PRODUCT:
                validatePercentagePromotion(request);
                break;
            case FIXED_ORDER:
            case FIXED_PRODUCT:
                validateFixedAmountPromotion(request);
                break;
            case BUY_X_GET_Y:
                validateBuyXGetYPromotion(request);
                break;
        }
        
        // Validate điều kiện áp dụng
        validatePromotionConditions(request);
    }

    /**
     * Validate business rules tổng thể
     */
    public void validatePromotionBusinessRules(PromotionCreateRequest request) {
        // Kiểm tra xung đột thời gian
        validateTimeConflicts(request);
        
        // Kiểm tra logic business
        validatePromotionLogic(request);
        
        // Kiểm tra giới hạn hệ thống
        validateSystemLimits(request);
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new PromotionValidationException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        
        if (startDate.isEqual(endDate)) {
            throw new PromotionValidationException("Ngày bắt đầu và ngày kết thúc không được giống nhau");
        }
        
        // Kiểm tra khoảng cách tối thiểu (ví dụ: ít nhất 1 giờ)
        if (startDate.plusHours(1).isAfter(endDate)) {
            throw new PromotionValidationException("Chương trình khuyến mãi phải kéo dài ít nhất 1 giờ");
        }
    }

    private void validateStatusAndStartDate(PromotionStatus status, LocalDateTime startDate) {
        LocalDateTime now = LocalDateTime.now();
        
        switch (status) {
            case ACTIVE:
                if (startDate.isAfter(now.plusMinutes(5))) {
                    throw new PromotionValidationException(
                        "Không thể đặt trạng thái ACTIVE cho chương trình khuyến mãi bắt đầu trong tương lai");
                }
                break;
            case UPCOMING:
                if (startDate.isBefore(now)) {
                    throw new PromotionValidationException(
                        "Chương trình khuyến mãi UPCOMING không thể có ngày bắt đầu trong quá khứ");
                }
                break;
            case EXPIRED:
                throw new PromotionValidationException("Không thể tạo chương trình khuyến mãi với trạng thái EXPIRED");
        }
    }

    private void validatePromotionName(String name) {
        if (promotionHeaderRepository.findByName(name).isPresent()) {
            throw new PromotionValidationException("Tên chương trình khuyến mãi đã tồn tại: " + name);
        }
    }

    private void validateNumericValues(PromotionLineCreateRequest request) {
        if (request.getPriority() < 0 || request.getPriority() > 100) {
            throw new PromotionValidationException("Độ ưu tiên phải trong khoảng 0-100");
        }
        
        if (request.getMaxTotalQuantity() != null && request.getMaxTotalQuantity() <= 0) {
            throw new PromotionValidationException("Số lượng tối đa phải lớn hơn 0");
        }
        
        if (request.getMaxPerCustomer() != null && request.getMaxPerCustomer() <= 0) {
            throw new PromotionValidationException("Số lần sử dụng tối đa cho mỗi khách hàng phải lớn hơn 0");
        }
        
        // Validate logic giữa maxTotalQuantity và maxPerCustomer
        if (request.getMaxTotalQuantity() != null && request.getMaxPerCustomer() != null) {
            if (request.getMaxPerCustomer() > request.getMaxTotalQuantity()) {
                throw new PromotionValidationException(
                    "Số lần sử dụng tối đa cho mỗi khách hàng không được lớn hơn tổng số lượng tối đa");
            }
        }
    }

    private void validatePromotionTypeConsistency(PromotionLineCreateRequest request) {
        // Kiểm tra consistency giữa promotion type và các field khác
        if (request.getPromotionType() == PromotionType.BUY_X_GET_Y) {
            if (request.getPromotionDetails() == null || request.getPromotionDetails().isEmpty()) {
                throw new PromotionValidationException("Promotion type BUY_X_GET_Y phải có ít nhất một detail");
            }
        }
    }

    private void validatePercentagePromotion(PromotionDetailCreateRequest request) {
        if (request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PromotionValidationException("Phần trăm giảm giá phải lớn hơn 0");
        }
        
        if (request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new PromotionValidationException("Phần trăm giảm giá không được lớn hơn 100%");
        }
        
        // Validate max discount value cho percentage promotion
        if (request.getMaxDiscountValue() != null && 
            request.getMaxDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PromotionValidationException("Giá trị giảm giá tối đa phải lớn hơn 0");
        }
    }

    private void validateFixedAmountPromotion(PromotionDetailCreateRequest request) {
        if (request.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PromotionValidationException("Số tiền giảm giá phải lớn hơn 0");
        }
        
        // Validate reasonable amount (ví dụ: không quá 10 triệu)
        if (request.getValue().compareTo(BigDecimal.valueOf(10_000_000)) > 0) {
            throw new PromotionValidationException("Số tiền giảm giá không được lớn hơn 10,000,000 VNĐ");
        }
    }

    private void validateBuyXGetYPromotion(PromotionDetailCreateRequest request) {
        if (request.getConditionBuyQuantity() == null || request.getGiftQuantity() == null) {
            throw new PromotionValidationException(
                "Promotion BUY_X_GET_Y phải có số lượng mua và số lượng tặng");
        }
        
        if (request.getConditionBuyQuantity() <= 0 || request.getGiftQuantity() <= 0) {
            throw new PromotionValidationException(
                "Số lượng mua và số lượng tặng phải lớn hơn 0");
        }
        
        if (request.getGiftProductUnitId() == null) {
            throw new PromotionValidationException("Promotion BUY_X_GET_Y phải có sản phẩm tặng");
        }
        
        // Validate logic: gift quantity không nên lớn hơn buy quantity
        if (request.getGiftQuantity() > request.getConditionBuyQuantity()) {
            log.warn("Gift quantity ({}) lớn hơn buy quantity ({}), cần xem xét lại", 
                request.getGiftQuantity(), request.getConditionBuyQuantity());
        }
    }

    private void validatePromotionConditions(PromotionDetailCreateRequest request) {
        // Phải có ít nhất một điều kiện áp dụng
        if (request.getConditionProductUnitId() == null && request.getConditionCategoryId() == null) {
            throw new PromotionValidationException(
                "Phải có ít nhất một điều kiện áp dụng (sản phẩm hoặc danh mục)");
        }
        
        // Không được có cả product unit và category cùng lúc
        if (request.getConditionProductUnitId() != null && request.getConditionCategoryId() != null) {
            throw new PromotionValidationException(
                "Không thể áp dụng đồng thời cho cả sản phẩm cụ thể và danh mục");
        }
        
        // Validate min order value
        if (request.getMinOrderValue() != null && 
            request.getMinOrderValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new PromotionValidationException("Giá trị đơn hàng tối thiểu không được âm");
        }
    }

    private void validateTimeConflicts(PromotionCreateRequest request) {
        // Kiểm tra xung đột với các promotion khác
        List<PromotionHeader> overlappingPromotions = promotionHeaderRepository.findOverlappingPromotions(
                0L, request.getStartDate(), request.getEndDate());
        
        // Cảnh báo nếu có overlap nhưng không block (tùy business logic)
        if (!overlappingPromotions.isEmpty()) {
            log.warn("Tìm thấy {} chương trình khuyến mãi có thời gian overlap với promotion mới", 
                overlappingPromotions.size());
        }
    }

    private void validatePromotionLogic(PromotionCreateRequest request) {
        if (request.getPromotionLines() != null) {
            // Kiểm tra không có quá nhiều promotion lines
            if (request.getPromotionLines().size() > 10) {
                throw new PromotionValidationException(
                    "Một chương trình khuyến mãi không được có quá 10 dòng khuyến mãi");
            }
            
            // Kiểm tra priority không trùng lặp
            long distinctPriorities = request.getPromotionLines().stream()
                    .mapToInt(PromotionLineCreateRequest::getPriority)
                    .distinct()
                    .count();
            
            if (distinctPriorities != request.getPromotionLines().size()) {
                throw new PromotionValidationException(
                    "Các dòng khuyến mãi không được có cùng độ ưu tiên");
            }
        }
    }

    private void validateSystemLimits(PromotionCreateRequest request) {
        // Kiểm tra giới hạn hệ thống
        long totalActivePromotions = promotionHeaderRepository.countByStatus(PromotionStatus.ACTIVE);
        
        if (totalActivePromotions >= 100) { // Giới hạn 100 promotion active cùng lúc
            throw new PromotionValidationException(
                "Hệ thống không thể có quá 100 chương trình khuyến mãi hoạt động cùng lúc");
        }
        
        // Kiểm tra thời gian tối đa (ví dụ: không quá 1 năm)
        if (request.getStartDate().plusYears(1).isBefore(request.getEndDate())) {
            throw new PromotionValidationException(
                "Chương trình khuyến mãi không được kéo dài quá 1 năm");
        }
    }

    /**
     * Validate khi update promotion
     */
    public void validatePromotionUpdate(Long promotionId, PromotionCreateRequest request) {
        // Validate cơ bản
        validatePromotionRequest(request);
        
        // Validate business rules đặc biệt cho update
        validateUpdateBusinessRules(promotionId, request);
    }

    private void validateUpdateBusinessRules(Long promotionId, PromotionCreateRequest request) {
        // Lấy promotion hiện tại
        PromotionHeader currentPromotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionValidationException("Không tìm thấy promotion để update"));
        
        // Không cho phép thay đổi ngày bắt đầu nếu promotion đã active
        if (currentPromotion.getStatus() == PromotionStatus.ACTIVE) {
            if (!currentPromotion.getStartDate().equals(request.getStartDate())) {
                throw new PromotionValidationException(
                    "Không thể thay đổi ngày bắt đầu của chương trình khuyến mãi đang hoạt động");
            }
        }
        
        // Không cho phép thay đổi ngày kết thúc thành quá khứ
        if (request.getEndDate().isBefore(LocalDateTime.now())) {
            throw new PromotionValidationException(
                "Không thể đặt ngày kết thúc trong quá khứ");
        }
    }
}
