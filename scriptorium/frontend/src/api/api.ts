import axios from 'axios';
import type {
  ClusterDTO,
  GraphPayload,
  LoginRequest,
  LoginResponse,
  NodeDTO,
} from '@/types';

const client = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT from localStorage on every request if present
client.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ── Public endpoints ──────────────────────────────────────────────────────────

export const getClusters = (): Promise<ClusterDTO[]> =>
  client.get<ClusterDTO[]>('/public/clusters').then((r) => r.data);

export const getClusterGraph = (clusterId: string): Promise<GraphPayload> =>
  client
    .get<GraphPayload>(`/public/clusters/${clusterId}/graph`)
    .then((r) => r.data);

export const getManuscript = (id: string): Promise<NodeDTO> =>
  client.get<NodeDTO>(`/public/manuscripts/${id}`).then((r) => r.data);

export const getNeighbors = (id: string): Promise<NodeDTO[]> =>
  client
    .get<NodeDTO[]>(`/public/manuscripts/${id}/neighbors`)
    .then((r) => r.data);

export const searchManuscripts = (
  q: string,
  language?: string,
): Promise<NodeDTO[]> =>
  client
    .get<NodeDTO[]>('/public/search', { params: { q, language } })
    .then((r) => r.data);

// ── Auth endpoints ────────────────────────────────────────────────────────────

export const login = (body: LoginRequest): Promise<LoginResponse> =>
  client.post<LoginResponse>('/auth/login', body).then((r) => r.data);

export const register = (body: {
  email: string;
  name: string;
  institution?: string;
  password: string;
}): Promise<LoginResponse> =>
  client.post<LoginResponse>('/auth/register', body).then((r) => r.data);
