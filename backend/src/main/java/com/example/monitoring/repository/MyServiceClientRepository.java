package com.example.monitoring.repository;

import com.example.monitoring.entity.MyServiceClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyServiceClientRepository extends JpaRepository<MyServiceClient, Long> {
    List<MyServiceClient> findByServiceIdOrderByOrganizationNameAsc(Long serviceId);
}
