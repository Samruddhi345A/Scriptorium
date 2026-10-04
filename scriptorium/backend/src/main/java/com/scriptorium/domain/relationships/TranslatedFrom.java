package com.scriptorium.domain.relationships;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import com.scriptorium.domain.ManuscriptNode;

@RelationshipProperties
@Getter
@Setter
public class TranslatedFrom {

    @RelationshipId
    private Long id;

    @TargetNode
    private ManuscriptNode target;

    private String confidence; // established, probable, speculative
    private String description;
    private String scholarlySource;
}
