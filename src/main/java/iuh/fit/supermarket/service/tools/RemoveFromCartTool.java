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
 * Tool để xóa sản phẩm khỏi giỏ hàng thông qua AI
 */
@Component("removeFromCartTool")
@Slf4j
@RequiredArgsConstructor
@Description("Xóa sản phẩm khỏi giỏ hàng của khách hàng.")
public class RemoveFromCartTool implements Function<RemoveFromCartTool.Request, String> {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho việc xóa sản phẩm khỏi giỏ hàng
     */
    public record Request(
            @Description("ID của khách hàng") Integer customerId,
            @Description("ID của đơn vị sản phẩm cần xóa") Long productUnitId
    ) {}

    /**
     * Thực hiện xóa sản phẩm khỏi giỏ hàng
     *
     * @param request chứa customerId và productUnitId
     * @return JSON string chứa thông tin giỏ hàng hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Removing from cart - Customer: {}, ProductUnit: {}",
                    request.customerId(), request.productUnitId());
            CartInfo cartInfo = cartService.removeFromCart(
                    request.customerId(),
                    request.productUnitId()
            );
            return objectMapper.writeValueAsString(cartInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý giỏ hàng: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error removing from cart: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể xóa sản phẩm khỏi giỏ hàng: " + e.getMessage() + "\"}";
        }
    }
}
