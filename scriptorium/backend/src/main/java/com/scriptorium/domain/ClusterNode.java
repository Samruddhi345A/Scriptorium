package com.scriptorium.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Cluster")
@Getter
@Setter
public class ClusterNode {

    @Id
    @GeneratedValue(GeneratedValue.UUIDGenerator.class)
    private String id;

    private String name;
    private String description;
    private String era;         // e.g. "Medieval", "Renaissance"
    private String tradition;   // e.g. "Arthurian", "Biblical", "Classical"
}
