package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dic_server")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DicServer {

    @Id
    private Long id;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "ip")
    private String ip;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "env_id")
    private DicEnv env;

    @Column(name = "warn_ram")
    private Integer warnRam;

    @Column(name = "warn_disk")
    private Integer warnDisk;

    @Column(name = "crit_ram")
    private Integer critRam;

    @Column(name = "crit_disk")
    private Integer critDisk;
}
