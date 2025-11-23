package iuh.fit.supermarket.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông tin user hiện tại từ token
 * Hỗ trợ cả nhân viên (Employee) và khách hàng (Customer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoResponse {

    /**
     * Loại user: EMPLOYEE hoặc CUSTOMER
     */
    private String userType;

    /**
     * ID của employee (nếu là nhân viên)
     */
    private Integer employeeId;

    /**
     * ID của customer (nếu là khách hàng)
     */
    private Integer customerId;

    /**
     * Tên đầy đủ
     */
    private String name;

    /**
     * Email
     */
    private String email;

    /**
     * Số điện thoại (chỉ có cho customer)
     */
    private String phone;

    /**
     * Vai trò trong hệ thống (ADMIN, MANAGER, STAFF cho employee; CUSTOMER cho khách hàng)
     */
    private UserRole userRole;

    /**
     * Loại khách hàng (VIP, REGULAR) - chỉ có cho customer
     */
    private CustomerType customerType;

    /**
     * Địa chỉ khách hàng - chỉ có cho customer
     */
    private String address;

    /**
     * Constructor cho Employee
     */
    public static UserInfoResponse forEmployee(Integer employeeId, String name, String email, UserRole userRole) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserType("EMPLOYEE");
        response.setEmployeeId(employeeId);
        response.setName(name);
        response.setEmail(email);
        response.setUserRole(userRole);
        return response;
    }

    /**
     * Constructor cho Customer
     */
    public static UserInfoResponse forCustomer(Integer customerId, String name, String email, String phone, CustomerType customerType, String address) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserType("CUSTOMER");
        response.setCustomerId(customerId);
        response.setName(name);
        response.setEmail(email);
        response.setPhone(phone);
        response.setUserRole(UserRole.CUSTOMER);
        response.setCustomerType(customerType);
        response.setAddress(address);
        return response;
    }
}
