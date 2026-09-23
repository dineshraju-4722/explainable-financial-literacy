-- V2__add_assessment_response_unique_constraint.sql
-- Enforce single response per question within an assessment attempt

ALTER TABLE assessment_responses
ADD CONSTRAINT uq_assessment_question UNIQUE (assessment_id, question_id);
