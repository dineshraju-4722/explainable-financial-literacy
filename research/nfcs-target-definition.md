# NFCS 2024 Target Variable Definition & Archetype Methodology

## 1. Research Project & Target Overview

This document specifies the target variable definition and mathematical formulation for the research project:

> **"Explainable Machine Learning-Based Financial Literacy Assessment and Personalized Financial Education System"**

The objective of the machine learning model is to predict a participant's financial capability archetype. Rather than relying solely on objective test scores (which creates a trivial identity mapping), the research target operationalizes the **Subjective-Objective Financial Knowledge Matrix**, constructing a four-class financial-literacy archetype based on the interaction between:
1. **Objective Financial Literacy** (5-question quiz score from NFCS 2024)
2. **Subjective Financial Knowledge** (Self-assessed financial confidence rating from NFCS 2024)

---

## 2. NFCS Variables Used for Target Construction

The target is constructed exclusively using six historical microdata variables from the FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**:

| Variable | Description | Valid Response Range | Non-Substantive Codes | Target Role |
| :--- | :--- | :--- | :--- | :--- |
| `M4` | Self-assessed overall financial knowledge rating | `1` (Very Low) – `7` (Very High) | `98` (Don't know), `99` (Prefer not to say) | Subjective Knowledge Measure |
| `M6` | Interest rate calculation ($100 at 2% over 5 years) | `1` (>$102), `2` (=$102), `3` (<$102) | `98` (Don't know), `99` (Prefer not to say) | Objective Quiz Item 1 |
| `M7` | Inflation purchasing power (1% interest vs 2% inflation) | `1` (More), `2` (Same), `3` (Less) | `98` (Don't know), `99` (Prefer not to say) | Objective Quiz Item 2 |
| `M8` | Bond price and interest rate inverse relationship | `1` (Rise), `2` (Fall), `3` (Same), `4` (No relationship) | `98` (Don't know), `99` (Prefer not to say) | Objective Quiz Item 3 |
| `M9` | Mortgage term interest comparison (15-yr vs 30-yr) | `1` (True), `2` (False) | `98` (Don't know), `99` (Prefer not to say) | Objective Quiz Item 4 |
| `M10` | Single stock vs stock mutual fund diversification | `1` (True), `2` (False) | `98` (Don't know), `99` (Prefer not to say) | Objective Quiz Item 5 |

---

## 3. Objective Financial Literacy Score Calculation

The objective financial literacy score ($S_{	ext{obj}}$) is defined as the unweighted sum of correct responses across the five core NFCS financial literacy questions ($M6, M7, M8, M9, M10$):

$$S_{	ext{obj}} = \sum_{k \in \{6,7,8,9,10\}} \mathbb{I}(M_k = 	ext{correct}_k)$$

Where the indicator function $\mathbb{I}(\cdot)$ evaluates to $1$ for the exact official correct code and $0$ otherwise (including non-substantive responses `98` and `99`):

- $	ext{correct}_6 = 1$ (More than \$102)
- $	ext{correct}_7 = 3$ (Less than today)
- $	ext{correct}_8 = 2$ (They will fall)
- $	ext{correct}_9 = 1$ (True)
- $	ext{correct}_{10} = 2$ (False)

### Objective Score Range & Candidate Thresholds

- **Score Range**: $S_{	ext{obj}} \in \{0, 1, 2, 3, 4, 5\}$
- **Threshold Cutoff**:
  - $	ext{LOW Objective Literacy} \iff S_{	ext{obj}} \in \{0, 1, 2\}$
  - $	ext{HIGH Objective Literacy} \iff S_{	ext{obj}} \in \{3, 4, 5\}$

---

## 4. Subjective Financial Knowledge Rating & Threshold

The subjective financial knowledge measure ($S_{	ext{subj}}$) is extracted directly from question `M4`:

> *"On a scale from 1 to 7, where 1 means very low and 7 means very high, how would you assess your overall financial knowledge?"*

### Subjective Score Range & Candidate Thresholds

- **Valid Scale**: $S_{	ext{subj}} \in \{1, 2, 3, 4, 5, 6, 7\}$
- **Threshold Cutoff**:
  - $	ext{LOW Subjective Knowledge} \iff S_{	ext{subj}} \in \{1, 2, 3, 4\}$
  - $	ext{HIGH Subjective Knowledge} \iff S_{	ext{subj}} \in \{5, 6, 7\}$

---

## 5. Four-Class Target Matrix & Archetype Definitions

Combining the binary objective and subjective thresholds yields a $2 	imes 2$ matrix resulting in four distinct financial capability archetypes:

| | **LOW Subjective** ($M4 \le 4$) | **HIGH Subjective** ($M4 \ge 5$) |
| :--- | :--- | :--- |
| **HIGH Objective** ($S_{	ext{obj}} \ge 3$) | **UNDERCONFIDENT** | **COMPETENT** |
| **LOW Objective** ($S_{	ext{obj}} \le 2$) | **NAIVE** | **OVERCONFIDENT** |

> [!IMPORTANT]
> The target class names MUST be exactly as specified above:
> - `COMPETENT`: High objective knowledge ($S_{	ext{obj}} \ge 3$), high self-confidence ($M4 \ge 5$).
> - `OVERCONFIDENT`: Low objective knowledge ($S_{	ext{obj}} \le 2$), high self-confidence ($M4 \ge 5$). *Presents highest behavioral risk of costly financial mistakes.*
> - `UNDERCONFIDENT`: High objective knowledge ($S_{	ext{obj}} \ge 3$), low self-confidence ($M4 \le 4$). *Presents risk of financial hesitation or under-investment.*
> - `NAIVE`: Low objective knowledge ($S_{	ext{obj}} \le 2$), low self-confidence ($M4 \le 4$). *Presents basic foundational educational needs.*

---

## 6. Dataset Target Distribution Verification

The target calculation was verified directly against the raw `NFCS 2024 State Data 250623.csv` microdata file ($N_{	ext{raw}} = 25,539$).

### Filter & Exclusion Log
1. Raw survey respondents: **25,539**
2. Non-substantive `M4` responses (`98` = 403, `99` = 106): **509 excluded (1.99%)**
3. Missing objective quiz items: **0**
4. **Valid Target Sample Size ($N_{	ext{valid}}$)**: **25,030**

### Empirical Target Distribution

| Target Class | Encoded Label | Record Count ($N$) | Percentage (%) | Research Priority |
| :--- | :--- | :---: | :---: | :--- |
| **COMPETENT** | `0` / `COMPETENT` | 10,953 | 43.76% | Majority baseline class |
| **OVERCONFIDENT** | `1` / `OVERCONFIDENT` | 5,716 | 22.84% | Primary high-risk target |
| **NAIVE** | `2` / `NAIVE` | 4,605 | 18.40% | Foundational intervention target |
| **UNDERCONFIDENT** | `3` / `UNDERCONFIDENT` | 3,756 | 15.01% | Efficacy-building intervention target |
| **TOTAL VALID** | - | **25,030** | **100.00%** | Balanced 4-class distribution |

*Note: The empirically calculated record counts and percentages match the theoretical candidate distribution exactly.*

---

## 7. Methodological Assumptions & Literature Alignment

| Aspect | Status | Details & Justification |
| :--- | :--- | :--- |
| **5-Question Quiz Standard** | **Verified Fact** | Standard 5-item Lusardi & Mitchell financial literacy measure implemented in NFCS 2009–2024. |
| **Subjective Scale Cutoff ($\ge 5$)** | **Project Decision** | $5-7$ represents above-average confidence on a 7-point Likert scale. *REQUIRES RESEARCH JUSTIFICATION* in final paper comparing sensitivity against $M4 \ge 6$. |
| **Objective Cutoff ($\ge 3$)** | **Verified Fact** | Standard FINRA threshold for "high" financial literacy (passing 3+ out of 5 questions). |
| **Archetype Classification** | **Literature Derived** | Extension of capability mismatch framework adapted from Zhu (2025) and Lusardi & Mitchell (2014). |
