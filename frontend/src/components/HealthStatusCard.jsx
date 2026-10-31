import React from 'react';
import { Activity, Server, Cpu, Database, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export default function HealthStatusCard({ title, serviceName, data, loading, error, onRefresh }) {
  const isOnline = data && data.status === 'UP';

  return (
    <div className="glass-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            padding: '0.6rem',
            borderRadius: '10px',
            background: 'rgba(99, 102, 241, 0.15)',
            color: '#818cf8',
            display: 'flex'
          }}>
            {serviceName === 'backend' ? <Server size={22} /> : <Cpu size={22} />}
          </div>
          <div>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 700, color: '#f8fafc' }}>{title}</h3>
            <span style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Service: {serviceName}</span>
          </div>
        </div>
        
        <button 
          onClick={onRefresh}
          style={{
            background: 'transparent',
            border: 'none',
            color: '#94a3b8',
            cursor: 'pointer',
            padding: '0.4rem',
            borderRadius: '6px',
            transition: 'color 0.2s'
          }}
          title="Refresh Status"
        >
          <RefreshCw size={16} className={loading ? 'spin' : ''} />
        </button>
      </div>

      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        marginBottom: '1rem',
        padding: '0.6rem 0.8rem',
        borderRadius: '8px',
        background: isOnline ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
        border: `1px solid ${isOnline ? 'rgba(16, 185, 129, 0.2)' : 'rgba(239, 68, 68, 0.2)'}`
      }}>
        <span className={`status-indicator ${isOnline ? 'status-online' : 'status-offline'}`} />
        <span style={{
          fontSize: '0.875rem',
          fontWeight: 600,
          color: isOnline ? '#34d399' : '#f87171'
        }}>
          {loading ? 'Checking connection...' : (isOnline ? 'OPERATIONAL' : 'OFFLINE / UNREACHABLE')}
        </span>
      </div>

      {data && (
        <div style={{ fontSize: '0.85rem', color: '#cbd5e1', display: 'flex', flexDirection: 'column', gap: '0.4rem' }}>
          <div><strong>Version:</strong> {data.version || 'N/A'}</div>
          <div><strong>Timestamp:</strong> {new Date(data.timestamp).toLocaleTimeString()}</div>
          
          {serviceName === 'backend' && data.database && (
            <div style={{ marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <Database size={14} color="#818cf8" />
                <span><strong>PostgreSQL DB:</strong> {data.database.status}</span>
              </div>
            </div>
          )}

          {serviceName === 'ml-service' && data.models_status && (
            <div style={{ marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid rgba(255, 255, 255, 0.08)' }}>
              <div><strong>Models Pipeline Status:</strong> {data.models_status.trained ? 'Trained Artifacts Loaded' : 'Initialized (Awaiting Dataset)'}</div>
              <div><strong>Active Model Architecture:</strong> {data.models_status.active_model || (data.models_status.available_models ? data.models_status.available_models.join(', ') : 'LightGBM_TOP75_ClassWeighted')}</div>
            </div>
          )}
        </div>
      )}

      {error && !data && (
        <div style={{ fontSize: '0.8rem', color: '#f87171', marginTop: '0.5rem' }}>
          Unable to establish connection to endpoint. Ensure server process is running.
        </div>
      )}
    </div>
  );
}
