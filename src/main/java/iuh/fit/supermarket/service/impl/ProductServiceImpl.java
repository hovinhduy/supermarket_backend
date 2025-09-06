package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của ProductService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductUnitRepository productUnitRepository;
    private final AttributeRepository attributeRepository;
    private final ProductAttributeRepository productAttributeRepository;

    /**
     * Tạo sản phẩm mới
     */
    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Bắt đầu tạo sản phẩm mới: {}", request.getName());

        // Tạo mã sản phẩm tự động nếu chưa có
        String productCode = generateProductCode();

        // Tạo entity Product
        Product product = new Product();
        product.setCode(productCode);
        product.setName(request.getName());
        product.setProductType(request.getProductType());
        product.setDescription(request.getDescription());
        product.setAllowsSale(request.getAllowsSale());

        // Xử lý đơn vị cơ bản
        if (request.getBaseUnit() != null) {
            product.setUnit(request.getBaseUnit().getUnit());
            product.setBasePrice(request.getBaseUnit().getBasePrice());
            product.setCost(request.getBaseUnit().getCost());
            product.setBarcode(request.getBaseUnit().getBarcode());
        }

        // Xử lý thông tin tồn kho
        if (request.getInventory() != null) {
            product.setMinQuantity(request.getInventory().getMinQuantity());
            product.setMaxQuantity(request.getInventory().getMaxQuantity());
            product.setOnHand(request.getInventory().getOnHand());
        }

        // Lưu sản phẩm
        product = productRepository.save(product);
        log.info("Đã lưu sản phẩm với ID: {}", product.getId());

        // Tạo đơn vị cơ bản
        if (request.getBaseUnit() != null) {
            createProductUnit(product, request.getBaseUnit().getUnit(), request.getBaseUnit().getBasePrice(),
                    1, request.getBaseUnit().getBarcode());
        }

        // Tạo các đơn vị bổ sung
        if (request.getAdditionalUnits() != null) {
            for (ProductCreateRequest.AdditionalUnitDto unitDto : request.getAdditionalUnits()) {
                createProductUnit(product, unitDto.getUnit(), unitDto.getBasePrice(),
                        unitDto.getConversionValue(), unitDto.getBarcode());
            }
        }

        // Tạo thuộc tính sản phẩm
        if (request.getAttributes() != null) {
            for (ProductCreateRequest.ProductAttributeDto attrDto : request.getAttributes()) {
                createProductAttribute(product, attrDto.getAttributeId(), attrDto.getValue());
            }
        }

        log.info("Tạo sản phẩm thành công với mã: {}", productCode);
        return mapToProductResponse(product);
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.info("Lấy thông tin sản phẩm ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        return mapToProductResponse(product);
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("Cập nhật sản phẩm ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Cập nhật thông tin cơ bản
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getFullName() != null)
            product.setFullName(request.getFullName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
            // TODO: Validate category exists
        }
        if (request.getBasePrice() != null)
            product.setBasePrice(request.getBasePrice());
        if (request.getCost() != null)
            product.setCost(request.getCost());
        if (request.getUnit() != null)
            product.setUnit(request.getUnit());
        if (request.getBarcode() != null)
            product.setBarcode(request.getBarcode());
        if (request.getTradeMarkName() != null)
            product.setTradeMarkName(request.getTradeMarkName());
        if (request.getAllowsSale() != null)
            product.setAllowsSale(request.getAllowsSale());
        if (request.getIsActive() != null)
            product.setIsActive(request.getIsActive());
        if (request.getMinQuantity() != null)
            product.setMinQuantity(request.getMinQuantity());
        if (request.getMaxQuantity() != null)
            product.setMaxQuantity(request.getMaxQuantity());

        product = productRepository.save(product);
        log.info("Cập nhật sản phẩm thành công");

        return mapToProductResponse(product);
    }

    /**
     * Xóa sản phẩm (soft delete)
     */
    @Override
    public void deleteProduct(Long id) {
        log.info("Xóa sản phẩm ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        product.setIsDeleted(true);
        product.setIsActive(false);
        productRepository.save(product);

        log.info("Xóa sản phẩm thành công");
    }

    /**
     * Lấy danh sách sản phẩm với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(Pageable pageable) {
        log.info("Lấy danh sách sản phẩm với phân trang");

        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToProductResponse);
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        log.info("Tìm kiếm sản phẩm với từ khóa: {}", keyword);

        List<Product> products = productRepository.findByNameContaining(keyword);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        log.info("Lấy sản phẩm theo danh mục ID: {}", categoryId);

        List<Product> products = productRepository.findByCategoryIdAndIsDeleted(
                categoryId, false);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo biến thể sản phẩm
     */
    @Override
    public ProductResponse createProductVariant(Long productId, ProductVariantCreateRequest request) {
        log.info("Tạo biến thể cho sản phẩm ID: {}", productId);

        Product parentProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm gốc với ID: " + productId));

        // Tạo sản phẩm biến thể mới
        Product variant = new Product();
        variant.setCode(generateProductCode());
        variant.setName(parentProduct.getName());
        variant.setProductType(1); // Biến thể luôn là sản phẩm đơn giản

        if (request.getPricing() != null) {
            variant.setBasePrice(request.getPricing().getBasePrice());
            variant.setCost(request.getPricing().getCost());
        }

        variant = productRepository.save(variant);

        // Cập nhật sản phẩm gốc
        parentProduct.setHasVariants(true);
        parentProduct.setVariantCount(parentProduct.getVariantCount() + 1);
        productRepository.save(parentProduct);

        // Tạo thuộc tính phân biệt cho biến thể
        if (request.getAttributes() != null) {
            for (ProductVariantCreateRequest.VariantAttributeDto attrDto : request.getAttributes()) {
                createProductAttribute(variant, attrDto.getAttributeId(), attrDto.getValue());
            }
        }

        log.info("Tạo biến thể thành công với mã: {}", variant.getCode());
        return mapToProductResponse(variant);
    }

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts() {
        log.info("Lấy danh sách sản phẩm tồn kho thấp");

        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo mã sản phẩm tự động
     */
    @Override
    public String generateProductCode() {
        log.info("Tạo mã sản phẩm tự động");

        List<String> maxCodes = productRepository.findMaxProductCode();

        if (maxCodes.isEmpty()) {
            return "SP000001";
        }

        String maxCode = maxCodes.get(0);
        try {
            int number = Integer.parseInt(maxCode.substring(2)) + 1;
            return String.format("SP%06d", number);
        } catch (Exception e) {
            log.warn("Lỗi khi parse mã sản phẩm: {}", maxCode);
            return "SP000001";
        }
    }

    /**
     * Tạo đơn vị sản phẩm
     */
    private void createProductUnit(Product product, String unit, BigDecimal basePrice,
            Integer conversionValue, String barcode) {
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);
        productUnit.setUnit(unit);
        productUnit.setBasePrice(basePrice);
        productUnit.setConversionValue(conversionValue);
        productUnit.setBarcode(barcode);

        productUnitRepository.save(productUnit);
    }

    /**
     * Tạo thuộc tính sản phẩm
     */
    private void createProductAttribute(Product product, Long attributeId, String value) {
        ProductAttribute productAttribute = new ProductAttribute();
        productAttribute.setProduct(product);

        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + attributeId));
        productAttribute.setAttribute(attribute);
        productAttribute.setValue(value);

        productAttributeRepository.save(productAttribute);
    }

    /**
     * Map Product entity thành ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCode(product.getCode());
        response.setName(product.getName());
        response.setFullName(product.getFullName());
        response.setDescription(product.getDescription());
        response.setProductType(product.getProductType());
        response.setHasVariants(product.getHasVariants());
        response.setVariantCount(product.getVariantCount());
        response.setBasePrice(product.getBasePrice());
        response.setCost(product.getCost());
        response.setLatestPurchasePrice(product.getLatestPurchasePrice());
        response.setUnit(product.getUnit());
        response.setConversionValue(product.getConversionValue());
        response.setOnHand(product.getOnHand());
        response.setOnOrder(product.getOnOrder());
        response.setReserved(product.getReserved());
        response.setMinQuantity(product.getMinQuantity());
        response.setMaxQuantity(product.getMaxQuantity());
        response.setBarcode(product.getBarcode());
        response.setTradeMarkName(product.getTradeMarkName());
        response.setAllowsSale(product.getAllowsSale());
        response.setIsActive(product.getIsActive());
        response.setCreatedDate(product.getCreatedDate());
        response.setModifiedDate(product.getModifiedDate());

        // Map category nếu có
        if (product.getCategory() != null) {
            ProductResponse.CategoryDto categoryDto = new ProductResponse.CategoryDto();
            categoryDto.setId(product.getCategory().getCategoryId().longValue());
            categoryDto.setName(product.getCategory().getName());
            response.setCategory(categoryDto);
        }

        // Map product units
        if (product.getProductUnits() != null) {
            List<ProductResponse.ProductUnitDto> unitDtos = product.getProductUnits().stream()
                    .map(this::mapToProductUnitDto)
                    .collect(Collectors.toList());
            response.setProductUnits(unitDtos);
        }

        // Map attributes
        if (product.getAttributes() != null) {
            List<ProductResponse.ProductAttributeDto> attrDtos = product.getAttributes().stream()
                    .map(this::mapToProductAttributeDto)
                    .collect(Collectors.toList());
            response.setAttributes(attrDtos);
        }


        return response;
    }

    /**
     * Map ProductUnit entity thành DTO
     */
    private ProductResponse.ProductUnitDto mapToProductUnitDto(ProductUnit unit) {
        ProductResponse.ProductUnitDto dto = new ProductResponse.ProductUnitDto();
        dto.setId(unit.getId());
        dto.setCode(unit.getCode());
        dto.setUnit(unit.getUnit());
        dto.setBasePrice(unit.getBasePrice());
        dto.setConversionValue(unit.getConversionValue());
        dto.setAllowsSale(unit.getAllowsSale());
        dto.setBarcode(unit.getBarcode());
        return dto;
    }

    /**
     * Map ProductAttribute entity thành DTO
     */
    private ProductResponse.ProductAttributeDto mapToProductAttributeDto(ProductAttribute attr) {
        ProductResponse.ProductAttributeDto dto = new ProductResponse.ProductAttributeDto();
        dto.setId(attr.getId());
        dto.setAttributeName(attr.getAttribute().getName());
        dto.setValue(attr.getValue());
        return dto;
    }
  
}
