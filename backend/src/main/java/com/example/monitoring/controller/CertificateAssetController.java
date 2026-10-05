package com.example.monitoring.controller;

import com.example.monitoring.dto.CertificateAssetResponse;
import com.example.monitoring.entity.CertificateAsset;
import com.example.monitoring.service.CertificateAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateAssetController {

    private final CertificateAssetService certificateAssetService;

    /**
     * Загрузка SSL-сертификата (файл + пароль).
     * user_id пока передаём в заголовке или по умолчанию 1.
     */
    @PostMapping(value = "/ssl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificateAssetResponse> uploadSsl(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        long uid = userId != null ? userId : 1L;
        try {
            CertificateAsset saved = certificateAssetService.saveSsl(uid, file, password);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Загрузка файла ЭЦП.
     */
    @PostMapping(value = "/ecp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificateAssetResponse> uploadEcp(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        long uid = userId != null ? userId : 1L;
        try {
            CertificateAsset saved = certificateAssetService.saveEcp(uid, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Удаление сертификата по id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        long uid = userId != null ? userId : 1L;
        if (certificateAssetService.deleteByIdAndUserId(id, uid)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Список сертификатов пользователя (опционально по типу).
     */
    @GetMapping
    public List<CertificateAssetResponse> list(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "type", required = false) String type
    ) {
        long uid = userId != null ? userId : 1L;
        List<CertificateAsset> list = type != null && !type.isBlank()
                ? certificateAssetService.findByUserIdAndType(uid, type.toUpperCase())
                : certificateAssetService.findByUserId(uid);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CertificateAssetResponse toResponse(CertificateAsset a) {
        return CertificateAssetResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .type(a.getType())
                .status(a.getStatus())
                .keyOriginalName(a.getKeyOriginalName())
                .certOriginalName(a.getCertOriginalName())
                .certFingerprintSha256(a.getCertFingerprintSha256())
                .certSubject(a.getCertSubject())
                .certIssuer(a.getCertIssuer())
                .validFrom(a.getValidFrom())
                .validTo(a.getValidTo())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
