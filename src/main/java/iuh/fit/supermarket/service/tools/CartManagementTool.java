package iuh.fit.supermarket.service.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.structured.CartInfo;
import iuh.fit.supermarket.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Tool để thêm sản phẩm vào giỏ hàng thông qua AI
 */
@Component("addToCartTool")
@Slf4j
@RequiredArgsConstructor
@Description("Thêm sản phẩm vào giỏ hàng của khách hàng. CHỈ sử dụng khi khách hàng yêu cầu MUA hoặc THÊM sản phẩm.")
public class CartManagementTool implements Function<CartManagementTool.AddToCartRequest, String> {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho việc thêm sản phẩm vào giỏ hàng
     */
    public record AddToCartRequest(
            @Description("ID của khách hàng") Integer customerId,
            @Description("ID của đơn vị sản phẩm (product_unit_id, KHÔNG phải product_id)") Long productUnitId,
            @Description("Số lượng sản phẩm muốn thêm") Integer quantity
    ) {}

    /**
     * Thực hiện thêm sản phẩm vào giỏ hàng
     *
     * @param request chứa customerId, productUnitId, quantity
     * @return JSON string chứa thông tin giỏ hàng hoặc error
     */
    @Override
    public String apply(AddToCartRequest request) {
        try {
            log.info("Adding to cart - Customer: {}, ProductUnit: {}, Quantity: {}",
                    request.customerId(), request.productUnitId(), request.quantity());
            CartInfo cartInfo = cartService.addToCart(
                    request.customerId(),
                    request.productUnitId(),
                    request.quantity()
            );
            return objectMapper.writeValueAsString(cartInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý giỏ hàng: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error adding to cart: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể thêm sản phẩm vào giỏ hàng: " + e.getMessage() + "\"}";
        }
    }
}
