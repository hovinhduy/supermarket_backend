package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.ProductResponse;
import iuh.fit.supermarket.entity.Brand;
import iuh.fit.supermarket.entity.Category;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.exception.ProductNotFoundException;
import iuh.fit.supermarket.repository.BrandRepository;
import iuh.fit.supermarket.repository.CategoryRepository;
import iuh.fit.supermarket.repository.ProductRepository;
import iuh.fit.supermarket.service.ProductRecommendationService;
import iuh.fit.supermarket.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ProductRecommendationService
 * Service này cung cấp các hàm đơn giản để AI tìm kiếm và gợi ý sản phẩm
 */
@Service
@Transactional(readOnly = true)
public class ProductRecommendationServiceImpl implements ProductRecommendationService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductService productService;

    /**
     * Constructor injection cho tất cả dependencies
     */
    public ProductRecommendationServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductService productService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productService = productService;
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        List<Product> products = productRepository.findByNameContaining(keyword);
        
        return products.stream()
                .limit(10) // Giới hạn 10 sản phẩm để tránh response quá dài
                .map(product -> productService.getProductById(product.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết sản phẩm
     */
    @Override
    public ProductResponse getProductDetails(Long productId) {
        return productService.getProductById(productId);
    }

    /**
     * Lấy sản phẩm theo danh mục
     */
    @Override
    public List<ProductResponse> getProductsByCategory(String categoryName) {
        Category category = categoryRepository.findByNameContainingIgnoreCase(categoryName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy danh mục: " + categoryName));

        List<Product> products = productRepository.findByCategoryIdAndIsDeleted(
                category.getCategoryId().longValue(), false);
        
        return products.stream()
                .limit(10)
                .map(product -> productService.getProductById(product.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm theo thương hiệu
     */
    @Override
    public List<ProductResponse> getProductsByBrand(String brandName) {
        Brand brand = brandRepository.findByNameContaining(brandName)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy thương hiệu: " + brandName));

        List<Product> products = productRepository.findByIsActiveAndIsDeleted(true, false)
                .stream()
                .filter(p -> p.getBrand() != null && p.getBrand().getBrandId().equals(brand.getBrandId()))
                .collect(Collectors.toList());
        
        return products.stream()
                .limit(10)
                .map(product -> productService.getProductById(product.getId()))
                .collect(Collectors.toList());
    }
}
