package com.agriguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AiPredictionResult {

    @JsonProperty("health_status")
    private String healthStatus;

    @JsonProperty("disease_key")
    private String diseaseKey;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("suggested_crop")
    private String suggestedCrop;

    @JsonProperty("category")
    private String category;

    @JsonProperty("model_notes")
    private String modelNotes;

    @JsonProperty("is_plant")
    private Boolean isPlant = true;

    @JsonProperty("plant_confidence")
    private Double plantConfidence = 1.0;

    @JsonProperty("rejection_reason")
    private String rejectionReason;

    @JsonProperty("rejection_message")
    private String rejectionMessage;

    @JsonProperty("features_summary")
    private Map<String, Object> featuresSummary;

    public AiPredictionResult() {}

    public Boolean getIsPlant() { return isPlant; }
    public void setIsPlant(Boolean plant) { isPlant = plant; }
    public boolean isPlant() { return isPlant != null && isPlant; }

    public Double getPlantConfidence() { return plantConfidence; }
    public void setPlantConfidence(Double plantConfidence) { this.plantConfidence = plantConfidence; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getRejectionMessage() { return rejectionMessage; }
    public void setRejectionMessage(String rejectionMessage) { this.rejectionMessage = rejectionMessage; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getSuggestedCrop() { return suggestedCrop; }
    public void setSuggestedCrop(String suggestedCrop) { this.suggestedCrop = suggestedCrop; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getModelNotes() { return modelNotes; }
    public void setModelNotes(String modelNotes) { this.modelNotes = modelNotes; }

    public Map<String, Object> getFeaturesSummary() { return featuresSummary; }
    public void setFeaturesSummary(Map<String, Object> featuresSummary) { this.featuresSummary = featuresSummary; }
}
