package iuh.fit.supermarket.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.fit.supermarket.enums.PromotionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho PromotionHeader
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionHeaderDTO {
    
    private Long promotionId;
    
    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    private String name;
    
    private String description;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    
    @NotNull(message = "Trạng thái không được để trống")
    private PromotionStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private List<PromotionLineDTO> promotionLines;
}
