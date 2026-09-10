package com.agriguard.dto;

import java.util.Map;

public class StatsResponse {
    private long totalScans;
    private long healthyCount;
    private long diseasedCount;
    private long stressedCount;
    private double healthRatePercent;
    private Map<String, Long> cropDistribution;
    private Map<String, Long> severityDistribution;

    public StatsResponse() {}

    public long getTotalScans() { return totalScans; }
    public void setTotalScans(long totalScans) { this.totalScans = totalScans; }

    public long getHealthyCount() { return healthyCount; }
    public void setHealthyCount(long healthyCount) { this.healthyCount = healthyCount; }

    public long getDiseasedCount() { return diseasedCount; }
    public void setDiseasedCount(long diseasedCount) { this.diseasedCount = diseasedCount; }

    public long getStressedCount() { return stressedCount; }
    public void setStressedCount(long stressedCount) { this.stressedCount = stressedCount; }

    public double getHealthRatePercent() { return healthRatePercent; }
    public void setHealthRatePercent(double healthRatePercent) { this.healthRatePercent = healthRatePercent; }

    public Map<String, Long> getCropDistribution() { return cropDistribution; }
    public void setCropDistribution(Map<String, Long> cropDistribution) { this.cropDistribution = cropDistribution; }

    public Map<String, Long> getSeverityDistribution() { return severityDistribution; }
    public void setSeverityDistribution(Map<String, Long> severityDistribution) { this.severityDistribution = severityDistribution; }
}
