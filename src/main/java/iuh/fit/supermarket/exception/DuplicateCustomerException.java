package iuh.fit.supermarket.exception;

/**
 * Exception khi khách hàng đã tồn tại (email hoặc số điện thoại trùng)
 */
public class DuplicateCustomerException extends CustomerException {

    public DuplicateCustomerException(String message) {
        super(message);
    }

    public DuplicateCustomerException(String field, String value) {
        super("Khách hàng với " + field + " '" + value + "' đã tồn tại trong hệ thống");
    }
}
