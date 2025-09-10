package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho giá trị thuộc tính
 */
@Data
public class AttributeValueDto {

    /**
     * ID giá trị thuộc tính
     */
    private Long id;

    /**
     * Giá trị cụ thể của thuộc tính
     */
    private String value;

    /**
     * Mô tả về giá trị thuộc tính
     */
    private String description;

    /**
     * Thông tin thuộc tính mà giá trị này thuộc về
     */
    private AttributeDto attribute;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime modifiedDate;
}
