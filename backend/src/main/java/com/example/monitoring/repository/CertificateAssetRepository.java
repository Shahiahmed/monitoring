package com.example.monitoring.repository;

import com.example.monitoring.entity.CertificateAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateAssetRepository extends JpaRepository<CertificateAsset, Long> {
    List<CertificateAsset> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CertificateAsset> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
    Optional<CertificateAsset> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
