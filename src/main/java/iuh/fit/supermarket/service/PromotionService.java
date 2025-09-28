package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.entity.PromotionLine;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho quản lý promotion
 */
public interface PromotionService {

    // ==================== PROMOTION HEADER OPERATIONS ====================
    
    /**
     * Tạo chương trình khuyến mãi mới
     */
    PromotionHeaderDTO createPromotion(PromotionCreateRequest request);
    
    /**
     * Cập nhật chương trình khuyến mãi
     */
    PromotionHeaderDTO updatePromotion(Long promotionId, PromotionCreateRequest request);
    
    /**
     * Xóa chương trình khuyến mãi
     */
    void deletePromotion(Long promotionId);
    
    /**
     * Lấy thông tin chương trình khuyến mãi theo ID
     */
    PromotionHeaderDTO getPromotionById(Long promotionId);
    
    /**
     * Lấy danh sách tất cả chương trình khuyến mãi
     */
    Page<PromotionHeaderDTO> getAllPromotions(Pageable pageable);
    
    /**
     * Tìm kiếm chương trình khuyến mãi theo tên
     */
    Page<PromotionHeaderDTO> searchPromotionsByName(String keyword, Pageable pageable);
    
    /**
     * Lấy danh sách chương trình khuyến mãi theo trạng thái
     */
    Page<PromotionHeaderDTO> getPromotionsByStatus(PromotionStatus status, Pageable pageable);
    
    /**
     * Lấy danh sách chương trình khuyến mãi đang hoạt động
     */
    List<PromotionHeaderDTO> getCurrentActivePromotions();
    
    /**
     * Lấy danh sách chương trình khuyến mãi sắp hết hạn
     */
    List<PromotionHeaderDTO> getPromotionsExpiringWithin(int days);

    // ==================== PROMOTION LINE OPERATIONS ====================
    
    /**
     * Tạo dòng khuyến mãi mới
     */
    PromotionLineDTO createPromotionLine(Long promotionId, PromotionLineCreateRequest request);
    
    /**
     * Cập nhật dòng khuyến mãi
     */
    PromotionLineDTO updatePromotionLine(Long lineId, PromotionLineCreateRequest request);
    
    /**
     * Xóa dòng khuyến mãi
     */
    void deletePromotionLine(Long lineId);
    
    /**
     * Lấy thông tin dòng khuyến mãi theo ID
     */
    PromotionLineDTO getPromotionLineById(Long lineId);
    
    /**
     * Lấy danh sách dòng khuyến mãi theo promotion ID
     */
    List<PromotionLineDTO> getPromotionLinesByPromotionId(Long promotionId);
    
    /**
     * Lấy danh sách dòng khuyến mãi theo loại
     */
    List<PromotionLineDTO> getPromotionLinesByType(PromotionType promotionType);

    // ==================== PROMOTION DETAIL OPERATIONS ====================
    
    /**
     * Tạo chi tiết khuyến mãi mới
     */
    PromotionDetailDTO createPromotionDetail(Long lineId, PromotionDetailCreateRequest request);
    
    /**
     * Cập nhật chi tiết khuyến mãi
     */
    PromotionDetailDTO updatePromotionDetail(Long detailId, PromotionDetailCreateRequest request);
    
    /**
     * Xóa chi tiết khuyến mãi
     */
    void deletePromotionDetail(Long detailId);
    
    /**
     * Lấy danh sách chi tiết khuyến mãi theo line ID
     */
    List<PromotionDetailDTO> getPromotionDetailsByLineId(Long lineId);

    // ==================== BUSINESS LOGIC OPERATIONS ====================
    
    /**
     * Tính toán discount cho đơn hàng
     */
    PromotionDiscountResult calculateDiscount(BigDecimal orderAmount, List<OrderItemDTO> orderItems);
    
    /**
     * Tính toán discount cho sản phẩm cụ thể
     */
    PromotionDiscountResult calculateProductDiscount(Long productUnitId, Integer quantity, BigDecimal unitPrice);
    
    /**
     * Lấy danh sách promotion áp dụng cho sản phẩm
     */
    List<PromotionLineDTO> getApplicablePromotionsForProduct(Long productUnitId);
    
    /**
     * Lấy danh sách promotion áp dụng cho category
     */
    List<PromotionLineDTO> getApplicablePromotionsForCategory(Long categoryId);
    
    /**
     * Kiểm tra xung đột promotion
     */
    void validatePromotionConflicts(PromotionCreateRequest request);
    
    /**
     * Kiểm tra xung đột promotion line
     */
    void validatePromotionLineConflicts(Long promotionId, PromotionLineCreateRequest request);

    // ==================== VALIDATION OPERATIONS ====================
    
    /**
     * Validate promotion data
     */
    void validatePromotionData(PromotionCreateRequest request);
    
    /**
     * Validate promotion line data
     */
    void validatePromotionLineData(PromotionLineCreateRequest request);
    
    /**
     * Validate promotion detail data
     */
    void validatePromotionDetailData(PromotionDetailCreateRequest request);

    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Kích hoạt promotion
     */
    PromotionHeaderDTO activatePromotion(Long promotionId);
    
    /**
     * Tạm dừng promotion
     */
    PromotionHeaderDTO pausePromotion(Long promotionId);
    
    /**
     * Kết thúc promotion
     */
    PromotionHeaderDTO expirePromotion(Long promotionId);
    
    /**
     * Bulk operations - tạo nhiều promotion
     */
    List<PromotionHeaderDTO> createBulkPromotions(List<PromotionCreateRequest> requests);
    
    /**
     * Bulk operations - cập nhật trạng thái nhiều promotion
     */
    void updateBulkPromotionStatus(List<Long> promotionIds, PromotionStatus status);
    
    /**
     * Lấy thống kê promotion
     */
    PromotionStatisticsDTO getPromotionStatistics();
    
    /**
     * DTO cho OrderItem
     */
    class OrderItemDTO {
        private Long productUnitId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private Long categoryId;
        
        // Constructors, getters, setters
        public OrderItemDTO() {}
        
        public OrderItemDTO(Long productUnitId, Integer quantity, BigDecimal unitPrice, Long categoryId) {
            this.productUnitId = productUnitId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.categoryId = categoryId;
        }
        
        // Getters and setters
        public Long getProductUnitId() { return productUnitId; }
        public void setProductUnitId(Long productUnitId) { this.productUnitId = productUnitId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    }
}
