package com.example.monitoring.repository;

import com.example.monitoring.entity.IncidentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentFileRepository extends JpaRepository<IncidentFile, Long> {
    List<IncidentFile> findByIncidentIdOrderByUploadedAtDesc(Long incidentId);
    long countByIncidentId(Long incidentId);
}
