package iuh.fit.supermarket.validation;

import iuh.fit.supermarket.entity.PromotionLine;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Validator để kiểm tra tính hợp lệ của ngày bắt đầu và ngày kết thúc
 */
public class PromotionDatesValidator implements ConstraintValidator<ValidPromotionDates, PromotionLine> {

    @Override
    public void initialize(ValidPromotionDates constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    /**
     * Kiểm tra tính hợp lệ của ngày
     * @param promotionLine đối tượng promotion line cần validate
     * @param context context của validator
     * @return true nếu hợp lệ, false nếu không hợp lệ
     */
    @Override
    public boolean isValid(PromotionLine promotionLine, ConstraintValidatorContext context) {
        // Nếu các trường null thì để các validator khác xử lý (@NotNull)
        if (promotionLine == null || promotionLine.getStartDate() == null || promotionLine.getEndDate() == null) {
            return true;
        }

        LocalDate startDate = promotionLine.getStartDate();
        LocalDate endDate = promotionLine.getEndDate();
        LocalDate today = LocalDate.now();

        // Kiểm tra ngày kết thúc phải lớn hơn ngày bắt đầu
        if (!endDate.isAfter(startDate)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Ngày kết thúc phải lớn hơn ngày bắt đầu")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        // Kiểm tra ngày kết thúc phải lớn hơn ngày hiện tại
        if (!endDate.isAfter(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Ngày kết thúc phải lớn hơn ngày hiện tại")
                    .addPropertyNode("endDate")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
