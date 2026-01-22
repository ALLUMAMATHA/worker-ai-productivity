package com.worker.ai_productivity.controller;

import com.worker.ai_productivity.model.Worker;
import com.worker.ai_productivity.model.Workstation;
import com.worker.ai_productivity.repository.WorkerRepository;
import com.worker.ai_productivity.repository.WorkstationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LookupController {

    private final WorkerRepository workerRepository;
    private final WorkstationRepository workstationRepository;

    public LookupController(WorkerRepository workerRepository, WorkstationRepository workstationRepository) {
        this.workerRepository = workerRepository;
        this.workstationRepository = workstationRepository;
    }

    @GetMapping("/workers")
    public List<Worker> listWorkers() {
        return workerRepository.findAll();
    }

    @GetMapping("/workstations")
    public List<Workstation> listStations() {
        return workstationRepository.findAll();
    }
}
