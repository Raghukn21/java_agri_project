package com.agriguard.dto;

import com.agriguard.entity.CategoryEnum;

public class DiseaseDto {
    private Long id;
    private String diseaseKey;
    private String displayName;
    private Long cropId;
    private String cropName;
    private CategoryEnum category;
    private String description;
    private String symptoms;
    private String treatment;
    private String prevention;
    private String severityGuidance;
    private String organicControl;
    private String chemicalControl;
    private Boolean isGeneral;

    public DiseaseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDiseaseKey() { return diseaseKey; }
    public void setDiseaseKey(String diseaseKey) { this.diseaseKey = diseaseKey; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Long getCropId() { return cropId; }
    public void setCropId(Long cropId) { this.cropId = cropId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

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
