package iuh.fit.supermarket.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho request tạo promotion line
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLineCreateRequest {
    
    @NotBlank(message = "Mã dòng khuyến mãi không được để trống")
    private String lineCode;
    
    private String description;
    
    @NotNull(message = "Loại khuyến mãi không được để trống")
    private PromotionType promotionType;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;
    
    @NotNull(message = "Trạng thái không được để trống")
    private PromotionStatus status;
    
    private Boolean isCombinable = false;
    
    @Min(value = 1, message = "Số lượng tối đa phải lớn hơn 0")
    private Integer maxTotalQuantity;
    
    @Min(value = 1, message = "Số lần sử dụng tối đa cho mỗi khách hàng phải lớn hơn 0")
    private Integer maxPerCustomer;
    
    @Min(value = 0, message = "Độ ưu tiên không được âm")
    private Integer priority = 0;
    
    @Valid
    private List<PromotionDetailCreateRequest> promotionDetails;
}
