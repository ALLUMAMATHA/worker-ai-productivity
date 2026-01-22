package com.worker.ai_productivity.metrics.dto;

public class FactoryMetricsDto {

    private long totalProductiveSeconds;
    private long totalProductionCount;
    private double averageProductionRatePerHour;
    private double averageUtilizationPercentAcrossWorkers;

    public FactoryMetricsDto() {}

    public long getTotalProductiveSeconds() { return totalProductiveSeconds; }
    public void setTotalProductiveSeconds(long totalProductiveSeconds) { this.totalProductiveSeconds = totalProductiveSeconds; }

    public long getTotalProductionCount() { return totalProductionCount; }
    public void setTotalProductionCount(long totalProductionCount) { this.totalProductionCount = totalProductionCount; }

    public double getAverageProductionRatePerHour() { return averageProductionRatePerHour; }
    public void setAverageProductionRatePerHour(double averageProductionRatePerHour) { this.averageProductionRatePerHour = averageProductionRatePerHour; }

    public double getAverageUtilizationPercentAcrossWorkers() { return averageUtilizationPercentAcrossWorkers; }
    public void setAverageUtilizationPercentAcrossWorkers(double averageUtilizationPercentAcrossWorkers) { this.averageUtilizationPercentAcrossWorkers = averageUtilizationPercentAcrossWorkers; }
}
