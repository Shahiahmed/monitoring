package com.example.monitoring.repository;

import com.example.monitoring.entity.ApplicationNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApplicationNewRepository extends JpaRepository<ApplicationNew, Long> {

    @Query("SELECT a FROM ApplicationNew a ORDER BY a.name ASC")
    List<ApplicationNew> findAllOrderByName();
}
