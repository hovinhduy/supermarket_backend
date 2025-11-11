package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.ChatData;
import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;
import iuh.fit.supermarket.dto.chat.structured.AIStructuredResponse;
import iuh.fit.supermarket.dto.chat.structured.ResponseData;
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
 * Function Calling cho phép AI tự động gọi các tools phù hợp dựa trên intent
 * của user,
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
            functionBeans.keySet().forEach(name -> System.out.println("   - " + name));
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
     * 3. Biết thông tin khách hàng đang chat để cá nhân hóa trải nghiệm
     */
    @Override
    public ChatResponse sendMessage(ChatRequest request, Integer customerId) {
        // Verify customer tồn tại
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Không tìm thấy khách hàng với ID: " + customerId));

        // Lấy hoặc tạo conversation
        ChatConversation conversation = getOrCreateConversation(request.conversationId(), customer);

        // Lưu user message
        saveMessage(conversation, SenderType.USER, request.message());

        // Lấy conversation history để build context
        List<ChatMessage> recentMessages = messageRepository
                .findTopNByConversationIdOrderByTimestampDesc(conversation.getId(), MEMORY_LIMIT);
        Collections.reverse(recentMessages); // Đảo ngược để có thứ tự chronological

        // Build prompt messages với system message và history (bao gồm thông tin
        // customer)
        List<Message> messages = buildPromptMessages(recentMessages, customer, request.message());

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
                    null);
        }

        // Lưu AI response với structured data
        ChatData chatData = convertToChatData(structuredResponse.data());
        ChatMessage aiMessage = saveMessage(conversation, SenderType.AI, structuredResponse.message(), chatData);

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
        return saveMessage(conversation, senderType, content, null);
    }

    /**
     * Lưu message với data vào database
     */
    private ChatMessage saveMessage(ChatConversation conversation, SenderType senderType, String content,
            ChatData data) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setData(data);
        message.setTimestamp(LocalDateTime.now());

        return messageRepository.save(message);
    }

    /**
     * Convert ResponseData từ AI sang ChatData để lưu vào database
     */
    private ChatData convertToChatData(ResponseData responseData) {
        if (responseData == null) {
            return null;
        }

        ChatData chatData = new ChatData();

        // Convert các typed lists sang List<Object> để lưu dạng JSON
        if (responseData.products() != null) {
            chatData.setProducts(new ArrayList<>(responseData.products()));
        }

        if (responseData.orders() != null) {
            chatData.setOrders(new ArrayList<>(responseData.orders()));
        }

        if (responseData.promotions() != null) {
            chatData.setPromotions(new ArrayList<>(responseData.promotions()));
        }

        if (responseData.stock() != null) {
            chatData.setStock(List.of(responseData.stock()));
        }

        if (responseData.policy() != null) {
            chatData.setPolicy(List.of(responseData.policy()));
        }

        if (responseData.cart() != null) {
            chatData.setCart(List.of(responseData.cart()));
        }

        return chatData;
    }

    /**
     * Build prompt messages từ history với thông tin customer
     * Với Function Calling, không cần inject context nữa - AI sẽ tự gọi tools khi
     * cần
     */
    private List<Message> buildPromptMessages(List<ChatMessage> recentMessages, Customer customer,
            String userMessage) {
        List<Message> messages = new ArrayList<>();

        // System message với context về siêu thị, hướng dẫn sử dụng tools và thông tin
        // customer
        messages.add(new SystemMessage(getSystemPrompt(customer)));

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
     * 3. Thông tin về khách hàng đang chat
     */
    private String getSystemPrompt(Customer customer) {
        String customerInfo = buildCustomerInfo(customer);

        return """
                Bạn là trợ lý AI của siêu thị với khả năng sử dụng các TOOLS (functions) để tra cứu thông tin.

                ===== THÔNG TIN KHÁCH HÀNG =====
                %s

                Hãy sử dụng thông tin này để cá nhân hóa trải nghiệm cho khách hàng.
                Khi khách hỏi về "đơn hàng của tôi", "giỏ hàng của tôi", bạn đã biết họ là ai.

                ⚠️ QUAN TRỌNG - BẢO MẬT:
                - KHÔNG bao giờ tiết lộ ID khách hàng (customer_id) trong phản hồi
                - Chỉ dùng tên khách hàng hoặc "bạn" khi nhắc đến khách hàng
                - Ví dụ: Nói "Đây là giỏ hàng của bạn" thay vì "Đây là giỏ hàng của khách hàng ID 3"

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
                   → ⚠️ BẮT BUỘC: PHẢI gọi productSearchTool TRƯỚC để tìm đúng sản phẩm
                   → Chỉ dùng product_unit_id từ kết quả productSearchTool, KHÔNG ĐƯỢC tự bịa

                   → ⚠️ QUY TRÌNH BẮT BUỘC:
                      Bước 1: Gọi productSearchTool để tìm sản phẩm
                      Bước 2: Kiểm tra kết quả:
                         • Nếu KHÔNG TÌM THẤY (0 kết quả):
                           → response_type: "ERROR"
                           → message: "Xin lỗi, tôi không tìm thấy [tên sản phẩm] trong cửa hàng. Bạn có thể thử tìm sản phẩm khác hoặc liên hệ nhân viên."
                           → DỪNG LẠI, KHÔNG ĐƯỢC thêm sản phẩm bất kỳ

                         • Nếu TÌM THẤY NHIỀU KẾT QUẢ (>1 sản phẩm với đơn vị khác nhau):
                           → response_type: "PRODUCT_INFO"
                           → message: "Chúng tôi có [tên sản phẩm] với các loại: [liệt kê]. Bạn muốn thêm loại nào vào giỏ hàng?"
                           → data.products: [danh sách các sản phẩm tìm được]
                           → DỪNG LẠI, CHỜ khách hàng chọn rõ ràng

                         • Nếu TÌM THẤY ĐÚNG 1 KẾT QUẢ:
                           → Tiếp tục Bước 3

                      Bước 3: Gọi addToCartTool(productUnitId=X, quantity=Y) - CHỈ 1 LẦN

                   → ⚠️ QUAN TRỌNG: CHỈ gọi 1 lần với đúng số lượng khách yêu cầu
                   → KHÔNG BAO GIỜ gọi addToCart nhiều lần cho cùng một yêu cầu
                   → KHÔNG BAO GIỜ thêm sản phẩm khác với yêu cầu của khách
                   → KHÔNG BAO GIỜ tự ý chọn sản phẩm khi có nhiều lựa chọn

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
                ✅ Có thể gọi nhiều tools KHÁC NHAU nếu cần thiết (VD: productSearchTool + addToCartTool)
                ✅ Dựa vào kết quả từ tools để trả lời chính xác
                ❌ KHÔNG bịa thông tin nếu tool không trả về kết quả
                ❌ NGHIÊM CẤM gọi cùng một tool nhiều lần cho một yêu cầu đơn lẻ

                ⚠️ QUY TẮC VÀNG VỀ SỐ LƯỢNG:
                - "thêm 1 lon coca" = addToCartTool(quantity=1) - GỌI 1 LẦN DUY NHẤT
                - "thêm 5 hộp sữa" = addToCartTool(quantity=5) - GỌI 1 LẦN DUY NHẤT
                - Số lượng đã được truyền vào parameter quantity, KHÔNG gọi tool nhiều lần
                - Tool addToCartTool đã tự động xử lý số lượng bên trong

                VÍ DỤ ĐÚNG:

                Trường hợp 1 - Tìm thấy ĐÚNG 1 sản phẩm:
                - User: "thêm 2 lon coca vào giỏ"
                - AI:
                  Bước 1: [Gọi productSearchTool("coca")]
                          → tìm thấy 1 kết quả: "Coca Cola lon", product_unit_id=3
                  Bước 2: [Gọi addToCartTool(productUnitId=3, quantity=2)] - CHỈ 1 LẦN
                  Bước 3: Parse kết quả [CART] từ tool
                  Bước 4: Trả về CART_INFO với message "Đã thêm 2 lon Coca Cola vào giỏ hàng."

                Trường hợp 2 - Tìm thấy NHIỀU sản phẩm:
                - User: "thêm coca vào giỏ"
                - AI:
                  Bước 1: [Gọi productSearchTool("coca")]
                          → tìm thấy 3 kết quả:
                            1. Coca Cola lon 330ml (product_unit_id=3)
                            2. Coca Cola lốc 6 lon (product_unit_id=15)
                            3. Coca Cola thùng 24 lon (product_unit_id=28)
                  Bước 2: DỪNG LẠI, trả về response:
                          response_type: "PRODUCT_INFO"
                          message: "Chúng tôi có Coca Cola với các loại: lon 330ml (12,000₫), lốc 6 lon (70,000₫), thùng 24 lon (270,000₫). Bạn muốn thêm loại nào vào giỏ hàng?"
                          data.products: [3 sản phẩm trên]
                  Bước 3: CHỜ khách hàng trả lời rõ ràng (VD: "thêm lon" hoặc "thêm lốc")

                Trường hợp 3 - KHÔNG tìm thấy sản phẩm:
                - User: "thêm pepsi vào giỏ"
                - AI:
                  Bước 1: [Gọi productSearchTool("pepsi")]
                          → không tìm thấy kết quả nào
                  Bước 2: Trả về response:
                          response_type: "ERROR"
                          message: "Xin lỗi, tôi không tìm thấy Pepsi trong cửa hàng. Bạn có thể thử tìm sản phẩm khác hoặc liên hệ nhân viên."
                  Bước 3: KHÔNG GỌI addToCartTool, KHÔNG thêm sản phẩm bất kỳ

                VÍ DỤ SAI (KHÔNG ĐƯỢC LÀM):
                ❌ SAI 1 - Gọi tool nhiều lần:
                - User: "thêm 2 lon coca vào giỏ"
                - AI: [Gọi addToCartTool(productUnitId=3, quantity=1)] - Lần 1
                - AI: [Gọi addToCartTool(productUnitId=3, quantity=1)] - Lần 2 ❌ SAI

                ❌ SAI 2 - Không tìm kiếm sản phẩm hoặc thêm sản phẩm sai:
                - User: "thêm coca vào giỏ"
                - AI: [Gọi addToCartTool(productUnitId=1, quantity=1)] ❌ SAI - product_unit_id=1 là sữa, không phải coca
                - AI phải gọi productSearchTool("coca") trước để lấy đúng product_unit_id

                ❌ SAI 4 - Tự ý chọn khi có nhiều kết quả:
                - User: "thêm coca vào giỏ"
                - AI: [Gọi productSearchTool("coca")] → tìm thấy 3 loại (lon, lốc, thùng)
                - AI: [Gọi addToCartTool(productUnitId=3)] ❌ SAI - tự ý chọn lon mà không hỏi khách
                - AI phải HỎI khách chọn loại nào

                ❌ SAI 5 - Thêm sản phẩm khác khi không tìm thấy:
                - User: "thêm pepsi vào giỏ"
                - AI: [Gọi productSearchTool("pepsi")] → không tìm thấy
                - AI: [Gọi addToCartTool(productUnitId=3)] ❌ SAI - thêm Coca thay vì báo không có Pepsi
                - AI phải báo "Không tìm thấy Pepsi", KHÔNG được thêm sản phẩm khác

                ❌ SAI 3 - Tự bịa thông tin không có trong tool results:
                - Tool trả về: "Tổng cần thanh toán: 105,000₫"
                - AI suggestions: "Mua thêm 110,000₫ để được MIỄN PHÍ SHIP!" ❌ SAI - con số 110,000 không có trong tool output
                - AI CHỈ ĐƯỢC dùng thông tin từ tool results, KHÔNG tự tính toán hoặc bịa

                ===== FORMAT OUTPUT (QUAN TRỌNG) =====
                Response của bạn PHẢI là một JSON object với cấu trúc sau:
                {
                  "response_type": "PRODUCT_INFO" | "ORDER_INFO" | "PROMOTION_INFO" | "STOCK_INFO" | "CART_INFO" | "GENERAL_ANSWER" | "ERROR",
                  "message": "Câu trả lời văn bản thân thiện cho khách hàng",
                  "data": {
                    // Tùy thuộc response_type:
                    // - PRODUCT_INFO: {"products": [...]}
                    // - ORDER_INFO: {"orders": [...]}
                    // - PROMOTION_INFO: {"promotions": [...]}
                    // - STOCK_INFO: {"stock": {...}}
                    // - CART_INFO: {"cart": {...}}
                    // - GENERAL_ANSWER: {"policy": {...}}
                  },
                  "suggestions": ["Câu hỏi gợi ý 1", "Câu hỏi gợi ý 2"],
                  "metadata": {
                    "result_count": 3,
                    "tools_used": "productSearchTool"
                  }
                }

                ===== CÁCH PARSE TOOL RESULTS =====

                1. Khi nhận được tool results dạng [PRODUCT], parse thành ProductInfo:
                   - product_unit_id → product_id (QUAN TRỌNG: Phải có để frontend dùng)
                   - name → name (tên sản phẩm)
                   - code → code (mã sản phẩm/barcode)
                   - price → price (giá bán, numeric)
                   - unit → unit (đơn vị)
                   - brand → brand (thương hiệu)
                   - stock_status → stock_status (Còn hàng/Hết hàng)
                   - image_url → image_url (QUAN TRỌNG: URL hình ảnh chính, nếu là N/A thì để null)
                   - description → description (mô tả)

                2. Khi nhận được tool results dạng [ORDER], parse thành OrderInfo:
                   - order_id → order_id (QUAN TRỌNG: ID đơn hàng, numeric)
                   - order_code → order_code (QUAN TRỌNG: Mã đơn hàng duy nhất, string)
                   - order_date → order_date (ngày đặt hàng, ISO datetime)
                   - status → status (trạng thái đơn hàng bằng tiếng Việt)
                   - total_amount → total_amount (tổng tiền, numeric)
                   - delivery_type → delivery_method (loại hình giao hàng, optional)
                   - delivery_address → delivery_address (địa chỉ giao hàng, optional)

                3. Khi nhận được tool results dạng [PROMOTIONS], parse thành PromotionInfo:
                   Tool trả về JSON objects, mỗi object có cấu trúc:
                   {
                     "promotion_line_id": number,
                     "promotion_code": "string",
                     "name": "Tên chương trình",
                     "description": "Mô tả chi tiết",
                     "summary": "Mô tả ngắn gọn dễ hiểu" (VD: "Mua 5 tặng 1", "Giảm 10% đơn từ 500k"),
                     "type": "BUY_X_GET_Y" | "ORDER_DISCOUNT" | "PRODUCT_DISCOUNT",
                     "start_date": "yyyy-MM-dd",
                     "end_date": "yyyy-MM-dd",
                     "status": "ACTIVE" | "UPCOMING" | "EXPIRED",
                     "usage_limit": number | null,
                     "usage_count": number,

                     // Chỉ 1 trong 3 detail sau được điền, 2 cái còn lại là null
                     "buy_x_get_y_detail": {
                       "buy_product_name": "Sản phẩm phải mua",
                       "buy_min_quantity": number,
                       "buy_min_value": number,
                       "gift_product_name": "Sản phẩm được tặng/giảm",
                       "gift_quantity": number,
                       "gift_discount_type": "FREE" | "PERCENTAGE" | "FIXED_AMOUNT",
                       "gift_discount_value": number,
                       "gift_max_quantity": number
                     },
                     "order_discount_detail": {
                       "discount_type": "PERCENTAGE" | "FIXED_AMOUNT",
                       "discount_value": number,
                       "max_discount": number,
                       "min_order_value": number,
                       "min_order_quantity": number
                     },
                     "product_discount_detail": {
                       "discount_type": "PERCENTAGE" | "FIXED_AMOUNT",
                       "discount_value": number,
                       "apply_to_type": "ALL" | "PRODUCT",
                       "apply_to_product_name": "Tên sản phẩm" | null,
                       "min_order_value": number,
                       "min_promotion_value": number,
                       "min_promotion_quantity": number
                     }
                   }

                   Parse CHÍNH XÁC theo cấu trúc trên, giữ nguyên các field name và structure.

                   ⚠️ KHI TRẢ LỜI VỀ KHUYẾN MÃI:
                   - Sử dụng field "summary" để tạo message văn bản ngắn gọn, dễ hiểu cho khách
                   - VD: "Hiện có chương trình Mua 5 tặng 1 cho Sữa tươi Vinamilk"
                   - Không cần liệt kê chi tiết kỹ thuật (discount_type, discount_value...) trong message
                   - Structured data sẽ chứa đầy đủ thông tin chi tiết

                VÍ DỤ:
                - Khách hỏi về sản phẩm → response_type: "PRODUCT_INFO", data.products chứa thông tin
                - Khách hỏi về đơn hàng → response_type: "ORDER_INFO", data.orders chứa thông tin
                - Khách hỏi về khuyến mãi → response_type: "PROMOTION_INFO", data.promotions chứa thông tin, message dùng "summary"
                - Khách thao tác giỏ hàng → response_type: "CART_INFO", data.cart chứa thông tin
                - Khách hỏi chính sách → response_type: "GENERAL_ANSWER", data.policy chứa thông tin

                4. Khi nhận được tool results dạng [CART], parse thành CartInfo:
                   Tool trả về plain text mô tả giỏ hàng, ví dụ:

                   [CART]
                   Cart ID: 123
                   ---
                   [1] Sữa tươi Vinamilk 1L
                       - Product Unit ID: 456
                       - Số lượng: 2
                       - Giá: 25,000₫ x 2 = 50,000₫
                       - Giá sau KM: 45,000₫
                       - Tồn kho: 100
                       - Khuyến mãi: Giảm 10%
                   [2] Bánh mì sandwich
                       - Product Unit ID: 789
                       - Số lượng: 1
                       - Giá: 15,000₫
                       - Không có khuyến mãi
                   ---
                   Tổng items: 2
                   Tổng tiền trước KM: 65,000₫
                   Giảm giá sản phẩm: 5,000₫
                   Giảm giá đơn hàng: 0₫
                   Tổng cần thanh toán: 60,000₫

                   Parse thành CartInfo object với cấu trúc:
                   {
                     "cart_id": 123,
                     "items": [
                       {
                         "product_unit_id": 456 (QUAN TRỌNG: numeric, để frontend dùng),
                         "product_name": "Sữa tươi Vinamilk 1L",
                         "unit_name": "Hộp",
                         "quantity": 2,
                         "unit_price": 25000.0,
                         "original_total": 50000.0,
                         "final_total": 45000.0,
                         "image_url": "URL" (nếu có, null nếu N/A),
                         "stock_quantity": 100,
                         "has_promotion": true,
                         "promotion_name": "Giảm 10%"
                       },
                       ...
                     ],
                     "total_items": 2,
                     "sub_total": 65000.0,
                     "line_item_discount": 5000.0,
                     "order_discount": 0.0,
                     "total_payable": 60000.0,
                     "updated_at": "2025-11-11T10:30:00" (ISO datetime)
                   }

                   ⚠️ KHI TRẢ LỜI VỀ GIỎ HÀNG:
                   - response_type phải là "CART_INFO"
                   - message văn bản ngắn gọn, ví dụ: "Đã thêm 2 hộp Sữa tươi Vinamilk vào giỏ hàng. Giỏ hàng của bạn hiện có 2 sản phẩm, tổng cần thanh toán là 60,000₫"
                   - Không liệt kê chi tiết từng item trong message - chỉ tổng quan
                   - Structured data sẽ chứa đầy đủ thông tin chi tiết từng item
                   - suggestions:
                     + PHẢI lấy thông tin free ship TỪ TOOL OUTPUT (dòng cuối cùng của [CART])
                     + KHÔNG tự tính toán con số free ship
                     + VD đúng: Tool output có "💡 Mua thêm 95,000₫ để được MIỄN PHÍ SHIP!" → suggestions: ["Mua thêm 95,000₫ để được MIỄN PHÍ SHIP!"]
                     + VD sai: Tự tính 200000 - 105000 = 95000 rồi ghi "Mua thêm 110,000₫..." ❌ SAI CON SỐ
                     + Các gợi ý khác: "Xem chi tiết giỏ hàng", "Tiến hành thanh toán"

                LƯU Ý:
                - KHÔNG được bỏ sót product_id (product_unit_id) và image_url khi parse [PRODUCT]
                - KHÔNG được bỏ sót order_id và order_code khi parse [ORDER]
                - KHÔNG được bỏ sót promotion_line_id và detail objects khi parse [PROMOTIONS]
                - Khi parse [PROMOTIONS], PHẢI kiểm tra type và điền đúng detail object tương ứng
                - KHÔNG được bỏ sót product_unit_id, cart_id khi parse [CART]
                - Khi parse [CART], số tiền phải là numeric (double), không phải string

                ===== QUY TẮC VÀNG: KHÔNG ĐƯỢC BỊA THÔNG TIN =====
                ⚠️ NGHIÊM CẤM tự bịa hoặc đoán:
                - Sản phẩm không tìm thấy từ tools
                - Khuyến mãi không có trong kết quả tool
                - Đơn hàng không tồn tại
                - Giá cả, chi tiết không rõ ràng
                - Số tiền free ship, con số khuyến mãi
                - Bất kỳ con số nào không có trong tool results

                ⚠️ ĐẶC BIỆT VỀ THÔNG TIN FREE SHIP:
                - Tool [CART] đã có dòng free ship ở cuối (VD: "💡 Mua thêm 95,000₫ để được MIỄN PHÍ SHIP!")
                - AI CHỈ ĐƯỢC lấy thông tin free ship TỪ DÒNG NÀY, không tự tính
                - KHÔNG được tự tính: 200000 - total_payable
                - PHẢI copy CHÍNH XÁC con số từ tool output

                ⚠️ ĐẶC BIỆT VỀ THÊM SẢN PHẨM VÀO GIỎ (QUAN TRỌNG NHẤT):

                1. LUÔN LUÔN gọi productSearchTool trước:
                   - User nói "thêm coca" → PHẢI gọi productSearchTool("coca") trước
                   - CHỈ dùng product_unit_id từ kết quả tìm kiếm
                   - KHÔNG ĐƯỢC dùng product_unit_id random hoặc sản phẩm khác

                2. Khi productSearchTool trả về 0 kết quả:
                   - response_type: "ERROR"
                   - message: "Xin lỗi, không tìm thấy [tên sản phẩm] trong cửa hàng."
                   - TUYỆT ĐỐI KHÔNG gọi addToCartTool
                   - TUYỆT ĐỐI KHÔNG thêm sản phẩm thay thế

                3. Khi productSearchTool trả về NHIỀU kết quả (>1):
                   - response_type: "PRODUCT_INFO"
                   - message: Hỏi khách chọn rõ loại nào (lon, lốc, thùng, kg, gói...)
                   - data.products: danh sách các sản phẩm
                   - CHỜ khách hàng trả lời cụ thể
                   - TUYỆT ĐỐI KHÔNG tự ý chọn 1 trong số đó

                4. Chỉ gọi addToCartTool khi:
                   - Tìm thấy ĐÚNG 1 sản phẩm phù hợp
                   - HOẶC khách đã chọn rõ ràng từ danh sách

                VÍ DỤ CỤ THỂ:
                ✅ ĐÚNG:
                - User: "thêm coca vào giỏ"
                - Tool: tìm thấy 3 loại
                - AI: "Chúng tôi có Coca Cola lon, lốc 6 lon, và thùng 24 lon. Bạn muốn thêm loại nào?"
                - User: "thêm lon"
                - AI: [Gọi addToCartTool với product_unit_id của lon]

                ❌ SAI:
                - User: "thêm coca vào giỏ"
                - Tool: tìm thấy 3 loại
                - AI: [Tự ý chọn lon và gọi addToCartTool] ❌ NGHIÊM CẤM

                ❌ SAI:
                - User: "thêm pepsi vào giỏ"
                - Tool: không tìm thấy
                - AI: [Thêm coca thay thế] ❌ NGHIÊM CẤM

                ✅ NẾU TOOL KHÔNG TRẢ VỀ KẾT QUẢ:
                → response_type: "ERROR"
                → message: "Tôi đã kiểm tra nhưng không tìm thấy thông tin về [vấn đề]. Bạn có thể liên hệ CSKH qua hotline để được hỗ trợ chi tiết."

                ===== CHÍNH SÁCH SIÊU THỊ (Thông tin cố định) =====
                Bạn có thể trả lời TRỰC TIẾP (không cần gọi tool) về:
                - Đổi trả trong 7 ngày với sản phẩm còn nguyên vẹn
                - Hiện tại siêu thị không miễn phí giao hàng
                - Thanh toán: mua hàng trên app phải thành toán mới được mua hàng, không cho nợ
                - Giờ mở cửa: 7:00 - 22:00 hàng ngày
                → Dùng response_type: "GENERAL_ANSWER"

                ===== NGHIÊM CẤM (Từ chối lịch sự) =====
                - Chính trị, tôn giáo, y tế, pháp luật
                - Lịch sử, địa lý, khoa học (ngoài sản phẩm)
                - Viết code, làm bài tập, dịch thuật
                - Tư vấn đầu tư, tài chính
                - Không trả lời các câu hỏi ngoài phạm vi siêu thị

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
                """
                .formatted(customerInfo);
    }

    /**
     * Build thông tin customer để cung cấp cho AI
     */
    private String buildCustomerInfo(Customer customer) {
        StringBuilder info = new StringBuilder();

        info.append("- ID Khách hàng: ").append(customer.getCustomerId()).append("\n");

        if (customer.getUser() != null) {
            info.append("- Tên: ").append(customer.getUser().getName()).append("\n");
            info.append("- Email: ").append(customer.getUser().getEmail()).append("\n");
            if (customer.getUser().getPhone() != null) {
                info.append("- Số điện thoại: ").append(customer.getUser().getPhone()).append("\n");
            }
        }

        if (customer.getCustomerCode() != null) {
            info.append("- Mã khách hàng: ").append(customer.getCustomerCode()).append("\n");
        }

        info.append("- Loại khách hàng: ").append(customer.getCustomerType()).append("\n");

        if (customer.getAddress() != null) {
            info.append("- Địa chỉ: ").append(customer.getAddress()).append("\n");
        }

        return info.toString();
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
                message.getData(),
                message.getTimestamp());
    }
}
