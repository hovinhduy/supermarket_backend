package iuh.fit.supermarket.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Test độ chính xác (Accuracy) của AI trong việc gọi đúng Tool
 * Đo lường tỷ lệ AI gọi đúng tool và tỷ lệ gọi sai tool
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AIToolCallingAccuracyTest {

        @Autowired
        private ChatService chatService;

        @Autowired
        private CustomerRepository customerRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private static Integer testCustomerId;
        private static final List<ToolCallTestCase> testCases = new ArrayList<>();
        private static final List<ToolCallResult> results = new ArrayList<>();

        /**
         * Test case cho việc gọi tool
         */
        record ToolCallTestCase(
                        String testName,
                        String userMessage,
                        String expectedTool,
                        AIStructuredResponse.ResponseType expectedResponseType) {
        }

        /**
         * Kết quả test
         */
        record ToolCallResult(
                        String testName,
                        String userMessage,
                        String expectedTool,
                        String actualToolsUsed,
                        AIStructuredResponse.ResponseType expectedResponseType,
                        AIStructuredResponse.ResponseType actualResponseType,
                        boolean isCorrectTool,
                        boolean isCorrectResponseType,
                        long latencyMs) {
        }

        @BeforeAll
        static void setupTestCases() {
                // Test cases cho ProductSearchTool
                testCases.add(new ToolCallTestCase(
                                "Tìm kiếm sản phẩm - Coca",
                                "Tìm coca",
                                "productSearchTool",
                                AIStructuredResponse.ResponseType.PRODUCT_INFO));

                testCases.add(new ToolCallTestCase(
                                "Tìm kiếm sản phẩm - Sữa",
                                "Có sữa nào không?",
                                "productSearchTool",
                                AIStructuredResponse.ResponseType.PRODUCT_INFO));

                // Test cases cho OrderSearchTool
                testCases.add(new ToolCallTestCase(
                                "Xem đơn hàng",
                                "Xem đơn hàng của tôi",
                                "orderSearchTool",
                                AIStructuredResponse.ResponseType.ORDER_INFO));

                testCases.add(new ToolCallTestCase(
                                "Lịch sử mua hàng",
                                "Tôi đã mua gì?",
                                "orderSearchTool",
                                AIStructuredResponse.ResponseType.ORDER_INFO));

                // Test cases cho PromotionSearchTool
                testCases.add(new ToolCallTestCase(
                                "Xem khuyến mãi",
                                "Có khuyến mãi gì không?",
                                "promotionSearchTool",
                                AIStructuredResponse.ResponseType.PROMOTION_INFO));

                // Test cases cho CartManagementTool (addToCart)
                testCases.add(new ToolCallTestCase(
                                "Thêm sản phẩm vào giỏ",
                                "Thêm coca vào giỏ hàng",
                                "productSearchTool,addToCartTool",
                                AIStructuredResponse.ResponseType.CART_INFO));

                testCases.add(new ToolCallTestCase(
                                "Mua sản phẩm",
                                "Mua 2 lon pepsi",
                                "productSearchTool,addToCartTool",
                                AIStructuredResponse.ResponseType.CART_INFO));

                // Test cases cho GetCartTool
                testCases.add(new ToolCallTestCase(
                                "Xem giỏ hàng",
                                "Xem giỏ hàng của tôi",
                                "getCartTool",
                                AIStructuredResponse.ResponseType.CART_INFO));

                // Test cases cho RemoveFromCartTool
                testCases.add(new ToolCallTestCase(
                                "Xóa sản phẩm khỏi giỏ",
                                "Xóa coca khỏi giỏ hàng",
                                "removeFromCartTool",
                                AIStructuredResponse.ResponseType.CART_INFO));

                // Test cases cho ClearCartTool
                testCases.add(new ToolCallTestCase(
                                "Xóa toàn bộ giỏ hàng",
                                "Xóa hết giỏ hàng",
                                "clearCartTool",
                                AIStructuredResponse.ResponseType.CART_INFO));

                // Test cases cho GENERAL_ANSWER (không gọi tool)
                testCases.add(new ToolCallTestCase(
                                "Câu hỏi chung",
                                "Siêu thị mở cửa lúc mấy giờ?",
                                "none",
                                AIStructuredResponse.ResponseType.GENERAL_ANSWER));

                log.info("✅ Đã setup {} test cases", testCases.size());
        }

        @BeforeEach
        void setup() {
                if (testCustomerId == null) {
                        // Lấy customer đầu tiên để test
                        Customer customer = customerRepository.findAll().stream()
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy customer nào để test"));
                        testCustomerId = customer.getCustomerId();
                        log.info("🔑 Sử dụng Customer ID: {} cho test", testCustomerId);
                }
        }

        /**
         * Chạy tất cả test cases và thu thập kết quả
         */
        @Test
        @Order(1)
        @DisplayName("Chạy tất cả test cases và đo accuracy")
        void testAllToolCalls() {
                log.info("🚀 Bắt đầu test {} cases...", testCases.size());

                for (ToolCallTestCase testCase : testCases) {
                        try {
                                log.info("\n📝 Test: {}", testCase.testName());
                                log.info("   Message: {}", testCase.userMessage());
                                log.info("   Expected Tool: {}", testCase.expectedTool());

                                // Đo thời gian
                                long startTime = System.currentTimeMillis();

                                // Gọi AI
                                ChatRequest request = new ChatRequest(null, testCase.userMessage());
                                ChatResponse response = chatService.sendMessage(request, testCustomerId);

                                long latency = System.currentTimeMillis() - startTime;

                                // Lấy structured response
                                AIStructuredResponse structuredResponse = response.structuredData();
                                String actualToolsUsed = structuredResponse.metadata() != null
                                                ? structuredResponse.metadata().toolsUsed()
                                                : "none";
                                AIStructuredResponse.ResponseType actualResponseType = structuredResponse
                                                .responseType();

                                // Kiểm tra tool có đúng không
                                boolean isCorrectTool = checkToolMatch(testCase.expectedTool(), actualToolsUsed);
                                boolean isCorrectResponseType = testCase.expectedResponseType() == actualResponseType;

                                // Lưu kết quả
                                ToolCallResult result = new ToolCallResult(
                                                testCase.testName(),
                                                testCase.userMessage(),
                                                testCase.expectedTool(),
                                                actualToolsUsed,
                                                testCase.expectedResponseType(),
                                                actualResponseType,
                                                isCorrectTool,
                                                isCorrectResponseType,
                                                latency);
                                results.add(result);

                                // Log kết quả
                                String toolStatus = isCorrectTool ? "✅ ĐÚNG" : "❌ SAI";
                                String typeStatus = isCorrectResponseType ? "✅ ĐÚNG" : "❌ SAI";
                                log.info("   Tool: {} (Expected: {}, Actual: {})",
                                                toolStatus, testCase.expectedTool(), actualToolsUsed);
                                log.info("   Type: {} (Expected: {}, Actual: {})",
                                                typeStatus, testCase.expectedResponseType(), actualResponseType);
                                log.info("   Latency: {} ms", latency);

                        } catch (Exception e) {
                                log.error("❌ Lỗi khi test case: {}", testCase.testName(), e);
                                // Lưu kết quả lỗi
                                results.add(new ToolCallResult(
                                                testCase.testName(),
                                                testCase.userMessage(),
                                                testCase.expectedTool(),
                                                "ERROR",
                                                testCase.expectedResponseType(),
                                                AIStructuredResponse.ResponseType.ERROR,
                                                false,
                                                false,
                                                0));
                        }
                }

                log.info("\n✅ Hoàn thành {} test cases", testCases.size());
        }

        /**
         * Tính toán và hiển thị metrics
         */
        @Test
        @Order(2)
        @DisplayName("Tính toán Accuracy Metrics")
        void calculateAccuracyMetrics() {
                Assertions.assertFalse(results.isEmpty(), "Không có kết quả test nào");

                int totalTests = results.size();
                long correctTools = results.stream().filter(ToolCallResult::isCorrectTool).count();
                long correctTypes = results.stream().filter(ToolCallResult::isCorrectResponseType).count();

                double toolAccuracy = (double) correctTools / totalTests * 100;
                double typeAccuracy = (double) correctTypes / totalTests * 100;

                log.info("\n" + "=".repeat(80));
                log.info("📊 KẾT QUẢ ĐO LƯỜNG ACCURACY");
                log.info("=".repeat(80));
                log.info("Tổng số test cases: {}", totalTests);
                log.info("Tool gọi đúng: {} / {} ({:.2f}%)", correctTools, totalTests, toolAccuracy);
                log.info("Response type đúng: {} / {} ({:.2f}%)", correctTypes, totalTests, typeAccuracy);
                log.info("=".repeat(80));

                // Chi tiết các case sai
                log.info("\n📋 CHI TIẾT CÁC CASE SAI:");
                results.stream()
                                .filter(r -> !r.isCorrectTool() || !r.isCorrectResponseType())
                                .forEach(r -> {
                                        log.info("\n❌ {}", r.testName());
                                        log.info("   Message: {}", r.userMessage());
                                        if (!r.isCorrectTool()) {
                                                log.info("   Tool SAI - Expected: {}, Actual: {}",
                                                                r.expectedTool(), r.actualToolsUsed());
                                        }
                                        if (!r.isCorrectResponseType()) {
                                                log.info("   Type SAI - Expected: {}, Actual: {}",
                                                                r.expectedResponseType(), r.actualResponseType());
                                        }
                                });

                log.info("\n" + "=".repeat(80));

                // Assert accuracy phải >= 80%
                Assertions.assertTrue(toolAccuracy >= 80.0,
                                String.format("Tool Accuracy quá thấp: %.2f%% (yêu cầu >= 80%%)", toolAccuracy));
                Assertions.assertTrue(typeAccuracy >= 80.0,
                                String.format("Response Type Accuracy quá thấp: %.2f%% (yêu cầu >= 80%%)",
                                                typeAccuracy));
        }

        /**
         * Kiểm tra tool có match không (hỗ trợ multiple tools)
         */
        private boolean checkToolMatch(String expected, String actual) {
                if (expected.equals(actual)) {
                        return true;
                }

                // Nếu expected có nhiều tools (phân cách bằng dấu phẩy)
                String[] expectedTools = expected.split(",");
                String[] actualTools = actual.split(",");

                // Kiểm tra tất cả expected tools có trong actual không
                for (String expectedTool : expectedTools) {
                        boolean found = false;
                        for (String actualTool : actualTools) {
                                if (actualTool.trim().contains(expectedTool.trim())) {
                                        found = true;
                                        break;
                                }
                        }
                        if (!found) {
                                return false;
                        }
                }

                return true;
        }
}
