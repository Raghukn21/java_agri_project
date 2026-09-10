package com.agriguard.service;

import com.agriguard.dto.AiPredictionResult;
import com.agriguard.dto.DiagnosisResponse;
import com.agriguard.dto.DiseaseDto;
import com.agriguard.entity.*;
import com.agriguard.repository.CropRepository;
import com.agriguard.repository.DiagnosisRepository;
import com.agriguard.repository.DiseaseRepository;
import com.agriguard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AiClientService aiClientService;
    @Mock
    private AdvisoryService advisoryService;
    @Mock
    private DiseaseService diseaseService;
    @Mock
    private DiseaseRepository diseaseRepository;
    @Mock
    private CropRepository cropRepository;
    @Mock
    private DiagnosisRepository diagnosisRepository;
    @Mock
    private UserRepository userRepository;

    private DiagnosisService diagnosisService;

    @BeforeEach
    void setUp() {
        diagnosisService = new DiagnosisService(
                fileStorageService,
                aiClientService,
                advisoryService,
                diseaseService,
                diseaseRepository,
                cropRepository,
                diagnosisRepository,
                userRepository
        );
    }

    @Test
    @DisplayName("Should successfully diagnose plant image and return formatted DiagnosisResponse")
    void testDiagnosePlantSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "plant.jpg", "image/jpeg", "fake image data".getBytes()
        );

        when(fileStorageService.storeFile(any())).thenReturn("stored_plant.jpg");
        when(fileStorageService.getFileUrl(any())).thenReturn("/uploads/stored_plant.jpg");

        AiPredictionResult aiResult = new AiPredictionResult();
        aiResult.setDiseaseKey("tomato_early_blight");
        aiResult.setDisplayName("Early Blight (Tomato)");
        aiResult.setHealthStatus("DISEASED");
        aiResult.setSeverity("HIGH");
        aiResult.setConfidence(0.92);
        aiResult.setSuggestedCrop("Tomato");
        when(aiClientService.predict(any(), any(), any(), any(), any())).thenReturn(aiResult);

        Disease disease = new Disease();
        disease.setId(1L);
        disease.setDiseaseKey("tomato_early_blight");
        disease.setDisplayName("Early Blight (Tomato)");
        disease.setCategory(CategoryEnum.FUNGAL);
        when(diseaseRepository.findByDiseaseKey("tomato_early_blight")).thenReturn(Optional.of(disease));

        DiseaseDto dto = new DiseaseDto();
        dto.setId(1L);
        dto.setDisplayName("Early Blight (Tomato)");
        dto.setDiseaseKey("tomato_early_blight");
        when(diseaseService.mapToDto(any())).thenReturn(dto);

        when(advisoryService.generateChecklist(any(), any(), any(), anyDouble()))
                .thenReturn(List.of("Check leaf undersides", "Apply fungicide"));
        when(advisoryService.generateActionPlan(any(), any(), any(), anyDouble()))
                .thenReturn(Map.of("strategy", "ACTIVE_INTERVENTION"));

        Diagnosis savedDiagnosis = new Diagnosis();
        savedDiagnosis.setId(101L);
        savedDiagnosis.setImagePath("stored_plant.jpg");
        savedDiagnosis.setDiseaseKey("tomato_early_blight");
        savedDiagnosis.setDisease(disease);
        savedDiagnosis.setPredictedCrop("Tomato");
        savedDiagnosis.setHealthStatus(HealthStatusEnum.DISEASED);
        savedDiagnosis.setSeverity(SeverityEnum.HIGH);
        savedDiagnosis.setConfidence(0.92);
        savedDiagnosis.setDiagnosedAt(LocalDateTime.now());

        when(diagnosisRepository.save(any(Diagnosis.class))).thenReturn(savedDiagnosis);

        DiagnosisResponse response = diagnosisService.diagnosePlant(
                file, "Tomato", "Greenhouse", "Vegetative", "Yellow spots with concentric rings", null
        );

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(HealthStatusEnum.DISEASED, response.getHealthStatus());
        assertEquals(SeverityEnum.HIGH, response.getSeverity());
        assertEquals(0.92, response.getConfidence());
        assertEquals("Tomato", response.getPredictedCrop());
        assertFalse(response.getIsLowConfidence());
        assertTrue(response.getExpertConsultationRecommended()); // High severity recommends consultation
        verify(diagnosisRepository, times(1)).save(any(Diagnosis.class));
    }

    @Test
    @DisplayName("Should flag low confidence diagnosis when score < 0.55")
    void testLowConfidenceDiagnosis() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", "image bytes".getBytes()
        );

        when(fileStorageService.storeFile(any())).thenReturn("stored_leaf.jpg");
        when(fileStorageService.getFileUrl(any())).thenReturn("/uploads/stored_leaf.jpg");

        AiPredictionResult aiResult = new AiPredictionResult();
        aiResult.setDiseaseKey("unknown_stress_general");
        aiResult.setHealthStatus("STRESSED");
        aiResult.setSeverity("LOW");
        aiResult.setConfidence(0.42); // Below 0.55 threshold
        aiResult.setSuggestedCrop("General Plant");
        when(aiClientService.predict(any(), any(), any(), any(), any())).thenReturn(aiResult);

        Disease disease = new Disease();
        disease.setDiseaseKey("unknown_stress_general");
        disease.setDisplayName("Unidentified Stress");
        disease.setCategory(CategoryEnum.ABIOTIC);
        when(diseaseRepository.findByDiseaseKey("unknown_stress_general")).thenReturn(Optional.of(disease));

        Diagnosis savedDiagnosis = new Diagnosis();
        savedDiagnosis.setId(102L);
        savedDiagnosis.setImagePath("stored_leaf.jpg");
        savedDiagnosis.setConfidence(0.42);
        savedDiagnosis.setHealthStatus(HealthStatusEnum.STRESSED);
        savedDiagnosis.setSeverity(SeverityEnum.LOW);
        savedDiagnosis.setDisease(disease);

        when(diagnosisRepository.save(any(Diagnosis.class))).thenReturn(savedDiagnosis);

        DiagnosisResponse response = diagnosisService.diagnosePlant(
                file, null, "Open Field", null, null, null
        );

        assertNotNull(response);
        assertTrue(response.getIsLowConfidence());
        assertTrue(response.getExpertConsultationRecommended());
    }

    @Test
    @DisplayName("Should throw InvalidPlantImageException and avoid saving to DB when image is not a plant")
    void testRejectNonPlantImageThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "human_portrait.jpg", "image/jpeg", "image bytes".getBytes()
        );

        AiPredictionResult aiResult = new AiPredictionResult();
        aiResult.setIsPlant(false);
        aiResult.setRejectionReason("HUMAN_DETECTED");
        aiResult.setRejectionMessage("Human face detected. AgriGuard AI only accepts plant photos.");
        when(aiClientService.predict(any(), any(), any(), any(), any())).thenReturn(aiResult);

        com.agriguard.exception.InvalidPlantImageException ex = assertThrows(
                com.agriguard.exception.InvalidPlantImageException.class,
                () -> diagnosisService.diagnosePlant(file, "Tomato", "Open Field", "Vegetative", "Person photo", null)
        );

        assertEquals("HUMAN_DETECTED", ex.getRejectionReason());
        assertTrue(ex.getMessage().contains("Human face detected"));
        verify(diagnosisRepository, never()).save(any(Diagnosis.class));
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @DisplayName("Should successfully delete diagnosis record when ID exists")
    void testDeleteDiagnosis() {
        when(diagnosisRepository.existsById(50L)).thenReturn(true);
        doNothing().when(diagnosisRepository).deleteById(50L);

        boolean result = diagnosisService.deleteDiagnosis(50L);
        assertTrue(result);
        verify(diagnosisRepository, times(1)).deleteById(50L);
    }
}
