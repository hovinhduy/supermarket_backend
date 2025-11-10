package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.*;
import iuh.fit.supermarket.dto.chat.tool.*;
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
import iuh.fit.supermarket.service.OrderLookupService;
import iuh.fit.supermarket.service.PromotionLookupService;
import iuh.fit.supermarket.service.ProductService;
import iuh.fit.supermarket.service.CartLookupService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Enhanced Chat Service với Manual Function Calling
 *
 * Do Spring AI 1.0.3 chưa hỗ trợ tốt Function Calling,
 * service này implement manual function calling:
 * 1. AI trả về tool calls dạng special format
 * 2. Service parse và execute tools
 * 3. Inject kết quả và gọi AI lại để tạo response cuối
 */
@Service("enhancedChatService")
@Primary  // Ưu tiên service này
@Transactional
@Slf4j
public class EnhancedChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final ChatClient chatClient;

    // Tool services
    private final OrderLookupService orderLookupService;
    private final PromotionLookupService promotionLookupService;
    private final ProductService productService;
    private final CartLookupService cartLookupService;

    private static final int MEMORY_LIMIT = 10;

    // Pattern để detect tool calls từ AI response
    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
        "\\[TOOL_CALL:(\\w+)\\((.*?)\\)\\]"
    );

    public EnhancedChatServiceImpl(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            CustomerRepository customerRepository,
            ChatClient.Builder chatClientBuilder,
            OrderLookupService orderLookupService,
            PromotionLookupService promotionLookupService,
            ProductService productService,
            CartLookupService cartLookupService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.customerRepository = customerRepository;
        this.chatClient = chatClientBuilder.build();
        this.orderLookupService = orderLookupService;
        this.promotionLookupService = promotionLookupService;
        this.productService = productService;
        this.cartLookupService = cartLookupService;

        log.info("🚀 Enhanced Chat Service initialized với Manual Function Calling");
    }

    @Override
    public ConversationResponse createConversation(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));

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

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        // Verify customer
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Không tìm thấy khách hàng với ID: " + request.customerId()));

        // Get or create conversation
        ChatConversation conversation = getOrCreateConversation(request.conversationId(), customer);

        // Save user message
        saveMessage(conversation, SenderType.USER, request.message());

        // Get conversation history
        List<ChatMessage> recentMessages = messageRepository
                .findTopNByConversationIdOrderByTimestampDesc(conversation.getId(), MEMORY_LIMIT);
        Collections.reverse(recentMessages);

        // Step 1: Ask AI which tools to call
        String toolDecision = getToolDecision(request.message(), recentMessages);

        // Step 2: Execute tools if needed
        String toolResults = "";
        if (containsToolCalls(toolDecision)) {
            toolResults = executeToolCalls(toolDecision, request.customerId());
            log.info("📊 Tool results: {}", toolResults);
        }

        // Step 3: Generate final structured response with tool results
        AIStructuredResponse structuredResponse = generateFinalStructuredResponse(
            request.message(),
            recentMessages,
            toolResults
        );

        // Save AI response (lưu dạng text message)
        ChatMessage aiMessage = saveMessage(conversation, SenderType.AI, structuredResponse.message());

        // Update conversation title if first message
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
     * Step 1: Hỏi AI cần gọi tools nào
     */
    private String getToolDecision(String userMessage, List<ChatMessage> history) {
        String toolPrompt = getToolDecisionPrompt();

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(toolPrompt));

        // Add history
        for (ChatMessage msg : history) {
            if (msg.getSenderType() == SenderType.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Add current message
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);
        String decision = chatClient.prompt(prompt).call().content();

        log.info("🤖 AI Tool Decision: {}", decision);
        return decision;
    }

    /**
     * Check if response contains tool calls
     */
    private boolean containsToolCalls(String response) {
        return response != null && response.contains("[TOOL_CALL:");
    }

    /**
     * Execute tool calls từ AI response
     */
    private String executeToolCalls(String toolDecision, Integer customerId) {
        StringBuilder results = new StringBuilder();
        Matcher matcher = TOOL_CALL_PATTERN.matcher(toolDecision);

        while (matcher.find()) {
            String toolName = matcher.group(1);
            String params = matcher.group(2);

            log.info("🔧 Executing tool: {} with params: {}", toolName, params);

            String result = executeToolByName(toolName, params, customerId);
            results.append("\n[TOOL_RESULT:").append(toolName).append("]\n");
            results.append(result);
            results.append("\n[/TOOL_RESULT]\n");
        }

        return results.toString();
    }

    /**
     * Execute a specific tool by name
     */
    private String executeToolByName(String toolName, String params, Integer customerId) {
        try {
            switch (toolName) {
                case "orderLookup":
                    return orderLookupService.getRecentOrders(customerId, 3);

                case "promotions":
                    return promotionLookupService.getActivePromotions(5);

                case "productSearch":
                    // Parse query from params
                    String query = extractParam(params, "query");
                    return productService.searchProductsForAI(query, 5);

                case "stockCheck":
                    // Parse productId from params
                    Long productId = Long.parseLong(extractParam(params, "productId"));
                    return productService.checkStockForAI(productId);

                case "productDetail":
                    // Parse productId from params
                    Long detailId = Long.parseLong(extractParam(params, "productId"));
                    return productService.getProductDetailsForAI(detailId);

                case "addToCart":
                    // Parse productUnitId, productName, quantity from params
                    Long productUnitId = Long.parseLong(extractParam(params, "productUnitId"));
                    String productName = extractParam(params, "productName");
                    String quantityStr = extractParam(params, "quantity");
                    Integer quantity = quantityStr.isEmpty() ? 1 : Integer.parseInt(quantityStr);
                    return cartLookupService.addToCart(customerId, productUnitId, productName, quantity);

                case "updateCartItem":
                    // Parse productUnitId, productName, newQuantity from params
                    Long updateProductUnitId = Long.parseLong(extractParam(params, "productUnitId"));
                    String updateProductName = extractParam(params, "productName");
                    Integer newQuantity = Integer.parseInt(extractParam(params, "newQuantity"));
                    return cartLookupService.updateCartItem(customerId, updateProductUnitId, updateProductName, newQuantity);

                case "removeFromCart":
                    // Parse productUnitId, productName from params
                    Long removeProductUnitId = Long.parseLong(extractParam(params, "productUnitId"));
                    String removeProductName = extractParam(params, "productName");
                    return cartLookupService.removeFromCart(customerId, removeProductUnitId, removeProductName);

                case "getCartSummary":
                    // No params needed
                    return cartLookupService.getCartSummary(customerId);

                default:
                    return "Tool không được hỗ trợ: " + toolName;
            }
        } catch (Exception e) {
            log.error("Error executing tool {}: {}", toolName, e.getMessage());
            return "Lỗi khi thực thi tool " + toolName + ": " + e.getMessage();
        }
    }

    /**
     * Extract parameter value from params string
     */
    private String extractParam(String params, String paramName) {
        // Simple parsing: query='value' or productId=123
        Pattern pattern = Pattern.compile(paramName + "=['\"](.*?)['\"]");
        Matcher matcher = pattern.matcher(params);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Try without quotes
        pattern = Pattern.compile(paramName + "=(\\w+)");
        matcher = pattern.matcher(params);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "";
    }

    /**
     * Step 3: Generate final structured response với tool results
     */
    private AIStructuredResponse generateFinalStructuredResponse(String userMessage, List<ChatMessage> history, String toolResults) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(getFinalResponsePrompt()));

        // Add history
        for (ChatMessage msg : history) {
            if (msg.getSenderType() == SenderType.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }

        // Add tool results if any
        if (!toolResults.isEmpty()) {
            messages.add(new SystemMessage("Kết quả từ tools:\n" + toolResults));
        }

        // Add current message
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);

        // Gọi AI và nhận structured response
        try {
            return chatClient.prompt(prompt).call().entity(AIStructuredResponse.class);
        } catch (Exception e) {
            log.error("⚠️ AI không trả về structured format, fallback về text: {}", e.getMessage());
            String textResponse = chatClient.prompt(prompt).call().content();
            return new AIStructuredResponse(
                    AIStructuredResponse.ResponseType.GENERAL_ANSWER,
                    textResponse,
                    null,
                    null,
                    null
            );
        }
    }

    /**
     * Prompt để AI quyết định gọi tools nào
     */
    private String getToolDecisionPrompt() {
        return """
            Bạn là AI assistant có khả năng gọi các tools sau:

            1. orderLookup() - Tra cứu đơn hàng của khách
               Dùng khi: hỏi về đơn hàng, order, giao hàng, đã mua

            2. promotions() - Lấy khuyến mãi hiện có
               Dùng khi: hỏi về khuyến mãi, giảm giá, sale, ưu đãi

            3. productSearch(query='keyword') - Tìm sản phẩm
               Dùng khi: tìm sản phẩm, có bán gì

            4. stockCheck(productId=123) - Kiểm tra tồn kho
               Dùng khi: hỏi còn hàng không, tồn kho

            5. productDetail(productId=123) - Chi tiết sản phẩm
               Dùng khi: hỏi thông tin chi tiết, thành phần

            6. addToCart(productUnitId=123, productName='...', quantity=2) - Thêm vào giỏ hàng
               Dùng khi: khách muốn thêm vào giỏ, mua

            7. updateCartItem(productUnitId=123, productName='...', newQuantity=5) - Cập nhật giỏ
               Dùng khi: khách muốn thay đổi số lượng

            8. removeFromCart(productUnitId=123, productName='...') - Xóa khỏi giỏ
               Dùng khi: khách muốn xóa sản phẩm khỏi giỏ

            9. getCartSummary() - Xem giỏ hàng
               Dùng khi: khách muốn xem giỏ, kiểm tra giỏ

            NHIỆM VỤ: Phân tích câu hỏi và trả về các tool cần gọi.

            FORMAT OUTPUT:
            - Nếu cần gọi tool: [TOOL_CALL:toolName(params)]
            - Có thể gọi nhiều tools
            - Nếu không cần tool: "NO_TOOLS_NEEDED"

            Ví dụ:
            - "Có khuyến mãi gì?" → [TOOL_CALL:promotions()]
            - "Tìm sữa tươi" → [TOOL_CALL:productSearch(query='sữa tươi')]
            - "Thêm 2 lon coca vào giỏ" → [TOOL_CALL:addToCart(productUnitId=1, productName='Coca Cola lon', quantity=2)]
            - "Xem giỏ hàng của tôi" → [TOOL_CALL:getCartSummary()]
            - "Xin chào" → NO_TOOLS_NEEDED
            """;
    }

    /**
     * Prompt để generate final structured response
     */
    private String getFinalResponsePrompt() {
        return """
            Bạn là trợ lý AI của siêu thị.

            NHIỆM VỤ: Trả lời câu hỏi của khách hàng dựa trên:
            1. Lịch sử cuộc trò chuyện (nếu có)
            2. Kết quả từ tools (nếu có trong [TOOL_RESULT])
            3. Kiến thức chung về siêu thị

            ===== FORMAT OUTPUT (QUAN TRỌNG) =====
            Response của bạn PHẢI là một JSON object với cấu trúc sau:
            {
              "response_type": "PRODUCT_INFO" | "ORDER_INFO" | "PROMOTION_INFO" | "STOCK_INFO" | "GENERAL_ANSWER" | "ERROR",
              "message": "Câu trả lời văn bản thân thiện cho khách hàng",
              "data": {
                // Parse kết quả từ tools thành structured data
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
            - image_url → image_url (QUAN TRỌNG: URL hình ảnh chính)
            - description → description (mô tả)

            QUY TẮC:
            - Trả lời bằng tiếng Việt, thân thiện
            - Nếu có kết quả từ tools, parse ĐÚNG các field vào structured data
            - KHÔNG được bỏ sót product_unit_id và image_url
            - message: câu trả lời văn bản cho người dùng
            - data: dữ liệu có cấu trúc từ tool results
            - suggestions: gợi ý câu hỏi tiếp theo
            - Nếu không có thông tin, response_type = "ERROR"
            - KHÔNG bịa thông tin

            CHÍNH SÁCH SIÊU THỊ:
            - Miễn phí giao hàng đơn từ 200,000đ
            - Đổi trả trong 7 ngày
            - Giờ mở cửa: 7:00 - 22:00
            → Dùng response_type: "GENERAL_ANSWER"
            """;
    }

    // Các methods khác giữ nguyên từ ChatServiceImpl gốc
    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Integer customerId) {
        List<ChatConversation> conversations = conversationRepository
                .findByCustomerIdOrderByUpdatedAtDesc(customerId);

        return conversations.stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationHistory(String conversationId, Integer customerId) {
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

    @Override
    public void deleteConversation(String conversationId, Integer customerId) {
        ChatConversation conversation = conversationRepository
                .findByIdAndCustomerId(conversationId, customerId)
                .orElseThrow(
                        () -> new RuntimeException("Không tìm thấy conversation hoặc bạn không có quyền truy cập"));

        conversationRepository.delete(conversation);
    }

    // Helper methods
    private ChatConversation getOrCreateConversation(String conversationId, Customer customer) {
        if (conversationId != null && !conversationId.isEmpty()) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy conversation với ID: " + conversationId));
        }

        ChatConversation newConversation = new ChatConversation();
        newConversation.setId(UUID.randomUUID().toString());
        newConversation.setCustomer(customer);
        newConversation.setTitle("Cuộc trò chuyện mới");
        newConversation.setCreatedAt(LocalDateTime.now());
        newConversation.setUpdatedAt(LocalDateTime.now());

        return conversationRepository.save(newConversation);
    }

    private ChatMessage saveMessage(ChatConversation conversation, SenderType senderType, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    private void updateConversationTitle(ChatConversation conversation, String firstMessage) {
        String title = firstMessage.length() > 50
                ? firstMessage.substring(0, 50) + "..."
                : firstMessage;
        conversation.setTitle(title);
        conversationRepository.save(conversation);
    }

    private ConversationResponse toConversationResponse(ChatConversation conversation) {
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

    private MessageResponse toMessageResponse(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderType(),
                message.getContent(),
                message.getTimestamp());
    }
}