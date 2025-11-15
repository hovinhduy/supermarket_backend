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
 * Tool để xóa toàn bộ giỏ hàng thông qua AI
 */
@Component("clearCartTool")
@Slf4j
@RequiredArgsConstructor
@Description("Xóa toàn bộ sản phẩm trong giỏ hàng của khách hàng.")
public class ClearCartTool implements Function<ClearCartTool.Request, String> {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho việc xóa toàn bộ giỏ hàng
     */
    public record Request(
            @Description("ID của khách hàng") Integer customerId
    ) {}

    /**
     * Thực hiện xóa toàn bộ giỏ hàng
     *
     * @param request chứa customerId
     * @return JSON string chứa thông tin giỏ hàng rỗng hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Clearing cart for customer: {}", request.customerId());
            CartInfo cartInfo = cartService.clearCart(request.customerId());
            return objectMapper.writeValueAsString(cartInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý giỏ hàng: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error clearing cart: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể xóa giỏ hàng: " + e.getMessage() + "\"}";
        }
    }
}
