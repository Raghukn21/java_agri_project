package com.agriguard.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @Column(name = "family_name", length = 100)
    private String familyName;

    @Column(name = "common_diseases", columnDefinition = "TEXT")
    private String commonDiseases;

    @Column(name = "general_care", columnDefinition = "TEXT")
    private String generalCare;

    @Column(name = "optimal_temp", length = 50)
    private String optimalTemp;

    @Column(name = "optimal_ph", length = 30)
    private String optimalPh;

    @Column(name = "watering_tips", columnDefinition = "TEXT")
    private String wateringTips;

    @Column(name = "icon_url")
    private String iconUrl;

    public Crop() {}

    public Crop(String name, String scientificName, String familyName, String commonDiseases, 
                String generalCare, String optimalTemp, String optimalPh, String wateringTips, String iconUrl) {
        this.name = name;
        this.scientificName = scientificName;
        this.familyName = familyName;
        this.commonDiseases = commonDiseases;
        this.generalCare = generalCare;
        this.optimalTemp = optimalTemp;
        this.optimalPh = optimalPh;
        this.wateringTips = wateringTips;
        this.iconUrl = iconUrl;
    }

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
