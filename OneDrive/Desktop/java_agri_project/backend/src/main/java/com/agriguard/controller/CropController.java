package com.agriguard.controller;

import com.agriguard.dto.CropDto;
import com.agriguard.dto.DiseaseDto;
import com.agriguard.service.CropService;
import com.agriguard.service.DiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/crops")
@CrossOrigin(origins = "*")
public class CropController {

    private final CropService cropService;
    private final DiseaseService diseaseService;

    public CropController(CropService cropService, DiseaseService diseaseService) {
        this.cropService = cropService;
        this.diseaseService = diseaseService;
    }

    @GetMapping
    public ResponseEntity<List<CropDto>> getAllCrops() {
        return ResponseEntity.ok(cropService.getAllCrops());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCropById(@PathVariable("id") Long id) {
        Optional<CropDto> crop = cropService.getCropById(id);
        if (crop.isPresent()) {
            return ResponseEntity.ok(crop.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Crop not found for id " + id));
    }

    @GetMapping("/{id}/diseases")
    public ResponseEntity<List<DiseaseDto>> getDiseasesForCrop(@PathVariable("id") Long id) {
        List<DiseaseDto> diseases = diseaseService.getByCropId(id);
        return ResponseEntity.ok(diseases);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CropDto>> searchCrops(@RequestParam("q") String query) {
        return ResponseEntity.ok(cropService.searchCrops(query));
    }
}
