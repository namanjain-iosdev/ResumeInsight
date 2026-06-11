package com.cvanalyzer.repository;

import com.cvanalyzer.entity.GeneratedResume;
import com.cvanalyzer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    Page<GeneratedResume> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<GeneratedResume> findByOriginalResumeIdOrderByVersionNumberAsc(Long originalResumeId);
    Optional<GeneratedResume> findByIdAndUser(Long id, User user);
    long countByOriginalResumeId(Long originalResumeId);
}
