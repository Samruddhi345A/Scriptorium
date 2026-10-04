import { useGraphStore } from '@/store/useGraphStore';
import { REL_LABELS } from '@/types';

const FIELD_LABEL: Record<string, string> = {
  language: 'Language',
  approximateDate: 'Date',
  originRegion: 'Origin',
  author: 'Author',
  script: 'Script',
  holdingInstitution: 'Held at',
  shelfmark: 'Shelfmark',
  condition: 'Condition',
};

export default function SidePanel() {
  const { selectedNode, setSelectedNode, getEdgesForNode } = useGraphStore();

  if (!selectedNode) return null;

  const edges = getEdgesForNode(selectedNode.id);

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={() => setSelectedNode(null)}
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 49,
          background: 'transparent',
        }}
        aria-hidden="true"
      />

      {/* Panel */}
      <aside
        role="complementary"
        aria-label="Manuscript details"
        style={{
          position: 'fixed',
          top: 0,
          right: 0,
          bottom: 0,
          width: 380,
          zIndex: 50,
          background: 'var(--color-surface)',
          borderLeft: '1px solid var(--color-border)',
          display: 'flex',
          flexDirection: 'column',
          overflowY: 'auto',
          boxShadow: '-4px 0 24px rgba(0,0,0,0.5)',
          animation: 'slideIn 0.2s ease',
        }}
      >
        <style>{`
          @keyframes slideIn {
            from { transform: translateX(100%); opacity: 0; }
            to   { transform: translateX(0);    opacity: 1; }
          }
        `}</style>

        {/* Header */}
        <div
          style={{
            padding: '20px 20px 16px',
            borderBottom: '1px solid var(--color-border)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            gap: 12,
          }}
        >
          <h2
            style={{
              fontFamily: 'var(--font-serif)',
              fontSize: '17px',
              color: 'var(--color-text)',
              lineHeight: 1.35,
            }}
          >
            {selectedNode.title}
          </h2>
          <button
            onClick={() => setSelectedNode(null)}
            aria-label="Close panel"
            style={{
              flexShrink: 0,
              background: 'none',
              border: '1px solid var(--color-border)',
              borderRadius: 4,
              color: 'var(--color-muted)',
              fontSize: '14px',
              padding: '2px 8px',
              lineHeight: 1.5,
            }}
          >
            ✕
          </button>
        </div>

        {/* Metadata fields */}
        <dl style={{ padding: '16px 20px', display: 'grid', gap: 10 }}>
          {Object.entries(FIELD_LABEL).map(([key, label]) => {
            const value = selectedNode[key as keyof typeof selectedNode] as string;
            if (!value) return null;
            return (
              <div key={key} style={{ display: 'grid', gridTemplateColumns: '110px 1fr', gap: 8 }}>
                <dt style={{ fontSize: '11px', fontWeight: 600, color: 'var(--color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', paddingTop: 2 }}>
                  {label}
                </dt>
                <dd style={{ fontSize: '13px', color: 'var(--color-text)' }}>{value}</dd>
              </div>
            );
          })}
        </dl>

        {/* Summary */}
        {selectedNode.summary && (
          <div style={{ padding: '0 20px 20px' }}>
            <h3
              style={{
                fontSize: '11px',
                fontWeight: 600,
                color: 'var(--color-muted)',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                marginBottom: 8,
              }}
            >
              Summary
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--color-text)', lineHeight: 1.6 }}>
              {selectedNode.summary}
            </p>
          </div>
        )}

        {/* Relationships */}
        {edges.length > 0 && (
          <div
            style={{
              padding: '16px 20px 24px',
              borderTop: '1px solid var(--color-border)',
            }}
          >
            <h3
              style={{
                fontSize: '11px',
                fontWeight: 600,
                color: 'var(--color-muted)',
                textTransform: 'uppercase',
                letterSpacing: '0.05em',
                marginBottom: 12,
              }}
            >
              Connections ({edges.length})
            </h3>
            <ul style={{ listStyle: 'none', display: 'grid', gap: 10 }}>
              {edges.map((edge) => (
                <li
                  key={edge.id}
                  style={{
                    background: 'rgba(255,255,255,0.03)',
                    border: '1px solid var(--color-border)',
                    borderRadius: 6,
                    padding: '8px 12px',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                    <span
                      style={{
                        fontSize: '11px',
                        fontWeight: 600,
                        color: 'var(--color-primary)',
                        textTransform: 'uppercase',
                        letterSpacing: '0.04em',
                      }}
                    >
                      {REL_LABELS[edge.type] ?? edge.type}
                    </span>
                    <ConfidenceBadge confidence={edge.confidence} />
                  </div>
                  {edge.description && (
                    <p style={{ fontSize: '12px', color: 'var(--color-text)', lineHeight: 1.5 }}>
                      {edge.description}
                    </p>
                  )}
                  {edge.scholarlySource && (
                    <p style={{ fontSize: '11px', color: 'var(--color-muted)', marginTop: 4, fontStyle: 'italic' }}>
                      {edge.scholarlySource}
                    </p>
                  )}
                </li>
              ))}
            </ul>
          </div>
        )}
      </aside>
    </>
  );
}

function ConfidenceBadge({ confidence }: { confidence: string }) {
  const styles: Record<string, { color: string; bg: string }> = {
    established: { color: '#6fcf97', bg: 'rgba(111,207,151,0.12)' },
    probable:    { color: '#7a8fbb', bg: 'rgba(122,143,187,0.12)' },
    speculative: { color: '#b0906a', bg: 'rgba(176,144,106,0.12)' },
  };
  const s = styles[confidence] ?? styles.speculative;
  return (
    <span
      style={{
        fontSize: '10px',
        fontWeight: 700,
        textTransform: 'uppercase',
        letterSpacing: '0.06em',
        color: s.color,
        background: s.bg,
        borderRadius: 4,
        padding: '1px 6px',
      }}
    >
      {confidence}
    </span>
  );
}
