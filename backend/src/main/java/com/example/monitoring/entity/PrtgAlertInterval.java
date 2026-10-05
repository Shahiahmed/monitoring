package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prtg_alert_intervals")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrtgAlertInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prtg_alert_id", nullable = false)
    private PrtgAlert prtgAlert;

    @Column(name = "date_from")
    private OffsetDateTime dateFrom;

    @Column(name = "date_to")
    private OffsetDateTime dateTo;

    @Column(name = "diff_minutes")
    private Integer diffMinutes;
}
