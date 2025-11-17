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

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Test tổng hợp: Đo cả Accuracy và Latency, xuất báo cáo chi tiết
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AIMetricsReportTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static Integer testCustomerId;
    private static final List<TestResult> results = new ArrayList<>();

    /**
     * Kết quả test tổng hợp
     */
    record TestResult(
            int testNumber,
            String testName,
            String userMessage,
            String expectedTool,
            String actualToolsUsed,
            AIStructuredResponse.ResponseType expectedResponseType,
            AIStructuredResponse.ResponseType actualResponseType,
            boolean isCorrectTool,
            boolean isCorrectResponseType,
            long latencyMs,
            String timestamp) {
    }

    /**
     * Test case
     */
    record TestCase(
            String testName,
            String userMessage,
            String expectedTool,
            AIStructuredResponse.ResponseType expectedResponseType) {
    }

    @BeforeEach
    void setup() {
        if (testCustomerId == null) {
            Customer customer = customerRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy customer nào để test"));
            testCustomerId = customer.getCustomerId();
            log.info("🔑 Sử dụng Customer ID: {} cho test", testCustomerId);
        }
    }

    /**
     * Chạy test tổng hợp
     */
    @Test
    @Order(1)
    @DisplayName("Chạy test tổng hợp Accuracy + Latency")
    void runComprehensiveTest() {
        List<TestCase> testCases = createTestCases();

        log.info("🚀 Bắt đầu test {} cases...", testCases.size());

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);

            try {
                log.info("\n📝 Test {}/{}: {}", i + 1, testCases.size(), testCase.testName());
                log.info("   Message: {}", testCase.userMessage());

                // Đo thời gian
                long startTime = System.currentTimeMillis();

                ChatRequest request = new ChatRequest(null, testCase.userMessage());
                ChatResponse response = chatService.sendMessage(request, testCustomerId);

                long latency = System.currentTimeMillis() - startTime;

                // Lấy kết quả
                AIStructuredResponse structuredResponse = response.structuredData();
                String actualToolsUsed = structuredResponse.metadata() != null
                        ? structuredResponse.metadata().toolsUsed()
                        : "none";
                AIStructuredResponse.ResponseType actualResponseType = structuredResponse.responseType();

                // Kiểm tra
                boolean isCorrectTool = checkToolMatch(testCase.expectedTool(), actualToolsUsed);
                boolean isCorrectResponseType = testCase.expectedResponseType() == actualResponseType;

                // Lưu kết quả
                TestResult result = new TestResult(
                        i + 1,
                        testCase.testName(),
                        testCase.userMessage(),
                        testCase.expectedTool(),
                        actualToolsUsed,
                        testCase.expectedResponseType(),
                        actualResponseType,
                        isCorrectTool,
                        isCorrectResponseType,
                        latency,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                results.add(result);

                // Log
                String toolStatus = isCorrectTool ? "✅" : "❌";
                String typeStatus = isCorrectResponseType ? "✅" : "❌";
                log.info("   Tool: {} | Type: {} | Latency: {} ms", toolStatus, typeStatus, latency);

            } catch (Exception e) {
                log.error("❌ Lỗi: {}", e.getMessage());
                results.add(new TestResult(
                        i + 1,
                        testCase.testName(),
                        testCase.userMessage(),
                        testCase.expectedTool(),
                        "ERROR",
                        testCase.expectedResponseType(),
                        AIStructuredResponse.ResponseType.ERROR,
                        false,
                        false,
                        0,
                        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            }
        }

        log.info("\n✅ Hoàn thành {} test cases", results.size());
    }

    /**
     * Tính toán metrics và xuất báo cáo
     */
    @Test
    @Order(2)
    @DisplayName("Tính toán metrics và xuất báo cáo")
    void generateMetricsReport() {
        Assertions.assertFalse(results.isEmpty(), "Không có kết quả test");

        // Tính Accuracy
        int totalTests = results.size();
        long correctTools = results.stream().filter(TestResult::isCorrectTool).count();
        long correctTypes = results.stream().filter(TestResult::isCorrectResponseType).count();
        double toolAccuracy = (double) correctTools / totalTests * 100;
        double typeAccuracy = (double) correctTypes / totalTests * 100;

        // Tính Latency
        List<Long> latencies = results.stream()
                .map(TestResult::latencyMs)
                .filter(l -> l > 0)
                .sorted()
                .toList();

        long minLatency = latencies.isEmpty() ? 0 : latencies.get(0);
        long maxLatency = latencies.isEmpty() ? 0 : latencies.get(latencies.size() - 1);
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = calculatePercentile(latencies, 50);
        long p95 = calculatePercentile(latencies, 95);
        long p99 = calculatePercentile(latencies, 99);

        // Hiển thị báo cáo
        printReport(totalTests, correctTools, correctTypes, toolAccuracy, typeAccuracy,
                minLatency, maxLatency, avgLatency, p50, p95, p99);

        // Xuất file CSV
        exportToCSV();

        // Assert
        Assertions.assertTrue(toolAccuracy >= 80.0,
                String.format("Tool Accuracy quá thấp: %.2f%%", toolAccuracy));
        Assertions.assertTrue(p95 < 5000,
                String.format("P95 Latency quá cao: %d ms", p95));
    }

    /**
     * In báo cáo ra console
     */
    private void printReport(int totalTests, long correctTools, long correctTypes,
            double toolAccuracy, double typeAccuracy,
            long minLatency, long maxLatency, double avgLatency,
            long p50, long p95, long p99) {

        log.info("\n" + "=".repeat(100));
        log.info("📊 BÁO CÁO METRICS AI TOOL CALLING");
        log.info("=".repeat(100));
        log.info("Thời gian: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        log.info("");

        log.info("📈 ACCURACY METRICS:");
        log.info("   Tổng số test cases: {}", totalTests);
        log.info("   Tool gọi đúng: {} / {} ({:.2f}%)", correctTools, totalTests, toolAccuracy);
        log.info("   Response type đúng: {} / {} ({:.2f}%)", correctTypes, totalTests, typeAccuracy);
        log.info("");

        log.info("⏱️  LATENCY METRICS:");
        log.info("   Min Latency: {} ms", minLatency);
        log.info("   Max Latency: {} ms", maxLatency);
        log.info("   Average Latency: {:.2f} ms", avgLatency);
        log.info("   P50 (Median): {} ms", p50);
        log.info("   P95: {} ms", p95);
        log.info("   P99: {} ms", p99);
        log.info("");

        log.info("❌ CHI TIẾT CÁC CASE SAI:");
        results.stream()
                .filter(r -> !r.isCorrectTool() || !r.isCorrectResponseType())
                .forEach(r -> {
                    log.info("   Test #{}: {}", r.testNumber(), r.testName());
                    log.info("      Message: {}", r.userMessage());
                    if (!r.isCorrectTool()) {
                        log.info("      Tool SAI - Expected: {}, Actual: {}",
                                r.expectedTool(), r.actualToolsUsed());
                    }
                    if (!r.isCorrectResponseType()) {
                        log.info("      Type SAI - Expected: {}, Actual: {}",
                                r.expectedResponseType(), r.actualResponseType());
                    }
                    log.info("      Latency: {} ms", r.latencyMs());
                    log.info("");
                });

        log.info("=".repeat(100));
    }

    /**
     * Xuất kết quả ra file CSV
     */
    private void exportToCSV() {
        String filename = "ai_metrics_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            // Header
            writer.append("Test#,Test Name,User Message,Expected Tool,Actual Tool,Expected Type,Actual Type,")
                    .append("Tool Correct,Type Correct,Latency(ms),Timestamp\n");

            // Data
            for (TestResult result : results) {
                writer.append(String.valueOf(result.testNumber())).append(",")
                        .append(escapeCsv(result.testName())).append(",")
                        .append(escapeCsv(result.userMessage())).append(",")
                        .append(escapeCsv(result.expectedTool())).append(",")
                        .append(escapeCsv(result.actualToolsUsed())).append(",")
                        .append(result.expectedResponseType().toString()).append(",")
                        .append(result.actualResponseType().toString()).append(",")
                        .append(result.isCorrectTool() ? "TRUE" : "FALSE").append(",")
                        .append(result.isCorrectResponseType() ? "TRUE" : "FALSE").append(",")
                        .append(String.valueOf(result.latencyMs())).append(",")
                        .append(result.timestamp()).append("\n");
            }

            log.info("✅ Đã xuất báo cáo ra file: {}", filename);

        } catch (IOException e) {
            log.error("❌ Lỗi khi xuất file CSV: {}", e.getMessage());
        }
    }

    /**
     * Escape CSV value
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Tạo danh sách test cases
     */
    private List<TestCase> createTestCases() {
        return List.of(
                // ProductSearch
                new TestCase("Tìm sản phẩm - Coca", "Tìm coca",
                        "productSearchTool", AIStructuredResponse.ResponseType.PRODUCT_INFO),
                new TestCase("Tìm sản phẩm - Sữa", "Có sữa nào không?",
                        "productSearchTool", AIStructuredResponse.ResponseType.PRODUCT_INFO),
                new TestCase("Tìm sản phẩm - Bánh", "Tìm bánh mì",
                        "productSearchTool", AIStructuredResponse.ResponseType.PRODUCT_INFO),

                // OrderSearch
                new TestCase("Xem đơn hàng", "Xem đơn hàng của tôi",
                        "orderSearchTool", AIStructuredResponse.ResponseType.ORDER_INFO),
                new TestCase("Lịch sử mua", "Tôi đã mua gì?",
                        "orderSearchTool", AIStructuredResponse.ResponseType.ORDER_INFO),

                // PromotionSearch
                new TestCase("Xem khuyến mãi", "Có khuyến mãi gì không?",
                        "promotionSearchTool", AIStructuredResponse.ResponseType.PROMOTION_INFO),

                // AddToCart
                new TestCase("Thêm vào giỏ", "Thêm coca vào giỏ hàng",
                        "productSearchTool,addToCartTool", AIStructuredResponse.ResponseType.CART_INFO),
                new TestCase("Mua sản phẩm", "Mua 2 lon pepsi",
                        "productSearchTool,addToCartTool", AIStructuredResponse.ResponseType.CART_INFO),

                // GetCart
                new TestCase("Xem giỏ hàng", "Xem giỏ hàng của tôi",
                        "getCartTool", AIStructuredResponse.ResponseType.CART_INFO),

                // GeneralAnswer
                new TestCase("Câu hỏi chung", "Siêu thị mở cửa lúc mấy giờ?",
                        "none", AIStructuredResponse.ResponseType.GENERAL_ANSWER));
    }

    /**
     * Kiểm tra tool match
     */
    private boolean checkToolMatch(String expected, String actual) {
        if (expected.equals(actual)) {
            return true;
        }

        String[] expectedTools = expected.split(",");
        String[] actualTools = actual.split(",");

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

    /**
     * Tính percentile
     */
    private long calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index);
    }
}
