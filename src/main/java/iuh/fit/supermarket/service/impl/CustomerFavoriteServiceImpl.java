package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.favorite.CustomerFavoriteResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.CustomerFavorite;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.repository.CustomerFavoriteRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.service.CustomerFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation của CustomerFavoriteService
 * Quản lý sản phẩm yêu thích của khách hàng
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerFavoriteServiceImpl implements CustomerFavoriteService {

    private final CustomerFavoriteRepository customerFavoriteRepository;
    private final CustomerRepository customerRepository;
    private final ProductUnitRepository productUnitRepository;

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     * Kiểm tra tồn tại của customer và productUnit trước khi thêm
     */
    @Override
    @Transactional
    public String addFavorite(Integer customerId, Long productUnitId) {
        log.info("Thêm sản phẩm yêu thích - Customer: {}, ProductUnit: {}", customerId, productUnitId);

        // Validate input
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }
        if (productUnitId == null || productUnitId <= 0) {
            throw new IllegalArgumentException("ID đơn vị sản phẩm không hợp lệ");
        }

        // Kiểm tra xem đã tồn tại chưa
        if (customerFavoriteRepository.existsByCustomerIdAndProductUnitId(customerId, productUnitId)) {
            log.warn("Sản phẩm đã có trong danh sách yêu thích - Customer: {}, ProductUnit: {}",
                    customerId, productUnitId);
            return "Sản phẩm đã có trong danh sách yêu thích";
        }

        // Lấy thông tin customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId));

        // Lấy thông tin productUnit
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị sản phẩm với ID: " + productUnitId));

        // Kiểm tra productUnit có active không
        if (productUnit.getIsDeleted() || !productUnit.getIsActive()) {
            throw new IllegalArgumentException("Sản phẩm không còn khả dụng");
        }

        // Tạo mới CustomerFavorite
        CustomerFavorite favorite = new CustomerFavorite();
        favorite.setCustomer(customer);
        favorite.setProductUnit(productUnit);
        favorite.setCreatedAt(LocalDateTime.now());
        favorite.setUpdatedAt(LocalDateTime.now());

        // Lưu vào database
        customerFavoriteRepository.save(favorite);
        log.info("Đã thêm sản phẩm vào danh sách yêu thích - Customer: {}, ProductUnit: {}",
                customerId, productUnitId);

        return "Đã thêm sản phẩm vào danh sách yêu thích";
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     */
    @Override
    @Transactional
    public String removeFavorite(Integer customerId, Long productUnitId) {
        log.info("Xóa sản phẩm yêu thích - Customer: {}, ProductUnit: {}", customerId, productUnitId);

        // Validate input
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }
        if (productUnitId == null || productUnitId <= 0) {
            throw new IllegalArgumentException("ID đơn vị sản phẩm không hợp lệ");
        }

        // Kiểm tra xem có tồn tại không
        if (!customerFavoriteRepository.existsByCustomerIdAndProductUnitId(customerId, productUnitId)) {
            log.warn("Sản phẩm không có trong danh sách yêu thích - Customer: {}, ProductUnit: {}",
                    customerId, productUnitId);
            throw new IllegalArgumentException("Sản phẩm không có trong danh sách yêu thích");
        }

        // Xóa khỏi database
        customerFavoriteRepository.deleteByCustomerIdAndProductUnitId(customerId, productUnitId);
        log.info("Đã xóa sản phẩm khỏi danh sách yêu thích - Customer: {}, ProductUnit: {}",
                customerId, productUnitId);

        return "Đã xóa sản phẩm khỏi danh sách yêu thích";
    }

    /**
     * Lấy danh sách sản phẩm yêu thích của khách hàng
     * Trả về thông tin chi tiết bao gồm tên, giá, hình ảnh
     */
    @Override
    @Transactional(readOnly = true)
    public List<CustomerFavoriteResponse> getFavorites(Integer customerId) {
        log.info("Lấy danh sách sản phẩm yêu thích - Customer: {}", customerId);

        // Validate input
        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("ID khách hàng không hợp lệ");
        }

        // Kiểm tra customer có tồn tại không
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Không tìm thấy khách hàng với ID: " + customerId);
        }

        // Lấy danh sách yêu thích
        List<CustomerFavoriteResponse> favorites = customerFavoriteRepository.findFavoritesByCustomerId(customerId);
        log.info("Tìm thấy {} sản phẩm yêu thích cho khách hàng {}", favorites.size(), customerId);

        return favorites;
    }

    /**
     * Kiểm tra sản phẩm đã có trong danh sách yêu thích chưa
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Integer customerId, Long productUnitId) {
        log.debug("Kiểm tra sản phẩm yêu thích - Customer: {}, ProductUnit: {}", customerId, productUnitId);

        // Validate input
        if (customerId == null || customerId <= 0 || productUnitId == null || productUnitId <= 0) {
            return false;
        }

        return customerFavoriteRepository.existsByCustomerIdAndProductUnitId(customerId, productUnitId);
    }

    /**
     * Đếm số lượng sản phẩm yêu thích
     */
    @Override
    @Transactional(readOnly = true)
    public Integer countFavorites(Integer customerId) {
        log.debug("Đếm số lượng sản phẩm yêu thích - Customer: {}", customerId);

        // Validate input
        if (customerId == null || customerId <= 0) {
            return 0;
        }

        return customerFavoriteRepository.countByCustomerId(customerId);
    }
}
