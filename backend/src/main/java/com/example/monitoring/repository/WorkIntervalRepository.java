package com.example.monitoring.repository;

import com.example.monitoring.entity.WorkInterval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkIntervalRepository extends JpaRepository<WorkInterval, Long> {
    List<WorkInterval> findByWorkIdOrderById(Long workId);
}
