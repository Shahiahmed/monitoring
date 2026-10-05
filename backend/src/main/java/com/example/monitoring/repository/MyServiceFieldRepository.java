package com.example.monitoring.repository;

import com.example.monitoring.entity.MyServiceField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyServiceFieldRepository extends JpaRepository<MyServiceField, Long> {
    List<MyServiceField> findByServiceIdOrderBySortOrderAscIdAsc(Long serviceId);
    List<MyServiceField> findByServiceIdAndFormatIdIsNull(Long serviceId);
}
