package com.scriptorium.service;

import com.scriptorium.domain.ManuscriptNode;
import com.scriptorium.domain.relationships.*;
import com.scriptorium.dto.ClusterDTO;
import com.scriptorium.dto.EdgeDTO;
import com.scriptorium.dto.GraphPayload;
import com.scriptorium.dto.NodeDTO;
import com.scriptorium.repository.ClusterRepository;
import com.scriptorium.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GraphService {

    private final ManuscriptRepository manuscriptRepository;
    private final ClusterRepository clusterRepository;
    private final ManuscriptService manuscriptService;

    // ── Public APIs ───────────────────────────────────────────────────────────

    public List<ClusterDTO> getAllClusters() {
        return clusterRepository.findAllWithPublishedManuscripts()
                .stream()
                .map(c -> ClusterDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .era(c.getEra())
                        .tradition(c.getTradition())
                        .build())
                .toList();
    }

    /**
     * Returns the full graph for a cluster:
     * all published manuscripts in the cluster plus all relationships between them.
     *
     * Cypher (executed via repository):
     *   MATCH (c:Cluster {id: $clusterId})<-[:BELONGS_TO]-(m:Manuscript)
     *   WHERE m.isPublished = true
     *   OPTIONAL MATCH (m)-[r]-(connected:Manuscript)
     *   WHERE connected.isPublished = true
     *   RETURN m, collect(r), collect(connected)
     */
    public GraphPayload getGraphForCluster(String clusterId) {
        // Verify cluster exists
        clusterRepository.findById(clusterId)
                .orElseThrow(() -> new NoSuchElementException("Cluster not found: " + clusterId));

        List<ManuscriptNode> nodes = manuscriptRepository.findPublishedByClusterId(clusterId);
        return buildPayload(nodes);
    }

    /**
     * Returns a depth-limited graph starting from a given manuscript.
     *
     * Cypher (executed via repository):
     *   MATCH path = (start:Manuscript {id: $id})-[*1..$depth]-(connected:Manuscript)
     *   WHERE connected.isPublished = true
     *   RETURN nodes(path), relationships(path)
     */
    public GraphPayload getGraphFromManuscript(String manuscriptId, int depth) {
        if (depth < 1 || depth > 5) {
            throw new IllegalArgumentException("Depth must be between 1 and 5.");
        }
        // Include the start node itself
        ManuscriptNode start = manuscriptRepository.findById(manuscriptId)
                .orElseThrow(() -> new NoSuchElementException("Manuscript not found: " + manuscriptId));

        List<ManuscriptNode> reachable = manuscriptRepository.findReachableFromId(manuscriptId, depth);

        // Build a deduplicated set that includes the start node
        Map<String, ManuscriptNode> nodeMap = new LinkedHashMap<>();
        if (Boolean.TRUE.equals(start.getIsPublished())) {
            nodeMap.put(start.getId(), start);
        }
        reachable.forEach(n -> nodeMap.put(n.getId(), n));

        return buildPayload(new ArrayList<>(nodeMap.values()));
    }

    // ── Graph builder ─────────────────────────────────────────────────────────

    /**
     * Walks the in-memory relationship collections on each ManuscriptNode and
     * emits one EdgeDTO per relationship whose target is also in the node set.
     */
    private GraphPayload buildPayload(List<ManuscriptNode> nodes) {
        // Index by id for fast target lookup
        Set<String> nodeIds = new HashSet<>();
        List<NodeDTO> nodeDTOs = new ArrayList<>();

        for (ManuscriptNode node : nodes) {
            nodeDTOs.add(manuscriptService.toNodeDTO(node));
            nodeIds.add(node.getId());
        }

        List<EdgeDTO> edges = new ArrayList<>();

        for (ManuscriptNode node : nodes) {
            extractEdges(node, nodeIds, edges);
        }

        return GraphPayload.builder()
                .nodes(nodeDTOs)
                .edges(edges)
                .build();
    }

    private void extractEdges(ManuscriptNode node, Set<String> nodeIds, List<EdgeDTO> edges) {
        String sourceId = node.getId();

        for (TranslatedFrom r : node.getTranslatedFrom()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "TRANSLATED_FROM", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
        for (CopiedFrom r : node.getCopiedFrom()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "COPIED_FROM", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
        for (CommentaryOn r : node.getCommentaryOn()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "COMMENTARY_ON", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
        for (DerivedFrom r : node.getDerivedFrom()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "DERIVED_FROM", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
        for (ParallelTradition r : node.getParallelTradition()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "PARALLEL_TRADITION", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
        for (InfluencedBy r : node.getInfluencedBy()) {
            if (r.getTarget() != null && nodeIds.contains(r.getTarget().getId())) {
                edges.add(buildEdge(r.getId(), sourceId, r.getTarget().getId(),
                        "INFLUENCED_BY", r.getConfidence(), r.getDescription(), r.getScholarlySource()));
            }
        }
    }

    private EdgeDTO buildEdge(Long relId, String source, String target,
                               String type, String confidence,
                               String description, String scholarlySource) {
        return EdgeDTO.builder()
                .id(relId != null ? relId.toString() : source + "-" + type + "-" + target)
                .source(source)
                .target(target)
                .type(type)
                .confidence(confidence)
                .description(description)
                .scholarlySource(scholarlySource)
                .build();
    }
}
