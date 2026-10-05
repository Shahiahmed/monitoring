package com.example.monitoring.repository;

import com.example.monitoring.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {

    @Query(value =
        "SELECT DISTINCT y FROM (" +
        "SELECT EXTRACT(YEAR FROM date_from)::int AS y FROM work_intervals WHERE date_from IS NOT NULL " +
        "UNION " +
        "SELECT EXTRACT(YEAR FROM date_to)::int AS y FROM work_intervals WHERE date_to IS NOT NULL" +
        ") t ORDER BY y DESC",
        nativeQuery = true)
    List<Object> findDistinctYears();
}
