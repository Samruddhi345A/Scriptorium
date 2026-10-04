# Scriptorium — Complete Project Brain Dump
### Everything you need to understand this codebase without reading it line by line

---

## TABLE OF CONTENTS

1. What Is This App?
2. The 30-Second Mental Model
3. Tech Stack — What Each Tool Does
4. Folder Structure — Every File Explained
5. The Two Databases — Why Two?
6. The Data — What Gets Stored
7. Neo4j Graph Model — Nodes and Edges
8. Seed Data — What is Pre-loaded
9. Backend Architecture — Layer by Layer
10. Every REST Endpoint Explained
11. Security — How Auth Works End to End
12. Data Flow — A Request from Click to Response
13. GraphService — The Core Brain
14. Data Structures Used and Why
15. Frontend Architecture — Layer by Layer
16. React Flow — How the Graph is Rendered
17. State Management — Zustand Store
18. The Complete Request Lifecycle (Full Trace)
19. Docker — How All 4 Services Connect
20. Environment Variables — Every Single One
21. Error Handling — What Happens When Things Go Wrong
22. DTO Transformation Chain
23. What Happens on First Startup
24. Cheat Sheet — Key Classes and Their Jobs

---

## 1. What Is This App?

Scriptorium is a READ-ONLY digital museum for historical manuscripts.

Think of it like a Wikipedia graph, except instead of hyperlinks between articles,
you have scholarly relationships between historical text versions.

The problem it solves:
A manuscript like "Historia Regum Britanniae" (written in Latin in 1138) was translated
into French, then into Middle English, then into Welsh — each version slightly different.
Scholars want to see these relationships visually, understand how texts evolved,
and trace which version influenced which.

What users can do:
- Public visitors: browse clusters of manuscripts, click nodes, read metadata, explore relationships
- Curators (admins): log in, add manuscript records, draw relationships between them, publish them

What users CANNOT do:
- Nobody edits the actual manuscript text (read-only museum)
- Public users cannot add or change anything

---

## 2. The 30-Second Mental Model

NEO4J (Graph DB):
  [Latin MS 1138] --TRANSLATED_FROM--> [French MS 1155]
       |                                      |
  BELONGS_TO                           BELONGS_TO
       |                                      |
       +---------> [Cluster: Historia] <------+

POSTGRESQL (Relational):
  curators table: email | password_hash | role | ...

SPRING BOOT BACKEND:
  /api/public/**  --> anyone can call these
  /api/auth/**    --> login / register
  /api/curator/** --> only curators with JWT token

REACT FRONTEND:
  Home page --> card grid of clusters
  Cluster page --> interactive React Flow graph
  Click a node --> side panel with full details

---

## 3. Tech Stack — What Each Tool Does

Java 21             Backend language
Spring Boot 3.5.4   The framework that glues the whole backend together
Maven               Build tool — downloads deps, compiles, packages to .jar
Spring Data Neo4j   Maps Java classes to Neo4j nodes/relationships
Spring Data JPA     Maps Java interfaces to SQL queries for PostgreSQL
Hibernate           The actual ORM under JPA
Flyway              Runs SQL migration scripts on startup
PostgreSQL 16       Relational DB — used ONLY for the curators table
Neo4j 5.24          Graph DB — stores manuscripts as nodes, relationships as edges
JJWT 0.12.6         Creates and validates JWT tokens
BCrypt              Hashes passwords before storing (irreversible)
spring-dotenv 4.0   Reads .env file and exposes as Spring properties
Lombok              Generates boilerplate: getters, setters, builders
React 18.3          Frontend framework
TypeScript 5.6      Typed JavaScript — catches errors at compile time
Vite 6.0            Frontend build tool, very fast dev server
@xyflow/react 12    Graph visualization — handles dragging, zooming, edges
Zustand 5           Tiny global state management
Axios 1.7           HTTP client for API calls
React Router 6      Client-side routing
Docker              Containerizes all 4 services
Docker Compose      Runs all 4 containers together
nginx 1.27          Serves the built React app in Docker

---

## 4. Folder Structure — Every File Explained

scriptorium/
  .env                              Your secrets. NEVER commit this.
  .env.example                      Template showing what keys exist, empty values
  .gitignore                        Tells git to ignore .env, node_modules, target/, etc.
  docker-compose.yml                Defines all 4 Docker services
  README.md                         Quick-start guide

  backend/
    pom.xml                         Maven config: all dependencies, Java version, build
    Dockerfile                      2-stage build: JDK build stage, JRE runtime stage
    src/main/java/com/scriptorium/
      ScriptoriumApplication.java   The main() method. Entry point. Boots Spring.
      config/
        SecurityConfig.java         URL access rules, CORS settings, BCrypt bean
        Neo4jConfig.java            Enables Neo4j repositories (mostly auto-configured)
        S3Config.java               Placeholder for future S3 file storage
        DevDataSeeder.java          Runs on dev startup, seeds 8 manuscripts
      domain/
        ManuscriptNode.java         A manuscript in Neo4j. All 20+ fields. @Node
        ClusterNode.java            A cluster in Neo4j. 5 fields. @Node
        CuratorEntity.java          A curator in PostgreSQL. @Entity
        relationships/
          TranslatedFrom.java       @RelationshipProperties: id, target, confidence, description, scholarlySource
          CopiedFrom.java           Same structure
          CommentaryOn.java         Same structure
          DerivedFrom.java          Same structure
          ParallelTradition.java    Same structure
          InfluencedBy.java         Same structure
      repository/
        ManuscriptRepository.java   Extends Neo4jRepository. Custom Cypher queries declared here.
        ClusterRepository.java      Extends Neo4jRepository.
        CuratorRepository.java      Extends JpaRepository (PostgreSQL). findByEmail, existsByEmail.
      dto/
        NodeDTO.java                11 fields the frontend receives per manuscript
        EdgeDTO.java                What the frontend receives for one relationship
        GraphPayload.java           Wrapper: { nodes: [...], edges: [...] }
        ClusterDTO.java             What the frontend receives for one cluster
        LoginRequest.java           { email, password }
        LoginResponse.java          { token, email, name, role }
        RegisterRequest.java        { email, name, institution, password }
        ManuscriptRequest.java      Curator sends to create/update a manuscript
        RelationshipRequest.java    Curator sends to link two manuscripts
        ErrorResponse.java          { status, error, message, timestamp, fieldErrors }
      service/
        AuthService.java            register() and login() logic
        ManuscriptService.java      create/update/publish/search manuscript logic
        GraphService.java           Core graph building logic. MOST IMPORTANT SERVICE.
      controller/
        PublicManuscriptController.java  5 public GET endpoints
        AuthController.java              /register and /login
        CuratorController.java           4 curator-only endpoints (require JWT)
      security/
        JwtUtil.java                Creates JWT, validates, extracts email/role
        JwtFilter.java              Runs before EVERY request. Validates JWT.
      exception/
        GlobalExceptionHandler.java Catches ALL exceptions. Returns clean JSON errors.
    src/main/resources/
      application.properties        Spring config: DB URLs, Neo4j URI, JWT settings
      db/migration/
        V1__create_curators.sql     Flyway creates the curators table in PostgreSQL

  frontend/
    package.json                    npm dependencies and scripts
    tsconfig.json                   TypeScript settings. Sets up @/ alias for src/
    vite.config.ts                  Proxies /api -> localhost:8080 in dev
    index.html                      Single HTML file. Has <div id="root"> where React mounts.
    Dockerfile                      Builds React then serves with nginx
    nginx.conf                      Serves static files, proxies /api/, SPA fallback
    src/
      main.tsx                      Entry point. Renders <App /> into #root.
      index.css                     CSS variables for the dark museum theme
      App.tsx                       Router: maps URL paths to page components
      types/index.ts                TypeScript interfaces matching backend DTOs
      api/api.ts                    All axios calls. JWT interceptor auto-attaches token.
      store/useGraphStore.ts        Zustand store: clusters, graphPayload, selectedNode, loading, error
      components/
        ManuscriptGraph.tsx         React Flow canvas. BFS layout. Custom nodes/edges.
        NodeCard.tsx                Custom React Flow node: language badge + title + date
        SidePanel.tsx               Slide-in drawer: full manuscript details + connections
      pages/
        Home.tsx                    Cluster listing page. Grid of clickable cluster cards.
        ClusterView.tsx             Graph page. Full viewport. Loads and renders graph.

---

## 5. The Two Databases — Why Two?

RULE: use the right tool for the shape of your data.

NEO4J — for the manuscripts
  Manuscripts are graph-shaped data. A manuscript is defined by its relationships
  to other manuscripts. "This is a copy of that, which was translated from this."
  Neo4j stores this natively. Query: "Give me all manuscripts within 3 hops of this one"
  = direct edge traversal. In PostgreSQL this would need multiple JOIN operations.

POSTGRESQL — for curator accounts
  Curator login is flat relational data. One row per curator. Simple email lookup.
  No relationships between curators. ACID transactions for safe registration.
  Flyway gives clean versioned schema history.

THE SEPARATION:
  "Who is allowed to log in?"                    -> Ask PostgreSQL
  "What manuscripts exist and how are connected?" -> Ask Neo4j

These two questions are completely independent. No data crosses between them.

---

## 6. The Data — What Gets Stored

ManuscriptNode (Neo4j) — all fields:
  id                  UUID string (auto-generated by Neo4j)
  title               Display title: "Historia Regum Britanniae"
  originalTitle       Title in original language
  language            ISO 639 code: la=Latin, fro=Old French, enm=Middle English, cy=Welsh
  script              Physical writing: "Caroline minuscule", "Gothic textura"
  approximateDate     Human-readable: "c. 1138", "c. 1155", "1476"
  dateLower           Integer year — start of scholarly date range
  dateUpper           Integer year — end of scholarly date range
  originRegion        Where written: "Monmouth, Wales / Oxford"
  scribe              Who physically wrote it (often unknown)
  author              Who composed it: "Geoffrey of Monmouth", "Wace"
  holdingInstitution  Where it is today: "Bodleian Library, University of Oxford"
  shelfmark           Library reference: "MS Rawlinson B 148"
  condition           Physical state: "good", "fair", "excellent", "damaged"
  summary             Long text about the manuscript's significance
  contentUrl          URL to scanned content (empty now, ready for S3)
  wordCount           Integer count
  isPublished         Boolean — only true = visible to public
  createdAt           Timestamp of when curator added it
  cluster             Reference to ClusterNode (via BELONGS_TO relationship)

ClusterNode (Neo4j):
  id          UUID string
  name        "Historia Regum Britanniae"
  description Long text about the tradition
  era         "Medieval", "Late Medieval"
  tradition   "Arthurian / British Chronicle"

CuratorEntity (PostgreSQL):
  id            UUID (PostgreSQL gen_random_uuid())
  email         Unique login identifier
  name          Display name
  institution   Optional: "University of Oxford"
  role          Always "curator" now (designed for future expansion)
  password_hash BCrypt hash. PLAIN PASSWORD IS NEVER STORED.
  created_at    Timestamp with timezone

Relationship properties (all 6 types have identical fields):
  id              Long (Neo4j internal relationship ID)
  target          Reference to target ManuscriptNode
  confidence      "established" | "probable" | "speculative"
  description     "Wace explicitly acknowledges Geoffrey in the prologue"
  scholarlySource "Weiss, J. (2002). Wace's Roman de Brut. University of Exeter Press."

---

## 7. Neo4j Graph Model — Nodes and Edges

Node types:
  (:Manuscript)  — a single version of a text
  (:Cluster)     — a thematic grouping of manuscripts

Relationship types (all Manuscript-to-Manuscript except BELONGS_TO):
  [:TRANSLATED_FROM]    "this is a translation of that"
  [:COPIED_FROM]        "this is a direct copy of that"
  [:COMMENTARY_ON]      "this comments on that"
  [:DERIVED_FROM]       "this loosely derives from that"
  [:PARALLEL_TRADITION] "these evolved separately from the same source"
  [:INFLUENCED_BY]      "this was stylistically influenced by that"
  [:BELONGS_TO]         Manuscript -> Cluster. No extra properties.

Visual of Cluster 1:
  [Historia Regum Britanniae]  Latin, c.1138, Geoffrey of Monmouth
          |
          | TRANSLATED_FROM (established)
          v
  [Roman de Brut]              Old French, c.1155, Wace
          |
          | TRANSLATED_FROM (established)
          v
  [Brut]                       Middle English, c.1190-1215, Layamon

  [Historia Regum Britanniae]
          |
          | TRANSLATED_FROM (established)
          v
  [Brut y Brenhinedd]          Welsh, c.1330

  [Brut y Brenhinedd]
          |
          | PARALLEL_TRADITION (probable)
          v
  [Brut]                       (Welsh and Middle English traditions connected)

  [Historia Regum Britanniae]
          |
          | DERIVED_FROM (established)
          v
  [Historia... Variant Version] 1951 critical edition

  All 5 also have: -[:BELONGS_TO]-> [Cluster: Historia Regum Britanniae]

BELONGS_TO is special:
  Every other relationship is Manuscript->Manuscript.
  BELONGS_TO is Manuscript->Cluster and has NO extra properties.
  This is how a cluster "contains" manuscripts — not a list field, but graph edges.

---

## 8. Seed Data — What is Pre-loaded

DevDataSeeder.java runs on startup when spring.profiles.active=dev.
It checks manuscriptRepository.count() > 0 first — skips if data exists.

CLUSTER 1: Historia Regum Britanniae
  1  Historia Regum Britanniae      Latin (la)        c.1138        Geoffrey of Monmouth    Bodleian Library — MS Rawlinson B 148
  2  Roman de Brut                  Old French (fro)  c.1155        Wace                    BnF — MS fr. 1450
  3  Brut                           Middle English    c.1190-1215   Layamon                 British Library — Cotton MS Caligula A IX
  4  Brut y Brenhinedd              Welsh (cy)        c.1330-1350   Unknown                 NLW — Peniarth MS 44
  5  Historia...Variant Version     English (en)      1951          Jacob Hammer (ed.)      Mediaeval Academy of America — Pub. No.57

  Relationships:
    French --> TRANSLATED_FROM --> Latin (established)
    English --> TRANSLATED_FROM --> French (established)
    Welsh --> TRANSLATED_FROM --> Latin (established)
    Welsh --> PARALLEL_TRADITION --> English (probable)
    Scholarly --> DERIVED_FROM --> Latin (established)

CLUSTER 2: The Canterbury Tales
  6  Canterbury Tales (Hengwrt)     Middle English    c.1400-1410   Adam Pinkhurst (attr.)  NLW — Peniarth MS 392D
  7  Canterbury Tales (Ellesmere)   Middle English    c.1400-1405   Adam Pinkhurst (attr.)  Huntington Library — EL 26 C 9
  8  Canterbury Tales (Caxton)      Middle English    1476          — (printed)             British Library — IB.55021

  Relationships:
    Ellesmere --> COPIED_FROM --> Hengwrt (probable)
    Caxton --> DERIVED_FROM --> Ellesmere (speculative)

All 8 nodes: isPublished = true. Visible to public immediately.

---

## 9. Backend Architecture — Layer by Layer

HTTP Request
     |
     v
JwtFilter (runs before ALL requests)
  -> Extracts Bearer token, validates it, injects user identity into context
     |
     v
SecurityConfig (Spring Security)
  -> Decides: is this URL allowed for this user?
     /api/public/**  -> anyone
     /api/curator/** -> must have ROLE_CURATOR
     |
     v
CONTROLLER LAYER
  -> Receives HTTP, validates input with @Valid, calls service, returns HTTP response
  PublicManuscriptController / AuthController / CuratorController
     |
     v
SERVICE LAYER  (ALL business logic lives here)
  -> Controllers call services. Services call repositories.
  -> NO raw queries in controllers. NO business logic in repositories.
  AuthService / ManuscriptService / GraphService
     |
     v
REPOSITORY LAYER  (pure database access)
  -> Spring generates the implementation from your interface
  ManuscriptRepository (Neo4j) / ClusterRepository (Neo4j) / CuratorRepository (PostgreSQL)
     |
     +--------+--------+
     v                 v
   Neo4j          PostgreSQL

GlobalExceptionHandler (safety net outside the chain):
  If ANY layer throws, Spring routes it here before responding.
  Ensures you always get clean JSON instead of a stack trace.

---

## 10. Every REST Endpoint Explained

PUBLIC ENDPOINTS (no auth needed):

GET /api/public/clusters
  What: Returns clusters that have at least one published manuscript
  Who calls it: Home page on load
  Response: [ { id, name, description, era, tradition }, ... ]

GET /api/public/clusters/{id}/graph
  What: Full graph for a cluster — all published manuscripts + all relationships between them
  Who calls it: ClusterView page on load
  Response: { nodes: [NodeDTO...], edges: [EdgeDTO...] }
  NodeDTO fields: id, title, language, approximateDate, summary, condition, originRegion, holdingInstitution, shelfmark, author, script
  EdgeDTO fields: id, source, target, type, confidence, description, scholarlySource

GET /api/public/manuscripts/{id}
  What: Full detail for one published manuscript
  Response: NodeDTO

GET /api/public/manuscripts/{id}/neighbors
  What: All published manuscripts directly connected to this one
  Response: [NodeDTO...]

GET /api/public/search?q=&language=
  What: Full-text search across title, originalTitle, summary, author, originRegion
  Params: q (search term), language (ISO 639 code, optional)
  Response: [NodeDTO...]

AUTH ENDPOINTS (no auth needed):

POST /api/auth/register
  Body: { email, name, institution, password }
  What: Creates curator account. Hashes password. Returns JWT (auto-login).
  Response 201: { token, email, name, role }
  Validation: valid email format, password min 8 chars, email not already taken

POST /api/auth/login
  Body: { email, password }
  What: Validates credentials. Returns JWT.
  Response 200: { token, email, name, role }
  Note: Same error message for wrong email AND wrong password (security: don't reveal which)

CURATOR ENDPOINTS (require Authorization: Bearer <jwt>):

POST /api/curator/manuscripts
  Body: ManuscriptRequest (all fields, clusterId optional)
  What: Creates new manuscript in Neo4j. Starts as isPublished=false.
  Response 201: NodeDTO

PUT /api/curator/manuscripts/{id}
  Body: ManuscriptRequest
  What: Updates all fields of existing manuscript
  Response 200: NodeDTO

PATCH /api/curator/manuscripts/{id}/publish
  Body: none
  What: Sets isPublished=true. Now visible to public.
  Response 200: NodeDTO

POST /api/curator/relationships
  Body: { sourceId, targetId, type, confidence, description, scholarlySource }
  What: Creates directed relationship between two manuscripts
  Validation: type must be one of 6 valid types, confidence must be established/probable/speculative
  Response 201: no body

---

## 11. Security — How Auth Works End to End

REGISTRATION / LOGIN:
  1. CuratorRepository.findByEmail(email) -> fetches row from PostgreSQL
  2. BCryptPasswordEncoder.matches(rawPassword, storedHash)
     -> BCrypt re-hashes rawPassword with the salt embedded in storedHash and compares
     -> The original password is NEVER stored and NEVER decrypted
  3. If match: JwtUtil.generateToken(email, role)
     -> JWT structure: header.payload.signature (3 Base64 parts separated by dots)
     -> Header: { "alg": "HS256" }
     -> Payload: { "sub": "email", "role": "curator", "iat": now, "exp": now+24h }
     -> Signature: HMAC-SHA256(base64(header)+"."+base64(payload), JWT_SECRET)
     -> Signature proves nobody tampered with the token

SUBSEQUENT CURATOR REQUESTS:
  JwtFilter intercepts EVERY request:
    1. Extracts "Bearer <token>" from Authorization header
    2. JwtUtil.isTokenValid(token): verifies signature using JWT_SECRET, checks expiry
    3. JwtUtil.extractEmail(token) and extractRole(token) from payload
    4. Creates UsernamePasswordAuthenticationToken(email, null, [ROLE_CURATOR])
    5. Puts it in SecurityContextHolder (Spring's per-request auth storage)
  SecurityConfig checks: /api/curator/** requires ROLE_CURATOR -> allowed

WHAT HAPPENS WITH BAD TOKENS:
  Wrong signature -> JwtException -> token invalid -> user NOT authenticated
  Expired token -> expiration check fails -> token invalid
  No token at all -> JwtFilter does nothing -> user is anonymous
  Anonymous hits /api/curator/** -> Spring Security returns 403 Forbidden

---

## 12. Data Flow — A Request from Click to Response

Scenario: User opens the Historia Regum Britanniae cluster graph

1. USER CLICKS "Explore graph" on the cluster card

2. REACT ROUTER navigates to /cluster/abc-123
   ClusterView.tsx renders

3. ClusterView useEffect:
   setLoading(true)
   getClusterGraph("abc-123") [api.ts]
   axios.get("/api/public/clusters/abc-123/graph")
   Vite proxy forwards to http://localhost:8080/api/public/clusters/abc-123/graph

4. HTTP REQUEST hits Spring Boot

5. JwtFilter: No Authorization header -> does nothing -> anonymous

6. SecurityConfig: /api/public/** -> permitAll() -> allowed through

7. PublicManuscriptController.getClusterGraph("abc-123")
   -> GraphService.getGraphForCluster("abc-123")

8. GraphService.getGraphForCluster("abc-123"):
   -> clusterRepository.findById("abc-123") [verifies exists]
   -> manuscriptRepository.findPublishedByClusterId("abc-123")
      Cypher: MATCH (c:Cluster {id: "abc-123"})<-[:BELONGS_TO]-(m:Manuscript)
              WHERE m.isPublished = true
              OPTIONAL MATCH (m)-[r]-(connected:Manuscript)
              WHERE connected.isPublished = true
              RETURN m, collect(r), collect(connected)
      Returns List<ManuscriptNode> with relationship lists populated:
        node.getTranslatedFrom() = [TranslatedFrom{target: frenchMS, confidence: "established"}]
   -> buildPayload(nodes)

9. GraphService.buildPayload(nodes):
   -> HashSet<String> nodeIds = {id1, id2, id3, id4, id5}
   -> Loop over nodes: create NodeDTO for each (strips internal fields)
   -> Loop over nodes again: for each relationship list, for each rel:
        if nodeIds.contains(rel.getTarget().getId()) [O(1) HashSet lookup]
            create EdgeDTO and add to edges list
   -> Returns GraphPayload { nodes: [5 NodeDTOs], edges: [5 EdgeDTOs] }

10. Controller returns ResponseEntity.ok(graphPayload)
    Jackson serializes to JSON -> HTTP 200 sent

11. Axios receives response -> api.ts returns typed GraphPayload
    ClusterView: setGraphPayload(payload) [Zustand]
    setLoading(false)

12. Zustand change triggers React re-render:
    ManuscriptGraph.tsx receives payload
    layoutNodes(payload):
      -> Map<string, string[]> edgesBySource (adjacency list for BFS)
      -> Map<string, number> generation (node's column/depth)
      -> BFS assigns generation 0 to root nodes, 1 to their children, etc.
      -> Position: x = generation*280px, y = rowIndex*140px centered at 0
      -> Returns ReactFlow nodes and edges
    ReactFlow renders: 5 nodes, 5 edges

13. USER SEES interactive graph:
    -> Nodes arranged left-to-right by generation
    -> Gold edges = established, dashed blue = probable, dotted = speculative
    -> Edge labels show relationship type
    -> Can drag, zoom, pan

14. USER CLICKS a node:
    -> onNodeClick in ManuscriptGraph.tsx
    -> getNodeById(node.id) [Zustand derived fn]
    -> setSelectedNode(manuscript) [Zustand]
    -> SidePanel.tsx re-renders:
       Shows: language, approximateDate, originRegion, author, script, holdingInstitution, shelfmark, condition
       Shows: summary text
       getEdgesForNode(id) -> filters edges from Zustand store for this node
       Shows: connections list with confidence badges and scholarly citations

---

## 13. GraphService — The Core Brain

File: service/GraphService.java
This is the most important file in the backend.

getAllClusters()
  Input: nothing
  Cypher: MATCH (c:Cluster)<-[:BELONGS_TO]-(m:Manuscript) WHERE m.isPublished=true RETURN DISTINCT c
  Output: List<ClusterDTO>

getGraphForCluster(clusterId)
  Input: cluster UUID string
  Step 1: Verify cluster exists (throws 404 if not)
  Step 2: manuscriptRepository.findPublishedByClusterId(clusterId)
          Cypher: OPTIONAL MATCH gets nodes + relationships in one query
          Spring Data Neo4j hydrates ManuscriptNode objects with relationship lists populated
  Step 3: buildPayload(nodes)
  Output: GraphPayload { nodes, edges }

getGraphFromManuscript(manuscriptId, depth)
  Input: manuscript UUID, depth integer 1-5
  Step 1: Validate depth 1-5 (throws 400 if outside range)
  Step 2: Load start node
  Step 3: manuscriptRepository.findReachableFromId(id, depth)
          Cypher: MATCH path = (start:Manuscript {id:$id})-[*1..$depth]-(connected)
                  WHERE connected.isPublished=true
          Returns all manuscripts reachable within depth hops
  Step 4: LinkedHashMap deduplication (start node first, then reachable)
  Step 5: buildPayload(deduped)
  Output: GraphPayload { nodes, edges }

buildPayload(nodes) — private, the real work
  Input: List<ManuscriptNode>
  Step 1: HashSet<String> nodeIds <- all IDs (for O(1) membership check)
  Step 2: For each node: create NodeDTO (strips isPublished, createdAt, etc.)
  Step 3: For each node: call extractEdges(node, nodeIds, edges)
          extractEdges():
            Walk node.getTranslatedFrom(), node.getCopiedFrom(), etc. (all 6 lists)
            For each rel: if nodeIds.contains(rel.getTarget().getId()) -> create EdgeDTO
  Output: GraphPayload { List<NodeDTO>, List<EdgeDTO> }

WHY the HashSet check matters:
  A manuscript can link to a manuscript in a DIFFERENT cluster.
  Without the check: edge points to a node ID not in the nodes list.
  Result: React Flow renders broken edges pointing to nothing.
  With HashSet: only edges where BOTH endpoints are in the result set are included.

---

## 14. Data Structures Used and Why

1. HashSet<String>  --  GraphService.buildPayload()  --  variable: nodeIds
   Code: Set<String> nodeIds = new HashSet<>();
         nodeIds.contains(someId) // check if node is in result set
   Why HashSet: O(1) average lookup via hashing
   Alternative (List.contains): O(n) per check, O(n^2) total for n nodes
   Impact: 100 nodes -> HashSet=100 ops, List=10,000 ops

2. LinkedHashMap<String, ManuscriptNode>  --  GraphService.getGraphFromManuscript()  --  variable: nodeMap
   Code: Map<String, ManuscriptNode> nodeMap = new LinkedHashMap<>();
         nodeMap.put(start.getId(), start);
         reachable.forEach(n -> nodeMap.put(n.getId(), n)); // duplicate IDs auto-overwrite
   Why HashMap: O(1) put/get. Auto-deduplicates by key.
   Why Linked: Preserves insertion order -> start node always first in final list.
   Alternative (List + manual contains check): O(n) per element, messy code.

3. LinkedHashSet<ManuscriptNode>  --  ManuscriptService.collectAllRelated()  --  variable: related
   Code: Set<ManuscriptNode> related = new LinkedHashSet<>();
         node.getTranslatedFrom().forEach(r -> related.add(r.getTarget()));
         // ... all 6 types
   Why: Same manuscript can appear in multiple relationship lists.
        LinkedHashSet deduplicates while keeping first-seen order.

4. Map<String, String>  --  GlobalExceptionHandler  --  variable: fieldErrors
   Code: Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage())
   Result: { "email": "Must be valid email", "password": "Min 8 chars" }
   Why: Maps field name -> error message. Returned as JSON so UI can highlight each field.

5. Map<string, string[]>  --  ManuscriptGraph.tsx layout  --  variable: edgesBySource
   Code: const edgesBySource = new Map<string, string[]>();
         payload.edges.forEach(e => { ... edgesBySource.set(e.source, list); });
   Why: BFS needs "what are the children of node X?" repeatedly.
        Map gives O(1) lookup vs scanning all edges each time.

6. Map<string, number>  --  ManuscriptGraph.tsx layout  --  variable: generation
   Code: const generation = new Map<string, number>();
   Why: Tracks BFS depth (column) per node.
        generation.get(id) = how many hops from a root node = which column to place it in.

SUMMARY TABLE:
  Structure                        | Where                            | Purpose
  ---------------------------------|----------------------------------|---------------------------
  HashSet<String>                  | GraphService.buildPayload()      | O(1) edge endpoint check
  LinkedHashMap<String,Manuscript> | GraphService.getGraphFrom...()   | Dedup multi-path traversal
  LinkedHashSet<ManuscriptNode>    | ManuscriptService.collect...()   | Dedup neighbour collection
  Map<String, String>              | GlobalExceptionHandler           | Field-level error messages
  Map<string, string[]>            | ManuscriptGraph.tsx              | BFS adjacency list
  Map<string, number>              | ManuscriptGraph.tsx              | BFS generation tracking

---

## 15. Frontend Architecture — Layer by Layer

URL Change (React Router)
          |
          v
PAGES  (Home.tsx, ClusterView.tsx)
  Top-level components, one per route.
  Fetch data on mount. Manage page layout.
          |
          | reads/writes
          v
ZUSTAND STORE  (useGraphStore.ts)
  Global state. Any component can read/update without prop drilling.
  clusters, graphPayload, selectedNode, loading, error
          |
          | state changes trigger re-renders
          v
COMPONENTS  (ManuscriptGraph, NodeCard, SidePanel)
  Pure UI. Read state from store. Report interactions to store.
          |
          | when loading data
          v
API LAYER  (api/api.ts)
  All axios calls. Returns typed promises.
  JWT interceptor auto-attaches token from localStorage.
          |
          | HTTP over /api/
          v
  Spring Boot Backend

STATE vs PROPS:
  Props: data passed parent->child (ManuscriptGraph receives payload as prop)
  Store: data any component reads directly (selectedNode used by ManuscriptGraph AND SidePanel)

---

## 16. React Flow — How the Graph is Rendered

React Flow data format:
  Nodes: { id, type: "manuscript", position: {x,y}, data: { manuscript: NodeDTO } }
  Edges: { id, source, target, type: "manuscript", data: EdgeDTO, markerEnd: {...} }

Custom node — NodeCard:
  React Flow calls NodeCard({ data, selected }) for each node.
  Renders:
    1. Language badge (gold chip: "LATIN", "OLD FRENCH", etc.)
    2. Title in serif font
    3. Approximate date in muted color
    4. Gold border when selected
    5. Two handles (top, bottom) where edges attach

Custom edge — ManuscriptEdge:
  React Flow calls ManuscriptEdge({ sourceX, sourceY, targetX, targetY, data }) for each edge.
  Renders:
    1. getBezierPath() -> smooth curve between nodes
    2. Color by confidence:
         established  -> gold #c9a96e, solid, strokeWidth 2
         probable     -> blue #7a8fbb, dashed "6 3", strokeWidth 1.5
         speculative  -> muted #6a7a6a, dotted "2 5", strokeWidth 1.5
    3. Floating label: "TRANSLATED FROM", "COPIED FROM", etc.
    4. Arrowhead at target end

Layout algorithm (layoutNodes() in ManuscriptGraph.tsx):
  React Flow does NOT auto-layout. We assign x,y manually.
  Step 1: Find root nodes (no incoming edges) -> generation 0
  Step 2: BFS outward -> each node's generation = parent generation + 1
  Step 3: Group by generation
  Step 4: x = generation * 280px (column spacing)
          y = rowIndex * 140px, centered at 0

  Example with 5 nodes:
    Gen 0          Gen 1          Gen 2
    Latin (1138)   French (1155)  English (1190)
    x=0, y=0       x=280, y=-140  x=560, y=0
                   Welsh (1330)
                   x=280, y=0
                   Scholarly (1951)
                   x=280, y=140

---

## 17. State Management — Zustand Store

File: store/useGraphStore.ts

STATE:
  clusters: ClusterDTO[]      loaded once on Home, cached for navigation back
  graphPayload: GraphPayload  { nodes, edges } for current view, cleared on cluster change
  selectedNode: NodeDTO|null  manuscript shown in SidePanel. null = panel closed.
  loading: boolean            true while API call is in flight
  error: string|null          error message for UI. null = no error.

SETTERS (all are just set({ field: value })):
  setClusters, setGraphPayload, setSelectedNode, setLoading, setError

DERIVED HELPERS (computed from state, not stored):
  getNodeById(id)        -> graphPayload.nodes.find(n => n.id === id)
  getEdgesForNode(id)    -> graphPayload.edges.filter(e => e.source===id || e.target===id)

HOW IT FLOWS:
  Home.tsx:           getClusters() -> setClusters([...])
  ClusterView.tsx:    getClusterGraph(id) -> setGraphPayload({nodes, edges})
  ManuscriptGraph:    user clicks node -> setSelectedNode(manuscript)
  SidePanel:          reads selectedNode, reads getEdgesForNode(id)
                      user closes -> setSelectedNode(null)

---

## 18. The Complete Request Lifecycle (Full Trace)

Scenario: Curator creates a new manuscript

1. POST /api/curator/manuscripts
   Authorization: Bearer eyJ...
   Body: { title: "The Vulgate Cycle", language: "fro", approximateDate: "c.1215-1235", clusterId: "abc" }

2. JwtFilter:
   -> extracts token
   -> JwtUtil.isTokenValid() -> verifies HMAC-SHA256 signature with JWT_SECRET -> valid
   -> JwtUtil.extractRole() -> "curator"
   -> SecurityContextHolder.setAuthentication(ROLE_CURATOR)

3. SecurityConfig: /api/curator/** requires ROLE_CURATOR -> allowed

4. CuratorController.createManuscript(request):
   -> @Valid validates: title not blank, language matches [a-z]{2,3}
   -> manuscriptService.create(request)

5. ManuscriptService.create(request):
   -> new ManuscriptNode()
   -> applyRequest(node, request) -> copies all DTO fields to entity
   -> node.setIsPublished(false) [starts unpublished]
   -> node.setCreatedAt(now)
   -> clusterRepository.findById(clusterId) -> ClusterNode found
   -> node.setCluster(cluster) [sets BELONGS_TO relationship]
   -> manuscriptRepository.save(node)
      Spring Data Neo4j Cypher:
        CREATE (m:Manuscript {id: "new-uuid", title: "The Vulgate Cycle", ...})
        CREATE (m)-[:BELONGS_TO]->(c)
   -> toNodeDTO(savedNode) -> strips internal fields, returns public NodeDTO

6. CuratorController returns ResponseEntity.status(201).body(nodeDTO)
   Jackson serializes to JSON: { "id": "new-uuid", "title": "The Vulgate Cycle", ... }

7. Curator then calls PATCH /api/curator/manuscripts/{new-uuid}/publish
   -> ManuscriptService.publish(id)
   -> node.setIsPublished(true)
   -> manuscriptRepository.save(node)
   -> Now the manuscript appears in all public queries

---

## 19. Docker — How All 4 Services Connect

INTERNAL NETWORK:
  Docker creates an internal network. Services reach each other by SERVICE NAME as hostname.
  "postgres" is the hostname for PostgreSQL.
  "neo4j" is the hostname for Neo4j.
  "backend" is the hostname for Spring Boot.
  This is why docker-compose.yml overrides: DB_URL=jdbc:postgresql://postgres:5432/scriptorium
  NOT localhost (localhost inside backend container = the container itself)

STARTUP ORDER (healthchecks enforce this):
  1. postgres starts -> healthcheck: pg_isready
  2. neo4j starts -> healthcheck: wget http://localhost:7474
  3. backend starts ONLY when postgres AND neo4j are healthy
     -> Flyway runs V1__create_curators.sql
     -> DevDataSeeder seeds Neo4j
     -> App ready on port 8080
  4. frontend starts -> depends_on: backend
     -> Serves React app on port 80 (mapped to 5173 on host)
     -> nginx proxies /api/ to http://backend:8080

BACKEND DOCKERFILE (2-stage):
  Stage 1 (eclipse-temurin:21-jdk-alpine):
    Install Maven -> mvn package -DskipTests -> produces target/*.jar
  Stage 2 (eclipse-temurin:21-jre-alpine):
    Creates non-root user "scriptorium" (security)
    Copies .jar from stage 1
    ENTRYPOINT: java -XX:+UseContainerSupport -jar app.jar

FRONTEND DOCKERFILE (2-stage):
  Stage 1 (node:20-alpine):
    npm ci -> npm run build -> produces dist/
  Stage 2 (nginx:1.27-alpine):
    Copies dist/ to /usr/share/nginx/html
    Copies nginx.conf
    nginx serves files, proxies /api/, SPA fallback for React Router

WHY 2-STAGE BUILDS:
  Final image has NO build tools (no Maven, no Node, no npm).
  Smaller image, smaller attack surface.
  JDK is 400MB+. JRE is ~100MB. Node+npm is ~300MB. nginx is ~40MB.

---

## 20. Environment Variables — Every Single One

DB_URL
  Used by: application.properties -> Spring JPA
  Local: jdbc:postgresql://localhost:5432/scriptorium
  Docker: jdbc:postgresql://postgres:5432/scriptorium (overridden in docker-compose)

DB_USERNAME
  PostgreSQL username. Usually "postgres".

DB_PASSWORD
  PostgreSQL password. You choose this.

NEO4J_URI
  Used by: application.properties -> Spring Data Neo4j
  Local: bolt://localhost:7687
  Docker: bolt://neo4j:7687 (overridden in docker-compose)

NEO4J_USERNAME
  Neo4j username. Default is "neo4j".

NEO4J_PASSWORD
  Neo4j password. You choose this. Also used in docker-compose to initialize Neo4j.

JWT_SECRET
  The HMAC-SHA256 signing key for JWT tokens.
  Minimum 32 characters.
  If this leaks: anyone can forge curator tokens. Treat like a database password.

SPRING_PROFILES_ACTIVE
  Set to "dev" -> DevDataSeeder runs on startup and seeds Neo4j.
  Set to "prod" or remove -> no seeding.

HOW spring-dotenv WORKS:
  The library reads .env on startup.
  Each line KEY=VALUE is registered as a Spring property.
  ${DB_URL} in application.properties resolves to the .env value.
  No code changes needed.

---

## 21. Error Handling — What Happens When Things Go Wrong

GlobalExceptionHandler.java is annotated @RestControllerAdvice.
Spring automatically routes ALL uncaught exceptions from ALL controllers to it.

EXCEPTION -> HTTP STATUS MAPPING:
  MethodArgumentNotValidException -> 400  When @Valid on request body fails
  IllegalArgumentException        -> 400  Business rule violated (duplicate email, invalid type)
  NoSuchElementException          -> 404  repository.findById().orElseThrow() found nothing
  AccessDeniedException           -> 403  Valid JWT but wrong role
  Exception (catch-all)           -> 500  Unexpected error. Stack trace logged. Generic message returned.

RESPONSE FORMAT (always the same):
  {
    "status": 400,
    "error": "Validation Failed",
    "message": "One or more fields failed validation.",
    "timestamp": "2026-10-02T14:30:00",
    "fieldErrors": {
      "email": "Must be a valid email address",
      "password": "Password must be at least 8 characters"
    }
  }
  fieldErrors is null for non-validation errors (404, 500, etc.).

---

## 22. DTO Transformation Chain

Data gets transformed at each boundary. Never passes raw through layers.

DATABASE (Neo4j):
  ManuscriptNode (20+ fields: isPublished, createdAt, cluster ref, relationship lists)
          |
          | manuscriptService.toNodeDTO()
          v
API RESPONSE:
  NodeDTO (11 fields: id, title, language, approximateDate, summary, condition,
           originRegion, holdingInstitution, shelfmark, author, script)
          |
          | axios + TypeScript interface
          v
FRONTEND STATE (Zustand):
  NodeDTO (same 11 fields — TypeScript interface exactly matches Java class)
          |
          | ManuscriptGraph.tsx layoutNodes()
          v
REACT FLOW NODE:
  { id, type: "manuscript", position: {x,y}, data: { manuscript: NodeDTO } }
          |
          | NodeCard.tsx renders
          v
DOM: <div> language badge + title + date </div>

EDGE TRANSFORMATION:
  Neo4j relationship: TranslatedFrom { id: Long, target: ManuscriptNode, confidence, description, ... }
          |
          | GraphService.buildEdge()
          v
  EdgeDTO { id: "1", source: "abc", target: "def", type: "TRANSLATED_FROM", confidence: "established", ... }
          |
          | ManuscriptGraph.tsx
          v
  React Flow Edge { id, source, target, type: "manuscript", data: EdgeDTO, markerEnd: {...} }
          |
          | ManuscriptEdge renders
          v
  DOM: bezier curve with color, dash, label based on EdgeDTO.confidence and EdgeDTO.type

WHY STRIP FIELDS IN DTO:
  isPublished -> internal flag, only true=visible reach the API anyway
  createdAt   -> internal metadata, not shown in UI
  wordCount   -> not in UI yet (easy to add to NodeDTO later)
  contentUrl  -> not used yet (for future scanned text display)
  Relationship lists -> extracted separately into EdgeDTO objects

---

## 23. What Happens on First Startup

1. JVM starts, Spring Boot begins auto-configuration

2. spring-dotenv reads .env file
   -> Registers all vars as Spring properties

3. Spring Boot auto-configures DataSource (PostgreSQL)
   -> Creates connection pool using DB_URL, DB_USERNAME, DB_PASSWORD

4. Flyway runs:
   -> Checks flyway_schema_history table in PostgreSQL
   -> Table doesn't exist yet -> creates it
   -> Scans classpath:db/migration/ for scripts
   -> Finds V1__create_curators.sql -> not in history yet -> runs it
   -> CREATE TABLE curators (...)
   -> Records it in flyway_schema_history

5. Spring Boot configures Neo4jDriver
   -> Opens Bolt connection to NEO4J_URI using NEO4J_USERNAME/NEO4J_PASSWORD

6. Spring Security configures:
   -> SecurityFilterChain with rules from SecurityConfig
   -> Registers JwtFilter as a pre-authentication filter

7. All beans are wired (dependency injection):
   -> Controllers get Services injected
   -> Services get Repositories injected
   -> JwtFilter gets JwtUtil injected

8. "Started ScriptoriumApplication in X.X seconds"

9. DevDataSeeder.run() (only if profile=dev):
   -> manuscriptRepository.count() == 0 -> proceed
   -> Creates ClusterNode "Historia Regum Britanniae" -> saves to Neo4j
   -> Creates 5 ManuscriptNode objects with full metadata -> saves each
   -> Creates relationship objects, saves via parent nodes
   -> Creates ClusterNode "The Canterbury Tales" -> saves
   -> Creates 3 ManuscriptNode objects -> saves each
   -> Creates relationship objects -> saves
   -> "Dev seed: complete."

10. Application ready.
    Neo4j: 2 clusters, 8 manuscripts, 7 relationships.
    PostgreSQL: curators table exists, 0 rows.

---

## 24. Cheat Sheet — Key Classes and Their Jobs

BACKEND:
  ScriptoriumApplication        Has main(). Starts everything.
  ManuscriptNode                Java representation of a manuscript in Neo4j. All fields. @Node.
  ClusterNode                   Java representation of a cluster in Neo4j. @Node.
  CuratorEntity                 Java representation of a curator row in PostgreSQL. @Entity.
  TranslatedFrom (+ 5 others)   Relationship class. Holds confidence, description, scholarlySource, target node ref.
  ManuscriptRepository          Interface. You declare queries; Spring generates the code. Neo4j.
  ClusterRepository             Same, for clusters. Neo4j.
  CuratorRepository             Same, for curators. PostgreSQL JPA.
  AuthService                   register() -> hash+save+JWT. login() -> check+JWT.
  ManuscriptService             create/update/publish/search manuscripts. addRelationship between two manuscripts.
  GraphService                  THE MOST IMPORTANT. Builds GraphPayload {nodes,edges} from Neo4j data. Uses HashSet for edge filtering.
  PublicManuscriptController    5 public GET endpoints. No auth.
  AuthController                /register and /login POST endpoints.
  CuratorController             4 curator write endpoints. Require JWT.
  JwtUtil                       generateToken(email, role)->JWT. isTokenValid(token)->bool. extractEmail/Role(token)->string.
  JwtFilter                     Intercepts every HTTP request. Extracts+validates JWT. Sets auth in Spring Security context.
  SecurityConfig                URL access rules. /api/public/**=open. /api/curator/**=ROLE_CURATOR required.
  GlobalExceptionHandler        Catches ALL exceptions. Returns consistent JSON errors.
  DevDataSeeder                 Runs once on dev startup. Seeds 8 manuscripts across 2 clusters.

FRONTEND:
  main.tsx              Entry point. Mounts App into #root.
  App.tsx               Router. "/" -> Home, "/cluster/:id" -> ClusterView.
  index.css             CSS variables for dark museum theme.
  types/index.ts        TypeScript interfaces matching backend DTOs. CONFIDENCE_STYLE and REL_LABELS maps.
  api/api.ts            All axios calls. /api base URL. JWT interceptor. Typed returns.
  useGraphStore.ts      Zustand. clusters, graphPayload, selectedNode, loading, error. getNodeById(), getEdgesForNode().
  Home.tsx              Fetches clusters on mount. Grid of ClusterCard buttons. Navigate to /cluster/:id on click.
  ClusterView.tsx       Fetches graph on id change. Renders ManuscriptGraph + SidePanel + Legend. Loading/error states.
  ManuscriptGraph.tsx   React Flow canvas. BFS layoutNodes(). NodeCard and ManuscriptEdge custom types. onNodeClick -> setSelectedNode.
  NodeCard.tsx          Custom React Flow node. Language badge + title + date. Selection state.
  SidePanel.tsx         Slide-in drawer. Full manuscript metadata + summary + connections with confidence badges.

---
## 3. TECH STACK - WHAT EACH TOOL DOES

Java 21             Backend language. Modern features: records, text blocks, pattern matching.
Spring Boot 3.5.4   The framework. Auto-configures everything. You declare what you want, it wires it.
Maven               Build tool. Downloads dependencies from pom.xml, compiles, packages to a .jar file.
Spring Data Neo4j   Maps Java classes to Neo4j nodes/relationships. No raw Cypher needed for basics.
Spring Data JPA     Maps Java interfaces to SQL queries. Hibernate is the engine underneath.
Flyway              Runs SQL migration scripts on startup. Versioned DB schema history.
PostgreSQL 16       Relational DB. Used ONLY for the curators login table.
Neo4j 5.24          Graph DB. Stores manuscripts as nodes, relationships as edges.
JJWT 0.12.6         Creates and validates JWT tokens for curator authentication.
BCrypt              Hashes passwords before storing. Irreversible - original password never recoverable.
spring-dotenv 4.0   Reads .env file and exposes each line as a Spring property.
Lombok              Generates boilerplate Java: getters, setters, builders, constructors via annotations.
React 18.3          Frontend framework. Builds UI as reusable components.
TypeScript 5.6      Typed JavaScript. Catches type errors at compile time not runtime.
Vite 6.0            Frontend build tool. Extremely fast dev server with HMR.
@xyflow/react 12    The graph visualization library. Handles node dragging, zooming, custom edges.
Zustand 5           Tiny global state management. One store object any component can read/write.
Axios 1.7           HTTP client. Makes typed API calls from React to Spring Boot.
React Router 6      Client-side routing. / = Home, /cluster/:id = graph page.
Docker              Containerizes all 4 services identically.
Docker Compose      Orchestrates all 4 containers with one command.
nginx 1.27          Serves the built React app as static files in production Docker setup.

---
## 4. FOLDER STRUCTURE - EVERY FILE EXPLAINED

scriptorium/                          ROOT OF THE MONOREPO
  .env                                Your secrets (DB passwords, JWT secret). NEVER commit.
  .env.example                        Template - shows what keys exist with empty values.
  .gitignore                          Ignores: .env, node_modules/, target/, .idea/, dist/
  docker-compose.yml                  Defines all 4 Docker services and how they connect.
  README.md                           Quick-start guide.

  backend/
    pom.xml                           Maven config: all Java dependencies, Java 21, build settings.
    Dockerfile                        2-stage: JDK stage builds .jar, JRE stage runs it.
    src/main/java/com/scriptorium/
      ScriptoriumApplication.java     The main() method. Entry point. @SpringBootApplication.

      config/
        SecurityConfig.java           URL access rules. CORS. BCryptPasswordEncoder bean (strength 12).
        Neo4jConfig.java              @EnableNeo4jRepositories. Mostly auto-configured by Spring Boot.
        S3Config.java                 Empty placeholder. Has commented code for future AWS S3 setup.
        DevDataSeeder.java            @Profile("dev") @CommandLineRunner. Seeds 8 manuscripts on startup.

      domain/
        ManuscriptNode.java           @Node("Manuscript"). The manuscript in Neo4j. 20+ fields.
        ClusterNode.java              @Node("Cluster"). The grouping. 5 fields.
        CuratorEntity.java            @Entity @Table("curators"). PostgreSQL row. email + password_hash.
        relationships/
          TranslatedFrom.java         @RelationshipProperties. Fields: id(Long), target(ManuscriptNode), confidence, description, scholarlySource.
          CopiedFrom.java             Identical structure, different type name.
          CommentaryOn.java           Identical structure.
          DerivedFrom.java            Identical structure.
          ParallelTradition.java      Identical structure.
          InfluencedBy.java           Identical structure.

      repository/
        ManuscriptRepository.java     extends Neo4jRepository<ManuscriptNode, String>. Custom @Query Cypher methods.
        ClusterRepository.java        extends Neo4jRepository<ClusterNode, String>. Custom @Query methods.
        CuratorRepository.java        extends JpaRepository<CuratorEntity, UUID>. findByEmail(), existsByEmail().

      dto/
        NodeDTO.java                  11 public fields: id, title, language, approximateDate, summary, condition, originRegion, holdingInstitution, shelfmark, author, script.
        EdgeDTO.java                  7 fields: id, source, target, type, confidence, description, scholarlySource.
        GraphPayload.java             Wrapper: { List<NodeDTO> nodes, List<EdgeDTO> edges }.
        ClusterDTO.java               5 fields: id, name, description, era, tradition.
        LoginRequest.java             { email (validated), password (not blank) }.
        LoginResponse.java            { token, email, name, role }.
        RegisterRequest.java          { email (validated), name, institution, password (min 8 chars) }.
        ManuscriptRequest.java        All manuscript fields + clusterId. @Pattern on language code.
        RelationshipRequest.java      sourceId, targetId, type (@Pattern enum), confidence (@Pattern enum), description, scholarlySource.
        ErrorResponse.java            { status, error, message, timestamp, Map<String,String> fieldErrors }.

      service/
        AuthService.java              register() hashes password, saves curator, returns JWT. login() checks BCrypt, returns JWT.
        ManuscriptService.java        create/update/publish manuscripts. addRelationship (switch on 6 types). toNodeDTO() mapper.
        GraphService.java             THE MOST IMPORTANT. getAllClusters(). getGraphForCluster(). getGraphFromManuscript(). buildPayload().

      controller/
        PublicManuscriptController.java  @RequestMapping("/api/public"). 5 GET endpoints. No auth.
        AuthController.java              @RequestMapping("/api/auth"). POST /register (201), POST /login (200).
        CuratorController.java           @RequestMapping("/api/curator"). 4 endpoints. All require JWT.

      security/
        JwtUtil.java                  generateToken(email,role)->JWT string. isTokenValid(token)->boolean. extractEmail/Role(token)->String.
        JwtFilter.java                extends OncePerRequestFilter. Runs on every request. Validates JWT. Sets SecurityContext.

      exception/
        GlobalExceptionHandler.java   @RestControllerAdvice. Catches all exceptions. Returns consistent ErrorResponse JSON.

    src/main/resources/
      application.properties          spring.datasource.*, spring.neo4j.*, jwt.secret, jwt.expiration-ms, spring.profiles.active.
      db/migration/
        V1__create_curators.sql       Flyway migration. CREATE TABLE curators (...). CREATE INDEX on email.

  frontend/
    package.json                      Dependencies: @xyflow/react, axios, react-router-dom, zustand. DevDeps: vite, typescript, @vitejs/plugin-react.
    tsconfig.json                     TypeScript settings. Strict mode. @/ alias pointing to src/.
    vite.config.ts                    Vite config. Proxies /api -> http://localhost:8080 in dev. @/ alias.
    index.html                        Single HTML file. <div id="root">. Loads src/main.tsx.
    Dockerfile                        2-stage: node:20 builds dist/, nginx:1.27 serves it.
    nginx.conf                        Serves static assets (1y cache), proxies /api/ to backend:8080, SPA fallback.
    src/
      main.tsx                        Entry. createRoot(document.getElementById('root')).render(<StrictMode><App/></StrictMode>).
      index.css                       CSS custom properties: --color-bg, --color-primary, --font-serif, --radius, etc.
      App.tsx                         BrowserRouter with Routes. "/" -> Home, "/cluster/:id" -> ClusterView, "*" -> redirect to "/".
      types/index.ts                  Interfaces: NodeDTO, EdgeDTO, GraphPayload, ClusterDTO, Confidence, RelationshipType, ManuscriptNodeData. Maps: CONFIDENCE_STYLE, REL_LABELS.
      api/api.ts                      axios.create({ baseURL: '/api' }). Request interceptor attaches JWT from localStorage. getClusters, getClusterGraph, getManuscript, getNeighbors, searchManuscripts, login, register.
      store/useGraphStore.ts          Zustand create(). State: clusters, graphPayload, selectedNode, loading, error. Setters. Derived: getNodeById(), getEdgesForNode().
      components/
        ManuscriptGraph.tsx           useNodesState/useEdgesState. layoutNodes() BFS. ReactFlow with custom nodeTypes={manuscript: NodeCard} and edgeTypes={manuscript: ManuscriptEdge}. Controls, MiniMap, Background.
        NodeCard.tsx                  memo(). Handle top/bottom. Language badge. Serif title. Date. Selected styling.
        SidePanel.tsx                 Fixed positioned aside. Slide-in animation. dl grid for metadata. Summary. Connections list with ConfidenceBadge. Backdrop div to close.
      pages/
        Home.tsx                      useEffect -> getClusters() -> setClusters(). Grid of ClusterCard buttons. Navigate on click.
        ClusterView.tsx               useEffect on :id param -> getClusterGraph() -> setGraphPayload(). Renders ManuscriptGraph + SidePanel + Legend. Loading/error overlay.

---
## 5. THE TWO DATABASES - WHY TWO?

RULE: Use the right tool for the SHAPE of your data.

NEO4J IS RIGHT FOR MANUSCRIPTS because:
  Manuscripts are GRAPH-SHAPED data. A manuscript exists in relation to others.
  "This is a translation of that, which was copied from this, which influenced that."
  Neo4j stores this natively. Query: "all manuscripts within 3 hops" = direct edge traversal.
  In PostgreSQL the same thing needs: manuscripts table + relationships table + multi-level JOINs.
  Neo4j makes graph traversal its primary feature, not an afterthought.

POSTGRESQL IS RIGHT FOR CURATOR ACCOUNTS because:
  Curator login is FLAT RELATIONAL data. One row per curator. Simple lookup by email.
  No relationships between curators. It's just a table.
  ACID transactions: a failed registration either fully succeeds or fully fails (no half-created accounts).
  Flyway gives a clean versioned schema history. Works perfectly with JPA/Hibernate.

THE PRACTICAL SEPARATION:
  "Who is allowed to log in?" ----------------------> Ask PostgreSQL
  "What manuscripts exist and how are connected?" --> Ask Neo4j
  These two questions are completely independent.
  No data ever crosses between the two databases.

---
## 6. THE DATA - WHAT GETS STORED

MANUSCRIPTNODE (Neo4j) - all fields:
  id                  UUID string, auto-generated by Neo4j on save
  title               "Historia Regum Britanniae", "Roman de Brut", "Brut"
  originalTitle       Title in original language (may differ from display title)
  language            ISO 639-1/2 code: la=Latin, fro=Old French, enm=Middle English, cy=Welsh, en=English
  script              Physical writing style: "Caroline minuscule", "Gothic textura", "Modern typography"
  approximateDate     Human-readable string: "c. 1138", "c. 1155-1165", "1476"
  dateLower           Integer: 1130 (start of scholarly date range)
  dateUpper           Integer: 1145 (end of scholarly date range)
  originRegion        "Monmouth, Wales / Oxford", "Normandy, France", "Worcestershire, England"
  scribe              Who physically copied the text: "Adam Pinkhurst (attr.)", null if unknown
  author              Who composed the text: "Geoffrey of Monmouth", "Wace", "Layamon"
  holdingInstitution  Where it physically is today: "Bodleian Library, University of Oxford"
  shelfmark           Library catalog reference: "MS Rawlinson B 148", "EL 26 C 9"
  condition           Physical state of the manuscript: "good", "fair", "excellent", "damaged", "fragmentary"
  summary             Long description of the manuscript's scholarly significance (several paragraphs)
  contentUrl          URL to scanned content pages (empty now, architecture is S3-ready)
  wordCount           Integer word count of the manuscript text
  isPublished         Boolean. false = curator draft, true = visible to public. Defaults to false on create.
  createdAt           LocalDateTime. When a curator added it to the system.
  cluster             Reference to the ClusterNode this belongs to (via BELONGS_TO relationship)

CLUSTERNODE (Neo4j):
  id          UUID string, auto-generated
  name        "Historia Regum Britanniae", "The Canterbury Tales"
  description Long text describing the textual tradition
  era         "Medieval", "Late Medieval", "Renaissance", "Early Modern"
  tradition   "Arthurian / British Chronicle", "Middle English Literary"

CURATORENTITY (PostgreSQL):
  id            UUID, generated by PostgreSQL DEFAULT gen_random_uuid()
  email         TEXT UNIQUE NOT NULL - the login identifier
  name          TEXT NOT NULL - display name
  institution   TEXT (nullable) - "University of Oxford", "British Library"
  role          TEXT DEFAULT 'curator' - designed to support admin/curator/reader later
  password_hash TEXT NOT NULL - BCrypt hash. THE PLAIN PASSWORD IS NEVER STORED.
  created_at    TIMESTAMPTZ DEFAULT now()

RELATIONSHIP PROPERTIES (all 6 types share identical structure):
  id              Long - Neo4j internal relationship ID, auto-assigned
  target          Reference to the target ManuscriptNode (the "to" end of the arrow)
  confidence      String enum: "established" | "probable" | "speculative"
  description     "Wace explicitly acknowledges Geoffrey of Monmouth in the prologue."
  scholarlySource "Weiss, J. (2002). Wace's Roman de Brut. University of Exeter Press."

NOTE ON CONFIDENCE:
  established  = scholarly consensus, directly documented in primary sources
  probable     = strong circumstantial evidence, most scholars agree
  speculative  = plausible hypothesis, debated in literature

---
## 7. NEO4J GRAPH MODEL - NODES AND EDGES

NODE TYPES:
  (:Manuscript) - a single physical version of a text
  (:Cluster)    - a thematic/traditional grouping of manuscripts

RELATIONSHIP TYPES (all Manuscript->Manuscript except BELONGS_TO):
  [:TRANSLATED_FROM]    Source is a translation of target. "This French text was translated from this Latin text."
  [:COPIED_FROM]        Source is a direct copy of target. "This manuscript was copied from that one."
  [:COMMENTARY_ON]      Source is a commentary about target. "This text comments on and explains that text."
  [:DERIVED_FROM]       Source loosely derives from target. "This edition is based on that manuscript tradition."
  [:PARALLEL_TRADITION] Both evolved independently from the same ancestor. "These two vernacular traditions both derive from the Latin original but developed separately."
  [:INFLUENCED_BY]      Source was stylistically influenced by target. "The author read and was influenced by this earlier work."
  [:BELONGS_TO]         Manuscript -> Cluster. NO extra properties. Just the arrow.

BELONGS_TO IS SPECIAL:
  ALL other relationships are Manuscript-to-Manuscript.
  BELONGS_TO is Manuscript-to-Cluster.
  It has NO extra properties (no confidence, no description).
  A Cluster does not have a list of manuscripts - instead manuscripts POINT to the cluster.
  To get all manuscripts in a cluster, Neo4j traverses incoming BELONGS_TO arrows.

VISUAL OF CLUSTER 1 (Historia Regum Britanniae):

  [Historia Regum Britanniae, Latin, c.1138]
         |          |          |
         |          |          +---DERIVED_FROM(established)---> [Variant Version, 1951]
         |          |
         |          +---TRANSLATED_FROM(established)---> [Brut y Brenhinedd, Welsh, c.1330]
         |                                                        |
         |                                               PARALLEL_TRADITION(probable)
         |                                                        |
         +---TRANSLATED_FROM(established)---> [Roman de Brut, Old French, c.1155]
                                                        |
                                              TRANSLATED_FROM(established)
                                                        |
                                                        v
                                              [Brut, Middle English, c.1190-1215] <---+

  All 5 manuscripts also connect to [Cluster: Historia Regum Britanniae] via BELONGS_TO.

---
## 8. SEED DATA - WHAT IS PRE-LOADED

FILE: config/DevDataSeeder.java
TRIGGER: @Profile("dev") + implements CommandLineRunner -> runs on startup
GUARD: checks manuscriptRepository.count() > 0 -> if data exists, skips entirely

CLUSTER 1: Historia Regum Britanniae
  1. Historia Regum Britanniae        | la  | c.1138      | Geoffrey of Monmouth | Bodleian Library, Oxford - MS Rawlinson B 148
  2. Roman de Brut                    | fro | c.1155      | Wace                 | Bibliotheque nationale de France - MS fr.1450
  3. Brut                             | enm | c.1190-1215 | Layamon              | British Library - Cotton MS Caligula A IX
  4. Brut y Brenhinedd                | cy  | c.1330-1350 | (unknown)            | National Library of Wales - Peniarth MS 44
  5. Historia...Variant Version       | en  | 1951        | Jacob Hammer (ed.)   | Mediaeval Academy of America - Pub.No.57

  Seeded relationships:
    Roman de Brut ------TRANSLATED_FROM(established)-----> Historia Regum Britanniae
      (Wace acknowledges Geoffrey explicitly in his prologue - Brook & Leslie 1963)
    Brut -----------TRANSLATED_FROM(established)---------> Roman de Brut
      (Layamon names Wace's French book as one of his three sources)
    Brut y Brenhinedd --TRANSLATED_FROM(established)----> Historia Regum Britanniae
      (Welsh Brut follows the Latin text closely - Roberts 1971)
    Brut y Brenhinedd --PARALLEL_TRADITION(probable)----> Brut
      (Welsh and Middle English traditions developed independently from same Latin source)
    Variant Version ----DERIVED_FROM(established)--------> Historia Regum Britanniae
      (Hammer's edition is a critical edition of the Latin variant manuscript tradition)

CLUSTER 2: The Canterbury Tales
  6. Canterbury Tales (Hengwrt)       | enm | c.1400-1410 | Adam Pinkhurst (attr.) | Nat.Library of Wales - Peniarth MS 392D
  7. Canterbury Tales (Ellesmere)     | enm | c.1400-1405 | Adam Pinkhurst (attr.) | Huntington Library - EL 26 C 9
  8. Canterbury Tales (Caxton 1st ed.)| enm | 1476        | (printed)              | British Library - IB.55021

  Seeded relationships:
    Ellesmere --COPIED_FROM(probable)----> Hengwrt
      (Both share scribe Adam Pinkhurst, textual relationship debated - Mooney 2006)
    Caxton ----DERIVED_FROM(speculative)-> Ellesmere
      (Caxton's copy text not conclusively identified - Blake 1985)

ALL 8 NODES: isPublished = true. Visible to public immediately on first startup.

---
## 9. BACKEND ARCHITECTURE - LAYER BY LAYER

REQUEST JOURNEY (top to bottom):

  HTTP Request arrives
        |
        v
  [JwtFilter - runs BEFORE every request]
    -> Checks Authorization header for "Bearer <token>"
    -> If token present: JwtUtil.isTokenValid() verifies HMAC signature + expiry
    -> If valid: extracts email and role, sets authentication in SecurityContextHolder
    -> If no token / invalid token: does nothing (request continues as anonymous)
        |
        v
  [SecurityConfig - Spring Security decision point]
    -> /api/public/**  -> permitAll() -> anyone through
    -> /api/auth/**    -> permitAll() -> anyone through
    -> /api/curator/** -> hasRole("CURATOR") -> checks SecurityContextHolder
                          if ROLE_CURATOR found -> through
                          if not found -> 403 Forbidden
    -> anything else   -> denyAll() -> 403
        |
        v
  [CONTROLLER LAYER]
    -> Receives the HttpServletRequest, path variables, request body
    -> @Valid on @RequestBody triggers Bean Validation
    -> Calls the appropriate service method
    -> Wraps result in ResponseEntity and returns
    -> NO business logic here
        |
        v
  [SERVICE LAYER] - ALL business logic lives here
    -> Processes data, applies rules, coordinates between repositories
    -> AuthService: BCrypt, JWT generation
    -> ManuscriptService: CRUD, relationship management, search
    -> GraphService: Cypher queries, data structure transformations
    -> NO HTTP-specific code here (no HttpServletRequest, no ResponseEntity)
        |
        v
  [REPOSITORY LAYER] - pure database access
    -> You write the interface, Spring generates the implementation
    -> ManuscriptRepository and ClusterRepository: Spring Data Neo4j -> Cypher -> Neo4j
    -> CuratorRepository: Spring Data JPA -> SQL -> PostgreSQL
    -> NO business logic here
        |
        +--------+--------+
        v                 v
     Neo4j            PostgreSQL

  [GlobalExceptionHandler] - safety net OUTSIDE the normal flow
    -> @RestControllerAdvice means Spring routes ALL uncaught exceptions here
    -> Catches: MethodArgumentNotValidException, IllegalArgumentException,
       NoSuchElementException, AccessDeniedException, Exception (catch-all)
    -> Always returns ErrorResponse JSON, never a stack trace

---
## 10. EVERY REST ENDPOINT EXPLAINED

PUBLIC ENDPOINTS (no Authorization header needed):

  GET /api/public/clusters
    Controller: PublicManuscriptController.getAllClusters()
    Service:    GraphService.getAllClusters()
    Repository: ClusterRepository.findAllWithPublishedManuscripts()
    Cypher:     MATCH (c:Cluster)<-[:BELONGS_TO]-(m:Manuscript) WHERE m.isPublished=true RETURN DISTINCT c
    Response:   200 OK, Array of ClusterDTO
    Used by:    Home page on load

  GET /api/public/clusters/{id}/graph
    Controller: PublicManuscriptController.getClusterGraph(id)
    Service:    GraphService.getGraphForCluster(id)
    Repository: clusterRepository.findById(id) [verify exists] + manuscriptRepository.findPublishedByClusterId(id)
    Cypher:     MATCH (c:Cluster {id:})<-[:BELONGS_TO]-(m:Manuscript) WHERE m.isPublished=true
                OPTIONAL MATCH (m)-[r]-(connected:Manuscript) WHERE connected.isPublished=true
                RETURN m, collect(r), collect(connected)
    Response:   200 OK, GraphPayload { nodes: NodeDTO[], edges: EdgeDTO[] }
    Throws:     404 if cluster not found
    Used by:    ClusterView page on load

  GET /api/public/manuscripts/{id}
    Controller: PublicManuscriptController.getManuscript(id)
    Service:    ManuscriptService.getPublishedById(id)
    Repository: manuscriptRepository.findByIdAndIsPublishedTrue(id)
    Response:   200 OK, NodeDTO
    Throws:     404 if not found or not published
    Used by:    Direct manuscript links

  GET /api/public/manuscripts/{id}/neighbors
    Controller: PublicManuscriptController.getNeighbors(id)
    Service:    ManuscriptService.getNeighbours(id)
    Repository: manuscriptRepository.findWithNeighbours(id)
    Cypher:     MATCH (m:Manuscript {id:})-[r]-(connected:Manuscript) WHERE connected.isPublished=true
                RETURN m, collect(r), collect(connected)
    Logic:      collectAllRelated(node) walks all 6 relationship lists, filters published, deduplicates
    Response:   200 OK, NodeDTO[]
    Used by:    Getting just the direct neighbours without the full cluster graph

  GET /api/public/search?q=&language=&era=
    Controller: PublicManuscriptController.search(q, language, era)
    Service:    ManuscriptService.search(q, language)
    Repository: manuscriptRepository.search(query, language)
    Cypher:     MATCH (m:Manuscript) WHERE m.isPublished=true AND (
                  toLower(m.title) CONTAINS toLower() OR
                  toLower(m.originalTitle) CONTAINS toLower() OR
                  toLower(m.summary) CONTAINS toLower() OR
                  toLower(m.author) CONTAINS toLower() OR
                  toLower(m.originRegion) CONTAINS toLower())
                AND ( IS NULL OR m.language = )
                RETURN m
    Note:       era param is accepted but not yet wired to the Cypher query
    Response:   200 OK, NodeDTO[]

AUTH ENDPOINTS:

  POST /api/auth/register
    Controller: AuthController.register(RegisterRequest)
    Validation: @Valid - email format, name not blank, password min 8 chars
    Service:    AuthService.register()
      1. curatorRepository.existsByEmail(email) -> throws 400 if already exists
      2. new CuratorEntity() -> set fields
      3. passwordEncoder.encode(password) -> BCrypt strength 12 hash
      4. curatorRepository.save(curator) -> INSERT into PostgreSQL
      5. jwtUtil.generateToken(email, "curator") -> JWT string (24h expiry)
      6. return LoginResponse { token, email, name, role }
    Response:   201 Created, LoginResponse
    Throws:     400 if email taken or validation fails

  POST /api/auth/login
    Controller: AuthController.login(LoginRequest)
    Validation: @Valid - email format, password not blank
    Service:    AuthService.login()
      1. curatorRepository.findByEmail(email) -> throws 400 "Invalid email or password." if not found
      2. passwordEncoder.matches(rawPassword, storedHash) -> throws 400 if false
         NOTE: same error message for wrong email AND wrong password (security: don't leak which)
      3. jwtUtil.generateToken(email, role) -> JWT string
      4. return LoginResponse { token, email, name, role }
    Response:   200 OK, LoginResponse

CURATOR ENDPOINTS (require Authorization: Bearer <jwt> with ROLE_CURATOR):

  POST /api/curator/manuscripts
    Controller: CuratorController.createManuscript(ManuscriptRequest)
    Validation: title not blank, language matches ^[a-z]{2,3}$
    Service:    ManuscriptService.create()
      1. new ManuscriptNode()
      2. applyRequest(node, request) -> copies all DTO fields to entity
      3. node.setIsPublished(false) -> starts as draft
      4. node.setCreatedAt(now)
      5. if request.getClusterId() != null: clusterRepository.findById() -> node.setCluster()
      6. manuscriptRepository.save(node) -> Neo4j CREATE node + BELONGS_TO relationship
      7. toNodeDTO(savedNode)
    Response:   201 Created, NodeDTO
    Throws:     404 if clusterId not found, 400 if validation fails

  PUT /api/curator/manuscripts/{id}
    Controller: CuratorController.updateManuscript(id, ManuscriptRequest)
    Service:    ManuscriptService.update()
      1. manuscriptRepository.findById(id) -> throws 404 if not found
      2. applyRequest(node, request) -> overwrites all fields
      3. if clusterId provided: find cluster, update relationship
      4. manuscriptRepository.save(node)
    Response:   200 OK, NodeDTO

  PATCH /api/curator/manuscripts/{id}/publish
    Controller: CuratorController.publishManuscript(id)
    Service:    ManuscriptService.publish()
      1. manuscriptRepository.findById(id) -> throws 404 if not found
      2. node.setIsPublished(true)
      3. manuscriptRepository.save(node)
    Response:   200 OK, NodeDTO
    Effect:     Manuscript now appears in all public queries

  POST /api/curator/relationships
    Controller: CuratorController.addRelationship(RelationshipRequest)
    Validation: sourceId/targetId not blank, type must be one of 6 valid types, confidence must be established/probable/speculative
    Service:    ManuscriptService.addRelationship()
      1. manuscriptRepository.findById(sourceId) -> throws 404 if not found
      2. manuscriptRepository.findById(targetId) -> throws 404 if not found
      3. switch(request.getType()): creates appropriate relationship object
         e.g. case "TRANSLATED_FROM": new TranslatedFrom(); set target, confidence, description, scholarlySource
      4. source.getTranslatedFrom().add(rel) [or appropriate list]
      5. manuscriptRepository.save(source) -> Neo4j creates the relationship in the graph
    Response:   201 Created, no body

---
## 11. SECURITY - HOW AUTH WORKS END TO END

STEP 1: Password Hashing (on register)
  BCryptPasswordEncoder.encode("mypassword123")
  -> BCrypt generates a random salt, hashes the password + salt together
  -> Produces something like: ".hashedValueHere"
  -> This hash is stored in PostgreSQL. THE ORIGINAL PASSWORD IS DISCARDED.
  -> BCrypt is one-way: you cannot reverse the hash to get the password back.

STEP 2: Password Verification (on login)
  BCryptPasswordEncoder.matches("mypassword123", "")
  -> BCrypt extracts the salt from the stored hash
  -> Re-hashes "mypassword123" using that same salt
  -> Compares the result to the stored hash
  -> Returns true if they match, false if not
  -> NEVER decrypts anything

STEP 3: JWT Generation (on successful login/register)
  JwtUtil.generateToken("sarah@oxford.ac.uk", "curator")
  -> Builds a JWT with three parts:

  HEADER (Base64):   { "alg": "HS256" }
  PAYLOAD (Base64):  { "sub": "sarah@oxford.ac.uk", "role": "curator", "iat": 1234567890, "exp": 1234654290 }
  SIGNATURE:         HMAC-SHA256( base64(header) + "." + base64(payload), JWT_SECRET )

  The signature PROVES the payload hasn't been tampered with.
  If someone changes "curator" to "admin" in the payload, the signature check fails.
  JWT_SECRET is the key that only the server knows.

  FULL TOKEN: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzYXJhaEB....<signature>

STEP 4: JWT Validation (JwtFilter, every request)
  Authorization: Bearer eyJhbGciOiJIUzI1NiJ9....

  JwtFilter.doFilterInternal():
    1. request.getHeader("Authorization") -> "Bearer eyJhbGci..."
    2. token = header.substring(7) -> strips "Bearer "
    3. JwtUtil.isTokenValid(token):
       -> Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)
       -> If signature wrong: JwtException caught -> return false
       -> If expired: getExpiration().after(now) -> return false
       -> If valid: return true
    4. extractEmail(token) -> "sarah@oxford.ac.uk"
    5. extractRole(token) -> "curator"
    6. new UsernamePasswordAuthenticationToken("sarah@oxford.ac.uk", null, [ROLE_CURATOR])
    7. SecurityContextHolder.getContext().setAuthentication(auth)

  SecurityConfig then evaluates:
    .requestMatchers("/api/curator/**").hasRole("CURATOR")
    -> Spring checks SecurityContextHolder for ROLE_CURATOR
    -> Found -> request proceeds to controller

WHAT FAILS:
  No token:          JwtFilter does nothing, SecurityContext is anonymous
                     -> /api/public/** still works (permitAll)
                     -> /api/curator/** returns 403

  Expired token:     isTokenValid() returns false, no authentication set
                     -> same as no token

  Tampered token:    signature check fails, JwtException caught, isTokenValid() returns false

  Valid token, wrong role: (not possible currently since all curators have role="curator"
                     but the architecture supports it: hasRole("ADMIN") would also work)

---
## 12. DATA FLOW - A COMPLETE REQUEST TRACE

Scenario: Public user opens the Historia Regum Britanniae cluster graph page.

--- FRONTEND SIDE ---

1. User is on Home page. Sees cluster card "Historia Regum Britanniae".
   Clicks "Explore graph" button.

2. React Router: navigate("/cluster/abc-123-uuid")

3. ClusterView.tsx mounts. useEffect runs (dependency: [id]):
     setLoading(true)          [Zustand: loading becomes true, spinner shows]
     setError(null)            [Zustand: clear any previous error]
     setGraphPayload({...})    [Zustand: clear previous cluster's graph]
     getClusterGraph("abc-123-uuid")  [api.ts]
       -> axios.get("/api/public/clusters/abc-123-uuid/graph")
       -> Vite dev server proxy: rewrites to http://localhost:8080/api/public/clusters/abc-123-uuid/graph
       -> HTTP GET request sent

--- NETWORK ---

4. HTTP GET /api/public/clusters/abc-123-uuid/graph
   No Authorization header.

--- BACKEND SIDE ---

5. JwtFilter.doFilterInternal():
     request.getHeader("Authorization") -> null
     No token -> does nothing -> filterChain.doFilter() continues

6. SecurityConfig:
     /api/public/** -> permitAll() -> allowed through

7. PublicManuscriptController.getClusterGraph("abc-123-uuid"):
     returns ResponseEntity.ok(graphService.getGraphForCluster("abc-123-uuid"))

8. GraphService.getGraphForCluster("abc-123-uuid"):
   a. clusterRepository.findById("abc-123-uuid")
      Spring Data Neo4j -> Cypher: MATCH (c:Cluster) WHERE c.id = "abc-123-uuid" RETURN c
      -> found: ClusterNode { id, name: "Historia Regum Britanniae", ... }

   b. manuscriptRepository.findPublishedByClusterId("abc-123-uuid")
      Cypher:
        MATCH (c:Cluster {id: "abc-123-uuid"})<-[:BELONGS_TO]-(m:Manuscript)
        WHERE m.isPublished = true
        OPTIONAL MATCH (m)-[r]-(connected:Manuscript)
        WHERE connected.isPublished = true
        RETURN m, collect(r), collect(connected)

      Neo4j executes this graph traversal.
      Spring Data Neo4j maps results to List<ManuscriptNode>.
      Each ManuscriptNode has its relationship lists populated:
        latinMS.getTranslatedFrom() = []  (nothing translated FROM the Latin, the Latin is the source)
        frenchMS.getTranslatedFrom() = [ TranslatedFrom{ target=latinMS, confidence="established", description="...", scholarlySource="..." } ]
        englishMS.getTranslatedFrom() = [ TranslatedFrom{ target=frenchMS, confidence="established", ... } ]
        welshMS.getTranslatedFrom() = [ TranslatedFrom{ target=latinMS, confidence="established", ... } ]
        welshMS.getParallelTradition() = [ ParallelTradition{ target=englishMS, confidence="probable", ... } ]
        scholarlyMS.getDerivedFrom() = [ DerivedFrom{ target=latinMS, confidence="established", ... } ]
      Returns: List of 5 ManuscriptNode objects

   c. buildPayload(List<ManuscriptNode> with 5 nodes):

      Step 1: Build HashSet<String> nodeIds
        nodeIds = { "latin-id", "french-id", "english-id", "welsh-id", "scholarly-id" }

      Step 2: Build List<NodeDTO> nodeDTOs
        for each ManuscriptNode node in the list:
          nodeDTOs.add( NodeDTO.builder()
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
            .build() )
          // notice: isPublished, createdAt, wordCount, contentUrl, cluster NOT included
        nodeDTOs now has 5 NodeDTO objects

      Step 3: Build List<EdgeDTO> edges
        for each ManuscriptNode node in the list:
          extractEdges(node, nodeIds, edges):

            // TRANSLATED_FROM relationships
            for TranslatedFrom rel : node.getTranslatedFrom():
              if rel.getTarget() != null AND nodeIds.contains(rel.getTarget().getId()):
                // YES: french-id is in nodeIds, english-id is in nodeIds, etc.
                edges.add( EdgeDTO.builder()
                  .id( String.valueOf(rel.getId()) )   // Neo4j internal rel ID as string
                  .source( node.getId() )              // the "from" manuscript
                  .target( rel.getTarget().getId() )   // the "to" manuscript
                  .type("TRANSLATED_FROM")
                  .confidence( rel.getConfidence() )   // "established"
                  .description( rel.getDescription() )
                  .scholarlySource( rel.getScholarlySource() )
                  .build() )

            // Same for COPIED_FROM, COMMENTARY_ON, DERIVED_FROM, PARALLEL_TRADITION, INFLUENCED_BY

        edges now has 5 EdgeDTO objects (one per seeded relationship)

      Returns: GraphPayload { nodes: [5 NodeDTOs], edges: [5 EdgeDTOs] }

9. Controller: ResponseEntity.ok(graphPayload)
   Jackson serializes GraphPayload to JSON:
     {
       "nodes": [ { "id":"latin-id", "title":"Historia...", "language":"la", ... }, ... ],
       "edges": [ { "id":"1", "source":"french-id", "target":"latin-id", "type":"TRANSLATED_FROM", "confidence":"established", ... }, ... ]
     }
   HTTP 200 OK response with JSON body

--- NETWORK ---

10. Response arrives at Vite dev server proxy, forwarded to the React app

--- FRONTEND SIDE ---

11. axios promise resolves with { data: GraphPayload }
    api.ts returns r.data (the typed GraphPayload)

12. ClusterView.tsx .then() callback:
      setGraphPayload(payload)   [Zustand: stores the full payload]
      setLoading(false)          [Zustand: spinner disappears]

13. Zustand state change triggers React re-render of ClusterView.tsx
    graphPayload is now non-null and non-empty -> renders ManuscriptGraph

14. ManuscriptGraph.tsx receives payload as prop.
    useMemo(() => layoutNodes(payload), [payload]) runs:

      layoutNodes():
        // Build adjacency map for BFS
        inDegree = Map { "latin-id"->0, "french-id"->1, "english-id"->1, "welsh-id"->1, "scholarly-id"->1 }
        // Root nodes (inDegree=0): just latin-id
        generation = Map { "latin-id" -> 0 }
        queue = ["latin-id"]

        edgesBySource = Map {
          "french-id": ["latin-id"],
          "english-id": ["french-id"],
          "welsh-id": ["latin-id", "english-id"],
          "scholarly-id": ["latin-id"]
        }

        BFS loop:
          Process "latin-id" (gen 0):
            children via edgesBySource.get("latin-id") = ["french-id"... wait, that's reversed]
            Actually edgesBySource maps SOURCE to TARGETS:
            "french-id" -> TRANSLATED_FROM -> "latin-id" (target)
            So edgesBySource.get("french-id") = ["latin-id"]

            Rethinking: root nodes have inDegree=0 = no incoming edges = they ARE sources
            "latin-id" has inDegree=0, so it's generation 0
            edgesBySource maps source->targets for outgoing edges
            But latin IS the target (manuscripts point FROM newer TO older)
            So latin appears as a TARGET in many edges, meaning high inDegree for the targets

            Actually inDegree counts how many edges point TO each node.
            latin-id is pointed TO by: french, english, welsh, scholarly -> inDegree=4? No...
            Let's reread: inDegree.set(e.target, ..+1) increments the TARGET's count.
            french->TRANSLATED_FROM->latin: target=latin -> latin's inDegree++
            english->TRANSLATED_FROM->french: target=french -> french's inDegree++
            welsh->TRANSLATED_FROM->latin: target=latin -> latin's inDegree again++
            welsh->PARALLEL_TRADITION->english: target=english -> english's inDegree++
            scholarly->DERIVED_FROM->latin: target=latin -> latin's inDegree again++

            So: latin has inDegree=3, french=1, english=1, welsh=0, scholarly=0
            Generation 0 (no incoming): welsh, scholarly
            Generation 1: any node reachable from gen-0 nodes

            Layout result example:
              welsh (gen 0):     x=0, y=-70 (first in col 0, 2 items, centered)
              scholarly (gen 0): x=0, y=+70
              ...and BFS continues

        Position formula: x = generation * 280, y = rowIndex * 140 - centered

        Returns:
          nodes = [ ReactFlow Node { id, type:"manuscript", position:{x,y}, data:{manuscript:NodeDTO} }, ... ]
          edges = [ ReactFlow Edge { id, source, target, type:"manuscript", data:edgeDTO, markerEnd:{ArrowClosed} }, ... ]

15. useNodesState(initialNodes) and useEdgesState(initialEdges) set up the managed state.

16. ReactFlow renders:
    - 5 manuscript nodes positioned by the BFS layout
    - 5 edges as bezier curves
    - For each edge, ManuscriptEdge component renders:
        getBezierPath(sourceX,sourceY,targetX,targetY) -> SVG path
        color = EDGE_COLOR[confidence] -> gold/blue/muted based on "established"/"probable"/"speculative"
        dash = STROKE_DASH[confidence] -> undefined/6-3/2-5
        floating label div: REL_LABELS[type] -> "Translated from"
        arrowhead marker at target end
    - Background grid, Controls (zoom in/out/fit), MiniMap

17. USER CLICKS the "Historia Regum Britanniae" Latin node

18. onNodeClick(event, reactFlowNode) fires:
      getNodeById(reactFlowNode.id) -> useGraphStore: graphPayload.nodes.find(n => n.id === reactFlowNode.id)
      Returns: NodeDTO { id:"latin-id", title:"Historia Regum Britanniae", language:"la", ... }
      setSelectedNode(manuscript) -> Zustand state update

19. SidePanel.tsx re-renders (it reads selectedNode from Zustand):
      selectedNode is no longer null -> panel renders

      Metadata section (dl grid):
        language: "la" -> LANGUAGE_NAMES["la"] = "Latin"
        approximateDate: "c. 1138"
        originRegion: "Monmouth, Wales / Oxford"
        author: "Geoffrey of Monmouth"
        script: "Caroline minuscule"
        holdingInstitution: "Bodleian Library, University of Oxford"
        shelfmark: "MS Rawlinson B 148"
        condition: "good"

      Summary section:
        "The foundational Latin prose chronicle by Geoffrey of Monmouth..."

      Connections section:
        getEdgesForNode("latin-id"):
          -> graphPayload.edges.filter(e => e.source==="latin-id" || e.target==="latin-id")
          -> Finds all 5 edges (latin is target of french, welsh; source connections exist)
          Shows each with: type label, confidence badge (gold/blue/muted), description, scholarly citation

20. USER SEES the slide-in panel with full manuscript details.
    Clicking anywhere outside or the X closes it (setSelectedNode(null)).
