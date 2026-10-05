package com.example.monitoring.repository;

import com.example.monitoring.entity.IncidentInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentIntervalRepository extends JpaRepository<IncidentInterval, Long> {
    List<IncidentInterval> findByIncidentIdOrderById(Long incidentId);
}
