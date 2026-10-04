import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getClusters } from '@/api/api';
import { useGraphStore } from '@/store/useGraphStore';
import type { ClusterDTO } from '@/types';

export default function Home() {
  const navigate = useNavigate();
  const { clusters, setClusters, loading, setLoading, error, setError } = useGraphStore();

  useEffect(() => {
    if (clusters.length > 0) return; // already loaded
    setLoading(true);
    setError(null);
    getClusters()
      .then(setClusters)
      .catch((err) => setError(err?.response?.data?.message ?? 'Failed to load clusters.'))
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div style={{ minHeight: '100vh', background: 'var(--color-bg)' }}>
      {/* ── Nav ─────────────────────────────────────────────────────── */}
      <nav
        style={{
          borderBottom: '1px solid var(--color-border)',
          padding: '0 32px',
          height: 56,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: 'var(--color-surface)',
        }}
      >
        <span
          style={{
            fontFamily: 'var(--font-serif)',
            fontSize: '20px',
            color: 'var(--color-primary)',
            letterSpacing: '0.04em',
          }}
        >
          Scriptorium
        </span>
        <span style={{ fontSize: '12px', color: 'var(--color-muted)' }}>
          Digital Museum of Historical Manuscripts
        </span>
      </nav>

      {/* ── Hero ─────────────────────────────────────────────────────── */}
      <header
        style={{
          padding: '72px 32px 48px',
          maxWidth: 720,
          margin: '0 auto',
          textAlign: 'center',
        }}
      >
        <h1
          style={{
            fontFamily: 'var(--font-serif)',
            fontSize: '42px',
            color: 'var(--color-text)',
            lineHeight: 1.2,
            marginBottom: 16,
          }}
        >
          Explore the{' '}
          <span style={{ color: 'var(--color-primary)' }}>manuscript graph</span>
        </h1>
        <p style={{ fontSize: '16px', color: 'var(--color-muted)', lineHeight: 1.7 }}>
          Browse interconnected clusters of historical manuscripts. Each node is a version
          of a text; each edge is a scholarly relationship — translation, copying,
          derivation, or influence.
        </p>
      </header>

      {/* ── Cluster grid ─────────────────────────────────────────────── */}
      <main style={{ maxWidth: 1080, margin: '0 auto', padding: '0 32px 80px' }}>
        {loading && (
          <p style={{ textAlign: 'center', color: 'var(--color-muted)', marginTop: 40 }}>
            Loading clusters…
          </p>
        )}

        {error && (
          <div
            role="alert"
            style={{
              background: 'rgba(220,60,60,0.1)',
              border: '1px solid rgba(220,60,60,0.3)',
              borderRadius: 8,
              padding: '12px 16px',
              color: '#e07070',
              marginTop: 24,
            }}
          >
            {error}
          </div>
        )}

        {!loading && !error && clusters.length === 0 && (
          <p style={{ textAlign: 'center', color: 'var(--color-muted)', marginTop: 40 }}>
            No clusters found.
          </p>
        )}

        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
            gap: 20,
            marginTop: 8,
          }}
        >
          {clusters.map((cluster) => (
            <ClusterCard
              key={cluster.id}
              cluster={cluster}
              onClick={() => navigate(`/cluster/${cluster.id}`)}
            />
          ))}
        </div>
      </main>
    </div>
  );
}

// ── Cluster card ──────────────────────────────────────────────────────────────

function ClusterCard({
  cluster,
  onClick,
}: {
  cluster: ClusterDTO;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      style={{
        background: 'var(--color-surface)',
        border: '1px solid var(--color-border)',
        borderRadius: 10,
        padding: '24px',
        textAlign: 'left',
        cursor: 'pointer',
        transition: 'border-color 0.15s, transform 0.1s',
        display: 'flex',
        flexDirection: 'column',
        gap: 10,
      }}
      onMouseEnter={(e) => {
        (e.currentTarget as HTMLButtonElement).style.borderColor = 'var(--color-primary-dim)';
        (e.currentTarget as HTMLButtonElement).style.transform = 'translateY(-2px)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLButtonElement).style.borderColor = 'var(--color-border)';
        (e.currentTarget as HTMLButtonElement).style.transform = 'translateY(0)';
      }}
    >
      {/* Era + tradition badges */}
      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {cluster.era && <Badge label={cluster.era} />}
        {cluster.tradition && <Badge label={cluster.tradition} dim />}
      </div>

      {/* Name */}
      <h2
        style={{
          fontFamily: 'var(--font-serif)',
          fontSize: '18px',
          color: 'var(--color-text)',
          lineHeight: 1.3,
        }}
      >
        {cluster.name}
      </h2>

      {/* Description */}
      {cluster.description && (
        <p
          style={{
            fontSize: '13px',
            color: 'var(--color-muted)',
            lineHeight: 1.6,
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
          }}
        >
          {cluster.description}
        </p>
      )}

      {/* CTA */}
      <span
        style={{
          marginTop: 4,
          fontSize: '12px',
          fontWeight: 600,
          color: 'var(--color-primary)',
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
        }}
      >
        Explore graph →
      </span>
    </button>
  );
}

function Badge({ label, dim }: { label: string; dim?: boolean }) {
  return (
    <span
      style={{
        fontSize: '10px',
        fontWeight: 600,
        letterSpacing: '0.05em',
        textTransform: 'uppercase',
        color: dim ? 'var(--color-muted)' : 'var(--color-primary)',
        background: dim ? 'rgba(255,255,255,0.04)' : 'rgba(201,169,110,0.1)',
        border: `1px solid ${dim ? 'var(--color-border)' : 'rgba(201,169,110,0.25)'}`,
        borderRadius: 4,
        padding: '2px 7px',
      }}
    >
      {label}
    </span>
  );
}
