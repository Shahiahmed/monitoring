package com.example.monitoring.repository;

import com.example.monitoring.entity.PrtgAlertInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrtgAlertIntervalRepository extends JpaRepository<PrtgAlertInterval, Long> {
    List<PrtgAlertInterval> findByPrtgAlertIdOrderById(Long prtgAlertId);
}
