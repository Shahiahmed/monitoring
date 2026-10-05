package com.example.monitoring.repository;

import com.example.monitoring.entity.WorkFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkFileRepository extends JpaRepository<WorkFile, Long> {
    List<WorkFile> findByWorkIdOrderByUploadedAtDesc(Long workId);
    long countByWorkId(Long workId);
}
