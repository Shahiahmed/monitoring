package com.example.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateAssetResponse {
    private Long id;
    private Long userId;
    private String type;
    private String status;
    private String keyOriginalName;
    private String certOriginalName;
    private String certFingerprintSha256;
    private String certSubject;
    private String certIssuer;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private OffsetDateTime createdAt;
}
