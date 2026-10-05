package com.example.monitoring.repository;

import com.example.monitoring.entity.ApplicationInfoNew;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationInfoNewRepository extends JpaRepository<ApplicationInfoNew, Long> {

    List<ApplicationInfoNew> findByApplicationId(Long applicationId);

    List<ApplicationInfoNew> findByServerId(Long serverId);

    List<ApplicationInfoNew> findByEnvId(Long envId);
}
