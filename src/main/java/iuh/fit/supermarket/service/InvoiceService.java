package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.dto.sale.OrderPromotionRequestDTO;

import java.util.List;
import java.util.Map;

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

    /**
     * Lưu thông tin khuyến mãi đã áp dụng cho invoice
     * 
     * @param invoiceNumber Số hóa đơn
     * @param orderPromotions Danh sách khuyến mãi order level
     * @param itemPromotionsByIndex Map index -> PromotionAppliedDTO cho từng item
     */
    void saveAppliedPromotions(
            String invoiceNumber,
            List<OrderPromotionRequestDTO> orderPromotions,
            Map<Integer, PromotionAppliedDTO> itemPromotionsByIndex
    );
}
