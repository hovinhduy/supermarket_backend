package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface cho thao tác với tin nhắn chat
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Lấy danh sách tin nhắn của một conversation, sắp xếp theo thời gian
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(@Param("conversationId") String conversationId);

    /**
     * Lấy N tin nhắn gần nhất của một conversation để làm context memory
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId ORDER BY m.timestamp DESC LIMIT :limit")
    List<ChatMessage> findTopNByConversationIdOrderByTimestampDesc(@Param("conversationId") String conversationId, 
                                                                    @Param("limit") int limit);
}
