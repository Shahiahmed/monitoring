package com.example.monitoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "my_services")
@Getter @Setter @NoArgsConstructor
public class MyService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "published_at")
    private String publishedAt;

    @Column(name = "service_key", nullable = false)
    private String serviceKey;

    @Column(name = "information_system")
    private String informationSystem;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "smart_bridge_ticket")
    private String smartBridgeTicket;

    @JsonIgnore
    @Column(name = "contract_file", columnDefinition = "bytea")
    private byte[] contractFile;

    @Column(name = "contract_filename")
    private String contractFilename;

    @Column(name = "contract_content_type")
    private String contractContentType;

    @Column(name = "contract_expires_at")
    private String contractExpiresAt;

    @Transient
    public boolean isHasContract() {
        return contractFile != null && contractFile.length > 0;
    }
}
