package com.worker.ai_productivity.metrics.dto;

public class WorkerMetricsDto {

    private String workerId;
    private String name;
    private long totalActiveSeconds;
    private long totalIdleSeconds;
    private double utilizationPercent;
    private long totalUnitsProduced;
    private double unitsPerHour;

    public WorkerMetricsDto() {}

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalActiveSeconds() {
        return totalActiveSeconds;
    }

    public void setTotalActiveSeconds(long totalActiveSeconds) {
        this.totalActiveSeconds = totalActiveSeconds;
    }

    public long getTotalIdleSeconds() {
        return totalIdleSeconds;
    }

    public void setTotalIdleSeconds(long totalIdleSeconds) {
        this.totalIdleSeconds = totalIdleSeconds;
    }

    public double getUtilizationPercent() {
        return utilizationPercent;
    }

    public void setUtilizationPercent(double utilizationPercent) {
        this.utilizationPercent = utilizationPercent;
    }

    public long getTotalUnitsProduced() {
        return totalUnitsProduced;
    }

    public void setTotalUnitsProduced(long totalUnitsProduced) {
        this.totalUnitsProduced = totalUnitsProduced;
    }

    public double getUnitsPerHour() {
        return unitsPerHour;
    }

    public void setUnitsPerHour(double unitsPerHour) {
        this.unitsPerHour = unitsPerHour;
    }
}
