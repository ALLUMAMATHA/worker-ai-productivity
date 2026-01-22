package com.worker.ai_productivity.repository;

import com.worker.ai_productivity.model.AIEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;
import java.util.List;

public interface AIEventRepository extends JpaRepository<AIEvent, Long> {
    List<AIEvent> findTop5ByWorkerIdOrderByTimestampDesc(String workerId);

    List<AIEvent> findTop5ByWorkstationIdOrderByTimestampDesc(String workstationId);

    List<AIEvent> findTop50ByOrderByTimestampDesc();

    List<AIEvent> findByWorkerIdOrderByTimestampAsc(String workerId);

    List<AIEvent> findByWorkstationIdOrderByTimestampAsc(String stationId);

    boolean existsByWorkerIdAndWorkstationIdAndTimestampAndEventTypeAndCount(java.lang.String workerId, java.lang.String workstationId, java.time.OffsetDateTime timestamp, java.lang.String eventType, java.lang.Integer count);
}