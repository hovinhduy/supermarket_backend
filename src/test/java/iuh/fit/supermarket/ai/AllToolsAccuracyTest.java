package iuh.fit.supermarket.ai;

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
 * Test tất cả các tools, mỗi tool chạy 5 lần
 * Xuất báo cáo chi tiết ra console và file CSV
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AllToolsAccuracyTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    private static Integer testCustomerId;
    private static final int RUNS_PER_TOOL = 5;
    private static final List<ToolTestCase> testCases = new ArrayList<>();
    private static final List<TestResult> allResults = new ArrayList<>();

    record ToolTestCase(
            String toolName,
            String testMessage,
            String expectedTool,
            AIStructuredResponse.ResponseType expectedResponseType
    ) {}

    record TestResult(
            String toolName,
            int runNumber,
            String testMessage,
            String expectedTool,
            String actualTool,
            AIStructuredResponse.ResponseType expectedType,
            AIStructuredResponse.ResponseType actualType,
            boolean isCorrectTool,
            boolean isCorrectType,
            long latencyMs
    ) {}

    record ToolStats(
            String toolName,
            int totalRuns,
            int correctCalls,
            int incorrectCalls,
            double accuracy,
            double avgLatency,
            long minLatency,
            long maxLatency
    ) {}

    @BeforeAll
    static void setupTestCases() {
        testCases.add(new ToolTestCase("ProductSearch", "Tìm coca",
                "productSearchTool", AIStructuredResponse.ResponseType.PRODUCT_INFO));
        testCases.add(new ToolTestCase("ProductSearch", "Có sữa nào không?",
                "productSearchTool", AIStructuredResponse.ResponseType.PRODUCT_INFO));
        testCases.add(new ToolTestCase("OrderSearch", "Xem đơn hàng của tôi",
                "orderSearchTool", AIStructuredResponse.ResponseType.ORDER_INFO));
        testCases.add(new ToolTestCase("OrderSearch", "Tôi đã mua gì?",
                "orderSearchTool", AIStructuredResponse.ResponseType.ORDER_INFO));
        testCases.add(new ToolTestCase("PromotionSearch", "Có khuyến mãi gì không?",
                "promotionSearchTool", AIStructuredResponse.ResponseType.PROMOTION_INFO));
        testCases.add(new ToolTestCase("AddToCart", "Thêm coca vào giỏ hàng",
                "productSearchTool,addToCartTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("AddToCart", "Mua 2 lon pepsi",
                "productSearchTool,addToCartTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("GetCart", "Xem giỏ hàng của tôi",
                "getCartTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("RemoveFromCart", "Xóa coca khỏi giỏ hàng",
                "removeFromCartTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("ClearCart", "Xóa hết giỏ hàng",
                "clearCartTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("UpdateCartQuantity", "Đổi số lượng coca thành 3",
                "updateCartQuantityTool", AIStructuredResponse.ResponseType.CART_INFO));
        testCases.add(new ToolTestCase("GeneralAnswer", "Siêu thị mở cửa lúc mấy giờ?",
                "none", AIStructuredResponse.ResponseType.GENERAL_ANSWER));

        log.info("Setup {} test cases", testCases.size());
    }

    @BeforeEach
    void setup() {
        if (testCustomerId == null) {
            Customer customer = customerRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Khong tim thay customer"));
            testCustomerId = customer.getCustomerId();
            log.info("Su dung Customer ID: {}", testCustomerId);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Test tat ca tools, moi tool 5 lan")
    void testAllTools() {
        log.info("\n" + "=".repeat(100));
        log.info("BAT DAU TEST TAT CA TOOLS");
        log.info("So luong test cases: {}", testCases.size());
        log.info("So lan chay moi test case: {}", RUNS_PER_TOOL);
        log.info("Tong so requests: {}", testCases.size() * RUNS_PER_TOOL);
        log.info("=".repeat(100));

        int totalTests = testCases.size() * RUNS_PER_TOOL;
        int currentTest = 0;

        for (ToolTestCase testCase : testCases) {
            log.info("\nTEST: {} - \"{}\"", testCase.toolName(), testCase.testMessage());
            log.info("   Expected Tool: {}", testCase.expectedTool());

            for (int run = 1; run <= RUNS_PER_TOOL; run++) {
                currentTest++;
                try {
                    log.info("   Lan {}/{} (Tong: {}/{})...", run, RUNS_PER_TOOL, currentTest, totalTests);

                    long startTime = System.currentTimeMillis();
                    ChatRequest request = new ChatRequest(null, testCase.testMessage());
                    ChatResponse response = chatService.sendMessage(request, testCustomerId);
                    long latency = System.currentTimeMillis() - startTime;

                    AIStructuredResponse structuredResponse = response.structuredData();
                    String actualTool = structuredResponse.metadata() != null
                            ? structuredResponse.metadata().toolsUsed() : "none";
                    AIStructuredResponse.ResponseType actualType = structuredResponse.responseType();

                    boolean isCorrectTool = checkToolMatch(testCase.expectedTool(), actualTool);
                    boolean isCorrectType = testCase.expectedResponseType() == actualType;

                    TestResult result = new TestResult(testCase.toolName(), run, testCase.testMessage(),
                            testCase.expectedTool(), actualTool, testCase.expectedResponseType(),
                            actualType, isCorrectTool, isCorrectType, latency);
                    allResults.add(result);

                    String toolStatus = isCorrectTool ? "DUNG" : "SAI";
                    String typeStatus = isCorrectType ? "DUNG" : "SAI";
                    log.info("      Tool: {} | Type: {} | Latency: {} ms | Actual: {}",
                            toolStatus, typeStatus, latency, actualTool);

                } catch (Exception e) {
                    log.error("      Loi: {}", e.getMessage());
                    allResults.add(new TestResult(testCase.toolName(), run, testCase.testMessage(),
                            testCase.expectedTool(), "ERROR", testCase.expectedResponseType(),
                            AIStructuredResponse.ResponseType.ERROR, false, false, 0));
                }
            }
        }

        log.info("\nHoan thanh {} tests", allResults.size());
    }

    @Test
    @Order(2)
    @DisplayName("Tinh toan va hien thi ket qua tong hop")
    void calculateAndDisplayResults() {
        Assertions.assertFalse(allResults.isEmpty(), "Khong co ket qua test");

        int totalTests = allResults.size();
        long correctTools = allResults.stream().filter(TestResult::isCorrectTool).count();
        long correctTypes = allResults.stream().filter(TestResult::isCorrectType).count();
        double overallToolAccuracy = (double) correctTools / totalTests * 100;
        double overallTypeAccuracy = (double) correctTypes / totalTests * 100;

        List<Long> allLatencies = allResults.stream().map(TestResult::latencyMs).filter(l -> l > 0).toList();
        double avgLatency = allLatencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long minLatency = allLatencies.isEmpty() ? 0 : allLatencies.stream().min(Long::compare).orElse(0L);
        long maxLatency = allLatencies.isEmpty() ? 0 : allLatencies.stream().max(Long::compare).orElse(0L);

        Map<String, List<TestResult>> resultsByTool = new HashMap<>();
        for (TestResult result : allResults) {
            resultsByTool.computeIfAbsent(result.toolName(), k -> new ArrayList<>()).add(result);
        }

        List<ToolStats> toolStatsList = new ArrayList<>();
        for (Map.Entry<String, List<TestResult>> entry : resultsByTool.entrySet()) {
            String toolName = entry.getKey();
            List<TestResult> results = entry.getValue();

            int total = results.size();
            int correct = (int) results.stream().filter(TestResult::isCorrectTool).count();
            int incorrect = total - correct;
            double accuracy = (double) correct / total * 100;

            List<Long> latencies = results.stream().map(TestResult::latencyMs).filter(l -> l > 0).toList();
            double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
            long min = latencies.isEmpty() ? 0 : latencies.stream().min(Long::compare).orElse(0L);
            long max = latencies.isEmpty() ? 0 : latencies.stream().max(Long::compare).orElse(0L);

            toolStatsList.add(new ToolStats(toolName, total, correct, incorrect, accuracy, avg, min, max));
        }

        toolStatsList.sort((a, b) -> Double.compare(b.accuracy(), a.accuracy()));

        printResults(totalTests, correctTools, correctTypes, overallToolAccuracy, overallTypeAccuracy,
                avgLatency, minLatency, maxLatency, toolStatsList);

        exportToCSV(toolStatsList);

        Assertions.assertTrue(overallToolAccuracy >= 70.0,
                String.format("Tool Accuracy tong the qua thap: %.2f%%", overallToolAccuracy));
    }

    private void printResults(int totalTests, long correctTools, long correctTypes,
                              double overallToolAccuracy, double overallTypeAccuracy,
                              double avgLatency, long minLatency, long maxLatency,
                              List<ToolStats> toolStatsList) {

        log.info("\n" + "=".repeat(100));
        log.info("KET QUA TONG HOP - TEST TAT CA TOOLS");
        log.info("=".repeat(100));
        log.info("Thoi gian: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        log.info("");

        log.info("ACCURACY TONG THE:");
        log.info("   Tong so tests: {}", totalTests);
        log.info("   Tool goi dung: {} / {} ({:.2f}%)", correctTools, totalTests, overallToolAccuracy);
        log.info("   Response type dung: {} / {} ({:.2f}%)", correctTypes, totalTests, overallTypeAccuracy);
        log.info("");

        log.info("LATENCY TONG THE:");
        log.info("   Average: {:.2f} ms", avgLatency);
        log.info("   Min: {} ms", minLatency);
        log.info("   Max: {} ms", maxLatency);
        log.info("");

        log.info("CHI TIET TUNG TOOL:");
        log.info("-".repeat(100));
        log.info(String.format("%-20s | %-8s | %-10s | %-10s | %-12s | %-15s",
                "Tool Name", "Accuracy", "Correct", "Incorrect", "Avg Latency", "Min/Max Latency"));
        log.info("-".repeat(100));

        for (ToolStats stats : toolStatsList) {
            String accuracyStr = String.format("%.1f%%", stats.accuracy());
            String avgLatencyStr = String.format("%.0f ms", stats.avgLatency());
            String minMaxStr = String.format("%d/%d ms", stats.minLatency(), stats.maxLatency());

            log.info(String.format("%-20s | %-8s | %-10s | %-10s | %-12s | %-15s",
                    stats.toolName(), accuracyStr,
                    stats.correctCalls() + "/" + stats.totalRuns(),
                    stats.incorrectCalls(), avgLatencyStr, minMaxStr));
        }
        log.info("-".repeat(100));

        log.info("");
        log.info("TOP 3 TOOLS TOT NHAT:");
        toolStatsList.stream().limit(3).forEach(stats -> {
            log.info("   {}. {} - {:.1f}% accuracy",
                    toolStatsList.indexOf(stats) + 1, stats.toolName(), stats.accuracy());
        });

        log.info("");
        log.info("TOP 3 TOOLS CAN CAI THIEN:");
        toolStatsList.stream().sorted((a, b) -> Double.compare(a.accuracy(), b.accuracy()))
                .limit(3).forEach(stats -> {
                    log.info("   {}. {} - {:.1f}% accuracy (Can cai thien)",
                            stats.toolName(), stats.accuracy());
                });

        log.info("");
        log.info("=".repeat(100));
    }

    private void exportToCSV(List<ToolStats> toolStatsList) {
        String filename = "all_tools_accuracy_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("Tool Name,Total Runs,Correct Calls,Incorrect Calls,Accuracy(%),")
                    .append("Avg Latency(ms),Min Latency(ms),Max Latency(ms)\n");

            for (ToolStats stats : toolStatsList) {
                writer.append(stats.toolName()).append(",")
                        .append(String.valueOf(stats.totalRuns())).append(",")
                        .append(String.valueOf(stats.correctCalls())).append(",")
                        .append(String.valueOf(stats.incorrectCalls())).append(",")
                        .append(String.format("%.2f", stats.accuracy())).append(",")
                        .append(String.format("%.2f", stats.avgLatency())).append(",")
                        .append(String.valueOf(stats.minLatency())).append(",")
                        .append(String.valueOf(stats.maxLatency())).append("\n");
            }

            log.info("Da xuat bao cao ra file: {}", filename);

        } catch (IOException e) {
            log.error("Loi khi xuat file CSV: {}", e.getMessage());
        }
    }

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
}
