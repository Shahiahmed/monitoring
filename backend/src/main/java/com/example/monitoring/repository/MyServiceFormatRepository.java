package com.example.monitoring.repository;

import com.example.monitoring.entity.MyServiceFormat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyServiceFormatRepository extends JpaRepository<MyServiceFormat, Long> {
    List<MyServiceFormat> findByServiceIdOrderBySortOrderAscIdAsc(Long serviceId);
}
