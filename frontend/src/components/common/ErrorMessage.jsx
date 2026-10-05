import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

export default function ErrorMessage({ title = 'Error Loading Data', message, onRetry }) {
  return (
    <div
      className="glass-card"
      style={{
        padding: '2rem',
        textAlign: 'center',
        borderColor: 'rgba(239, 68, 68, 0.3)',
        background: 'rgba(239, 68, 68, 0.05)',
        margin: '2rem 0',
      }}
      role="alert"
    >
      <div style={{ display: 'inline-flex', padding: '0.75rem', background: 'rgba(239, 68, 68, 0.15)', borderRadius: '50%', marginBottom: '1rem' }}>
        <AlertTriangle size={28} color="#ef4444" />
      </div>
      <h3 style={{ color: '#f8fafc', fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem' }}>{title}</h3>
      <p style={{ color: '#94a3b8', fontSize: '0.95rem', maxWidth: '600px', margin: '0 auto 1.5rem', lineHeight: 1.5 }}>
        {message || 'An unexpected error occurred while communicating with the backend service.'}
      </p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="btn-primary"
          style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}
        >
          <RefreshCw size={16} />
          Retry Request
        </button>
      )}
    </div>
  );
}
