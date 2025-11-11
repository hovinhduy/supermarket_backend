package iuh.fit.supermarket.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation để validate ngày bắt đầu và ngày kết thúc của promotion
 * - Ngày kết thúc phải lớn hơn ngày bắt đầu
 * - Ngày kết thúc phải lớn hơn ngày hiện tại
 */
@Documented
@Constraint(validatedBy = PromotionDatesValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPromotionDates {

    String message() default "Ngày kết thúc phải lớn hơn ngày bắt đầu và lớn hơn ngày hiện tại";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
