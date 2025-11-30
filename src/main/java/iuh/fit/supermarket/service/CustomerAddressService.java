package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.address.CreateAddressRequest;
import iuh.fit.supermarket.dto.address.CustomerAddressDto;
import iuh.fit.supermarket.dto.address.UpdateAddressRequest;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.CustomerAddress;
import iuh.fit.supermarket.entity.User;

import iuh.fit.supermarket.repository.CustomerAddressRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho CustomerAddress
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    /**
     * Số lượng địa chỉ tối đa cho mỗi khách hàng
     */
    private static final int MAX_ADDRESSES_PER_CUSTOMER = 10;

    /**
     * Lấy danh sách địa chỉ của khách hàng hiện tại (từ token)
     *
     * @param username username từ token
     * @return danh sách địa chỉ
     */
    @Transactional(readOnly = true)
    public List<CustomerAddressDto> getMyAddresses(String username) {
        log.debug("Lấy danh sách địa chỉ của khách hàng: {}", username);

        Customer customer = getCustomerFromUsername(username);
        return customerAddressRepository.findAllByCustomerId(customer.getCustomerId())
                .stream()
                .map(CustomerAddressDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Tạo địa chỉ mới cho khách hàng hiện tại (từ token)
     *
     * @param username username từ token
     * @param request  thông tin địa chỉ
     * @return địa chỉ đã tạo
     */
    @Transactional
    public CustomerAddressDto createMyAddress(String username, CreateAddressRequest request) {
        log.info("Khách hàng {} tạo địa chỉ mới", username);

        Customer customer = getCustomerFromUsername(username);
        return createAddressForCustomer(customer, request);
    }

    /**
     * Cập nhật địa chỉ của khách hàng hiện tại (từ token)
     *
     * @param username  username từ token
     * @param addressId ID địa chỉ
     * @param request   thông tin cập nhật
     * @return địa chỉ đã cập nhật
     */
    @Transactional
    public CustomerAddressDto updateMyAddress(String username, Long addressId, UpdateAddressRequest request) {
        log.info("Khách hàng {} cập nhật địa chỉ ID: {}", username, addressId);

        Customer customer = getCustomerFromUsername(username);

        CustomerAddress address = customerAddressRepository.findByAddressIdAndCustomerId(addressId, customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        return updateAddressEntity(address, request);
    }

    /**
     * Xóa địa chỉ của khách hàng hiện tại (từ token)
     *
     * @param username  username từ token
     * @param addressId ID địa chỉ
     */
    @Transactional
    public void deleteMyAddress(String username, Long addressId) {
        log.info("Khách hàng {} xóa địa chỉ ID: {}", username, addressId);

        Customer customer = getCustomerFromUsername(username);

        CustomerAddress address = customerAddressRepository.findByAddressIdAndCustomerId(addressId, customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        address.setIsDeleted(true);
        customerAddressRepository.save(address);

        log.info("Đã xóa địa chỉ ID: {}", addressId);
    }

    /**
     * Đặt địa chỉ mặc định
     *
     * @param username  username từ token
     * @param addressId ID địa chỉ
     * @return địa chỉ đã cập nhật
     */
    @Transactional
    public CustomerAddressDto setDefaultAddress(String username, Long addressId) {
        log.info("Khách hàng {} đặt địa chỉ mặc định ID: {}", username, addressId);

        Customer customer = getCustomerFromUsername(username);

        CustomerAddress address = customerAddressRepository.findByAddressIdAndCustomerId(addressId, customer.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Bỏ đánh dấu mặc định cho tất cả địa chỉ của khách hàng
        customerAddressRepository.clearDefaultAddresses(customer.getCustomerId());

        // Đặt địa chỉ này làm mặc định
        address.setIsDefault(true);
        CustomerAddress savedAddress = customerAddressRepository.save(address);

        log.info("Đã đặt địa chỉ ID: {} làm mặc định", addressId);

        return CustomerAddressDto.fromEntity(savedAddress);
    }

    /**
     * Lấy địa chỉ mặc định của khách hàng
     *
     * @param username username từ token
     * @return địa chỉ mặc định hoặc null nếu chưa có
     */
    @Transactional(readOnly = true)
    public CustomerAddressDto getDefaultAddress(String username) {
        log.debug("Lấy địa chỉ mặc định của khách hàng: {}", username);

        Customer customer = getCustomerFromUsername(username);

        return customerAddressRepository.findDefaultAddressByCustomerId(customer.getCustomerId())
                .map(CustomerAddressDto::fromEntity)
                .orElse(null);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Tạo địa chỉ cho khách hàng
     */
    private CustomerAddressDto createAddressForCustomer(Customer customer, CreateAddressRequest request) {
        // Kiểm tra số lượng địa chỉ
        long addressCount = customerAddressRepository.countByCustomerId(customer.getCustomerId());
        if (addressCount >= MAX_ADDRESSES_PER_CUSTOMER) {
            throw new RuntimeException("Khách hàng chỉ được có tối đa " + MAX_ADDRESSES_PER_CUSTOMER + " địa chỉ");
        }

        // Nếu đánh dấu mặc định, bỏ đánh dấu các địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            customerAddressRepository.clearDefaultAddresses(customer.getCustomerId());
        }

        // Nếu là địa chỉ đầu tiên, tự động đặt làm mặc định
        boolean isFirstAddress = addressCount == 0;

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setRecipientName(request.getRecipientName().trim());
        address.setRecipientPhone(normalizePhone(request.getRecipientPhone()));
        address.setAddressLine(request.getAddressLine().trim());
        address.setWard(request.getWard() != null ? request.getWard().trim() : null);
        address.setCity(request.getCity() != null ? request.getCity().trim() : null);
        address.setIsDefault(isFirstAddress || Boolean.TRUE.equals(request.getIsDefault()));
        address.setLabel(request.getLabel());
        address.setIsDeleted(false);

        CustomerAddress savedAddress = customerAddressRepository.save(address);
        log.info("Đã tạo địa chỉ mới ID: {} cho khách hàng ID: {}", savedAddress.getAddressId(), customer.getCustomerId());

        return CustomerAddressDto.fromEntity(savedAddress);
    }

    /**
     * Cập nhật entity địa chỉ
     */
    private CustomerAddressDto updateAddressEntity(CustomerAddress address, UpdateAddressRequest request) {
        // Nếu đánh dấu mặc định, bỏ đánh dấu các địa chỉ khác
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            customerAddressRepository.clearDefaultAddresses(address.getCustomer().getCustomerId());
        }

        address.setRecipientName(request.getRecipientName().trim());
        address.setRecipientPhone(normalizePhone(request.getRecipientPhone()));
        address.setAddressLine(request.getAddressLine().trim());
        address.setWard(request.getWard() != null ? request.getWard().trim() : null);
        address.setCity(request.getCity() != null ? request.getCity().trim() : null);
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }
        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }

        CustomerAddress savedAddress = customerAddressRepository.save(address);
        log.info("Đã cập nhật địa chỉ ID: {}", savedAddress.getAddressId());

        return CustomerAddressDto.fromEntity(savedAddress);
    }

    /**
     * Lấy Customer từ username (có thể có prefix CUSTOMER:)
     */
    private Customer getCustomerFromUsername(String username) {
        String email = username;
        if (username.startsWith("CUSTOMER:")) {
            email = username.substring("CUSTOMER:".length());
        }

        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));

        return customerRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng"));
    }

    /**
     * Chuẩn hóa số điện thoại
     */
    private String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.trim().replaceAll("\\s+", "");
    }
}
