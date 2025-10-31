package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;

import java.util.List;

/**
 * Service interface cho chat AI
 */
public interface ChatService {

    /**
     * Tạo conversation mới
     * 
     * @param customerId ID khách hàng
     * @return thông tin conversation mới
     */
    ConversationResponse createConversation(Integer customerId);

    /**
     * Gửi tin nhắn và nhận response từ AI
     * 
     * @param request thông tin tin nhắn
     * @return response từ AI
     */
    ChatResponse sendMessage(ChatRequest request);

    /**
     * Lấy danh sách conversations của khách hàng
     * 
     * @param customerId ID khách hàng
     * @return danh sách conversations
     */
    List<ConversationResponse> getConversations(Integer customerId);

    /**
     * Lấy lịch sử chat của một conversation
     * 
     * @param conversationId ID conversation
     * @param customerId ID khách hàng (để verify ownership)
     * @return danh sách tin nhắn
     */
    List<MessageResponse> getConversationHistory(String conversationId, Integer customerId);

    /**
     * Xóa một conversation
     * 
     * @param conversationId ID conversation
     * @param customerId ID khách hàng (để verify ownership)
     */
    void deleteConversation(String conversationId, Integer customerId);
}