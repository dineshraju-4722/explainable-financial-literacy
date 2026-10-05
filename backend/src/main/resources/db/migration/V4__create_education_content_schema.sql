-- V4__create_education_content_schema.sql
-- Flyway migration establishing the curated educational content schema and initial reference content

CREATE TABLE educational_contents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    topic VARCHAR(50) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    short_description TEXT NOT NULL,
    learning_objectives TEXT NOT NULL,
    content TEXT NOT NULL,
    estimated_duration_minutes INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_educational_contents_topic ON educational_contents(topic);
CREATE INDEX idx_educational_contents_difficulty ON educational_contents(difficulty);

-- Initial Curated Educational Content Seeding (Deterministic UUIDs)

-- 1. BUDGETING
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000101',
 'Budgeting Fundamentals',
 'BUDGETING',
 'BEGINNER',
 'Learn the core principles of creating and maintaining a personal budget to track income and expenses.',
 'Understand net income, fixed vs. variable expenses, and the basics of monthly cash flow management.',
 'A budget is a quantitative plan for tracking financial income and expenditure over a specified timeframe. Effective budgeting ensures that cash inflows exceed outflows, establishing a positive net cash flow that can be directed toward savings or debt reduction.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000102',
 'Needs vs Wants and Cash Flow Management',
 'BUDGETING',
 'INTERMEDIATE',
 'Differentiate essential expenses from discretionary spending to optimize spending efficiency.',
 'Categorize spending into essential needs and discretionary wants, and apply rule-based budgeting techniques.',
 'Distinguishing between essential needs (housing, utilities, basic food) and discretionary wants (entertainment, luxury items) is fundamental to financial discipline. Methods such as the 50/30/20 rule allocate income deterministically across needs, wants, and savings goals.',
 15,
 TRUE);

-- 2. SAVING
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000201',
 'Emergency Savings Fundamentals',
 'SAVING',
 'BEGINNER',
 'Understand the purpose, sizing, and structure of an emergency reserve fund.',
 'Calculate necessary emergency reserve size and evaluate liquid savings options.',
 'An emergency fund provides liquid financial reserves to buffer against unforeseen income disruption or unexpected expense shocks. Financial best practices recommend maintaining 3 to 6 months of basic living expenses in highly liquid, capital-preserved accounts.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000202',
 'Saving Strategies and Goal Setting',
 'SAVING',
 'INTERMEDIATE',
 'Structure short-term, medium-term, and long-term saving goals with automated contributions.',
 'Design structured saving milestones and utilize pay-yourself-first strategies.',
 'Automated saving strategies systematically transfer funds into designated savings vehicles prior to discretionary spending. Aligning specific savings targets with designated time horizons optimizes capital allocation between short-term liquidity and long-term yield.',
 15,
 TRUE);

-- 3. CREDIT
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000301',
 'Credit Fundamentals and Scores',
 'CREDIT',
 'BEGINNER',
 'Master the fundamentals of credit reports, credit scores, and borrowing history.',
 'Identify key factors influencing credit scores including payment history and utilization.',
 'Credit scores quantify borrowing risk based on historical credit behavior. Key determinants include payment history, credit utilization ratio, length of credit history, types of credit used, and recent credit inquiries.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000302',
 'Responsible Credit Card Use and Utilization',
 'CREDIT',
 'INTERMEDIATE',
 'Manage credit card balances to avoid high-interest charges and maintain low credit utilization.',
 'Calculate credit utilization rates and maintain optimal balance-to-limit ratios.',
 'Maintaining a low credit utilization ratio (typically below 30% of aggregate available credit) and paying credit card statement balances in full each billing cycle prevents compounding revolving interest costs and preserves credit standing.',
 20,
 TRUE);

-- 4. DEBT_MANAGEMENT
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000401',
 'Understanding Debt and Interest Costs',
 'DEBT_MANAGEMENT',
 'BEGINNER',
 'Understand principal, annual percentage rate (APR), and the true cost of debt carrying balances.',
 'Differentiate between simple interest and APR, and evaluate debt obligations.',
 'Carrying debt balances incurs interest costs determined by the Annual Percentage Rate (APR). Understanding how daily or monthly interest accrues on outstanding principal is essential for evaluating the total lifetime cost of borrowing.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000402',
 'Strategic Debt Repayment Methods',
 'DEBT_MANAGEMENT',
 'INTERMEDIATE',
 'Compare debt avalanche and debt snowball strategies to accelerate liability elimination.',
 'Analyze financial efficiency of interest-prioritized vs. balance-prioritized debt payoff plans.',
 'Structured debt acceleration strategies prioritize extra principal payments. The debt avalanche method targets liabilities with the highest interest rates first to minimize total interest paid, while the debt snowball method targets smallest balances first to build behavioral momentum.',
 20,
 TRUE);

-- 5. INVESTING
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000501',
 'Investing Fundamentals and Asset Classes',
 'INVESTING',
 'BEGINNER',
 'Explore core concepts of investing, asset classes (stocks, bonds, cash equivalents), and long-term growth.',
 'Identify primary asset classes and understand the fundamental difference between saving and investing.',
 'Investing involves deploying capital into asset classes with the expectation of generating future returns or capital appreciation. Primary asset classes include equities (stocks), fixed income (bonds), real estate, and cash equivalents, each offering distinct return profiles and liquidity characteristics.',
 20,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000502',
 'Risk, Return, and Diversification',
 'INVESTING',
 'INTERMEDIATE',
 'Understand the relationship between market risk and potential return, and apply asset diversification.',
 'Analyze portfolio diversification principles and systematic vs. unsystematic risk.',
 'Diversification spreads investment capital across uncorrelated asset classes to reduce portfolio volatility and mitigate unsystematic risk. Modern portfolio theory demonstrates that diversification optimizes expected return for a given level of market risk.',
 20,
 TRUE);

-- 6. INSURANCE
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000601',
 'Insurance Fundamentals and Risk Protection',
 'INSURANCE',
 'BEGINNER',
 'Understand risk transfer, premiums, deductibles, and essential insurance coverages.',
 'Evaluate health, auto, property, and life insurance principles as risk management mechanisms.',
 'Insurance serves as a risk management mechanism that transfers potential catastrophic financial loss from an individual to an insurer in exchange for regular premium payments. Evaluating deductibles, policy limits, and coverage scopes ensures adequate financial protection.',
 15,
 TRUE);

-- 7. FINANCIAL_PLANNING
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000701',
 'Personal Financial Goal Setting',
 'FINANCIAL_PLANNING',
 'BEGINNER',
 'Define specific, measurable, achievable, relevant, and time-bound (SMART) financial goals.',
 'Construct structured short-term and long-term financial milestones.',
 'Comprehensive financial planning begins with establishing quantified goals across multiple time horizons. Structuring goals with explicit monetary targets and completion dates enables systematic tracking and strategy adjustments over time.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000702',
 'Holistic Long-Term Financial Planning',
 'FINANCIAL_PLANNING',
 'INTERMEDIATE',
 'Integrate budgeting, debt control, insurance, and retirement planning into a cohesive strategy.',
 'Synthesize multiple financial domains into a unified personal financial plan.',
 'Holistic financial planning synthesizes cash flow management, risk protection, debt control, tax awareness, and retirement wealth accumulation into an integrated roadmap, ensuring long-term financial resilience across changing life stages.',
 20,
 TRUE);

-- 8. INTEREST
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000801',
 'Simple and Compound Interest Fundamentals',
 'INTEREST',
 'BEGINNER',
 'Master the mathematical principles of simple interest versus exponential compound growth.',
 'Calculate interest growth over time and evaluate compounding frequencies.',
 'Simple interest calculates returns exclusively on the original principal, whereas compound interest calculates returns on both principal and accumulated interest. Over extended time horizons, compounding generates non-linear, exponential growth.',
 15,
 TRUE),
('d7b23114-1111-4a2b-8888-000000000802',
 'The Time Value of Money',
 'INTEREST',
 'INTERMEDIATE',
 'Understand present value, future value, and how interest rates affect financial decisions.',
 'Apply present value and future value concepts to evaluate cash flows.',
 'The time value of money establishes that a unit of currency today is worth more than the same unit in the future due to its potential earning capacity via interest. Discounting and compounding formulas enable objective comparison of cash flows occurring at different times.',
 15,
 TRUE);

-- 9. INFLATION
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000000901',
 'Understanding Inflation and Purchasing Power',
 'INFLATION',
 'BEGINNER',
 'Learn how price inflation erodes purchasing power over time and affects savings growth.',
 'Distinguish real rate of return from nominal rate of return.',
 'Inflation represents the rate at which the general level of prices for goods and services rises, eroding currency purchasing power over time. To maintain real wealth, investment returns must exceed the rate of inflation (Real Return = Nominal Return - Inflation Rate).',
 15,
 TRUE);

-- 10. RISK
INSERT INTO educational_contents (id, title, topic, difficulty, short_description, learning_objectives, content, estimated_duration_minutes, active) VALUES
('d7b23114-1111-4a2b-8888-000000001001',
 'Risk Awareness and Financial Volatility',
 'RISK',
 'BEGINNER',
 'Understand financial market risk, volatility, trade-offs, and personal risk tolerance.',
 'Identify types of financial risk (market, inflation, liquidity) and assess risk tolerance.',
 'Financial risk measures the probability or uncertainty of incurring financial loss or experiencing return variability. Evaluating personal risk tolerance, time horizon, and risk capacity guides appropriate asset allocation decisions.',
 15,
 TRUE);
