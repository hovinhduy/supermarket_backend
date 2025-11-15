package iuh.fit.supermarket.service.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.structured.OrderInfo;
import iuh.fit.supermarket.service.OrderSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Tool để lấy đơn hàng của khách hàng thông qua AI
 * Sử dụng Spring AI's function calling mechanism
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Description("Lấy danh sách đơn hàng của khách hàng. Trả về thông tin chi tiết các đơn hàng.")
public class OrderSearchTool implements Function<OrderSearchTool.Request, String> {

    private final OrderSearchService orderSearchService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho tìm kiếm đơn hàng
     */
    public record Request(
            @Description("ID của khách hàng") Integer customerId,
            @Description("Số lượng đơn hàng tối đa (mặc định 10)") Integer limit
    ) {
        public Request {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
        }
    }

    /**
     * Thực hiện lấy đơn hàng của khách hàng
     *
     * @param request chứa customerId và limit
     * @return JSON string chứa danh sách đơn hàng hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Fetching orders for customer: {}, limit: {}", request.customerId(), request.limit());
            List<OrderInfo> orders = orderSearchService.getCustomerOrders(
                    request.customerId(),
                    request.limit()
            );
            String result = objectMapper.writeValueAsString(orders);
            log.info("Found {} orders", orders.size());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error serializing orders: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý kết quả: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error fetching orders: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể lấy đơn hàng: " + e.getMessage() + "\"}";
        }
    }
}
