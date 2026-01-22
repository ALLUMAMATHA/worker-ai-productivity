package com.worker.ai_productivity.seed;

import com.worker.ai_productivity.model.AIEvent;
import com.worker.ai_productivity.model.Worker;
import com.worker.ai_productivity.model.Workstation;
import com.worker.ai_productivity.repository.AIEventRepository;
import com.worker.ai_productivity.repository.WorkerRepository;
import com.worker.ai_productivity.repository.WorkstationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WorkerRepository workerRepository;
    private final WorkstationRepository workstationRepository;
    private final AIEventRepository eventRepository;

    public DataInitializer(WorkerRepository workerRepository,
                           WorkstationRepository workstationRepository,
                           AIEventRepository eventRepository) {
        this.workerRepository = workerRepository;
        this.workstationRepository = workstationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    @Transactional
    public void seedIfEmpty() {
        try {
            if (workerRepository.count() == 0 && workstationRepository.count() == 0 && eventRepository.count() == 0) {
                seedAllSafely();
            }
        } catch (Exception ex) {
            System.err.println("DataInitializer.seedIfEmpty error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void seedAll() {
        seedAllSafely();
    }

    public void forceSeed() {
        eventRepository.deleteAll();
        workerRepository.deleteAll();
        workstationRepository.deleteAll();
        seedAllSafely();
    }

    private void seedAllSafely() {
        seedWorkers();
        seedWorkstations();
        seedEvents();
    }

    private void seedWorkers() {
        List<Worker> seedWorkers = List.of(
                new Worker("W1", "Mamatha"),
                new Worker("W2", "Pavan"),
                new Worker("W3", "Ratna"),
                new Worker("W4", "Sudha"),
                new Worker("W5", "Nanda"),
                new Worker("W6", "Kumar")
        );

        for (Worker w : seedWorkers) {
            try {
                Optional<Worker> existing = workerRepository.findByWorkerId(w.getWorkerId());
                if (existing.isEmpty()) {
                    workerRepository.save(w);
                }
            } catch (DataIntegrityViolationException ex) {
                System.err.println("seed worker skipped (constraint): " + w.getWorkerId() + " -> " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("seed worker error: " + w.getWorkerId() + " -> " + ex.getMessage());
            }
        }
    }

    private void seedWorkstations() {
        List<Workstation> seedStations = List.of(
                new Workstation("S1", "Cutting"),
                new Workstation("S2", "Welding"),
                new Workstation("S3", "Assembly"),
                new Workstation("S4", "Painting"),
                new Workstation("S5", "QC"),
                new Workstation("S6", "Packaging")
        );

        for (Workstation s : seedStations) {
            try {
                Optional<Workstation> existing = workstationRepository.findByWorkstationId(s.getWorkstationId());
                if (existing.isEmpty()) {
                    workstationRepository.save(s);
                }
            } catch (DataIntegrityViolationException ex) {
                System.err.println("seed station skipped (constraint): " + s.getWorkstationId() + " -> " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("seed station error: " + s.getWorkstationId() + " -> " + ex.getMessage());
            }
        }
    }

    private void seedEvents() {
        List<AIEvent> seedEvents = new ArrayList<>();

        // W1 - very productive
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:00:00Z"), "W1", "S1", "working", 0.96, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:00:00Z"), "W1", "S1", "product_count", 0.98, 20));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:15:00Z"), "W1", "S1", "working", 0.95, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:45:00Z"), "W1", "S1", "product_count", 0.97, 15));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:00:00Z"), "W1", "S1", "idle", 0.88, null));

        // W2 - mostly working, moderate production
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:30:00Z"), "W2", "S2", "working", 0.94, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:10:00Z"), "W2", "S2", "product_count", 0.95, 10));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:40:00Z"), "W2", "S2", "idle", 0.90, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:10:00Z"), "W2", "S2", "product_count", 0.92, 8));

        // W3 - more idle, low production
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:15:00Z"), "W3", "S3", "idle", 0.85, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:45:00Z"), "W3", "S3", "working", 0.90, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:20:00Z"), "W3", "S3", "product_count", 0.88, 4));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:00:00Z"), "W3", "S3", "idle", 0.80, null));

        // W4 - absent then bursts of production
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T06:30:00Z"), "W4", "S4", "absent", 0.0, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:50:00Z"), "W4", "S4", "working", 0.92, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:20:00Z"), "W4", "S4", "product_count", 0.95, 6));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:45:00Z"), "W4", "S4", "product_count", 0.94, 5));

        // W5 - steady medium production
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:10:00Z"), "W5", "S5", "working", 0.93, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:40:00Z"), "W5", "S5", "product_count", 0.96, 12));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:10:00Z"), "W5", "S5", "working", 0.92, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:50:00Z"), "W5", "S5", "idle", 0.90, null));

        // W6 - small output, lots of idle
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:00:00Z"), "W6", "S6", "idle", 0.80, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T07:30:00Z"), "W6", "S6", "working", 0.90, null));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T08:00:00Z"), "W6", "S6", "product_count", 0.95, 2));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:00:00Z"), "W6", "S6", "idle", 0.82, null));

        // extra station spikes
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:15:00Z"), "W2", "S6", "product_count", 0.96, 18));
        seedEvents.add(new AIEvent(OffsetDateTime.parse("2026-01-21T09:30:00Z"), "W5", "S2", "product_count", 0.97, 14));

        for (AIEvent ev : seedEvents) {
            try {
                boolean exists = eventRepository.existsByWorkerIdAndWorkstationIdAndTimestampAndEventTypeAndCount(
                        ev.getWorkerId(), ev.getWorkstationId(), ev.getTimestamp(), ev.getEventType(), ev.getCount()
                );
                if (!exists) {
                    try {
                        eventRepository.save(ev);
                    } catch (DataIntegrityViolationException ex) {
                        System.err.println("seed event dup/constraint - skipping: " + ev.getWorkerId() + "/" + ev.getWorkstationId() + " @ " + ev.getTimestamp());
                    }
                }
            } catch (Exception ex) {
                System.err.println("seed event exists-check error: " + ex.getMessage());
            }
        }
    }
}
