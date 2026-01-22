package com.worker.ai_productivity.metrics.service;

import com.worker.ai_productivity.metrics.dto.WorkerMetricsDto;
import com.worker.ai_productivity.metrics.dto.WorkstationMetricsDto;
import com.worker.ai_productivity.metrics.dto.FactoryMetricsDto;
import com.worker.ai_productivity.model.AIEvent;
import com.worker.ai_productivity.model.Worker;
import com.worker.ai_productivity.model.Workstation;
import com.worker.ai_productivity.repository.AIEventRepository;
import com.worker.ai_productivity.repository.WorkerRepository;
import com.worker.ai_productivity.repository.WorkstationRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Service
public class MetricsService {

    private final AIEventRepository eventRepository;
    private final WorkerRepository workerRepository;
    private final WorkstationRepository workstationRepository;

    public MetricsService(AIEventRepository eventRepository, WorkerRepository workerRepository, WorkstationRepository workstationRepository) {
        this.eventRepository = eventRepository;
        this.workerRepository = workerRepository;
        this.workstationRepository = workstationRepository;
    }

    public WorkerMetricsDto getWorkerMetrics(String workerId) {
        Worker worker = workerRepository.findByWorkerId(workerId).orElse(null);
        WorkerMetricsDto dto = new WorkerMetricsDto();

        dto.setWorkerId(workerId);
        dto.setName(worker == null ? "unknown" : worker.getName());

        List<AIEvent> events = eventRepository.findByWorkerIdOrderByTimestampAsc(workerId);
        if (events == null || events.isEmpty()) {
            return dto;
        }

        events.sort(Comparator.comparing(AIEvent::getTimestamp));

        long active = 0;
        long idle = 0;
        long absent = 0;
        long units = 0;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (int i = 0; i < events.size(); i++) {
            AIEvent current = events.get(i);
            OffsetDateTime start = current.getTimestamp();
            OffsetDateTime end = (i + 1 < events.size()) ? events.get(i + 1).getTimestamp() : now;
            if (end == null || start == null || end.isBefore(start)) continue;
            long seconds = Duration.between(start, end).getSeconds();
            String type = current.getEventType() == null ? "" : current.getEventType().toLowerCase();

            boolean countAsActive = false;
            if ("working".equals(type)) countAsActive = true;
            if ("product_count".equals(type)) {
                if (i + 1 >= events.size()) countAsActive = true;
                else {
                    AIEvent prev = (i - 1 >= 0) ? events.get(i - 1) : null;
                    AIEvent next = (i + 1 < events.size()) ? events.get(i + 1) : null;
                    String prevType = prev == null || prev.getEventType() == null ? "" : prev.getEventType().toLowerCase();
                    String nextType = next == null || next.getEventType() == null ? "" : next.getEventType().toLowerCase();
                    if ("working".equals(prevType) || "working".equals(nextType)) countAsActive = true;
                }
            }

            if (countAsActive) active += seconds;
            else if ("idle".equals(type)) idle += seconds;
            else if ("absent".equals(type)) absent += seconds;

            if ("product_count".equals(type) && current.getCount() != null) {
                units += current.getCount();
            }
        }

        long observed = active + idle + absent;
        double utilization = observed > 0 ? (100.0 * active / observed) : 0.0;
        double hours = observed / 3600.0;
        double rate = hours > 0 ? (units / hours) : 0.0;

        dto.setTotalActiveSeconds(active);
        dto.setTotalIdleSeconds(idle);
        dto.setUtilizationPercent(Math.round(utilization * 100.0) / 100.0);
        dto.setTotalUnitsProduced(units);
        dto.setUnitsPerHour(Math.round(rate * 100.0) / 100.0);

        return dto;
    }

    public WorkstationMetricsDto getWorkstationMetrics(String workstationId) {
        Workstation ws = workstationRepository.findByWorkstationId(workstationId).orElse(null);
        WorkstationMetricsDto dto = new WorkstationMetricsDto();
        if (ws == null) {
            dto.setWorkstationId(workstationId);
            dto.setName("unknown");
            return dto;
        }
        dto.setWorkstationId(ws.getWorkstationId());
        dto.setName(ws.getName());

        List<AIEvent> events = eventRepository.findByWorkstationIdOrderByTimestampAsc(workstationId);
        if (events == null || events.isEmpty()) {
            return dto;
        }
        events.sort(Comparator.comparing(AIEvent::getTimestamp));

        long occupancy = 0L;
        long active = 0L;
        long units = 0L;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        for (int i = 0; i < events.size(); i++) {
            AIEvent current = events.get(i);
            OffsetDateTime start = current.getTimestamp();
            OffsetDateTime end = (i + 1 < events.size()) ? events.get(i + 1).getTimestamp() : now;
            if (end == null || start == null || end.isBefore(start)) continue;
            long seconds = Duration.between(start, end).getSeconds();
            String type = current.getEventType() == null ? "" : current.getEventType().toLowerCase();

            boolean countAsActive = false;
            if ("working".equals(type)) countAsActive = true;
            if ("product_count".equals(type)) {
                if (i + 1 >= events.size()) countAsActive = true;
                else {
                    AIEvent prev = (i - 1 >= 0) ? events.get(i - 1) : null;
                    AIEvent next = (i + 1 < events.size()) ? events.get(i + 1) : null;
                    String prevType = prev == null || prev.getEventType() == null ? "" : prev.getEventType().toLowerCase();
                    String nextType = next == null || next.getEventType() == null ? "" : next.getEventType().toLowerCase();
                    if ("working".equals(prevType) || "working".equals(nextType)) countAsActive = true;
                }
            }

            if (countAsActive) {
                active += seconds;
                occupancy += seconds;
            } else if ("idle".equals(type)) {
                occupancy += seconds;
            }

            if ("product_count".equals(type) && current.getCount() != null) {
                units += current.getCount();
            }
        }

        dto.setOccupancySeconds(occupancy);
        double utilization = occupancy > 0 ? (100.0 * active / occupancy) : 0.0;
        dto.setUtilizationPercent(Math.round(utilization * 100.0) / 100.0);
        dto.setTotalUnitsProduced(units);

        double hours = occupancy / 3600.0;
        double throughput = hours > 0 ? (units / hours) : 0.0;
        dto.setThroughputPerHour(Math.round(throughput * 100.0) / 100.0);

        return dto;
    }

    public FactoryMetricsDto getFactoryMetrics() {
        FactoryMetricsDto dto = new FactoryMetricsDto();

        List<AIEvent> events = eventRepository.findAll();
        if (events == null || events.isEmpty()) {
            return dto;
        }

        long totalProductive = 0L;
        long totalUnits = 0L;

        events.sort(Comparator.comparing(AIEvent::getTimestamp));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        for (int i = 0; i < events.size(); i++) {
            AIEvent current = events.get(i);
            OffsetDateTime start = current.getTimestamp();
            OffsetDateTime end = (i + 1 < events.size()) ? events.get(i + 1).getTimestamp() : now;
            if (end == null || start == null || end.isBefore(start)) continue;
            long seconds = Duration.between(start, end).getSeconds();
            String type = current.getEventType() == null ? "" : current.getEventType().toLowerCase();

            boolean countAsActive = false;
            if ("working".equals(type)) countAsActive = true;
            if ("product_count".equals(type)) {
                if (i + 1 >= events.size()) countAsActive = true;
                else {
                    AIEvent prev = (i - 1 >= 0) ? events.get(i - 1) : null;
                    AIEvent next = (i + 1 < events.size()) ? events.get(i + 1) : null;
                    String prevType = prev == null || prev.getEventType() == null ? "" : prev.getEventType().toLowerCase();
                    String nextType = next == null || next.getEventType() == null ? "" : next.getEventType().toLowerCase();
                    if ("working".equals(prevType) || "working".equals(nextType)) countAsActive = true;
                }
            }

            if (countAsActive) totalProductive += seconds;
            if ("product_count".equals(type) && current.getCount() != null) totalUnits += current.getCount();
        }

        List<Worker> workers = workerRepository.findAll();
        int workerCount = workers.size();
        double sumUnitsPerHourPerWorker = 0.0;
        double sumUtilizationPercent = 0.0;

        for (Worker w : workers) {
            WorkerMetricsDto wm = getWorkerMetrics(w.getWorkerId());
            sumUnitsPerHourPerWorker += wm.getUnitsPerHour();
            sumUtilizationPercent += wm.getUtilizationPercent();
        }

        double avgProductionRatePerWorker = workerCount > 0 ? (sumUnitsPerHourPerWorker / workerCount) : 0.0;
        double avgUtilizationAcrossWorkers = workerCount > 0 ? (sumUtilizationPercent / workerCount) : 0.0;

        dto.setTotalProductiveSeconds(totalProductive);
        dto.setTotalProductionCount(totalUnits);
        dto.setAverageProductionRatePerHour(Math.round(avgProductionRatePerWorker * 100.0) / 100.0);
        dto.setAverageUtilizationPercentAcrossWorkers(Math.round(avgUtilizationAcrossWorkers * 100.0) / 100.0);

        return dto;
    }
}
