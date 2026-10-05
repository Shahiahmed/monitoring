package com.example.monitoring.service;

import com.example.monitoring.entity.ActivityLog;
import com.example.monitoring.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final int RETENTION_DAYS = 30;

    private final ActivityLogRepository repository;

    public void log(Long userId, String userEmail, String action,
                    String entityType, Long entityId, String description) {
        repository.save(ActivityLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public Page<ActivityLog> getPage(int page, int size) {
        return repository.findPageByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Scheduled(cron = "0 0 3 * * *") // каждый день в 03:00
    @Transactional
    public void purgeOldEntries() {
        repository.deleteByCreatedAtBefore(LocalDateTime.now().minusDays(RETENTION_DAYS));
    }
}
