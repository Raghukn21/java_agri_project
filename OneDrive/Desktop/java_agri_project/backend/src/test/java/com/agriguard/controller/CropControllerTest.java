package com.agriguard.controller;

import com.agriguard.dto.CropDto;
import com.agriguard.dto.DiseaseDto;
import com.agriguard.service.CropService;
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

@WebMvcTest(CropController.class)
@AutoConfigureMockMvc(addFilters = false)
class CropControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CropService cropService;

    @MockBean
    private DiseaseService diseaseService;

    @Test
    @DisplayName("GET /api/crops should return list of crops")
    void testGetAllCrops() throws Exception {
        CropDto tomato = new CropDto();
        tomato.setId(1L);
        tomato.setName("Tomato");
        tomato.setFamilyName("Solanaceae");

        CropDto potato = new CropDto();
        potato.setId(2L);
        potato.setName("Potato");
        potato.setFamilyName("Solanaceae");

        when(cropService.getAllCrops()).thenReturn(List.of(tomato, potato));

        mockMvc.perform(get("/api/crops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Tomato"))
                .andExpect(jsonPath("$[1].name").value("Potato"));
    }

    @Test
    @DisplayName("GET /api/crops/{id} should return crop when found")
    void testGetCropById() throws Exception {
        CropDto crop = new CropDto();
        crop.setId(1L);
        crop.setName("Tomato");
        crop.setFamilyName("Solanaceae");

        when(cropService.getCropById(1L)).thenReturn(Optional.of(crop));

        mockMvc.perform(get("/api/crops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tomato"))
                .andExpect(jsonPath("$.familyName").value("Solanaceae"));
    }

    @Test
    @DisplayName("GET /api/crops/{id}/diseases should return associated diseases")
    void testGetDiseasesForCrop() throws Exception {
        DiseaseDto d1 = new DiseaseDto();
        d1.setId(10L);
        d1.setDisplayName("Early Blight");
        d1.setDiseaseKey("tomato_early_blight");

        when(diseaseService.getByCropId(1L)).thenReturn(List.of(d1));

        mockMvc.perform(get("/api/crops/1/diseases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].diseaseKey").value("tomato_early_blight"));
    }
}
