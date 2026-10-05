package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prtg_alerts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrtgAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prtg_status")
    private String prtgStatus;

    @Column(name = "in_message")
    private String inMessage;

    @Column(name = "solution")
    private String solution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "prtgAlert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PrtgAlertInterval> intervals = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (intervals == null) intervals = new ArrayList<>();
    }

    public List<PrtgAlertInterval> getIntervals() {
        if (intervals == null) intervals = new ArrayList<>();
        return intervals;
    }
}
