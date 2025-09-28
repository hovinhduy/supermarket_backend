package iuh.fit.supermarket.dto.customer;

import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO cho Customer để tránh lazy loading issues
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    /**
     * ID duy nhất của khách hàng
     */
    private Integer customerId;

    /**
     * Tên khách hàng
     */
    private String name;

    /**
     * Email khách hàng
     */
    private String email;

    /**
     * Số điện thoại khách hàng
     */
    private String phone;

    /**
     * Giới tính
     */
    private Gender gender;

    /**
     * Địa chỉ khách hàng
     */
    private String address;

    /**
     * Ngày sinh khách hàng
     */
    private LocalDate dateOfBirth;

    /**
     * Loại khách hàng (Regular/VIP)
     */
    private CustomerType customerType;

    /**
     * Trạng thái xóa mềm
     */
    private Boolean isDeleted;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;

    /**
     * Constructor từ Customer entity
     */
    public static CustomerDto fromEntity(iuh.fit.supermarket.entity.Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return new CustomerDto(
            customer.getCustomerId(),
            customer.getName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getGender(),
            customer.getAddress(),
            customer.getDateOfBirth(),
            customer.getCustomerType(),
            customer.getIsDeleted(),
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}
