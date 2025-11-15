package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.chat.structured.PromotionInfo;

import java.util.List;

/**
 * Service để tìm kiếm và lấy thông tin khuyến mãi cho chatbot
 */
public interface PromotionSearchService {
    
    /**
     * Lấy danh sách khuyến mãi đang hoạt động trong cửa hàng
     * 
     * @param limit số lượng khuyến mãi tối đa (mặc định 10)
     * @return danh sách thông tin khuyến mãi
     */
    List<PromotionInfo> getActivePromotions(Integer limit);
}
