package com.worker.ai_productivity.controller;

import com.worker.ai_productivity.model.AIEvent;
import com.worker.ai_productivity.repository.AIEventRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventQueryController {

    private final AIEventRepository repo;

    public EventQueryController(AIEventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/workers/{id}/recent")
    public List<AIEvent> workerRecent(@PathVariable String id) {
        return repo.findTop5ByWorkerIdOrderByTimestampDesc(id);
    }

    @GetMapping("/workstations/{id}/recent")
    public List<AIEvent> stationRecent(@PathVariable String id) {
        return repo.findTop5ByWorkstationIdOrderByTimestampDesc(id);
    }

    @GetMapping("/recent")
    public List<AIEvent> recent() {
        return repo.findTop50ByOrderByTimestampDesc();
    }
}
