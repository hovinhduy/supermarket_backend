package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO cho API kiểm tra số lượng có thể trả của hóa đơn bán
 *
 * @param invoiceId ID hóa đơn
 * @param invoiceNumber Số hóa đơn
 * @param invoiceDate Ngày lập hóa đơn
 * @param customerName Tên khách hàng
 * @param customerPhone Số điện thoại khách hàng
 * @param lineItems Danh sách chi tiết sản phẩm và số lượng có thể trả
 * @param totalOriginalQuantity Tổng số lượng ban đầu
 * @param totalReturnedQuantity Tổng số lượng đã trả
 * @param totalAvailableQuantity Tổng số lượng còn có thể trả
 */
public record AvailableReturnQuantityResponse(
        Integer invoiceId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        String customerName,
        String customerPhone,
        List<LineItemQuantity> lineItems,
        Integer totalOriginalQuantity,
        Integer totalReturnedQuantity,
        Integer totalAvailableQuantity
) {
    /**
     * Thông tin chi tiết về số lượng có thể trả cho từng dòng sản phẩm
     *
     * @param lineItemId ID chi tiết hóa đơn
     * @param productName Tên sản phẩm
     * @param unitName Tên đơn vị tính
     * @param originalQuantity Số lượng ban đầu trong hóa đơn
     * @param returnedQuantity Số lượng đã trả
     * @param availableQuantity Số lượng còn có thể trả
     * @param unitPrice Đơn giá
     * @param priceAfterDiscount Giá sau giảm giá
     * @param isFullyReturned Đã trả hết hay chưa
     */
    public record LineItemQuantity(
            Integer lineItemId,
            String productName,
            String unitName,
            Integer originalQuantity,
            Integer returnedQuantity,
            Integer availableQuantity,
            BigDecimal unitPrice,
            BigDecimal priceAfterDiscount,
            Boolean isFullyReturned
    ) {
    }
}
