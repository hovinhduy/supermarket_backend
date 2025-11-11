package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho hóa đơn trả hàng (gom nhóm các sản phẩm theo hóa đơn)
 */
public record ReturnReportItemDTO(
        String originalInvoiceNumber,           // Hóa đơn mua
        LocalDate originalInvoiceDate,          // Ngày mua
        String returnCode,                      // Hóa đơn trả
        LocalDate returnDate,                   // Ngày trả
        List<ReturnProductItemDTO> products,    // Danh sách sản phẩm trong hóa đơn trả
        Integer totalQuantity,                  // Tổng số lượng trong hóa đơn trả này
        BigDecimal totalRefundAmount            // Tổng tiền hoàn trả của hóa đơn này
) {}
