package com.example.monitoring.controller;

import com.example.monitoring.dto.FileResponse;
import com.example.monitoring.entity.IncidentFile;
import com.example.monitoring.repository.IncidentFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incident-files")
@RequiredArgsConstructor
public class IncidentFileController {

    private final IncidentFileRepository fileRepository;

    @GetMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public List<FileResponse> list(@PathVariable Long incidentId) {
        return fileRepository.findByIncidentIdOrderByUploadedAtDesc(incidentId).stream()
            .map(f -> FileResponse.builder()
                .id(f.getId()).fileName(f.getFileName())
                .contentType(f.getContentType()).fileSize(f.getFileSize())
                .uploadedAt(f.getUploadedAt()).build())
            .collect(Collectors.toList());
    }

    @PostMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FileResponse> upload(@PathVariable Long incidentId,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        IncidentFile entity = IncidentFile.builder()
            .incidentId(incidentId)
            .fileName(file.getOriginalFilename())
            .contentType(file.getContentType())
            .fileSize(file.getSize())
            .data(file.getBytes())
            .build();
        IncidentFile saved = fileRepository.save(entity);
        return ResponseEntity.ok(FileResponse.builder()
            .id(saved.getId()).fileName(saved.getFileName())
            .contentType(saved.getContentType()).fileSize(saved.getFileSize())
            .uploadedAt(saved.getUploadedAt()).build());
    }

    @GetMapping("/download/{fileId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) {
        return fileRepository.findById(fileId)
            .map(f -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f.getFileName() + "\"")
                .contentType(f.getContentType() != null
                    ? MediaType.parseMediaType(f.getContentType()) : MediaType.APPLICATION_OCTET_STREAM)
                .body(f.getData()))
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileRepository.deleteById(fileId);
        return ResponseEntity.noContent().build();
    }
}
