package com.example.monitoring.dto;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class FileResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private OffsetDateTime uploadedAt;
}
