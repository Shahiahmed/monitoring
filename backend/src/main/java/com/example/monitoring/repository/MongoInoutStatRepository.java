package com.example.monitoring.repository;

import com.example.monitoring.entity.MongoInoutStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MongoInoutStatRepository extends JpaRepository<MongoInoutStat, Long> {

    List<MongoInoutStat> findAllByOrderByStatYearDescStatMonthDescCntDesc();

    List<MongoInoutStat> findBySubsystemOrderByStatYearAscStatMonthAsc(String subsystem);

    @Query("SELECT MAX(s.syncedAt) FROM MongoInoutStat s")
    Optional<LocalDateTime> findLastSyncedAt();

    @Modifying
    @Query("DELETE FROM MongoInoutStat s")
    void deleteAllInBulk();
}
