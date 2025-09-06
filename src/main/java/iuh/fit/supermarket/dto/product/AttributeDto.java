package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho thuộc tính
 */
@Data
public class AttributeDto {

    /**
     * ID thuộc tính
     */
    private Long id;

    /**
     * Tên thuộc tính
     */
    private String name;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime modifiedDate;
}
