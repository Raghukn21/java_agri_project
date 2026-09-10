package com.agriguard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnoses")
public class Diagnosis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "predicted_crop", length = 100)
    private String predictedCrop;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "disease_id", nullable = true)
    private Disease disease;

    @Column(name = "disease_key", nullable = false, length = 100)
    private String diseaseKey;

    @Column(nullable = false)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeverityEnum severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_status", nullable = false, length = 20)
    private HealthStatusEnum healthStatus;

    @Column(name = "growth_stage", length = 50)
    private String growthStage;

    @Column(length = 50)
    private String environment;

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes;

    @Column(name = "model_notes", columnDefinition = "TEXT")
    private String modelNotes;

    @Column(name = "diagnostic_checklist", columnDefinition = "TEXT")
    private String diagnosticChecklist;

    @Column(name = "diagnosed_at", updatable = false)
    private LocalDateTime diagnosedAt = LocalDateTime.now();

    public Diagnosis() {}

    public Diagnosis(User user, String imagePath, String originalFileName, String predictedCrop, 
                     Disease disease, String diseaseKey, Double confidence, SeverityEnum severity, 
                     HealthStatusEnum healthStatus, String growthStage, String environment, 
                     String userNotes, String modelNotes, String diagnosticChecklist) {
        this.user = user;
        this.imagePath = imagePath;
        this.originalFileName = originalFileName;
        this.predictedCrop = predictedCrop;
        this.disease = disease;
        this.diseaseKey = diseaseKey;
        this.confidence = confidence;
        this.severity = severity;
        this.healthStatus = healthStatus;
        this.growthStage = growthStage;
        this.environment = environment;
        this.userNotes = userNotes;
        this.modelNotes = modelNotes;
        this.diagnosticChecklist = diagnosticChecklist;
        this.diagnosedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getPredictedCrop() { return predictedCrop; }
    public void setPredictedCrop(String predictedCrop) { this.predictedCrop = predictedCrop; }

    public Disease getDisease() { return disease; }
    public void setDisease(Disease disease) { this.disease = disease; }

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public SeverityEnum getSeverity() { return severity; }
    public void setSeverity(SeverityEnum severity) { this.severity = severity; }

    public HealthStatusEnum getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatusEnum healthStatus) { this.healthStatus = healthStatus; }

    public String getGrowthStage() { return growthStage; }
    public void setGrowthStage(String growthStage) { this.growthStage = growthStage; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public String getModelNotes() { return modelNotes; }
    public void setModelNotes(String modelNotes) { this.modelNotes = modelNotes; }

    public String getDiagnosticChecklist() { return diagnosticChecklist; }
    public void setDiagnosticChecklist(String diagnosticChecklist) { this.diagnosticChecklist = diagnosticChecklist; }

    public LocalDateTime getDiagnosedAt() { return diagnosedAt; }
    public void setDiagnosedAt(LocalDateTime diagnosedAt) { this.diagnosedAt = diagnosedAt; }
}
