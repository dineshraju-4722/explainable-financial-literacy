package com.financialliteracy.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "assessment_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    @Column(name = "total_score")
    private Double totalScore;

    private Double percentage;

    @Column(name = "proficiency_level")
    private String proficiencyLevel;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "ml_archetype", length = 50)
    private com.financialliteracy.enums.MlArchetype mlArchetype;

    @Column(name = "ml_model_version", length = 50)
    private String mlModelVersion;

    @Column(name = "ml_predicted_at")
    private Instant mlPredictedAt;

    @Column(name = "ml_probabilities_json", columnDefinition = "TEXT")
    private String mlProbabilitiesJson;

    @PrePersist
    protected void onCreate() {
        if (this.calculatedAt == null) {
            this.calculatedAt = Instant.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssessmentResult that = (AssessmentResult) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
