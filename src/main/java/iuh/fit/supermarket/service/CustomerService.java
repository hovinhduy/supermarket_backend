package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.customer.*;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.UserRole;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho Customer
 * Sau refactoring: tạo User trước, sau đó tạo Customer với user_id FK
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
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
        return customerRepository.findAllByUser_IsDeletedFalse()
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

        // Map các field của User cần thêm prefix "user."
        String sortField = sortBy;
        if ("name".equals(sortField) || "email".equals(sortField) ||
            "phone".equals(sortField) || "createdAt".equals(sortField) ||
            "updatedAt".equals(sortField) || "dateOfBirth".equals(sortField) ||
            "gender".equals(sortField)) {
            sortField = "user." + sortField;
        } else if ("customerType".equals(sortField) || "address".equals(sortField) ||
                   "customerCode".equals(sortField) || "customerId".equals(sortField)) {
            // Các field này thuộc Customer, giữ nguyên
        } else {
            // Mặc định sort theo user.createdAt nếu field không hợp lệ
            sortField = "user.createdAt";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        return customerRepository.findAllByUser_IsDeletedFalse(pageable)
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

        Customer customer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Lấy khách hàng theo email
     * Sau refactoring: tìm User trước, sau đó lấy Customer
     *
     * @param email email khách hàng
     * @return CustomerDto
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.debug("Lấy khách hàng với email: {}", email);

        String normalizedEmail = customerValidator.normalizeEmail(email);

        // Tìm User với role CUSTOMER
        User user = userRepository.findCustomerByEmailOrPhone(normalizedEmail)
                .orElseThrow(() -> new CustomerNotFoundException("email", email));

        // Lấy Customer từ user_id
        Customer customer = customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> new CustomerNotFoundException("email", email));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Lấy khách hàng theo số điện thoại
     * Sau refactoring: tìm User trước, sau đó lấy Customer
     *
     * @param phone số điện thoại khách hàng
     * @return CustomerDto
     */
    @Transactional(readOnly = true)
    public CustomerDto getCustomerByPhone(String phone) {
        log.debug("Lấy khách hàng với phone: {}", phone);

        String normalizedPhone = customerValidator.normalizePhone(phone);

        // Tìm User với role CUSTOMER
        User user = userRepository.findCustomerByEmailOrPhone(normalizedPhone)
                .orElseThrow(() -> new CustomerNotFoundException("phone", phone));

        // Lấy Customer từ user_id
        Customer customer = customerRepository.findByUser_UserIdAndUser_IsDeletedFalse(user.getUserId())
                .orElseThrow(() -> new CustomerNotFoundException("phone", phone));

        return CustomerDto.fromEntity(customer);
    }

    /**
     * Tự động sinh mã khách hàng mới theo định dạng KH000001 đến KH999999
     * 
     * @return mã khách hàng mới
     */
    @Transactional(readOnly = true)
    public String generateCustomerCode() {
        Optional<Customer> lastCustomer = customerRepository.findTopByOrderByCustomerCodeDesc();

        if (lastCustomer.isPresent() && lastCustomer.get().getCustomerCode() != null) {
            String lastCode = lastCustomer.get().getCustomerCode();
            // Trích xuất số từ mã (KH000001 -> 1)
            int lastNumber = Integer.parseInt(lastCode.substring(2));
            
            // Kiểm tra giới hạn
            if (lastNumber >= 999999) {
                throw new CustomerValidationException("customerCode", "Đã hết mã khách hàng có thể tạo (KH999999)");
            }
            
            // Tạo mã mới
            int newNumber = lastNumber + 1;
            return String.format("KH%06d", newNumber);
        }

        // Nếu chưa có mã nào, bắt đầu từ KH000001
        return "KH000001";
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

        // Xử lý mã khách hàng
        String customerCode = request.getCustomerCode();
        if (customerCode == null || customerCode.trim().isEmpty()) {
            customerCode = generateCustomerCode();
            log.info("Tự động sinh mã khách hàng: {}", customerCode);
        } else {
            customerCode = customerCode.trim();
            // Kiểm tra mã đã tồn tại chưa
            if (customerRepository.existsByCustomerCode(customerCode)) {
                throw new DuplicateCustomerException("customerCode", customerCode);
            }
        }

        // Kiểm tra email đã tồn tại chưa (trong UserRepository)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (tìm User theo phone)
        Optional<User> existingUserByPhone = userRepository.findByPhoneAndIsDeletedFalse(normalizedPhone);

        if (existingUserByPhone.isPresent()) {
            User existingUser = existingUserByPhone.get();

            // Nếu user đã có mật khẩu -> conflict
            if (existingUser.getPasswordHash() != null && !existingUser.getPasswordHash().isEmpty()) {
                throw new DuplicateCustomerException("phone", normalizedPhone + " (đã đăng ký)");
            }

            // Nếu user chưa có mật khẩu -> cập nhật thông tin User
            log.info("Cập nhật user hiện có với phone: {} bằng cách thêm mật khẩu", normalizedPhone);

            existingUser.setName(request.getName().trim());
            existingUser.setEmail(normalizedEmail);
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            existingUser.setGender(request.getGender());
            existingUser.setDateOfBirth(request.getDateOfBirth());

            User savedUser = userRepository.save(existingUser);

            // Lấy Customer từ user_id và cập nhật customerCode, address
            Customer existingCustomer = customerRepository.findByUser_UserId(savedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy customer record"));

            existingCustomer.setCustomerCode(customerCode);
            existingCustomer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

            Customer savedCustomer = customerRepository.save(existingCustomer);
            log.info("Đã cập nhật khách hàng với ID: {}", savedCustomer.getCustomerId());

            return CustomerDto.fromEntity(savedCustomer);
        }

        // Tạo User mới
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(UserRole.CUSTOMER);
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);
        log.info("Đã tạo User mới với ID: {}", savedUser.getUserId());

        // Tạo Customer mới liên kết với User
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setCustomerCode(customerCode);
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setCustomerType(CustomerType.REGULAR); // Luôn là REGULAR cho self-registration

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

        // Xử lý mã khách hàng
        String customerCode = request.getCustomerCode();
        if (customerCode == null || customerCode.trim().isEmpty()) {
            customerCode = generateCustomerCode();
            log.info("Tự động sinh mã khách hàng: {}", customerCode);
        } else {
            customerCode = customerCode.trim();
            // Kiểm tra mã đã tồn tại chưa
            if (customerRepository.existsByCustomerCode(customerCode)) {
                throw new DuplicateCustomerException("customerCode", customerCode);
            }
        }

        // Kiểm tra email đã tồn tại chưa (trong UserRepository)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (nếu có)
        if (normalizedPhone != null && !normalizedPhone.isEmpty() &&
                userRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicateCustomerException("phone", normalizedPhone);
        }

        // Tạo User mới (admin tạo -> không có password)
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setPasswordHash(null); // Không có mật khẩu khi admin tạo
        user.setUserRole(UserRole.CUSTOMER);
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);
        log.info("Đã tạo User mới với ID: {}", savedUser.getUserId());

        // Tạo Customer mới liên kết với User
        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setCustomerCode(customerCode);
        customer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        customer.setCustomerType(request.getCustomerType() != null ? request.getCustomerType() : CustomerType.REGULAR);

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

        Customer existingCustomer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        User existingUser = existingCustomer.getUser();

        // Normalize dữ liệu
        String normalizedEmail = customerValidator.normalizeEmail(request.getEmail());
        String normalizedPhone = customerValidator.normalizePhone(request.getPhone());

        // Kiểm tra email đã tồn tại chưa (ngoại trừ user hiện tại)
        if (!existingUser.getEmail().equals(normalizedEmail) &&
                userRepository.existsByEmailAndUserIdNot(normalizedEmail, existingUser.getUserId())) {
            throw new DuplicateCustomerException("email", normalizedEmail);
        }

        // Kiểm tra số điện thoại đã tồn tại chưa (ngoại trừ user hiện tại)
        if (normalizedPhone != null && !normalizedPhone.isEmpty() &&
                !normalizedPhone.equals(existingUser.getPhone()) &&
                userRepository.existsByPhoneAndUserIdNot(normalizedPhone, existingUser.getUserId())) {
            throw new DuplicateCustomerException("phone", normalizedPhone);
        }

        // Cập nhật thông tin User
        existingUser.setName(request.getName().trim());
        existingUser.setEmail(normalizedEmail);
        existingUser.setPhone(normalizedPhone);
        existingUser.setGender(request.getGender());
        existingUser.setDateOfBirth(request.getDateOfBirth());

        userRepository.save(existingUser);

        // Cập nhật thông tin Customer (chỉ address và customerType)
        existingCustomer.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

        if (request.getCustomerType() != null) {
            existingCustomer.setCustomerType(request.getCustomerType());
        }

        Customer savedCustomer = customerRepository.save(existingCustomer);
        log.info("Đã cập nhật khách hàng với ID: {}", savedCustomer.getCustomerId());

        return CustomerDto.fromEntity(savedCustomer);
    }

    /**
     * Xóa mềm khách hàng
     * Sau refactoring: xóa mềm User thay vì Customer
     *
     * @param customerId ID khách hàng
     */
    @Transactional
    public void deleteCustomer(Integer customerId) {
        log.info("Xóa khách hàng với ID: {}", customerId);

        Customer customer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Xóa mềm User (sẽ cascade sang Customer qua relationship)
        User user = customer.getUser();
        user.setIsDeleted(true);
        userRepository.save(user);

        log.info("Đã xóa khách hàng với ID: {}", customerId);
    }

    /**
     * Xóa mềm nhiều khách hàng cùng lúc
     *
     * @param request yêu cầu xóa nhiều khách hàng
     * @return BulkDeleteCustomersResponse
     */
    @Transactional
    public BulkDeleteCustomersResponse bulkDeleteCustomers(BulkDeleteCustomersRequest request) {
        log.info("Bắt đầu xóa nhiều khách hàng - Tổng số: {}", request.getIdsCount());

        // Validate request
        validateBulkDeleteRequest(request);

        // Loại bỏ ID trùng lặp
        request.removeDuplicates();

        List<Integer> successIds = new ArrayList<>();
        List<Integer> failedIds = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        // Xử lý từng ID
        for (Integer customerId : request.getCustomerIds()) {
            try {
                // Kiểm tra khách hàng có tồn tại và chưa bị xóa không
                Optional<Customer> customerOpt = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId);

                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    // Xóa mềm User
                    User user = customer.getUser();
                    user.setIsDeleted(true);
                    userRepository.save(user);

                    successIds.add(customerId);
                    log.debug("Đã xóa thành công khách hàng với ID: {}", customerId);
                } else {
                    failedIds.add(customerId);
                    errors.add("Khách hàng với ID " + customerId + " không tồn tại hoặc đã bị xóa");
                    log.warn("Không tìm thấy khách hàng với ID: {}", customerId);
                }
            } catch (Exception e) {
                failedIds.add(customerId);
                errors.add("Lỗi khi xóa khách hàng ID " + customerId + ": " + e.getMessage());
                log.error("Lỗi khi xóa khách hàng với ID: {}", customerId, e);
            }
        }

        // Tạo response
        BulkDeleteCustomersResponse response = BulkDeleteCustomersResponse.withErrors(
                request.getIdsCount(),
                successIds,
                failedIds,
                errors);

        log.info("Hoàn thành xóa nhiều khách hàng - Thành công: {}, Thất bại: {}",
                response.getSuccessCount(), response.getFailedCount());

        return response;
    }

    /**
     * Khôi phục khách hàng đã bị xóa
     * Sau refactoring: khôi phục User
     *
     * @param customerId ID khách hàng
     */
    @Transactional
    public void restoreCustomer(Integer customerId) {
        log.info("Khôi phục khách hàng với ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Khôi phục User
        User user = customer.getUser();
        user.setIsDeleted(false);
        userRepository.save(user);

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

        Customer customer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        User user = customer.getUser();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new CustomerValidationException("oldPassword", "Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

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

        // Map các field của User cần thêm prefix "user."
        String sortField = request.getSortBy();
        if ("name".equals(sortField) || "email".equals(sortField) ||
            "phone".equals(sortField) || "createdAt".equals(sortField) ||
            "updatedAt".equals(sortField) || "dateOfBirth".equals(sortField) ||
            "gender".equals(sortField)) {
            sortField = "user." + sortField;
        } else if ("customerType".equals(sortField) || "address".equals(sortField) ||
                   "customerCode".equals(sortField) || "customerId".equals(sortField)) {
            // Các field này thuộc Customer, giữ nguyên
        } else {
            // Mặc định sort theo user.createdAt nếu field không hợp lệ
            sortField = "user.createdAt";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), sortField);
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
            customers = customerRepository.findByCustomerTypeAndUser_IsDeletedFalse(request.getCustomerType(), pageable);
        } else {
            // Get all customers
            customers = customerRepository.findAllByUser_IsDeletedFalse(pageable);
        }

        return customers.map(CustomerDto::fromEntity);
    }

    /**
     * Tìm kiếm khách hàng nâng cao với nhiều tiêu chí tùy chọn
     *
     * @param request yêu cầu tìm kiếm nâng cao
     * @return Page<CustomerDto>
     */
    @Transactional(readOnly = true)
    public Page<CustomerDto> searchCustomersAdvanced(CustomerAdvancedSearchRequest request) {
        log.debug("Tìm kiếm khách hàng nâng cao với từ khóa: {}, giới tính: {}, loại: {}",
                request.getTrimmedSearchTerm(), request.getGender(), request.getCustomerType());

        // Validate request
        validateAdvancedSearchRequest(request);

        // Tạo Pageable với sắp xếp
        // Map các field của User cần thêm prefix "user."
        String sortField = request.getSortBy();
        if ("name".equals(sortField) || "email".equals(sortField) ||
            "phone".equals(sortField) || "createdAt".equals(sortField) ||
            "updatedAt".equals(sortField)) {
            sortField = "user." + sortField;
        } else if ("customerType".equals(sortField) || "address".equals(sortField) ||
                   "customerCode".equals(sortField)) {
            // Các field này thuộc Customer, giữ nguyên
        } else {
            // Mặc định sort theo user.createdAt nếu field không hợp lệ
            sortField = "user.createdAt";
        }

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), sortField);
        Pageable pageable = PageRequest.of(request.getPage(), request.getLimit(), sort);

        // Gọi repository method với các tham số
        Page<Customer> customers = customerRepository.searchCustomersAdvanced(
                request.getTrimmedSearchTerm(),
                request.getGender(),
                request.getCustomerType(),
                pageable);

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
        return customerRepository.findByCustomerTypeAndUser_IsDeletedFalse(customerType)
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

        Customer customer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Check if customer is eligible for VIP
        User user = customer.getUser();
        if (user.getDateOfBirth() != null && !customerValidator.isEligibleForVip(user.getDateOfBirth())) {
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

        Customer customer = customerRepository.findByCustomerIdAndUser_IsDeletedFalse(customerId)
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

    /**
     * Validate CustomerAdvancedSearchRequest
     */
    private void validateAdvancedSearchRequest(CustomerAdvancedSearchRequest request) {
        // Validate page number
        if (request.getPage() < 0) {
            throw new CustomerValidationException("page", "Số trang phải >= 0");
        }

        // Validate limit
        if (request.getLimit() < 1 || request.getLimit() > 100) {
            throw new CustomerValidationException("limit", "Limit phải từ 1 đến 100");
        }

        // Validate sortBy field
        String[] allowedSortFields = { "name", "email", "phone", "createdAt", "updatedAt", "customerType", "gender" };
        boolean isValidSortField = false;
        for (String field : allowedSortFields) {
            if (field.equals(request.getSortBy())) {
                isValidSortField = true;
                break;
            }
        }
        if (!isValidSortField) {
            throw new CustomerValidationException("sortBy",
                    "Trường sắp xếp không hợp lệ. Chỉ cho phép: " + String.join(", ", allowedSortFields));
        }

        // Validate sortDirection
        if (!"asc".equalsIgnoreCase(request.getSortDirection())
                && !"desc".equalsIgnoreCase(request.getSortDirection())) {
            throw new CustomerValidationException("sortDirection", "Hướng sắp xếp chỉ cho phép 'asc' hoặc 'desc'");
        }

        // Validate searchTerm length if provided
        if (request.hasSearchTerm() && request.getTrimmedSearchTerm().length() > 100) {
            throw new CustomerValidationException("searchTerm", "Từ khóa tìm kiếm không được vượt quá 100 ký tự");
        }
    }

    /**
     * Validate BulkDeleteCustomersRequest
     */
    private void validateBulkDeleteRequest(BulkDeleteCustomersRequest request) {
        // Kiểm tra danh sách ID không null và không rỗng
        if (request.getCustomerIds() == null || request.getCustomerIds().isEmpty()) {
            throw new CustomerValidationException("customerIds", "Danh sách ID khách hàng không được rỗng");
        }

        // Kiểm tra số lượng ID không vượt quá giới hạn
        if (request.getCustomerIds().size() > 100) {
            throw new CustomerValidationException("customerIds", "Không thể xóa quá 100 khách hàng cùng lúc");
        }

        // Kiểm tra tất cả ID phải là số dương
        for (Integer customerId : request.getCustomerIds()) {
            if (customerId == null || customerId <= 0) {
                throw new CustomerValidationException("customerIds", "Tất cả ID khách hàng phải là số dương");
            }
        }
    }
}
