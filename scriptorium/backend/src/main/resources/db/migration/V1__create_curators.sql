-- Flyway migration V1: create curators table
-- PostgreSQL only — Neo4j schema is managed by Spring Data Neo4j at runtime.

CREATE TABLE IF NOT EXISTS curators (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email        TEXT        UNIQUE NOT NULL,
    name         TEXT        NOT NULL,
    institution  TEXT,
    role         TEXT        NOT NULL DEFAULT 'curator',
    password_hash TEXT       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_curators_email ON curators (email);
