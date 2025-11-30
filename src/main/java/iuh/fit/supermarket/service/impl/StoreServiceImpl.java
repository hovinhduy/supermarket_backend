package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.store.StoreDTO;
import iuh.fit.supermarket.entity.Store;
import iuh.fit.supermarket.exception.NotFoundException;
import iuh.fit.supermarket.repository.StoreRepository;
import iuh.fit.supermarket.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation của StoreService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    /**
     * Lấy danh sách tất cả cửa hàng đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<StoreDTO> getActiveStores() {
        log.info("Lấy danh sách cửa hàng đang hoạt động");
        return storeRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Lấy thông tin cửa hàng theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public StoreDTO getStoreById(Long storeId) {
        log.info("Lấy thông tin cửa hàng ID: {}", storeId);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy cửa hàng với ID: " + storeId));
        return toDTO(store);
    }

    /**
     * Chuyển đổi entity sang DTO
     */
    private StoreDTO toDTO(Store store) {
        return new StoreDTO(
                store.getStoreId(),
                store.getStoreCode(),
                store.getStoreName(),
                store.getAddress(),
                store.getPhone(),
                store.getOpeningTime(),
                store.getClosingTime(),
                store.getIsActive()
        );
    }
}
