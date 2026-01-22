package com.worker.ai_productivity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BulkAIEventDto {

    @NotEmpty
    @Valid
    private List<AIEventDto> events;

    public BulkAIEventDto() {}

    public List<AIEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<AIEventDto> events) {
        this.events = events;
    }
}
