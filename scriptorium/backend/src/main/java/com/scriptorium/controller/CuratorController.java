package com.scriptorium.controller;

import com.scriptorium.dto.ManuscriptRequest;
import com.scriptorium.dto.NodeDTO;
import com.scriptorium.dto.RelationshipRequest;
import com.scriptorium.service.ManuscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/curator")
@RequiredArgsConstructor
public class CuratorController {

    private final ManuscriptService manuscriptService;

    // POST /api/curator/manuscripts
    @PostMapping("/manuscripts")
    public ResponseEntity<NodeDTO> createManuscript(@Valid @RequestBody ManuscriptRequest request) {
        NodeDTO created = manuscriptService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/curator/manuscripts/:id
    @PutMapping("/manuscripts/{id}")
    public ResponseEntity<NodeDTO> updateManuscript(
            @PathVariable String id,
            @Valid @RequestBody ManuscriptRequest request) {
        return ResponseEntity.ok(manuscriptService.update(id, request));
    }

    // PATCH /api/curator/manuscripts/:id/publish
    @PatchMapping("/manuscripts/{id}/publish")
    public ResponseEntity<NodeDTO> publishManuscript(@PathVariable String id) {
        return ResponseEntity.ok(manuscriptService.publish(id));
    }

    // POST /api/curator/relationships
    @PostMapping("/relationships")
    public ResponseEntity<Void> addRelationship(@Valid @RequestBody RelationshipRequest request) {
        manuscriptService.addRelationship(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
