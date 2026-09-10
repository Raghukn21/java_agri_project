package com.agriguard.service;

import com.agriguard.dto.DiseaseDto;
import com.agriguard.entity.CategoryEnum;
import com.agriguard.entity.Disease;
import com.agriguard.repository.DiseaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public DiseaseService(DiseaseRepository diseaseRepository) {
        this.diseaseRepository = diseaseRepository;
    }

    public List<DiseaseDto> getAllDiseases() {
        return diseaseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<DiseaseDto> getByDiseaseKey(String diseaseKey) {
        return diseaseRepository.findByDiseaseKey(diseaseKey).map(this::mapToDto);
    }

    public List<DiseaseDto> getByCropId(Long cropId) {
        return diseaseRepository.findByCropId(cropId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DiseaseDto> getByCategory(CategoryEnum category) {
        return diseaseRepository.findByCategory(category).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DiseaseDto> searchDiseases(String query) {
        return diseaseRepository.searchDiseases(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public DiseaseDto mapToDto(Disease disease) {
        DiseaseDto dto = new DiseaseDto();
        dto.setId(disease.getId());
        dto.setDiseaseKey(disease.getDiseaseKey());
        dto.setDisplayName(disease.getDisplayName());
        if (disease.getCrop() != null) {
            dto.setCropId(disease.getCrop().getId());
            dto.setCropName(disease.getCrop().getName());
        }
        dto.setCategory(disease.getCategory());
        dto.setDescription(disease.getDescription());
        dto.setSymptoms(disease.getSymptoms());
        dto.setTreatment(disease.getTreatment());
        dto.setPrevention(disease.getPrevention());
        dto.setSeverityGuidance(disease.getSeverityGuidance());
        dto.setOrganicControl(disease.getOrganicControl());
        dto.setChemicalControl(disease.getChemicalControl());
        dto.setIsGeneral(disease.getIsGeneral());
        return dto;
    }
}
