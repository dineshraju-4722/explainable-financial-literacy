import React from 'react';
import { Cpu, Info, TrendingUp, TrendingDown } from 'lucide-react';

export default function ShapExplanationSection({ explanationData, archetype }) {
  if (!explanationData) return null;

  const targetClass = explanationData.targetClass || explanationData.target_class || archetype;
  const posContributors = explanationData.topPositiveContributors || explanationData.top_positive_contributors || [];
  const negContributors = explanationData.topNegativeContributors || explanationData.top_negative_contributors || [];

  return (
    <div className="glass-card" style={{ padding: '2rem', marginBottom: '2.5rem', borderColor: 'rgba(99, 102, 241, 0.3)' }}>
      {/* Section Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '0.75rem', flexWrap: 'wrap' }}>
        <div style={{ background: 'rgba(99, 102, 241, 0.2)', padding: '0.5rem', borderRadius: '10px', display: 'flex' }}>
          <Cpu size={22} color="#818cf8" />
        </div>
        <div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f8fafc', margin: 0, lineHeight: 1.2 }}>
            Why Did the Model Predict This Profile?
          </h2>
          <span style={{ fontSize: '0.85rem', color: '#94a3b8' }}>
            TreeSHAP Feature-Attribution Model Explanation ({targetClass})
          </span>
        </div>
      </div>

      <p style={{ color: '#cbd5e1', fontSize: '0.95rem', lineHeight: 1.5, marginBottom: '1.5rem' }}>
        The machine learning system applies TreeSHAP (SHapley Additive exPlanations) to measure how individual survey predictor variables contributed toward predicting the <strong>{targetClass}</strong> archetype.
      </p>

      {/* Mandatory Research & Safety Disclaimer */}
      <div
        style={{
          background: 'rgba(99, 102, 241, 0.08)',
          border: '1px solid rgba(99, 102, 241, 0.25)',
          borderRadius: 'var(--radius-md)',
          padding: '1rem 1.25rem',
          display: 'flex',
          gap: '0.75rem',
          alignItems: 'flex-start',
          marginBottom: '1.75rem',
        }}
        role="note"
      >
        <Info size={20} color="#818cf8" style={{ flexShrink: 0, marginTop: '2px' }} />
        <p style={{ color: '#cbd5e1', fontSize: '0.875rem', lineHeight: 1.5, margin: 0 }}>
          <strong>Model Explanation Disclaimer:</strong> These factors explain how the machine-learning model arrived at its prediction. They describe model behavior and should not be interpreted as causal explanations or financial advice. Educational recommendations are determined independently by explicit pedagogical rules.
        </p>
      </div>

      {/* Grid of Contributors */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem' }}>
        {/* Positive Contributors */}
        <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: '#34d399', fontWeight: 700, fontSize: '0.95rem' }}>
            <TrendingUp size={18} />
            <span>Positive Predictors (Pushed Toward {targetClass})</span>
          </div>

          {posContributors.length === 0 ? (
            <p style={{ color: '#64748b', fontSize: '0.85rem' }}>No significant positive features recorded.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              {posContributors.map((item, idx) => {
                const val = item.shapValue || item.shap_value || 0;
                return (
                  <div key={idx} style={{ background: 'rgba(255, 255, 255, 0.02)', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid rgba(52, 211, 153, 0.15)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                      <span style={{ fontWeight: 700, color: '#f8fafc' }}>{item.feature}</span>
                      <span style={{ fontWeight: 700, color: '#34d399' }}>+{val.toFixed(4)}</span>
                    </div>
                    <p style={{ fontSize: '0.78rem', color: '#94a3b8', margin: '0 0 0.4rem 0', lineHeight: 1.3 }}>
                      {item.description || 'Survey predictor variable'}
                    </p>
                    <div style={{ width: '100%', height: '5px', background: 'rgba(255, 255, 255, 0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${Math.min(Math.abs(val) * 300, 100)}%`, height: '100%', background: '#34d399' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Negative Contributors */}
        <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: '#f472b6', fontWeight: 700, fontSize: '0.95rem' }}>
            <TrendingDown size={18} />
            <span>Negative Predictors (Pushed Away From {targetClass})</span>
          </div>

          {negContributors.length === 0 ? (
            <p style={{ color: '#64748b', fontSize: '0.85rem' }}>No significant negative features recorded.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
              {negContributors.map((item, idx) => {
                const val = item.shapValue || item.shap_value || 0;
                return (
                  <div key={idx} style={{ background: 'rgba(255, 255, 255, 0.02)', padding: '0.65rem 0.85rem', borderRadius: '6px', border: '1px solid rgba(244, 114, 182, 0.15)' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                      <span style={{ fontWeight: 700, color: '#f8fafc' }}>{item.feature}</span>
                      <span style={{ fontWeight: 700, color: '#f472b6' }}>{val.toFixed(4)}</span>
                    </div>
                    <p style={{ fontSize: '0.78rem', color: '#94a3b8', margin: '0 0 0.4rem 0', lineHeight: 1.3 }}>
                      {item.description || 'Survey predictor variable'}
                    </p>
                    <div style={{ width: '100%', height: '5px', background: 'rgba(255, 255, 255, 0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                      <div style={{ width: `${Math.min(Math.abs(val) * 300, 100)}%`, height: '100%', background: '#f472b6' }} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
