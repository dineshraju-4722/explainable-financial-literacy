import React from 'react';

const PRIORITY_STYLES = {
  HIGH: {
    label: 'HIGH PRIORITY',
    bg: 'rgba(245, 158, 11, 0.15)',
    color: '#fbbf24',
    border: 'rgba(245, 158, 11, 0.3)',
  },
  MEDIUM: {
    label: 'MEDIUM PRIORITY',
    bg: 'rgba(99, 102, 241, 0.15)',
    color: '#818cf8',
    border: 'rgba(99, 102, 241, 0.3)',
  },
  LOW: {
    label: 'LOW PRIORITY',
    bg: 'rgba(16, 185, 129, 0.15)',
    color: '#34d399',
    border: 'rgba(16, 185, 129, 0.3)',
  },
};

export default function PriorityBadge({ priority }) {
  const style = PRIORITY_STYLES[priority] || PRIORITY_STYLES.MEDIUM;

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '0.25rem 0.65rem',
        borderRadius: '6px',
        fontSize: '0.75rem',
        fontWeight: 700,
        letterSpacing: '0.04em',
        background: style.bg,
        color: style.color,
        border: `1px solid ${style.border}`,
      }}
      aria-label={`Priority level: ${style.label}`}
    >
      {style.label}
    </span>
  );
}
