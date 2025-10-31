package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;
import iuh.fit.supermarket.entity.ChatConversation;
import iuh.fit.supermarket.entity.ChatMessage;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.SenderType;
import iuh.fit.supermarket.exception.CustomerNotFoundException;
import iuh.fit.supermarket.repository.ChatConversationRepository;
import iuh.fit.supermarket.repository.ChatMessageRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import iuh.fit.supermarket.service.OrderLookupService;
import iuh.fit.supermarket.service.PromotionLookupService;
// import iuh.fit.supermarket.service.ProductRecommendationService; // TODO: Sẽ dùng cho AI function calling
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation của ChatService
 * Xử lý logic chat AI với conversation memory và product recommendation
 */
@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final OrderLookupService orderLookupService;
    private final PromotionLookupService promotionLookupService;
    private final ChatClient chatClient;

    // TODO: Tích hợp ProductRecommendationService trong tương lai cho AI function
    // calling
    // private final ProductRecommendationService productRecommendationService;

    private static final int MEMORY_LIMIT = 10; // Giữ 10 messages gần nhất làm context

    /**
     * Constructor injection cho tất cả dependencies
     */
    public ChatServiceImpl(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            CustomerRepository customerRepository,
            OrderLookupService orderLookupService,
            PromotionLookupService promotionLookupService,
            ChatClient.Builder chatClientBuilder) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.orderLookupService = orderLookupService;
        this.promotionLookupService = promotionLookupService;
        this.chatClient = chatClientBuilder.build();
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
     * Gửi tin nhắn và nhận response từ AI
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

        // Build prompt với system message, context, history, và user message
        List<Message> messages = buildPromptMessages(recentMessages, request.customerId(), request.message());

        // Gọi AI
        Prompt prompt = new Prompt(messages);
        String aiResponse = chatClient.prompt(prompt).call().content();

        // Lưu AI response
        ChatMessage aiMessage = saveMessage(conversation, SenderType.AI, aiResponse);

        // Cập nhật conversation title nếu là message đầu tiên
        if (recentMessages.size() <= 2) {
            updateConversationTitle(conversation, request.message());
        }

        return new ChatResponse(
                conversation.getId(),
                aiMessage.getId(),
                aiResponse,
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
     * Build prompt messages từ history với context bổ sung
     */
    private List<Message> buildPromptMessages(List<ChatMessage> recentMessages, Integer customerId,
            String userMessage) {
        List<Message> messages = new ArrayList<>();

        // System message với context về siêu thị
        messages.add(new SystemMessage(getSystemPrompt()));

        // Thêm context động về đơn hàng và khuyến mãi
        String additionalContext = buildAdditionalContext(customerId, userMessage);
        if (!additionalContext.isEmpty()) {
            messages.add(new SystemMessage(additionalContext));
        }

        // Thêm history messages
        for (ChatMessage msg : recentMessages) {
            if (msg.getSenderType() == SenderType.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        return messages;
    }

    /**
     * Xây dựng context đầy đủ (luôn inject cả orders và promotions)
     * AI sẽ tự quyết định dùng thông tin nào
     */
    private String buildAdditionalContext(Integer customerId, String userMessage) {
        StringBuilder context = new StringBuilder();

        System.out.println("=== Building Full Context ===");
        System.out.println("User message: " + userMessage);

        // LUÔN lấy thông tin khuyến mãi
        try {
            String promotions = promotionLookupService.getActivePromotions(5);
            System.out.println(">>> Injecting promotions data");
            System.out.println("Promotions: " + promotions);
            context.append("\n[KHUYẾN MÃI HIỆN CÓ - Dùng khi khách hỏi về khuyến mãi, giảm giá, sale]\n");
            context.append(promotions);
            context.append("\n");
        } catch (Exception e) {
            System.err.println("Error loading promotions: " + e.getMessage());
        }

        // LUÔN lấy thông tin đơn hàng gần đây (giới hạn 3 để tiết kiệm token)
        try {
            String orders = orderLookupService.getRecentOrders(customerId, 3);
            System.out.println(">>> Injecting orders data");
            System.out.println("Orders: " + orders);
            context.append("\n[ĐƠN HÀNG GẦN ĐÂY - Dùng khi khách hỏi về đơn hàng, giao hàng]\n");
            context.append(orders);
            context.append("\n");
        } catch (Exception e) {
            System.err.println("Error loading orders: " + e.getMessage());
        }

        System.out.println("Final context length: " + context.length());
        System.out.println("=== End Building Context ===");

        return context.toString();
    }

    /**
     * System prompt cho AI - STRICT: Không được bịa thông tin
     */
    private String getSystemPrompt() {
        return """
                Bạn là trợ lý AI của siêu thị. BẠN CHỈ TRẢ LỜI DỰA TRÊN THÔNG TIN ĐƯỢC CUNG CẤP.
                
                ===== QUY TẮC VÀNG: KHÔNG ĐƯỢC BỊA THÔNG TIN =====
                ⚠️ NGHIÊM CẤM tự bịa hoặc đoán:
                - Sản phẩm không có trong hệ thống
                - Chương trình khuyến mãi không được cung cấp
                - Thông tin đơn hàng không có trong context
                - Giá cả, chi tiết sản phẩm không rõ ràng
                
                ✅ NẾU KHÔNG CÓ THÔNG TIN, HÃY NÓI:
                "Hiện tại tôi không có thông tin về [vấn đề]. Bạn có thể liên hệ bộ phận CSKH 
                để được hỗ trợ chi tiết hơn."
                
                ===== PHẠM VI HOẠT ĐỘNG =====
                Bạn CHỈ được trả lời về:
                1. Sản phẩm: CHỈ dựa trên thông tin được cung cấp trong context
                2. Đơn hàng: CHỈ dựa trên dữ liệu trong [Thông tin đơn hàng]
                3. Khuyến mãi: CHỈ dựa trên dữ liệu trong [Khuyến mãi hiện có]
                4. Chính sách: CHỈ thông tin cố định bên dưới
                5. Hỗ trợ mua sắm: Tư vấn chung, không bịa sản phẩm cụ thể
                
                ===== CÁCH SỬ DỤNG CONTEXT =====
                Context luôn chứa 2 phần:
                1. [KHUYẾN MÃI HIỆN CÓ] - Dùng khi khách hỏi về: khuyến mãi, giảm giá, sale, ưu đãi
                2. [ĐƠN HÀNG GẦN ĐÂY] - Dùng khi khách hỏi về: đơn hàng, đặt hàng, mua hàng, giao hàng
                
                Khi trả lời về đơn hàng:
                → Tìm trong [ĐƠN HÀNG GẦN ĐÂY]
                → Nếu "chưa có đơn hàng": "Tôi không thấy đơn hàng nào. Bạn muốn đặt hàng không?"
                
                Khi trả lời về khuyến mãi:
                → Tìm trong [KHUYẾN MÃI HIỆN CÓ]
                → Nếu "không có khuyến mãi": "Hiện không có KM. Liên hệ cửa hàng để cập nhật."
                
                QUAN TRỌNG: Hiểu intent dù có lỗi chính tả:
                - "khuyen mai", "khuển mãi", "km" → Đều là hỏi về khuyến mãi
                - "don hang", "đơn hàng", "order" → Đều là hỏi về đơn hàng
                
                ===== CHÍNH SÁCH SIÊU THỊ (Thông tin cố định) =====
                - Miễn phí giao hàng cho đơn từ 200,000đ
                - Đổi trả trong 7 ngày với sản phẩm còn nguyên vẹn, hóa đơn đầy đủ
                - Tích điểm: 1 điểm cho mỗi 10,000đ chi tiêu
                - Thanh toán: Tiền mặt, thẻ, chuyển khoản, ví điện tử (MoMo, ZaloPay)
                - Giờ mở cửa: 7:00 - 22:00 hàng ngày
                
                ===== NGHIÊM CẤM (Từ chối lịch sự) =====
                - Chính trị, tôn giáo, y tế, pháp luật
                - Lịch sử, địa lý, khoa học (ngoài sản phẩm)
                - Viết code, làm bài tập, dịch thuật
                - Tư vấn đầu tư, tài chính
                - Câu hỏi vui không liên quan
                
                Khi gặp câu hỏi ngoài phạm vi:
                "Xin lỗi, tôi chỉ hỗ trợ về siêu thị (sản phẩm, đơn hàng, khuyến mãi). 
                Bạn có câu hỏi gì về siêu thị không?"
                
                ===== CÁCH TRẢ LỜI =====
                - Tiếng Việt, ngắn gọn, thân thiện
                - CHỈ dùng thông tin có trong context hoặc chính sách cố định
                - Nếu không biết → Thừa nhận và hướng dẫn liên hệ CSKH
                - Không bịa số liệu, tên sản phẩm, chương trình khuyến mãi
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
