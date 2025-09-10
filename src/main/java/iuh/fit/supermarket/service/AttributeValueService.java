package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.AttributeValueCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeValueDto;

import java.util.List;

/**
 * Service interface cho quản lý giá trị thuộc tính
 */
public interface AttributeValueService {

    /**
     * Tạo giá trị thuộc tính mới
     * 
     * @param request thông tin giá trị thuộc tính cần tạo
     * @return thông tin giá trị thuộc tính đã tạo
     */
    AttributeValueDto createAttributeValue(AttributeValueCreateRequest request);

    /**
     * Lấy thông tin giá trị thuộc tính theo ID
     * 
     * @param id ID giá trị thuộc tính
     * @return thông tin giá trị thuộc tính
     */
    AttributeValueDto getAttributeValueById(Long id);

    /**
     * Cập nhật thông tin giá trị thuộc tính
     * 
     * @param id      ID giá trị thuộc tính
     * @param request thông tin cập nhật
     * @return thông tin giá trị thuộc tính đã cập nhật
     */
    AttributeValueDto updateAttributeValue(Long id, AttributeValueCreateRequest request);

    /**
     * Xóa giá trị thuộc tính
     * 
     * @param id ID giá trị thuộc tính
     */
    void deleteAttributeValue(Long id);

    /**
     * Lấy danh sách tất cả giá trị thuộc tính
     * 
     * @return danh sách giá trị thuộc tính
     */
    List<AttributeValueDto> getAllAttributeValues();

    /**
     * Lấy danh sách giá trị thuộc tính theo ID thuộc tính
     * 
     * @param attributeId ID thuộc tính
     * @return danh sách giá trị thuộc tính
     */
    List<AttributeValueDto> getAttributeValuesByAttributeId(Long attributeId);

    /**
     * Tìm kiếm giá trị thuộc tính theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @return danh sách giá trị thuộc tính phù hợp
     */
    List<AttributeValueDto> searchAttributeValues(String keyword);
}
