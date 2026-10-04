package com.scriptorium.controller;

import com.scriptorium.dto.ClusterDTO;
import com.scriptorium.dto.GraphPayload;
import com.scriptorium.dto.NodeDTO;
import com.scriptorium.service.GraphService;
import com.scriptorium.service.ManuscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicManuscriptController {

    private final GraphService graphService;
    private final ManuscriptService manuscriptService;

    // GET /api/public/clusters
    @GetMapping("/clusters")
    public ResponseEntity<List<ClusterDTO>> getAllClusters() {
        return ResponseEntity.ok(graphService.getAllClusters());
    }

    // GET /api/public/clusters/:id/graph
    @GetMapping("/clusters/{id}/graph")
    public ResponseEntity<GraphPayload> getClusterGraph(@PathVariable String id) {
        return ResponseEntity.ok(graphService.getGraphForCluster(id));
    }

    // GET /api/public/manuscripts/:id
    @GetMapping("/manuscripts/{id}")
    public ResponseEntity<NodeDTO> getManuscript(@PathVariable String id) {
        return ResponseEntity.ok(manuscriptService.getPublishedById(id));
    }

    // GET /api/public/manuscripts/:id/neighbors
    @GetMapping("/manuscripts/{id}/neighbors")
    public ResponseEntity<List<NodeDTO>> getNeighbors(@PathVariable String id) {
        return ResponseEntity.ok(manuscriptService.getNeighbours(id));
    }

    // GET /api/public/search?q=&language=&era=
    @GetMapping("/search")
    public ResponseEntity<List<NodeDTO>> search(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String era) {
        return ResponseEntity.ok(manuscriptService.search(q, language));
    }
}
