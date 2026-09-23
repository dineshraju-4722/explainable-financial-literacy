package com.financialliteracy.repository;

import com.financialliteracy.entity.Assessment;
import com.financialliteracy.enums.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
    List<Assessment> findByUserId(UUID userId);
    List<Assessment> findByUserIdAndStatus(UUID userId, AssessmentStatus status);
}
