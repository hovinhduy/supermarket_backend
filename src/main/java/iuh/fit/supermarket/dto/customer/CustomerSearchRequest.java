package iuh.fit.supermarket.dto.customer;

import iuh.fit.supermarket.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu tìm kiếm khách hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSearchRequest {

    /**
     * Từ khóa tìm kiếm (tên, email, hoặc số điện thoại)
     */
    private String searchTerm;

    /**
     * Loại khách hàng để lọc
     */
    private CustomerType customerType;

    /**
     * Số trang (bắt đầu từ 0)
     */
    private Integer page = 0;

    /**
     * Kích thước trang
     */
    private Integer size = 10;

    /**
     * Trường sắp xếp
     */
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp (asc/desc)
     */
    private String sortDirection = "desc";
}
