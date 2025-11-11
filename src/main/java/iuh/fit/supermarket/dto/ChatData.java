package iuh.fit.supermarket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO đại diện cho dữ liệu bổ sung trong cuộc trò chuyện chat
 * Lưu trữ thông tin về sản phẩm, đơn hàng, khuyến mãi, kho hàng và chính sách
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatData {

    /**
     * Danh sách thông tin sản phẩm liên quan
     */
    private List<Object> products;

    /**
     * Danh sách thông tin đơn hàng liên quan
     */
    private List<Object> orders;

    /**
     * Danh sách thông tin khuyến mãi liên quan
     */
    private List<Object> promotions;

    /**
     * Danh sách thông tin kho hàng liên quan
     */
    private List<Object> stock;

    /**
     * Danh sách thông tin chính sách liên quan
     */
    private List<Object> policy;
}
