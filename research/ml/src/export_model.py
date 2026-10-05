import os
import sys
import json
import joblib
import numpy as np
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder
from lightgbm import LGBMClassifier
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, classification_report

def main():
    print("=" * 80)
    print("PHASE 7A — MODEL & PREPROCESSOR EXPORT RUNNER")
    print("=" * 80)

    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, "..", "..", ".."))
    csv_path = os.path.join(project_root, "research", "dataset", "NFCS 2024 State Data 250623.csv")
    feature_spec_path = os.path.join(project_root, "research", "ml", "feature-selection", "final_selected_features.json")
    output_dir = os.path.join(project_root, "ml-service", "models")

    os.makedirs(output_dir, exist_ok=True)

    print(f"Loading dataset from: {csv_path}")
    raw_df = pd.read_csv(csv_path)
    assert len(raw_df) == 25539, f"Expected 25,539 raw observations, got {len(raw_df)}"

    valid_df = raw_df[raw_df['M4'].isin([1, 2, 3, 4, 5, 6, 7])].copy()
    assert len(valid_df) == 25030, f"Expected 25,030 valid observations, got {len(valid_df)}"

    # Quiz score calculation
    m6_corr = (valid_df['M6'] == 1).astype(int)
    m7_corr = (valid_df['M7'] == 3).astype(int)
    m8_corr = (valid_df['M8'] == 2).astype(int)
    m9_corr = (valid_df['M9'] == 1).astype(int)
    m10_corr = (valid_df['M10'] == 2).astype(int)
    valid_df['obj_score'] = m6_corr + m7_corr + m8_corr + m9_corr + m10_corr
    valid_df['obj_high'] = (valid_df['obj_score'] >= 3).astype(int)
    valid_df['subj_high'] = (valid_df['M4'] >= 5).astype(int)

    def assign_class(row):
        if row['obj_high'] == 1 and row['subj_high'] == 1:
            return 'COMPETENT'
        elif row['obj_high'] == 0 and row['subj_high'] == 1:
            return 'OVERCONFIDENT'
        elif row['obj_high'] == 1 and row['subj_high'] == 0:
            return 'UNDERCONFIDENT'
        else:
            return 'NAIVE'

    valid_df['financial_literacy_class'] = valid_df.apply(assign_class, axis=1)

    class_names = ['COMPETENT', 'OVERCONFIDENT', 'UNDERCONFIDENT', 'NAIVE']
    class_to_idx = {c: i for i, c in enumerate(class_names)}
    idx_to_class = {i: c for i, c in enumerate(class_names)}

    # Load TOP_75 feature specification
    with open(feature_spec_path, 'r') as f:
        feature_spec = json.load(f)

    selected_orig_vars = feature_spec['selected_orig_variables']
    selected_encoded_cols = feature_spec['selected_encoded_features']
    assert len(selected_orig_vars) == 75, f"Expected 75 original predictors, got {len(selected_orig_vars)}"
    assert len(selected_encoded_cols) == 503, f"Expected 503 encoded features, got {len(selected_encoded_cols)}"

    # Leakage check & 121 feature columns in original dataset order
    leakage_vars = ['M4', 'M6', 'M7', 'M8', 'M9', 'M10', 'M31', 'M50', 'NFCSID', 'wgt_n2', 'wgt_d2', 'wgt_s3']
    all_121_feature_cols = [c for c in valid_df.columns if c not in leakage_vars and c not in ['financial_literacy_class', 'obj_score', 'obj_high', 'subj_high']]
    
    intersection = set(selected_orig_vars).intersection(set(leakage_vars))
    assert len(intersection) == 0, f"Leakage detected: {intersection}"

    X_all_raw = valid_df[all_121_feature_cols].copy()
    y = valid_df['financial_literacy_class'].copy()

    # Preprocess with OneHotEncoder on 121 raw features
    full_encoder = OneHotEncoder(sparse_output=False, handle_unknown='ignore')
    X_encoded_all = full_encoder.fit_transform(X_all_raw.astype(str))
    all_encoded_feature_names = list(full_encoder.get_feature_names_out(all_121_feature_cols))

    encoded_to_orig = {}
    for enc_name in all_encoded_feature_names:
        matched = None
        for col in all_121_feature_cols:
            if enc_name.startswith(col + "_"):
                matched = col
                break
        if matched is None:
            matched = enc_name.split('_')[0]
        encoded_to_orig[enc_name] = matched

    top75_enc_indices = [
        idx for idx, enc_name in enumerate(all_encoded_feature_names)
        if encoded_to_orig[enc_name] in set(selected_orig_vars)
    ]
    top75_enc_names = [all_encoded_feature_names[i] for i in top75_enc_indices]
    assert len(top75_enc_indices) == 503, f"Expected 503 encoded features, got {len(top75_enc_indices)}"
    assert top75_enc_names == selected_encoded_cols, "Encoded feature names mismatch!"

    # Train / Val / Test split (70 / 15 / 15, seed 42)
    X_train_enc_all, X_temp_enc_all, y_train, y_temp = train_test_split(
        X_encoded_all, y, test_size=0.30, random_state=42, stratify=y
    )
    X_val_enc_all, X_test_enc_all, y_val, y_test = train_test_split(
        X_temp_enc_all, y_temp, test_size=0.50, random_state=42, stratify=y_temp
    )

    X_train_top75 = X_train_enc_all[:, top75_enc_indices]
    X_test_top75 = X_test_enc_all[:, top75_enc_indices]

    print(f"Train size: {len(X_train_top75)}, Test size: {len(X_test_top75)}")

    # Create & fit standalone preprocessor / encoder specifically for the 75 original predictors in exact feature order
    encoder = OneHotEncoder(handle_unknown='ignore', sparse_output=False)
    encoder.fit(valid_df[selected_orig_vars].astype(str))
    assert len(encoder.get_feature_names_out()) == 503, f"Expected 503 encoder feature names, got {len(encoder.get_feature_names_out())}"

    # Phase 6B Hyperparameters
    lgbm_params = {
        "objective": "multiclass",
        "num_class": 4,
        "class_weight": "balanced",
        "subsample_freq": 5,
        "subsample": 0.8,
        "reg_lambda": 0.0,
        "reg_alpha": 0.1,
        "num_leaves": 15,
        "n_estimators": 250,
        "min_split_gain": 0.01,
        "min_child_samples": 30,
        "max_depth": 8,
        "learning_rate": 0.05,
        "colsample_bytree": 0.7,
        "random_state": 42,
        "n_jobs": 4,
        "verbose": -1
    }

    X_train_df = pd.DataFrame(X_train_top75, columns=selected_encoded_cols)
    X_test_df = pd.DataFrame(X_test_top75, columns=selected_encoded_cols)

    print("Training final LightGBM TOP_75 model...")
    model = LGBMClassifier(**lgbm_params)
    model.fit(X_train_df, y_train)

    # Evaluate on held-out Test set
    y_pred = model.predict(X_test_df)

    acc = accuracy_score(y_test, y_pred)
    macro_prec = precision_score(y_test, y_pred, average='macro')
    macro_rec = recall_score(y_test, y_pred, average='macro')
    macro_f1 = f1_score(y_test, y_pred, average='macro')
    weighted_f1 = f1_score(y_test, y_pred, average='weighted')
    per_class_f1 = f1_score(y_test, y_pred, labels=class_names, average=None)

    print("\n" + "=" * 60)
    print("HELD-OUT TEST SET EVALUATION VERIFICATION")
    print("=" * 60)
    print(f"Accuracy:        {acc:.4f}")
    print(f"Macro Precision: {macro_prec:.4f}")
    print(f"Macro Recall:    {macro_rec:.4f}")
    print(f"Macro F1:        {macro_f1:.4f}")
    print(f"Weighted F1:     {weighted_f1:.4f}")
    print("-" * 60)
    for i, name in enumerate(class_names):
        print(f"  {name:<15} F1: {per_class_f1[i]:.4f}")

    assert abs(acc - 0.5619) < 0.005, f"Accuracy discrepancy: {acc} vs expected 0.5619"
    assert abs(macro_f1 - 0.5205) < 0.005, f"Macro F1 discrepancy: {macro_f1} vs expected 0.5205"

    # Export Artifacts
    print("\nSaving artifacts to:", output_dir)

    model_path = os.path.join(output_dir, "lightgbm_top75_model.joblib")
    encoder_path = os.path.join(output_dir, "encoder.joblib")
    feature_spec_out = os.path.join(output_dir, "top_75_features.json")
    class_map_out = os.path.join(output_dir, "class_mapping.json")
    metadata_out = os.path.join(output_dir, "model_metadata.json")

    joblib.dump(model, model_path)
    joblib.dump(encoder, encoder_path)

    with open(feature_spec_out, 'w') as f:
        json.dump({
            "selected_subset_name": "TOP_75",
            "orig_predictors": selected_orig_vars,
            "encoded_features": selected_encoded_cols,
            "orig_predictors_count": 75,
            "encoded_features_count": 503
        }, f, indent=2)

    with open(class_map_out, 'w') as f:
        json.dump({
            "idx_to_class": idx_to_class,
            "class_to_idx": class_to_idx
        }, f, indent=2)

    metadata = {
        "model_name": "LightGBM_TOP75_ClassWeighted",
        "version": "1.0.0",
        "created_at": "2026-10-04",
        "dataset": "FINRA NFCS 2024 State Data",
        "num_observations_train": len(X_train_top75),
        "num_observations_test": len(X_test_top75),
        "orig_predictors_count": 75,
        "encoded_features_count": 503,
        "metrics": {
            "accuracy": round(float(acc), 4),
            "macro_precision": round(float(macro_prec), 4),
            "macro_recall": round(float(macro_rec), 4),
            "macro_f1": round(float(macro_f1), 4),
            "weighted_f1": round(float(weighted_f1), 4),
            "per_class_f1": {name: round(float(per_class_f1[i]), 4) for i, name in enumerate(class_names)}
        },
        "hyperparameters": {
            "subsample_freq": 5,
            "subsample": 0.8,
            "reg_lambda": 0.0,
            "reg_alpha": 0.1,
            "num_leaves": 15,
            "n_estimators": 250,
            "min_split_gain": 0.01,
            "min_child_samples": 30,
            "max_depth": 8,
            "learning_rate": 0.05,
            "colsample_bytree": 0.7
        },
        "class_weights": "balanced"
    }

    with open(metadata_out, 'w') as f:
        json.dump(metadata, f, indent=2)

    print("Successfully exported all Phase 7A model artifacts!")

if __name__ == "__main__":
    main()
