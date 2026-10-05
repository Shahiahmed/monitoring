package com.example.monitoring.repository;

import com.example.monitoring.entity.MyService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyServiceRepository extends JpaRepository<MyService, Long> {
    List<MyService> findAllByOrderBySortOrderAscServiceNameAsc();
}
