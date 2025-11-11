package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO response cho báo cáo trả hàng
 */
public record ReturnReportResponseDTO(
        LocalDate fromDate,                     // Từ ngày
        LocalDate toDate,                       // Đến ngày
        List<ReturnReportItemDTO> returnItems,  // Danh sách sản phẩm trả
        Integer totalQuantity,                  // Tổng số lượng
        BigDecimal totalRefundAmount            // Tổng giá trị hoàn trả
) {}
