package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.customer.*;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.util.CustomerValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho Customer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerValidator customerValidator;

    /**
     * Lấy tất cả khách hàng chưa bị xóa
     * 
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getAllCustomers() {
        log.debug("Lấy danh sách tất cả khách hàng");
        return customerRepository.findAllByIsDeletedFalse()
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy khách hàng với phân trang
     * 
     * @param page          số trang
     * @param size          kích thước trang
     * @param sortBy        trường sắp xếp
     * @param sortDirection hướng sắp xếp
     * @return Page<CustomerDto>
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto> getCustomersWithPagination(int page, int size, String sortBy, String sortDirection) {
        log.debug("Lấy danh sách khách hàng với phân trang: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return customerRepository.findAllByIsDeletedFalse(pageable)
                .map(CustomerDto::fromEntity);
    }

    /**
     * Lấy khách hàng theo ID
     * 
     * @param customerId ID khách hàng
     * @return CustomerDto
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Integer customerId) {
        log.debug("Lấy khách hàng với ID: {}", customerId);

        Customer customer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Lấy khách hàng theo email
     * 
     * @param email email khách hàng
     * @return CustomerDto
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.debug("Lấy khách hàng với email: {}", email);

        String normalizedEmail = customerValidator.normalizeEmail(email);
        Customer customer = customerRepository.findByEmailAndIsDeletedFalse(normalizedEmail)
                .orElseThrow(() -> new CustomerNotFoundException("email", email));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Lấy khách hàng theo số điện thoại
     * 
     * @param phone số điện thoại khách hàng
     * @return CustomerDto
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        log.debug("Lấy khách hàng với phone: {}", phone);

        String normalizedPhone = customerValidator.normalizePhone(phone);
        Customer customer = customerRepository.findByPhoneAndIsDeletedFalse(normalizedPhone)
                .orElseThrow(() -> new CustomerNotFoundException("phone", phone));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Đăng ký khách hàng mới (self-registration)
     * 
     * @param request thông tin đăng ký khách hàng
     * @return CustomerDto
     */
    @Transactional
    public CustomerDto registerCustomer(RegisterCustomerRequest request) {
        log.info("Đăng ký khách hàng mới với email: {}, phone: {}", request.getEmail(), request.getPhone());

        // Validate dữ liệu đầu vào
        validateRegisterCustomerRequest(request);

        // Normalize dữ liệu
        String normalizedEmail = customerValidator.normalizeEmail(request.getEmail());
        String normalizedPhone = customerValidator.normalizePhone(request.getPhone());

        // Kiểm tra email đã tồn tại chưa
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        Optional<Customer> existingCustomerByPhone = customerRepository.findByPhone(normalizedPhone);

        if (existingCustomerByPhone.isPresent()) {
            Customer existingCustomer = existingCustomerByPhone.get();

            // Nếu khách hàng đã có mật khẩu -> conflict
            if (existingCustomer.getPasswordHash() != null && !existingCustomer.getPasswordHash().isEmpty()) {
                throw new DuplicateCustomerException("phone", normalizedPhone + " (đã đăng ký)");
            }

            // Nếu khách hàng chưa có mật khẩu -> cập nhật thông tin
            log.info("Cập nhật khách hàng hiện có với phone: {} bằng cách thêm mật khẩu", normalizedPhone);

            existingCustomer.setName(request.getName().trim());
            existingCustomer.setEmail(normalizedEmail);
            existingCustomer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existingCustomer.setGender(request.getGender());
            existingCustomer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
            existingCustomer.setDateOfBirth(request.getDateOfBirth());
            // Giữ nguyên customerType hiện có

            Customer savedCustomer = customerRepository.save(existingCustomer);
            log.info("Đã cập nhật khách hàng với ID: {}", savedCustomer.getCustomerId());

            return CustomerDto.fromEntity(savedCustomer);
        }

        // Tạo khách hàng mới
        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(normalizedPhone);
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerType(CustomerType.REGULAR); // Luôn là REGULAR cho self-registration
        customer.setIsDeleted(false);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Đã tạo khách hàng mới với ID: {}", savedCustomer.getCustomerId());

        return CustomerDto.fromEntity(savedCustomer);
    }

    /**
     * Tạo khách hàng mới bởi admin (không yêu cầu mật khẩu)
     * 
     * @param request thông tin khách hàng
     * @return CustomerDto
     */
    @Transactional
    public CustomerDto createCustomerByAdmin(CreateCustomerRequest request) {
        log.info("Admin tạo khách hàng mới với email: {}", request.getEmail());

        // Validate dữ liệu đầu vào
        validateCreateCustomerRequest(request);

        // Normalize dữ liệu
        String normalizedEmail = customerValidator.normalizeEmail(request.getEmail());
        String normalizedPhone = customerValidator.normalizePhone(request.getPhone());

        // Kiểm tra email đã tồn tại chưa
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (nếu có)
        if (normalizedPhone != null && !normalizedPhone.isEmpty() &&
                customerRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicateCustomerException("phone", normalizedPhone);
        }

        // Tạo entity Customer
        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(normalizedEmail);
        customer.setPhone(normalizedPhone);
        customer.setPasswordHash(null); // Không có mật khẩu khi admin tạo
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setCustomerType(request.getCustomerType() != null ? request.getCustomerType() : CustomerType.REGULAR);
        customer.setIsDeleted(false);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Đã tạo khách hàng mới với ID: {}", savedCustomer.getCustomerId());

        return CustomerDto.fromEntity(savedCustomer);
    }

    /**
     * Cập nhật thông tin khách hàng
     * 
     * @param customerId ID khách hàng
     * @param request    thông tin cập nhật
     * @return CustomerDto
     */
    @Transactional
    public CustomerDto updateCustomer(Integer customerId, UpdateCustomerRequest request) {
        log.info("Cập nhật khách hàng với ID: {}", customerId);

        // Validate dữ liệu đầu vào
        validateUpdateCustomerRequest(request);

        Customer existingCustomer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Normalize dữ liệu
        String normalizedEmail = customerValidator.normalizeEmail(request.getEmail());
        String normalizedPhone = customerValidator.normalizePhone(request.getPhone());

        // Kiểm tra email đã tồn tại chưa (ngoại trừ khách hàng hiện tại)
        if (!existingCustomer.getEmail().equals(normalizedEmail) &&
                customerRepository.existsByEmailAndCustomerIdNot(normalizedEmail, customerId)) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (ngoại trừ khách hàng hiện tại)
        if (normalizedPhone != null && !normalizedPhone.isEmpty() &&
                !normalizedPhone.equals(existingCustomer.getPhone()) &&
                customerRepository.existsByPhoneAndCustomerIdNot(normalizedPhone, customerId)) {
            throw new DuplicateCustomerException("phone", normalizedPhone);
        }

        // Cập nhật thông tin
        existingCustomer.setName(request.getName().trim());
        existingCustomer.setEmail(normalizedEmail);
        existingCustomer.setPhone(normalizedPhone);
        existingCustomer.setGender(request.getGender());
        existingCustomer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        existingCustomer.setDateOfBirth(request.getDateOfBirth());

        if (request.getCustomerType() != null) {
            existingCustomer.setCustomerType(request.getCustomerType());
        }

        Customer savedCustomer = customerRepository.save(existingCustomer);
        log.info("Đã cập nhật khách hàng với ID: {}", savedCustomer.getCustomerId());

        return CustomerDto.fromEntity(savedCustomer);
    }

    /**
     * Xóa mềm khách hàng
     * 
     * @param customerId ID khách hàng
     */
    @Transactional
    public void deleteCustomer(Integer customerId) {
        log.info("Xóa khách hàng với ID: {}", customerId);

        Customer customer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setIsDeleted(true);
        customerRepository.save(customer);

        log.info("Đã xóa khách hàng với ID: {}", customerId);
    }

    /**
     * Khôi phục khách hàng đã bị xóa
     * 
     * @param customerId ID khách hàng
     */
    @Transactional
    public void restoreCustomer(Integer customerId) {
        log.info("Khôi phục khách hàng với ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setIsDeleted(false);
        customerRepository.save(customer);

        log.info("Đã khôi phục khách hàng với ID: {}", customerId);
    }

    /**
     * Đổi mật khẩu khách hàng
     * 
     * @param customerId ID khách hàng
     * @param request    yêu cầu đổi mật khẩu
     */
    @Transactional
    public void changePassword(Integer customerId, ChangePasswordRequest request) {
        log.info("Đổi mật khẩu cho khách hàng với ID: {}", customerId);

        // Validate passwords match
        if (!customerValidator.passwordsMatch(request.getNewPassword(), request.getConfirmPassword())) {
            throw new CustomerValidationException("confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        // Validate new password
        if (!customerValidator.isValidPassword(request.getNewPassword())) {
            throw new CustomerValidationException("newPassword", "Mật khẩu mới phải từ 6 đến 50 ký tự");
        }

        Customer customer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), customer.getPasswordHash())) {
            throw new CustomerValidationException("oldPassword", "Mật khẩu cũ không đúng");
        }

        customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        customerRepository.save(customer);

        log.info("Đã đổi mật khẩu cho khách hàng với ID: {}", customerId);
    }

    /**
     * Tìm kiếm khách hàng theo nhiều tiêu chí
     * 
     * @param request yêu cầu tìm kiếm
     * @return Page<CustomerDto>
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomers(CustomerSearchRequest request) {
        log.debug("Tìm kiếm khách hàng với từ khóa: {}, loại: {}", request.getSearchTerm(), request.getCustomerType());

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<Customer> customers;

        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            if (request.getCustomerType() != null) {
                // Search with both term and customer type
                customers = customerRepository.searchCustomers(request.getSearchTerm().trim(), pageable)
                        .map(customer -> customer.getCustomerType() == request.getCustomerType() ? customer : null)
                        .map(customer -> customer);
            } else {
                // Search with term only
                customers = customerRepository.searchCustomers(request.getSearchTerm().trim(), pageable);
            }
        } else if (request.getCustomerType() != null) {
            // Filter by customer type only
            customers = customerRepository.findByCustomerTypeAndIsDeletedFalse(request.getCustomerType(), pageable);
        } else {
            // Get all customers
            customers = customerRepository.findAllByIsDeletedFalse(pageable);
        }

        return customers.map(CustomerDto::fromEntity);
    }

    /**
     * Tìm kiếm khách hàng theo tên
     * 
     * @param name tên khách hàng
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomersByName(String name) {
        log.debug("Tìm kiếm khách hàng với tên: {}", name);
        return customerRepository.findByNameContainingAndIsDeletedFalse(name)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm khách hàng theo email
     * 
     * @param email email khách hàng
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomersByEmail(String email) {
        log.debug("Tìm kiếm khách hàng với email: {}", email);
        return customerRepository.findByEmailContainingAndIsDeletedFalse(email)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm khách hàng theo số điện thoại
     * 
     * @param phone số điện thoại khách hàng
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> searchCustomersByPhone(String phone) {
        log.debug("Tìm kiếm khách hàng với phone: {}", phone);
        return customerRepository.findByPhoneContainingAndIsDeletedFalse(phone)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy khách hàng theo loại
     * 
     * @param customerType loại khách hàng
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersByType(CustomerType customerType) {
        log.debug("Lấy danh sách khách hàng với loại: {}", customerType);
        return customerRepository.findByCustomerTypeAndIsDeletedFalse(customerType)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lấy khách hàng VIP
     * 
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getVipCustomers() {
        log.debug("Lấy danh sách khách hàng VIP");
        return customerRepository.findVipCustomers()
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Nâng cấp khách hàng lên VIP
     * 
     * @param customerId ID khách hàng
     */
    @Transactional
    public void upgradeToVip(Integer customerId) {
        log.info("Nâng cấp khách hàng lên VIP với ID: {}", customerId);

        Customer customer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Check if customer is eligible for VIP
        if (customer.getDateOfBirth() != null && !customerValidator.isEligibleForVip(customer.getDateOfBirth())) {
            throw new CustomerValidationException("age", "Khách hàng phải từ 18 tuổi trở lên để trở thành VIP");
        }

        customer.setCustomerType(CustomerType.VIP);
        customerRepository.save(customer);

        log.info("Đã nâng cấp khách hàng lên VIP với ID: {}", customerId);
    }

    /**
     * Hạ cấp khách hàng xuống REGULAR
     * 
     * @param customerId ID khách hàng
     */
    @Transactional
    public void downgradeToRegular(Integer customerId) {
        log.info("Hạ cấp khách hàng xuống REGULAR với ID: {}", customerId);

        Customer customer = customerRepository.findByCustomerIdAndIsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setCustomerType(CustomerType.REGULAR);
        customerRepository.save(customer);

        log.info("Đã hạ cấp khách hàng xuống REGULAR với ID: {}", customerId);
    }

    /**
     * Đếm số lượng khách hàng theo loại
     * 
     * @param customerType loại khách hàng
     * @return số lượng khách hàng
     */
    @Transactional(readOnly = true)
    public long countCustomersByType(CustomerType customerType) {
        log.debug("Đếm số lượng khách hàng với loại: {}", customerType);
        return customerRepository.countByCustomerTypeAndIsDeletedFalse(customerType);
    }

    /**
     * Tìm khách hàng sinh nhật trong khoảng thời gian
     * 
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @return List<CustomerDto>
     */
    @Transactional(readOnly = true)
    public List<CustomerDto> getCustomersWithBirthdayBetween(LocalDate startDate, LocalDate endDate) {
        log.debug("Tìm khách hàng sinh nhật từ {} đến {}", startDate, endDate);
        return customerRepository.findCustomersWithBirthdayBetween(startDate, endDate)
                .stream()
                .map(CustomerDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Validate RegisterCustomerRequest
     */
    private void validateRegisterCustomerRequest(RegisterCustomerRequest request) {
        if (!customerValidator.isValidName(request.getName())) {
            throw new CustomerValidationException("name", "Tên khách hàng không hợp lệ");
        }

        if (!customerValidator.isValidEmail(request.getEmail())) {
            throw new CustomerValidationException("email", "Email không đúng định dạng");
        }

        if (!customerValidator.isValidPhone(request.getPhone())) {
            throw new CustomerValidationException("phone", "Số điện thoại không đúng định dạng");
        }

        if (!customerValidator.isValidPassword(request.getPassword())) {
            throw new CustomerValidationException("password", "Mật khẩu phải từ 6 đến 50 ký tự");
        }

        if (!customerValidator.passwordsMatch(request.getPassword(), request.getConfirmPassword())) {
            throw new CustomerValidationException("confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        if (!customerValidator.isValidDateOfBirth(request.getDateOfBirth())) {
            throw new CustomerValidationException("dateOfBirth", "Ngày sinh không hợp lệ");
        }

        if (!customerValidator.isValidAddress(request.getAddress())) {
            throw new CustomerValidationException("address", "Địa chỉ không được vượt quá 255 ký tự");
        }
    }

    /**
     * Validate CreateCustomerRequest (for admin creation - no password required)
     */
    private void validateCreateCustomerRequest(CreateCustomerRequest request) {
        if (!customerValidator.isValidName(request.getName())) {
            throw new CustomerValidationException("name", "Tên khách hàng không hợp lệ");
        }

        if (!customerValidator.isValidEmail(request.getEmail())) {
            throw new CustomerValidationException("email", "Email không đúng định dạng");
        }

        if (request.getPhone() != null && !customerValidator.isValidPhone(request.getPhone())) {
            throw new CustomerValidationException("phone", "Số điện thoại không đúng định dạng");
        }

        if (!customerValidator.isValidDateOfBirth(request.getDateOfBirth())) {
            throw new CustomerValidationException("dateOfBirth", "Ngày sinh không hợp lệ");
        }

        if (!customerValidator.isValidAddress(request.getAddress())) {
            throw new CustomerValidationException("address", "Địa chỉ không được vượt quá 255 ký tự");
        }
    }

    /**
     * Validate UpdateCustomerRequest
     */
    private void validateUpdateCustomerRequest(UpdateCustomerRequest request) {
        if (!customerValidator.isValidName(request.getName())) {
            throw new CustomerValidationException("name", "Tên khách hàng không hợp lệ");
        }

        if (!customerValidator.isValidEmail(request.getEmail())) {
            throw new CustomerValidationException("email", "Email không đúng định dạng");
        }

        if (request.getPhone() != null && !customerValidator.isValidPhone(request.getPhone())) {
            throw new CustomerValidationException("phone", "Số điện thoại không đúng định dạng");
        }

        if (!customerValidator.isValidDateOfBirth(request.getDateOfBirth())) {
            throw new CustomerValidationException("dateOfBirth", "Ngày sinh không hợp lệ");
        }

        if (!customerValidator.isValidAddress(request.getAddress())) {
            throw new CustomerValidationException("address", "Địa chỉ không được vượt quá 255 ký tự");
        }
    }
}
