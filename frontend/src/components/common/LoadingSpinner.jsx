import React from 'react';

export default function LoadingSpinner({ message = 'Loading assessment results...' }) {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '4rem 2rem',
        textAlign: 'center',
      }}
      role="status"
      aria-live="polite"
    >
      <div
        style={{
          width: '48px',
          height: '48px',
          border: '4px solid rgba(99, 102, 241, 0.2)',
          borderTop: '4px solid #818cf8',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite',
          marginBottom: '1.25rem',
        }}
      />
      <p style={{ color: '#94a3b8', fontSize: '1rem', fontWeight: 500 }}>{message}</p>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
