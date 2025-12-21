-- ===========================================
-- UrbanSphere - Auth Service Schema (Docker Init)
-- ===========================================

-- Switch to the correct DB
\c urbansphere_auth;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===========================================
-- USERS TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS auth_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Trigger to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_update_timestamp ON auth_users;
CREATE TRIGGER trg_update_timestamp
BEFORE UPDATE ON auth_users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();

-- ===========================================
-- ROLES TABLE
-- ===========================================
CREATE TABLE IF NOT EXISTS auth_roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO auth_roles (name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN')
ON CONFLICT DO NOTHING;

-- ===========================================
-- USER ↔ ROLE Mapping (Many-to-Many)
-- ===========================================
CREATE TABLE IF NOT EXISTS auth_user_roles (
    user_id UUID NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES auth_roles(id) ON DELETE CASCADE
);

-- ===========================================
-- REFRESH TOKENS
-- ===========================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES auth_users(id) ON DELETE CASCADE
);

-- ===========================================
-- LOGIN ATTEMPTS
-- ===========================================
CREATE TABLE IF NOT EXISTS login_attempts (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    success BOOLEAN NOT NULL,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);
