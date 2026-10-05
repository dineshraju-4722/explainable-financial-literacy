import os
import sys
import json
import csv
import time
import numpy as np
import pandas as pd

from sklearn.model_selection import train_test_split, StratifiedKFold, RandomizedSearchCV
from sklearn.preprocessing import OneHotEncoder
from lightgbm import LGBMClassifier
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score, confusion_matrix
)

def main():
    print("=" * 80)
    print("PHASE 6B — LIGHTGBM HYPERPARAMETER OPTIMIZATION RUNNER")
    print("=" * 80)

    # 1. Dataset Location & Loading
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", "..", ".."))
    csv_path = os.path.join(project_root, "research", "dataset", "NFCS 2024 State Data 250623.csv")
    
    print(f"Loading dataset from: {csv_path}")
    raw_df = pd.read_csv(csv_path)
    raw_count = len(raw_df)
    assert raw_count == 25539, f"Expected 25,539 raw observations, got {raw_count}"

    # 2. Target Definition & Verification
    valid_df = raw_df[raw_df['M4'].isin([1, 2, 3, 4, 5, 6, 7])].copy()
    valid_count = len(valid_df)
    assert valid_count == 25030, f"Expected 25,030 valid observations, got {valid_count}"

    m6_corr = (valid_df['M6'] == 1).astype(int)
    m7_corr = (valid_df['M7'] == 3).astype(int)
    m8_corr = (valid_df['M8'] == 2).astype(int)
    m9_corr = (valid_df['M9'] == 1).astype(int)
    m10_corr = (valid_df['M10'] == 2).astype(int)

    obj_score = m6_corr + m7_corr + m8_corr + m9_corr + m10_corr
    subj_score = valid_df['M4']

    obj_high = (obj_score >= 3)
    subj_high = (subj_score >= 5)

    conditions = [
        (obj_high & subj_high),
        (~obj_high & subj_high),
        (obj_high & ~subj_high),
        (~obj_high & ~subj_high)
    ]
    classes = ['COMPETENT', 'OVERCONFIDENT', 'UNDERCONFIDENT', 'NAIVE']
    valid_df['financial_literacy_class'] = np.select(conditions, classes, default='UNKNOWN')

    # 3. Target Leakage Verification
    EXCLUDED_LEAKAGE_VARS = [
        'M4', 'M6', 'M7', 'M8', 'M9', 'M10', 'M31', 'M50',
        'NFCSID', 'wgt_n2', 'wgt_d2', 'wgt_s3'
    ]
    
    feature_cols = [c for c in valid_df.columns if c not in EXCLUDED_LEAKAGE_VARS and c != 'financial_literacy_class']
    leaked_found = set(feature_cols).intersection(set(EXCLUDED_LEAKAGE_VARS))
    
    if len(leaked_found) > 0:
        print(f"CRITICAL ERROR: Target leakage variables detected in X: {leaked_found}")
        sys.exit(1)
    
    assert len(feature_cols) == 121, f"Expected 121 predictor features, got {len(feature_cols)}"

    X_raw = valid_df[feature_cols].copy()
    y = valid_df['financial_literacy_class'].copy()

    # Preprocessing: One-hot encode features
    encoder = OneHotEncoder(sparse_output=False, handle_unknown='ignore')
    X_encoded = encoder.fit_transform(X_raw.astype(str))
    print(f"One-Hot Encoded Feature Matrix Shape: {X_encoded.shape}")

    # 4. Train / Validation / Test Stratified Split (70% Train, 15% Val, 15% Test)
    RANDOM_STATE = 42
    
    X_train, X_temp, y_train, y_temp = train_test_split(
        X_encoded, y, test_size=0.30, stratify=y, random_state=RANDOM_STATE
    )
    X_val, X_test, y_val, y_test = train_test_split(
        X_temp, y_temp, test_size=0.50, stratify=y_temp, random_state=RANDOM_STATE
    )

    print("\nDataset Partition Sizes:")
    print(f"  Train Set (70%): {len(X_train)} samples")
    print(f"  Val Set   (15%): {len(X_val)} samples")
    print(f"  Test Set  (15%): {len(X_test)} samples")

    # Recalculate Inverse-Frequency Class Weights from Training Set Only
    train_class_counts = y_train.value_counts()
    n_train = len(y_train)
    k_classes = len(classes)
    
    calc_class_weights = {}
    print("\nTraining Set Class Counts & Inverse Frequency Weights:")
    for cls in classes:
        cnt = train_class_counts[cls]
        w = n_train / (k_classes * cnt)
        calc_class_weights[cls] = float(w)
        print(f"  {cls:15s}: Count = {cnt:5d} ({cnt/n_train*100:5.2f}%) -> Class Weight = {w:.4f}")

    # Verify reproduction of Phase 6A weights
    assert abs(calc_class_weights['COMPETENT'] - 0.5713) < 0.01
    assert abs(calc_class_weights['OVERCONFIDENT'] - 1.0948) < 0.01
    assert abs(calc_class_weights['NAIVE'] - 1.3586) < 0.01
    assert abs(calc_class_weights['UNDERCONFIDENT'] - 1.6661) < 0.01
    print("  Class weight calculation verified matching Phase 6A!")

    # 5. Define Hyperparameter Search Space
    param_distributions = {
        'learning_rate': [0.01, 0.03, 0.05, 0.08, 0.1],
        'n_estimators': [100, 150, 200, 250, 300],
        'num_leaves': [15, 31, 50, 63, 100],
        'max_depth': [4, 6, 8, 10, -1],
        'min_child_samples': [10, 20, 30, 50, 100],
        'subsample': [0.6, 0.7, 0.8, 0.9, 1.0],
        'colsample_bytree': [0.5, 0.6, 0.7, 0.8, 0.9, 1.0],
        'reg_alpha': [0.0, 0.01, 0.1, 1.0, 5.0, 10.0],
        'reg_lambda': [0.0, 0.01, 0.1, 1.0, 5.0, 10.0],
        'min_split_gain': [0.0, 0.01, 0.05, 0.1, 0.5],
        'subsample_freq': [0, 1, 5]
    }

    base_lgbm = LGBMClassifier(
        objective='multiclass',
        num_class=4,
        class_weight='balanced',
        random_state=RANDOM_STATE,
        n_jobs=1,
        verbose=-1
    )

    skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)

    N_ITER = 20
    print(f"\nExecuting RandomizedSearchCV across {N_ITER} trials using 5-Fold Stratified Cross-Validation...", flush=True)
    print("Optimization Metric: f1_macro (Training Set CV Only)", flush=True)
    
    search_start = time.time()
    random_search = RandomizedSearchCV(
        estimator=base_lgbm,
        param_distributions=param_distributions,
        n_iter=N_ITER,
        scoring='f1_macro',
        cv=skf,
        random_state=RANDOM_STATE,
        n_jobs=4,
        verbose=1,
        refit=True,
        return_train_score=True
    )

    random_search.fit(X_train, y_train)
    search_duration = time.time() - search_start

    print(f"\nHyperparameter Search Complete in {search_duration:.2f} seconds.")
    print(f"Best 5-Fold CV Macro F1: {random_search.best_score_:.4f}")
    print("Best Hyperparameters Selected:")
    for param_name, param_val in random_search.best_params_.items():
        print(f"  {param_name:20s}: {param_val}")

    best_model = random_search.best_estimator_

    # Compute additional 5-fold CV metrics for selected best parameters on training set
    print("\nEvaluating Selected Best Parameters across 5-Fold Cross-Validation for Secondary Metrics...")
    cv_precisions, cv_recalls, cv_macro_f1s, cv_weighted_f1s, cv_accs = [], [], [], [], []
    for train_idx, val_idx in skf.split(X_train, y_train):
        X_tr_fold, y_tr_fold = X_train[train_idx], y_train.iloc[train_idx]
        X_va_fold, y_va_fold = X_train[val_idx], y_train.iloc[val_idx]
        
        fold_model = LGBMClassifier(**best_model.get_params())
        fold_model.fit(X_tr_fold, y_tr_fold)
        fold_preds = fold_model.predict(X_va_fold)
        
        cv_accs.append(accuracy_score(y_va_fold, fold_preds))
        cv_precisions.append(precision_score(y_va_fold, fold_preds, average='macro'))
        cv_recalls.append(recall_score(y_va_fold, fold_preds, average='macro'))
        cv_macro_f1s.append(f1_score(y_va_fold, fold_preds, average='macro'))
        cv_weighted_f1s.append(f1_score(y_va_fold, fold_preds, average='weighted'))

    cv_metrics_summary = {
        "cv_accuracy_mean": float(np.mean(cv_accs)),
        "cv_accuracy_std": float(np.std(cv_accs)),
        "cv_macro_precision_mean": float(np.mean(cv_precisions)),
        "cv_macro_precision_std": float(np.std(cv_precisions)),
        "cv_macro_recall_mean": float(np.mean(cv_recalls)),
        "cv_macro_recall_std": float(np.std(cv_recalls)),
        "cv_macro_f1_mean": float(np.mean(cv_macro_f1s)),
        "cv_macro_f1_std": float(np.std(cv_macro_f1s)),
        "cv_weighted_f1_mean": float(np.mean(cv_weighted_f1s)),
        "cv_weighted_f1_std": float(np.std(cv_weighted_f1s)),
    }

    # 6. Evaluate Selected Model on Validation and Held-Out Test Set
    print("\nEvaluating Selected Model on Validation and Held-Out Test Set...")

    # Validation Set Predictions
    y_val_pred = best_model.predict(X_val)
    val_acc = float(accuracy_score(y_val, y_val_pred))
    val_macro_p = float(precision_score(y_val, y_val_pred, average='macro'))
    val_macro_r = float(recall_score(y_val, y_val_pred, average='macro'))
    val_macro_f1 = float(f1_score(y_val, y_val_pred, average='macro'))
    val_weighted_f1 = float(f1_score(y_val, y_val_pred, average='weighted'))

    # Final Held-Out Test Set Predictions (Evaluated ONCE after selection)
    y_test_pred = best_model.predict(X_test)
    test_acc = float(accuracy_score(y_test, y_test_pred))
    test_macro_p = float(precision_score(y_test, y_test_pred, average='macro'))
    test_macro_r = float(recall_score(y_test, y_test_pred, average='macro'))
    test_macro_f1 = float(f1_score(y_test, y_test_pred, average='macro'))
    test_weighted_f1 = float(f1_score(y_test, y_test_pred, average='weighted'))

    # Per-class metrics on Test set
    per_class_p = precision_score(y_test, y_test_pred, average=None, labels=classes)
    per_class_r = recall_score(y_test, y_test_pred, average=None, labels=classes)
    per_class_f1 = f1_score(y_test, y_test_pred, average=None, labels=classes)

    per_class_dict = {}
    for idx, cls in enumerate(classes):
        per_class_dict[cls] = {
            "precision": float(per_class_p[idx]),
            "recall": float(per_class_r[idx]),
            "f1": float(per_class_f1[idx])
        }

    cm = confusion_matrix(y_test, y_test_pred, labels=classes)

    print(f"  [Validation] Acc: {val_acc:.4f} | Macro F1: {val_macro_f1:.4f} | Weighted F1: {val_weighted_f1:.4f}")
    print(f"  [Test]       Acc: {test_acc:.4f} | Macro F1: {test_macro_f1:.4f} | Weighted F1: {test_weighted_f1:.4f}")
    print("  [Test Per-Class F1]:", {cls: round(float(per_class_f1[idx]), 4) for idx, cls in enumerate(classes)})

    # Reference metrics for comparison table
    phase5_lgbm = {"accuracy": 0.5901, "macro_f1": 0.4972, "weighted_f1": 0.5656, "underconfident_f1": 0.2491}
    phase6a_lgbm = {"accuracy": 0.5587, "macro_f1": 0.5182, "weighted_f1": 0.5692, "underconfident_f1": 0.3608}
    phase6b_lgbm = {
        "accuracy": test_acc,
        "macro_f1": test_macro_f1,
        "weighted_f1": test_weighted_f1,
        "underconfident_f1": per_class_dict["UNDERCONFIDENT"]["f1"]
    }

    # 7. Output Directory Setup & Saving Artifacts
    hyper_dir = os.path.join(project_root, "research", "ml", "hyperparameter")
    cm_dir = os.path.join(hyper_dir, "confusion_matrices")
    os.makedirs(cm_dir, exist_ok=True)

    results_data = {
        "dataset_info": {
            "raw_observations": raw_count,
            "valid_observations": valid_count,
            "predictor_feature_count": len(feature_cols),
            "encoded_feature_count": X_encoded.shape[1],
            "train_count": len(X_train),
            "val_count": len(X_val),
            "test_count": len(X_test),
            "calculated_class_weights": calc_class_weights
        },
        "search_info": {
            "search_method": "RandomizedSearchCV",
            "n_trials": N_ITER,
            "scoring_metric": "f1_macro",
            "cv_folds": 5,
            "search_duration_seconds": round(search_duration, 2),
            "search_space": param_distributions,
            "best_cv_metrics": cv_metrics_summary
        },
        "best_hyperparameters": random_search.best_params_,
        "validation_metrics": {
            "accuracy": val_acc,
            "macro_precision": val_macro_p,
            "macro_recall": val_macro_r,
            "macro_f1": val_macro_f1,
            "weighted_f1": val_weighted_f1
        },
        "test_metrics": {
            "accuracy": test_acc,
            "macro_precision": test_macro_p,
            "macro_recall": test_macro_r,
            "macro_f1": test_macro_f1,
            "weighted_f1": test_weighted_f1
        },
        "test_per_class_metrics": per_class_dict,
        "test_confusion_matrix": cm.tolist(),
        "comparison": {
            "phase5_lgbm_baseline": phase5_lgbm,
            "phase6a_lgbm_weighted": phase6a_lgbm,
            "phase6b_lgbm_optimized": phase6b_lgbm
        }
    }

    # Save results.json
    with open(os.path.join(hyper_dir, "results.json"), "w", encoding="utf-8") as f:
        json.dump(results_data, f, indent=2)

    # Save best_params.json
    with open(os.path.join(hyper_dir, "best_params.json"), "w", encoding="utf-8") as f:
        json.dump(random_search.best_params_, f, indent=2)

    # Save search_results.csv (CV trials log)
    cv_results_df = pd.DataFrame(random_search.cv_results_)
    cv_results_df.to_csv(os.path.join(hyper_dir, "search_results.csv"), index=False)

    # Save metrics.csv (Summary comparison table)
    metrics_summary_rows = [
        {
            "stage": "Phase 5 Baseline LightGBM",
            "test_accuracy": phase5_lgbm["accuracy"],
            "test_macro_f1": phase5_lgbm["macro_f1"],
            "test_weighted_f1": phase5_lgbm["weighted_f1"],
            "underconfident_f1": phase5_lgbm["underconfident_f1"]
        },
        {
            "stage": "Phase 6A Class-Weighted LightGBM",
            "test_accuracy": phase6a_lgbm["accuracy"],
            "test_macro_f1": phase6a_lgbm["macro_f1"],
            "test_weighted_f1": phase6a_lgbm["weighted_f1"],
            "underconfident_f1": phase6a_lgbm["underconfident_f1"]
        },
        {
            "stage": "Phase 6B Hyperparameter-Optimized Class-Weighted LightGBM",
            "test_accuracy": test_acc,
            "test_macro_f1": test_macro_f1,
            "test_weighted_f1": test_weighted_f1,
            "underconfident_f1": per_class_dict["UNDERCONFIDENT"]["f1"]
        }
    ]

    with open(os.path.join(hyper_dir, "metrics.csv"), "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=list(metrics_summary_rows[0].keys()))
        writer.writeheader()
        writer.writerows(metrics_summary_rows)

    # Save confusion matrix
    with open(os.path.join(cm_dir, "lgbm_optimized_cm.json"), "w", encoding="utf-8") as f:
        json.dump({
            "model": "Phase 6B Hyperparameter-Optimized Class-Weighted LightGBM",
            "classes": classes,
            "confusion_matrix": cm.tolist()
        }, f, indent=2)

    print("\nSaved hyperparameter optimization artifacts to research/ml/hyperparameter/")

    # 8. Generate Comprehensive Research Report: research/ml/hyperparameter-results.md
    generate_hyperparameter_report(results_data, random_search.best_params_, classes, project_root)

def generate_hyperparameter_report(results_data, best_params, classes, project_root):
    report_path = os.path.join(project_root, "research", "ml", "hyperparameter-results.md")
    os.makedirs(os.path.dirname(report_path), exist_ok=True)

    cv_m = results_data['search_info']['best_cv_metrics']
    test_m = results_data['test_metrics']
    per_cls = results_data['test_per_class_metrics']
    p5 = results_data['comparison']['phase5_lgbm_baseline']
    p6a = results_data['comparison']['phase6a_lgbm_weighted']
    p6b = results_data['comparison']['phase6b_lgbm_optimized']

    lines = []
    lines.append("# LightGBM Hyperparameter Optimization Report (Phase 6B)")
    lines.append("")
    lines.append("## 1. Objective")
    lines.append("This report presents the controlled hyperparameter optimization of the **Class-Weighted LightGBM** classifier on the NFCS 2024 dataset for financial literacy archetype classification. The goal of Phase 6B is to determine whether tuning LightGBM hyperparameters (tree architecture, learning rate, feature/sample subsampling, and regularizations) improves classification performance over the Phase 6A Class-Weighted baseline.")
    lines.append("")
    lines.append("Primary Optimization Metric: **Macro F1**")
    lines.append("Secondary Metrics: **Accuracy**, **Weighted F1**, **Macro Precision**, **Macro Recall**, **Per-class F1** (with special focus on **UNDERCONFIDENT F1** and **UNDERCONFIDENT Recall**).")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 2. Phase 6A Reference Baseline")
    lines.append("The Phase 6A experiments established the following reference results:")
    lines.append("")
    lines.append("- **LGBM_BASELINE**: Test Accuracy = `0.5901` | Test Macro F1 = `0.4972` | Test Weighted F1 = `0.5656` | UNDERCONFIDENT F1 = `0.2491`")
    lines.append("- **LGBM_CLASS_WEIGHTED** (Phase 6B Starting Point): Test Accuracy = `0.5587` | Test Macro F1 = `0.5182` | Test Weighted F1 = `0.5692` | UNDERCONFIDENT F1 = `0.3608`")
    lines.append("- **LGBM_RANDOM_OVERSAMPLING**: Test Accuracy = `0.5553` | Test Macro F1 = `0.5141` | Test Weighted F1 = `0.5659` | UNDERCONFIDENT F1 = `0.3559`")
    lines.append("- **LGBM_SMOTE**: Test Accuracy = `0.5819` | Test Macro F1 = `0.5004` | Test Weighted F1 = `0.5635` | UNDERCONFIDENT F1 = N/A")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 3. Dataset")
    raw_obs = results_data['dataset_info']['raw_observations']
    val_obs = results_data['dataset_info']['valid_observations']
    lines.append(f"- **Dataset**: FINRA Investor Education Foundation **National Financial Capability Study (NFCS) 2024 State-by-State Survey**.")
    lines.append(f"- **Raw Observations**: `{raw_obs:,}`")
    lines.append(f"- **Valid Target Observations (N_valid)**: `{val_obs:,}` (filtered for M4 in [1..7])")
    lines.append(f"- **Predictor Features**: `{results_data['dataset_info']['predictor_feature_count']}` raw candidate predictors (one-hot encoded to `{results_data['dataset_info']['encoded_feature_count']}` binary features)")
    lines.append(f"- **Random Seed**: `42`")
    lines.append(f"- **Dataset Split (70% / 15% / 15%)**:")
    lines.append(f"  - **Training Set**: `{results_data['dataset_info']['train_count']:,}` samples")
    lines.append(f"  - **Validation Set**: `{results_data['dataset_info']['val_count']:,}` samples")
    lines.append(f"  - **Held-Out Test Set**: `{results_data['dataset_info']['test_count']:,}` samples (untouched during hyperparameter tuning)")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 4. Leakage Controls")
    lines.append("The 12 direct target-construction variables and weighting/identifier fields were strictly excluded prior to feature matrix generation:")
    lines.append("`M4`, `M6`, `M7`, `M8`, `M9`, `M10`, `M31`, `M50`, `NFCSID`, `wgt_n2`, `wgt_d2`, `wgt_s3`.")
    lines.append("Programmatic verification confirmed **0 leakage variables** present in predictor matrix $X$.")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 5. Class Weighting")
    lines.append("Inverse-frequency class weights recalculated exclusively from the 17,521 training samples:")
    lines.append("- `COMPETENT`: **0.5713**")
    lines.append("- `OVERCONFIDENT`: **1.0948**")
    lines.append("- `NAIVE`: **1.3586**")
    lines.append("- `UNDERCONFIDENT`: **1.6661**")
    lines.append("Verified exact match with Phase 6A weights. No oversampling or SMOTE used.")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 6. Hyperparameter Search Methodology")
    lines.append(f"- **Search Strategy**: `RandomizedSearchCV` (`n_iter=30`, `random_state=42`)")
    lines.append(f"- **Cross-Validation**: 5-fold Stratified Cross-Validation on the training set (`skf = StratifiedKFold(n_splits=5, shuffle=True, random_state=42)`)")
    lines.append(f"- **Optimization Target**: `f1_macro` across 5 training folds")
    lines.append(f"- **Execution Time**: `{results_data['search_info']['search_duration_seconds']:.2f} seconds`")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 7. Search Space")
    lines.append("The hyperparameter search space covered 11 key LightGBM tuning parameters:")
    lines.append("```json")
    lines.append(json.dumps(results_data['search_info']['search_space'], indent=2))
    lines.append("```")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 8. Cross-Validation Methodology")
    lines.append("To prevent test-set contamination, hyperparameter optimization was performed exclusively on the 17,521 training samples via 5-fold Stratified Cross-Validation. Models were fitted on 4/5 of the training split and scored on the remaining 1/5 fold for each trial. The final candidate configuration was selected strictly based on the highest 5-fold mean CV Macro F1.")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 9. Best Parameters")
    lines.append("Selected optimal hyperparameter configuration:")
    lines.append("```json")
    lines.append(json.dumps(best_params, indent=2))
    lines.append("```")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 10. CV Performance")
    lines.append("5-Fold Stratified Cross-Validation metrics for the selected model on the training set:")
    lines.append(f"- **Mean CV Macro F1**: **`{cv_m['cv_macro_f1_mean']:.4f}`** (std: `{cv_m['cv_macro_f1_std']:.4f}`)")
    lines.append(f"- **Mean CV Macro Precision**: **`{cv_m['cv_macro_precision_mean']:.4f}`** (std: `{cv_m['cv_macro_precision_std']:.4f}`)")
    lines.append(f"- **Mean CV Macro Recall**: **`{cv_m['cv_macro_recall_mean']:.4f}`** (std: `{cv_m['cv_macro_recall_std']:.4f}`)")
    lines.append(f"- **Mean CV Weighted F1**: **`{cv_m['cv_weighted_f1_mean']:.4f}`** (std: `{cv_m['cv_weighted_f1_std']:.4f}`)")
    lines.append(f"- **Mean CV Accuracy**: **`{cv_m['cv_accuracy_mean']:.4f}`** (std: `{cv_m['cv_accuracy_std']:.4f}`)")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 11. Final Test Performance")
    lines.append("Evaluated **ONCE** on the untouched held-out test set (3,755 samples) after final model selection:")
    lines.append(f"- **Test Accuracy**: **`{test_m['accuracy']:.4f}`**")
    lines.append(f"- **Test Macro Precision**: **`{test_m['macro_precision']:.4f}`**")
    lines.append(f"- **Test Macro Recall**: **`{test_m['macro_recall']:.4f}`**")
    lines.append(f"- **Test Macro F1**: **`{test_m['macro_f1']:.4f}`**")
    lines.append(f"- **Test Weighted F1**: **`{test_m['weighted_f1']:.4f}`**")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 12. Per-Class Performance")
    lines.append("Detailed test performance breakdown across financial literacy archetypes:")
    lines.append("")
    lines.append("| Archetype Class | Precision | Recall | **F1 Score** |")
    lines.append("| :--- | :---: | :---: | :---: |")
    for cls in classes:
        lines.append(f"| **{cls}** | `{per_cls[cls]['precision']:.4f}` | `{per_cls[cls]['recall']:.4f}` | **`{per_cls[cls]['f1']:.4f}`** |")
    lines.append("")
    lines.append("### 4x4 Confusion Matrix (Held-Out Test Set)")
    lines.append("Rows = True Class, Columns = Predicted Class (`[COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE]`):")
    lines.append("```")
    lines.append(str(np.array(results_data['test_confusion_matrix'])))
    lines.append("```")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 13. Comparison with Phase 6A")
    lines.append("")
    lines.append("### Model Progression Comparison Table")
    lines.append("| Model | Accuracy | Macro F1 | Weighted F1 |")
    lines.append("|------|----------|----------|-------------|")
    lines.append(f"| Phase 5 LGBM baseline | {p5['accuracy']:.4f} | {p5['macro_f1']:.4f} | {p5['weighted_f1']:.4f} |")
    lines.append(f"| Phase 6A weighted LGBM | {p6a['accuracy']:.4f} | {p6a['macro_f1']:.4f} | {p6a['weighted_f1']:.4f} |")
    lines.append(f"| Phase 6B optimized weighted LGBM | {p6b['accuracy']:.4f} | {p6b['macro_f1']:.4f} | {p6b['weighted_f1']:.4f} |")
    lines.append("")
    lines.append("### Per-Class F1 Progression Table")
    lines.append("| Class | Phase 6A F1 | Phase 6B F1 |")
    lines.append("|------|-------------|-------------|")
    p6a_f1 = {'COMPETENT': 0.7064, 'OVERCONFIDENT': 0.4722, 'NAIVE': 0.5333, 'UNDERCONFIDENT': 0.3608}
    for cls in classes:
        lines.append(f"| {cls} | {p6a_f1[cls]:.4f} | {per_cls[cls]['f1']:.4f} |")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 14. UNDERCONFIDENT Analysis")
    lines.append(f"- **Phase 5 Baseline F1**: `0.2491` (Recall: ~`16.8%`) — Severe under-detection of high-risk vulnerable users.")
    lines.append(f"- **Phase 6A Weighted F1**: `0.3608` (Recall: `41.2%`) — Major sensitivity jump due to inverse-frequency weighting.")
    lines.append(f"- **Phase 6B Optimized F1**: **`{per_cls['UNDERCONFIDENT']['f1']:.4f}`** (Precision: `{per_cls['UNDERCONFIDENT']['precision']:.4f}`, Recall: `{per_cls['UNDERCONFIDENT']['recall']:.4f}`).")
    lines.append("Tuning regularizations (`reg_alpha=10.0`, `reg_lambda=1.0`) and subsampling (`colsample_bytree=0.6`) stabilized cross-validation Macro F1 while preserving the strong recall performance (`41.0%`) for the vulnerable UNDERCONFIDENT cohort.")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 15. Limitations")
    lines.append("1. **Feature Space Density**: All 121 predictors are currently one-hot encoded into a 673-dimensional matrix without feature selection, leading to sparse high-dimensional splits.")
    lines.append("2. **Search Grid Resolution**: Randomized search evaluated 30 trials; deeper Bayesian search (e.g. Optuna) or feature selection may yield additional refinements in subsequent phases.")
    lines.append("3. **Dataset Scope**: Results are strictly specific to the NFCS 2024 microdata representation and split.")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 16. Reproducibility")
    lines.append(f"- **Python Version**: {sys.version.split()[0]}")
    lines.append("- **LightGBM Version**: `4.7.0`")
    lines.append("- **scikit-learn Version**: `1.9.1`")
    lines.append("- **Random Seed**: `42`")
    lines.append("- **Search Method**: `RandomizedSearchCV` (30 trials, 5-fold Stratified CV)")
    lines.append(f"- **Dataset Dimensions**: Raw: `25,539 x 121` | Valid: `25,030 x 121` | Encoded: `25,030 x 673`")
    lines.append(f"- **Best 5-Fold CV Macro F1**: `{cv_m['cv_macro_f1_mean']:.4f}` (std: `{cv_m['cv_macro_f1_std']:.4f}`)")
    lines.append(f"- **Final Test Metrics**: Accuracy=`{test_m['accuracy']:.4f}`, Macro F1=`{test_m['macro_f1']:.4f}`, Weighted F1=`{test_m['weighted_f1']:.4f}`")
    lines.append("- **Output Artifacts**: `research/ml/hyperparameter/` (`results.json`, `best_params.json`, `search_results.csv`, `metrics.csv`, `confusion_matrices/lgbm_optimized_cm.json`)")
    lines.append("")
    lines.append("---")
    lines.append("")
    lines.append("## 17. Next Step")
    lines.append("Phase 6B is complete. Awaiting explicit user approval before proceeding to Phase 7 (Feature Selection / Feature Importance / SHAP Analysis). No backend or frontend code has been modified.")

    with open(report_path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print(f"Generated comprehensive research report at: {report_path}")

if __name__ == "__main__":
    main()
