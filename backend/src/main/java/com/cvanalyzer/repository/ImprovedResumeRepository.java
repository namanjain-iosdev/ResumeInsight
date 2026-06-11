package com.cvanalyzer.repository;

import com.cvanalyzer.entity.ImprovedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImprovedResumeRepository extends JpaRepository<ImprovedResume, Long> {
    Optional<ImprovedResume> findByAnalysisId(Long analysisId);
}
