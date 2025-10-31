package iuh.fit.supermarket.service;

/**
 * Service interface cho tra cứu khuyến mãi (dành cho AI chat)
 */
public interface PromotionLookupService {

    /**
     * Lấy danh sách khuyến mãi đang có
     * 
     * @param limit số lượng khuyến mãi cần lấy
     * @return thông tin khuyến mãi dạng text cho AI
     */
    String getActivePromotions(int limit);

    /**
     * Tìm kiếm khuyến mãi theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @return thông tin khuyến mãi phù hợp
     */
    String searchPromotions(String keyword);
}
