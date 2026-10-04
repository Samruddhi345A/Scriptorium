import { memo } from 'react';
import { Handle, Position, type NodeProps } from '@xyflow/react';
import type { ManuscriptNodeData } from '@/types';

const LANGUAGE_NAMES: Record<string, string> = {
  la: 'Latin',
  fro: 'Old French',
  enm: 'Middle English',
  en: 'English',
  cy: 'Welsh',
  de: 'German',
  fr: 'French',
  it: 'Italian',
  es: 'Spanish',
  ar: 'Arabic',
  el: 'Greek',
  he: 'Hebrew',
};

function NodeCard({ data, selected }: NodeProps<{ data: ManuscriptNodeData }>) {
  const { manuscript } = data as unknown as ManuscriptNodeData;
  const langLabel = LANGUAGE_NAMES[manuscript.language] ?? manuscript.language?.toUpperCase();

  return (
    <div
      style={{
        background: selected ? 'var(--color-node-hover)' : 'var(--color-node-bg)',
        border: `1px solid ${selected ? 'var(--color-primary)' : 'var(--color-border)'}`,
        borderRadius: 'var(--radius)',
        padding: '10px 14px',
        minWidth: 180,
        maxWidth: 220,
        boxShadow: selected
          ? '0 0 0 2px var(--color-primary-dim)'
          : '0 2px 8px rgba(0,0,0,0.4)',
        cursor: 'pointer',
        transition: 'border-color 0.15s, box-shadow 0.15s',
      }}
    >
      {/* React Flow connection handles */}
      <Handle type="target" position={Position.Top} style={{ background: 'var(--color-primary-dim)' }} />
      <Handle type="source" position={Position.Bottom} style={{ background: 'var(--color-primary-dim)' }} />

      {/* Language badge */}
      <div
        style={{
          display: 'inline-block',
          fontSize: '10px',
          fontWeight: 600,
          letterSpacing: '0.05em',
          textTransform: 'uppercase',
          color: 'var(--color-primary)',
          background: 'rgba(201,169,110,0.12)',
          borderRadius: 4,
          padding: '1px 6px',
          marginBottom: 6,
        }}
      >
        {langLabel}
      </div>

      {/* Title */}
      <div
        style={{
          fontFamily: 'var(--font-serif)',
          fontSize: '13px',
          fontWeight: 600,
          color: 'var(--color-text)',
          lineHeight: 1.35,
          marginBottom: 4,
          wordBreak: 'break-word',
        }}
      >
        {manuscript.title}
      </div>

      {/* Approximate date */}
      {manuscript.approximateDate && (
        <div style={{ fontSize: '11px', color: 'var(--color-muted)' }}>
          {manuscript.approximateDate}
        </div>
      )}
    </div>
  );
}

export default memo(NodeCard);
