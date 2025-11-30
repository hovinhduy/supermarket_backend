package iuh.fit.supermarket.dto.address;

import iuh.fit.supermarket.entity.CustomerAddress;
import iuh.fit.supermarket.enums.AddressLabel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho địa chỉ giao hàng của khách hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddressDto {

    /**
     * ID địa chỉ
     */
    private Long addressId;

    /**
     * ID khách hàng
     */
    private Integer customerId;

    /**
     * Tên người nhận hàng
     */
    private String recipientName;

    /**
     * Số điện thoại người nhận
     */
    private String recipientPhone;

    /**
     * Địa chỉ chi tiết (số nhà, tên đường)
     */
    private String addressLine;

    /**
     * Phường/Xã
     */
    private String ward;

    /**
     * Tỉnh/Thành phố
     */
    private String city;

    /**
     * Đánh dấu địa chỉ mặc định
     */
    private Boolean isDefault;

    /**
     * Nhãn địa chỉ (Nhà, Văn phòng, Tòa nhà)
     */
    private AddressLabel label;

    /**
     * Địa chỉ đầy đủ (nối các thành phần)
     */
    private String fullAddress;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     *
     * @param entity CustomerAddress entity
     * @return CustomerAddressDto
     */
    public static CustomerAddressDto fromEntity(CustomerAddress entity) {
        if (entity == null) {
            return null;
        }
        CustomerAddressDto dto = new CustomerAddressDto();
        dto.setAddressId(entity.getAddressId());
        dto.setCustomerId(entity.getCustomer() != null ? entity.getCustomer().getCustomerId() : null);
        dto.setRecipientName(entity.getRecipientName());
        dto.setRecipientPhone(entity.getRecipientPhone());
        dto.setAddressLine(entity.getAddressLine());
        dto.setWard(entity.getWard());
        dto.setCity(entity.getCity());
        dto.setIsDefault(entity.getIsDefault());
        dto.setLabel(entity.getLabel());
        dto.setFullAddress(entity.getFullAddress());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
