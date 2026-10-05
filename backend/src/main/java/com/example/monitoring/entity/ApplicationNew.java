package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "application_new")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationNew {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_new_seq")
    @SequenceGenerator(name = "application_new_seq", sequenceName = "application_new_seq", allocationSize = 1)
    private Long id;

    @Column(name = "artifact_id")          private String artifactId;
    @Column(name = "subsystem_inout")      private String subsystemInout;
    @Column(name = "description", length = 1000) private String description;
    @Column(name = "developer")            private String developer;
    @Column(name = "featured")             private Boolean featured;
    @Column(name = "is_mtszn")             private String isMtszn;
    @Column(name = "name")                 private String name;
    @Column(name = "precedent_production") private Integer precedentProduction;
    @Column(name = "precedent_test")       private Integer precedentTest;
    @Column(name = "procedures", length = 1000) private String procedures;
    @Column(name = "project_name")         private String projectName;
    @Column(name = "publication_date")     private LocalDateTime publicationDate;
    @Column(name = "schema_database")      private String schemaDatabase;
    @Column(name = "shep_service_id")      private String shepServiceId;
    @Column(name = "smart_bridge_page")    private String smartBridgePage;
    @Column(name = "url_production")       private String urlProduction;
    @Column(name = "url_test")             private String urlTest;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "app_type_id")
    private DicAppType appType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interaction_type_id")
    private DicInteractionType interactionType;

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ApplicationInfoNew> deployments;
}
