package com.worker.ai_productivity.service;

import com.worker.ai_productivity.dto.AIEventDto;
import com.worker.ai_productivity.model.AIEvent;
import com.worker.ai_productivity.repository.AIEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final AIEventRepository eventRepo;

    public EventService(AIEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Transactional
    public Map<String, Object> ingestSingle(AIEventDto dto) {
        AIEvent entity = mapDtoToEntity(dto);
        boolean exists = eventRepo.existsByWorkerIdAndWorkstationIdAndTimestampAndEventTypeAndCount(
                entity.getWorkerId(),
                entity.getWorkstationId(),
                entity.getTimestamp(),
                entity.getEventType(),
                entity.getCount()
        );
        if (exists) {
            return Map.of("saved", false, "reason", "duplicate");
        }
        try {
            AIEvent saved = eventRepo.save(entity);
            return Map.of("saved", true, "id", saved.getId());
        } catch (DataIntegrityViolationException ex) {
            return Map.of("saved", false, "reason", "duplicate");
        }
    }

    @Transactional
    public Map<String, Object> ingestBulk(List<AIEventDto> dtos) {
        List<AIEventDto> accepted = new ArrayList<>();
        List<Integer> rejectedIndexes = new ArrayList<>();

        for (int i = 0; i < dtos.size(); i++) {
            AIEventDto dto = dtos.get(i);
            AIEvent e = mapDtoToEntity(dto);
            boolean exists = eventRepo.existsByWorkerIdAndWorkstationIdAndTimestampAndEventTypeAndCount(
                    e.getWorkerId(),
                    e.getWorkstationId(),
                    e.getTimestamp(),
                    e.getEventType(),
                    e.getCount()
            );
            if (exists) {
                rejectedIndexes.add(i);
            } else {
                accepted.add(dto);
            }
        }

        List<AIEvent> toSave = accepted.stream().map(this::mapDtoToEntity).collect(Collectors.toList());
        List<AIEvent> saved;
        try {
            saved = eventRepo.saveAll(toSave);
        } catch (DataIntegrityViolationException ex) {
            saved = new ArrayList<>();
            for (AIEvent ev : toSave) {
                try {
                    saved.add(eventRepo.save(ev));
                } catch (DataIntegrityViolationException e) {
                }
            }
        }

        return Map.of(
                "requested", dtos.size(),
                "accepted", saved.size(),
                "rejected_indexes", rejectedIndexes
        );
    }

    private AIEvent mapDtoToEntity(AIEventDto dto) {
        OffsetDateTime ts = dto.getTimestamp();
        String workerId = dto.getWorkerId();
        String stationId = dto.getWorkstationId();
        String eventType = dto.getEventType();
        Double confidence = dto.getConfidence();
        Integer count = dto.getCount();
        return new AIEvent(ts, workerId, stationId, eventType, confidence, count);
    }
}
