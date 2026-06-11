create TABLE curators( id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       name VARCHAR(255) NOT NULL,
                       institution VARCHAR(255),
                       role VARCHAR(100),
                       hash_password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL);