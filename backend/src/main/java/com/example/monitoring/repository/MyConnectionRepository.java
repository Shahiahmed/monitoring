package com.example.monitoring.repository;

import com.example.monitoring.entity.MyConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MyConnectionRepository extends JpaRepository<MyConnection, Long> {

    @Query("SELECT DISTINCT c.serviceOwner FROM MyConnection c WHERE c.serviceOwner IS NOT NULL ORDER BY c.serviceOwner")
    List<String> findDistinctOwners();

    @Query("SELECT DISTINCT c.isClientMtzn FROM MyConnection c WHERE c.isClientMtzn IS NOT NULL ORDER BY c.isClientMtzn")
    List<String> findDistinctIsClients();
}
