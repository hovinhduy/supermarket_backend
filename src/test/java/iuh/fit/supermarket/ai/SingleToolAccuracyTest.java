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

import java.util.ArrayList;
import java.util.List;

/**
 * Tool đơn giản để test một câu hỏi 10 lần và đánh giá tỷ lệ gọi đúng tool
 * 
 * Cách sử dụng:
 * 1. Thay đổi TEST_MESSAGE với câu hỏi bạn muốn test
 * 2. Thay đổi EXPECTED_TOOL với tool mong đợi
 * 3. Chạy test: mvn test -Dtest=SingleToolAccuracyTest
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SingleToolAccuracyTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    private static Integer testCustomerId;
    private static final List<TestResult> results = new ArrayList<>();

    // ========== CẤU HÌNH TEST ==========
    // Thay đổi 2 biến này để test câu hỏi khác
    private static final String TEST_MESSAGE = "Tìm coca";
    private static final String EXPECTED_TOOL = "productSearchTool";
    private static final int TEST_RUNS = 10;
    // ====================================

    /**
     * Kết quả mỗi lần test
     */
    record TestResult(
            int runNumber,
            String actualToolsUsed,
            AIStructuredResponse.ResponseType responseType,
            boolean isCorrect,
            long latencyMs,
            String message) {
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
     * Chạy test 10 lần với cùng một câu hỏi
     */
    @Test
    @Order(1)
    @DisplayName("Test câu hỏi 10 lần và đánh giá tỷ lệ gọi đúng tool")
    void testToolAccuracy10Times() {
        log.info("\n" + "=".repeat(80));
        log.info("🚀 BẮT ĐẦU TEST");
        log.info("=".repeat(80));
        log.info("Câu hỏi test: \"{}\"", TEST_MESSAGE);
        log.info("Tool mong đợi: {}", EXPECTED_TOOL);
        log.info("Số lần chạy: {}", TEST_RUNS);
        log.info("=".repeat(80));

        for (int i = 1; i <= TEST_RUNS; i++) {
            try {
                log.info("\n📝 Lần chạy {}/{}", i, TEST_RUNS);

                // Đo thời gian
                long startTime = System.currentTimeMillis();

                // Gọi AI
                ChatRequest request = new ChatRequest(null, TEST_MESSAGE);
                ChatResponse response = chatService.sendMessage(request, testCustomerId);

                long latency = System.currentTimeMillis() - startTime;

                // Lấy kết quả
                AIStructuredResponse structuredResponse = response.structuredData();
                String actualToolsUsed = structuredResponse.metadata() != null
                        ? structuredResponse.metadata().toolsUsed()
                        : "none";
                AIStructuredResponse.ResponseType responseType = structuredResponse.responseType();

                // Kiểm tra tool có đúng không
                boolean isCorrect = checkToolMatch(EXPECTED_TOOL, actualToolsUsed);

                // Lưu kết quả
                TestResult result = new TestResult(
                        i,
                        actualToolsUsed,
                        responseType,
                        isCorrect,
                        latency,
                        structuredResponse.message());
                results.add(result);

                // Log kết quả
                String status = isCorrect ? "✅ ĐÚNG" : "❌ SAI";
                log.info("   Tool: {} (Expected: {}, Actual: {})", status, EXPECTED_TOOL, actualToolsUsed);
                log.info("   Response Type: {}", responseType);
                log.info("   Latency: {} ms", latency);

            } catch (Exception e) {
                log.error("❌ Lỗi lần chạy {}: {}", i, e.getMessage());
                results.add(new TestResult(
                        i,
                        "ERROR",
                        AIStructuredResponse.ResponseType.ERROR,
                        false,
                        0,
                        "Error: " + e.getMessage()));
            }
        }

        log.info("\n✅ Hoàn thành {} lần chạy", TEST_RUNS);
    }

    /**
     * Tính toán và hiển thị kết quả
     */
    @Test
    @Order(2)
    @DisplayName("Tính toán tỷ lệ gọi đúng tool")
    void calculateAccuracy() {
        Assertions.assertFalse(results.isEmpty(), "Không có kết quả test nào");

        int totalRuns = results.size();
        long correctCalls = results.stream().filter(TestResult::isCorrect).count();
        long incorrectCalls = totalRuns - correctCalls;
        double accuracy = (double) correctCalls / totalRuns * 100;

        // Tính latency
        List<Long> latencies = results.stream()
                .map(TestResult::latencyMs)
                .filter(l -> l > 0)
                .toList();
        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long minLatency = latencies.isEmpty() ? 0 : latencies.stream().min(Long::compare).orElse(0L);
        long maxLatency = latencies.isEmpty() ? 0 : latencies.stream().max(Long::compare).orElse(0L);

        // Hiển thị kết quả
        log.info("\n" + "=".repeat(80));
        log.info("📊 KẾT QUẢ ĐÁNH GIÁ");
        log.info("=".repeat(80));
        log.info("Câu hỏi test: \"{}\"", TEST_MESSAGE);
        log.info("Tool mong đợi: {}", EXPECTED_TOOL);
        log.info("");
        log.info("📈 ACCURACY:");
        log.info("   Tổng số lần chạy: {}", totalRuns);
        log.info("   Gọi đúng tool: {} lần", correctCalls);
        log.info("   Gọi sai tool: {} lần", incorrectCalls);
        log.info("   Tỷ lệ chính xác: {:.2f}%", accuracy);
        log.info("");
        log.info("⏱️  LATENCY:");
        log.info("   Average: {:.2f} ms", avgLatency);
        log.info("   Min: {} ms", minLatency);
        log.info("   Max: {} ms", maxLatency);
        log.info("");

        // Chi tiết từng lần chạy
        log.info("📋 CHI TIẾT TỪNG LẦN CHẠY:");
        log.info("-".repeat(80));
        log.info(String.format("%-5s | %-10s | %-30s | %-10s", "Lần", "Kết quả", "Tool gọi", "Latency"));
        log.info("-".repeat(80));
        for (TestResult result : results) {
            String status = result.isCorrect() ? "✅ ĐÚNG" : "❌ SAI";
            log.info(String.format("%-5d | %-10s | %-30s | %d ms",
                    result.runNumber(),
                    status,
                    result.actualToolsUsed(),
                    result.latencyMs()));
        }
        log.info("-".repeat(80));

        // Chi tiết các lần gọi sai
        List<TestResult> incorrectResults = results.stream()
                .filter(r -> !r.isCorrect())
                .toList();

        if (!incorrectResults.isEmpty()) {
            log.info("");
            log.info("❌ CHI TIẾT CÁC LẦN GỌI SAI:");
            for (TestResult result : incorrectResults) {
                log.info("   Lần {}: Tool gọi = {}, Response = {}",
                        result.runNumber(),
                        result.actualToolsUsed(),
                        result.message().substring(0, Math.min(100, result.message().length())) + "...");
            }
        }

        // Phân tích tool được gọi
        log.info("");
        log.info("📊 PHÂN TÍCH TOOL ĐƯỢC GỌI:");
        results.stream()
                .map(TestResult::actualToolsUsed)
                .distinct()
                .forEach(tool -> {
                    long count = results.stream()
                            .filter(r -> r.actualToolsUsed().equals(tool))
                            .count();
                    double percentage = (double) count / totalRuns * 100;
                    log.info("   {}: {} lần ({:.1f}%)", tool, count, percentage);
                });

        log.info("");
        log.info("=".repeat(80));

        // Đánh giá kết quả
        if (accuracy >= 90.0) {
            log.info("🎉 KẾT QUẢ: XUẤT SẮC (≥ 90%)");
        } else if (accuracy >= 80.0) {
            log.info("✅ KẾT QUẢ: TỐT (≥ 80%)");
        } else if (accuracy >= 70.0) {
            log.info("⚠️  KẾT QUẢ: TRUNG BÌNH (≥ 70%)");
        } else {
            log.info("❌ KẾT QUẢ: CẦN CẢI THIỆN (< 70%)");
        }
        log.info("=".repeat(80));

        // Assert
        Assertions.assertTrue(accuracy >= 70.0,
                String.format("Tỷ lệ chính xác quá thấp: %.2f%% (yêu cầu >= 70%%)", accuracy));
    }

    /**
     * Kiểm tra tool có match không
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
