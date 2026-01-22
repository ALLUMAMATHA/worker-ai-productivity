package com.worker.ai_productivity.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "ai_events",
        indexes = {
                @Index(name = "idx_ai_worker_ts", columnList = "worker_id, timestamp"),
                @Index(name = "idx_ai_station_ts", columnList = "workstation_id, timestamp")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_dedupe",
                        columnNames = {"worker_id", "workstation_id", "timestamp", "event_type", "count"}
                )
        }
)
public class AIEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "worker_id", nullable = false)
    private String workerId;

    @Column(name = "workstation_id", nullable = false)
    private String workstationId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    private Double confidence;

    private Integer count;

    public AIEvent() {}

    public AIEvent(
            OffsetDateTime timestamp,
            String workerId,
            String workstationId,
            String eventType,
            Double confidence,
            Integer count
    ) {
        this.timestamp = timestamp;
        this.workerId = workerId;
        this.workstationId = workstationId;
        this.eventType = eventType;
        this.confidence = confidence;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(String workstationId) {
        this.workstationId = workstationId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
