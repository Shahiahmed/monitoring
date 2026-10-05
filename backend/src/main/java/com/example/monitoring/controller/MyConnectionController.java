package com.example.monitoring.controller;

import com.example.monitoring.dto.MyConnectionResponse;
import com.example.monitoring.entity.MyConnection;
import com.example.monitoring.repository.MyConnectionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-connections")
public class MyConnectionController {

    private final MyConnectionRepository repo;

    public MyConnectionController(MyConnectionRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<MyConnectionResponse> list() {
        return repo.findAll().stream()
                .map(c -> new MyConnectionResponse(
                        c.getId(),
                        c.getConnectionDate(),
                        c.getServiceKey(),
                        c.getServiceOwner(),
                        c.getIsOwner(),
                        c.getIsClientMtzn(),
                        c.getSmartBridgeTicket()
                ))
                .toList();
    }

    @GetMapping("/owners")
    public List<String> owners() {
        return repo.findDistinctOwners();
    }

    @GetMapping("/is-clients")
    public List<String> isClients() {
        return repo.findDistinctIsClients();
    }
}
