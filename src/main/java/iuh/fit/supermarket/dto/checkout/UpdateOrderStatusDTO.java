package iuh.fit.supermarket.dto.checkout;

import iuh.fit.supermarket.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO để cập nhật trạng thái đơn hàng
 * Sử dụng bởi Admin để thay đổi trạng thái đơn hàng trong quá trình xử lý
 *
 * @param newStatus trạng thái mới của đơn hàng (bắt buộc)
 * @param note ghi chú về việc thay đổi trạng thái (tùy chọn)
 */
public record UpdateOrderStatusDTO(
    @NotNull(message = "Trạng thái mới không được để trống")
    OrderStatus newStatus,

    String note
) {
    /**
     * Compact constructor để validate dữ liệu đầu vào
     */
    public UpdateOrderStatusDTO {
        // Có thể thêm logic validate nghiệp vụ ở đây nếu cần
        // Ví dụ: kiểm tra các chuyển trạng thái hợp lệ
    }
}