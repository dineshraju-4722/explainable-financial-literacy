-- V6__seed_demo_user.sql
-- Seed default demo user to support seamless frontend initial assessment initialization

INSERT INTO users (id, name, email, age, education_level, occupation) VALUES
('3fa85f64-5717-4562-b3fc-2c963f66afa6',
 'Demo Learner',
 'demo.learner@example.com',
 25,
 'Bachelor',
 'Student')
ON CONFLICT (id) DO NOTHING;
