import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Sparkles, BookOpen, Award, CheckCircle } from 'lucide-react';

export default function Navbar() {
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="glass-card" style={{ padding: '1rem 2rem', marginBottom: '2rem', display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderRadius: 'var(--radius-lg)' }}>
      <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', textDecoration: 'none', color: '#f8fafc' }}>
        <div style={{ background: 'var(--primary-gradient)', padding: '0.4rem', borderRadius: '10px', display: 'flex' }}>
          <Sparkles size={20} color="#ffffff" />
        </div>
        <div>
          <span style={{ fontWeight: 800, fontSize: '1.1rem', letterSpacing: '-0.02em', background: 'var(--primary-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            FinLit Explainable AI
          </span>
          <span style={{ display: 'block', fontSize: '0.75rem', color: '#94a3b8' }}>
            FINRA NFCS Personalized Education Engine
          </span>
        </div>
      </Link>

      <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
        <Link
          to="/"
          style={{
            padding: '0.5rem 1rem',
            borderRadius: 'var(--radius-md)',
            textDecoration: 'none',
            fontSize: '0.9rem',
            fontWeight: 600,
            color: isActive('/') ? '#ffffff' : '#94a3b8',
            background: isActive('/') ? 'rgba(99, 102, 241, 0.2)' : 'transparent',
            border: isActive('/') ? '1px solid rgba(99, 102, 241, 0.4)' : '1px solid transparent',
          }}
        >
          Overview
        </Link>
        <Link
          to="/take"
          style={{
            padding: '0.5rem 1rem',
            borderRadius: 'var(--radius-md)',
            textDecoration: 'none',
            fontSize: '0.9rem',
            fontWeight: 600,
            color: isActive('/take') ? '#ffffff' : '#94a3b8',
            background: isActive('/take') ? 'rgba(99, 102, 241, 0.2)' : 'transparent',
            border: isActive('/take') ? '1px solid rgba(99, 102, 241, 0.4)' : '1px solid transparent',
          }}
        >
          Take Assessment
        </Link>
      </div>
    </nav>
  );
}
