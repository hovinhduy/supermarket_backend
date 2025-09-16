package iuh.fit.supermarket.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * DTO cho request lấy lịch sử thay đổi kho hàng với phân trang, tìm kiếm và sắp xếp
 */
@Data
@Schema(description = "Request để lấy lịch sử thay đổi kho hàng với phân trang, tìm kiếm và sắp xếp")
public class InventoryHistoryRequest {

    /**
     * Trang hiện tại (bắt đầu từ 1)
     */
    @Schema(description = "Số trang hiện tại, bắt đầu từ 1", example = "1", defaultValue = "1")
    private Integer page = 1;

    /**
     * Số lượng items mỗi trang
     */
    @Schema(description = "Số lượng bản ghi trên mỗi trang", example = "10", defaultValue = "10")
    private Integer limit = 10;

    /**
     * Từ khóa tìm kiếm (mã sản phẩm, mã biến thể, tên sản phẩm)
     */
    @Schema(description = "Từ khóa tìm kiếm trong mã sản phẩm, mã biến thể hoặc tên sản phẩm", example = "", defaultValue = "")
    private String search = "";

    /**
     * Danh sách điều kiện sắp xếp
     */
    @Schema(description = "Danh sách các điều kiện sắp xếp", example = "[{\"field\": \"time\", \"order\": \"DESC\"}]")
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
        @Schema(description = "Tên trường cần sắp xếp", example = "time", 
                allowableValues = { "time", "actualQuantity", "totalDifference", "increaseQuantity", "decreaseQuantity", "variantCode", "variantName" })
        private String field = "time";

        /**
         * Thứ tự sắp xếp: ASC hoặc DESC
         */
        @Schema(description = "Thứ tự sắp xếp", example = "DESC", allowableValues = { "ASC", "DESC" })
        private String order = "DESC";
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
}
