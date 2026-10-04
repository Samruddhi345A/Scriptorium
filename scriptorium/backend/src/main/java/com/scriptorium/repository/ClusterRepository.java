package com.scriptorium.repository;

import com.scriptorium.domain.ClusterNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClusterRepository extends Neo4jRepository<ClusterNode, String> {

    // All clusters that have at least one published manuscript
    @Query("""
            MATCH (c:Cluster)<-[:BELONGS_TO]-(m:Manuscript)
            WHERE m.isPublished = true
            RETURN DISTINCT c
            """)
    List<ClusterNode> findAllWithPublishedManuscripts();

    Optional<ClusterNode> findByName(String name);

    @Query("""
            MATCH (c:Cluster {id: $id})
            OPTIONAL MATCH (c)<-[:BELONGS_TO]-(m:Manuscript)
            WHERE m.isPublished = true
            RETURN c
            """)
    Optional<ClusterNode> findByIdWithPublishedManuscripts(@Param("id") String id);
}
