package com.agriguard.controller;

import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;
import com.agriguard.repository.DiagnosisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
@AutoConfigureMockMvc(addFilters = false)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiagnosisRepository diagnosisRepository;

    @Test
    @DisplayName("GET /api/stats should calculate accurate scan aggregations and distributions")
    void testGetStats() throws Exception {
        when(diagnosisRepository.count()).thenReturn(10L);
        when(diagnosisRepository.countByHealthStatus(HealthStatusEnum.HEALTHY)).thenReturn(7L);
        when(diagnosisRepository.countByHealthStatus(HealthStatusEnum.DISEASED)).thenReturn(2L);
        when(diagnosisRepository.countByHealthStatus(HealthStatusEnum.STRESSED)).thenReturn(1L);

        when(diagnosisRepository.countBySeverity(SeverityEnum.LOW)).thenReturn(5L);
        when(diagnosisRepository.countBySeverity(SeverityEnum.MEDIUM)).thenReturn(3L);
        when(diagnosisRepository.countBySeverity(SeverityEnum.HIGH)).thenReturn(2L);

        List<Object[]> mockCropRows = List.of(
                new Object[]{"Tomato", 6L},
                new Object[]{"Potato", 4L}
        );
        when(diagnosisRepository.countByCropDistribution()).thenReturn(mockCropRows);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScans").value(10))
                .andExpect(jsonPath("$.healthyCount").value(7))
                .andExpect(jsonPath("$.diseasedCount").value(2))
                .andExpect(jsonPath("$.stressedCount").value(1))
                .andExpect(jsonPath("$.healthRatePercent").value(70.0))
                .andExpect(jsonPath("$.cropDistribution.Tomato").value(6))
                .andExpect(jsonPath("$.cropDistribution.Potato").value(4))
                .andExpect(jsonPath("$.severityDistribution.LOW").value(5))
                .andExpect(jsonPath("$.severityDistribution.HIGH").value(2));
    }
}
