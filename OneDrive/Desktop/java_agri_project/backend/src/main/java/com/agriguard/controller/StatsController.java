package com.agriguard.controller;

import com.agriguard.dto.StatsResponse;
import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;
import com.agriguard.repository.DiagnosisRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class StatsController {

    private final DiagnosisRepository diagnosisRepository;

    public StatsController(DiagnosisRepository diagnosisRepository) {
        this.diagnosisRepository = diagnosisRepository;
    }

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        StatsResponse stats = new StatsResponse();

        long total = diagnosisRepository.count();
        long healthy = diagnosisRepository.countByHealthStatus(HealthStatusEnum.HEALTHY);
        long diseased = diagnosisRepository.countByHealthStatus(HealthStatusEnum.DISEASED);
        long stressed = diagnosisRepository.countByHealthStatus(HealthStatusEnum.STRESSED);

        stats.setTotalScans(total);
        stats.setHealthyCount(healthy);
        stats.setDiseasedCount(diseased);
        stats.setStressedCount(stressed);

        double rate = total > 0 ? (healthy * 100.0 / total) : 85.0;
        stats.setHealthRatePercent(Math.round(rate * 10.0) / 10.0);

        Map<String, Long> cropDist = new HashMap<>();
        List<Object[]> cropRows = diagnosisRepository.countByCropDistribution();
        for (Object[] row : cropRows) {
            String crop = (row[0] != null) ? row[0].toString() : "Unknown";
            Long count = (Long) row[1];
            cropDist.put(crop, count);
        }
        stats.setCropDistribution(cropDist);

        Map<String, Long> sevDist = new HashMap<>();
        sevDist.put("LOW", diagnosisRepository.countBySeverity(SeverityEnum.LOW));
        sevDist.put("MEDIUM", diagnosisRepository.countBySeverity(SeverityEnum.MEDIUM));
        sevDist.put("HIGH", diagnosisRepository.countBySeverity(SeverityEnum.HIGH));
        stats.setSeverityDistribution(sevDist);

        return ResponseEntity.ok(stats);
    }
}
