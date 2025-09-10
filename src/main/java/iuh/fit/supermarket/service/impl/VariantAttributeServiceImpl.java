package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.VariantAttributeDto;
import iuh.fit.supermarket.entity.AttributeValue;
import iuh.fit.supermarket.entity.ProductVariant;
import iuh.fit.supermarket.entity.VariantAttribute;
import iuh.fit.supermarket.repository.AttributeValueRepository;
import iuh.fit.supermarket.repository.ProductVariantRepository;
import iuh.fit.supermarket.repository.VariantAttributeRepository;
import iuh.fit.supermarket.service.VariantAttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của VariantAttributeService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VariantAttributeServiceImpl implements VariantAttributeService {

    private final VariantAttributeRepository variantAttributeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;

    /**
     * Tạo thuộc tính cho biến thể sản phẩm
     */
    @Override
    public VariantAttributeDto createVariantAttribute(Long variantId, Long attributeValueId) {
        log.info("Tạo thuộc tính cho biến thể ID: {}, AttributeValue ID: {}", variantId, attributeValueId);

        // Kiểm tra biến thể tồn tại
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " + variantId));

        // Kiểm tra giá trị thuộc tính tồn tại
        AttributeValue attributeValue = attributeValueRepository.findById(attributeValueId)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy giá trị thuộc tính với ID: " + attributeValueId));

        // Kiểm tra đã tồn tại chưa
        if (variantAttributeRepository.existsByVariantIdAndAttributeValueId(variantId, attributeValueId)) {
            throw new RuntimeException("Biến thể đã có thuộc tính này rồi");
        }

        // Tạo liên kết mới
        VariantAttribute variantAttribute = new VariantAttribute();
        variantAttribute.setVariant(variant);
        variantAttribute.setAttributeValue(attributeValue);

        variantAttribute = variantAttributeRepository.save(variantAttribute);
        log.info("Đã tạo thuộc tính biến thể với ID: {}", variantAttribute.getId());

        return mapToDto(variantAttribute);
    }

    /**
     * Tạo nhiều thuộc tính cho biến thể sản phẩm
     */
    @Override
    public List<VariantAttributeDto> createVariantAttributes(Long variantId, List<Long> attributeValueIds) {
        log.info("Tạo {} thuộc tính cho biến thể ID: {}", attributeValueIds.size(), variantId);

        return attributeValueIds.stream()
                .map(attributeValueId -> createVariantAttribute(variantId, attributeValueId))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả thuộc tính của một biến thể
     */
    @Override
    @Transactional(readOnly = true)
    public List<VariantAttributeDto> getVariantAttributes(Long variantId) {
        log.info("Lấy thuộc tính của biến thể ID: {}", variantId);

        List<VariantAttribute> variantAttributes = variantAttributeRepository.findByVariantId(variantId);
        return variantAttributes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả biến thể có chứa giá trị thuộc tính cụ thể
     */
    @Override
    @Transactional(readOnly = true)
    public List<VariantAttributeDto> getVariantsByAttributeValue(Long attributeValueId) {
        log.info("Lấy biến thể có giá trị thuộc tính ID: {}", attributeValueId);

        List<VariantAttribute> variantAttributes = variantAttributeRepository.findByAttributeValueId(attributeValueId);
        return variantAttributes.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Xóa thuộc tính khỏi biến thể
     */
    @Override
    public void removeVariantAttribute(Long variantId, Long attributeValueId) {
        log.info("Xóa thuộc tính biến thể ID: {}, AttributeValue ID: {}", variantId, attributeValueId);

        List<VariantAttribute> variantAttributes = variantAttributeRepository.findByVariantId(variantId);
        variantAttributes.stream()
                .filter(va -> va.getAttributeValue().getId().equals(attributeValueId))
                .forEach(variantAttributeRepository::delete);

        log.info("Đã xóa thuộc tính biến thể");
    }

    /**
     * Xóa tất cả thuộc tính của một biến thể
     */
    @Override
    public void removeAllVariantAttributes(Long variantId) {
        log.info("Xóa tất cả thuộc tính của biến thể ID: {}", variantId);

        variantAttributeRepository.deleteByVariantId(variantId);
        log.info("Đã xóa tất cả thuộc tính của biến thể");
    }

    /**
     * Cập nhật thuộc tính của biến thể
     */
    @Override
    public List<VariantAttributeDto> updateVariantAttributes(Long variantId, List<Long> attributeValueIds) {
        log.info("Cập nhật thuộc tính cho biến thể ID: {}", variantId);

        // Xóa tất cả thuộc tính cũ
        removeAllVariantAttributes(variantId);

        // Tạo thuộc tính mới
        return createVariantAttributes(variantId, attributeValueIds);
    }

    /**
     * Kiểm tra biến thể có thuộc tính cụ thể hay không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasVariantAttribute(Long variantId, Long attributeValueId) {
        return variantAttributeRepository.existsByVariantIdAndAttributeValueId(variantId, attributeValueId);
    }

    /**
     * Tìm biến thể theo tổ hợp thuộc tính
     */
    @Override
    @Transactional(readOnly = true)
    public Long findVariantByAttributeCombination(Long productId, List<Long> attributeValueIds) {
        log.info("Tìm biến thể của sản phẩm ID: {} với tổ hợp thuộc tính: {}", productId, attributeValueIds);

        // Tìm tất cả biến thể của sản phẩm
        List<ProductVariant> variants = productVariantRepository.findByProductId(productId);

        for (ProductVariant variant : variants) {
            List<VariantAttribute> variantAttributes = variantAttributeRepository
                    .findByVariantId(variant.getVariantId());

            // Lấy danh sách ID thuộc tính của biến thể này
            List<Long> variantAttributeValueIds = variantAttributes.stream()
                    .map(va -> va.getAttributeValue().getId())
                    .collect(Collectors.toList());

            // Kiểm tra xem có khớp với tổ hợp cần tìm không
            if (variantAttributeValueIds.size() == attributeValueIds.size() &&
                    variantAttributeValueIds.containsAll(attributeValueIds)) {
                return variant.getVariantId();
            }
        }

        return null;
    }

    /**
     * Lấy tên thuộc tính kết hợp của biến thể
     */
    @Override
    @Transactional(readOnly = true)
    public String getVariantAttributeDisplayName(Long variantId) {
        log.info("Lấy tên hiển thị thuộc tính của biến thể ID: {}", variantId);

        List<VariantAttribute> variantAttributes = variantAttributeRepository.findByVariantId(variantId);

        return variantAttributes.stream()
                .map(va -> va.getAttributeValue().getValue())
                .collect(Collectors.joining(" - "));
    }

    /**
     * Chuyển đổi entity thành DTO
     */
    private VariantAttributeDto mapToDto(VariantAttribute variantAttribute) {
        VariantAttributeDto dto = new VariantAttributeDto();
        dto.setId(variantAttribute.getId());
        dto.setVariantId(variantAttribute.getVariant().getVariantId());
        dto.setAttributeValueId(variantAttribute.getAttributeValue().getId());
        dto.setAttributeName(variantAttribute.getAttributeValue().getAttribute().getName());
        dto.setAttributeValue(variantAttribute.getAttributeValue().getValue());
        dto.setAttributeValueDescription(variantAttribute.getAttributeValue().getDescription());
        dto.setCreatedDate(variantAttribute.getCreatedDate());
        return dto;
    }
}
