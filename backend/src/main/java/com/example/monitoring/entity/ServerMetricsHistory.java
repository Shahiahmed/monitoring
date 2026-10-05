package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "server_metrics_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerMetricsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "server_id", nullable = false)
    private Long serverId;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    @Column(name = "cpu_percent")
    private Double cpuPercent;

    @Column(name = "memory_percent")
    private Double memoryPercent;

    @Column(name = "disk_percent")
    private Double diskPercent;

    @Column(name = "memory_used_mb")
    private Long memoryUsedMb;

    @Column(name = "memory_total_mb")
    private Long memoryTotalMb;

    @Column(name = "disk_used_gb")
    private Long diskUsedGb;

    @Column(name = "disk_total_gb")
    private Long diskTotalGb;
}
