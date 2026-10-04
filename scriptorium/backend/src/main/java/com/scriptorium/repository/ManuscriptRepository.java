package com.scriptorium.repository;

import com.scriptorium.domain.ManuscriptNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManuscriptRepository extends Neo4jRepository<ManuscriptNode, String> {

    List<ManuscriptNode> findAllByIsPublishedTrue();

    Optional<ManuscriptNode> findByIdAndIsPublishedTrue(String id);

    // Full-text search across title, originalTitle, summary, author, originRegion
    @Query("""
            MATCH (m:Manuscript)
            WHERE m.isPublished = true
              AND (
                toLower(m.title)         CONTAINS toLower($query)
                OR toLower(m.originalTitle) CONTAINS toLower($query)
                OR toLower(m.summary)    CONTAINS toLower($query)
                OR toLower(m.author)     CONTAINS toLower($query)
                OR toLower(m.originRegion) CONTAINS toLower($query)
              )
              AND ($language IS NULL OR m.language = $language)
            RETURN m
            """)
    List<ManuscriptNode> search(@Param("query") String query,
                                @Param("language") String language);

    // Neighbours of a manuscript (published only)
    @Query("""
            MATCH (m:Manuscript {id: $id})-[r]-(connected:Manuscript)
            WHERE connected.isPublished = true
            RETURN m, collect(r), collect(connected)
            """)
    Optional<ManuscriptNode> findWithNeighbours(@Param("id") String id);

    // Graph for a cluster — used by GraphService
    @Query("""
            MATCH (c:Cluster {id: $clusterId})<-[:BELONGS_TO]-(m:Manuscript)
            WHERE m.isPublished = true
            OPTIONAL MATCH (m)-[r]-(connected:Manuscript)
            WHERE connected.isPublished = true
            RETURN m, collect(r), collect(connected)
            """)
    List<ManuscriptNode> findPublishedByClusterId(@Param("clusterId") String clusterId);

    // Variable-depth traversal — used by GraphService
    @Query("""
            MATCH path = (start:Manuscript {id: $id})-[*1..$depth]-(connected:Manuscript)
            WHERE connected.isPublished = true
            WITH nodes(path) AS ns, relationships(path) AS rs
            UNWIND ns AS n
            WITH COLLECT(DISTINCT n) AS allNodes, rs
            UNWIND allNodes AS m
            RETURN m
            """)
    List<ManuscriptNode> findReachableFromId(@Param("id") String id,
                                             @Param("depth") int depth);
}
