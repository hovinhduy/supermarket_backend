package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.sale.SaleInvoiceFullDTO;

/**
 * Service interface cho việc tạo PDF hóa đơn
 */
public interface InvoicePdfService {

    /**
     * Tạo PDF cho hóa đơn bán hàng
     *
     * @param invoice thông tin hóa đơn đầy đủ
     * @return byte array của file PDF
     */
    byte[] generateInvoicePdf(SaleInvoiceFullDTO invoice);
}
