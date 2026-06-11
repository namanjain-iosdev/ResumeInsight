package com.cvanalyzer.repository;

import com.cvanalyzer.entity.ChatHistory;
import com.cvanalyzer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    Page<ChatHistory> findByUserOrderByCreatedAtAsc(User user, Pageable pageable);
    void deleteAllByUser(User user);
    long countByUser(User user);
}
