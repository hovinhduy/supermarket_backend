package iuh.fit.supermarket.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;
import iuh.fit.supermarket.dto.chat.structured.ResponseData;
import iuh.fit.supermarket.dto.chat.structured.ResponseMetadata;
import iuh.fit.supermarket.entity.ChatConversation;
import iuh.fit.supermarket.entity.ChatMessage;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.enums.SenderType;
import iuh.fit.supermarket.repository.ChatConversationRepository;
import iuh.fit.supermarket.repository.ChatMessageRepository;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.service.ChatService;
import iuh.fit.supermarket.service.tools.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation của ChatService sử dụng Spring AI với Google GenAI (Gemini)
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    // Tool services
    private final ProductSearchTool productSearchTool;
    private final OrderSearchTool orderSearchTool;
    private final PromotionSearchTool promotionSearchTool;
    private final CartManagementTool addToCartTool;
    private final RemoveFromCartTool removeFromCartTool;
    private final UpdateCartQuantityTool updateCartQuantityTool;
    private final GetCartTool getCartTool;
    private final ClearCartTool clearCartTool;

    @Value("${google.genai.chat.memory-size:10}")
    private Integer memorySize;

    /**
     * Tạo conversation mới cho khách hàng
     */
    @Override
    @Transactional
    public ConversationResponse createConversation(Integer customerId) {
        log.info("Tạo conversation mới cho khách hàng ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy khách hàng với ID: " + customerId));

        ChatConversation conversation = new ChatConversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setCustomer(customer);
        conversation.setTitle("Cuộc trò chuyện mới");

        ChatConversation savedConversation = conversationRepository.save(conversation);
        log.info("Tạo conversation thành công với ID: {}", savedConversation.getId());

        return mapToConversationResponse(savedConversation);
    }

    /**
     * Gửi tin nhắn và nhận response từ AI
     */
    @Override
    @Transactional
    public ChatResponse sendMessage(ChatRequest request, Integer customerId) {
        log.info("Gửi tin nhắn chat - Customer ID: {}, Conversation ID: {}",
                customerId, request.conversationId());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy khách hàng với ID: " + customerId));

        ChatConversation conversation;

        if (request.conversationId() == null || request.conversationId().isBlank()) {
            conversation = createNewConversation(customer);
        } else {
            conversation = conversationRepository.findByIdAndCustomerId(
                            request.conversationId(), customerId)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy conversation hoặc bạn không có quyền truy cập"));
        }

        // Lưu tin nhắn của user
        ChatMessage userMessage = saveUserMessage(conversation, request.message());

        // Lấy lịch sử chat để làm context
        List<ChatMessage> history = messageRepository
                .findTopNByConversationIdOrderByTimestampDesc(conversation.getId(), memorySize);

        // Gọi AI với Spring AI ChatClient
        AIStructuredResponse structuredResponse = callAIWithChatClient(request.message(), history, customer);

        // Extract text message từ structured response
        String aiResponseText = structuredResponse.message();

        // Lưu tin nhắn từ AI
        ChatMessage aiMessage = saveAiMessage(conversation, aiResponseText, structuredResponse);

        // Cập nhật title conversation
        updateConversationTitle(conversation, request.message());

        log.info("Xử lý chat thành công - Message ID: {}, ResponseType: {}",
                aiMessage.getId(), structuredResponse.responseType());

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
        log.info("Lấy danh sách conversations cho khách hàng ID: {}", customerId);

        List<ChatConversation> conversations = conversationRepository
                .findByCustomerIdOrderByUpdatedAtDesc(customerId);

        return conversations.stream()
                .map(this::mapToConversationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử chat của một conversation
     */
    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationHistory(String conversationId, Integer customerId) {
        log.info("Lấy lịch sử chat - Conversation ID: {}, Customer ID: {}", conversationId, customerId);

        conversationRepository.findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy conversation hoặc bạn không có quyền truy cập"));

        List<ChatMessage> messages = messageRepository
                .findByConversationIdOrderByTimestampAsc(conversationId);

        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một conversation
     */
    @Override
    @Transactional
    public void deleteConversation(String conversationId, Integer customerId) {
        log.info("Xóa conversation ID: {}, Customer ID: {}", conversationId, customerId);

        ChatConversation conversation = conversationRepository
                .findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy conversation hoặc bạn không có quyền truy cập"));

        conversationRepository.delete(conversation);
        log.info("Xóa conversation thành công: {}", conversationId);
    }

    // ========== Private Helper Methods ==========

    /**
     * Gọi AI sử dụng Spring AI ChatClient với function calling
     */
    private AIStructuredResponse callAIWithChatClient(String userMessage, List<ChatMessage> history,
                                                      Customer customer) {
        try {
            // Build conversation history
            List<Message> messages = new ArrayList<>();

            // Thêm lịch sử chat (đảo ngược vì query lấy DESC)
            for (int i = history.size() - 1; i >= 0; i--) {
                ChatMessage msg = history.get(i);
                if (msg.getSenderType() == SenderType.USER) {
                    messages.add(new UserMessage(msg.getContent()));
                } else {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }

            // Thêm tin nhắn hiện tại nếu chưa có trong history
            if (history.isEmpty() || !history.get(0).getContent().equals(userMessage)) {
                messages.add(new UserMessage(userMessage));
            }

            // Build customer info
            String customerName = (customer.getUser() != null && customer.getUser().getName() != null)
                    ? customer.getUser().getName()
                    : "Quý khách";
            Integer customerId = customer.getCustomerId();

            // System instruction
            String systemInstruction = buildSystemInstruction(customerName, customerId);

            // Gọi AI với tools - Spring AI yêu cầu bean names
            log.info("🤖 Calling AI with {} messages and 8 tools", messages.size());

            String response = chatClient.prompt()
                    .system(systemInstruction)
                    .messages(messages)
                    .functions(
                            "productSearchTool",
                            "orderSearchTool",
                            "promotionSearchTool",
                            "addToCartTool",
                            "removeFromCartTool",
                            "updateCartQuantityTool",
                            "getCartTool",
                            "clearCartTool"
                    )
                    .call()
                    .content();

            log.info("📥 AI Response received: {} chars", response != null ? response.length() : 0);
            log.debug("AI Response: {}", response);

            // Parse JSON response thành AIStructuredResponse
            return parseStructuredResponse(response);

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI: {}", e.getMessage(), e);
            return new AIStructuredResponse(
                    AIStructuredResponse.ResponseType.ERROR,
                    "Xin lỗi, đã có lỗi xảy ra khi xử lý yêu cầu của bạn. Vui lòng thử lại sau.",
                    null,
                    null,
                    ResponseMetadata.simple(0, "none"));
        }
    }

    /**
     * Build system instruction cho AI
     */
    private String buildSystemInstruction(String customerName, Integer customerId) {
        return """
                Bạn là trợ lý AI thông minh của siêu thị.
                Trả lời bằng tiếng Việt, thân thiện và chuyên nghiệp.

                🔑 NGƯỜI DÙNG HIỆN TẠI: %s (ID: %d)

                ⚠️ QUY TẮC QUAN TRỌNG:
                1. BẮT BUỘC gọi function để lấy dữ liệu - KHÔNG bịa/tưởng tượng!
                2. SAU KHI nhận kết quả từ function, BẮT BUỘC phải trả về JSON response cuối cùng
                3. NẾU function trả [] → response_type: ERROR, message: 'Không tìm thấy'
                4. COPY NGUYÊN VẸN data từ function response vào trường data
                5. MỌI response PHẢI là JSON hợp lệ, KHÔNG được trả plain text
                6. Không thể hiện thông tin nội bộ hệ thống cho khách hàng dưới mọi hình thức
                7. Luôn sử dụng gọi tên khách hàng là %s trong các câu trả lời
                8. Không được tìm id của khách hàng khác
                9. Không được trả lời id nội bộ như product_unit_id, order_id, promotion_id trong message

                🛒 QUY TẮC THÊM VÀO GIỎ HÀNG (QUAN TRỌNG):
                - KHI khách nói "thêm [tên sản phẩm]" hoặc "mua [tên sản phẩm]":
                  1. GỌI productSearchTool với chính xác từ khóa khách nói
                  2. NẾU productSearchTool trả về [] (mảng rỗng):
                     → DỪNG NGAY! KHÔNG được thêm sản phẩm khác!
                     → Trả response_type: ERROR
                     → Message: "Không tìm thấy sản phẩm '[tên]'. Bạn có thể tìm kiếm sản phẩm tương tự hoặc xem danh sách khuyến mãi."
                  3. NẾU productSearchTool trả về danh sách sản phẩm:
                     → CHỈ thêm sản phẩm ĐẦU TIÊN trong danh sách
                     → GỌI addToCartTool với product_unit_id của sản phẩm đầu tiên
                - ⚠️ NGHIÊM CẤM tự ý thêm sản phẩm khác khi không tìm thấy!
                - KHÔNG YÊU CẦU khách cung cấp product_unit_id - TỰ ĐỘNG tìm và thêm!
                - Số lượng mặc định = 1 nếu khách không nói rõ

                💡 QUY TẮC GỢI Ý (SUGGESTIONS):
                - KHI response_type = "PRODUCT_INFO" (tìm kiếm sản phẩm):
                  → Suggestions PHẢI bao gồm: "Thêm [tên sản phẩm] vào giỏ hàng"
                  → Ví dụ: ["Thêm Coca Cola vào giỏ hàng", "Xem sản phẩm tương tự", "Xem khuyến mãi"]
                - KHI response_type = "CART_INFO" (thao tác giỏ hàng):
                  → Suggestions: ["Xem giỏ hàng", "Tiếp tục mua sắm", "Thanh toán"]
                - KHI response_type = "ORDER_INFO":
                  → Suggestions: ["Xem chi tiết đơn hàng", "Mua lại sản phẩm này"]
                - KHI response_type = "PROMOTION_INFO":
                  → Suggestions: ["Xem sản phẩm khuyến mãi", "Tìm kiếm sản phẩm"]

                📚 FUNCTIONS (dùng customerId=%d cho cart/orders):
                - productSearchTool: tìm kiếm sản phẩm
                - orderSearchTool: lấy đơn hàng của khách
                - promotionSearchTool: lấy khuyến mãi
                - addToCartTool: thêm sản phẩm vào giỏ
                - removeFromCartTool: xóa sản phẩm khỏi giỏ
                - updateCartQuantityTool: cập nhật số lượng
                - getCartTool: xem giỏ hàng
                - clearCartTool: xóa toàn bộ giỏ hàng

                📤 FORMAT RESPONSE (PHẢI TRẢ VỀ JSON):
                {
                  "response_type": "PRODUCT_INFO|ORDER_INFO|PROMOTION_INFO|CART_INFO|GENERAL_ANSWER|ERROR",
                  "message": "Tin nhắn trả lời cho khách hàng",
                  "data": {
                    "products": [...],
                    "orders": [...],
                    "promotions": [...],
                    "cart": {...}
                  },
                  "suggestions": ["Gợi ý 1", "Gợi ý 2"],
                  "metadata": {
                    "result_count": 0,
                    "tools_used": "tool_name"
                  }
                }

                💡 VÍ DỤ 1 - TÌM KIẾM SẢN PHẨM:
                User: "Tìm coca"
                1. Gọi productSearchTool("coca", 10)
                2. Nhận [{"product_unit_id": 9, "name": "Coca Cola", "price": 10000, ...}]
                3. Trả JSON:
                {
                  "response_type": "PRODUCT_INFO",
                  "message": "Tôi tìm thấy Coca Cola cho bạn...",
                  "data": {"products": [{"product_unit_id": 9, "name": "Coca Cola", ...}]},
                  "suggestions": ["Thêm Coca Cola vào giỏ hàng", "Xem sản phẩm tương tự", "Xem khuyến mãi"],
                  "metadata": {"result_count": 1, "tools_used": "productSearchTool"}
                }

                💡 VÍ DỤ 2 - THÊM VÀO GIỎ (TÌM THẤY):
                User: "Thêm coca vào giỏ"
                1. Gọi productSearchTool("coca", 10)
                2. Nhận [{"product_unit_id": 9, "name": "Coca Cola", ...}]
                3. Gọi addToCartTool(customerId=%d, productUnitId=9, quantity=1)
                4. Nhận {"total_items": 1, "total_price": 10000, ...}
                5. Trả JSON:
                {
                  "response_type": "CART_INFO",
                  "message": "Đã thêm Coca Cola vào giỏ hàng!",
                  "data": {"cart": {"total_items": 1, ...}},
                  "suggestions": ["Xem giỏ hàng", "Tiếp tục mua sắm", "Thanh toán"],
                  "metadata": {"result_count": 1, "tools_used": "productSearchTool,addToCartTool"}
                }

                💡 VÍ DỤ 3 - THÊM VÀO GIỎ (KHÔNG TÌM THẤY):
                User: "Thêm thùng coca vào giỏ"
                1. Gọi productSearchTool("thùng coca", 10)
                2. Nhận [] (RỖNG - không có kết quả)
                3. DỪNG NGAY! KHÔNG gọi addToCartTool! KHÔNG thêm sản phẩm tương tự!
                4. Trả JSON:
                {
                  "response_type": "ERROR",
                  "message": "Không tìm thấy sản phẩm 'thùng coca'. Bạn có thể tìm kiếm sản phẩm tương tự (ví dụ: 'coca', 'lon coca') hoặc xem danh sách khuyến mãi.",
                  "data": null,
                  "suggestions": ["Tìm coca", "Tìm lon coca", "Xem khuyến mãi"],
                  "metadata": {"result_count": 0, "tools_used": "productSearchTool"}
                }

                💡 VÍ DỤ 4 - SAI: TỰ Ý THÊM SẢN PHẨM KHÁC (NGHIÊM CẤM):
                ❌ KHÔNG ĐƯỢC LÀM NHƯ SAU:
                User: "Thêm thùng coca"
                1. Gọi productSearchTool("thùng coca") → Nhận []
                2. ❌ SAI: Tự ý gọi productSearchTool("coca") và thêm "Lon Coca"
                3. ❌ SAI: "Tôi không thấy thùng coca nhưng đã thêm lon coca"
                → ĐỪNG BAO GIỜ LÀM ĐIỀU NÀY!
                """.formatted(customerName, customerId, customerName, customerId, customerId);
    }

    /**
     * Parse JSON response từ AI thành AIStructuredResponse
     */
    private AIStructuredResponse parseStructuredResponse(String rawResponse) {
        try {
            // Check null/empty response
            if (rawResponse == null || rawResponse.isEmpty()) {
                log.error("❌ AI response is null or empty");
                return new AIStructuredResponse(
                        AIStructuredResponse.ResponseType.ERROR,
                        "Xin lỗi, không nhận được phản hồi từ AI. Vui lòng thử lại.",
                        null,
                        null,
                        ResponseMetadata.simple(0, "none"));
            }

            String cleanedResponse = rawResponse.trim();

            log.debug("Raw response before cleaning: {}",
                    cleanedResponse.substring(0, Math.min(200, cleanedResponse.length())));

            // Remove markdown code blocks nếu có
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            log.debug("Cleaned response (first 300 chars): {}",
                    cleanedResponse.substring(0, Math.min(300, cleanedResponse.length())));

            // Try parse directly
            try {
                AIStructuredResponse response = objectMapper.readValue(cleanedResponse,
                        AIStructuredResponse.class);
                log.info("✅ Successfully parsed as JSON - Type: {}, ResultCount: {}",
                        response.responseType(),
                        response.metadata() != null ? response.metadata().resultCount() : 0);
                return response;
            } catch (Exception firstTry) {
                log.warn("⚠️ First parse failed, trying double-parse: {}", firstTry.getMessage());

                // Try double-parse (Gemini sometimes returns JSON string)
                try {
                    String jsonString = objectMapper.readValue(cleanedResponse, String.class);
                    log.debug("🔄 Detected JSON string, double-parsing");

                    AIStructuredResponse response = objectMapper.readValue(jsonString,
                            AIStructuredResponse.class);
                    log.info("✅ Successfully double-parsed - Type: {}, ResultCount: {}",
                            response.responseType(),
                            response.metadata() != null ? response.metadata().resultCount() : 0);
                    return response;
                } catch (Exception secondTry) {
                    log.error("❌ Double-parse also failed: {}", secondTry.getMessage());
                    throw firstTry;
                }
            }

        } catch (Exception e) {
            log.error("❌ Lỗi parse JSON response: {}", e.getMessage());
            log.error("Raw response (first 500 chars): {}",
                    rawResponse.substring(0, Math.min(500, rawResponse.length())));

            // Fallback: tạo GENERAL_ANSWER với raw text
            return new AIStructuredResponse(
                    AIStructuredResponse.ResponseType.GENERAL_ANSWER,
                    rawResponse,
                    null,
                    List.of("Bạn cần hỗ trợ gì thêm?", "Tôi có thể giúp gì khác?"),
                    ResponseMetadata.simple(0, "none"));
        }
    }

    /**
     * Tạo conversation mới
     */
    private ChatConversation createNewConversation(Customer customer) {
        ChatConversation conversation = new ChatConversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setCustomer(customer);
        conversation.setTitle("Cuộc trò chuyện mới");
        return conversationRepository.save(conversation);
    }

    /**
     * Lưu tin nhắn từ user
     */
    private ChatMessage saveUserMessage(ChatConversation conversation, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(SenderType.USER);
        message.setContent(content);
        return messageRepository.save(message);
    }

    /**
     * Lưu tin nhắn từ AI với structured data
     */
    private ChatMessage saveAiMessage(ChatConversation conversation, String content,
                                      AIStructuredResponse structuredResponse) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(SenderType.AI);
        message.setContent(content);

        // Lưu structured data vào ChatData nếu có
        if (structuredResponse != null && structuredResponse.data() != null) {
            iuh.fit.supermarket.dto.ChatData chatData = convertToChatData(structuredResponse);
            message.setData(chatData);
            log.debug("Saved ChatData with {} products",
                    chatData.getProducts() != null ? chatData.getProducts().size() : 0);
        }

        return messageRepository.save(message);
    }

    /**
     * Convert AIStructuredResponse.ResponseData sang ChatData
     */
    private iuh.fit.supermarket.dto.ChatData convertToChatData(AIStructuredResponse structuredResponse) {
        ResponseData responseData = structuredResponse.data();
        iuh.fit.supermarket.dto.ChatData chatData = new iuh.fit.supermarket.dto.ChatData();

        if (responseData.products() != null && !responseData.products().isEmpty()) {
            chatData.setProducts(List.copyOf(responseData.products()));
        }

        if (responseData.orders() != null && !responseData.orders().isEmpty()) {
            chatData.setOrders(List.copyOf(responseData.orders()));
        }

        if (responseData.promotions() != null && !responseData.promotions().isEmpty()) {
            chatData.setPromotions(List.copyOf(responseData.promotions()));
        }

        if (responseData.cart() != null) {
            chatData.setCart(List.of(responseData.cart()));
        }

        return chatData;
    }

    /**
     * Cập nhật title conversation dựa trên tin nhắn đầu tiên
     */
    private void updateConversationTitle(ChatConversation conversation, String firstMessage) {
        if (conversation.getTitle().equals("Cuộc trò chuyện mới")) {
            String title = firstMessage.length() > 50
                    ? firstMessage.substring(0, 50) + "..."
                    : firstMessage;
            conversation.setTitle(title);
            conversationRepository.save(conversation);
        }
    }

    /**
     * Chuyển đổi entity sang ConversationResponse DTO
     */
    private ConversationResponse mapToConversationResponse(ChatConversation conversation) {
        String lastMessage = null;
        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            ChatMessage lastMsg = conversation.getMessages().get(conversation.getMessages().size() - 1);
            lastMessage = lastMsg.getContent().length() > 100
                    ? lastMsg.getContent().substring(0, 100) + "..."
                    : lastMsg.getContent();
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
     * Chuyển đổi entity sang MessageResponse DTO
     */
    private MessageResponse mapToMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderType(),
                message.getContent(),
                message.getData(),
                message.getTimestamp());
    }
}
