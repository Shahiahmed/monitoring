package com.example.monitoring.repository;

import com.example.monitoring.entity.ServerMetricsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServerMetricsHistoryRepository extends JpaRepository<ServerMetricsHistory, Long> {

    List<ServerMetricsHistory> findByServerIdAndCollectedAtAfterOrderByCollectedAtAsc(
            Long serverId, LocalDateTime after
    );

    void deleteByCollectedAtBefore(LocalDateTime before);

    void deleteByServerId(Long serverId);
}
