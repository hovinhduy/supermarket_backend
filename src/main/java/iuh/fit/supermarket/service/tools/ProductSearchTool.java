package iuh.fit.supermarket.service.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.structured.ProductInfo;
import iuh.fit.supermarket.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * Tool để tìm kiếm sản phẩm thông qua AI
 * Sử dụng Spring AI's function calling mechanism
 */
@Component
@Slf4j
@RequiredArgsConstructor
@Description("Tìm kiếm sản phẩm trong siêu thị theo từ khóa. Trả về danh sách sản phẩm với thông tin chi tiết.")
public class ProductSearchTool implements Function<ProductSearchTool.Request, String> {

    private final ProductSearchService productSearchService;
    private final ObjectMapper objectMapper;

    /**
     * Request model cho tìm kiếm sản phẩm
     */
    public record Request(
            @Description("Từ khóa tìm kiếm sản phẩm") String searchTerm,
            @Description("Số lượng kết quả tối đa (mặc định 10)") Integer limit
    ) {
        public Request {
            if (limit == null || limit <= 0) {
                limit = 10;
            }
        }
    }

    /**
     * Thực hiện tìm kiếm sản phẩm
     *
     * @param request chứa searchTerm và limit
     * @return JSON string chứa danh sách sản phẩm hoặc error
     */
    @Override
    public String apply(Request request) {
        try {
            log.info("Searching products with term: {}, limit: {}", request.searchTerm(), request.limit());
            List<ProductInfo> products = productSearchService.searchProducts(
                    request.searchTerm(),
                    request.limit()
            );
            String result = objectMapper.writeValueAsString(products);
            log.info("Found {} products", products.size());
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error serializing products: {}", e.getMessage());
            return "{\"error\": \"Không thể xử lý kết quả tìm kiếm: " + e.getMessage() + "\"}";
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage(), e);
            return "{\"error\": \"Không thể tìm kiếm sản phẩm: " + e.getMessage() + "\"}";
        }
    }
}
