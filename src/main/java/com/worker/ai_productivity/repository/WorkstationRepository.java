package com.worker.ai_productivity.repository;

import com.worker.ai_productivity.model.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    Optional<Workstation> findByWorkstationId(String workstationId);

    List<Workstation> findByWorkstationIdIn(List<String> workstationIds);
}
