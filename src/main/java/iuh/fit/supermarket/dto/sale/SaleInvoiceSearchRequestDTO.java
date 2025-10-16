package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.enums.InvoiceStatus;
import java.time.LocalDate;

/**
 * DTO chứa các tiêu chí tìm kiếm và lọc hoá đơn bán
 */
public record SaleInvoiceSearchRequestDTO(
        String invoiceNumber,
        String customerName,
        LocalDate fromDate,
        LocalDate toDate,
        InvoiceStatus status,
        Integer pageNumber,
        Integer pageSize
) {
}
