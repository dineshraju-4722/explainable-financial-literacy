import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ErrorMessage from '../components/common/ErrorMessage';
import ArchetypeCard from '../components/education/ArchetypeCard';
import RecommendationCard from '../components/education/RecommendationCard';
import TopicPerformanceTable from '../components/education/TopicPerformanceTable';
import ShapExplanationSection from '../components/education/ShapExplanationSection';
import LearningLibrary from '../components/education/LearningLibrary';
import assessmentApi from '../api/assessmentApi';
import educationApi from '../api/educationApi';
import mlApi from '../api/mlApi';
import { Award, BookOpen, Layers, CheckCircle2, ArrowLeft, RefreshCw } from 'lucide-react';

export default function AssessmentResultPage() {
  const { assessmentId } = useParams();

  const [result, setResult] = useState(null);
  const [education, setEducation] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [explanation, setExplanation] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [educationError, setEducationError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    setEducationError(null);

    try {
      // 1. Fetch Primary Assessment Result
      const resData = await assessmentApi.getAssessmentResult(assessmentId);
      setResult(resData);

      // 2. Fetch Education Recommendations
      try {
        const eduData = await educationApi.getEducationRecommendations(assessmentId);
        setEducation(eduData);
      } catch (eduErr) {
        console.warn('Education recommendations endpoint unavailable:', eduErr);
        setEducationError('Personalized educational recommendations are temporarily unavailable.');
      }

      // 3. Fetch Assessment Analytics for Topic Performance Breakdown
      try {
        const anaData = await assessmentApi.getAssessmentAnalytics(assessmentId);
        setAnalytics(anaData);

        // 4. Optionally Fetch TreeSHAP Explanation if Feature Vector Available
        if (anaData && anaData.featureVector && resData && resData.mlArchetype) {
          try {
            // Flatten feature vector map for ML service call
            const flatFeatures = {};
            if (anaData.featureVector.categoryFeatures) {
              Object.entries(anaData.featureVector.categoryFeatures).forEach(([catKey, metrics]) => {
                flatFeatures[`cat_${catKey}`] = String(metrics.percentage || 0);
              });
            }
            flatFeatures['overall_percentage'] = String(anaData.overallPercentage || resData.percentage || 0);

            const expData = await mlApi.explainArchetype(flatFeatures, resData.mlArchetype, 5);
            if (expData && expData.explanation) {
              setExplanation(expData.explanation);
            }
          } catch (expErr) {
            console.warn('SHAP explanation call optional fallback:', expErr);
          }
        }
      } catch (anaErr) {
        console.warn('Analytics endpoint optional call failed:', anaErr);
      }
    } catch (err) {
      console.error('Failed to load assessment result:', err);
      const status = err.response?.status;
      let msg = 'Failed to load assessment result from backend.';
      if (status === 404) msg = `Assessment not found (ID: ${assessmentId}).`;
      else if (status === 400) msg = 'Cannot view results for an incomplete or unsubmitted assessment.';
      else if (status === 503) msg = 'ML Archetype prediction service is currently unavailable.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (assessmentId) {
      fetchData();
    }
  }, [assessmentId]);

  if (loading) {
    return (
      <div className="container">
        <Navbar />
        <LoadingSpinner message="Calculating assessment results and loading educational profile..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="container">
        <Navbar />
        <ErrorMessage title="Assessment Result Unavailable" message={error} onRetry={fetchData} />
        <div style={{ textAlign: 'center', marginTop: '1.5rem' }}>
          <Link to="/take" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
            <ArrowLeft size={16} /> Take New Assessment
          </Link>
        </div>
      </div>
    );
  }

  const recommendations = education?.recommendations || [];
  const archetype = result?.mlArchetype || education?.archetype || 'COMPETENT';
  const categoryAnalytics = analytics?.categoryAnalytics || [];

  return (
    <div className="container">
      <Navbar />

      {/* Top Breadcrumb / Action Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <span style={{ fontSize: '0.85rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
            Assessment Result & Educational Profile
          </span>
          <h1 style={{ fontSize: '2rem', fontWeight: 800, color: '#f8fafc', margin: 0 }}>
            Financial Literacy Profile
          </h1>
        </div>

        <Link to="/take" style={{ color: '#818cf8', textDecoration: 'none', fontSize: '0.9rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
          <ArrowLeft size={16} /> Start New Assessment
        </Link>
      </div>

      {/* Assessment Summary Stats Grid */}
      <div className="card-grid" style={{ marginBottom: '2rem', marginTop: 0 }}>
        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <span style={{ color: '#94a3b8', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>Overall Score</span>
          <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#f8fafc', marginTop: '0.25rem' }}>
            {result?.percentage !== undefined ? `${result.percentage.toFixed(0)}%` : 'N/A'}
          </div>
          <span style={{ fontSize: '0.85rem', color: '#64748b' }}>
            {result?.correctAnswers} / {result?.totalQuestions} Questions Correct
          </span>
        </div>

        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <span style={{ color: '#94a3b8', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>Proficiency Tier</span>
          <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#818cf8', marginTop: '0.25rem' }}>
            {result?.proficiencyLevel || 'INTERMEDIATE'}
          </div>
          <span style={{ fontSize: '0.85rem', color: '#64748b' }}>Standard Rule Assessment</span>
        </div>

        <div className="glass-card" style={{ padding: '1.5rem' }}>
          <span style={{ color: '#94a3b8', fontSize: '0.8rem', fontWeight: 700, textTransform: 'uppercase' }}>ML Archetype</span>
          <div style={{ fontSize: '2.25rem', fontWeight: 800, color: '#c084fc', marginTop: '0.25rem' }}>
            {archetype}
          </div>
          <span style={{ fontSize: '0.85rem', color: '#64748b' }}>
            LightGBM TOP_75 Classifier
          </span>
        </div>
      </div>

      {/* 1. ML Archetype Profile Section */}
      <div style={{ marginBottom: '2.5rem' }}>
        <ArchetypeCard
          archetype={archetype}
          summaryOverride={education?.archetypeSummary}
          probabilities={result?.mlProbabilities}
        />
      </div>

      {/* 2. Topic Performance Breakdown */}
      {categoryAnalytics.length > 0 && (
        <TopicPerformanceTable categories={categoryAnalytics} />
      )}

      {/* 3. Personalized Educational Recommendations */}
      <section style={{ marginBottom: '3rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <BookOpen size={22} color="#818cf8" />
              <h2 style={{ fontSize: '1.6rem', fontWeight: 800, color: '#f8fafc', margin: 0 }}>
                Recommended Learning Priorities
              </h2>
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.95rem', margin: '0.25rem 0 0 0' }}>
              Prioritized learning modules derived from your objective assessment performance and archetype context.
            </p>
          </div>
        </div>

        {educationError && (
          <ErrorMessage title="Educational Recommendations Notice" message={educationError} />
        )}

        {recommendations.length === 0 ? (
          <div className="glass-card" style={{ padding: '2rem', textAlign: 'center', color: '#94a3b8' }}>
            <p>No additional learning recommendations were generated for this completed assessment.</p>
          </div>
        ) : (
          <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))' }}>
            {recommendations.map((rec) => (
              <RecommendationCard key={rec.recommendationId || rec.contentId} recommendation={rec} />
            ))}
          </div>
        )}
      </section>

      {/* 4. Model Explanation Section (TreeSHAP) */}
      <ShapExplanationSection explanationData={explanation} archetype={archetype} />

      {/* 5. Complete Curated Learning Library */}
      <LearningLibrary recommendations={recommendations} />
    </div>
  );
}
