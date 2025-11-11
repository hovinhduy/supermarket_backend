package iuh.fit.supermarket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO request cho báo cáo trả hàng
 */
public record ReturnReportRequestDTO(
        @NotNull(message = "Từ ngày không được để trống")
        LocalDate fromDate,

        @NotNull(message = "Đến ngày không được để trống")
        LocalDate toDate
) {
    /**
     * Compact canonical constructor - validate dữ liệu đầu vào
     */
    public ReturnReportRequestDTO {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng đến ngày");
        }
    }
}
