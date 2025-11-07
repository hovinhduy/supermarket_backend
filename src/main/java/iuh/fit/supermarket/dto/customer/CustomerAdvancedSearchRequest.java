package iuh.fit.supermarket.dto.customer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu tìm kiếm khách hàng nâng cao
 * Hỗ trợ tìm kiếm theo nhiều tiêu chí và phân trang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body cho tìm kiếm khách hàng nâng cao")
public class CustomerAdvancedSearchRequest {

    /**
     * Số trang (bắt đầu từ 0)
     */
    @Schema(description = "Số trang (bắt đầu từ 0)", example = "0", minimum = "0")
    @Min(value = 0, message = "Số trang phải >= 0")
    private Integer page = 0;

    /**
     * Số lượng bản ghi trên mỗi trang
     */
    @Schema(description = "Số lượng bản ghi trên mỗi trang", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "Limit phải >= 1")
    @Max(value = 100, message = "Limit phải <= 100")
    private Integer limit = 10;

    /**
     * Từ khóa tìm kiếm (tìm trong tên, email, số điện thoại)
     */
    @Schema(description = "Từ khóa tìm kiếm (tìm trong tên, email, số điện thoại)", example = "Nguyen Van A")
    private String searchTerm;

    /**
     * Lọc theo giới tính
     */
    @Schema(description = "Lọc theo giới tính", example = "MALE", allowableValues = { "MALE", "FEMALE" })
    private Gender gender;

    /**
     * Lọc theo loại khách hàng
     */
    @Schema(description = "Lọc theo loại khách hàng", example = "VIP", allowableValues = { "REGULAR", "VIP" })
    private CustomerType customerType;

    /**
     * Trường sắp xếp
     * Các trường từ User: name, email, phone, createdAt, updatedAt, dateOfBirth, gender
     * Các trường từ Customer: customerType, address, customerCode, customerId
     */
    @Schema(description = "Trường sắp xếp", example = "createdAt", allowableValues = { "name", "email", "phone",
            "createdAt", "updatedAt", "dateOfBirth", "gender", "customerType", "address", "customerCode", "customerId" })
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp
     */
    @Schema(description = "Hướng sắp xếp", example = "desc", allowableValues = { "asc", "desc" })
    private String sortDirection = "desc";

    /**
     * Kiểm tra xem có từ khóa tìm kiếm không
     */
    @JsonIgnore
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }

    /**
     * Kiểm tra xem có lọc theo giới tính không
     */
    @JsonIgnore
    public boolean hasGenderFilter() {
        return gender != null;
    }

    /**
     * Kiểm tra xem có lọc theo loại khách hàng không
     */
    @JsonIgnore
    public boolean hasCustomerTypeFilter() {
        return customerType != null;
    }

    /**
     * Lấy từ khóa tìm kiếm đã được trim
     */
    @JsonIgnore
    public String getTrimmedSearchTerm() {
        return hasSearchTerm() ? searchTerm.trim() : null;
    }
}
