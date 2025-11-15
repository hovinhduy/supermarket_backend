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
 * Tool để cập nhật số lượng sản phẩm trong giỏ hàng thông qua AI
 */
@Component("updateCartQuantityTool")
@Slf4j
@RequiredArgsConstructor
@Description("Cập nhật số lượng của một sản phẩm đã có trong giỏ hàng.")
public class UpdateCartQuantityTool implements Function<UpdateCartQuantityTool.Request, String> {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho việc cập nhật số lượng
     */
    public record Request(
            @Description("ID của khách hàng") Integer customerId,
            @Description("ID của đơn vị sản phẩm") Long productUnitId,
            @Description("Số lượng mới") Integer quantity
    ) {}

    /**
     * Thực hiện cập nhật số lượng sản phẩm trong giỏ hàng
     *
     * @param request chứa customerId, productUnitId, quantity
     * @return JSON string chứa thông tin giỏ hàng hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Updating cart quantity - Customer: {}, ProductUnit: {}, Quantity: {}",
                    request.customerId(), request.productUnitId(), request.quantity());
            CartInfo cartInfo = cartService.updateQuantity(
                    request.customerId(),
                    request.productUnitId(),
                    request.quantity()
            );
            return objectMapper.writeValueAsString(cartInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý giỏ hàng: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error updating cart quantity: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể cập nhật số lượng: " + e.getMessage() + "\"}";
        }
    }
}
