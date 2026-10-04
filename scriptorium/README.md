# Scriptorium

A read-only digital museum for historical manuscripts, visualised as an interactive graph.

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Maven |
| Graph DB | Neo4j 5 (Spring Data Neo4j) |
| Relational DB | PostgreSQL 16 (Spring Data JPA + Flyway) |
| Auth | JWT (jjwt 0.12) + BCrypt |
| Frontend | React 18 + TypeScript + Vite |
| Graph UI | @xyflow/react (React Flow) |
| State | Zustand |
| Container | Docker + Docker Compose |

## Quick Start (Docker)

```bash
cp .env.example .env
# edit .env — set DB_PASSWORD, NEO4J_PASSWORD, JWT_SECRET
docker compose up --build
# Frontend  → http://localhost:5173
# Backend   → http://localhost:8080
# Neo4j UI  → http://localhost:7474
```

## Local Development

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## Seed Data

On first startup in dev profile, Neo4j is seeded with:
- Cluster 1 — Historia Regum Britanniae: 5 manuscripts (Latin original through 20th century scholarly edition)
- Cluster 2 — The Canterbury Tales: 3 manuscripts (Hengwrt, Ellesmere, Caxton first edition)
