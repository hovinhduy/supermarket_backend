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
     * Mã khách hàng
     */
    private String customerCode;

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
     * Sau refactoring: lấy name, email, phone, gender, dateOfBirth, isDeleted từ User entity
     */
    public static CustomerDto fromEntity(iuh.fit.supermarket.entity.Customer customer) {
        if (customer == null) {
            return null;
        }

        // Lấy thông tin từ User entity
        var user = customer.getUser();

        return new CustomerDto(
            customer.getCustomerId(),
            user.getName(),           // Từ User
            user.getEmail(),          // Từ User
            user.getPhone(),          // Từ User
            customer.getCustomerCode(),
            user.getGender(),         // Từ User
            customer.getAddress(),
            user.getDateOfBirth(),    // Từ User
            customer.getCustomerType(),
            user.getIsDeleted(),      // Từ User
            user.getCreatedAt(),      // Từ User
            user.getUpdatedAt()       // Từ User
        );
    }
}
