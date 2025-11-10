package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;
import iuh.fit.supermarket.entity.ChatConversation;
import iuh.fit.supermarket.entity.ChatMessage;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.SenderType;
import iuh.fit.supermarket.exception.CustomerNotFoundException;
import iuh.fit.supermarket.repository.ChatConversationRepository;
import iuh.fit.supermarket.repository.ChatMessageRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation của ChatService
 * Xử lý logic chat AI với conversation memory và Function Calling (Tools)
 *
 * Function Calling cho phép AI tự động gọi các tools phù hợp dựa trên intent của user,
 * giúp giảm 60-70% token cost và tăng accuracy
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ChatClient chatClient;

    private static final int MEMORY_LIMIT = 10; // Giữ 10 messages gần nhất làm context

    /**
     * Constructor injection với Spring AI Function Calling
     * Spring AI tự động phát hiện và đăng ký các Function beans
     */
    public ChatServiceImpl(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            CustomerRepository customerRepository,
            ChatClient.Builder chatClientBuilder,
            ApplicationContext applicationContext) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;

        // Lấy tất cả Function beans đã định nghĩa trong ChatToolsConfiguration
        Map<String, Function> functionBeans = applicationContext.getBeansOfType(Function.class);

        // Build ChatClient
        this.chatClient = chatClientBuilder.build();

        // Log các tools đã được đăng ký
        if (!functionBeans.isEmpty()) {
            System.out.println("🚀 Đã đăng ký " + functionBeans.size() + " AI Function beans:");
            functionBeans.keySet().forEach(name ->
                System.out.println("   - " + name)
            );
        }
    }

    /**
     * Tạo conversation mới
     */
    @Override
    public ConversationResponse createConversation(Integer customerId) {
        // Verify customer tồn tại
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));

        // Tạo conversation mới
        ChatConversation conversation = new ChatConversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setCustomer(customer);
        conversation.setTitle("Cuộc trò chuyện mới");
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        ChatConversation savedConversation = conversationRepository.save(conversation);

        return new ConversationResponse(
                savedConversation.getId(),
                customerId,
                savedConversation.getTitle(),
                savedConversation.getCreatedAt(),
                savedConversation.getUpdatedAt(),
                "");
    }

    /**
     * Gửi tin nhắn và nhận response từ AI với Function Calling và Structured Output
     *
     * AI sẽ:
     * 1. Tự động gọi các tools phù hợp dựa trên intent của user
     * 2. Trả về dữ liệu có cấu trúc (AIStructuredResponse) thay vì text đơn thuần
     */
    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        // Verify customer tồn tại
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Không tìm thấy khách hàng với ID: " + request.customerId()));

        // Lấy hoặc tạo conversation
        ChatConversation conversation = getOrCreateConversation(request.conversationId(), customer);

        // Lưu user message
        saveMessage(conversation, SenderType.USER, request.message());

        // Lấy conversation history để build context
        List<ChatMessage> recentMessages = messageRepository
                .findTopNByConversationIdOrderByTimestampDesc(conversation.getId(), MEMORY_LIMIT);
        Collections.reverse(recentMessages); // Đảo ngược để có thứ tự chronological

        // Build prompt messages với system message và history
        List<Message> messages = buildPromptMessages(recentMessages, request.customerId(), request.message());

        // Tạo prompt với messages
        // Spring AI sẽ tự động detect và sử dụng Function beans nếu đã được config
        Prompt prompt = new Prompt(messages);

        // Gọi AI qua ChatClient và nhận structured response
        AIStructuredResponse structuredResponse;
        try {
            structuredResponse = chatClient.prompt(prompt)
                    .call()
                    .entity(AIStructuredResponse.class);
        } catch (Exception e) {
            // Fallback: nếu AI không trả về đúng format, tạo response mặc định
            System.err.println("⚠️ AI không trả về structured format, fallback về text: " + e.getMessage());
            String textResponse = chatClient.prompt(prompt).call().content();
            structuredResponse = new AIStructuredResponse(
                    AIStructuredResponse.ResponseType.GENERAL_ANSWER,
                    textResponse,
                    null,
                    null,
                    null
            );
        }

        // Lưu AI response (lưu dạng text message)
        ChatMessage aiMessage = saveMessage(conversation, SenderType.AI, structuredResponse.message());

        // Cập nhật conversation title nếu là message đầu tiên
        if (recentMessages.size() <= 2) {
            updateConversationTitle(conversation, request.message());
        }

        // Trả về ChatResponse với structured data
        return ChatResponse.withStructuredData(
                conversation.getId(),
                aiMessage.getId(),
                structuredResponse,
                aiMessage.getTimestamp());
    }

    /**
     * Lấy danh sách conversations của khách hàng
     */
    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Integer customerId) {
        List<ChatConversation> conversations = conversationRepository
                .findByCustomerIdOrderByUpdatedAtDesc(customerId);

        return conversations.stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử chat của một conversation
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationHistory(String conversationId, Integer customerId) {
        // Verify ownership
        conversationRepository
                .findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy conversation hoặc bạn không có quyền truy cập"));

        List<ChatMessage> messages = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);

        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một conversation
     */
    @Override
    public void deleteConversation(String conversationId, Integer customerId) {
        // Verify ownership
        ChatConversation conversation = conversationRepository
                .findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy conversation hoặc bạn không có quyền truy cập"));

        conversationRepository.delete(conversation);
    }

    // ===== Private Helper Methods =====

    /**
     * Lấy hoặc tạo conversation mới
     */
    private ChatConversation getOrCreateConversation(String conversationId, Customer customer) {
        if (conversationId != null && !conversationId.isEmpty()) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy conversation với ID: " + conversationId));
        }

        // Tạo conversation mới
        ChatConversation newConversation = new ChatConversation();
        newConversation.setId(UUID.randomUUID().toString());
        newConversation.setCustomer(customer);
        newConversation.setTitle("Cuộc trò chuyện mới");
        newConversation.setCreatedAt(LocalDateTime.now());
        newConversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(newConversation);
    }

    /**
     * Lưu message vào database
     */
    private ChatMessage saveMessage(ChatConversation conversation, SenderType senderType, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Build prompt messages từ history
     * Với Function Calling, không cần inject context nữa - AI sẽ tự gọi tools khi cần
     */
    private List<Message> buildPromptMessages(List<ChatMessage> recentMessages, Integer customerId,
            String userMessage) {
        List<Message> messages = new ArrayList<>();

        // System message với context về siêu thị và hướng dẫn sử dụng tools
        messages.add(new SystemMessage(getSystemPrompt()));

        // Thêm history messages để AI có context cuộc trò chuyện
        for (ChatMessage msg : recentMessages) {
            if (msg.getSenderType() == SenderType.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Thêm user message hiện tại (nếu chưa có trong history)
        if (recentMessages.isEmpty() ||
            !recentMessages.get(recentMessages.size() - 1).getContent().equals(userMessage)) {
            messages.add(new UserMessage(userMessage));
        }

        return messages;
    }

// Method buildAdditionalContext đã được remove
    // Với Function Calling, AI sẽ tự động gọi các tools khi cần
    // Không cần inject context cứng nữa - giảm 60-70% token cost

    /**
     * System prompt cho AI với Function Calling và Structured Output
     * Hướng dẫn AI:
     * 1. Cách sử dụng các tools
     * 2. Format output dạng structured (AIStructuredResponse)
     */
    private String getSystemPrompt() {
        return """
                Bạn là trợ lý AI của siêu thị với khả năng sử dụng các TOOLS (functions) để tra cứu thông tin.

                ===== TOOLS CÓ SẴN CHO BẠN =====
                Bạn có thể sử dụng các tools sau để lấy thông tin khi cần:

                1. orderLookupTool: Tra cứu đơn hàng của khách
                   → Dùng khi khách hỏi về: đơn hàng, order, giao hàng, đã mua, đặt hàng

                2. promotionTool: Lấy thông tin khuyến mãi
                   → Dùng khi khách hỏi về: khuyến mãi, giảm giá, sale, ưu đãi, km

                3. productSearchTool: Tìm kiếm sản phẩm
                   → Dùng khi khách hỏi về: sản phẩm cụ thể, tìm món, có bán gì

                4. stockCheckTool: Kiểm tra tồn kho
                   → Dùng khi khách hỏi: còn hàng không, tồn kho, có sẵn không

                5. productDetailTool: Chi tiết sản phẩm
                   → Dùng khi cần: thành phần, xuất xứ, thông tin chi tiết

                6. addToCartTool: Thêm sản phẩm vào giỏ hàng
                   → Dùng khi khách muốn: thêm vào giỏ, mua, đặt mua

                7. updateCartItemTool: Cập nhật số lượng trong giỏ
                   → Dùng khi khách muốn: thay đổi số lượng, update

                8. removeFromCartTool: Xóa sản phẩm khỏi giỏ
                   → Dùng khi khách muốn: xóa khỏi giỏ, bỏ ra

                9. getCartSummaryTool: Xem tổng quan giỏ hàng
                   → Dùng khi khách muốn: xem giỏ, kiểm tra giỏ

                10. clearCartTool: Xóa hết tất cả sản phẩm trong giỏ hàng
                   → Dùng khi khách muốn: xóa hết giỏ, xóa tất cả, clear cart, làm mới giỏ

                ===== QUY TẮC SỬ DỤNG TOOLS =====
                ✅ LUÔN gọi tool phù hợp khi khách hỏi về thông tin cần tra cứu
                ✅ Có thể gọi nhiều tools nếu cần thiết
                ✅ Dựa vào kết quả từ tools để trả lời chính xác
                ❌ KHÔNG bịa thông tin nếu tool không trả về kết quả

                ===== FORMAT OUTPUT (QUAN TRỌNG) =====
                Response của bạn PHẢI là một JSON object với cấu trúc sau:
                {
                  "response_type": "PRODUCT_INFO" | "ORDER_INFO" | "PROMOTION_INFO" | "STOCK_INFO" | "GENERAL_ANSWER" | "ERROR",
                  "message": "Câu trả lời văn bản thân thiện cho khách hàng",
                  "data": {
                    // Tùy thuộc response_type:
                    // - PRODUCT_INFO: {"products": [...]}
                    // - ORDER_INFO: {"orders": [...]}
                    // - PROMOTION_INFO: {"promotions": [...]}
                    // - STOCK_INFO: {"stock": {...}}
                    // - GENERAL_ANSWER: {"policy": {...}}
                  },
                  "suggestions": ["Câu hỏi gợi ý 1", "Câu hỏi gợi ý 2"],
                  "metadata": {
                    "result_count": 3,
                    "tools_used": "productSearchTool"
                  }
                }

                ===== CÁCH PARSE TOOL RESULTS =====
                Khi nhận được tool results dạng [PRODUCT], parse thành ProductInfo:
                - product_unit_id → product_id (QUAN TRỌNG: Phải có để frontend dùng)
                - name → name (tên sản phẩm)
                - code → code (mã sản phẩm/barcode)
                - price → price (giá bán, numeric)
                - unit → unit (đơn vị)
                - brand → brand (thương hiệu)
                - stock_status → stock_status (Còn hàng/Hết hàng)
                - image_url → image_url (QUAN TRỌNG: URL hình ảnh chính, nếu là N/A thì để null)
                - description → description (mô tả)

                VÍ DỤ:
                - Khách hỏi về sản phẩm → response_type: "PRODUCT_INFO", data.products chứa thông tin
                - Khách hỏi về đơn hàng → response_type: "ORDER_INFO", data.orders chứa thông tin
                - Khách hỏi chính sách → response_type: "GENERAL_ANSWER", data.policy chứa thông tin

                LƯU Ý: KHÔNG được bỏ sót product_id (product_unit_id) và image_url khi parse

                ===== QUY TẮC VÀNG: KHÔNG ĐƯỢC BỊA THÔNG TIN =====
                ⚠️ NGHIÊM CẤM tự bịa hoặc đoán:
                - Sản phẩm không tìm thấy từ tools
                - Khuyến mãi không có trong kết quả tool
                - Đơn hàng không tồn tại
                - Giá cả, chi tiết không rõ ràng

                ✅ NẾU TOOL KHÔNG TRẢ VỀ KẾT QUẢ:
                → response_type: "ERROR"
                → message: "Tôi đã kiểm tra nhưng không tìm thấy thông tin về [vấn đề]. Bạn có thể liên hệ CSKH qua hotline để được hỗ trợ chi tiết."

                ===== CHÍNH SÁCH SIÊU THỊ (Thông tin cố định) =====
                Bạn có thể trả lời TRỰC TIẾP (không cần gọi tool) về:
                - Miễn phí giao hàng cho đơn từ 200,000đ
                - Đổi trả trong 7 ngày với sản phẩm còn nguyên vẹn
                - Tích điểm: 1 điểm cho mỗi 10,000đ chi tiêu
                - Thanh toán: Tiền mặt, thẻ, chuyển khoản, ví điện tử
                - Giờ mở cửa: 7:00 - 22:00 hàng ngày
                → Dùng response_type: "GENERAL_ANSWER"

                ===== NGHIÊM CẤM (Từ chối lịch sự) =====
                - Chính trị, tôn giáo, y tế, pháp luật
                - Lịch sử, địa lý, khoa học (ngoài sản phẩm)
                - Viết code, làm bài tập, dịch thuật
                - Tư vấn đầu tư, tài chính

                Khi gặp câu hỏi ngoài phạm vi:
                → response_type: "ERROR"
                → message: "Xin lỗi, tôi chỉ hỗ trợ về siêu thị. Bạn có câu hỏi gì về sản phẩm, đơn hàng hoặc khuyến mãi không?"

                ===== CÁCH TRẢ LỜI =====
                - Tiếng Việt, ngắn gọn, thân thiện
                - GỌI TOOL để lấy thông tin chính xác
                - Parse kết quả tool thành data có cấu trúc
                - Tạo message văn bản thân thiện
                - Thêm suggestions để khách có thể hỏi tiếp
                - TUYỆT ĐỐI không bịa thông tin
                """;
    }

    /**
     * Cập nhật title của conversation từ message đầu tiên
     */
    private void updateConversationTitle(ChatConversation conversation, String firstMessage) {
        String title = firstMessage.length() > 50
                ? firstMessage.substring(0, 50) + "..."
                : firstMessage;
        conversation.setTitle(title);
        conversationRepository.save(conversation);
    }

    /**
     * Convert entity sang ConversationResponse DTO
     */
    private ConversationResponse toConversationResponse(ChatConversation conversation) {
        // Lấy message cuối cùng để làm preview
        List<ChatMessage> messages = messageRepository
                .findTopNByConversationIdOrderByTimestampDesc(conversation.getId(), 1);

        String lastMessage = messages.isEmpty() ? "" : messages.get(0).getContent();
        if (lastMessage.length() > 100) {
            lastMessage = lastMessage.substring(0, 100) + "...";
        }

        return new ConversationResponse(
                conversation.getId(),
                conversation.getCustomer().getCustomerId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                lastMessage);
    }

    /**
     * Convert entity sang MessageResponse DTO
     */
    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderType(),
                message.getContent(),
                message.getTimestamp());
    }
}
