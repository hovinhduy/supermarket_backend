package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface cho quản lý khuyến mãi
 */
public interface PromotionService {

    /**
     * Tạo mới chương trình khuyến mãi
     * 
     * @param request thông tin chương trình khuyến mãi cần tạo
     * @return PromotionHeaderDTO đã được tạo
     */
    PromotionHeaderDTO createPromotion(PromotionCreateRequest request);

    /**
     * Cập nhật chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     * @param request     thông tin cập nhật
     * @return PromotionHeaderDTO đã được cập nhật
     */
    PromotionHeaderDTO updatePromotion(Long promotionId, PromotionUpdateRequest request);

    /**
     * Xóa chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     */
    void deletePromotion(Long promotionId);

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     * @return PromotionHeaderDTO
     */
    PromotionHeaderDTO getPromotionById(Long promotionId);

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi theo mã
     * 
     * @param promotionCode mã chương trình khuyến mãi
     * @return PromotionHeaderDTO
     */
    PromotionHeaderDTO getPromotionByCode(String promotionCode);

    /**
     * Lấy tất cả chương trình khuyến mãi với phân trang
     * 
     * @param page          số trang (bắt đầu từ 0)
     * @param size          số lượng bản ghi mỗi trang
     * @param sortBy        trường sắp xếp
     * @param sortDirection hướng sắp xếp (ASC hoặc DESC)
     * @return Page chứa danh sách PromotionHeaderDTO
     */
    Page<PromotionHeaderDTO> getAllPromotions(Integer page, Integer size, String sortBy, String sortDirection);

    /**
     * Tìm kiếm chương trình khuyến mãi theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @param page    số trang
     * @param size    số lượng bản ghi mỗi trang
     * @return Page chứa danh sách PromotionHeaderDTO
     */
    Page<PromotionHeaderDTO> searchPromotions(String keyword, Integer page, Integer size);

    /**
     * Tìm kiếm chương trình khuyến mãi theo nhiều tiêu chí
     * 
     * @param searchRequest tiêu chí tìm kiếm
     * @return Page chứa danh sách PromotionHeaderDTO
     */
    Page<PromotionHeaderDTO> searchPromotionsAdvanced(PromotionSearchRequest searchRequest);

    /**
     * Lấy danh sách khuyến mãi theo trạng thái
     * 
     * @param status trạng thái khuyến mãi
     * @param page   số trang
     * @param size   số lượng bản ghi mỗi trang
     * @return Page chứa danh sách PromotionHeaderDTO
     */
    Page<PromotionHeaderDTO> getPromotionsByStatus(PromotionStatus status, Integer page, Integer size);

    /**
     * Lấy danh sách khuyến mãi theo loại
     * 
     * @param promotionType loại khuyến mãi
     * @param page          số trang
     * @param size          số lượng bản ghi mỗi trang
     * @return Page chứa danh sách PromotionHeaderDTO
     */
    Page<PromotionHeaderDTO> getPromotionsByType(PromotionType promotionType, Integer page, Integer size);

    /**
     * Lấy danh sách khuyến mãi đang hoạt động
     * 
     * @return danh sách PromotionHeaderDTO
     */
    List<PromotionHeaderDTO> getActivePromotions();

    /**
     * Lấy danh sách khuyến mãi sắp hết hạn (trong vòng N ngày)
     * 
     * @param days số ngày
     * @return danh sách PromotionHeaderDTO
     */
    List<PromotionHeaderDTO> getExpiringPromotions(int days);

    /**
     * Cập nhật trạng thái chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     * @param status      trạng thái mới
     * @return PromotionHeaderDTO đã được cập nhật
     */
    PromotionHeaderDTO updatePromotionStatus(Long promotionId, PromotionStatus status);

    /**
     * Tăng số lần sử dụng khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     */
    void incrementUsageCount(Long promotionId);

    /**
     * Kiểm tra xem mã khuyến mãi đã tồn tại chưa
     * 
     * @param promotionCode mã khuyến mãi
     * @return true nếu đã tồn tại, ngược lại false
     */
    boolean isPromotionCodeExists(String promotionCode);

    /**
     * Đếm số lượng khuyến mãi theo trạng thái
     * 
     * @param status trạng thái
     * @return số lượng khuyến mãi
     */
    long countByStatus(PromotionStatus status);
}

