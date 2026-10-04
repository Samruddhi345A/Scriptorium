import { create } from 'zustand';
import type { NodeDTO, EdgeDTO, ClusterDTO, GraphPayload } from '@/types';

interface GraphState {
  // Cluster listing
  clusters: ClusterDTO[];
  setClusters: (clusters: ClusterDTO[]) => void;

  // Active graph data (nodes + edges for the current cluster/view)
  graphPayload: GraphPayload | null;
  setGraphPayload: (payload: GraphPayload) => void;

  // The manuscript currently selected in the side panel (null = panel closed)
  selectedNode: NodeDTO | null;
  setSelectedNode: (node: NodeDTO | null) => void;

  // Loading / error state
  loading: boolean;
  setLoading: (loading: boolean) => void;

  error: string | null;
  setError: (error: string | null) => void;

  // Derived helpers
  getNodeById: (id: string) => NodeDTO | undefined;
  getEdgesForNode: (id: string) => EdgeDTO[];
}

export const useGraphStore = create<GraphState>((set, get) => ({
  clusters: [],
  setClusters: (clusters) => set({ clusters }),

  graphPayload: null,
  setGraphPayload: (graphPayload) => set({ graphPayload }),

  selectedNode: null,
  setSelectedNode: (selectedNode) => set({ selectedNode }),

  loading: false,
  setLoading: (loading) => set({ loading }),

  error: null,
  setError: (error) => set({ error }),

  getNodeById: (id) => get().graphPayload?.nodes.find((n) => n.id === id),

  getEdgesForNode: (id) =>
    (get().graphPayload?.edges ?? []).filter(
      (e) => e.source === id || e.target === id,
    ),
}));
