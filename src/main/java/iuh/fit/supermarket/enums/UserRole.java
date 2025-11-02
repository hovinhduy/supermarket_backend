package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa các vai trò của user trong hệ thống
 * Bao gồm cả employee roles (ADMIN, MANAGER, STAFF) và customer role (CUSTOMER)
 */
public enum UserRole {
    /**
     * Quản trị viên - có quyền cao nhất trong hệ thống
     */
    ADMIN,

    /**
     * Quản lý - có quyền quản lý nhân viên và các chức năng nghiệp vụ
     */
    MANAGER,

    /**
     * Nhân viên - thực hiện các nghiệp vụ bán hàng, nhập hàng
     */
    STAFF,

    /**
     * Khách hàng - người dùng cuối sử dụng hệ thống để mua hàng
     */
    CUSTOMER
}
