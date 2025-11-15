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
 * Tool để lấy thông tin giỏ hàng hiện tại thông qua AI
 */
@Component("getCartTool")
@Slf4j
@RequiredArgsConstructor
@Description("Xem thông tin giỏ hàng hiện tại của khách hàng, bao gồm các sản phẩm và tổng tiền.")
public class GetCartTool implements Function<GetCartTool.Request, String> {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho việc lấy thông tin giỏ hàng
     */
    public record Request(
            @Description("ID của khách hàng") Integer customerId
    ) {}

    /**
     * Thực hiện lấy thông tin giỏ hàng
     *
     * @param request chứa customerId
     * @return JSON string chứa thông tin giỏ hàng hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Getting cart for customer: {}", request.customerId());
            CartInfo cartInfo = cartService.getCart(request.customerId());
            return objectMapper.writeValueAsString(cartInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý giỏ hàng: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error getting cart: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể lấy thông tin giỏ hàng: " + e.getMessage() + "\"}";
        }
    }
}
