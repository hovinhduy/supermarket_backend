package iuh.fit.supermarket.service.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.structured.PromotionInfo;
import iuh.fit.supermarket.service.PromotionSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Tool để lấy danh sách khuyến mãi đang hoạt động thông qua AI
 * Sử dụng Spring AI's function calling mechanism
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Description("Lấy danh sách các chương trình khuyến mãi đang hoạt động trong siêu thị.")
public class PromotionSearchTool implements Function<PromotionSearchTool.Request, String> {

    private final PromotionSearchService promotionSearchService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho tìm kiếm khuyến mãi
     */
    public record Request(
            @Description("Số lượng khuyến mãi tối đa (mặc định 10)") Integer limit) {
        public Request {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
        }
    }

    /**
     * Thực hiện lấy danh sách khuyến mãi đang hoạt động
     *
     * @param request chứa limit
     * @return JSON string chứa danh sách khuyến mãi hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Fetching active promotions, limit: {}", request.limit());
            List<PromotionInfo> promotions = promotionSearchService.getActivePromotions(request.limit());
            String result = objectMapper.writeValueAsString(promotions);
            log.info("Found {} promotions", promotions.size());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error serializing promotions: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý kết quả: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error fetching promotions: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể lấy khuyến mãi: " + e.getMessage() + "\"}";
        }
    }
}
