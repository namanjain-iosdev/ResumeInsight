package com.cvanalyzer.repository;

import com.cvanalyzer.entity.Analysis;
import com.cvanalyzer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Page<Analysis> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Optional<Analysis> findByIdAndUser(Long id, User user);
    List<Analysis> findByResumeId(Long resumeId);
    long countByUser(User user);

    @Query("SELECT AVG(a.atsScore) FROM Analysis a WHERE a.user = :user")
    Double findAverageAtsScoreByUser(@Param("user") User user);
}
