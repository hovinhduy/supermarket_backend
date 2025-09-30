package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.entity.Category;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.PromotionDetail;
import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.exception.DuplicatePromotionException;
import iuh.fit.supermarket.exception.PromotionNotFoundException;
import iuh.fit.supermarket.exception.PromotionValidationException;
import iuh.fit.supermarket.repository.CategoryRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.repository.PromotionDetailRepository;
import iuh.fit.supermarket.repository.PromotionHeaderRepository;
import iuh.fit.supermarket.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class PromotionServiceImpl implements PromotionService {

    private final PromotionHeaderRepository promotionHeaderRepository;
    private final PromotionDetailRepository promotionDetailRepository;
    private final ProductUnitRepository productUnitRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Tạo mới chương trình khuyến mãi
     */
    @Override
    @Transactional
    public PromotionHeaderDTO createPromotion(PromotionCreateRequest request) {
        log.info("Tạo mới chương trình khuyến mãi: {}", request.getPromotionCode());

        // Kiểm tra mã khuyến mãi đã tồn tại chưa
        if (promotionHeaderRepository.existsByPromotionCode(request.getPromotionCode())) {
            throw new DuplicatePromotionException(
                    "Mã khuyến mãi '" + request.getPromotionCode() + "' đã tồn tại");
        }

        // Validate ngày bắt đầu và ngày kết thúc
        validatePromotionDates(request.getStartDate(), request.getEndDate());

        // Tạo PromotionHeader
        PromotionHeader promotion = new PromotionHeader();
        promotion.setPromotionCode(request.getPromotionCode());
        promotion.setPromotionName(request.getPromotionName());
        promotion.setPromotionType(request.getPromotionType());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setStatus(request.getStatus());
        promotion.setMaxUsagePerCustomer(request.getMaxUsagePerCustomer());
        promotion.setMaxUsageTotal(request.getMaxUsageTotal());
        promotion.setCurrentUsageCount(0);

        // Lưu promotion header trước
        PromotionHeader savedPromotion = promotionHeaderRepository.save(promotion);

        // Tạo chi tiết khuyến mãi
        List<PromotionDetail> details = new ArrayList<>();
        for (PromotionDetailCreateRequest detailRequest : request.getPromotionDetails()) {
            PromotionDetail detail = createPromotionDetail(savedPromotion, detailRequest);
            details.add(detail);
        }

        savedPromotion.setPromotionDetails(details);
        promotionDetailRepository.saveAll(details);

        log.info("Đã tạo chương trình khuyến mãi thành công: {}", savedPromotion.getPromotionId());
        return convertToDTO(savedPromotion);
    }

    /**
     * Cập nhật chương trình khuyến mãi
     */
    @Override
    @Transactional
    public PromotionHeaderDTO updatePromotion(Long promotionId, PromotionUpdateRequest request) {
        log.info("Cập nhật chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        // Cập nhật các trường nếu có giá trị mới
        if (request.getPromotionName() != null) {
            promotion.setPromotionName(request.getPromotionName());
        }
        if (request.getPromotionType() != null) {
            promotion.setPromotionType(request.getPromotionType());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null && request.getEndDate() != null) {
            validatePromotionDates(request.getStartDate(), request.getEndDate());
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            promotion.setStatus(request.getStatus());
        }
        if (request.getMaxUsagePerCustomer() != null) {
            promotion.setMaxUsagePerCustomer(request.getMaxUsagePerCustomer());
        }
        if (request.getMaxUsageTotal() != null) {
            promotion.setMaxUsageTotal(request.getMaxUsageTotal());
        }

        // Cập nhật chi tiết khuyến mãi nếu có
        if (request.getPromotionDetails() != null && !request.getPromotionDetails().isEmpty()) {
            // Xóa các chi tiết cũ
            List<PromotionDetail> oldDetails = promotionDetailRepository.findByPromotionId(promotionId);
            promotionDetailRepository.deleteAll(oldDetails);

            // Tạo chi tiết mới
            List<PromotionDetail> newDetails = new ArrayList<>();
            for (PromotionDetailCreateRequest detailRequest : request.getPromotionDetails()) {
                PromotionDetail detail = createPromotionDetail(promotion, detailRequest);
                newDetails.add(detail);
            }
            promotionDetailRepository.saveAll(newDetails);
            promotion.setPromotionDetails(newDetails);
        }

        PromotionHeader updatedPromotion = promotionHeaderRepository.save(promotion);
        log.info("Đã cập nhật chương trình khuyến mãi thành công: {}", promotionId);

        return convertToDTO(updatedPromotion);
    }

    /**
     * Xóa chương trình khuyến mãi
     */
    @Override
    @Transactional
    public void deletePromotion(Long promotionId) {
        log.info("Xóa chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        // Xóa các chi tiết trước
        List<PromotionDetail> details = promotionDetailRepository.findByPromotionId(promotionId);
        promotionDetailRepository.deleteAll(details);

        // Xóa promotion header
        promotionHeaderRepository.delete(promotion);
        log.info("Đã xóa chương trình khuyến mãi thành công: {}", promotionId);
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi
     */
    @Override
    @Transactional(readOnly = true)
    public PromotionHeaderDTO getPromotionById(Long promotionId) {
        log.info("Lấy thông tin chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findByIdWithDetails(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        return convertToDTO(promotion);
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi theo mã
     */
    @Override
    @Transactional(readOnly = true)
    public PromotionHeaderDTO getPromotionByCode(String promotionCode) {
        log.info("Lấy thông tin chương trình khuyến mãi theo mã: {}", promotionCode);

        PromotionHeader promotion = promotionHeaderRepository.findByPromotionCode(promotionCode)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với mã: " + promotionCode));

        return convertToDTO(promotion);
    }

    /**
     * Lấy tất cả chương trình khuyến mãi với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> getAllPromotions(Integer page, Integer size,
            String sortBy, String sortDirection) {
        log.info("Lấy danh sách tất cả chương trình khuyến mãi - Page: {}, Size: {}", page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PromotionHeader> promotions = promotionHeaderRepository.findAll(pageable);
        return promotions.map(this::convertToDTO);
    }

    /**
     * Tìm kiếm chương trình khuyến mãi theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> searchPromotions(String keyword, Integer page, Integer size) {
        log.info("Tìm kiếm chương trình khuyến mãi với từ khóa: {}", keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PromotionHeader> promotions = promotionHeaderRepository.searchByKeyword(keyword, pageable);
        return promotions.map(this::convertToDTO);
    }

    /**
     * Tìm kiếm chương trình khuyến mãi theo nhiều tiêu chí
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> searchPromotionsAdvanced(PromotionSearchRequest searchRequest) {
        log.info("Tìm kiếm nâng cao chương trình khuyến mãi");

        Sort.Direction direction = searchRequest.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                Sort.by(direction, searchRequest.getSortBy()));

        Page<PromotionHeader> promotions = promotionHeaderRepository.searchPromotions(
                searchRequest.getKeyword(),
                searchRequest.getPromotionType(),
                searchRequest.getStatus(),
                searchRequest.getStartDateFrom(),
                searchRequest.getStartDateTo(),
                searchRequest.getEndDateFrom(),
                searchRequest.getEndDateTo(),
                pageable);

        return promotions.map(this::convertToDTO);
    }

    /**
     * Lấy danh sách khuyến mãi theo trạng thái
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> getPromotionsByStatus(PromotionStatus status,
            Integer page, Integer size) {
        log.info("Lấy danh sách khuyến mãi theo trạng thái: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PromotionHeader> promotions = promotionHeaderRepository.findByStatus(status, pageable);
        return promotions.map(this::convertToDTO);
    }

    /**
     * Lấy danh sách khuyến mãi theo loại
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PromotionHeaderDTO> getPromotionsByType(PromotionType promotionType,
            Integer page, Integer size) {
        log.info("Lấy danh sách khuyến mãi theo loại: {}", promotionType);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PromotionHeader> promotions = promotionHeaderRepository.findByPromotionType(promotionType, pageable);
        return promotions.map(this::convertToDTO);
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<PromotionHeaderDTO> getActivePromotions() {
        log.info("Lấy danh sách khuyến mãi đang hoạt động");

        List<PromotionHeader> promotions = promotionHeaderRepository.findActivePromotions(
                LocalDateTime.now(), PromotionStatus.ACTIVE);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách khuyến mãi sắp hết hạn
     */
    @Override
    @Transactional(readOnly = true)
    public List<PromotionHeaderDTO> getExpiringPromotions(int days) {
        log.info("Lấy danh sách khuyến mãi sắp hết hạn trong {} ngày", days);

        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime expirationDate = currentDate.plusDays(days);

        List<PromotionHeader> promotions = promotionHeaderRepository.findExpiringPromotions(
                currentDate, expirationDate, PromotionStatus.ACTIVE);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái chương trình khuyến mãi
     */
    @Override
    @Transactional
    public PromotionHeaderDTO updatePromotionStatus(Long promotionId, PromotionStatus status) {
        log.info("Cập nhật trạng thái chương trình khuyến mãi ID {} sang {}", promotionId, status);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        promotion.setStatus(status);
        PromotionHeader updatedPromotion = promotionHeaderRepository.save(promotion);

        log.info("Đã cập nhật trạng thái chương trình khuyến mãi thành công");
        return convertToDTO(updatedPromotion);
    }

    /**
     * Tăng số lần sử dụng khuyến mãi
     */
    @Override
    @Transactional
    public void incrementUsageCount(Long promotionId) {
        log.info("Tăng số lần sử dụng khuyến mãi ID: {}", promotionId);

        PromotionHeader promotion = promotionHeaderRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(
                        "Không tìm thấy chương trình khuyến mãi với ID: " + promotionId));

        promotion.setCurrentUsageCount(promotion.getCurrentUsageCount() + 1);
        promotionHeaderRepository.save(promotion);
    }

    /**
     * Kiểm tra xem mã khuyến mãi đã tồn tại chưa
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isPromotionCodeExists(String promotionCode) {
        return promotionHeaderRepository.existsByPromotionCode(promotionCode);
    }

    /**
     * Đếm số lượng khuyến mãi theo trạng thái
     */
    @Override
    @Transactional(readOnly = true)
    public long countByStatus(PromotionStatus status) {
        return promotionHeaderRepository.countByStatus(status);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Validate ngày bắt đầu và ngày kết thúc
     */
    private void validatePromotionDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (endDate.isBefore(startDate)) {
            throw new PromotionValidationException(
                    "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    /**
     * Tạo chi tiết khuyến mãi từ request
     */
    private PromotionDetail createPromotionDetail(PromotionHeader promotion,
            PromotionDetailCreateRequest request) {
        PromotionDetail detail = new PromotionDetail();
        detail.setPromotion(promotion);

        // Validate và set dữ liệu theo loại khuyến mãi
        switch (promotion.getPromotionType()) {
            case BUY_X_GET_Y:
                validateAndSetBuyXGetYDetails(detail, request);
                break;
            case ORDER_DISCOUNT:
                validateAndSetOrderDiscountDetails(detail, request);
                break;
            case PRODUCT_DISCOUNT:
                validateAndSetProductDiscountDetails(detail, request);
                break;
            default:
                throw new PromotionValidationException("Loại khuyến mãi không hợp lệ");
        }

        return detail;
    }

    /**
     * Validate và set chi tiết cho khuyến mãi MUA X TẶNG Y
     */
    private void validateAndSetBuyXGetYDetails(PromotionDetail detail,
            PromotionDetailCreateRequest request) {
        // Validate: Phải có sản phẩm mua hoặc danh mục mua
        if (request.getBuyProductId() == null && request.getBuyCategoryId() == null) {
            throw new PromotionValidationException(
                    "Phải chỉ định sản phẩm hoặc danh mục phải mua cho khuyến mãi Mua X Tặng Y");
        }

        // Set sản phẩm mua
        if (request.getBuyProductId() != null) {
            ProductUnit buyProduct = productUnitRepository.findById(request.getBuyProductId())
                    .orElseThrow(() -> new PromotionValidationException(
                            "Không tìm thấy sản phẩm mua với ID: " + request.getBuyProductId()));
            detail.setBuyProduct(buyProduct);
        }

        // Set danh mục mua
        if (request.getBuyCategoryId() != null) {
            Category buyCategory = categoryRepository.findById(request.getBuyCategoryId())
                    .orElseThrow(() -> new PromotionValidationException(
                            "Không tìm thấy danh mục mua với ID: " + request.getBuyCategoryId()));
            detail.setBuyCategory(buyCategory);
        }

        detail.setBuyMinQuantity(request.getBuyMinQuantity());
        detail.setBuyMinValue(request.getBuyMinValue());

        // Validate và set sản phẩm tặng
        if (request.getGiftProductId() == null) {
            throw new PromotionValidationException(
                    "Phải chỉ định sản phẩm được tặng cho khuyến mãi Mua X Tặng Y");
        }

        ProductUnit giftProduct = productUnitRepository.findById(request.getGiftProductId())
                .orElseThrow(() -> new PromotionValidationException(
                        "Không tìm thấy sản phẩm tặng với ID: " + request.getGiftProductId()));
        detail.setGiftProduct(giftProduct);
        detail.setGiftDiscountType(request.getGiftDiscountType());
        detail.setGiftDiscountValue(request.getGiftDiscountValue());
        detail.setGiftMaxQuantity(request.getGiftMaxQuantity());
    }

    /**
     * Validate và set chi tiết cho khuyến mãi GIẢM GIÁ ĐƠN HÀNG
     */
    private void validateAndSetOrderDiscountDetails(PromotionDetail detail,
            PromotionDetailCreateRequest request) {
        if (request.getOrderDiscountType() == null || request.getOrderDiscountValue() == null) {
            throw new PromotionValidationException(
                    "Phải chỉ định loại và giá trị giảm giá cho khuyến mãi Giảm giá đơn hàng");
        }

        detail.setOrderDiscountType(request.getOrderDiscountType());
        detail.setOrderDiscountValue(request.getOrderDiscountValue());
        detail.setOrderDiscountMaxValue(request.getOrderDiscountMaxValue());
        detail.setOrderMinTotalValue(request.getOrderMinTotalValue());
        detail.setOrderMinTotalQuantity(request.getOrderMinTotalQuantity());
    }

    /**
     * Validate và set chi tiết cho khuyến mãi GIẢM GIÁ SẢN PHẨM
     */
    private void validateAndSetProductDiscountDetails(PromotionDetail detail,
            PromotionDetailCreateRequest request) {
        if (request.getProductDiscountType() == null || request.getProductDiscountValue() == null) {
            throw new PromotionValidationException(
                    "Phải chỉ định loại và giá trị giảm giá cho khuyến mãi Giảm giá sản phẩm");
        }

        if (request.getApplyToType() == null) {
            throw new PromotionValidationException(
                    "Phải chỉ định phạm vi áp dụng cho khuyến mãi Giảm giá sản phẩm");
        }

        detail.setProductDiscountType(request.getProductDiscountType());
        detail.setProductDiscountValue(request.getProductDiscountValue());
        detail.setApplyToType(request.getApplyToType());

        // Validate và set sản phẩm áp dụng nếu có
        if (request.getApplyToProductId() != null) {
            ProductUnit applyToProduct = productUnitRepository.findById(request.getApplyToProductId())
                    .orElseThrow(() -> new PromotionValidationException(
                            "Không tìm thấy sản phẩm áp dụng với ID: " + request.getApplyToProductId()));
            detail.setApplyToProduct(applyToProduct);
        }

        // Validate và set danh mục áp dụng nếu có
        if (request.getApplyToCategoryId() != null) {
            Category applyToCategory = categoryRepository.findById(request.getApplyToCategoryId())
                    .orElseThrow(() -> new PromotionValidationException(
                            "Không tìm thấy danh mục áp dụng với ID: " + request.getApplyToCategoryId()));
            detail.setApplyToCategory(applyToCategory);
        }

        detail.setProductMinOrderValue(request.getProductMinOrderValue());
        detail.setProductMinPromotionValue(request.getProductMinPromotionValue());
        detail.setProductMinPromotionQuantity(request.getProductMinPromotionQuantity());
    }

    /**
     * Chuyển đổi từ Entity sang DTO
     */
    private PromotionHeaderDTO convertToDTO(PromotionHeader promotion) {
        PromotionHeaderDTO dto = PromotionHeaderDTO.builder()
                .promotionId(promotion.getPromotionId())
                .promotionCode(promotion.getPromotionCode())
                .promotionName(promotion.getPromotionName())
                .promotionType(promotion.getPromotionType())
                .description(promotion.getDescription())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .status(promotion.getStatus())
                .maxUsagePerCustomer(promotion.getMaxUsagePerCustomer())
                .maxUsageTotal(promotion.getMaxUsageTotal())
                .currentUsageCount(promotion.getCurrentUsageCount())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();

        // Convert chi tiết nếu có
        if (promotion.getPromotionDetails() != null && !promotion.getPromotionDetails().isEmpty()) {
            List<PromotionDetailDTO> detailDTOs = promotion.getPromotionDetails().stream()
                    .map(this::convertDetailToDTO)
                    .collect(Collectors.toList());
            dto.setPromotionDetails(detailDTOs);
        }

        return dto;
    }

    /**
     * Chuyển đổi từ PromotionDetail Entity sang DTO
     */
    private PromotionDetailDTO convertDetailToDTO(PromotionDetail detail) {
        return PromotionDetailDTO.builder()
                .detailId(detail.getDetailId())
                .promotionId(detail.getPromotion().getPromotionId())
                // BUY_X_GET_Y fields
                .buyProductId(detail.getBuyProduct() != null ? detail.getBuyProduct().getId() : null)
                .buyCategoryId(detail.getBuyCategory() != null ? detail.getBuyCategory().getCategoryId() : null)
                .buyMinQuantity(detail.getBuyMinQuantity())
                .buyMinValue(detail.getBuyMinValue())
                .giftProductId(detail.getGiftProduct() != null ? detail.getGiftProduct().getId() : null)
                .giftDiscountType(detail.getGiftDiscountType())
                .giftDiscountValue(detail.getGiftDiscountValue())
                .giftMaxQuantity(detail.getGiftMaxQuantity())
                // ORDER_DISCOUNT fields
                .orderDiscountType(detail.getOrderDiscountType())
                .orderDiscountValue(detail.getOrderDiscountValue())
                .orderDiscountMaxValue(detail.getOrderDiscountMaxValue())
                .orderMinTotalValue(detail.getOrderMinTotalValue())
                .orderMinTotalQuantity(detail.getOrderMinTotalQuantity())
                // PRODUCT_DISCOUNT fields
                .productDiscountType(detail.getProductDiscountType())
                .productDiscountValue(detail.getProductDiscountValue())
                .applyToType(detail.getApplyToType())
                .applyToProductId(detail.getApplyToProduct() != null ? detail.getApplyToProduct().getId() : null)
                .applyToCategoryId(
                        detail.getApplyToCategory() != null ? detail.getApplyToCategory().getCategoryId() : null)
                .productMinOrderValue(detail.getProductMinOrderValue())
                .productMinPromotionValue(detail.getProductMinPromotionValue())
                .productMinPromotionQuantity(detail.getProductMinPromotionQuantity())
                .build();
    }
}

