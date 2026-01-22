package com.worker.ai_productivity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class AIEventDto {

    @NotNull
    private OffsetDateTime timestamp;

    @NotBlank
    @JsonProperty("worker_id")
    private String workerId;

    @NotBlank
    @JsonProperty("workstation_id")
    private String workstationId;

    @NotBlank
    @JsonProperty("event_type")
    private String eventType;

    @DecimalMin("0.0")
    private Double confidence;

    @Min(0)
    private Integer count;

    public AIEventDto() {}

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
