package com.agriguard.service;

import com.agriguard.dto.CropDto;
import com.agriguard.entity.Crop;
import com.agriguard.repository.CropRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CropService {

    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    public List<CropDto> getAllCrops() {
        return cropRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<CropDto> getCropById(Long id) {
        return cropRepository.findById(id).map(this::mapToDto);
    }

    public Optional<CropDto> getCropByName(String name) {
        return cropRepository.findByNameIgnoreCase(name).map(this::mapToDto);
    }

    public List<CropDto> searchCrops(String query) {
        return cropRepository.searchCrops(query).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CropDto mapToDto(Crop crop) {
        CropDto dto = new CropDto();
        dto.setId(crop.getId());
        dto.setName(crop.getName());
        dto.setScientificName(crop.getScientificName());
        dto.setFamilyName(crop.getFamilyName());
        dto.setCommonDiseases(crop.getCommonDiseases());
        dto.setGeneralCare(crop.getGeneralCare());
        dto.setOptimalTemp(crop.getOptimalTemp());
        dto.setOptimalPh(crop.getOptimalPh());
        dto.setWateringTips(crop.getWateringTips());
        dto.setIconUrl(crop.getIconUrl());
        return dto;
    }
}
