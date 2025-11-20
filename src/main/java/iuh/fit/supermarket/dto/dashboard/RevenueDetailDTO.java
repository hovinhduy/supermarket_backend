package iuh.fit.supermarket.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho chi tiết doanh thu theo khoảng thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RevenueDetailDTO {
    
    /**
     * Nhãn (label) của điểm dữ liệu
     * VD: "0h", "1h", "Thứ 2", "Ngày 1"
     */
    private String label;
    
    /**
     * Giá trị doanh thu tại điểm dữ liệu này
     */
    private BigDecimal revenue;
    
    /**
     * Số lượng hóa đơn tại điểm dữ liệu này
     */
    private Long invoiceCount;
}
