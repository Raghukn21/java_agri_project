package com.agriguard.repository;

import com.agriguard.entity.CategoryEnum;
import com.agriguard.entity.Disease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiseaseRepository extends JpaRepository<Disease, Long> {
    Optional<Disease> findByDiseaseKey(String diseaseKey);
    List<Disease> findByCropId(Long cropId);
    List<Disease> findByCategory(CategoryEnum category);
    List<Disease> findByIsGeneralTrue();

    @Query("SELECT d FROM Disease d WHERE LOWER(d.displayName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.symptoms) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Disease> searchDiseases(@Param("query") String query);
}
