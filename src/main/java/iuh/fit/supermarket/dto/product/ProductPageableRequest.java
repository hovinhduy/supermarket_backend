package iuh.fit.supermarket.dto.product;

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
 * DTO cho request phân trang và lọc sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request để lấy danh sách sản phẩm với phân trang, tìm kiếm và sắp xếp")
public class ProductPageableRequest {

    /**
     * Số trang hiện tại (bắt đầu từ 0)
     */
    @Schema(description = "Số trang hiện tại, bắt đầu từ 0", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Số trang phải >= 0")
    private Integer page = 0;

    /**
     * Số lượng sản phẩm mỗi trang
     */
    @Schema(description = "Số lượng sản phẩm trên mỗi trang", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Số lượng sản phẩm phải >= 1")
    @Max(value = 100, message = "Số lượng sản phẩm không được vượt quá 100")
    private Integer size = 10;

    /**
     * Từ khóa tìm kiếm (tìm trong tên, mã sản phẩm)
     */
    @Schema(description = "Từ khóa tìm kiếm trong tên và mã sản phẩm", example = "")
    private String searchTerm;

    /**
     * ID danh mục để lọc
     */
    @Schema(description = "ID danh mục để lọc sản phẩm", example = "1")
    private Integer categoryId;

    /**
     * ID thương hiệu để lọc
     */
    @Schema(description = "ID thương hiệu để lọc sản phẩm", example = "1")
    private Integer brandId;

    /**
     * Trạng thái hoạt động để lọc
     */
    @Schema(description = "Trạng thái hoạt động (true: hoạt động, false: không hoạt động, null: tất cả)", example = "true")
    private Boolean isActive;

    /**
     * Có tích điểm thưởng để lọc
     */
    @Schema(description = "Có tích điểm thưởng (true: có, false: không, null: tất cả)", example = "true")
    private Boolean isRewardPoint;

    /**
     * Danh sách sắp xếp
     */
    @Schema(description = "Danh sách các trường sắp xếp")
    @Valid
    private List<String> sort;

    /**
     * Lấy từ khóa tìm kiếm đã được trim
     *
     * @return từ khóa tìm kiếm hoặc empty string
     */
    @JsonIgnore
    public String getSearchTermTrimmed() {
        return searchTerm != null ? searchTerm.trim() : "";
    }

    /**
     * Kiểm tra có từ khóa tìm kiếm không
     *
     * @return true nếu có từ khóa tìm kiếm
     */
    @JsonIgnore
    public boolean hasSearchTerm() {
        String trimmed = getSearchTermTrimmed();
        return !trimmed.isEmpty();
    }

    /**
     * Kiểm tra có lọc theo danh mục không
     *
     * @return true nếu có lọc theo danh mục
     */
    @JsonIgnore
    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    /**
     * Kiểm tra có lọc theo thương hiệu không
     *
     * @return true nếu có lọc theo thương hiệu
     */
    @JsonIgnore
    public boolean hasBrandFilter() {
        return brandId != null;
    }

    /**
     * Kiểm tra có lọc theo trạng thái hoạt động không
     *
     * @return true nếu có lọc theo trạng thái hoạt động
     */
    @JsonIgnore
    public boolean hasActiveFilter() {
        return isActive != null;
    }

    /**
     * Kiểm tra có lọc theo tích điểm thưởng không
     *
     * @return true nếu có lọc theo tích điểm thưởng
     */
    @JsonIgnore
    public boolean hasRewardPointFilter() {
        return isRewardPoint != null;
    }

    /**
     * Lấy page number hợp lệ
     *
     * @return page number >= 0
     */
    @JsonIgnore
    public int getValidPage() {
        return page != null && page >= 0 ? page : 0;
    }

    /**
     * Lấy page size hợp lệ
     *
     * @return page size từ 1-100
     */
    @JsonIgnore
    public int getValidSize() {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
