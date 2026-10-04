package com.scriptorium.service;

import com.scriptorium.domain.ClusterNode;
import com.scriptorium.domain.ManuscriptNode;
import com.scriptorium.domain.relationships.*;
import com.scriptorium.dto.ManuscriptRequest;
import com.scriptorium.dto.NodeDTO;
import com.scriptorium.dto.RelationshipRequest;
import com.scriptorium.repository.ClusterRepository;
import com.scriptorium.repository.ManuscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ManuscriptService {

    private final ManuscriptRepository manuscriptRepository;
    private final ClusterRepository clusterRepository;

    // ── Public ────────────────────────────────────────────────────────────────

    public NodeDTO getPublishedById(String id) {
        ManuscriptNode node = manuscriptRepository.findByIdAndIsPublishedTrue(id)
                .orElseThrow(() -> new NoSuchElementException("Manuscript not found: " + id));
        return toNodeDTO(node);
    }

    public List<NodeDTO> getNeighbours(String id) {
        ManuscriptNode node = manuscriptRepository.findWithNeighbours(id)
                .orElseThrow(() -> new NoSuchElementException("Manuscript not found: " + id));

        // Collect all directly related nodes
        return collectAllRelated(node).stream()
                .map(this::toNodeDTO)
                .toList();
    }

    public List<NodeDTO> search(String query, String language) {
        String lang = (language != null && language.isBlank()) ? null : language;
        return manuscriptRepository.search(query, lang)
                .stream()
                .map(this::toNodeDTO)
                .toList();
    }

    // ── Curator ───────────────────────────────────────────────────────────────

    @Transactional
    public NodeDTO create(ManuscriptRequest request) {
        ManuscriptNode node = new ManuscriptNode();
        applyRequest(node, request);
        node.setCreatedAt(LocalDateTime.now());
        node.setIsPublished(false);

        if (request.getClusterId() != null) {
            ClusterNode cluster = clusterRepository.findById(request.getClusterId())
                    .orElseThrow(() -> new NoSuchElementException("Cluster not found: " + request.getClusterId()));
            node.setCluster(cluster);
        }

        return toNodeDTO(manuscriptRepository.save(node));
    }

    @Transactional
    public NodeDTO update(String id, ManuscriptRequest request) {
        ManuscriptNode node = manuscriptRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Manuscript not found: " + id));
        applyRequest(node, request);

        if (request.getClusterId() != null) {
            ClusterNode cluster = clusterRepository.findById(request.getClusterId())
                    .orElseThrow(() -> new NoSuchElementException("Cluster not found: " + request.getClusterId()));
            node.setCluster(cluster);
        }

        return toNodeDTO(manuscriptRepository.save(node));
    }

    @Transactional
    public NodeDTO publish(String id) {
        ManuscriptNode node = manuscriptRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Manuscript not found: " + id));
        node.setIsPublished(true);
        return toNodeDTO(manuscriptRepository.save(node));
    }

    @Transactional
    public void addRelationship(RelationshipRequest request) {
        ManuscriptNode source = manuscriptRepository.findById(request.getSourceId())
                .orElseThrow(() -> new NoSuchElementException("Source manuscript not found: " + request.getSourceId()));
        ManuscriptNode target = manuscriptRepository.findById(request.getTargetId())
                .orElseThrow(() -> new NoSuchElementException("Target manuscript not found: " + request.getTargetId()));

        switch (request.getType()) {
            case "TRANSLATED_FROM" -> {
                TranslatedFrom rel = new TranslatedFrom();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getTranslatedFrom().add(rel);
            }
            case "COPIED_FROM" -> {
                CopiedFrom rel = new CopiedFrom();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getCopiedFrom().add(rel);
            }
            case "COMMENTARY_ON" -> {
                CommentaryOn rel = new CommentaryOn();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getCommentaryOn().add(rel);
            }
            case "DERIVED_FROM" -> {
                DerivedFrom rel = new DerivedFrom();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getDerivedFrom().add(rel);
            }
            case "PARALLEL_TRADITION" -> {
                ParallelTradition rel = new ParallelTradition();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getParallelTradition().add(rel);
            }
            case "INFLUENCED_BY" -> {
                InfluencedBy rel = new InfluencedBy();
                rel.setTarget(target);
                rel.setConfidence(request.getConfidence());
                rel.setDescription(request.getDescription());
                rel.setScholarlySource(request.getScholarlySource());
                source.getInfluencedBy().add(rel);
            }
            default -> throw new IllegalArgumentException("Unknown relationship type: " + request.getType());
        }

        manuscriptRepository.save(source);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyRequest(ManuscriptNode node, ManuscriptRequest req) {
        node.setTitle(req.getTitle());
        node.setOriginalTitle(req.getOriginalTitle());
        node.setLanguage(req.getLanguage());
        node.setScript(req.getScript());
        node.setApproximateDate(req.getApproximateDate());
        node.setDateLower(req.getDateLower());
        node.setDateUpper(req.getDateUpper());
        node.setOriginRegion(req.getOriginRegion());
        node.setScribe(req.getScribe());
        node.setAuthor(req.getAuthor());
        node.setHoldingInstitution(req.getHoldingInstitution());
        node.setShelfmark(req.getShelfmark());
        node.setCondition(req.getCondition());
        node.setSummary(req.getSummary());
        node.setContentUrl(req.getContentUrl());
        node.setWordCount(req.getWordCount());
    }

    public NodeDTO toNodeDTO(ManuscriptNode node) {
        return NodeDTO.builder()
                .id(node.getId())
                .title(node.getTitle())
                .language(node.getLanguage())
                .approximateDate(node.getApproximateDate())
                .summary(node.getSummary())
                .condition(node.getCondition())
                .originRegion(node.getOriginRegion())
                .holdingInstitution(node.getHoldingInstitution())
                .shelfmark(node.getShelfmark())
                .author(node.getAuthor())
                .script(node.getScript())
                .build();
    }

    private List<ManuscriptNode> collectAllRelated(ManuscriptNode node) {
        java.util.Set<ManuscriptNode> related = new java.util.LinkedHashSet<>();
        node.getTranslatedFrom().forEach(r -> related.add(r.getTarget()));
        node.getCopiedFrom().forEach(r -> related.add(r.getTarget()));
        node.getCommentaryOn().forEach(r -> related.add(r.getTarget()));
        node.getDerivedFrom().forEach(r -> related.add(r.getTarget()));
        node.getParallelTradition().forEach(r -> related.add(r.getTarget()));
        node.getInfluencedBy().forEach(r -> related.add(r.getTarget()));
        return related.stream().filter(n -> n != null && Boolean.TRUE.equals(n.getIsPublished())).toList();
    }
}
