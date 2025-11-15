package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.structured.ProductInfo;
import iuh.fit.supermarket.dto.price.PriceDetailDto;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.entity.ProductImage;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.repository.ProductRepository;
import iuh.fit.supermarket.repository.WarehouseRepository;
import iuh.fit.supermarket.service.PriceService;
import iuh.fit.supermarket.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ProductSearchService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final PriceService priceService;
    private final WarehouseRepository warehouseRepository;

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductInfo> searchProducts(String searchTerm, Integer limit) {
        log.info("Tìm kiếm sản phẩm với từ khóa: {}, limit: {}", searchTerm, limit);

        // Mặc định limit là 10
        int maxResults = limit != null && limit > 0 ? Math.min(limit, 50) : 10;

        // Tìm kiếm sản phẩm
        List<Product> products = productRepository.findByNameContaining(searchTerm != null ? searchTerm : "");

        // Convert sang ProductInfo và giới hạn số lượng
        return products.stream()
                .limit(maxResults)
                .map(this::convertToProductInfo)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductInfo getProductById(Long productId) {
        log.info("Lấy thông tin sản phẩm ID: {}", productId);

        return productRepository.findById(productId)
                .map(this::convertToProductInfo)
                .orElse(null);
    }

    /**
     * Convert Product entity sang ProductInfo DTO
     */
    private ProductInfo convertToProductInfo(Product product) {
        // Lấy product unit đầu tiên (active)
        ProductUnit primaryUnit = product.getProductUnits().stream()
                .filter(ProductUnit::getIsActive)
                .filter(pu -> !pu.getIsDeleted())
                .min(Comparator.comparing(ProductUnit::getCreatedAt))
                .orElse(null);

        if (primaryUnit == null) {
            return null;
        }

        // Lấy giá hiện tại từ PriceService
        PriceDetailDto priceDetail = priceService.getCurrentPriceByProductUnitId(primaryUnit.getId());
        BigDecimal currentPrice = priceDetail != null ? priceDetail.getSalePrice() : null;

        // Lấy hình ảnh đầu tiên (sorted by sort_order or created_at)
        String imageUrl = product.getImages().stream()
                .min(Comparator.comparing(ProductImage::getSortOrder)
                        .thenComparing(ProductImage::getCreatedAt))
                .map(ProductImage::getImageUrl)
                .orElse(null);

        // Kiểm tra tồn kho qua WarehouseRepository
        Integer stockQuantity = warehouseRepository.findByProductUnitId(primaryUnit.getId())
                .map(warehouse -> warehouse.getQuantityOnHand())
                .orElse(0);
        String stockStatus = stockQuantity > 0 ? "Còn hàng" : "Hết hàng";

        return new ProductInfo(
                product.getId(),
                primaryUnit.getId(), // QUAN TRỌNG: productUnitId để thêm vào giỏ hàng
                product.getName(),
                product.getCode(),
                currentPrice,
                primaryUnit.getUnit().getName(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                stockStatus,
                imageUrl,
                product.getDescription(),
                false, // TODO: Check promotion
                null   // TODO: Get promotion price
        );
    }
}
