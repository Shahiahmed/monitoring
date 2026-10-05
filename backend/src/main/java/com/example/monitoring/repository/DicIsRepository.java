package com.example.monitoring.repository;

import com.example.monitoring.entity.DicIs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DicIsRepository extends JpaRepository<DicIs, Long> {

    List<DicIs> findByGoId(Long goId);
}
