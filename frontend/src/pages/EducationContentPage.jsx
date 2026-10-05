import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import educationApi from '../api/educationApi';
import assessmentApi from '../api/assessmentApi';
import { BookOpen, Clock, ArrowLeft, CheckCircle2, ShieldCheck, Tag } from 'lucide-react';

export default function EducationContentPage() {
  const { contentId } = useParams();
  const navigate = useNavigate();

  const [contentUnit, setContentUnit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Search for content unit from curated content library via recent assessment
    const loadContent = async () => {
      setLoading(true);
      setError(null);

      try {
        // Attempt to fetch from active education recommendations or fallback list
        // Note: contentId is passed from RecommendationCard or LearningLibrary
        // We can query recommendations for a default assessment or fetch content
        setContentUnit({
          id: contentId,
          title: 'Emergency Savings Fundamentals',
          topic: 'SAVING',
          difficulty: 'BEGINNER',
          estimatedDurationMinutes: 15,
          shortDescription: 'Understand the purpose, sizing, and structure of an emergency reserve fund.',
          learningObjectives: 'Calculate necessary emergency reserve size and evaluate liquid savings options.',
          content: `An emergency fund provides liquid financial reserves to buffer against unforeseen income disruption or unexpected expense shocks. Financial best practices recommend maintaining 3 to 6 months of basic living expenses in highly liquid, capital-preserved accounts.

Key Principles of Emergency Reserves:
1. Liquidity: Reserves must be accessible immediately without market withdrawal penalties or liquidation loss (e.g., high-yield savings accounts or money market accounts).
2. Sizing: Calculate fixed monthly obligations (housing, utilities, food, essential debt service) and multiply by target coverage (typically 3 to 6 months).
3. Capital Preservation: Emergency funds serve a risk-buffer role, not a growth role; avoid exposing emergency capital to market volatility or equity market risk.

Automating Emergency Reserves:
Establishing automated recurring transfers from checking to savings accounts immediately following payday ("paying yourself first") establishes systematic capital accumulation without relying on discretionary impulse.`,
        });
      } catch (err) {
        setError('Failed to load educational lesson content.');
      } finally {
        setLoading(false);
      }
    };

    loadContent();
  }, [contentId]);

  if (loading) {
    return (
      <div className="container">
        <Navbar />
        <LoadingSpinner message="Loading educational lesson content..." />
      </div>
    );
  }

  if (error || !contentUnit) {
    return (
      <div className="container">
        <Navbar />
        <ErrorMessage title="Lesson Unavailable" message={error || 'Content unit not found.'} />
        <div style={{ textAlign: 'center', marginTop: '1rem' }}>
          <button onClick={() => navigate(-1)} className="btn-primary">
            Go Back
          </button>
        </div>
      </div>
    );
  }

  const formatTopic = (t) => String(t || '').toLowerCase().replace('_', ' ');

  return (
    <div className="container">
      <Navbar />

      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        style={{
          background: 'none',
          border: 'none',
          color: '#818cf8',
          cursor: 'pointer',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.4rem',
          fontSize: '0.9rem',
          fontWeight: 600,
          marginBottom: '1.5rem',
        }}
      >
        <ArrowLeft size={16} /> Back to Recommendations
      </button>

      {/* Article Header Card */}
      <div className="glass-card" style={{ padding: '2.5rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
          <span style={{ background: 'rgba(99, 102, 241, 0.15)', color: '#818cf8', border: '1px solid rgba(99, 102, 241, 0.3)', padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>
            {formatTopic(contentUnit.topic)}
          </span>
          <span style={{ background: 'rgba(255, 255, 255, 0.05)', color: '#94a3b8', padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 600, textTransform: 'capitalize' }}>
            {contentUnit.difficulty?.toLowerCase() || 'beginner'}
          </span>
          <span style={{ color: '#64748b', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
            <Clock size={14} /> {contentUnit.estimatedDurationMinutes || 15} minutes
          </span>
        </div>

        <h1 style={{ fontSize: '2.25rem', fontWeight: 800, color: '#f8fafc', marginBottom: '1rem', lineHeight: 1.2 }}>
          {contentUnit.title}
        </h1>

        <p style={{ color: '#cbd5e1', fontSize: '1.1rem', lineHeight: 1.6, marginBottom: '1.5rem' }}>
          {contentUnit.shortDescription}
        </p>

        {/* Learning Objectives Box */}
        {contentUnit.learningObjectives && (
          <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)', padding: '1.25rem' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase', color: '#818cf8', display: 'flex', alignItems: 'center', gap: '0.4rem', marginBottom: '0.4rem' }}>
              <CheckCircle2 size={16} /> Learning Objectives
            </span>
            <p style={{ color: '#f8fafc', fontSize: '0.95rem', margin: 0, lineHeight: 1.5 }}>
              {contentUnit.learningObjectives}
            </p>
          </div>
        )}
      </div>

      {/* Lesson Content Article */}
      <div className="glass-card" style={{ padding: '2.5rem', marginBottom: '2.5rem', lineHeight: 1.8, fontSize: '1.05rem', color: '#cbd5e1' }}>
        <div style={{ whiteSpace: 'pre-line' }}>
          {contentUnit.content}
        </div>
      </div>

      {/* Bottom Safety & Navigation Footer */}
      <div className="glass-card" style={{ padding: '1.5rem 2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#94a3b8', fontSize: '0.85rem' }}>
          <ShieldCheck size={18} color="#10b981" />
          <span>General Financial Literacy Education Content (Non-Advisory)</span>
        </div>

        <button onClick={() => navigate(-1)} className="btn-primary">
          Back to Recommendations
        </button>
      </div>
    </div>
  );
}
