# UrbanSphere — Microservice Responsibilities & Bounded Contexts

UrbanSphere follows a strict domain-driven, microservices architecture where each service:

Owns its data

Has a single bounded context

Publishes/consumes Kafka events

Exposes APIs only for its domain

Is stateless (except stateful caches / DB)

Has isolated scaling characteristics

This document formally defines responsibilities for each service and ensures clean boundaries, which is required for architectural clarity, maintainability, and AWS compliance.

## 1. API Gateway (Edge Routing Layer)
Primary Responsibilities

Validate incoming JWTs (public keys from Auth)

Enforce rate limits & quotas

Request authentication & authorization

Route HTTP traffic to internal microservices

API versioning (e.g., /v1/...)

Global request/response normalization

Request timeouts (avoid backend overload)

Non-Responsibilities

❌ No business logic
❌ No direct DB access
❌ No Kafka processing
❌ No caching of business data
❌ No calling third-party APIs

Reason for Separation

Gateway must remain thin, secure, and extremely reliable.
Heavy logic here creates bottlenecks and failure propagation.

## 2. Auth Service
Responsibilities

User registration

Login

Password hashing (BCrypt)

JWT (RS256) generation + signing

Refresh token issuing & rotation

RBAC role & permission management

Suspicious login tracking

Public JWKS endpoint for API Gateway & services

Non-Responsibilities

❌ User profile management
❌ Device metadata
❌ Location ingestion
❌ Notification control

Data Ownership

users (credentials)

roles / permissions

refresh_tokens

Why?

Security must be isolated with minimal domain coupling.
This reduces attack surface and aligns with AWS IAM separation principles.

## 3. User Service
Responsibilities

User profile (name, phone, email)

User preferences (privacy, alert radius, dark mode, etc.)

Device metadata (device type, last-seen info)

Consent management (location sharing consent)

Expose read APIs for other services

Non-Responsibilities

❌ Authentication
❌ Routing
❌ Traffic/Hazard logic
❌ GPS ingestion

Data Ownership

profiles

user_devices

user_preferences

consents

Why?

User profile lifecycle evolves independently; separating it keeps Auth minimal.

## 4. Location Service (High Throughput Ingestion)
Responsibilities

Receive GPS telemetry (HTTP/gRPC)

Validate and normalize coordinates

Map matching / geofence tagging (lightweight)

Publish events → device.location.v1

Maintain recent location cache (Redis)

Provide "latest location" APIs

Non-Responsibilities

❌ Traffic analytics
❌ Hazard detection
❌ Routing
❌ User notifications

Data Ownership

Minimal; optionally short-term Redis store only

Why?

Location ingestion needs to be ultra-lightweight, scalable, and async.

## 5. Traffic Service
Responsibilities

Consume device.location.v1

Compute segment-level speed & density

Maintain rolling & time-bucket aggregates

Provide congestion levels for routing service

Store traffic history in Postgres

Optionally publish traffic.update.v1 (future)

Non-Responsibilities

❌ Routing calculations
❌ Hazard decision-making
❌ User notifications

Data Ownership

traffic_segments

traffic_history

Why?

Traffic requires stateful, CPU-heavy aggregation.
Keeping it separate avoids overwhelming Location service.

## 6. Routing Service
Responsibilities

Generate optimal routes (fastest, safest, shortest)

Integrate traffic + hazard signals

Redis-based caching for acceleration

External routing API calls (ORS/OSRM) when necessary

Publish routing.request.v1 for analytics

Provide route preview/history APIs

Non-Responsibilities

❌ Hazard creation
❌ Traffic ingestion
❌ User profile
❌ Notification logic

Data Ownership

Route history (optional)

Cache keys (Redis)

Why?

Routing has very different scaling needs (compute-heavy bursts).
Must be isolated to avoid impacting ingestion systems.

## 7. Safety Service
Responsibilities

Handle user-reported hazards

Validate hazard metadata (location, severity, type)

Store hazard lifecycle (NEW → VERIFIED → RESOLVED)

Consume device telemetry (optional future auto-detection)

Publish hazard.report.v1

Provide hazard map APIs

Non-Responsibilities

❌ Sending notifications
❌ Routing & traffic calculations
❌ GPS ingestion

Data Ownership

hazards

hazard_history

Why?

Safety workflows require accurate data + moderation, separate from routing/traffic.

## 8. Notification Service
Responsibilities

Consume hazard.report.v1

Determine recipients using:

user preferences

geospatial radius

consent flags

Deliver Push/SMS/Email

Deduplication

Delivery logs with status tracking

Rate-limiting to avoid user spam

Non-Responsibilities

❌ Creating hazard data
❌ Traffic computation
❌ User profile management

Data Ownership

notification_logs

Provider credentials (secure)

Why?

Notification delivery has unique reliability and failure considerations.

## 9. Society Service
Responsibilities

Community (“society”) features

Resident approvals

Building/wing mapping

Community events / bulletin board

Gate logs (future)

Optional events to Kafka

Non-Responsibilities

❌ Traffic
❌ Routing
❌ Safety logic
❌ Notifications

Data Ownership

communities

residents

events

Why?

Community features evolve independently from mobility/safety concerns.

## 10. Analytics Service (Batch + ML)
Responsibilities

Consume all major Kafka topics

Offline analytics & time-series computation

ETL: Kafka → S3 → Athena/Redshift

Generate hotspot maps, mobility reports

Support ML model training

Non-Responsibilities

❌ Real-time routing
❌ Real-time traffic
❌ Notification delivery

Data Ownership

Derived datasets in S3

Athena/Redshift tables

Why?

Long-running analytics cannot affect real-time pipelines.

## 11. Inter-Service Interaction Summary
flowchart LR
Auth -.-> API[API Gateway]
API --> User
API --> Location
API --> Routing
API --> Safety
API --> Traffic
API --> Notification
API --> Society

Location -->|device.location.v1| Kafka[(MSK)]
Traffic <-- Kafka
Safety <-- Kafka
Notification <-- Kafka

Safety -->|hazard.report.v1| Kafka
Routing -->|routing.request.v1| Kafka
User -->|user.activity.v1| Kafka

## 12. Final Responsibility Matrix (AWS Review Format)
Service	Responsible For	Not Responsible For
API Gateway	Auth validation, routing	Business logic, DB
Auth	Login, JWT, RBAC	Profiles, routing
User	Profile, devices	Auth, hazards
Location	GPS ingestion	Traffic, routing
Traffic	Congestion analytics	Routing
Routing	Route generation	Traffic calc
Safety	Hazards	Notifications
Notification	Delivering alerts	Creating hazards
Society	Community logic	Routing, traffic
Analytics	ETL, Reporting	Real-time ops

This matrix proves strong bounded contexts, required for AWS architectural approval.

## 13. Summary

This document clearly demonstrates:

Clean domain boundaries

No cross-service data coupling

Each service owns its schema

Event-driven communication paths

Scalability through independent autoscaling

AWS-ready decomposition suitable for EKS + MSK

This is the level of clarity AWS expects before issuing credits or evaluating workloads for production deployment.