import React from 'react';
import { Award, Compass, ShieldCheck, Zap } from 'lucide-react';

const ARCHETYPE_CONFIGS = {
  COMPETENT: {
    label: 'COMPETENT',
    title: 'Demonstrated & Self-Assessed Capability',
    icon: ShieldCheck,
    badgeBg: 'rgba(16, 185, 129, 0.15)',
    badgeColor: '#10b981',
    borderColor: 'rgba(16, 185, 129, 0.4)',
    summary: 'Your results indicate relatively strong financial literacy capability. The recommended content focuses on reinforcing and extending that capability.',
    recommendedFocus: 'Reinforce advanced concepts, financial planning strategies, and long-term asset allocation.',
  },
  OVERCONFIDENT: {
    label: 'OVERCONFIDENT',
    title: 'High Self-Assessed Confidence with Concept Gaps',
    icon: Compass,
    badgeBg: 'rgba(245, 158, 11, 0.15)',
    badgeColor: '#f59e0b',
    borderColor: 'rgba(245, 158, 11, 0.4)',
    summary: 'Your assessment pattern suggests that your self-assessed financial knowledge is higher than your demonstrated performance on the assessment. The recommended content focuses on strengthening core concepts and validating financial assumptions.',
    recommendedFocus: 'Validate core financial assumptions, review foundational principles, and reinforce risk awareness.',
  },
  UNDERCONFIDENT: {
    label: 'UNDERCONFIDENT',
    title: 'High Demonstrated Knowledge with Conservative Confidence',
    icon: Award,
    badgeBg: 'rgba(129, 140, 248, 0.15)',
    badgeColor: '#818cf8',
    borderColor: 'rgba(129, 140, 248, 0.4)',
    summary: 'Your assessment pattern suggests stronger demonstrated performance than your self-assessed financial knowledge. The recommended content focuses on reinforcing your existing knowledge and building confidence in applying it.',
    recommendedFocus: 'Reinforce existing demonstrated knowledge and build confidence in practical financial decision-making.',
  },
  NAIVE: {
    label: 'NAIVE',
    title: 'Foundational Knowledge Building Opportunity',
    icon: Zap,
    badgeBg: 'rgba(192, 132, 252, 0.15)',
    badgeColor: '#c084fc',
    borderColor: 'rgba(192, 132, 252, 0.4)',
    summary: 'The assessment indicates opportunities to strengthen foundational financial concepts.',
    recommendedFocus: 'Build foundational understanding in budgeting, saving reserves, debt mechanics, and compound interest.',
  },
};

export default function ArchetypeCard({ archetype, summaryOverride, probabilities }) {
  const config = ARCHETYPE_CONFIGS[archetype] || ARCHETYPE_CONFIGS.COMPETENT;
  const IconComponent = config.icon;

  return (
    <div
      className="glass-card"
      style={{
        padding: '2rem',
        borderColor: config.borderColor,
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ background: config.badgeBg, padding: '0.6rem', borderRadius: '12px', display: 'flex' }}>
            <IconComponent size={24} color={config.badgeColor} />
          </div>
          <div>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', color: config.badgeColor }}>
              Predicted ML Archetype
            </span>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#f8fafc', margin: 0, lineHeight: 1.2 }}>
              {config.label}
            </h2>
          </div>
        </div>

        <span
          style={{
            background: config.badgeBg,
            color: config.badgeColor,
            border: `1px solid ${config.borderColor}`,
            padding: '0.4rem 1rem',
            borderRadius: '20px',
            fontSize: '0.85rem',
            fontWeight: 700,
          }}
        >
          {config.title}
        </span>
      </div>

      <p style={{ color: '#cbd5e1', fontSize: '1rem', lineHeight: 1.6, marginBottom: '1.5rem' }}>
        {summaryOverride || config.summary}
      </p>

      <div style={{ background: 'rgba(15, 23, 42, 0.6)', padding: '1rem 1.25rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)', marginBottom: probabilities ? '1.5rem' : '0' }}>
        <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', color: '#94a3b8', display: 'block', marginBottom: '0.3rem' }}>
          Recommended Educational Focus
        </span>
        <p style={{ color: '#f8fafc', fontSize: '0.925rem', fontWeight: 500, margin: 0 }}>
          {config.recommendedFocus}
        </p>
      </div>

      {probabilities && (
        <div style={{ marginTop: '1rem' }}>
          <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', color: '#94a3b8', display: 'block', marginBottom: '0.6rem' }}>
            Model Archetype Probability Distribution
          </span>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: '0.5rem' }}>
            {Object.entries(probabilities).map(([archKey, probVal]) => {
              const pct = (probVal * 100).toFixed(1);
              const isSelected = archKey === archetype;
              return (
                <div
                  key={archKey}
                  style={{
                    background: isSelected ? 'rgba(99, 102, 241, 0.2)' : 'rgba(255, 255, 255, 0.03)',
                    border: isSelected ? '1px solid rgba(99, 102, 241, 0.5)' : '1px solid var(--border-color)',
                    borderRadius: '8px',
                    padding: '0.5rem 0.75rem',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: isSelected ? '#f8fafc' : '#94a3b8', fontWeight: isSelected ? 700 : 500, marginBottom: '0.25rem' }}>
                    <span>{archKey}</span>
                    <span>{pct}%</span>
                  </div>
                  <div style={{ width: '100%', height: '4px', background: 'rgba(255, 255, 255, 0.1)', borderRadius: '2px', overflow: 'hidden' }}>
                    <div style={{ width: `${pct}%`, height: '100%', background: isSelected ? 'var(--primary-accent)' : '#64748b' }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
