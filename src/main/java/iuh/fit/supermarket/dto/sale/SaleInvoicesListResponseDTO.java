package iuh.fit.supermarket.dto.sale;

import java.util.List;

/**
 * DTO chứa danh sách hoá đơn bán với thông tin khuyến mãi đầy đủ
 */
public record SaleInvoicesListResponseDTO(
        List<SaleInvoiceFullDTO> invoices,
        Integer totalCount,
        Integer pageNumber,
        Integer pageSize
) {
}
