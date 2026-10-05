import os
import json
import joblib
import pandas as pd
import numpy as np
import shap
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional

VARIABLE_DESCRIPTIONS = {
    'M1_1': 'Overall Financial Knowledge (Self-Assessment Scale 1-7)',
    'J20': 'Overall Financial Satisfaction (Scale 1-10)',
    'J1': 'Difficulty Covering Monthly Expenses',
    'J32': 'Financial Stress / Anxiety in Life',
    'J8': 'Checking Account Overdraft Frequency',
    'A50A': 'Gender Identity (Female / Male)',
    'A14': 'Household Income Volatility / Unexpected Drop',
    'B60': 'Unbanked Status / No Checking or Savings Account',
    'J2': 'Spending Behavior Relative to Income',
    'B14A_1': 'Cryptocurrency Ownership Status',
    'C10_2012': 'Carried Credit Card Debt Burden',
    'A5_2015': 'Highest Education Level Completed',
    'J43': 'Confidence in Coming Up with $2,000 in Emergency',
    'A50B': 'Ethnicity / Racial Identity',
    'C11_2012': 'Heavy Debt Burden Perception',
    'STATEQ': 'US State Jurisdiction',
    'C5_2012': 'Hardship Withdrawal from Retirement Account',
    'A3Ar_w': 'Age Category Bracket',
    'A21_2015': 'Felt Financial Hardship in Past Year',
    'J33_40': 'Short-term Financial Emergency Preparedness',
    'A8_2021': 'Annual Household Income Tier',
    'C4_2012': 'Regular Retirement Account Contributions',
    'J63': 'Financial Advice Seeking Behavior',
    'J41_3': 'Used Payday Loan / Pawn Shop / Rent-to-Own (AFS)',
    'F2_3': 'Paid Minimum Only on Credit Card',
    'A41': 'Primary Source of Income Shock',
    'F2_6': 'Used Credit Card for Cash Advance',
    'E20': 'Homeowner Status / Mortgage Holding',
    'C3_2012': 'Taxable Stock / Bond Account Holding',
    'G25_3': 'Behind on Student Loan Payments'
}

def get_var_description(var_name: str) -> str:
    return VARIABLE_DESCRIPTIONS.get(var_name, f"Predictor Variable {var_name}")

class ModelService:
    def __init__(self, models_dir: Optional[str] = None):
        if models_dir is None:
            script_dir = os.path.dirname(os.path.abspath(__file__))
            models_dir = os.path.abspath(os.path.join(script_dir, "..", "..", "models"))
        
        self.models_dir = models_dir
        self.model = None
        self.encoder = None
        self.explainer = None
        self.orig_predictors: List[str] = []
        self.encoded_features: List[str] = []
        self.class_names: List[str] = []
        self.metadata: Dict[str, Any] = {}
        self.encoded_to_orig: Dict[str, str] = {}
        self.is_loaded = False

    def load_artifacts(self):
        model_path = os.path.join(self.models_dir, "lightgbm_top75_model.joblib")
        encoder_path = os.path.join(self.models_dir, "encoder.joblib")
        feature_spec_path = os.path.join(self.models_dir, "top_75_features.json")
        class_map_path = os.path.join(self.models_dir, "class_mapping.json")
        metadata_path = os.path.join(self.models_dir, "model_metadata.json")

        if not os.path.exists(model_path) or not os.path.exists(encoder_path):
            raise FileNotFoundError(f"Model artifacts not found in directory: {self.models_dir}")

        self.model = joblib.load(model_path)
        self.encoder = joblib.load(encoder_path)

        with open(feature_spec_path, "r") as f:
            feature_spec = json.load(f)
            self.orig_predictors = feature_spec["orig_predictors"]
            self.encoded_features = feature_spec["encoded_features"]

        with open(class_map_path, "r") as f:
            class_map = json.load(f)
            self.class_names = list(class_map["class_to_idx"].keys())

        with open(metadata_path, "r") as f:
            self.metadata = json.load(f)

        # Build feature mapping: encoded feature name -> parent original variable
        for enc_name in self.encoded_features:
            matched = None
            for orig_var in self.orig_predictors:
                if enc_name.startswith(orig_var + "_"):
                    matched = orig_var
                    break
            if matched is None:
                matched = enc_name.split('_')[0]
            self.encoded_to_orig[enc_name] = matched

        # Initialize thread-safe TreeExplainer
        self.explainer = shap.TreeExplainer(self.model)

        self.is_loaded = True
        print(f"[ModelService] Loaded LightGBM TOP_75 model v{self.metadata.get('version', '1.0.0')} & TreeExplainer")

    def validate_features(self, features: Dict[str, Any]) -> List[str]:
        """
        Validate that all 75 TOP_75 variables are present in features dict.
        Returns list of missing variable codes.
        """
        if not self.is_loaded:
            self.load_artifacts()

        missing = []
        for var_name in self.orig_predictors:
            if var_name not in features or features[var_name] is None or str(features[var_name]).strip() == "":
                missing.append(var_name)
        return missing

    def _prepare_encoded_df(self, features: Dict[str, Any]) -> pd.DataFrame:
        input_row = {}
        for var in self.orig_predictors:
            val = features.get(var)
            if val is None or str(val).strip() == "":
                val = "1"
            else:
                val = str(val).strip()
            input_row[var] = val

        input_df = pd.DataFrame([input_row], columns=self.orig_predictors)
        encoded_array = self.encoder.transform(input_df)
        return pd.DataFrame(encoded_array, columns=self.encoded_features)

    def predict(self, features: Dict[str, Any]) -> Dict[str, Any]:
        if not self.is_loaded:
            self.load_artifacts()

        encoded_df = self._prepare_encoded_df(features)

        # Run LightGBM prediction
        probas = self.model.predict_proba(encoded_df)[0]
        pred_class_idx = int(np.argmax(probas))
        pred_archetype = self.model.classes_[pred_class_idx]

        probability_map = {}
        for idx, class_name in enumerate(self.model.classes_):
            probability_map[str(class_name)] = round(float(probas[idx]), 4)

        return {
            "archetype": str(pred_archetype),
            "probabilities": probability_map,
            "model_version": self.metadata.get("version", "1.0.0"),
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "metadata": {
                "model_name": self.metadata.get("model_name", "LightGBM_TOP75_ClassWeighted"),
                "macro_f1": self.metadata.get("metrics", {}).get("macro_f1", 0.5205),
                "accuracy": self.metadata.get("metrics", {}).get("accuracy", 0.5619)
            }
        }

    def explain(self, features: Dict[str, Any], target_class: Optional[str] = None, top_k: int = 5) -> Dict[str, Any]:
        if not self.is_loaded:
            self.load_artifacts()

        if top_k < 1 or top_k > 10:
            raise ValueError(f"top_k must be between 1 and 10, got {top_k}")

        # 1. Get base prediction for context & consistency
        pred_res = self.predict(features)
        predicted_archetype = pred_res["archetype"]

        if target_class is None or str(target_class).strip() == "":
            target_class = predicted_archetype
        else:
            target_class = str(target_class).strip().upper()

        if target_class not in list(self.model.classes_):
            raise ValueError(f"Invalid target_class '{target_class}'. Must be one of: {list(self.model.classes_)}")

        class_idx = list(self.model.classes_).index(target_class)

        # 2. Compute TreeSHAP values
        encoded_df = self._prepare_encoded_df(features)
        shap_values = self.explainer.shap_values(encoded_df)

        # Extract 1D array of 503 SHAP values for target_class
        if isinstance(shap_values, list):
            class_shap_503 = shap_values[class_idx][0]
        elif len(shap_values.shape) == 3:
            class_shap_503 = shap_values[0, :, class_idx]
        else:
            class_shap_503 = shap_values[0]

        # 3. Aggregate SHAP values back to original 75 NFCS variables
        orig_shap_sums: Dict[str, float] = {var: 0.0 for var in self.orig_predictors}
        for enc_idx, enc_name in enumerate(self.encoded_features):
            parent_var = self.encoded_to_orig.get(enc_name, enc_name.split('_')[0])
            if parent_var in orig_shap_sums:
                orig_shap_sums[parent_var] += float(class_shap_503[enc_idx])

        # 4. Extract Top Positive & Negative Contributors
        positive_items = [(var, val) for var, val in orig_shap_sums.items() if val > 0.0]
        negative_items = [(var, val) for var, val in orig_shap_sums.items() if val < 0.0]

        positive_items.sort(key=lambda x: x[1], reverse=True)
        negative_items.sort(key=lambda x: x[1])  # most negative first

        top_pos = positive_items[:top_k]
        top_neg = negative_items[:top_k]

        def build_contributor_list(items):
            result = []
            for var_name, shap_val in items:
                result.append({
                    "feature": var_name,
                    "description": get_var_description(var_name),
                    "shap_value": round(float(shap_val), 4)
                })
            return result

        return {
            "archetype": predicted_archetype,
            "probabilities": pred_res["probabilities"],
            "model_version": pred_res["model_version"],
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "explanation": {
                "target_class": target_class,
                "top_k": top_k,
                "top_positive_contributors": build_contributor_list(top_pos),
                "top_negative_contributors": build_contributor_list(top_neg)
            }
        }

# Global Singleton Instance
model_service = ModelService()
