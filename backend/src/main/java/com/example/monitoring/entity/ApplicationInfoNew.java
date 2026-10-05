package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "application_info_new")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationInfoNew {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_info_new_seq")
    @SequenceGenerator(name = "application_info_new_seq", sequenceName = "application_info_new_seq", allocationSize = 1)
    private Long id;

    @Column(name = "auto_created") private Boolean autoCreated;
    @Column(name = "info")         private String info;
    @Column(name = "inner_url")    private String innerUrl;
    @Column(name = "precedent")    private String precedent;
    @Column(name = "url")          private String url;
    @Column(name = "database_id")  private Long databaseId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    private ApplicationNew application;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "env_id")
    private DicEnv env;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_id")
    private DicServer server;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id")
    private DicStatus status;
}
