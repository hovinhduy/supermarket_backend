package iuh.fit.supermarket.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

/**
 * Utility class cho việc validate dữ liệu khách hàng
 */
@Component
public class CustomerValidator {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\d{10}$");

    private static final Pattern VIETNAMESE_PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-6|8|9]|9[0-4|6-9])[0-9]{7}$");

    /**
     * Validate email format
     * 
     * @param email email cần validate
     * @return true nếu email hợp lệ
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate phone number format
     * 
     * @param phone số điện thoại cần validate
     * @return true nếu số điện thoại hợp lệ (chỉ 10 số)
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        // Remove non-digit characters
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate Vietnamese phone number format
     * 
     * @param phone số điện thoại cần validate
     * @return true nếu số điện thoại Việt Nam hợp lệ
     */
    public boolean isValidVietnamesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return true; // Phone is optional
        }
        String cleanPhone = phone.replaceAll("[\\s\\-()]", "");
        return VIETNAMESE_PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate customer name
     * 
     * @param name tên khách hàng
     * @return true nếu tên hợp lệ
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmedName = name.trim();
        return trimmedName.length() >= 2 && trimmedName.length() <= 100;
    }

    /**
     * Validate date of birth
     * 
     * @param dateOfBirth ngày sinh
     * @return true nếu ngày sinh hợp lệ
     */
    public boolean isValidDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return true; // Date of birth is optional
        }

        LocalDate now = LocalDate.now();

        // Check if date is in the past
        if (!dateOfBirth.isBefore(now)) {
            return false;
        }

        // Check if age is reasonable (between 0 and 150 years)
        int age = Period.between(dateOfBirth, now).getYears();
        return age >= 0 && age <= 150;
    }

    /**
     * Validate password strength
     * 
     * @param password mật khẩu cần validate
     * @return true nếu mật khẩu đủ mạnh
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // At least 6 characters
        if (password.length() < 6) {
            return false;
        }

        // At most 50 characters
        if (password.length() > 50) {
            return false;
        }

        return true;
    }

    /**
     * Validate password strength (advanced)
     * 
     * @param password mật khẩu cần validate
     * @return true nếu mật khẩu đủ mạnh
     */
    public boolean isStrongPassword(String password) {
        if (!isValidPassword(password)) {
            return false;
        }

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }

        // Require at least 3 out of 4 character types
        int typeCount = 0;
        if (hasLower)
            typeCount++;
        if (hasUpper)
            typeCount++;
        if (hasDigit)
            typeCount++;
        if (hasSpecial)
            typeCount++;

        return typeCount >= 3;
    }

    /**
     * Validate address
     * 
     * @param address địa chỉ cần validate
     * @return true nếu địa chỉ hợp lệ
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return true; // Address is optional
        }
        return address.trim().length() <= 255;
    }

    /**
     * Validate customer age for VIP eligibility
     * 
     * @param dateOfBirth ngày sinh
     * @return true nếu khách hàng đủ tuổi để trở thành VIP (>= 18)
     */
    public boolean isEligibleForVip(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return false;
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= 18;
    }

    /**
     * Normalize phone number (remove spaces, dashes, parentheses)
     * 
     * @param phone số điện thoại cần normalize
     * @return số điện thoại đã được normalize
     */
    public String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[\\s\\-()]", "");
    }

    /**
     * Normalize email (trim and lowercase)
     * 
     * @param email email cần normalize
     * @return email đã được normalize
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validate passwords match
     * 
     * @param password        mật khẩu
     * @param confirmPassword xác nhận mật khẩu
     * @return true nếu hai mật khẩu khớp nhau
     */
    public boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }
}
