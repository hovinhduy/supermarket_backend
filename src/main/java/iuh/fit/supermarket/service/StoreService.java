package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.store.StoreDTO;

import java.util.List;

/**
 * Service interface cho quản lý cửa hàng
 */
public interface StoreService {

    /**
     * Lấy danh sách tất cả cửa hàng đang hoạt động
     */
    List<StoreDTO> getActiveStores();

    /**
     * Lấy thông tin cửa hàng theo ID
     */
    StoreDTO getStoreById(Long storeId);
}
