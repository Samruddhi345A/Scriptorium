import { useCallback, useMemo } from 'react';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type Node,
  type Edge,
  type NodeTypes,
  type EdgeTypes,
  useNodesState,
  useEdgesState,
  ConnectionLineType,
  MarkerType,
  BaseEdge,
  EdgeLabelRenderer,
  getBezierPath,
  type EdgeProps,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import NodeCard from './NodeCard';
import { useGraphStore } from '@/store/useGraphStore';
import type { GraphPayload, ManuscriptNodeData, EdgeDTO, Confidence } from '@/types';
import { REL_LABELS } from '@/types';

// ── Edge style by confidence ──────────────────────────────────────────────────

const EDGE_COLOR: Record<Confidence, string> = {
  established: 'var(--color-edge-est)',
  probable:    'var(--color-edge-prob)',
  speculative: 'var(--color-edge-spec)',
};

const STROKE_DASH: Record<Confidence, string | undefined> = {
  established: undefined,
  probable:    '6 3',
  speculative: '2 5',
};

// ── Custom edge component ─────────────────────────────────────────────────────

function ManuscriptEdge({
  sourceX, sourceY, targetX, targetY,
  sourcePosition, targetPosition,
  data, markerEnd, style,
}: EdgeProps) {
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX, sourceY, sourcePosition,
    targetX, targetY, targetPosition,
  });

  const edgeData = data as unknown as EdgeDTO;
  const confidence = (edgeData?.confidence ?? 'speculative') as Confidence;
  const color      = EDGE_COLOR[confidence];
  const dash       = STROKE_DASH[confidence];

  return (
    <>
      <BaseEdge
        path={edgePath}
        markerEnd={markerEnd}
        style={{
          ...style,
          stroke: color,
          strokeWidth: confidence === 'established' ? 2 : 1.5,
          strokeDasharray: dash,
        }}
      />
      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            background: 'var(--color-surface)',
            border: `1px solid var(--color-border)`,
            borderRadius: 4,
            padding: '1px 6px',
            fontSize: '10px',
            color: color,
            fontWeight: 600,
            letterSpacing: '0.04em',
            pointerEvents: 'none',
            whiteSpace: 'nowrap',
            textTransform: 'uppercase',
          }}
          className="nodrag nopan"
        >
          {REL_LABELS[edgeData?.type] ?? edgeData?.type}
        </div>
      </EdgeLabelRenderer>
    </>
  );
}

// ── Node / edge type registrations ───────────────────────────────────────────

const nodeTypes: NodeTypes = { manuscript: NodeCard as unknown as NodeTypes['manuscript'] };
const edgeTypes: EdgeTypes = { manuscript: ManuscriptEdge };

// ── Layout helper: simple horizontal layered layout ──────────────────────────

function layoutNodes(payload: GraphPayload): { nodes: Node[]; edges: Edge[] } {
  const COL_WIDTH  = 280;
  const ROW_HEIGHT = 140;

  // Build adjacency to determine rough "generation" per node
  const inDegree = new Map<string, number>();
  payload.nodes.forEach((n) => inDegree.set(n.id, 0));
  payload.edges.forEach((e) => {
    inDegree.set(e.target, (inDegree.get(e.target) ?? 0) + 1);
  });

  // Nodes with no incoming edges are generation 0
  const generation = new Map<string, number>();
  const queue: string[] = [];
  payload.nodes.forEach((n) => {
    if ((inDegree.get(n.id) ?? 0) === 0) {
      generation.set(n.id, 0);
      queue.push(n.id);
    }
  });

  // Simple BFS-based generation assignment
  const edgesBySource = new Map<string, string[]>();
  payload.edges.forEach((e) => {
    const list = edgesBySource.get(e.source) ?? [];
    list.push(e.target);
    edgesBySource.set(e.source, list);
  });

  while (queue.length > 0) {
    const id = queue.shift()!;
    const gen = generation.get(id) ?? 0;
    for (const targetId of edgesBySource.get(id) ?? []) {
      if (!generation.has(targetId)) {
        generation.set(targetId, gen + 1);
        queue.push(targetId);
      }
    }
  }

  // Assign remaining nodes a generation
  payload.nodes.forEach((n) => {
    if (!generation.has(n.id)) generation.set(n.id, 0);
  });

  // Group by generation and position
  const byGen = new Map<number, string[]>();
  generation.forEach((gen, id) => {
    const arr = byGen.get(gen) ?? [];
    arr.push(id);
    byGen.set(gen, arr);
  });

  const nodeMap = new Map(payload.nodes.map((n) => [n.id, n]));
  const nodes: Node[] = [];

  byGen.forEach((ids, gen) => {
    ids.forEach((id, i) => {
      const manuscript = nodeMap.get(id)!;
      nodes.push({
        id,
        type: 'manuscript',
        position: {
          x: gen * COL_WIDTH,
          y: i * ROW_HEIGHT - ((ids.length - 1) * ROW_HEIGHT) / 2,
        },
        data: { manuscript } as unknown as Record<string, unknown>,
      });
    });
  });

  const edges: Edge[] = payload.edges.map((e) => ({
    id: e.id,
    source: e.source,
    target: e.target,
    type: 'manuscript',
    data: e as unknown as Record<string, unknown>,
    markerEnd: { type: MarkerType.ArrowClosed, color: EDGE_COLOR[e.confidence as Confidence] },
  }));

  return { nodes, edges };
}

// ── Main component ────────────────────────────────────────────────────────────

interface Props {
  payload: GraphPayload;
}

export default function ManuscriptGraph({ payload }: Props) {
  const { setSelectedNode, getNodeById } = useGraphStore();

  const { nodes: initialNodes, edges: initialEdges } = useMemo(
    () => layoutNodes(payload),
    [payload],
  );

  const [nodes, , onNodesChange] = useNodesState(initialNodes);
  const [edges, , onEdgesChange] = useEdgesState(initialEdges);

  const onNodeClick = useCallback(
    (_: React.MouseEvent, node: Node) => {
      const manuscript = getNodeById(node.id);
      if (manuscript) setSelectedNode(manuscript);
    },
    [getNodeById, setSelectedNode],
  );

  return (
    <div style={{ width: '100%', height: '100%' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onNodeClick={onNodeClick}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        connectionLineType={ConnectionLineType.Bezier}
        fitView
        fitViewOptions={{ padding: 0.3 }}
        minZoom={0.2}
        maxZoom={2}
        style={{ background: 'var(--color-bg)' }}
        proOptions={{ hideAttribution: false }}
      >
        <Background color="var(--color-border)" gap={28} size={1} />
        <Controls
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: 8,
          }}
        />
        <MiniMap
          nodeColor="var(--color-node-bg)"
          maskColor="rgba(15,14,17,0.7)"
          style={{
            background: 'var(--color-surface)',
            border: '1px solid var(--color-border)',
            borderRadius: 8,
          }}
        />
      </ReactFlow>
    </div>
  );
}
