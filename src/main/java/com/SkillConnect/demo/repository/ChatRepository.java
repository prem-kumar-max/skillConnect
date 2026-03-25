package com.SkillConnect.demo.repository;

import com.SkillConnect.demo.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findBySenderId(Long userId);

    List<Chat> findByReceiverId(Long providerId);

    List<Chat> findBySenderIdAndReceiverId(Long senderId, Long receiverId);


    @Query(value = "select * from chat where (sender_id = ?1 and receiver_id = ?2) or (receiver_id = ?1 and sender_id = ?2)" , nativeQuery = true)
    List<Chat> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

    List<Chat> findBySenderIdAndReceiverIdAndSendAtBetween(Long senderId, Long receiverId, LocalDateTime startDate, LocalDateTime endDate);

    List<Chat> findBySendAtAfter(LocalDateTime sendAt);
}
