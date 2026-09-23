package com.financialliteracy.repository;

import com.financialliteracy.entity.Question;
import com.financialliteracy.enums.FinancialLiteracyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByActiveTrue();
    List<Question> findByCategoryAndActiveTrue(FinancialLiteracyCategory category);
}
