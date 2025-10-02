package iuh.fit.supermarket.dto.promotion;

import lombok.EqualsAndHashCode;

/**
 * DTO cho request tạo mới detail và gắn vào một promotion line đã tồn tại
 * ID của line sẽ được truyền qua URL path parameter
 */
@EqualsAndHashCode(callSuper = true)
public class PromotionDetailWithLineRequestDTO extends PromotionDetailRequestDTO {
    // Class này kế thừa tất cả fields từ PromotionDetailRequestDTO
    // Không cần field promotionLineId vì sẽ truyền qua URL path parameter
}
