package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.chat.ChatRequest;
import iuh.fit.supermarket.dto.chat.ChatResponse;
import iuh.fit.supermarket.dto.chat.ConversationResponse;
import iuh.fit.supermarket.dto.chat.MessageResponse;
import iuh.fit.supermarket.service.ChatService;
import iuh.fit.supermarket.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API endpoints cho Chat AI
 * Customer ID được lấy tự động từ SecurityContext
 */
@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat AI", description = "APIs cho tính năng chat với AI")
public class ChatController {
    private final ChatService chatService;
    private final SecurityUtil securityUtil;

    public ChatController(ChatService chatService, SecurityUtil securityUtil) {
        this.chatService = chatService;
        this.securityUtil = securityUtil;
    }

    /**
     * Tạo conversation mới
     * Customer ID được lấy tự động từ authentication context
     */
    @PostMapping("/conversations")
    @Operation(summary = "Tạo cuộc hội thoại mới", description = "Tạo một conversation mới cho khách hàng đang đăng nhập")
    public ResponseEntity<ApiResponse<ConversationResponse>> createConversation() {
        try {
            Integer customerId = securityUtil.getCurrentCustomerId();
            ConversationResponse conversation = chatService.createConversation(customerId);
            return ResponseEntity.ok(ApiResponse.success("Tạo conversation thành công", conversation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi tạo conversation: " + e.getMessage()));
        }
    }

    /**
     * Gửi tin nhắn và nhận response từ AI
     * Customer ID được lấy tự động từ authentication context
     */
    @PostMapping("/message")
    @Operation(
        summary = "Gửi tin nhắn chat",
        description = "Gửi tin nhắn và nhận phản hồi từ AI. " +
                     "Customer ID được lấy tự động từ thông tin đăng nhập. " +
                     "Nếu conversationId = null, hệ thống sẽ tự động tạo conversation mới. " +
                     "Nếu muốn tiếp tục conversation cũ, truyền conversationId từ response trước."
    )
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            Integer customerId = securityUtil.getCurrentCustomerId();
            ChatResponse response = chatService.sendMessage(request, customerId);
            return ResponseEntity.ok(ApiResponse.success("Chat thành công", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xử lý chat: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách conversations của khách hàng
     * Customer ID được lấy tự động từ authentication context
     */
    @GetMapping("/conversations")
    @Operation(summary = "Lấy danh sách cuộc hội thoại", description = "Lấy tất cả conversations của khách hàng đang đăng nhập")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        try {
            Integer customerId = securityUtil.getCurrentCustomerId();
            List<ConversationResponse> conversations = chatService.getConversations(customerId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách conversations thành công", conversations));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy conversations: " + e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử chat của một conversation
     * Customer ID được lấy tự động từ authentication context để verify ownership
     */
    @GetMapping("/conversations/{conversationId}/history")
    @Operation(summary = "Lấy lịch sử chat", description = "Lấy tất cả tin nhắn trong một conversation")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversationHistory(
            @PathVariable String conversationId) {
        try {
            Integer customerId = securityUtil.getCurrentCustomerId();
            List<MessageResponse> messages = chatService.getConversationHistory(conversationId, customerId);
            return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử chat thành công", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi lấy lịch sử chat: " + e.getMessage()));
        }
    }

    /**
     * Xóa một conversation
     * Customer ID được lấy tự động từ authentication context để verify ownership
     */
    @DeleteMapping("/conversations/{conversationId}")
    @Operation(summary = "Xóa cuộc hội thoại", description = "Xóa một conversation và tất cả tin nhắn trong đó")
    public ResponseEntity<ApiResponse<Void>> deleteConversation(
            @PathVariable String conversationId) {
        try {
            Integer customerId = securityUtil.getCurrentCustomerId();
            chatService.deleteConversation(conversationId, customerId);
            return ResponseEntity.ok(ApiResponse.success("Xóa conversation thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi khi xóa conversation: " + e.getMessage()));
        }
    }
}