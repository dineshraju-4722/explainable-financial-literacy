import React from 'react';

const TYPE_CONFIGS = {
  FOUNDATION: { label: 'Foundation', color: '#c084fc', bg: 'rgba(192, 132, 252, 0.12)' },
  REINFORCEMENT: { label: 'Reinforcement', color: '#60a5fa', bg: 'rgba(96, 165, 250, 0.12)' },
  CONFIDENCE_BUILDING: { label: 'Confidence Building', color: '#38bdf8', bg: 'rgba(56, 189, 248, 0.12)' },
  ADVANCED: { label: 'Advanced', color: '#a78bfa', bg: 'rgba(167, 139, 250, 0.12)' },
  PRACTICAL_APPLICATION: { label: 'Practical Application', color: '#34d399', bg: 'rgba(52, 211, 153, 0.12)' },
};

export default function RecommendationTypeBadge({ type }) {
  const config = TYPE_CONFIGS[type] || { label: type, color: '#94a3b8', bg: 'rgba(148, 163, 184, 0.12)' };

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '0.25rem 0.65rem',
        borderRadius: '6px',
        fontSize: '0.75rem',
        fontWeight: 600,
        background: config.bg,
        color: config.color,
        border: `1px solid ${config.color}33`,
      }}
    >
      {config.label}
    </span>
  );
}
