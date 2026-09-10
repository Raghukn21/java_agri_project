package com.agriguard.dto;

import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DiagnosisResponse {
    private Long id;
    private HealthStatusEnum healthStatus;
    private String predictedCrop;
    private String diseaseKey;
    private DiseaseDto disease;
    private Double confidence;
    private SeverityEnum severity;
    private String growthStage;
    private String environment;
    private String userNotes;
    private String modelNotes;
    private String imageUrl;
    private List<String> diagnosticChecklist;
    private Map<String, Object> actionPlan;
    private Map<String, Object> featuresSummary;
    private Boolean isLowConfidence;
    private Boolean expertConsultationRecommended;
    private LocalDateTime diagnosedAt;

    public DiagnosisResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HealthStatusEnum getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatusEnum healthStatus) { this.healthStatus = healthStatus; }

    public String getPredictedCrop() { return predictedCrop; }
    public void setPredictedCrop(String predictedCrop) { this.predictedCrop = predictedCrop; }

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }

    public DiseaseDto getDisease() { return disease; }
    public void setDisease(DiseaseDto disease) { this.disease = disease; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public SeverityEnum getSeverity() { return severity; }
    public void setSeverity(SeverityEnum severity) { this.severity = severity; }

    public String getGrowthStage() { return growthStage; }
    public void setGrowthStage(String growthStage) { this.growthStage = growthStage; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public String getModelNotes() { return modelNotes; }
    public void setModelNotes(String modelNotes) { this.modelNotes = modelNotes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getDiagnosticChecklist() { return diagnosticChecklist; }
    public void setDiagnosticChecklist(List<String> diagnosticChecklist) { this.diagnosticChecklist = diagnosticChecklist; }

    public Map<String, Object> getActionPlan() { return actionPlan; }
    public void setActionPlan(Map<String, Object> actionPlan) { this.actionPlan = actionPlan; }

    public Map<String, Object> getFeaturesSummary() { return featuresSummary; }
    public void setFeaturesSummary(Map<String, Object> featuresSummary) { this.featuresSummary = featuresSummary; }

    public Boolean getIsLowConfidence() { return isLowConfidence; }
    public void setIsLowConfidence(Boolean lowConfidence) { isLowConfidence = lowConfidence; }

    public Boolean getExpertConsultationRecommended() { return expertConsultationRecommended; }
    public void setExpertConsultationRecommended(Boolean expertConsultationRecommended) { this.expertConsultationRecommended = expertConsultationRecommended; }

    public LocalDateTime getDiagnosedAt() { return diagnosedAt; }
    public void setDiagnosedAt(LocalDateTime diagnosedAt) { this.diagnosedAt = diagnosedAt; }
}
