package iuh.fit.supermarket.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO cho yêu cầu xóa nhiều khách hàng cùng lúc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body cho việc xóa nhiều khách hàng cùng lúc")
public class BulkDeleteCustomersRequest {

    /**
     * Danh sách ID khách hàng cần xóa
     */
    @Schema(description = "Danh sách ID khách hàng cần xóa", example = "[1, 2, 3, 4, 5]", required = true)
    @NotNull(message = "Danh sách ID khách hàng không được null")
    @NotEmpty(message = "Danh sách ID khách hàng không được rỗng")
    @Size(min = 1, max = 100, message = "Danh sách ID phải có từ 1 đến 100 phần tử")
    private List<Integer> customerIds;

    /**
     * Kiểm tra xem danh sách ID có hợp lệ không
     */
    public boolean hasValidIds() {
        return customerIds != null && !customerIds.isEmpty() &&
                customerIds.stream().allMatch(id -> id != null && id > 0);
    }

    /**
     * Lấy số lượng ID trong danh sách
     */
    @JsonIgnore
    public int getIdsCount() {
        return customerIds != null ? customerIds.size() : 0;
    }

    /**
     * Loại bỏ các ID trùng lặp
     */
    public void removeDuplicates() {
        if (customerIds != null) {
            customerIds = customerIds.stream()
                    .distinct()
                    .toList();
        }
    }
}
