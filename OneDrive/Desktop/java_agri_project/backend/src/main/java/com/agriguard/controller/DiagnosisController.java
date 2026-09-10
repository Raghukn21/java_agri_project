package com.agriguard.controller;

import com.agriguard.dto.DiagnosisResponse;
import com.agriguard.exception.InvalidPlantImageException;
import com.agriguard.service.DiagnosisService;
import com.agriguard.util.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final JwtUtils jwtUtils;

    public DiagnosisController(DiagnosisService diagnosisService, JwtUtils jwtUtils) {
        this.diagnosisService = diagnosisService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping(value = "/diagnose", consumes = {"multipart/form-data"})
    public ResponseEntity<?> diagnosePlant(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "cropName", required = false) String cropName,
            @RequestParam(value = "environment", required = false) String environment,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "growthStage", required = false) String growthStage,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Plant photo image file is required."));
        }

        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                userId = jwtUtils.getUserIdFromToken(token);
            }
        }

        String env = (environment != null && !environment.isBlank()) ? environment : location;

        try {
            DiagnosisResponse response = diagnosisService.diagnosePlant(
                    image,
                    cropName,
                    env,
                    growthStage,
                    notes,
                    userId
            );
            return ResponseEntity.ok(response);
        } catch (InvalidPlantImageException e) {
            return ResponseEntity.status(422).body(Map.of(
                    "error", "INVALID_PLANT_IMAGE",
                    "message", e.getMessage(),
                    "rejectionReason", e.getRejectionReason() != null ? e.getRejectionReason() : "NON_PLANT_DETECTED",
                    "plantProbability", e.getPlantProbability() != null ? e.getPlantProbability() : 0.0,
                    "isPlant", false
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Diagnosis failed: " + e.getMessage()));
        }
    }

    @GetMapping("/diagnoses")
    public ResponseEntity<List<DiagnosisResponse>> getDiagnosisHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                userId = jwtUtils.getUserIdFromToken(token);
            }
        }

        List<DiagnosisResponse> history = diagnosisService.getHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/diagnoses/{id}")
    public ResponseEntity<?> getDiagnosisById(@PathVariable("id") Long id) {
        Optional<DiagnosisResponse> diag = diagnosisService.getDiagnosisById(id);
        if (diag.isPresent()) {
            return ResponseEntity.ok(diag.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Diagnosis record not found for id " + id));
    }

    @DeleteMapping("/diagnoses/{id}")
    public ResponseEntity<?> deleteDiagnosis(@PathVariable("id") Long id) {
        boolean deleted = diagnosisService.deleteDiagnosis(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Diagnosis record deleted successfully."));
        }
        return ResponseEntity.status(404).body(Map.of("error", "Diagnosis record not found."));
    }
}
