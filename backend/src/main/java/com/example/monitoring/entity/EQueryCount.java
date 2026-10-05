package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "e_query_counts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EQueryCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oracle_id")
    private Long oracleId;

    @Column(name = "year_month", nullable = false)
    private String yearMonth;

    @Column(nullable = false)
    private String code;

    @Column(name = "shep_service_id")
    private String shepServiceId;

    @Column(name = "monthly_count", nullable = false)
    private Long monthlyCount;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;
}
