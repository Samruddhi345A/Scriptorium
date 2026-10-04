// ── Domain types mirroring the backend DTOs ──────────────────────────────────

export interface NodeDTO {
  id: string;
  title: string;
  language: string;
  approximateDate: string;
  summary: string;
  condition: string;
  originRegion: string;
  holdingInstitution: string;
  shelfmark: string;
  author: string;
  script: string;
}

export type Confidence = 'established' | 'probable' | 'speculative';

export type RelationshipType =
  | 'TRANSLATED_FROM'
  | 'COPIED_FROM'
  | 'COMMENTARY_ON'
  | 'DERIVED_FROM'
  | 'PARALLEL_TRADITION'
  | 'INFLUENCED_BY';

export interface EdgeDTO {
  id: string;
  source: string;
  target: string;
  type: RelationshipType;
  confidence: Confidence;
  description: string;
  scholarlySource: string;
}

export interface GraphPayload {
  nodes: NodeDTO[];
  edges: EdgeDTO[];
}

export interface ClusterDTO {
  id: string;
  name: string;
  description: string;
  era: string;
  tradition: string;
}

// ── Auth types ────────────────────────────────────────────────────────────────

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  name: string;
  role: string;
}

// ── React Flow node / edge data ───────────────────────────────────────────────

/** Data payload attached to each React Flow node */
export interface ManuscriptNodeData {
  manuscript: NodeDTO;
}

/** Confidence → React Flow edge style mapping */
export const CONFIDENCE_STYLE: Record<Confidence, React.CSSProperties> = {
  established: { strokeWidth: 2 },
  probable: { strokeWidth: 2, strokeDasharray: '6 3' },
  speculative: { strokeWidth: 1.5, strokeDasharray: '2 4' },
};

/** Relationship type → display label */
export const REL_LABELS: Record<RelationshipType, string> = {
  TRANSLATED_FROM: 'Translated from',
  COPIED_FROM: 'Copied from',
  COMMENTARY_ON: 'Commentary on',
  DERIVED_FROM: 'Derived from',
  PARALLEL_TRADITION: 'Parallel tradition',
  INFLUENCED_BY: 'Influenced by',
};
