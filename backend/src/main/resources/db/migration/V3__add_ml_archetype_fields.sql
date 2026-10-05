-- V3__add_ml_archetype_fields.sql
-- Migration adding ML Archetype prediction fields to assessment_results table

ALTER TABLE assessment_results ADD COLUMN ml_archetype VARCHAR(50);
ALTER TABLE assessment_results ADD COLUMN ml_model_version VARCHAR(50);
ALTER TABLE assessment_results ADD COLUMN ml_predicted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE assessment_results ADD COLUMN ml_probabilities_json TEXT;
