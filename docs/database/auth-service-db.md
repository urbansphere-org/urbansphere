# UrbanSphere Auth Service — Database Overview

This document provides a concise overview of the Auth Service database schema used for authentication, authorization, session management, and security auditing.

---

# 1. ERD (Short Version)

```mermaid
erDiagram
    auth_users ||--o{ auth_user_roles : has
    auth_roles ||--o{ auth_user_roles : has
    auth_users ||--o{ refresh_tokens : has

    auth_users {
        UUID id PK
        varchar email
        varchar password_hash
        boolean is_email_verified
    }

    auth_roles {
        int id PK
        varchar name
    }

    refresh_tokens {
        UUID id PK
        UUID user_id FK
    }

    login_attempts {
        int id PK
        varchar email
        boolean success
    }



2. Tables (Summary)
2.1 auth_users

Stores user accounts.
Important fields:

id (UUID, PK)

email (unique)

password_hash

is_email_verified

Timestamps with auto-updating updated_at trigger

2.2 auth_roles

Defines system roles.

Seeded roles:

ROLE_USER

ROLE_ADMIN

2.3 auth_user_roles

Many-to-many mapping between users and roles.
Composite PK: (user_id, role_id).
Cascade deletes keep data clean.

2.4 refresh_tokens

Manages session persistence.

Key fields:

refresh_token_hash (hashed only)

expires_at

revoked

One user → many tokens (multiple devices).

2.5 login_attempts

Logs login attempts for security/rate-limiting.

Fields:

email

success

ip_address

No FK to avoid leaking valid emails.

3. Security Highlights

Passwords are hashed (bcrypt/Argon2).

Refresh tokens stored hashed only.

Login attempts help detect brute-force behavior.

Cascading deletes remove related tokens and role entries safely.

4. Migration Notes

Implemented via:

V1__init_auth_schema.sql


Creates:

UUID extension

Users, roles, tokens, attempts

Role seed data

Timestamp trigger

This concise guide covers the core structure and purpose of the Auth Service database.