package iuh.fit.supermarket.dto.store;

import java.time.LocalTime;

/**
 * DTO cho thông tin cửa hàng
 */
public record StoreDTO(
        Long storeId,
        String storeCode,
        String storeName,
        String address,
        String phone,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean isActive
) {}
