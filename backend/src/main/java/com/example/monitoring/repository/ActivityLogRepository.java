package com.example.monitoring.repository;

import com.example.monitoring.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findPageByOrderByCreatedAtDesc(Pageable pageable);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
