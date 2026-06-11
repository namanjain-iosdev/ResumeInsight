package com.cvanalyzer.repository;

import com.cvanalyzer.entity.Resume;
import com.cvanalyzer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Page<Resume> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
    List<Resume> findByUserOrderByVersionNumberAsc(User user);
    long countByUser(User user);
}
