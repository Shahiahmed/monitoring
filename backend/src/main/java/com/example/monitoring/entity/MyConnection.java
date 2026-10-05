package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "my_connections")
@Getter @Setter
public class MyConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "connection_date")
    private LocalDateTime connectionDate;

    @Column(name = "service_key")
    private String serviceKey;

    @Column(name = "service_owner")
    private String serviceOwner;

    @Column(name = "is_owner")
    private String isOwner;

    @Column(name = "is_client_mtzn")
    private String isClientMtzn;

    @Column(name = "smart_bridge_ticket")
    private String smartBridgeTicket;
}
