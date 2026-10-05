package com.example.monitoring.repository;

import com.example.monitoring.entity.EQueryCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EQueryCountRepository extends JpaRepository<EQueryCount, Long> {

    List<EQueryCount> findAllByOrderByYearMonthDescMonthlyCountDesc();

    @Query("SELECT MAX(e.syncedAt) FROM EQueryCount e")
    Optional<LocalDateTime> findLastSyncedAt();

    @Modifying
    @Query("DELETE FROM EQueryCount e")
    void deleteAllInBulk();

    @Query("""
        SELECT e.yearMonth,
               CASE WHEN e.shepServiceId = :serviceKey THEN e.code ELSE e.shepServiceId END,
               e.monthlyCount,
               c.organizationName
        FROM EQueryCount e
        LEFT JOIN MyServiceClient c
          ON c.serviceId = (SELECT s.id FROM MyService s WHERE s.serviceKey = :serviceKey)
         AND c.shepSenderId = CASE WHEN e.shepServiceId = :serviceKey THEN e.code ELSE e.shepServiceId END
        WHERE e.shepServiceId = :serviceKey OR e.code = :serviceKey
        ORDER BY e.yearMonth ASC
        """)
    List<Object[]> findByServiceKey(String serviceKey);
}
