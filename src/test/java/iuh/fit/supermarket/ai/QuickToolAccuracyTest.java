package iuh.fit.supermarket.ai;

import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tool nhanh để test một câu hỏi 10 lần
 * 
 * HƯỚNG DẪN SỬ DỤNG:
 * 1. Mở file này
 * 2. Thay đổi 2 biến trong method testQuestion():
 * - message: Câu hỏi bạn muốn test
 * - expectedTool: Tool mong đợi AI sẽ gọi
 * 3. Chạy: .\mvnw.cmd test -Dtest=QuickToolAccuracyTest
 * 4. Xem kết quả trong console
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class QuickToolAccuracyTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    private Integer testCustomerId;

    @BeforeEach
    void setup() {
        Customer customer = customerRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy customer"));
        testCustomerId = customer.getCustomerId();
    }

    @Test
    void testQuestion() {
        // ========== THAY ĐỔI 2 DÒNG NÀY ==========
        String message = "Tìm coca";
        String expectedTool = "productSearchTool";
        // ==========================================

        int runs = 10;
        int correctCount = 0;
        long totalLatency = 0;

        log.info("\n" + "=".repeat(100));
        log.info("🚀 TEST: \"{}\"", message);
        log.info("🎯 Tool mong đợi: {}", expectedTool);
        log.info("🔄 Số lần chạy: {}", runs);
        log.info("=".repeat(100));

        for (int i = 1; i <= runs; i++) {
            try {
                long start = System.currentTimeMillis();
                ChatRequest request = new ChatRequest(null, message);
                ChatResponse response = chatService.sendMessage(request, testCustomerId);
                long latency = System.currentTimeMillis() - start;
                totalLatency += latency;

                AIStructuredResponse data = response.structuredData();
                String actualTool = data.metadata() != null ? data.metadata().toolsUsed() : "none";
                boolean correct = actualTool.contains(expectedTool);

                if (correct)
                    correctCount++;

                String status = correct ? "✅" : "❌";
                log.info("Lần {}: {} | Tool: {} | Latency: {} ms", i, status, actualTool, latency);

            } catch (Exception e) {
                log.error("Lần {}: ❌ Lỗi - {}", i, e.getMessage());
            }
        }

        double accuracy = (double) correctCount / runs * 100;
        double avgLatency = (double) totalLatency / runs;

        log.info("=".repeat(100));
        log.info("📊 KẾT QUẢ:");
        log.info("   ✅ Gọi đúng: {}/{} lần", correctCount, runs);
        log.info("   ❌ Gọi sai: {}/{} lần", runs - correctCount, runs);
        log.info("   📈 Tỷ lệ chính xác: {:.1f}%", accuracy);
        log.info("   ⏱️  Latency trung bình: {:.0f} ms", avgLatency);
        log.info("");

        if (accuracy >= 90) {
            log.info("🎉 ĐÁNH GIÁ: XUẤT SẮC!");
        } else if (accuracy >= 80) {
            log.info("✅ ĐÁNH GIÁ: TỐT!");
        } else if (accuracy >= 70) {
            log.info("⚠️  ĐÁNH GIÁ: TRUNG BÌNH");
        } else {
            log.info("❌ ĐÁNH GIÁ: CẦN CẢI THIỆN");
        }

        log.info("=".repeat(100));
    }
}
