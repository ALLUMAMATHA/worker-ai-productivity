package com.worker.ai_productivity.repository;

import com.worker.ai_productivity.model.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByWorkerId(String workerId);
}
