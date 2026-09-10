package com.agriguard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "diseases")
public class Disease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disease_key", nullable = false, unique = true, length = 100)
    private String diseaseKey;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "crop_id", nullable = true)
    private Crop crop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryEnum category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(columnDefinition = "TEXT")
    private String prevention;

    @Column(name = "severity_guidance", columnDefinition = "TEXT")
    private String severityGuidance;

    @Column(name = "organic_control", columnDefinition = "TEXT")
    private String organicControl;

    @Column(name = "chemical_control", columnDefinition = "TEXT")
    private String chemicalControl;

    @Column(name = "is_general")
    private Boolean isGeneral = false;

    public Disease() {}

    public Disease(String diseaseKey, String displayName, Crop crop, CategoryEnum category, 
                   String description, String symptoms, String treatment, String prevention, 
                   String severityGuidance, String organicControl, String chemicalControl, Boolean isGeneral) {
        this.diseaseKey = diseaseKey;
        this.displayName = displayName;
        this.crop = crop;
        this.category = category;
        this.description = description;
        this.symptoms = symptoms;
        this.treatment = treatment;
        this.prevention = prevention;
        this.severityGuidance = severityGuidance;
        this.organicControl = organicControl;
        this.chemicalControl = chemicalControl;
        this.isGeneral = isGeneral != null ? isGeneral : false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }

    public CategoryEnum getCategory() { return category; }
    public void setCategory(CategoryEnum category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getPrevention() { return prevention; }
    public void setPrevention(String prevention) { this.prevention = prevention; }

    public String getSeverityGuidance() { return severityGuidance; }
    public void setSeverityGuidance(String severityGuidance) { this.severityGuidance = severityGuidance; }

    public String getOrganicControl() { return organicControl; }
    public void setOrganicControl(String organicControl) { this.organicControl = organicControl; }

    public String getChemicalControl() { return chemicalControl; }
    public void setChemicalControl(String chemicalControl) { this.chemicalControl = chemicalControl; }

    public Boolean getIsGeneral() { return isGeneral; }
    public void setIsGeneral(Boolean isGeneral) { this.isGeneral = isGeneral; }
}
