import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { BookOpen, Clock, ChevronRight, Filter } from 'lucide-react';

const TOPICS_LIST = [
  'ALL',
  'BUDGETING',
  'SAVING',
  'CREDIT',
  'DEBT_MANAGEMENT',
  'INVESTING',
  'INSURANCE',
  'FINANCIAL_PLANNING',
  'INTEREST',
  'INFLATION',
  'RISK',
];

export default function LearningLibrary({ recommendations = [] }) {
  const [selectedTopic, setSelectedTopic] = useState('ALL');

  const filtered = selectedTopic === 'ALL'
    ? recommendations
    : recommendations.filter((r) => r.topic === selectedTopic);

  const formatTopicName = (topStr) => topStr.toLowerCase().replace('_', ' ');

  return (
    <div className="glass-card" style={{ padding: '2rem', marginBottom: '2.5rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <BookOpen size={22} color="#818cf8" />
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#f8fafc', margin: 0 }}>
            Curated Financial Learning Library
          </h2>
        </div>

        {/* Filter Pills */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', flexWrap: 'wrap' }}>
          <Filter size={16} color="#94a3b8" style={{ marginRight: '0.2rem' }} />
          {TOPICS_LIST.map((topic) => {
            const isActive = selectedTopic === topic;
            return (
              <button
                key={topic}
                onClick={() => setSelectedTopic(topic)}
                style={{
                  padding: '0.35rem 0.75rem',
                  borderRadius: '20px',
                  fontSize: '0.78rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  border: isActive ? '1px solid var(--primary-accent)' : '1px solid var(--border-color)',
                  background: isActive ? 'var(--primary-accent)' : 'rgba(255, 255, 255, 0.03)',
                  color: isActive ? '#ffffff' : '#94a3b8',
                  transition: 'all 0.2s ease',
                  textTransform: 'capitalize',
                }}
              >
                {topic === 'ALL' ? 'All Topics' : formatTopicName(topic)}
              </button>
            );
          })}
        </div>
      </div>

      {filtered.length === 0 ? (
        <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
          <p>No learning modules found for the selected filter.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.25rem' }}>
          {filtered.map((item) => (
            <div
              key={item.contentId || item.recommendationId}
              style={{
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                padding: '1.25rem',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#818cf8', textTransform: 'uppercase' }}>
                    {formatTopicName(item.topic)}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: '#64748b', textTransform: 'capitalize' }}>
                    {item.difficulty?.toLowerCase() || 'beginner'}
                  </span>
                </div>
                <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', marginBottom: '0.4rem', lineHeight: 1.3 }}>
                  {item.title}
                </h4>
                <p style={{ color: '#94a3b8', fontSize: '0.85rem', lineHeight: 1.4, marginBottom: '1rem' }}>
                  {item.shortDescription}
                </p>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingTop: '0.75rem', borderTop: '1px solid rgba(255, 255, 255, 0.05)' }}>
                <span style={{ fontSize: '0.78rem', color: '#64748b', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <Clock size={13} />
                  {item.estimatedDurationMinutes || 15} min
                </span>
                <Link
                  to={`/education/content/${item.contentId}`}
                  style={{
                    color: '#818cf8',
                    fontSize: '0.85rem',
                    fontWeight: 600,
                    textDecoration: 'none',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.2rem',
                  }}
                >
                  Read Lesson <ChevronRight size={14} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
