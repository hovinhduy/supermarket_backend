package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa các vai trò nhân viên trong hệ thống
 * Tương ứng với cột role trong bảng employees
 */
public enum EmployeeRole {
    /**
     * Quản trị viên hệ thống
     */
    ADMIN("Admin"),
    
    /**
     * Quản lý cửa hàng
     */
    MANAGER("Manager"),
    
    /**
     * Nhân viên bán hàng
     */
    STAFF("Staff");
    
    private final String value;
    
    /**
     * Constructor cho EmployeeRole
     * @param value giá trị string tương ứng trong database
     */
    EmployeeRole(String value) {
        this.value = value;
    }
    
    /**
     * Lấy giá trị string của enum
     * @return giá trị string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Chuyển đổi từ string sang enum
     * @param value giá trị string
     * @return EmployeeRole tương ứng
     */
    public static EmployeeRole fromValue(String value) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown EmployeeRole: " + value);
    }
}
