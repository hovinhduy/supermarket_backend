package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với cuộc hội thoại chat
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, String> {

    /**
     * Tìm tất cả conversations của một khách hàng, sắp xếp theo thời gian cập nhật mới nhất
     */
    @Query("SELECT c FROM ChatConversation c WHERE c.customer.customerId = :customerId ORDER BY c.updatedAt DESC")
    List<ChatConversation> findByCustomerIdOrderByUpdatedAtDesc(@Param("customerId") Integer customerId);

    /**
     * Tìm conversation theo ID và customerId để đảm bảo quyền truy cập
     */
    @Query("SELECT c FROM ChatConversation c WHERE c.id = :conversationId AND c.customer.customerId = :customerId")
    Optional<ChatConversation> findByIdAndCustomerId(@Param("conversationId") String conversationId, 
                                                       @Param("customerId") Integer customerId);
}
