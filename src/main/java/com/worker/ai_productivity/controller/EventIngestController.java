package com.worker.ai_productivity.controller;

import com.worker.ai_productivity.dto.AIEventDto;
import com.worker.ai_productivity.dto.BulkAIEventDto;
import com.worker.ai_productivity.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@Validated
public class EventIngestController {

    private final EventService eventService;

    public EventIngestController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> ingestSingle(@Valid @RequestBody AIEventDto dto) {
        if ("product_count".equalsIgnoreCase(dto.getEventType()) && dto.getCount() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "count is required for product_count events"));
        }
        Map<String, Object> res = eventService.ingestSingle(dto);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/events/bulk")
    public ResponseEntity<?> ingestBulk(@Valid @RequestBody BulkAIEventDto bulkDto) {
        Map<String, Object> res = eventService.ingestBulk(bulkDto.getEvents());
        return ResponseEntity.ok(res);
    }
}
