import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/common/Navbar';
import HealthStatusCard from '../components/HealthStatusCard';
import axios from 'axios';
import { Sparkles, Layers, BookOpen, ArrowRight, Play, Award } from 'lucide-react';

const BACKEND_HEALTH_URL = import.meta.env.VITE_API_BASE_URL 
  ? `${import.meta.env.VITE_API_BASE_URL}/health`
  : 'http://localhost:8080/api/health';

const ML_HEALTH_URL = import.meta.env.VITE_ML_SERVICE_BASE_URL
  ? `${import.meta.env.VITE_ML_SERVICE_BASE_URL}/health`
  : 'http://localhost:8000/health';

export default function HomePage() {
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
      <Navbar />

      {/* Header Banner */}
      <header className="header">
        <div className="title-badge">
          <Sparkles size={16} />
          Academic Research Project & Personalized Platform
        </div>
        <h1 className="main-title">
          Explainable ML Financial Literacy Assessment & Personalized Education System
        </h1>
        <p className="subtitle">
          An interactive, explainable AI platform extending FINRA NFCS microdata research with LightGBM archetype predictions, TreeSHAP feature attributions, and a deterministic personalized education engine.
        </p>

        <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link to="/take" className="btn-primary" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '0.5rem', padding: '0.85rem 1.75rem', fontSize: '1rem' }}>
            <Play size={18} />
            Take Financial Literacy Assessment
          </Link>
        </div>
      </header>

      {/* Infrastructure Verification Section */}
      <section style={{ marginBottom: '3rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', flexWrap: 'wrap', gap: '1rem' }}>
          <h2 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#f8fafc', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Layers size={20} color="#818cf8" />
            Infrastructure & Service Verification
          </h2>
          <span style={{ fontSize: '0.85rem', color: '#10b981', background: 'rgba(16, 185, 129, 0.1)', padding: '0.3rem 0.8rem', borderRadius: '20px', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
            Phase 7 Architecture Operational
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

      {/* Core Architectural Pillars */}
      <section style={{ marginTop: '2rem' }}>
        <h2 style={{ fontSize: '1.35rem', fontWeight: 700, color: '#f8fafc', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <BookOpen size={20} color="#818cf8" />
          System Architectural Pipeline
        </h2>

        <div className="card-grid">
          <div className="glass-card">
            <div style={{ color: '#818cf8', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              1. ML Archetype Classifier
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              Frozen Class-Weighted LightGBM trained on 75 FINRA NFCS predictors predicting 4 financial literacy archetypes (COMPETENT, OVERCONFIDENT, UNDERCONFIDENT, NAIVE).
            </p>
          </div>

          <div className="glass-card">
            <div style={{ color: '#c084fc', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              2. TreeSHAP Explanation API
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              Instance-level TreeSHAP feature attributions quantifying exact model split contributions toward predicted archetypes for research transparency.
            </p>
          </div>

          <div className="glass-card">
            <div style={{ color: '#f472b6', fontWeight: 700, fontSize: '1.1rem', marginBottom: '0.5rem' }}>
              3. Personalized Education Engine
            </div>
            <p style={{ color: '#94a3b8', fontSize: '0.9rem', lineHeight: 1.5 }}>
              Deterministic pedagogical rule engine deriving topic performance, priority tiers (HIGH, MEDIUM, LOW), and curated learning modules.
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
