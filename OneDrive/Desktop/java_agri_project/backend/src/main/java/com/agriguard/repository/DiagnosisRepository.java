package com.agriguard.repository;

import com.agriguard.entity.Diagnosis;
import com.agriguard.entity.HealthStatusEnum;
import com.agriguard.entity.SeverityEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findByUserIdOrderByDiagnosedAtDesc(Long userId);
    List<Diagnosis> findAllByOrderByDiagnosedAtDesc();
    Page<Diagnosis> findAllByOrderByDiagnosedAtDesc(Pageable pageable);
    
    long countByHealthStatus(HealthStatusEnum healthStatus);
    long countBySeverity(SeverityEnum severity);

    @Query("SELECT d.predictedCrop, COUNT(d) FROM Diagnosis d GROUP BY d.predictedCrop ORDER BY COUNT(d) DESC")
    List<Object[]> countByCropDistribution();

    @Query("SELECT d.healthStatus, COUNT(d) FROM Diagnosis d GROUP BY d.healthStatus")
    List<Object[]> countByHealthStatusDistribution();
}
