package com.agriguard.dto;

public class CropDto {
    private Long id;
    private String name;
    private String scientificName;
    private String familyName;
    private String commonDiseases;
    private String generalCare;
    private String optimalTemp;
    private String optimalPh;
    private String wateringTips;
    private String iconUrl;

    public CropDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getCommonDiseases() { return commonDiseases; }
    public void setCommonDiseases(String commonDiseases) { this.commonDiseases = commonDiseases; }

    public String getGeneralCare() { return generalCare; }
    public void setGeneralCare(String generalCare) { this.generalCare = generalCare; }

    public String getOptimalTemp() { return optimalTemp; }
    public void setOptimalTemp(String optimalTemp) { this.optimalTemp = optimalTemp; }

    public String getOptimalPh() { return optimalPh; }
    public void setOptimalPh(String optimalPh) { this.optimalPh = optimalPh; }

    public String getWateringTips() { return wateringTips; }
    public void setWateringTips(String wateringTips) { this.wateringTips = wateringTips; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}
