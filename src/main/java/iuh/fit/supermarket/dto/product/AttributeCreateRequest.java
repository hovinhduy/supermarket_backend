package iuh.fit.supermarket.dto.product;

import lombok.Data;

/**
 * DTO cho yêu cầu tạo thuộc tính mới
 */
@Data
public class AttributeCreateRequest {

    /**
     * Tên thuộc tính
     */
    private String name;
}
