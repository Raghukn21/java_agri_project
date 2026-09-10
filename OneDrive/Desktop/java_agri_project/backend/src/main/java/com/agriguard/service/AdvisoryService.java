package com.agriguard.service;

import com.agriguard.entity.Disease;
import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdvisoryService {

    public List<String> generateChecklist(Disease disease, HealthStatusEnum status, SeverityEnum severity, Double confidence) {
        List<String> checklist = new ArrayList<>();

        if (status == HealthStatusEnum.HEALTHY) {
            checklist.add("Continue regular monitoring (inspect leaf undersides weekly).");
            checklist.add("Maintain consistent drip watering directly at soil level.");
            checklist.add("Apply balanced organic compost / mulch to retain soil vitality.");
            return checklist;
        }

        if (confidence != null && confidence < 0.55) {
            checklist.add("Double-check soil moisture: Is soil waterlogged or bone dry?");
            checklist.add("Inspect leaf undersides with a magnifying lens for micro-pests (spider mites / thrips).");
            checklist.add("Verify soil pH and test for electrical conductivity (salinity).");
            checklist.add("Take a clear sample to your local university agricultural extension center.");
            return checklist;
        }

        if (severity == SeverityEnum.HIGH) {
            checklist.add("🚨 IMMEDIATE: Isolate or bag severely diseased sections to prevent spore dispersal.");
            checklist.add("Sanitize all pruning shears with 70% isopropyl alcohol between cuts.");
        } else {
            checklist.add("Prune off early symptomatic lower foliage using sterilized shears.");
        }

        if (disease != null && disease.getCategory() != null) {
            switch (disease.getCategory()) {
                case FUNGAL:
                    checklist.add("Cease overhead sprinkler watering immediately; transition to drip lines.");
                    checklist.add("Increase air circulation by thinning interior dense suckers.");
                    checklist.add("Apply protective bio-fungicide or copper spray in the early morning.");
                    break;
                case BACTERIAL:
                    checklist.add("Do NOT handle or cultivate plants while foliage is wet from rain or dew.");
                    checklist.add("Apply copper bactericide combined with bio-stimulants.");
                    break;
                case VIRAL:
                    checklist.add("Deploy yellow sticky traps to suppress whitefly, aphid, or thrips vectors.");
                    checklist.add("Remove infected plants if spread threatens adjacent healthy crops.");
                    break;
                case PEST:
                    checklist.add("Spray neem oil / insecticidal soap on stems and leaf undersides.");
                    checklist.add("Introduce beneficial predatory insects (lacewings / ladybugs).");
                    break;
                case ABIOTIC:
                    checklist.add("Review nutrient dosing (N-P-K balance and micronutrient chelation).");
                    checklist.add("Adjust irrigation timer and verify root zone drainage.");
                    break;
                default:
                    checklist.add("Follow standard agronomic sanitation and nutrient balancing.");
            }
        }

        checklist.add("Re-scan plant in 5-7 days to verify recovery trajectory.");
        return checklist;
    }

    public Map<String, Object> generateActionPlan(Disease disease, HealthStatusEnum status, SeverityEnum severity, Double confidence) {
        Map<String, Object> plan = new LinkedHashMap<>();

        List<String> immediateSteps = new ArrayList<>();
        List<String> organicRemedies = new ArrayList<>();
        List<String> chemicalTreatments = new ArrayList<>();
        List<String> culturalPractices = new ArrayList<>();
        String expertAdvisory;

        if (status == HealthStatusEnum.HEALTHY) {
            immediateSteps.add("No corrective intervention needed. Plant exhibits strong vigor.");
            organicRemedies.add("Monthly compost tea root drench to sustain beneficial rhizosphere.");
            chemicalTreatments.add("None required.");
            culturalPractices.add("Maintain regular staking, balanced irrigation, and weed control.");
            expertAdvisory = "Your crop health is optimal. Continue good agronomic practices.";
        } else if (confidence != null && confidence < 0.55) {
            immediateSteps.add("Perform root ball inspection: check for dark soggy roots or rootbound constriction.");
            immediateSteps.add("Check ambient temperature and light exposure for heat or scorch stress.");
            organicRemedies.add("Foliar seaweed / kelp biostimulant drench to alleviate osmotic stress.");
            organicRemedies.add("Broad-spectrum cold-pressed neem oil spray (5ml/L water).");
            chemicalTreatments.add("Avoid broad-spectrum synthetic pesticides until exact cause is diagnosed.");
            culturalPractices.add("Aerate compacted soil, improve bed drainage, and ensure 6-8 hrs daylight.");
            expertAdvisory = "Because confidence is below 55%, we strongly advise consulting your local agricultural extension service or agronomist before applying synthetic chemical controls.";
        } else {
            if (disease != null && disease.getTreatment() != null && !disease.getTreatment().isBlank()) {
                immediateSteps.addAll(Arrays.asList(disease.getTreatment().split("\\. ")));
            } else {
                immediateSteps.add("Remove heavily infected leaves and destroy plant debris.");
            }

            if (disease != null && disease.getOrganicControl() != null && !disease.getOrganicControl().isBlank()) {
                organicRemedies.addAll(Arrays.asList(disease.getOrganicControl().split(", |\\. ")));
            } else {
                organicRemedies.add("Apply organic copper octanoate or Bacillus subtilis foliar spray.");
            }

            if (disease != null && disease.getChemicalControl() != null && !disease.getChemicalControl().isBlank()) {
                chemicalTreatments.addAll(Arrays.asList(disease.getChemicalControl().split(", |\\. ")));
            } else {
                chemicalTreatments.add("Targeted fungicide/bactericide approved for specific crop.");
            }

            if (disease != null && disease.getPrevention() != null && !disease.getPrevention().isBlank()) {
                culturalPractices.addAll(Arrays.asList(disease.getPrevention().split("\\. ")));
            } else {
                culturalPractices.add("Maintain proper spacing, drip irrigation, and 3-year crop rotation.");
            }

            if (severity == SeverityEnum.HIGH) {
                expertAdvisory = "HIGH SEVERITY ALERT: This infection can spread rapidly across your field or greenhouse. If symptoms do not stabilize within 48-72 hours post-treatment, engage an agronomist immediately.";
            } else {
                expertAdvisory = "Monitor crop closely over the next week. Apply treatments early morning or late afternoon to avoid leaf burn.";
            }
        }

        plan.put("immediateSteps", immediateSteps);
        plan.put("organicRemedies", organicRemedies);
        plan.put("chemicalTreatments", chemicalTreatments);
        plan.put("culturalPractices", culturalPractices);
        plan.put("expertAdvisory", expertAdvisory);

        return plan;
    }
}
