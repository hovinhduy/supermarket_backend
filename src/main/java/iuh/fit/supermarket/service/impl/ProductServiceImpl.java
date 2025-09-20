package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.entity.*;
import iuh.fit.supermarket.repository.*;
import iuh.fit.supermarket.service.ProductService;
import iuh.fit.supermarket.service.VariantAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
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
    private final AttributeValueRepository attributeValueRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VariantAttributeRepository variantAttributeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final VariantAttributeService variantAttributeService;

    /**
     * Tạo sản phẩm mới
     */
    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Bắt đầu tạo sản phẩm mới: {}", request.getName());

        // Tạo entity Product (chỉ thông tin chung)
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setIsActive(true);
        product.setIsDeleted(false);
        product.setVariantCount(1); // Mặc định có 1 variant cơ bản

        // Lưu sản phẩm
        product = productRepository.save(product);
        log.info("Đã lưu sản phẩm với ID: {}", product.getId());

        // Kiểm tra baseUnit bắt buộc
        if (request.getBaseUnit() == null || request.getBaseUnit().getUnit() == null
                || request.getBaseUnit().getUnit().trim().isEmpty()) {
            throw new RuntimeException("Đơn vị cơ bản (baseUnit) là bắt buộc khi tạo sản phẩm");
        }

        // Tạo đơn vị cơ bản và variant mặc định
        ProductUnit baseUnit = createProductUnit(product, request.getBaseUnit().getUnit(), 1, true);

        // Tạo variant cơ bản với đơn vị này
        createDefaultProductVariant(product, baseUnit, request.getBaseUnit(), request.getAllowsSale());

        // Tạo các đơn vị bổ sung
        if (request.getAdditionalUnits() != null) {
            for (ProductCreateRequest.AdditionalUnitDto unitDto : request.getAdditionalUnits()) {
                createProductUnit(product, unitDto.getUnit(), unitDto.getConversionValue());
            }
        }
        log.info("Tạo sản phẩm thành công với mã: {}");
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

        // Cập nhật thông tin cơ bản (chỉ thông tin chung)
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getDescription() != null)
            product.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
        }
        if (request.getIsActive() != null)
            product.setIsActive(request.getIsActive());

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

        // Xóa mềm sản phẩm
        product.setIsDeleted(true);
        product.setIsActive(false);
        productRepository.save(product);

        // Xóa mềm tất cả các biến thể của sản phẩm
        softDeleteProductVariants(id);

        log.info("Xóa sản phẩm và các biến thể thành công");
    }

    /**
     * Xóa nhiều sản phẩm cùng lúc (soft delete)
     */
    @Override
    public void deleteProducts(List<Long> ids) {
        log.info("Xóa nhiều sản phẩm với {} ID: {}", ids.size(), ids);

        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("Danh sách ID sản phẩm không được rỗng");
        }

        // Tìm tất cả sản phẩm theo danh sách ID
        List<Product> products = productRepository.findAllById(ids);

        // Kiểm tra xem có sản phẩm nào không tồn tại
        if (products.size() != ids.size()) {
            List<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toList());
            List<Long> notFoundIds = ids.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toList());
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + notFoundIds);
        }

        // Cập nhật trạng thái xóa cho tất cả sản phẩm
        for (Product product : products) {
            product.setIsDeleted(true);
            product.setIsActive(false);
        }

        // Lưu tất cả sản phẩm đã cập nhật
        productRepository.saveAll(products);

        // Xóa mềm tất cả các biến thể của từng sản phẩm
        for (Product product : products) {
            softDeleteProductVariants(product.getId());
        }

        log.info("Xóa {} sản phẩm và các biến thể thành công", products.size());
    }

    /**
     * Xóa mềm tất cả các biến thể của một sản phẩm
     */
    private void softDeleteProductVariants(Long productId) {
        log.info("Xóa mềm các biến thể của sản phẩm ID: {}", productId);

        // Tìm tất cả biến thể của sản phẩm (bao gồm cả đã xóa)
        List<ProductVariant> variants = productVariantRepository.findAllByProductId(productId);

        if (variants != null && !variants.isEmpty()) {
            // Cập nhật trạng thái xóa mềm cho tất cả biến thể
            for (ProductVariant variant : variants) {
                variant.setIsDeleted(true);
                variant.setIsActive(false);
                variant.setAllowsSale(false); // Không cho phép bán nữa
            }

            // Lưu tất cả biến thể đã cập nhật
            productVariantRepository.saveAll(variants);

            log.info("Đã xóa mềm {} biến thể của sản phẩm ID: {}", variants.size(), productId);
        } else {
            log.info("Không tìm thấy biến thể nào cho sản phẩm ID: {}", productId);
        }
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
     * Lấy danh sách sản phẩm với filtering, searching và sorting nâng cao
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsAdvanced(ProductPageableRequest request) {
        log.info("Lấy danh sách sản phẩm nâng cao: page={}, limit={}, search={}, isActive={}",
                request.getPage(), request.getLimit(), request.getSearchTerm(), request.getIsActive());

        // Tạo Pageable object từ request
        Pageable pageable = createPageableFromRequest(request);

        // Gọi repository để lấy dữ liệu
        Page<Product> products = productRepository.findProductsAdvanced(
                request.getSearchTerm(),
                request.getActiveValue(),
                pageable);

        // Map sang ProductResponse
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

        // Kiểm tra sản phẩm tồn tại
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm gốc với ID: " + productId));

        // TODO: Cần thay đổi logic tạo biến thể theo mô hình mới
        // Không tạo Product mới, mà tạo ProductVariant
        throw new RuntimeException("Chức năng tạo biến thể đang được cập nhật theo mô hình mới");
    }

    /**
     * Tạo mã variant tự động cho sản phẩm đơn giản (theo product)
     */
    private String generateVariantCode(String productCode) {
        log.info("Tạo mã variant tự động cho sản phẩm: {}", productCode);

        // Với sản phẩm đơn giản, mã variant = mã sản phẩm + "-01"
        String baseVariantCode = productCode + "-01";

        // Kiểm tra xem mã đã tồn tại chưa
        int counter = 1;
        String variantCode = baseVariantCode;
        while (productVariantRepository.existsByVariantCode(variantCode)) {
            counter++;
            variantCode = productCode + String.format("-%02d", counter);
        }

        return variantCode;
    }

    /**
     * Tạo mã variant tự động toàn cục (SP000001, SP000002...)
     */
    private String generateGlobalVariantCode() {
        log.info("Tạo mã variant tự động toàn cục");

        List<String> maxCodes = productVariantRepository.findMaxVariantCode();

        if (maxCodes.isEmpty()) {
            return "SP000001";
        }

        String maxCode = maxCodes.get(0);
        try {
            // Chỉ lấy số từ mã SP000001 (bỏ qua SP)
            int number = Integer.parseInt(maxCode.substring(2)) + 1;
            return String.format("SP%06d", number);
        } catch (Exception e) {
            log.warn("Lỗi khi parse mã variant: {}", maxCode);
            return "SP000001";
        }
    }

    /**
     * Tạo hoặc sử dụng mã variant từ request
     * Nếu variantCode từ request không null và không trống, kiểm tra tính duy nhất
     * và sử dụng
     * Nếu variantCode trùng lặp, báo lỗi
     * Nếu không có variantCode, tự động tạo mã variant mới
     */
    private String generateOrUseVariantCode(String requestedVariantCode) {
        // Nếu có variantCode từ request và không trống
        if (requestedVariantCode != null && !requestedVariantCode.trim().isEmpty()) {
            String trimmedCode = requestedVariantCode.trim();

            // Kiểm tra mã variant đã tồn tại chưa
            if (productVariantRepository.existsByVariantCode(trimmedCode)) {
                log.error("Mã variant {} đã tồn tại trong hệ thống", trimmedCode);
                throw new RuntimeException(
                        "Mã variant '" + trimmedCode + "' đã tồn tại trong hệ thống. Vui lòng sử dụng mã khác.");
            }

            log.info("Sử dụng mã variant từ request: {}", trimmedCode);
            return trimmedCode;
        }

        // Nếu không có variantCode từ request, tạo tự động
        log.info("Không có mã variant từ request, tạo mã tự động");
        return generateGlobalVariantCode();
    }

    /**
     * Tạo variant mặc định cho sản phẩm đơn giản
     */
    private void createDefaultProductVariant(Product product, ProductUnit unit,
            ProductCreateRequest.BaseUnitDto baseUnitDto,
            Boolean allowsSale) {
        log.info("Tạo variant mặc định cho sản phẩm: {}", product.getId());

        ProductVariant variant = new ProductVariant();

        // Tạo variant code: sử dụng từ request nếu có, nếu không thì tự động tạo
        String variantCode = generateOrUseVariantCode(baseUnitDto.getVariantCode());
        variant.setVariantCode(variantCode);

        // Tạo tên variant: Tên sản phẩm + Đơn vị
        String variantName = product.getName() + " - " + unit.getUnit();
        variant.setVariantName(variantName);

        // Thiết lập thông tin cơ bản
        variant.setProduct(product);
        variant.setUnit(unit);

        // Thiết lập barcode nếu có
        if (baseUnitDto.getBarcode() != null && !baseUnitDto.getBarcode().trim().isEmpty()) {
            variant.setBarcode(baseUnitDto.getBarcode());
        }

        // Thiết lập trạng thái cho phép bán từ request hoặc mặc định
        if (allowsSale != null) {
            variant.setAllowsSale(allowsSale);
        } else {
            variant.setAllowsSale(true);
        }
        variant.setIsActive(true);
        variant.setIsDeleted(false);

        // Lưu variant
        productVariantRepository.save(variant);
        log.info("Đã tạo variant mặc định với mã: {} (chỉ thông tin cơ bản)", variantCode);
    }

    /**
     * Tạo đơn vị sản phẩm (không chứa giá)
     */
    private ProductUnit createProductUnit(Product product, String unit, Integer conversionValue) {
        return createProductUnit(product, unit, conversionValue, false);
    }

    /**
     * Tạo đơn vị sản phẩm với khả năng đánh dấu là đơn vị cơ bản
     */
    private ProductUnit createProductUnit(Product product, String unit, Integer conversionValue, Boolean isBaseUnit) {
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);
        productUnit.setUnit(unit);
        productUnit.setConversionValue(conversionValue);
        productUnit.setIsBaseUnit(isBaseUnit != null ? isBaseUnit : false);
        productUnit.setIsActive(true);
        productUnit.setSortOrder(0);

        return productUnitRepository.save(productUnit);
    }

    /**
     * Map Product entity thành ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setVariantCount(product.getVariantCount());
        response.setIsActive(product.getIsActive());
        response.setCreatedDate(product.getCreatedDate());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map category nếu có
        if (product.getCategory() != null) {
            ProductResponse.CategoryDto categoryDto = new ProductResponse.CategoryDto();
            categoryDto.setId(product.getCategory().getCategoryId().longValue());
            categoryDto.setName(product.getCategory().getName());
            response.setCategory(categoryDto);
        }

        // Map brand nếu có
        if (product.getBrand() != null) {
            ProductResponse.BrandDto brandDto = new ProductResponse.BrandDto();
            // Brand entity dùng Integer ID tương tự Category
            brandDto.setId(product.getBrand().getBrandId().longValue());
            brandDto.setName(product.getBrand().getName());
            response.setBrand(brandDto);
        }

        // Map variants (biến thể sản phẩm)
        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
        if (variants != null && !variants.isEmpty()) {
            List<ProductVariantDto> variantDtos = variants.stream()
                    .map(this::mapToProductVariantDto)
                    .collect(Collectors.toList());
            response.setVariants(variantDtos);
        }

        return response;
    }

    /**
     * Tạo sản phẩm mới với nhiều biến thể cùng lúc
     */
    @Override
    public ProductResponse createProductWithVariants(ProductCreateWithVariantsRequest request) {
        log.info("Bắt đầu tạo sản phẩm với nhiều biến thể: {}", request.getName());

        // Validation dữ liệu đầu vào
        validateProductCreateWithVariantsRequest(request);

        // Kiểm tra sự tồn tại của category và brand
        Category category = validateAndGetCategory(request.getCategoryId());

        // Brand không bắt buộc, chỉ kiểm tra tồn tại nếu có truyền brandId
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = validateAndGetBrand(request.getBrandId());
        }

        // Tạo entity Product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand);
        product.setIsActive(true);
        product.setIsDeleted(false);
        product.setVariantCount(request.getVariants().size());

        // Lưu sản phẩm
        product = productRepository.save(product);
        log.info("Đã lưu sản phẩm với ID: {}", product.getId());

        // Tạo các biến thể
        int totalVariants = 0;
        for (ProductCreateWithVariantsRequest.VariantDto variantDto : request.getVariants()) {
            createProductVariantFromDto(product, variantDto, request.getAllowsSale());
            // Mỗi variantDto có thể tạo nhiều ProductVariant (một cho mỗi unit)
            totalVariants += variantDto.getUnits() != null ? variantDto.getUnits().size() : 0;
        }

        log.info("Tạo sản phẩm với {} biến thể (từ {} SKUs) thành công với mã: {} (chỉ thông tin cơ bản)",
                totalVariants, request.getVariants().size());
        return mapToProductResponse(product);
    }

    /**
     * Tạo biến thể sản phẩm từ DTO - tạo một ProductVariant cho mỗi đơn vị
     */
    private void createProductVariantFromDto(Product product,
            ProductCreateWithVariantsRequest.VariantDto variantDto,
            Boolean allowsSale) {

        if (variantDto.getUnits() == null || variantDto.getUnits().isEmpty()) {
            throw new RuntimeException("Biến thể phải có ít nhất một đơn vị");
        }

        // Tạo một ProductVariant cho mỗi đơn vị
        for (int i = 0; i < variantDto.getUnits().size(); i++) {
            ProductCreateWithVariantsRequest.VariantUnitDto unitDto = variantDto.getUnits().get(i);

            // Kiểm tra xem đã có base unit cho loại đơn vị này chưa
            Optional<ProductUnit> existingBaseUnit = productUnitRepository
                    .findByProductIdAndUnitAndIsBaseUnit(product.getId(), unitDto.getUnit(), true);

            // Ưu tiên cờ isBaseUnit từ request; nếu null thì chỉ gán base unit cho đơn vị
            // đầu tiên nếu chưa tồn tại base unit nào cho sản phẩm
            Boolean isBaseUnit;
            if (unitDto.getIsBaseUnit() != null) {
                isBaseUnit = unitDto.getIsBaseUnit();
            } else {
                // Nếu không chỉ định, chỉ gán base unit cho đơn vị đầu tiên của variant đầu
                // tiên
                boolean anyBaseUnitExists = productUnitRepository.findByProductIdAndIsBaseUnit(product.getId(), true)
                        .isPresent();
                isBaseUnit = !anyBaseUnitExists && i == 0;
            }

            // Nếu đã có base unit cho loại đơn vị này, sử dụng lại
            if (existingBaseUnit.isPresent() && Boolean.TRUE.equals(isBaseUnit)) {
                log.info("Sử dụng lại base unit đã tồn tại: {} cho sản phẩm: {}",
                        unitDto.getUnit());
                // Không cần tạo mới, sẽ sử dụng lại trong findOrCreateProductUnit
            }

            // Tìm hoặc tạo ProductUnit cho đơn vị này
            ProductUnit productUnit = findOrCreateProductUnit(product, unitDto.getUnit(), unitDto.getConversionValue(),
                    isBaseUnit);

            // Tạo variant code: sử dụng từ request nếu có, nếu không thì tự động tạo
            String variantCode = generateOrUseVariantCode(unitDto.getVariantCode());

            // Tạo ProductVariant
            ProductVariant productVariant = new ProductVariant();
            productVariant.setProduct(product);
            productVariant.setVariantCode(variantCode);
            productVariant.setVariantName(generateVariantNameWithUnit(product, variantDto, unitDto.getUnit()));
            productVariant.setUnit(productUnit);
            productVariant.setBarcode(unitDto.getBarcode());
            productVariant.setIsActive(true);
            productVariant.setIsDeleted(false);
            productVariant.setAllowsSale(allowsSale != null ? allowsSale : true);

            // Lưu ProductVariant
            productVariant = productVariantRepository.save(productVariant);
            log.info("Đã tạo ProductVariant với code: {} cho đơn vị: {} (chỉ thông tin cơ bản)", variantCode,
                    unitDto.getUnit());

            // Tạo thuộc tính cho tất cả các biến thể (vì mỗi unit là 1 variant riêng)
            if (variantDto.getAttributes() != null && !variantDto.getAttributes().isEmpty()) {
                for (ProductCreateWithVariantsRequest.VariantAttributeDto attrDto : variantDto.getAttributes()) {
                    createVariantAttribute(productVariant, attrDto.getAttributeId(), attrDto.getValue());
                }
            }
        }
    }

    /**
     * Tìm hoặc tạo ProductUnit
     */
    private ProductUnit findOrCreateProductUnit(Product product, String unitName, Integer conversionValue) {
        return findOrCreateProductUnit(product, unitName, conversionValue, false);
    }

    /**
     * Tìm hoặc tạo ProductUnit với khả năng đánh dấu là đơn vị cơ bản
     */
    private ProductUnit findOrCreateProductUnit(Product product, String unitName, Integer conversionValue,
            Boolean isBaseUnit) {
        // Kiểm tra xem ProductUnit đã tồn tại chưa
        Optional<ProductUnit> existingUnit = productUnitRepository.findByProductIdAndUnit(product.getId(), unitName);

        if (existingUnit.isPresent()) {
            log.info("ProductUnit đã tồn tại: {} cho sản phẩm: {}", unitName, product.getId());
            return existingUnit.get();
        }

        // Tạo ProductUnit mới nếu chưa tồn tại
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);
        productUnit.setUnit(unitName);
        productUnit.setConversionValue(conversionValue != null ? conversionValue : 1);
        productUnit.setIsBaseUnit(isBaseUnit != null ? isBaseUnit : false);
        productUnit.setIsActive(true);

        // Tạo code cho unit
        productUnit.setCode(product.getId() + "-" + unitName);

        ProductUnit savedUnit = productUnitRepository.save(productUnit);
        log.info("Đã tạo ProductUnit mới: {} cho sản phẩm: {} (isBaseUnit: {})",
                unitName, product.getId(), isBaseUnit);

        return savedUnit;
    }

    /**
     * Tạo tên biến thể từ thuộc tính
     */
    private String generateVariantName(Product product, ProductCreateWithVariantsRequest.VariantDto variantDto) {
        StringBuilder nameBuilder = new StringBuilder(product.getName());

        if (variantDto.getAttributes() != null && !variantDto.getAttributes().isEmpty()) {
            for (ProductCreateWithVariantsRequest.VariantAttributeDto attr : variantDto.getAttributes()) {
                nameBuilder.append(" - ").append(attr.getValue());
            }
        }

        return nameBuilder.toString();
    }

    /**
     * Tạo tên biến thể từ thuộc tính có kèm đơn vị
     */
    private String generateVariantNameWithUnit(Product product, ProductCreateWithVariantsRequest.VariantDto variantDto,
            String unitName) {
        StringBuilder nameBuilder = new StringBuilder(product.getName());

        if (variantDto.getAttributes() != null && !variantDto.getAttributes().isEmpty()) {
            for (ProductCreateWithVariantsRequest.VariantAttributeDto attr : variantDto.getAttributes()) {
                nameBuilder.append(" - ").append(attr.getValue());
            }
        }

        // Thêm đơn vị vào cuối tên
        nameBuilder.append(" - ").append(unitName);

        return nameBuilder.toString();
    }

    /**
     * Tạo thuộc tính cho biến thể
     */
    private void createVariantAttribute(ProductVariant productVariant, Long attributeId, String value) {
        try {
            // Tìm hoặc tạo AttributeValue
            AttributeValue attributeValue = attributeValueRepository
                    .findByValueAndAttributeId(value, attributeId)
                    .orElseGet(() -> {
                        // Tạo AttributeValue mới
                        Attribute attribute = attributeRepository.findById(attributeId)
                                .orElseThrow(
                                        () -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + attributeId));

                        AttributeValue newValue = new AttributeValue();
                        newValue.setAttribute(attribute);
                        newValue.setValue(value);
                        return attributeValueRepository.save(newValue);
                    });

            // Kiểm tra xem liên kết đã tồn tại chưa
            if (!variantAttributeRepository.existsByVariantIdAndAttributeValueId(
                    productVariant.getVariantId(), attributeValue.getId())) {

                // Tạo liên kết VariantAttribute
                VariantAttribute variantAttribute = new VariantAttribute();
                variantAttribute.setVariant(productVariant);
                variantAttribute.setAttributeValue(attributeValue);

                variantAttributeRepository.save(variantAttribute);

                log.info("Đã tạo thuộc tính {} = {} cho variant: {}",
                        attributeValue.getAttribute().getName(), value, productVariant.getVariantCode());
            } else {
                log.info("Thuộc tính {} = {} đã tồn tại cho variant: {}",
                        attributeValue.getAttribute().getName(), value, productVariant.getVariantCode());
            }

        } catch (Exception e) {
            log.error("Lỗi khi tạo thuộc tính cho variant: ", e);
            throw new RuntimeException("Lỗi khi tạo thuộc tính: " + e.getMessage());
        }
    }

    /**
     * Validation dữ liệu đầu vào cho createProductWithVariants
     */
    private void validateProductCreateWithVariantsRequest(ProductCreateWithVariantsRequest request) {
        // Kiểm tra tên sản phẩm bắt buộc
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Tên sản phẩm là bắt buộc");
        }

        // Kiểm tra categoryId bắt buộc
        if (request.getCategoryId() == null) {
            throw new RuntimeException("ID danh mục sản phẩm (categoryId) là bắt buộc");
        }

        // Brand không bắt buộc, sẽ kiểm tra tồn tại trong logic chính nếu có truyền

        // Kiểm tra danh sách variants
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new RuntimeException("Cần ít nhất một biến thể để tạo sản phẩm");
        }

        // Kiểm tra trùng lặp attributeId trong mỗi variant
        validateVariantAttributes(request.getVariants());
    }

    /**
     * Kiểm tra trùng lặp attributeId trong danh sách thuộc tính của mỗi variant
     */
    private void validateVariantAttributes(List<ProductCreateWithVariantsRequest.VariantDto> variants) {
        for (int variantIndex = 0; variantIndex < variants.size(); variantIndex++) {
            ProductCreateWithVariantsRequest.VariantDto variant = variants.get(variantIndex);

            // Bỏ qua nếu variant không có attributes
            if (variant.getAttributes() == null || variant.getAttributes().isEmpty()) {
                continue;
            }

            // Kiểm tra giới hạn tối đa 3 thuộc tính
            if (variant.getAttributes().size() > 3) {
                throw new RuntimeException(String.format(
                        "Variant thứ %d có %d thuộc tính, vượt quá giới hạn tối đa 3 thuộc tính cho mỗi variant",
                        variantIndex + 1, variant.getAttributes().size()));
            }

            // Sử dụng Set để theo dõi các attributeId đã xuất hiện
            Set<Long> seenAttributeIds = new HashSet<>();

            for (int attrIndex = 0; attrIndex < variant.getAttributes().size(); attrIndex++) {
                ProductCreateWithVariantsRequest.VariantAttributeDto attribute = variant.getAttributes().get(attrIndex);

                // Kiểm tra attributeId không được null
                if (attribute.getAttributeId() == null) {
                    throw new RuntimeException(String.format(
                            "AttributeId không được để trống tại variant thứ %d, thuộc tính thứ %d",
                            variantIndex + 1, attrIndex + 1));
                }

                // Kiểm tra trùng lặp attributeId
                if (seenAttributeIds.contains(attribute.getAttributeId())) {
                    throw new RuntimeException(String.format(
                            "Phát hiện trùng lặp attributeId %d trong variant thứ %d. Mỗi thuộc tính chỉ được xuất hiện một lần trong cùng một variant",
                            attribute.getAttributeId(), variantIndex + 1));
                }

                seenAttributeIds.add(attribute.getAttributeId());
            }
        }
    }

    /**
     * Kiểm tra và lấy thông tin Category
     */
    private Category validateAndGetCategory(Long categoryId) {
        // Chuyển đổi Long sang Integer vì Category entity sử dụng Integer
        Integer categoryIdInt = Math.toIntExact(categoryId);

        Category category = categoryRepository.findById(categoryIdInt)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục sản phẩm với ID: " + categoryId));

        // Kiểm tra category có đang hoạt động không
        if (!category.getIsActive()) {
            throw new RuntimeException("Danh mục sản phẩm với ID: " + categoryId + " đang không hoạt động");
        }

        log.info("Đã xác thực danh mục: {} (ID: {})", category.getName(), categoryId);
        return category;
    }

    /**
     * Kiểm tra và lấy thông tin Brand
     */
    private Brand validateAndGetBrand(Long brandId) {
        // Chuyển đổi Long sang Integer vì Brand entity sử dụng Integer
        Integer brandIdInt = Math.toIntExact(brandId);

        Brand brand = brandRepository.findById(brandIdInt)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu sản phẩm với ID: " + brandId));

        // Kiểm tra brand có đang hoạt động không
        if (!brand.getIsActive()) {
            throw new RuntimeException("Thương hiệu sản phẩm với ID: " + brandId + " đang không hoạt động");
        }

        log.info("Đã xác thực thương hiệu: {} (ID: {})", brand.getName(), brandId);
        return brand;
    }

    /**
     * Map ProductVariant entity thành ProductVariantDto
     */
    private ProductVariantDto mapToProductVariantDto(ProductVariant variant) {
        ProductVariantDto dto = new ProductVariantDto();

        dto.setVariantId(variant.getVariantId());
        dto.setVariantName(variant.getVariantName());
        dto.setVariantCode(variant.getVariantCode());
        dto.setBarcode(variant.getBarcode());
        dto.setAllowsSale(variant.getAllowsSale());
        dto.setIsActive(variant.getIsActive());

        dto.setCreatedAt(variant.getCreatedAt());
        dto.setUpdatedAt(variant.getUpdatedAt());

        // Map unit information
        if (variant.getUnit() != null) {
            ProductVariantDto.ProductUnitDto unitDto = new ProductVariantDto.ProductUnitDto();
            unitDto.setId(variant.getUnit().getId());
            unitDto.setCode(variant.getUnit().getCode());
            unitDto.setUnit(variant.getUnit().getUnit());
            unitDto.setConversionValue(variant.getUnit().getConversionValue());
            unitDto.setIsBaseUnit(variant.getUnit().getIsBaseUnit());
            dto.setUnit(unitDto);
        }

        // Map variant attributes
        List<VariantAttribute> variantAttributes = variantAttributeRepository.findByVariantId(variant.getVariantId());
        if (variantAttributes != null && !variantAttributes.isEmpty()) {
            List<VariantAttributeDto> attributeDtos = variantAttributes.stream()
                    .map(this::mapToVariantAttributeDto)
                    .collect(Collectors.toList());
            dto.setAttributes(attributeDtos);
        }

        // Map images (nếu cần)
        // TODO: Implement image mapping if needed

        return dto;
    }

    /**
     * Cập nhật thông tin biến thể sản phẩm
     */
    @Override
    public ProductVariantDto updateProductVariant(Long variantId, ProductVariantUpdateRequest request) {
        log.info("Cập nhật biến thể sản phẩm ID: {}", variantId);

        // Kiểm tra biến thể tồn tại
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Cập nhật thông tin cơ bản
        if (request.getVariantName() != null) {
            variant.setVariantName(request.getVariantName());
        }

        if (request.getBarcode() != null) {
            // Kiểm tra barcode đã tồn tại chưa (ngoại trừ biến thể hiện tại)
            Optional<ProductVariant> existingVariant = productVariantRepository.findByBarcode(request.getBarcode());
            if (existingVariant.isPresent() && !existingVariant.get().getVariantId().equals(variantId)) {
                throw new RuntimeException("Mã vạch đã tồn tại: " + request.getBarcode());
            }
            variant.setBarcode(request.getBarcode());
        }

        // Cập nhật số lượng tồn kho thông qua WarehouseService
        // Không cập nhật trực tiếp trong ProductVariant nữa
        // TODO: Implement warehouse update logic through WarehouseService if needed

        if (request.getAllowsSale() != null) {
            variant.setAllowsSale(request.getAllowsSale());
        }

        if (request.getIsActive() != null) {
            variant.setIsActive(request.getIsActive());
        }

        // Cập nhật đơn vị nếu có
        if (request.getUnitId() != null) {
            ProductUnit unit = productUnitRepository.findById(request.getUnitId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị với ID: " + request.getUnitId()));

            // Kiểm tra đơn vị có thuộc về cùng sản phẩm không
            if (!unit.getProduct().getId().equals(variant.getProduct().getId())) {
                throw new RuntimeException("Đơn vị không thuộc về sản phẩm này");
            }

            variant.setUnit(unit);
        }

        // Lưu biến thể đã cập nhật
        variant = productVariantRepository.save(variant);
        log.info("Đã cập nhật biến thể với ID: {}", variantId);

        // Cập nhật thuộc tính biến thể nếu có
        if (request.getAttributeValueIds() != null && !request.getAttributeValueIds().isEmpty()) {
            variantAttributeService.updateVariantAttributes(variantId, request.getAttributeValueIds());
            log.info("Đã cập nhật {} thuộc tính cho biến thể", request.getAttributeValueIds().size());
        }

        return mapToProductVariantDto(variant);
    }

    /**
     * Lấy thông tin biến thể theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductVariantDto getProductVariantById(Long variantId) {
        log.info("Lấy thông tin biến thể ID: {}", variantId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        return mapToProductVariantDto(variant);
    }

    /**
     * Lấy danh sách biến thể theo ID sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductVariantDto> getProductVariantsByProductId(Long productId) {
        log.info("Lấy danh sách biến thể cho sản phẩm ID: {}", productId);

        // Kiểm tra sản phẩm tồn tại
        productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // Lấy danh sách biến thể
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

        return variants.stream()
                .map(this::mapToProductVariantDto)
                .collect(Collectors.toList());
    }

    /**
     * Xóa nhiều biến thể cùng lúc (soft delete)
     */
    @Override
    public void deleteProductVariants(List<Long> variantIds) {
        log.info("Xóa {} biến thể với IDs: {}", variantIds != null ? variantIds.size() : 0, variantIds);

        if (variantIds == null || variantIds.isEmpty()) {
            throw new RuntimeException("Danh sách ID biến thể không được rỗng");
        }

        // Lấy tất cả biến thể cần xóa
        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);

        if (variants.isEmpty()) {
            throw new RuntimeException("Không tìm thấy biến thể nào với các ID đã cho");
        }

        // Kiểm tra nếu có ID không tồn tại
        List<Long> foundIds = variants.stream()
                .map(ProductVariant::getVariantId)
                .collect(Collectors.toList());

        List<Long> notFoundIds = variantIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());

        if (!notFoundIds.isEmpty()) {
            log.warn("Một số ID biến thể không tồn tại: {}", notFoundIds);
        }

        // Xóa mềm các biến thể
        variants.forEach(variant -> {
            variant.setIsDeleted(true);
            variant.setIsActive(false);
        });

        productVariantRepository.saveAll(variants);

        log.info("Đã xóa {} biến thể thành công", variants.size());
    }

    /**
     * Xóa một biến thể (soft delete)
     */
    @Override
    public void deleteProductVariant(Long variantId) {
        log.info("Xóa biến thể ID: {}", variantId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Xóa mềm biến thể
        variant.setIsDeleted(true);
        variant.setIsActive(false);
        productVariantRepository.save(variant);

        log.info("Đã xóa biến thể thành công");
    }

    /**
     * Map VariantAttribute entity thành VariantAttributeDto
     */
    private VariantAttributeDto mapToVariantAttributeDto(VariantAttribute variantAttribute) {
        VariantAttributeDto dto = new VariantAttributeDto();

        dto.setId(variantAttribute.getId());
        dto.setVariantId(variantAttribute.getVariant().getVariantId());

        if (variantAttribute.getAttributeValue() != null) {
            dto.setAttributeValueId(variantAttribute.getAttributeValue().getId());
            dto.setAttributeValue(variantAttribute.getAttributeValue().getValue());
            dto.setAttributeValueDescription(variantAttribute.getAttributeValue().getDescription());

            if (variantAttribute.getAttributeValue().getAttribute() != null) {
                dto.setAttributeName(variantAttribute.getAttributeValue().getAttribute().getName());
            }
        }

        return dto;
    }

    /**
     * Tạo Pageable object từ ProductPageableRequest
     */
    private Pageable createPageableFromRequest(ProductPageableRequest request) {
        // Tạo Sort object từ sorts trong request
        Sort sort = Sort.unsorted();

        if (request.getSorts() != null && !request.getSorts().isEmpty()) {
            List<Sort.Order> orders = request.getSorts().stream()
                    .map(sortCriteria -> {
                        Sort.Direction direction = "DESC".equalsIgnoreCase(sortCriteria.getOrder())
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;
                        return new Sort.Order(direction, mapSortField(sortCriteria.getField()));
                    })
                    .collect(Collectors.toList());
            sort = Sort.by(orders);
        }

        // Tạo PageRequest với page index, size và sort
        return PageRequest.of(
                request.getPageIndex(),
                request.getValidLimit(),
                sort);
    }

    /**
     * Map tên field từ frontend sang tên field entity
     */
    private String mapSortField(String field) {
        // Map các tên field phổ biến
        switch (field.toLowerCase()) {
            case "name":
                return "name";
            case "code":
                return "code";
            case "createdate":
            case "created_date":
                return "createdDate";
            case "updatedAt":
            case "updated_at":
                return "updatedAt";
            case "isactive":
            case "is_active":
                return "isActive";
            case "variantcount":
            case "variant_count":
                return "variantCount";
            default:
                // Nếu không match, trả về field gốc (có thể gây lỗi nếu field không tồn tại)
                log.warn("Không nhận dạng được sort field: {}, sử dụng 'name' làm mặc định", field);
                return "name";
        }
    }

}
