package com.example.monitoring.repository;

import com.example.monitoring.entity.PrtgAlertFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrtgAlertFileRepository extends JpaRepository<PrtgAlertFile, Long> {
    List<PrtgAlertFile> findByPrtgAlertIdOrderByUploadedAtDesc(Long prtgAlertId);
    long countByPrtgAlertId(Long prtgAlertId);
}
