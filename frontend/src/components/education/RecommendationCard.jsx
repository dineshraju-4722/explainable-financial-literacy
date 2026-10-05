import React from 'react';
import { Link } from 'react-router-dom';
import PriorityBadge from './PriorityBadge';
import RecommendationTypeBadge from './RecommendationTypeBadge';
import {
  Wallet,
  PiggyBank,
  CreditCard,
  Receipt,
  TrendingUp,
  Shield,
  Target,
  Percent,
  ArrowUpRight,
  Activity,
  FileText,
  Clock,
  BookOpen,
  ChevronRight,
} from 'lucide-react';

const TOPIC_ICONS = {
  BUDGETING: Wallet,
  SAVING: PiggyBank,
  CREDIT: CreditCard,
  DEBT_MANAGEMENT: Receipt,
  INVESTING: TrendingUp,
  INSURANCE: Shield,
  FINANCIAL_PLANNING: Target,
  INTEREST: Percent,
  INFLATION: ArrowUpRight,
  RISK: Activity,
  TAXATION: FileText,
};

export default function RecommendationCard({ recommendation }) {
  const {
    contentId,
    topic,
    priority,
    recommendationType,
    title,
    shortDescription,
    rationale,
    learningObjective,
    estimatedDurationMinutes,
    difficulty,
  } = recommendation;

  const IconComponent = TOPIC_ICONS[topic] || BookOpen;

  const formatTopic = (topStr) => topStr.toLowerCase().replace('_', ' ');

  return (
    <div
      className="glass-card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        justify: 'space-between',
        padding: '1.5rem',
        height: '100%',
        transition: 'transform 0.2s ease, border-color 0.2s ease',
      }}
    >
      <div>
        {/* Header Badges */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <div style={{ background: 'rgba(99, 102, 241, 0.15)', padding: '0.4rem', borderRadius: '8px', display: 'flex' }}>
              <IconComponent size={16} color="#818cf8" />
            </div>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              {formatTopic(topic)}
            </span>
          </div>

          <div style={{ display: 'flex', gap: '0.4rem', flexWrap: 'wrap' }}>
            <PriorityBadge priority={priority} />
            <RecommendationTypeBadge type={recommendationType} />
          </div>
        </div>

        {/* Title */}
        <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: '#f8fafc', marginBottom: '0.5rem', lineHeight: 1.3 }}>
          {title}
        </h3>

        {/* Short Description */}
        <p style={{ color: '#cbd5e1', fontSize: '0.9rem', lineHeight: 1.5, marginBottom: '1rem' }}>
          {shortDescription}
        </p>

        {/* Rationale Callout */}
        {rationale && (
          <div
            style={{
              background: 'rgba(15, 23, 42, 0.5)',
              borderLeft: '3px solid var(--primary-accent)',
              padding: '0.75rem 1rem',
              borderRadius: '0 8px 8px 0',
              marginBottom: '1rem',
            }}
          >
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: '#818cf8', display: 'block', marginBottom: '0.2rem' }}>
              Why this is recommended:
            </span>
            <p style={{ color: '#cbd5e1', fontSize: '0.85rem', margin: 0, lineHeight: 1.4 }}>
              {rationale}
            </p>
          </div>
        )}

        {/* Learning Objective */}
        {learningObjective && (
          <div style={{ marginBottom: '1.25rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: '#64748b', display: 'block', marginBottom: '0.2rem' }}>
              Learning Objective:
            </span>
            <p style={{ color: '#94a3b8', fontSize: '0.85rem', margin: 0, lineHeight: 1.4 }}>
              {learningObjective}
            </p>
          </div>
        )}
      </div>

      {/* Footer Info & Action */}
      <div style={{ borderTop: '1px solid var(--border-color)', pt: '1rem', paddingTop: '1rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', fontSize: '0.8rem', color: '#94a3b8' }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Clock size={14} />
            {estimatedDurationMinutes || 15} min
          </span>
          {difficulty && (
            <span style={{ background: 'rgba(255, 255, 255, 0.05)', padding: '0.2rem 0.5rem', borderRadius: '4px', textTransform: 'capitalize' }}>
              {difficulty.toLowerCase()}
            </span>
          )}
        </div>

        <Link
          to={`/education/content/${contentId}`}
          className="btn-primary"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.3rem',
            padding: '0.5rem 1rem',
            fontSize: '0.85rem',
            textDecoration: 'none',
          }}
        >
          <span>Learn More</span>
          <ChevronRight size={16} />
        </Link>
      </div>
    </div>
  );
}
