package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mongo_inout_stat")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MongoInoutStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subsystem;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "stat_year", nullable = false)
    private Integer statYear;

    @Column(name = "stat_month", nullable = false)
    private Integer statMonth;

    @Column(nullable = false)
    private Long cnt;

    @Column(name = "insert_date")
    private LocalDateTime insertDate;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;
}
