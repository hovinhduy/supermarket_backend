package iuh.fit.supermarket.ai;

import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test độ trễ (Latency) của AI
 * Đo lường thời gian phản hồi P50, P95, P99 và Average
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AILatencyMetricsTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    private static Integer testCustomerId;
    private static final List<Long> latencies = new ArrayList<>();
    private static final int SAMPLE_SIZE = 20; // Số lượng request để test

    /**
     * Các test messages đa dạng
     */
    private static final List<String> TEST_MESSAGES = List.of(
            "Tìm coca",
            "Có sữa nào không?",
            "Xem đơn hàng của tôi",
            "Có khuyến mãi gì không?",
            "Thêm coca vào giỏ hàng",
            "Xóa coca khỏi giỏ hàng",
            "Xóa hết giỏ hàng",
            "Xem giỏ hàng",
            "Siêu thị mở cửa lúc mấy giờ?",
            "Xem giỏ hàng của tôi",
            "Tôi đã mua gì?",
            "Mua 2 lon coca");

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
     * Test latency với nhiều requests
     */
    @Test
    @Order(1)
    @DisplayName("Đo latency với nhiều requests")
    void measureLatency() {
        log.info("🚀 Bắt đầu đo latency với {} requests...", SAMPLE_SIZE);

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            try {
                // Chọn message ngẫu nhiên
                String message = TEST_MESSAGES.get(i % TEST_MESSAGES.size());

                log.info("\n📝 Request {}/{}: {}", i + 1, SAMPLE_SIZE, message);

                // Đo thời gian
                long startTime = System.currentTimeMillis();

                ChatRequest request = new ChatRequest(null, message);
                ChatResponse response = chatService.sendMessage(request, testCustomerId);

                long latency = System.currentTimeMillis() - startTime;
                latencies.add(latency);

                log.info("   ⏱️ Latency: {} ms", latency);
                log.info("   📊 Response Type: {}", response.structuredData() != null
                        ? response.structuredData().responseType()
                        : "N/A");

            } catch (Exception e) {
                log.error("❌ Lỗi khi gửi request {}: {}", i + 1, e.getMessage());
            }
        }

        log.info("\n✅ Hoàn thành {} requests", latencies.size());
    }

    /**
     * Tính toán và hiển thị latency metrics
     */
    @Test
    @Order(2)
    @DisplayName("Tính toán Latency Metrics (P50, P95, P99)")
    void calculateLatencyMetrics() {
        Assertions.assertFalse(latencies.isEmpty(), "Không có dữ liệu latency nào");

        // Sort latencies để tính percentile
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        Collections.sort(sortedLatencies);

        // Tính các metrics
        long min = sortedLatencies.get(0);
        long max = sortedLatencies.get(sortedLatencies.size() - 1);
        double avg = sortedLatencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = calculatePercentile(sortedLatencies, 50);
        long p95 = calculatePercentile(sortedLatencies, 95);
        long p99 = calculatePercentile(sortedLatencies, 99);

        // Hiển thị kết quả
        log.info("\n" + "=".repeat(80));
        log.info("📊 KẾT QUẢ ĐO LƯỜNG LATENCY");
        log.info("=".repeat(80));
        log.info("Số lượng requests: {}", latencies.size());
        log.info("Min Latency: {} ms", min);
        log.info("Max Latency: {} ms", max);
        log.info("Average Latency: {:.2f} ms", avg);
        log.info("P50 (Median): {} ms", p50);
        log.info("P95: {} ms", p95);
        log.info("P99: {} ms", p99);
        log.info("=".repeat(80));

        // Phân tích phân phối
        log.info("\n📈 PHÂN TÍCH PHÂN PHỐI:");
        analyzeDistribution(sortedLatencies);

        log.info("\n" + "=".repeat(80));

        // Assert P95 phải < 5000ms (5 giây)
        Assertions.assertTrue(p95 < 5000,
                String.format("P95 Latency quá cao: %d ms (yêu cầu < 5000ms)", p95));

        // Assert Average phải < 3000ms (3 giây)
        Assertions.assertTrue(avg < 3000,
                String.format("Average Latency quá cao: %.2f ms (yêu cầu < 3000ms)", avg));
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

    /**
     * Phân tích phân phối latency
     */
    private void analyzeDistribution(List<Long> sortedLatencies) {
        // Chia thành các bucket: <1s, 1-2s, 2-3s, 3-5s, >5s
        long under1s = sortedLatencies.stream().filter(l -> l < 1000).count();
        long between1and2s = sortedLatencies.stream().filter(l -> l >= 1000 && l < 2000).count();
        long between2and3s = sortedLatencies.stream().filter(l -> l >= 2000 && l < 3000).count();
        long between3and5s = sortedLatencies.stream().filter(l -> l >= 3000 && l < 5000).count();
        long over5s = sortedLatencies.stream().filter(l -> l >= 5000).count();

        int total = sortedLatencies.size();

        log.info("< 1s:     {} requests ({:.1f}%)", under1s, (double) under1s / total * 100);
        log.info("1-2s:     {} requests ({:.1f}%)", between1and2s, (double) between1and2s / total * 100);
        log.info("2-3s:     {} requests ({:.1f}%)", between2and3s, (double) between2and3s / total * 100);
        log.info("3-5s:     {} requests ({:.1f}%)", between3and5s, (double) between3and5s / total * 100);
        log.info("> 5s:     {} requests ({:.1f}%)", over5s, (double) over5s / total * 100);
    }

    /**
     * Test latency cho từng loại tool
     */
    @Test
    @Order(3)
    @DisplayName("Đo latency theo từng loại tool")
    void measureLatencyByToolType() {
        log.info("\n🔍 Đo latency theo từng loại tool...");

        List<ToolLatencyTest> toolTests = List.of(
                new ToolLatencyTest("ProductSearch", "Tìm coca"),
                new ToolLatencyTest("OrderSearch", "Xem đơn hàng của tôi"),
                new ToolLatencyTest("PromotionSearch", "Có khuyến mãi gì không?"),
                new ToolLatencyTest("AddToCart", "Thêm pepsi vào giỏ hàng"),
                new ToolLatencyTest("GetCart", "Xem giỏ hàng"),
                new ToolLatencyTest("GeneralAnswer", "Siêu thị mở cửa lúc mấy giờ?"));

        log.info("\n" + "=".repeat(80));
        log.info("📊 LATENCY THEO TỪNG LOẠI TOOL");
        log.info("=".repeat(80));

        for (ToolLatencyTest test : toolTests) {
            List<Long> toolLatencies = new ArrayList<>();

            // Chạy 3 lần cho mỗi tool
            for (int i = 0; i < 3; i++) {
                try {
                    long startTime = System.currentTimeMillis();

                    ChatRequest request = new ChatRequest(null, test.message());
                    chatService.sendMessage(request, testCustomerId);

                    long latency = System.currentTimeMillis() - startTime;
                    toolLatencies.add(latency);

                } catch (Exception e) {
                    log.error("❌ Lỗi khi test {}: {}", test.toolName(), e.getMessage());
                }
            }

            if (!toolLatencies.isEmpty()) {
                double avgLatency = toolLatencies.stream().mapToLong(Long::longValue).average().orElse(0);
                log.info("{}: {:.2f} ms (avg of {} runs)", test.toolName(), avgLatency, toolLatencies.size());
            }
        }

        log.info("=".repeat(80));
    }

    /**
     * Record cho tool latency test
     */
    record ToolLatencyTest(String toolName, String message) {
    }
}
