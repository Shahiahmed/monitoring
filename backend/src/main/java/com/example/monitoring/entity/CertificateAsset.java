package com.example.monitoring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "certificate_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", nullable = false, length = 16)
    private String type; // ECP | SSL | BOTH

    @Column(name = "status", nullable = false, length = 16)
    private String status; // active | expired | revoked | disabled

    @Column(name = "key_storage", columnDefinition = "TEXT")
    private String keyStorage;

    @Column(name = "cert_storage", columnDefinition = "TEXT")
    private String certStorage;

    @Column(name = "key_original_name", columnDefinition = "TEXT")
    private String keyOriginalName;

    @Column(name = "cert_original_name", columnDefinition = "TEXT")
    private String certOriginalName;

    @Column(name = "cert_fingerprint_sha256", length = 64)
    private String certFingerprintSha256;

    @Column(name = "cert_subject", columnDefinition = "TEXT")
    private String certSubject;

    @Column(name = "cert_issuer", columnDefinition = "TEXT")
    private String certIssuer;

    @Column(name = "cert_serial_number", columnDefinition = "TEXT")
    private String certSerialNumber;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_to")
    private OffsetDateTime validTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
