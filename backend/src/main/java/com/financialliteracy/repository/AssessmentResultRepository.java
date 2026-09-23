package com.financialliteracy.repository;

import com.financialliteracy.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, UUID> {
    Optional<AssessmentResult> findByAssessmentId(UUID assessmentId);
}
