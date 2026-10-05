package com.example.monitoring.service;

import com.example.monitoring.entity.DicServer;
import com.example.monitoring.entity.ServerMetricsHistory;
import com.example.monitoring.dto.ServerMetricsResponse;
import com.example.monitoring.repository.DicServerRepository;
import com.example.monitoring.repository.ServerMetricsHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsCollectorService {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollectorService.class);
    private static final long PROD_ENV_ID    = 2L;
    private static final int  RETENTION_DAYS = 30;

    private final DicServerRepository serverRepository;
    private final ServerMetricsHistoryRepository historyRepository;
    private final SshMetricsService sshMetricsService;

    @Scheduled(initialDelay = 60 * 1000, fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void collectAndSave() {
        List<DicServer> servers = serverRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .filter(s -> s.getEnv() != null && PROD_ENV_ID == s.getEnv().getId())
                .toList();

        if (servers.isEmpty()) return;

        log.info("Сбор метрик для {} серверов...", servers.size());
        LocalDateTime now = LocalDateTime.now();

        for (DicServer server : servers) {
            try {
                ServerMetricsResponse m = sshMetricsService.fetchMetricsForServer(server, true);
                if (m.error() != null) {
                    log.warn("Сервер {} ({}): SSH ошибка — {}", server.getId(), server.getIp(), m.error());
                    continue;
                }

                // Сохраняем usedMb = total - available, чтобы (total - used) = available.
                Long storedUsedMb = (m.memoryTotalMb() != null && m.memoryAvailableMb() != null)
                        ? m.memoryTotalMb() - m.memoryAvailableMb()
                        : m.memoryUsedMb();

                historyRepository.save(
                        ServerMetricsHistory.builder()
                                .serverId(server.getId())
                                .collectedAt(now)
                                .cpuPercent(m.cpuPercent())
                                .memoryPercent(m.memoryPercent())
                                .diskPercent(m.diskPercent())
                                .memoryUsedMb(storedUsedMb)
                                .memoryTotalMb(m.memoryTotalMb())
                                .diskUsedGb(m.diskUsedGb())
                                .diskTotalGb(m.diskTotalGb())
                                .build()
                );

            } catch (Exception e) {
                log.error("Ошибка сбора метрик для сервера {}: {}", server.getId(), e.getMessage());
            }
        }

        historyRepository.deleteByCollectedAtBefore(LocalDateTime.now().minusDays(RETENTION_DAYS));
        log.info("Сбор метрик завершён.");
    }
}
