package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "incidents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "failure_type_id")
    private DicFailureType failureType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private DicLocation location;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "incident_is",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "is_id")
    )
    private Set<DicIs> informationSystems = new HashSet<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<IncidentInterval> intervals = new ArrayList<>();

    @Column(name = "fixed")
    private Boolean fixed;

    @Column(name = "empty_time")
    private Boolean emptyTime;

    @Column(name = "include_availability", nullable = false)
    private Boolean includeAvailability = true;

    @Column(name = "in_message")
    private String inMessage;

    @Column(name = "out_message")
    private String outMessage;

    @Column(name = "act")
    private String act;

    @Column(name = "problem")
    private String problem;

    @Column(name = "solution")
    private String solution;

    @Column(name = "source_prtg_id")
    private Long sourcePrtgId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (includeAvailability == null) includeAvailability = true;
        if (informationSystems == null) informationSystems = new HashSet<>();
        if (intervals == null) intervals = new ArrayList<>();
    }

    public List<IncidentInterval> getIntervals() {
        if (intervals == null) intervals = new ArrayList<>();
        return intervals;
    }

    public Set<DicIs> getInformationSystems() {
        if (informationSystems == null) informationSystems = new HashSet<>();
        return informationSystems;
    }
}
