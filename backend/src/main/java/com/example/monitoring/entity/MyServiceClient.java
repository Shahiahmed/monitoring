package com.example.monitoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "my_service_clients")
@Getter @Setter @NoArgsConstructor
public class MyServiceClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(name = "information_system")
    private String informationSystem;

    @JsonProperty("isPaid")
    @Column(name = "is_paid", nullable = false)
    private boolean isPaid = false;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "smart_bridge_ticket")
    private String smartBridgeTicket;

    @Column(name = "connection_basis", columnDefinition = "TEXT")
    private String connectionBasis;

    @Column(name = "connection_date")
    private LocalDate connectionDate;

    @Column(name = "shep_sender_id")
    private String shepSenderId;

    @Column(name = "contract_file_name")
    private String contractFileName;

    @JsonIgnore
    @Column(name = "contract_file_data")
    private byte[] contractFileData;

    public boolean hasContract() {
        return contractFileName != null;
    }
}
