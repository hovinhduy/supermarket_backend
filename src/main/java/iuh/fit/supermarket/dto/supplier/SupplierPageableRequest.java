package iuh.fit.supermarket.dto.supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho request phân trang và lọc nhà cung cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để lấy danh sách nhà cung cấp với phân trang, tìm kiếm và sắp xếp")
public class SupplierPageableRequest {

    /**
     * Trang hiện tại (bắt đầu từ 1)
     */
    @Schema(description = "Số trang hiện tại, bắt đầu từ 1", example = "1", defaultValue = "1")
    @Min(value = 1, message = "Số trang phải lớn hơn hoặc bằng 1")
    private Integer page = 1;

    /**
     * Số lượng items mỗi trang
     */
    @Schema(description = "Số lượng nhà cung cấp trên mỗi trang", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Số lượng items phải lớn hơn 0")
    @Max(value = 100, message = "Số lượng items không được vượt quá 100")
    private Integer limit = 10;

    /**
     * Từ khóa tìm kiếm (tên, mã, email, phone)
     */
    @Schema(description = "Từ khóa tìm kiếm trong tên, mã, email hoặc phone", example = "", defaultValue = "")
    private String search = "";

    /**
     * Trạng thái hoạt động (1: active, 0: inactive, null: tất cả)
     */
    @Schema(description = "Trạng thái hoạt động (1: hoạt động, 0: không hoạt động, null: tất cả)", example = "1")
    private Integer isActive;

    /**
     * Danh sách sắp xếp
     */
    @Schema(description = "Danh sách các trường sắp xếp")
    @Valid
    private List<SortRequest> sorts;

    /**
     * DTO cho thông tin sắp xếp
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin sắp xếp")
    public static class SortRequest {

        /**
         * Tên trường cần sắp xếp
         */
        @Schema(description = "Tên trường cần sắp xếp", example = "name", allowableValues = { "name", "code", "email",
                "phone", "createdAt", "updatedAt" })
        private String field = "name";

        /**
         * Hướng sắp xếp
         */
        @Schema(description = "Hướng sắp xếp", example = "ASC", allowableValues = { "ASC", "DESC" })
        private String order = "ASC";
    }

    /**
     * Lấy giá trị boolean của isActive
     *
     * @return Boolean value hoặc null nếu không filter
     */
    @JsonIgnore
    public Boolean getActiveValue() {
        if (isActive == null) {
            return null;
        }
        return isActive == 1;
    }

    /**
     * Lấy số trang cho Spring Data (bắt đầu từ 0)
     *
     * @return page number cho Spring Data
     */
    @JsonIgnore
    public int getPageForSpringData() {
        return Math.max(0, (page != null ? page : 1) - 1);
    }

    /**
     * Lấy search term đã được trim
     *
     * @return search term hoặc empty string
     */
    @JsonIgnore
    public String getSearchTerm() {
        return search != null ? search.trim() : "";
    }

    /**
     * Kiểm tra có search term không
     *
     * @return true nếu có search term
     */
    @JsonIgnore
    public boolean hasSearchTerm() {
        return getSearchTerm() != null && !getSearchTerm().isEmpty();
    }

    /**
     * Kiểm tra có filter theo isActive không
     *
     * @return true nếu có filter theo isActive
     */
    @JsonIgnore
    public boolean hasActiveFilter() {
        return isActive != null;
    }
}
