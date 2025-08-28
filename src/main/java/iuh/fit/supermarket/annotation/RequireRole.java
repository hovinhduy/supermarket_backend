package iuh.fit.supermarket.annotation;

import iuh.fit.supermarket.enums.EmployeeRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để kiểm tra quyền truy cập dựa trên role
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * Các role được phép truy cập
     */
    EmployeeRole[] value();
    
    /**
     * Thông báo lỗi khi không có quyền
     */
    String message() default "Bạn không có quyền truy cập chức năng này";
}
