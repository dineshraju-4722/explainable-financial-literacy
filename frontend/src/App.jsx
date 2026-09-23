import React, { useState, useEffect } from 'react';
import axios from 'axios';
import HealthStatusCard from './components/HealthStatusCard';
import { BookOpen, ShieldCheck, Cpu, Layers, Sparkles, Database, Check } from 'lucide-react';

const BACKEND_HEALTH_URL = import.meta.env.VITE_API_BASE_URL 
  ? `${import.meta.env.VITE_API_BASE_URL}/health`
  : 'http://localhost:8080/api/health';

const ML_HEALTH_URL = import.meta.env.VITE_ML_SERVICE_BASE_URL
  ? `${import.meta.env.VITE_ML_SERVICE_BASE_URL}/health`
  : 'http://localhost:8000/health';

export default function App() {
  const [backendHealth, setBackendHealth] = useState(null);
  const [backendLoading, setBackendLoading] = useState(true);
  const [backendError, setBackendError] = useState(null);

  const [mlHealth, setMlHealth] = useState(null);
  const [mlLoading, setMlLoading] = useState(true);
  const [mlError, setMlError] = useState(null);

  const fetchBackendHealth = async () => {
    setBackendLoading(true);
    setBackendError(null);
    try {
      const res = await axios.get(BACKEND_HEALTH_URL);
      setBackendHealth(res.data);
    } catch (err) {
      setBackendError(err);
      setBackendHealth(null);
    } finally {
      setBackendLoading(false);
    }
  };

  const fetchMlHealth = async () => {
    setMlLoading(true);
    setMlError(null);
    try {
      const res = await axios.get(ML_HEALTH_URL);
      setMlHealth(res.data);
    } catch (err) {
      setMlError(err);
      setMlHealth(null);
    } finally {
      setMlLoading(false);
    }
  };

  useEffect(() => {
    fetchBackendHealth();
    fetchMlHealth();
  }, []);

  return (
    <div className="container">
      {/* Header Banner */}
      <header className="header">
        <div className="title-badge">
          <Sparkles size={16} />
          Academic Research Project & Extension
        </div>
        <h1 className="main-title">
          Explainable Machine Learning-Based Financial Literacy Assessment & Personalized Education System
        </h1>
        <p className="subtitle">
          An interactive, explainable AI platform extending the foundational research of Alex Yue Feng Zhu (2025) 
          <em> Technology in Society</em> (DOI: 10.1016/j.techsoc.2024.102797).
        </p>
      </header>

      {/* System Phase 1 Status Grid */}
      <section>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem' }}>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#f8fafc', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Layers size={20} color="#818cf8" />
            Phase 1 Infrastructure & Health Verification
          </h2>
          <span style={{ fontSize: '0.85rem', color: '#10b981', background: 'rgba(16, 185, 129, 0.1)', padding: '0.3rem 0.8rem', borderRadius: '20px', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
            Phase 1 Skeleton Active
          </span>
        </div>

        <div className="card-grid">
          <HealthStatusCard
            title="Spring Boot Backend Service"
            serviceName="backend"
            data={backendHealth}
            loading={backendLoading}
            error={backendError}
            onRefresh={fetchBackendHealth}
          />

          <HealthStatusCard
            title="Python FastAPI ML Service"
            serviceName="ml-service"
            data={mlHealth}
            loading={mlLoading}
            error={mlError}
            onRefresh={fetchMlHealth}
          />
        </div>
      </section>

      {/* Research Architectural Pillar Summary */}
      <section style={{ marginTop: '3rem' }}>
        <h2 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#f8fafc', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <BookOpen size={20} color="#818cf8" />
          Core Research Extensions Strategy
        </h2>

        <div className="card-grid">
          <div className="glass-card">
            <div style={{ color: '#818cf8', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              1. Weak-Area Detection
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              Categorical knowledge analysis across 10 specific financial domains (Saving, Budgeting, Interest, Compound Interest, Inflation, Debt, Investment, Risk, Insurance, Decision-Making).
            </p>
          </div>

          <div className="glass-card">
            <div style={{ color: '#c084fc', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              2. Personalized Learning Engine
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              Deterministic rule-based educational recommendations linking detected weak areas directly to structured lessons, practical examples, quizzes, and reassessment tracking.
            </p>
          </div>

          <div className="glass-card">
            <div style={{ color: '#f472b6', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              3. Humanized Explainable AI
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              SHAP feature-attribution explanations translated into clear, non-intimidating natural language insights for students, researchers, and administrators.
            </p>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <p>Explainable ML-Based Financial Literacy Assessment System &copy; 2026</p>
        <p style={{ marginTop: '0.25rem', fontSize: '0.8rem', color: '#64748b' }}>
          Built with Spring Boot 3.x (Java 21), Python 3.11 (FastAPI/LightGBM/SHAP), React + Vite, and PostgreSQL 16.
        </p>
      </footer>
    </div>
  );
}
