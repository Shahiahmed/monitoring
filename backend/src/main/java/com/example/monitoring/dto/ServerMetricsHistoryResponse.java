package com.example.monitoring.dto;

import java.time.LocalDateTime;

public record ServerMetricsHistoryResponse(
        LocalDateTime collectedAt,
        Double cpuPercent,
        Double memoryPercent,
        Double diskPercent,
        Long memoryUsedMb,
        Long memoryTotalMb,
        Long diskUsedGb,
        Long diskTotalGb
) {}
