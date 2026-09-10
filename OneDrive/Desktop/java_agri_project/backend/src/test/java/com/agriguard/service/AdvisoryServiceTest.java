package com.agriguard.service;

import com.agriguard.entity.CategoryEnum;
import com.agriguard.entity.Disease;
import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdvisoryServiceTest {

    private AdvisoryService advisoryService;

    @BeforeEach
    void setUp() {
        advisoryService = new AdvisoryService();
    }

    @Test
    @DisplayName("Should generate healthy maintenance advisory for healthy status")
    void testGenerateActionPlanHealthy() {
        Disease disease = new Disease();
        disease.setDiseaseKey("tomato_healthy");
        disease.setDisplayName("Healthy Tomato Plant");
        disease.setCategory(CategoryEnum.UNKNOWN);

        Map<String, Object> plan = advisoryService.generateActionPlan(
                disease,
                HealthStatusEnum.HEALTHY,
                SeverityEnum.LOW,
                0.95
        );

        assertNotNull(plan);
        @SuppressWarnings("unchecked")
        List<String> immediate = (List<String>) plan.get("immediateSteps");
        assertFalse(immediate.isEmpty());
        assertTrue(immediate.get(0).contains("No corrective intervention needed") || immediate.get(0).contains("optimal"));
    }

    @Test
    @DisplayName("Should generate immediate emergency quarantine steps for HIGH severity fungal disease")
    void testGenerateActionPlanHighSeverityFungal() {
        Disease disease = new Disease();
        disease.setDiseaseKey("tomato_late_blight");
        disease.setDisplayName("Late Blight (Tomato)");
        disease.setCategory(CategoryEnum.FUNGAL);
        disease.setTreatment("Apply copper hydroxide or metalaxyl spray. Remove blighted canopy immediately.");
        disease.setPrevention("Avoid overhead irrigation. Destroy infected foliage.");
        disease.setOrganicControl("Trichoderma harzianum soil inoculation.");
        disease.setChemicalControl("Mancozeb 75% WP @ 2.5g/L water.");

        Map<String, Object> plan = advisoryService.generateActionPlan(
                disease,
                HealthStatusEnum.DISEASED,
                SeverityEnum.HIGH,
                0.92
        );

        assertNotNull(plan);
        @SuppressWarnings("unchecked")
        List<String> immediate = (List<String>) plan.get("immediateSteps");
        assertFalse(immediate.isEmpty());
        String advisory = (String) plan.get("expertAdvisory");
        assertTrue(advisory.contains("HIGH SEVERITY ALERT"));
    }

    @Test
    @DisplayName("Should recommend agronomist consultation when confidence is low (< 0.55)")
    void testLowConfidenceChecklist() {
        Disease disease = new Disease();
        disease.setDiseaseKey("unknown_stress_general");
        disease.setDisplayName("Unidentified Abiotic Stress");
        disease.setCategory(CategoryEnum.ABIOTIC);

        List<String> checklist = advisoryService.generateChecklist(
                disease,
                HealthStatusEnum.STRESSED,
                SeverityEnum.MEDIUM,
                0.48
        );

        assertNotNull(checklist);
        assertTrue(checklist.stream().anyMatch(item -> item.contains("agricultural extension center") || item.contains("Verify soil pH")));
    }

    @Test
    @DisplayName("Should generate specific checklists for viral and pest conditions")
    void testViralChecklist() {
        Disease viralDisease = new Disease();
        viralDisease.setDiseaseKey("tomato_yellow_leaf_curl");
        viralDisease.setDisplayName("Tomato Yellow Leaf Curl Virus");
        viralDisease.setCategory(CategoryEnum.VIRAL);

        List<String> checklist = advisoryService.generateChecklist(
                viralDisease,
                HealthStatusEnum.DISEASED,
                SeverityEnum.HIGH,
                0.88
        );

        assertNotNull(checklist);
        assertTrue(checklist.stream().anyMatch(item -> item.contains("traps") || item.contains("vectors") || item.contains("whitefly")));
    }
}
