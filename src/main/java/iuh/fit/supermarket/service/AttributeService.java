package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.AttributeCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeDto;

import java.util.List;

/**
 * Service interface cho quản lý thuộc tính
 */
public interface AttributeService {

    /**
     * Tạo thuộc tính mới
     * 
     * @param request thông tin thuộc tính cần tạo
     * @return thông tin thuộc tính đã tạo
     */
    AttributeDto createAttribute(AttributeCreateRequest request);

    /**
     * Lấy thông tin thuộc tính theo ID
     * 
     * @param id ID thuộc tính
     * @return thông tin thuộc tính
     */
    AttributeDto getAttributeById(Long id);

    /**
     * Cập nhật thông tin thuộc tính
     * 
     * @param id      ID thuộc tính
     * @param request thông tin cập nhật
     * @return thông tin thuộc tính đã cập nhật
     */
    AttributeDto updateAttribute(Long id, AttributeCreateRequest request);

    /**
     * Xóa thuộc tính
     * 
     * @param id ID thuộc tính
     */
    void deleteAttribute(Long id);

    /**
     * Lấy danh sách tất cả thuộc tính
     * 
     * @return danh sách thuộc tính
     */
    List<AttributeDto> getAllAttributes();
}
