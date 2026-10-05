package com.example.monitoring.dto;

import java.util.List;

/**
 * Реальные метрики сервера, полученные по SSH.
 * Совместимо со старым monitoring-system-src: used/available из free, диск /dev/sda1 или /.
 */
public record ServerMetricsResponse(
        Long serverId,
        Double cpuPercent,
        Double memoryPercent,
        Double diskPercent,
        Long memoryUsedMb,
        Long memoryTotalMb,
        Long memoryAvailableMb,
        Long diskUsedGb,
        Long diskTotalGb,
        String error,
        List<ProcessInfo> topProcesses
) {
    public static ServerMetricsResponse error(Long serverId, String error) {
        return new ServerMetricsResponse(serverId, null, null, null, null, null, null, null, null, error, List.of());
    }

    public static ServerMetricsResponse ok(
            Long serverId,
            double cpu,
            double memoryPercent,
            double diskPercent,
            long memoryUsedMb,
            long memoryTotalMb,
            long memoryAvailableMb,
            long diskUsedGb,
            long diskTotalGb,
            List<ProcessInfo> topProcesses
    ) {
        return new ServerMetricsResponse(
                serverId,
                cpu,
                memoryPercent,
                diskPercent,
                memoryUsedMb,
                memoryTotalMb,
                memoryAvailableMb,
                diskUsedGb,
                diskTotalGb,
                null,
                topProcesses
        );
    }
}
