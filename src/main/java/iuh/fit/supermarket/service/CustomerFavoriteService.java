package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.favorite.CustomerFavoriteResponse;

import java.util.List;

/**
 * Service để quản lý sản phẩm yêu thích của khách hàng
 */
public interface CustomerFavoriteService {

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return thông báo kết quả
     */
    String addFavorite(Integer customerId, Long productUnitId);

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return thông báo kết quả
     */
    String removeFavorite(Integer customerId, Long productUnitId);

    /**
     * Lấy danh sách sản phẩm yêu thích của khách hàng
     *
     * @param customerId ID khách hàng
     * @return danh sách sản phẩm yêu thích
     */
    List<CustomerFavoriteResponse> getFavorites(Integer customerId);

    /**
     * Kiểm tra sản phẩm đã có trong danh sách yêu thích chưa
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return true nếu đã tồn tại
     */
    boolean isFavorite(Integer customerId, Long productUnitId);

    /**
     * Đếm số lượng sản phẩm yêu thích
     *
     * @param customerId ID khách hàng
     * @return số lượng sản phẩm yêu thích
     */
    Integer countFavorites(Integer customerId);
}
