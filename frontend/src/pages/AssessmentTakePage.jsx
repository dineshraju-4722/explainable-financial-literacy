import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import assessmentApi from '../api/assessmentApi';
import { CheckCircle, HelpCircle, ArrowRight, Sparkles } from 'lucide-react';

export default function AssessmentTakePage() {
  const navigate = useNavigate();

  const [userId, setUserId] = useState('3fa85f64-5717-4562-b3fc-2c963f66afa6'); // Default demo user UUID
  const [assessmentId, setAssessmentId] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentStep, setCurrentStep] = useState(0);
  const [selectedOptions, setSelectedOptions] = useState({});

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const initAssessment = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Initialize Assessment
      const assessment = await assessmentApi.createAssessment(userId, 'INITIAL');
      setAssessmentId(assessment.id);

      // 2. Fetch Questions
      const qList = await assessmentApi.getAssessmentQuestions(assessment.id);
      setQuestions(qList);
    } catch (err) {
      console.error('Failed to initialize assessment:', err);
      setError('Failed to initialize assessment. Please ensure backend is running.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    initAssessment();
  }, []);

  const handleOptionSelect = (questionId, optionId) => {
    setSelectedOptions((prev) => ({
      ...prev,
      [questionId]: optionId,
    }));
  };

  const handleNext = async () => {
    const currentQ = questions[currentStep];
    const selectedOpt = selectedOptions[currentQ.id];

    if (!selectedOpt) return;

    try {
      // Submit answer for current question
      await assessmentApi.submitAnswer(assessmentId, currentQ.id, selectedOpt);

      if (currentStep < questions.length - 1) {
        setCurrentStep((prev) => prev + 1);
      } else {
        // Complete assessment on last step
        setSubmitting(true);
        await assessmentApi.completeAssessment(assessmentId);
        navigate(`/assessments/${assessmentId}/result`);
      }
    } catch (err) {
      console.error('Error submitting answer:', err);
      setError('Failed to submit response. Please try again.');
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <Navbar />
        <LoadingSpinner message="Initializing assessment & loading questions from backend..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <Navbar />
        <ErrorMessage title="Assessment Setup Error" message={error} onRetry={initAssessment} />
      </div>
    );
  }

  if (questions.length === 0) {
    return (
      <div className="container">
        <Navbar />
        <div className="glass-card" style={{ padding: '3rem', textAlign: 'center' }}>
          <HelpCircle size={48} color="#818cf8" style={{ marginBottom: '1rem' }} />
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#f8fafc', marginBottom: '0.5rem' }}>No Questions Found</h2>
          <p style={{ color: '#94a3b8', marginBottom: '1.5rem' }}>The assessment engine returned zero active questions.</p>
          <button onClick={initAssessment} className="btn-primary">
            Retry Initialization
          </button>
        </div>
      </div>
    );
  }

  const currentQ = questions[currentStep];
  const isSelected = selectedOptions[currentQ.id];
  const isLast = currentStep === questions.length - 1;

  return (
    <div className="container">
      <Navbar />

      <div style={{ maxWidth: '800px', margin: '0 auto' }}>
        {/* Progress Bar */}
        <div style={{ marginBottom: '2rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: '#94a3b8', marginBottom: '0.5rem' }}>
            <span>Question {currentStep + 1} of {questions.length}</span>
            <span>Category: <strong>{currentQ.category}</strong></span>
          </div>
          <div style={{ width: '100%', height: '8px', background: 'rgba(255, 255, 255, 0.08)', borderRadius: '4px', overflow: 'hidden' }}>
            <div style={{ width: `${((currentStep + 1) / questions.length) * 100}%`, height: '100%', background: 'var(--primary-gradient)', transition: 'width 0.3s ease' }} />
          </div>
        </div>

        {/* Question Card */}
        <div className="glass-card" style={{ padding: '2.5rem', marginBottom: '2rem' }}>
          <div style={{ display: 'inline-flex', padding: '0.3rem 0.8rem', background: 'rgba(99, 102, 241, 0.15)', borderRadius: '20px', color: '#818cf8', fontSize: '0.8rem', fontWeight: 700, marginBottom: '1rem', textTransform: 'uppercase' }}>
            {currentQ.difficulty || 'BEGINNER'}
          </div>

          <h2 style={{ fontSize: '1.4rem', fontWeight: 700, color: '#f8fafc', marginBottom: '1.75rem', lineHeight: 1.4 }}>
            {currentQ.questionText}
          </h2>

          {/* Options */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', marginBottom: '2rem' }}>
            {currentQ.options?.map((opt) => {
              const checked = selectedOptions[currentQ.id] === opt.id;
              return (
                <button
                  key={opt.id}
                  onClick={() => handleOptionSelect(currentQ.id, opt.id)}
                  style={{
                    padding: '1.1rem 1.25rem',
                    borderRadius: 'var(--radius-md)',
                    border: checked ? '2px solid var(--primary-accent)' : '1px solid var(--border-color)',
                    background: checked ? 'rgba(99, 102, 241, 0.15)' : 'rgba(15, 23, 42, 0.6)',
                    color: checked ? '#ffffff' : '#cbd5e1',
                    fontSize: '1rem',
                    fontWeight: checked ? 700 : 500,
                    textAlign: 'left',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justify: 'space-between',
                    transition: 'all 0.2s ease',
                  }}
                >
                  <span>{opt.optionText}</span>
                  <div
                    style={{
                      width: '20px',
                      height: '20px',
                      borderRadius: '50%',
                      border: checked ? '6px solid var(--primary-accent)' : '2px solid #64748b',
                      background: checked ? '#ffffff' : 'transparent',
                    }}
                  />
                </button>
              );
            })}
          </div>

          {/* Actions */}
          <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
            <button
              onClick={handleNext}
              disabled={!isSelected || submitting}
              className="btn-primary"
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '0.5rem',
                opacity: !isSelected || submitting ? 0.5 : 1,
                cursor: !isSelected || submitting ? 'not-allowed' : 'pointer',
              }}
            >
              <span>{submitting ? 'Submitting & Scoring...' : isLast ? 'Complete & Score Assessment' : 'Next Question'}</span>
              <ArrowRight size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
