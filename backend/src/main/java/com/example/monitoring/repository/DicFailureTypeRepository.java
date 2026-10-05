package com.example.monitoring.repository;

import com.example.monitoring.entity.DicFailureType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DicFailureTypeRepository extends JpaRepository<DicFailureType, Long> {
}
