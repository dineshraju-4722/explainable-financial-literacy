-- V5__seed_questions.sql
-- Initial seed data for assessment questions and options

-- Question 1: INTEREST
INSERT INTO questions (id, question_text, category, difficulty, question_type, active) VALUES
('a1b2c3d4-0000-4000-8000-000000000001',
 'Suppose you had $100 in a savings account and the interest rate was 2% per year. After 5 years, how much do you think you would have in the account if you left the money to grow?',
 'INTEREST',
 'BEGINNER',
 'MULTIPLE_CHOICE',
 TRUE);

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
('b1b2c3d4-0000-4000-8000-000000000101', 'a1b2c3d4-0000-4000-8000-000000000001', 'More than $102', 1, TRUE),
('b1b2c3d4-0000-4000-8000-000000000102', 'a1b2c3d4-0000-4000-8000-000000000001', 'Exactly $102', 2, FALSE),
('b1b2c3d4-0000-4000-8000-000000000103', 'a1b2c3d4-0000-4000-8000-000000000001', 'Less than $102', 3, FALSE),
('b1b2c3d4-0000-4000-8000-000000000104', 'a1b2c3d4-0000-4000-8000-000000000001', 'Don''t know', 4, FALSE);

-- Question 2: INFLATION
INSERT INTO questions (id, question_text, category, difficulty, question_type, active) VALUES
('a1b2c3d4-0000-4000-8000-000000000002',
 'Imagine that the interest rate on your savings account was 1% per year and inflation was 2% per year. After 1 year, how much would you be able to buy with the money in this account?',
 'INFLATION',
 'BEGINNER',
 'MULTIPLE_CHOICE',
 TRUE);

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
('b1b2c3d4-0000-4000-8000-000000000201', 'a1b2c3d4-0000-4000-8000-000000000002', 'More than today', 1, FALSE),
('b1b2c3d4-0000-4000-8000-000000000202', 'a1b2c3d4-0000-4000-8000-000000000002', 'Exactly the same', 2, FALSE),
('b1b2c3d4-0000-4000-8000-000000000203', 'a1b2c3d4-0000-4000-8000-000000000002', 'Less than today', 3, TRUE),
('b1b2c3d4-0000-4000-8000-000000000204', 'a1b2c3d4-0000-4000-8000-000000000002', 'Don''t know', 4, FALSE);

-- Question 3: RISK
INSERT INTO questions (id, question_text, category, difficulty, question_type, active) VALUES
('a1b2c3d4-0000-4000-8000-000000000003',
 'Buying a single company''s stock usually provides a safer return than a stock mutual fund.',
 'RISK',
 'BEGINNER',
 'TRUE_FALSE',
 TRUE);

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
('b1b2c3d4-0000-4000-8000-000000000301', 'a1b2c3d4-0000-4000-8000-000000000003', 'True', 1, FALSE),
('b1b2c3d4-0000-4000-8000-000000000302', 'a1b2c3d4-0000-4000-8000-000000000003', 'False', 2, TRUE);

-- Question 4: BUDGETING
INSERT INTO questions (id, question_text, category, difficulty, question_type, active) VALUES
('a1b2c3d4-0000-4000-8000-000000000004',
 'Which of the following best describes the popular 50/30/20 rule for monthly budgeting?',
 'BUDGETING',
 'BEGINNER',
 'MULTIPLE_CHOICE',
 TRUE);

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
('b1b2c3d4-0000-4000-8000-000000000401', 'a1b2c3d4-0000-4000-8000-000000000004', '50% Needs, 30% Wants, 20% Savings & Debt Repayment', 1, TRUE),
('b1b2c3d4-0000-4000-8000-000000000402', 'a1b2c3d4-0000-4000-8000-000000000004', '50% Savings, 30% Needs, 20% Wants', 2, FALSE),
('b1b2c3d4-0000-4000-8000-000000000403', 'a1b2c3d4-0000-4000-8000-000000000004', '50% Debt, 30% Savings, 20% Needs', 3, FALSE),
('b1b2c3d4-0000-4000-8000-000000000404', 'a1b2c3d4-0000-4000-8000-000000000004', '50% Investments, 30% Taxes, 20% Living Expenses', 4, FALSE);

-- Question 5: DEBT_MANAGEMENT
INSERT INTO questions (id, question_text, category, difficulty, question_type, active) VALUES
('a1b2c3d4-0000-4000-8000-000000000005',
 'If you carry a revolving balance on a credit card charging 20% APR while maintaining extra money in a regular savings account earning 1% annual interest, what is the net financial impact?',
 'DEBT_MANAGEMENT',
 'INTERMEDIATE',
 'MULTIPLE_CHOICE',
 TRUE);

INSERT INTO question_options (id, question_id, option_text, option_order, is_correct) VALUES
('b1b2c3d4-0000-4000-8000-000000000501', 'a1b2c3d4-0000-4000-8000-000000000005', 'You lose net wealth because credit interest costs far exceed savings earnings', 1, TRUE),
('b1b2c3d4-0000-4000-8000-000000000502', 'a1b2c3d4-0000-4000-8000-000000000005', 'You gain net wealth because savings interest offsets debt', 2, FALSE),
('b1b2c3d4-0000-4000-8000-000000000503', 'a1b2c3d4-0000-4000-8000-000000000005', 'There is no overall net effect on your financial standing', 3, FALSE),
('b1b2c3d4-0000-4000-8000-000000000504', 'a1b2c3d4-0000-4000-8000-000000000005', 'Don''t know', 4, FALSE);
