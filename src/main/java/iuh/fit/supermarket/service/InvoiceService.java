package iuh.fit.supermarket.service;

/**
 * Service interface cho quản lý hóa đơn bán hàng
 */
public interface InvoiceService {

    /**
     * Tạo invoice cho order đã hoàn thành
     * 
     * @param orderId ID của order đã COMPLETED
     * @return Invoice number đã tạo
     */
    String createInvoiceForCompletedOrder(Long orderId);
}
