import { useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getClusterGraph } from '@/api/api';
import { useGraphStore } from '@/store/useGraphStore';
import ManuscriptGraph from '@/components/ManuscriptGraph';
import SidePanel from '@/components/SidePanel';

export default function ClusterView() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const {
    graphPayload,
    setGraphPayload,
    clusters,
    loading,
    setLoading,
    error,
    setError,
    selectedNode,
  } = useGraphStore();

  const cluster = clusters.find((c) => c.id === id);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    setError(null);
    setGraphPayload({ nodes: [], edges: [] }); // clear previous cluster
    getClusterGraph(id)
      .then(setGraphPayload)
      .catch((err) =>
        setError(err?.response?.data?.message ?? 'Failed to load graph.'),
      )
      .finally(() => setLoading(false));
  }, [id]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        background: 'var(--color-bg)',
      }}
    >
      {/* ── Nav ─────────────────────────────────────────────────────── */}
      <nav
        style={{
          flexShrink: 0,
          borderBottom: '1px solid var(--color-border)',
          padding: '0 20px',
          height: 52,
          display: 'flex',
          alignItems: 'center',
          gap: 16,
          background: 'var(--color-surface)',
          zIndex: 10,
        }}
      >
        <button
          onClick={() => navigate('/')}
          aria-label="Back to clusters"
          style={{
            background: 'none',
            border: '1px solid var(--color-border)',
            borderRadius: 6,
            color: 'var(--color-muted)',
            fontSize: '13px',
            padding: '4px 10px',
            display: 'flex',
            alignItems: 'center',
            gap: 4,
          }}
        >
          ← Back
        </button>

        <span
          style={{
            fontFamily: 'var(--font-serif)',
            fontSize: '16px',
            color: 'var(--color-primary)',
          }}
        >
          Scriptorium
        </span>

        {cluster && (
          <>
            <span style={{ color: 'var(--color-border)' }}>/</span>
            <span
              style={{
                fontFamily: 'var(--font-serif)',
                fontSize: '15px',
                color: 'var(--color-text)',
              }}
            >
              {cluster.name}
            </span>
          </>
        )}

        {/* Node + edge count */}
        {graphPayload && graphPayload.nodes.length > 0 && (
          <div
            style={{
              marginLeft: 'auto',
              display: 'flex',
              gap: 12,
              fontSize: '12px',
              color: 'var(--color-muted)',
            }}
          >
            <span>
              <strong style={{ color: 'var(--color-text)' }}>
                {graphPayload.nodes.length}
              </strong>{' '}
              manuscripts
            </span>
            <span>
              <strong style={{ color: 'var(--color-text)' }}>
                {graphPayload.edges.length}
              </strong>{' '}
              relationships
            </span>
          </div>
        )}
      </nav>

      {/* ── Graph canvas ─────────────────────────────────────────────── */}
      <div style={{ flex: 1, position: 'relative', overflow: 'hidden' }}>
        {loading && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 5,
              background: 'rgba(15,14,17,0.7)',
            }}
          >
            <span style={{ color: 'var(--color-muted)', fontSize: '14px' }}>
              Loading graph…
            </span>
          </div>
        )}

        {error && (
          <div
            role="alert"
            style={{
              position: 'absolute',
              top: 20,
              left: '50%',
              transform: 'translateX(-50%)',
              zIndex: 5,
              background: 'rgba(220,60,60,0.1)',
              border: '1px solid rgba(220,60,60,0.3)',
              borderRadius: 8,
              padding: '12px 20px',
              color: '#e07070',
              fontSize: '13px',
            }}
          >
            {error}
          </div>
        )}

        {!loading && graphPayload && graphPayload.nodes.length === 0 && !error && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <p style={{ color: 'var(--color-muted)' }}>
              No published manuscripts in this cluster yet.
            </p>
          </div>
        )}

        {graphPayload && graphPayload.nodes.length > 0 && (
          <ManuscriptGraph payload={graphPayload} />
        )}
      </div>

      {/* ── Legend ──────────────────────────────────────────────────── */}
      {!selectedNode && (
        <Legend />
      )}

      {/* ── Side panel ──────────────────────────────────────────────── */}
      <SidePanel />
    </div>
  );
}

function Legend() {
  return (
    <div
      aria-label="Edge confidence legend"
      style={{
        position: 'absolute',
        bottom: 60, // above React Flow controls
        left: 12,
        zIndex: 10,
        background: 'var(--color-surface)',
        border: '1px solid var(--color-border)',
        borderRadius: 8,
        padding: '10px 14px',
        display: 'flex',
        flexDirection: 'column',
        gap: 7,
        fontSize: '11px',
        color: 'var(--color-muted)',
      }}
    >
      <span style={{ fontWeight: 700, color: 'var(--color-text)', marginBottom: 2 }}>
        Edge confidence
      </span>
      {[
        { label: 'Established', color: 'var(--color-edge-est)', dash: 'none' },
        { label: 'Probable',    color: 'var(--color-edge-prob)', dash: '6px 3px' },
        { label: 'Speculative', color: 'var(--color-edge-spec)', dash: '2px 5px' },
      ].map(({ label, color, dash }) => (
        <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <svg width="28" height="10" aria-hidden="true">
            <line
              x1="0" y1="5" x2="28" y2="5"
              stroke={color}
              strokeWidth={dash === 'none' ? 2 : 1.5}
              strokeDasharray={dash === 'none' ? undefined : dash}
            />
          </svg>
          <span>{label}</span>
        </div>
      ))}
    </div>
  );
}
