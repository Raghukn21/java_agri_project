package com.agriguard.service;

import com.agriguard.dto.AiPredictionResult;
import com.agriguard.dto.DiagnosisResponse;
import com.agriguard.dto.DiseaseDto;
import com.agriguard.entity.*;
import com.agriguard.exception.InvalidPlantImageException;
import com.agriguard.repository.CropRepository;
import com.agriguard.repository.DiagnosisRepository;
import com.agriguard.repository.DiseaseRepository;
import com.agriguard.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiagnosisService {

    private final FileStorageService fileStorageService;
    private final AiClientService aiClientService;
    private final AdvisoryService advisoryService;
    private final DiseaseService diseaseService;
    private final DiseaseRepository diseaseRepository;
    private final CropRepository cropRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DiagnosisService(FileStorageService fileStorageService,
                            AiClientService aiClientService,
                            AdvisoryService advisoryService,
                            DiseaseService diseaseService,
                            DiseaseRepository diseaseRepository,
                            CropRepository cropRepository,
                            DiagnosisRepository diagnosisRepository,
                            UserRepository userRepository) {
        this.fileStorageService = fileStorageService;
        this.aiClientService = aiClientService;
        this.advisoryService = advisoryService;
        this.diseaseService = diseaseService;
        this.diseaseRepository = diseaseRepository;
        this.cropRepository = cropRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DiagnosisResponse diagnosePlant(MultipartFile file,
                                          String cropName,
                                          String environment,
                                          String growthStage,
                                          String notes,
                                          Long userId) {
        // 1. Run AI vision analysis & strict plant-only validation
        AiPredictionResult aiResult = aiClientService.predict(file, cropName, environment, growthStage, notes);

        if (!aiResult.isPlant()) {
            String message = (aiResult.getRejectionMessage() != null)
                    ? aiResult.getRejectionMessage()
                    : "Uploaded image is not a valid plant photo. Please upload a clear photo of an agricultural plant.";
            String reason = (aiResult.getRejectionReason() != null)
                    ? aiResult.getRejectionReason()
                    : "NON_PLANT_DETECTED";
            throw new InvalidPlantImageException(message, reason, aiResult.getPlantConfidence());
        }

        // 2. Store uploaded full-plant image
        String storedFileName = fileStorageService.storeFile(file);
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : storedFileName;

        // 3. Resolve Disease from DB
        String diseaseKey = (aiResult.getDiseaseKey() != null && !aiResult.getDiseaseKey().isBlank())
                ? aiResult.getDiseaseKey()
                : "unknown_stress_general";

        Optional<Disease> diseaseOpt = diseaseRepository.findByDiseaseKey(diseaseKey);
        Disease disease;
        if (diseaseOpt.isPresent()) {
            disease = diseaseOpt.get();
        } else {
            // Fallback to unknown stress general or create on the fly
            disease = diseaseRepository.findByDiseaseKey("unknown_stress_general")
                    .orElseGet(() -> {
                        Disease fallback = new Disease();
                        fallback.setDiseaseKey(diseaseKey);
                        fallback.setDisplayName(aiResult.getDisplayName() != null ? aiResult.getDisplayName() : "Unidentified Stress");
                        fallback.setCategory(CategoryEnum.UNKNOWN);
                        fallback.setDescription("Condition requires agronomic evaluation.");
                        fallback.setTreatment("Inspect soil moisture, aeration, and pests.");
                        fallback.setPrevention("Maintain balanced watering and crop sanitation.");
                        fallback.setIsGeneral(true);
                        return diseaseRepository.save(fallback);
                    });
        }

        // 4. Resolve Health Status and Severity Enums
        HealthStatusEnum healthStatus = parseHealthStatus(aiResult.getHealthStatus());
        SeverityEnum severity = parseSeverity(aiResult.getSeverity());
        Double confidence = (aiResult.getConfidence() != null) ? aiResult.getConfidence() : 0.75;
        String predictedCrop = (aiResult.getSuggestedCrop() != null && !aiResult.getSuggestedCrop().isBlank())
                ? aiResult.getSuggestedCrop()
                : (cropName != null && !cropName.isBlank() ? cropName : "General Plant");

        // 5. Generate Agronomic Checklist and Recovery Action Plan
        List<String> checklist = advisoryService.generateChecklist(disease, healthStatus, severity, confidence);
        Map<String, Object> actionPlan = advisoryService.generateActionPlan(disease, healthStatus, severity, confidence);

        String checklistJson = "";
        try {
            checklistJson = objectMapper.writeValueAsString(checklist);
        } catch (Exception ignored) {}

        // 6. Resolve User (if logged in)
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // 7. Save Diagnosis Record
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setUser(user);
        diagnosis.setImagePath(storedFileName);
        diagnosis.setOriginalFileName(originalFileName);
        diagnosis.setPredictedCrop(predictedCrop);
        diagnosis.setDisease(disease);
        diagnosis.setDiseaseKey(diseaseKey);
        diagnosis.setConfidence(confidence);
        diagnosis.setSeverity(severity);
        diagnosis.setHealthStatus(healthStatus);
        diagnosis.setGrowthStage(growthStage);
        diagnosis.setEnvironment(environment);
        diagnosis.setUserNotes(notes);
        diagnosis.setModelNotes(aiResult.getModelNotes());
        diagnosis.setDiagnosticChecklist(checklistJson);

        Diagnosis saved = diagnosisRepository.save(diagnosis);

        // 8. Build and Return DiagnosisResponse DTO
        return mapToResponse(saved, actionPlan, aiResult.getFeaturesSummary());
    }

    public List<DiagnosisResponse> getHistory(Long userId) {
        List<Diagnosis> list = (userId != null)
                ? diagnosisRepository.findByUserIdOrderByDiagnosedAtDesc(userId)
                : diagnosisRepository.findAllByOrderByDiagnosedAtDesc();

        return list.stream()
                .map(d -> mapToResponse(d, null, null))
                .collect(Collectors.toList());
    }

    public Optional<DiagnosisResponse> getDiagnosisById(Long id) {
        return diagnosisRepository.findById(id)
                .map(d -> mapToResponse(d, null, null));
    }

    public boolean deleteDiagnosis(Long id) {
        if (diagnosisRepository.existsById(id)) {
            diagnosisRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private DiagnosisResponse mapToResponse(Diagnosis d, Map<String, Object> actionPlan, Map<String, Object> featuresSummary) {
        DiagnosisResponse resp = new DiagnosisResponse();
        resp.setId(d.getId());
        resp.setHealthStatus(d.getHealthStatus());
        resp.setPredictedCrop(d.getPredictedCrop());
        resp.setDiseaseKey(d.getDiseaseKey());
        resp.setConfidence(d.getConfidence());
        resp.setSeverity(d.getSeverity());
        resp.setGrowthStage(d.getGrowthStage());
        resp.setEnvironment(d.getEnvironment());
        resp.setUserNotes(d.getUserNotes());
        resp.setModelNotes(d.getModelNotes());
        resp.setImageUrl(fileStorageService.getFileUrl(d.getImagePath()));
        resp.setDiagnosedAt(d.getDiagnosedAt());

        boolean isLow = d.getConfidence() != null && d.getConfidence() < 0.55;
        resp.setIsLowConfidence(isLow);
        resp.setExpertConsultationRecommended(isLow || d.getSeverity() == SeverityEnum.HIGH);

        if (d.getDisease() != null) {
            resp.setDisease(diseaseService.mapToDto(d.getDisease()));
        }

        // Deserialize checklist
        List<String> checklist = new ArrayList<>();
        if (d.getDiagnosticChecklist() != null && !d.getDiagnosticChecklist().isBlank()) {
            try {
                checklist = objectMapper.readValue(d.getDiagnosticChecklist(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        if (checklist.isEmpty() && d.getDisease() != null) {
            checklist = advisoryService.generateChecklist(d.getDisease(), d.getHealthStatus(), d.getSeverity(), d.getConfidence());
        }
        resp.setDiagnosticChecklist(checklist);

        if (actionPlan != null) {
            resp.setActionPlan(actionPlan);
        } else if (d.getDisease() != null) {
            resp.setActionPlan(advisoryService.generateActionPlan(d.getDisease(), d.getHealthStatus(), d.getSeverity(), d.getConfidence()));
        }

        resp.setFeaturesSummary(featuresSummary);
        return resp;
    }

    private HealthStatusEnum parseHealthStatus(String val) {
        if (val == null) return HealthStatusEnum.STRESSED;
        try {
            return HealthStatusEnum.valueOf(val.toUpperCase().trim());
        } catch (Exception e) {
            return HealthStatusEnum.STRESSED;
        }
    }

    private SeverityEnum parseSeverity(String val) {
        if (val == null) return SeverityEnum.MEDIUM;
        try {
            return SeverityEnum.valueOf(val.toUpperCase().trim());
        } catch (Exception e) {
            return SeverityEnum.MEDIUM;
        }
    }
}
