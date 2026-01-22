package com.worker.ai_productivity.metrics.dto;

public class WorkstationMetricsDto {

    private String workstationId;
    private String name;
    private long occupancySeconds;
    private double utilizationPercent;
    private long totalUnitsProduced;
    private double throughputPerHour;

    public WorkstationMetricsDto() {
    }

    public String getWorkstationId() {
        return workstationId;
    }

    public void setWorkstationId(String workstationId) {
        this.workstationId = workstationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOccupancySeconds() {
        return occupancySeconds;
    }

    public void setOccupancySeconds(long occupancySeconds) {
        this.occupancySeconds = occupancySeconds;
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

    public double getThroughputPerHour() {
        return throughputPerHour;
    }

    public void setThroughputPerHour(double throughputPerHour) {
        this.throughputPerHour = throughputPerHour;
    }
}