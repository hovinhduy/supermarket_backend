package iuh.fit.supermarket.dto.supplier;

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
 * DTO cho request xóa nhiều nhà cung cấp cùng lúc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để xóa nhiều nhà cung cấp cùng lúc")
public class SupplierBatchDeleteRequest {

    /**
     * Danh sách ID của các nhà cung cấp cần xóa
     */
    @Schema(description = "Danh sách ID của các nhà cung cấp cần xóa", example = "[1, 2, 3, 4, 5]", required = true)
    @NotNull(message = "Danh sách ID nhà cung cấp không được null")
    @NotEmpty(message = "Danh sách ID nhà cung cấp không được rỗng")
    @Size(min = 1, max = 100, message = "Số lượng nhà cung cấp phải từ 1 đến 100")
    private List<@NotNull(message = "ID nhà cung cấp không được null") Integer> supplierIds;

    /**
     * Kiểm tra danh sách có hợp lệ không
     * 
     * @return true nếu danh sách hợp lệ
     */
    @JsonIgnore
    public boolean isValid() {
        return supplierIds != null &&
                !supplierIds.isEmpty() &&
                supplierIds.size() <= 100 &&
                supplierIds.stream().allMatch(id -> id != null && id > 0);
    }

    /**
     * Lấy số lượng supplier cần xóa
     * 
     * @return số lượng supplier
     */
    @JsonIgnore
    public int getCount() {
        return supplierIds != null ? supplierIds.size() : 0;
    }

    /**
     * Lọc ra các ID hợp lệ (> 0)
     * 
     * @return danh sách ID hợp lệ
     */
    @JsonIgnore
    public List<Integer> getValidIds() {
        if (supplierIds == null) {
            return List.of();
        }
        return supplierIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    /**
     * Lấy các ID không hợp lệ
     * 
     * @return danh sách ID không hợp lệ
     */
    @JsonIgnore
    public List<Integer> getInvalidIds() {
        if (supplierIds == null) {
            return List.of();
        }
        return supplierIds.stream()
                .filter(id -> id == null || id <= 0)
                .toList();
    }
}
