package iuh.fit.supermarket.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DTO cho request phân trang và lọc sản phẩm
 */
@Data
@Schema(description = "Request để lấy danh sách sản phẩm với phân trang, tìm kiếm và sắp xếp")
public class ProductPageableRequest {

    /**
     * Trang hiện tại (bắt đầu từ 1)
     */
    @Schema(description = "Số trang hiện tại, bắt đầu từ 1", example = "1", defaultValue = "1")
    private Integer page = 1;

    /**
     * Số lượng items mỗi trang
     */
    @Schema(description = "Số lượng sản phẩm trên mỗi trang", example = "10", defaultValue = "10")
    private Integer limit = 10;

    /**
     * Từ khóa tìm kiếm (tên sản phẩm, mã sản phẩm)
     */
    @Schema(description = "Từ khóa tìm kiếm trong tên hoặc mã sản phẩm", example = "", defaultValue = "")
    private String search = "";

    /**
     * Lọc theo trạng thái hoạt động (1 = active, 0 = inactive, null = tất cả)
     */
    @Schema(description = "Lọc theo trạng thái: 1=hoạt động, 0=không hoạt động, null=tất cả", example = "1", allowableValues = {
            "0", "1" })
    private Integer isActive = 1;

    /**
     * Danh sách điều kiện sắp xếp
     */
    @Schema(description = "Danh sách các điều kiện sắp xếp", example = "[{\"field\": \"name\", \"order\": \"ASC\"}]")
    private List<SortCriteria> sorts;

    /**
     * DTO cho điều kiện sắp xếp
     */
    @Data
    @Schema(description = "Điều kiện sắp xếp")
    public static class SortCriteria {
        /**
         * Tên trường cần sắp xếp
         */
        @Schema(description = "Tên trường cần sắp xếp", example = "name", allowableValues = { "name", "code",
                "created_date", "modified_date", "is_active", "product_type", "variant_count" })
        private String field = "name";

        /**
         * Thứ tự sắp xếp: ASC hoặc DESC
         */
        @Schema(description = "Thứ tự sắp xếp", example = "ASC", allowableValues = { "ASC", "DESC" })
        private String order = "ASC";
    }

    /**
     * Chuyển đổi page từ 1-based sang 0-based cho Spring Data
     */
    @JsonIgnore
    @Schema(hidden = true)
    public Integer getPageIndex() {
        return Math.max(0, (page != null ? page : 1) - 1);
    }

    /**
     * Validate và trả về limit hợp lệ
     */
    @JsonIgnore
    @Schema(hidden = true)
    public Integer getValidLimit() {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 100); // Giới hạn tối đa 100 items
    }

    /**
     * Trả về search term đã trim
     */
    @JsonIgnore
    @Schema(hidden = true)
    public String getSearchTerm() {
        return search != null ? search.trim() : "";
    }

    /**
     * Kiểm tra có điều kiện search không
     */
    @JsonIgnore
    @Schema(hidden = true)
    public boolean hasSearch() {
        return !getSearchTerm().isEmpty();
    }

    /**
     * Kiểm tra có điều kiện lọc isActive không
     */
    @JsonIgnore
    @Schema(hidden = true)
    public boolean hasActiveFilter() {
        return isActive != null;
    }

    /**
     * Trả về giá trị boolean cho isActive filter
     */
    @JsonIgnore
    @Schema(hidden = true)
    public Boolean getActiveValue() {
        return isActive != null ? isActive == 1 : null;
    }
}
