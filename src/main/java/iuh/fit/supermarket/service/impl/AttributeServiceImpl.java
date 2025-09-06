package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.AttributeCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeDto;
import iuh.fit.supermarket.entity.Attribute;
import iuh.fit.supermarket.repository.AttributeRepository;
import iuh.fit.supermarket.service.AttributeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của AttributeService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;

    /**
     * Tạo thuộc tính mới
     */
    @Override
    public AttributeDto createAttribute(AttributeCreateRequest request) {
        log.info("Bắt đầu tạo thuộc tính mới: {}", request.getName());

        // Kiểm tra trùng lặp tên thuộc tính
        if (attributeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Thuộc tính với tên '" + request.getName() + "' đã tồn tại");
        }

        // Tạo entity Attribute
        Attribute attribute = new Attribute();
        attribute.setName(request.getName());

        // Lưu thuộc tính
        attribute = attributeRepository.save(attribute);
        log.info("Đã tạo thuộc tính với ID: {}", attribute.getId());

        return mapToAttributeDto(attribute);
    }

    /**
     * Lấy thông tin thuộc tính theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public AttributeDto getAttributeById(Long id) {
        log.info("Lấy thông tin thuộc tính ID: {}", id);

        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + id));

        return mapToAttributeDto(attribute);
    }

    /**
     * Cập nhật thông tin thuộc tính
     */
    @Override
    public AttributeDto updateAttribute(Long id, AttributeCreateRequest request) {
        log.info("Cập nhật thuộc tính ID: {}", id);

        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + id));

        // Kiểm tra trùng lặp tên nếu tên mới khác tên cũ
        if (!attribute.getName().equals(request.getName()) &&
                attributeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Thuộc tính với tên '" + request.getName() + "' đã tồn tại");
        }

        // Cập nhật thông tin
        attribute.setName(request.getName());
        attribute = attributeRepository.save(attribute);

        log.info("Cập nhật thuộc tính thành công");
        return mapToAttributeDto(attribute);
    }

    /**
     * Xóa thuộc tính
     */
    @Override
    public void deleteAttribute(Long id) {
        log.info("Xóa thuộc tính ID: {}", id);

        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuộc tính với ID: " + id));

        attributeRepository.delete(attribute);
        log.info("Xóa thuộc tính thành công");
    }

    /**
     * Lấy danh sách tất cả thuộc tính
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttributeDto> getAllAttributes() {
        log.info("Lấy danh sách tất cả thuộc tính");

        List<Attribute> attributes = attributeRepository.findAll();
        return attributes.stream()
                .map(this::mapToAttributeDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Attribute entity thành AttributeDto
     */
    private AttributeDto mapToAttributeDto(Attribute attribute) {
        AttributeDto dto = new AttributeDto();
        dto.setId(attribute.getId());
        dto.setName(attribute.getName());
        dto.setCreatedDate(attribute.getCreatedDate());
        dto.setModifiedDate(attribute.getModifiedDate());
        return dto;
    }
}
