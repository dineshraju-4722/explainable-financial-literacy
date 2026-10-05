import React from 'react';
import { Layers } from 'lucide-react';

const LEVEL_STYLES = {
  NEEDS_FOUNDATION: { label: 'NEEDS FOUNDATION', color: '#fbbf24', bg: 'rgba(245, 158, 11, 0.12)' },
  NEEDS_REINFORCEMENT: { label: 'NEEDS REINFORCEMENT', color: '#60a5fa', bg: 'rgba(96, 165, 250, 0.12)' },
  STRONG: { label: 'STRONG', color: '#34d399', bg: 'rgba(52, 211, 153, 0.12)' },
};

export default function TopicPerformanceTable({ categories }) {
  if (!categories || categories.length === 0) return null;

  return (
    <div className="glass-card" style={{ padding: '1.75rem', marginBottom: '2rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.25rem' }}>
        <Layers size={20} color="#818cf8" />
        <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
          Topic Performance Breakdown
        </h3>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: '#94a3b8', fontSize: '0.8rem', textTransform: 'uppercase' }}>
              <th style={{ padding: '0.75rem 1rem' }}>Topic</th>
              <th style={{ padding: '0.75rem 1rem' }}>Correct / Answered</th>
              <th style={{ padding: '0.75rem 1rem' }}>Score</th>
              <th style={{ padding: '0.75rem 1rem' }}>Performance Level</th>
            </tr>
          </thead>
          <tbody>
            {categories.map((cat, idx) => {
              const total = cat.totalQuestions || (cat.correctAnswers + cat.incorrectAnswers);
              const correct = cat.correctAnswers;
              const pct = cat.percentage !== undefined ? cat.percentage : (total > 0 ? (correct / total) * 100 : 0);

              let levelKey = 'STRONG';
              if (pct < 40.0) levelKey = 'NEEDS_FOUNDATION';
              else if (pct < 70.0) levelKey = 'NEEDS_REINFORCEMENT';

              const levelStyle = LEVEL_STYLES[levelKey];
              const formatCategory = (str) => String(str).toLowerCase().replace('_', ' ');

              return (
                <tr
                  key={cat.category || idx}
                  style={{
                    borderBottom: idx < categories.length - 1 ? '1px solid rgba(255, 255, 255, 0.04)' : 'none',
                    transition: 'background 0.2s ease',
                  }}
                >
                  <td style={{ padding: '0.85rem 1rem', fontWeight: 600, color: '#f8fafc', textTransform: 'capitalize' }}>
                    {formatCategory(cat.category)}
                  </td>
                  <td style={{ padding: '0.85rem 1rem', color: '#cbd5e1' }}>
                    {correct} / {total}
                  </td>
                  <td style={{ padding: '0.85rem 1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <span style={{ fontWeight: 700, color: '#f8fafc', minWidth: '40px' }}>
                        {pct.toFixed(0)}%
                      </span>
                      <div style={{ flex: 1, maxWidth: '120px', height: '6px', background: 'rgba(255, 255, 255, 0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                        <div style={{ width: `${pct}%`, height: '100%', background: levelStyle.color, borderRadius: '3px' }} />
                      </div>
                    </div>
                  </td>
                  <td style={{ padding: '0.85rem 1rem' }}>
                    <span
                      style={{
                        display: 'inline-block',
                        padding: '0.25rem 0.65rem',
                        borderRadius: '6px',
                        fontSize: '0.75rem',
                        fontWeight: 700,
                        background: levelStyle.bg,
                        color: levelStyle.color,
                        border: `1px solid ${levelStyle.color}44`,
                      }}
                    >
                      {levelStyle.label}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
