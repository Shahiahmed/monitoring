package com.example.monitoring.service;

import com.example.monitoring.entity.CertificateAsset;
import com.example.monitoring.repository.CertificateAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateAssetService {

    private final CertificateAssetRepository repository;

    @Transactional
    public CertificateAsset saveSsl(Long userId, MultipartFile file, String password) {
        CertificateAsset asset = CertificateAsset.builder()
                .userId(userId)
                .type("SSL")
                .status("active")
                .keyStorage(null)
                .certStorage(null)
                .keyOriginalName(file.getOriginalFilename())
                .certOriginalName(file.getOriginalFilename())
                .build();
        fillCertMetadata(asset, file, password);
        asset.setCreatedAt(OffsetDateTime.now());
        asset.setUpdatedAt(OffsetDateTime.now());
        return repository.save(asset);
    }

    @Transactional
    public CertificateAsset saveEcp(Long userId, MultipartFile file) {
        CertificateAsset asset = CertificateAsset.builder()
                .userId(userId)
                .type("ECP")
                .status("active")
                .keyStorage(null)
                .certStorage(null)
                .keyOriginalName(file.getOriginalFilename())
                .certOriginalName(file.getOriginalFilename())
                .build();
        fillCertMetadata(asset, file, null);
        asset.setCreatedAt(OffsetDateTime.now());
        asset.setUpdatedAt(OffsetDateTime.now());
        return repository.save(asset);
    }

    public List<CertificateAsset> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<CertificateAsset> findByUserIdAndType(Long userId, String type) {
        return repository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type);
    }

    @Transactional
    public boolean deleteByIdAndUserId(Long id, Long userId) {
        Optional<CertificateAsset> opt = repository.findByIdAndUserId(id, userId);
        if (opt.isPresent()) {
            repository.delete(opt.get());
            return true;
        }
        return false;
    }

    private void fillCertMetadata(CertificateAsset asset, MultipartFile file, String password) {
        try {
            byte[] bytes = file.getBytes();
            String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";

            Certificate cert = null;
            if (name.toLowerCase().endsWith(".pem") || name.toLowerCase().endsWith(".crt") || name.toLowerCase().endsWith(".cer")) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                cert = cf.generateCertificate(new ByteArrayInputStream(bytes));
            }
            if (cert == null && (name.toLowerCase().endsWith(".p12") || name.toLowerCase().endsWith(".pfx"))) {
                java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
                char[] pass = (password != null && !password.isBlank()) ? password.toCharArray() : new char[0];
                ks.load(new ByteArrayInputStream(bytes), pass);
                String alias = ks.aliases().hasMoreElements() ? ks.aliases().nextElement() : null;
                if (alias != null) {
                    cert = ks.getCertificate(alias);
                }
            }
            if (cert instanceof X509Certificate x509) {
                asset.setCertSubject(x509.getSubjectX500Principal().getName());
                asset.setCertIssuer(x509.getIssuerX500Principal().getName());
                asset.setCertSerialNumber(x509.getSerialNumber() != null ? x509.getSerialNumber().toString(16) : null);
                asset.setValidFrom(OffsetDateTime.ofInstant(x509.getNotBefore().toInstant(), ZoneId.systemDefault()));
                asset.setValidTo(OffsetDateTime.ofInstant(x509.getNotAfter().toInstant(), ZoneId.systemDefault()));
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(x509.getEncoded());
                asset.setCertFingerprintSha256(HexFormat.of().formatHex(digest));
            }
        } catch (Exception e) {
            log.warn("Не удалось извлечь метаданные сертификата: {}", e.getMessage());
        }
    }
}
