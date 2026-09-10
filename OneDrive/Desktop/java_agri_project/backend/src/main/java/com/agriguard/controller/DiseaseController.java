package com.agriguard.controller;

import com.agriguard.dto.DiseaseDto;
import com.agriguard.entity.CategoryEnum;
import com.agriguard.service.DiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diseases")
@CrossOrigin(origins = "*")
public class DiseaseController {

    private final DiseaseService diseaseService;

    public DiseaseController(DiseaseService diseaseService) {
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public ResponseEntity<List<DiseaseDto>> getAllDiseases() {
        return ResponseEntity.ok(diseaseService.getAllDiseases());
    }

    @GetMapping("/{key}")
    public ResponseEntity<?> getDiseaseByKey(@PathVariable("key") String key) {
        Optional<DiseaseDto> disease = diseaseService.getByDiseaseKey(key);
        if (disease.isPresent()) {
            return ResponseEntity.ok(disease.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Disease not found for key " + key));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getByCategory(@PathVariable("category") String category) {
        try {
            CategoryEnum cat = CategoryEnum.valueOf(category.toUpperCase().trim());
            return ResponseEntity.ok(diseaseService.getByCategory(cat));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid category: " + category));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiseaseDto>> searchDiseases(@RequestParam("q") String query) {
        return ResponseEntity.ok(diseaseService.searchDiseases(query));
    }
}
