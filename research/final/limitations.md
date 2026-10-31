# 9. Methodological & System Limitations

The following 10 methodological limitations are supported by empirical evidence and MUST be documented in paper and project presentations:

1. **Cross-Sectional Microdata Scope**: The FINRA NFCS is a cross-sectional survey dataset administered at a single point in time, limiting statistical modeling to observational association rather than longitudinal causal inference.
2. **Archetype Cutoff Sensitivity**: The four-class capability matrix depends on explicit threshold cutoffs ($M4 \ge 5$, $S_{\text{obj}} \ge 3$); sensitivity analysis against alternate cutoffs (e.g., $M4 \ge 6$) represents an avenue for future work.
3. **Survey Weight Exclusion**: Models were trained on unweighted survey microdata to avoid inflating minority sample variance; population-weighted estimates require weighted post-stratification.
4. **Sample vs Population Evaluation**: Performance metrics evaluate sample classification accuracy across held-out survey respondents, not population-adjusted estimates.
5. **Moderate Predictive Capacity**: Overall predictive performance (Accuracy $\sim$56.2%, Macro F1 $\sim$52.1%) reflects human behavioral variability and survey noise, rather than near-perfect classification.
6. **Minority Cohort Sensitivity**: Despite inverse-frequency class weighting, the minority `UNDERCONFIDENT` archetype F1 ($\sim$36.2%) remains lower than the majority `COMPETENT` archetype ($\sim$70.8%).
7. **Post-Hoc Feature Attribution**: TreeSHAP explanations describe decision-tree prediction logic; they do not establish causal relationships or therapeutic intervention outcomes.
8. **Deterministic Personalization Engine**: Application education recommendations follow expert-defined rule-based heuristics rather than longitudinal proof of improved financial behavior.
9. **Geographic & Temporal Scope**: The model is trained on US 2024 survey microdata and may not generalize automatically to non-US populations or future macro-economic conditions.
10. **Operational Assessment Instrument**: Operational web application questions are condensed pedagogical items designed for user engagement, distinct from the full 75-variable research survey instrument.
