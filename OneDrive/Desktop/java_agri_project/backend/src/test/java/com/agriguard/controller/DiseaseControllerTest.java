package com.agriguard.controller;

import com.agriguard.dto.DiseaseDto;
import com.agriguard.entity.CategoryEnum;
import com.agriguard.service.DiseaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiseaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class DiseaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DiseaseService diseaseService;

    @Test
    @DisplayName("GET /api/diseases should return all disease entries")
    void testGetAllDiseases() throws Exception {
        DiseaseDto d1 = new DiseaseDto();
        d1.setDiseaseKey("tomato_early_blight");
        d1.setDisplayName("Early Blight");
        d1.setCategory(CategoryEnum.FUNGAL);

        when(diseaseService.getAllDiseases()).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/diseases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diseaseKey").value("tomato_early_blight"));
    }

    @Test
    @DisplayName("GET /api/diseases/{key} should return specific disease details")
    void testGetDiseaseByKey() throws Exception {
        DiseaseDto d = new DiseaseDto();
        d.setDiseaseKey("nitrogen_deficiency_general");
        d.setDisplayName("Nitrogen Deficiency (Abiotic)");
        d.setCategory(CategoryEnum.ABIOTIC);

        when(diseaseService.getByDiseaseKey("nitrogen_deficiency_general")).thenReturn(Optional.of(d));

        mockMvc.perform(get("/api/diseases/nitrogen_deficiency_general"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diseaseKey").value("nitrogen_deficiency_general"))
                .andExpect(jsonPath("$.displayName").value("Nitrogen Deficiency (Abiotic)"))
                .andExpect(jsonPath("$.category").value("ABIOTIC"));
    }

    @Test
    @DisplayName("GET /api/diseases/category/FUNGAL should filter fungal diseases")
    void testGetByCategory() throws Exception {
        DiseaseDto d = new DiseaseDto();
        d.setDiseaseKey("apple_scab");
        d.setDisplayName("Apple Scab");
        d.setCategory(CategoryEnum.FUNGAL);

        when(diseaseService.getByCategory(CategoryEnum.FUNGAL)).thenReturn(List.of(d));

        mockMvc.perform(get("/api/diseases/category/FUNGAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diseaseKey").value("apple_scab"));
    }
}
