package com.financialliteracy.repository;

import com.financialliteracy.entity.EducationalContent;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EducationalContentRepository extends JpaRepository<EducationalContent, UUID> {
    List<EducationalContent> findByTopicAndActiveTrue(FinancialLiteracyCategory topic);
    List<EducationalContent> findByActiveTrue();
}
