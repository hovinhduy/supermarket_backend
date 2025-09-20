package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.AttributeDto;
import iuh.fit.supermarket.dto.product.AttributeValueCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeValueDto;
import iuh.fit.supermarket.entity.Attribute;
import iuh.fit.supermarket.entity.AttributeValue;
import iuh.fit.supermarket.repository.AttributeRepository;
import iuh.fit.supermarket.repository.AttributeValueRepository;
import iuh.fit.supermarket.service.AttributeValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của AttributeValueService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttributeValueServiceImpl implements AttributeValueService {

    private final AttributeValueRepository attributeValueRepository;
    private final AttributeRepository attributeRepository;

    /**
     * Tạo giá trị thuộc tính mới
     */
    @Override
    public AttributeValueDto createAttributeValue(AttributeValueCreateRequest request) {
        log.info("Bắt đầu tạo giá trị thuộc tính mới: {} cho thuộc tính ID: {}",
                request.getValue(), request.getAttributeId());

        // Kiểm tra thuộc tính có tồn tại không
        Attribute attribute = attributeRepository.findById(request.getAttributeId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + request.getAttributeId()));

        // Kiểm tra trùng lặp giá trị thuộc tính
        if (attributeValueRepository.existsByValueAndAttributeId(request.getValue(), request.getAttributeId())) {
            throw new RuntimeException(
                    "Giá trị '" + request.getValue() + "' đã tồn tại cho thuộc tính '" + attribute.getName() + "'");
        }

        // Tạo entity AttributeValue
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setValue(request.getValue());
        attributeValue.setDescription(request.getDescription());
        attributeValue.setAttribute(attribute);

        // Lưu giá trị thuộc tính
        attributeValue = attributeValueRepository.save(attributeValue);
        log.info("Đã tạo giá trị thuộc tính với ID: {}", attributeValue.getId());

        return mapToAttributeValueDto(attributeValue);
    }

    /**
     * Lấy thông tin giá trị thuộc tính theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public AttributeValueDto getAttributeValueById(Long id) {
        log.info("Lấy thông tin giá trị thuộc tính ID: {}", id);

        AttributeValue attributeValue = attributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giá trị thuộc tính với ID: " + id));

        return mapToAttributeValueDto(attributeValue);
    }

    /**
     * Cập nhật thông tin giá trị thuộc tính
     */
    @Override
    public AttributeValueDto updateAttributeValue(Long id, AttributeValueCreateRequest request) {
        log.info("Cập nhật giá trị thuộc tính ID: {}", id);

        AttributeValue attributeValue = attributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giá trị thuộc tính với ID: " + id));

        // Kiểm tra thuộc tính có tồn tại không
        Attribute attribute = attributeRepository.findById(request.getAttributeId())
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + request.getAttributeId()));

        // Kiểm tra trùng lặp giá trị nếu giá trị mới khác giá trị cũ hoặc thuộc tính
        // thay đổi
        if ((!attributeValue.getValue().equals(request.getValue()) ||
                !attributeValue.getAttribute().getId().equals(request.getAttributeId())) &&
                attributeValueRepository.existsByValueAndAttributeId(request.getValue(), request.getAttributeId())) {
            throw new RuntimeException(
                    "Giá trị '" + request.getValue() + "' đã tồn tại cho thuộc tính '" + attribute.getName() + "'");
        }

        // Cập nhật thông tin
        attributeValue.setValue(request.getValue());
        attributeValue.setDescription(request.getDescription());
        attributeValue.setAttribute(attribute);
        attributeValue = attributeValueRepository.save(attributeValue);

        log.info("Cập nhật giá trị thuộc tính thành công");
        return mapToAttributeValueDto(attributeValue);
    }

    /**
     * Xóa giá trị thuộc tính
     */
    @Override
    public void deleteAttributeValue(Long id) {
        log.info("Xóa giá trị thuộc tính ID: {}", id);

        AttributeValue attributeValue = attributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giá trị thuộc tính với ID: " + id));

        attributeValueRepository.delete(attributeValue);
        log.info("Xóa giá trị thuộc tính thành công");
    }

    /**
     * Lấy danh sách tất cả giá trị thuộc tính
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueDto> getAllAttributeValues() {
        log.info("Lấy danh sách tất cả giá trị thuộc tính");

        List<AttributeValue> attributeValues = attributeValueRepository.findAll();
        return attributeValues.stream()
                .map(this::mapToAttributeValueDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách giá trị thuộc tính theo ID thuộc tính
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueDto> getAttributeValuesByAttributeId(Long attributeId) {
        log.info("Lấy danh sách giá trị thuộc tính theo ID thuộc tính: {}", attributeId);

        // Kiểm tra thuộc tính có tồn tại không
        if (!attributeRepository.existsById(attributeId)) {
            throw new RuntimeException("Không tìm thấy thuộc tính với ID: " + attributeId);
        }

        List<AttributeValue> attributeValues = attributeValueRepository.findByAttributeId(attributeId);
        return attributeValues.stream()
                .map(this::mapToAttributeValueDto)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm giá trị thuộc tính theo từ khóa
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttributeValueDto> searchAttributeValues(String keyword) {
        log.info("Tìm kiếm giá trị thuộc tính với từ khóa: {}", keyword);

        List<AttributeValue> attributeValues = attributeValueRepository.searchByKeyword(keyword);
        return attributeValues.stream()
                .map(this::mapToAttributeValueDto)
                .collect(Collectors.toList());
    }

    /**
     * Map AttributeValue entity thành AttributeValueDto
     */
    private AttributeValueDto mapToAttributeValueDto(AttributeValue attributeValue) {
        AttributeValueDto dto = new AttributeValueDto();
        dto.setId(attributeValue.getId());
        dto.setValue(attributeValue.getValue());
        dto.setDescription(attributeValue.getDescription());
        dto.setCreatedDate(attributeValue.getCreatedDate());
        dto.setUpdatedAt(attributeValue.getUpdatedAt());

        // Map thông tin thuộc tính
        if (attributeValue.getAttribute() != null) {
            AttributeDto attributeDto = new AttributeDto();
            attributeDto.setId(attributeValue.getAttribute().getId());
            attributeDto.setName(attributeValue.getAttribute().getName());
            attributeDto.setCreatedDate(attributeValue.getAttribute().getCreatedDate());
            attributeDto.setUpdatedAt(attributeValue.getAttribute().getUpdatedAt());
            dto.setAttribute(attributeDto);
        }

        return dto;
    }
}
