-- init-multiple-dbs.sql
-- Creates individual databases for each microservice in the UrbanSphere monorepo

-- Use the default superuser created by the image to create DBs
CREATE DATABASE urbansphere_auth;
CREATE DATABASE urbansphere_user;
CREATE DATABASE urbansphere_location;
CREATE DATABASE urbansphere_routing;
CREATE DATABASE urbansphere_traffic;
CREATE DATABASE urbansphere_safety;
CREATE DATABASE urbansphere_notification;
CREATE DATABASE urbansphere_society;
CREATE DATABASE urbansphere_api_gateway;
CREATE DATABASE urbansphere_analytics;

-- Optionally create extensions in each DB (Postgres needs extension installed per-db)
\connect urbansphere_auth
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_user
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_location
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_routing
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_traffic
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_safety
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_notification
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_society
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_api_gateway
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\connect urbansphere_analytics
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- switch back to default
\connect postgres
