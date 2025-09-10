package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.VariantAttributeDto;
import iuh.fit.supermarket.entity.VariantAttribute;
import iuh.fit.supermarket.entity.ProductVariant;
import iuh.fit.supermarket.entity.AttributeValue;

import java.util.List;

/**
 * Service interface để xử lý logic nghiệp vụ liên quan đến thuộc tính biến thể
 * sản phẩm
 */
public interface VariantAttributeService {

    /**
     * Tạo thuộc tính cho biến thể sản phẩm
     * 
     * @param variantId        ID của biến thể
     * @param attributeValueId ID của giá trị thuộc tính
     * @return VariantAttributeDto đã tạo
     */
    VariantAttributeDto createVariantAttribute(Long variantId, Long attributeValueId);

    /**
     * Tạo nhiều thuộc tính cho biến thể sản phẩm
     * 
     * @param variantId         ID của biến thể
     * @param attributeValueIds danh sách ID của các giá trị thuộc tính
     * @return danh sách VariantAttributeDto đã tạo
     */
    List<VariantAttributeDto> createVariantAttributes(Long variantId, List<Long> attributeValueIds);

    /**
     * Lấy tất cả thuộc tính của một biến thể
     * 
     * @param variantId ID của biến thể
     * @return danh sách VariantAttributeDto
     */
    List<VariantAttributeDto> getVariantAttributes(Long variantId);

    /**
     * Lấy tất cả biến thể có chứa giá trị thuộc tính cụ thể
     * 
     * @param attributeValueId ID của giá trị thuộc tính
     * @return danh sách VariantAttributeDto
     */
    List<VariantAttributeDto> getVariantsByAttributeValue(Long attributeValueId);

    /**
     * Xóa thuộc tính khỏi biến thể
     * 
     * @param variantId        ID của biến thể
     * @param attributeValueId ID của giá trị thuộc tính
     */
    void removeVariantAttribute(Long variantId, Long attributeValueId);

    /**
     * Xóa tất cả thuộc tính của một biến thể
     * 
     * @param variantId ID của biến thể
     */
    void removeAllVariantAttributes(Long variantId);

    /**
     * Cập nhật thuộc tính của biến thể
     * Xóa tất cả thuộc tính cũ và thêm thuộc tính mới
     * 
     * @param variantId         ID của biến thể
     * @param attributeValueIds danh sách ID của các giá trị thuộc tính mới
     * @return danh sách VariantAttributeDto đã cập nhật
     */
    List<VariantAttributeDto> updateVariantAttributes(Long variantId, List<Long> attributeValueIds);

    /**
     * Kiểm tra biến thể có thuộc tính cụ thể hay không
     * 
     * @param variantId        ID của biến thể
     * @param attributeValueId ID của giá trị thuộc tính
     * @return true nếu biến thể có thuộc tính này
     */
    boolean hasVariantAttribute(Long variantId, Long attributeValueId);

    /**
     * Tìm biến thể theo tổ hợp thuộc tính
     * 
     * @param productId         ID của sản phẩm
     * @param attributeValueIds danh sách ID của các giá trị thuộc tính
     * @return ID của biến thể nếu tìm thấy, null nếu không
     */
    Long findVariantByAttributeCombination(Long productId, List<Long> attributeValueIds);

    /**
     * Lấy tên thuộc tính kết hợp của biến thể (dạng "Đỏ - L")
     * 
     * @param variantId ID của biến thể
     * @return tên thuộc tính kết hợp
     */
    String getVariantAttributeDisplayName(Long variantId);
}
