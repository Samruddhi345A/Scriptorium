package com.scriptorium.domain;

import com.scriptorium.domain.relationships.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Node("Manuscript")
@Getter
@Setter
public class ManuscriptNode {

    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    private String id;

    private String title;
    private String originalTitle;
    private String language;       // ISO 639 code, e.g. "la", "fr", "en"
    private String script;         // e.g. "Caroline minuscule", "Gothic textura"
    private String approximateDate;
    private Integer dateLower;
    private Integer dateUpper;
    private String originRegion;
    private String scribe;
    private String author;
    private String holdingInstitution;
    private String shelfmark;
    private String condition;      // e.g. "good", "damaged", "fragmentary"

    @Property("summary")
    private String summary;

    private String contentUrl;
    private Integer wordCount;
    private Boolean isPublished = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Outgoing relationships ────────────────────────────────────────────────

    @Relationship(type = "TRANSLATED_FROM", direction = Relationship.Direction.OUTGOING)
    private List<TranslatedFrom> translatedFrom = new ArrayList<>();

    @Relationship(type = "COPIED_FROM", direction = Relationship.Direction.OUTGOING)
    private List<CopiedFrom> copiedFrom = new ArrayList<>();

    @Relationship(type = "COMMENTARY_ON", direction = Relationship.Direction.OUTGOING)
    private List<CommentaryOn> commentaryOn = new ArrayList<>();

    @Relationship(type = "DERIVED_FROM", direction = Relationship.Direction.OUTGOING)
    private List<DerivedFrom> derivedFrom = new ArrayList<>();

    @Relationship(type = "PARALLEL_TRADITION", direction = Relationship.Direction.OUTGOING)
    private List<ParallelTradition> parallelTradition = new ArrayList<>();

    @Relationship(type = "INFLUENCED_BY", direction = Relationship.Direction.OUTGOING)
    private List<InfluencedBy> influencedBy = new ArrayList<>();

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private ClusterNode cluster;
}
