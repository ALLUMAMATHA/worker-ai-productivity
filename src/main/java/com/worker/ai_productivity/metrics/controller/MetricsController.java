package com.worker.ai_productivity.metrics.controller;

import com.worker.ai_productivity.metrics.dto.WorkerMetricsDto;
import com.worker.ai_productivity.metrics.dto.WorkstationMetricsDto;
import com.worker.ai_productivity.metrics.dto.FactoryMetricsDto;
import com.worker.ai_productivity.metrics.service.MetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/metrics/workers/{workerId}")
    public WorkerMetricsDto getWorkerMetrics(@PathVariable String workerId) {
        return metricsService.getWorkerMetrics(workerId);
    }

    @GetMapping("/metrics/workstations/{workstationId}")
    public WorkstationMetricsDto getWorkstationMetrics(@PathVariable String workstationId) {
        return metricsService.getWorkstationMetrics(workstationId);
    }

    @GetMapping("/metrics/factory")
    public FactoryMetricsDto getFactoryMetrics() {
        return metricsService.getFactoryMetrics();
    }
}
