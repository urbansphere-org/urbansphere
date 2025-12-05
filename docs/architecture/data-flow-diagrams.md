# UrbanSphere — Data Flow Diagrams (DFD) & Runtime Sequence Flows

This document provides end-to-end data movement visualization across UrbanSphere’s microservices, event pipelines, user interactions, and real-time ingestion paths.
It includes:

Level-0 Context Diagram

Level-1 Detailed Internal System Flow

Runtime Sequence Diagrams for

Authentication

GPS Ingestion

Routing

Hazard Detection

Notifications

Notes on traceability, idempotency, retries, and backpressure, required for AWS architectural approval.

## 1. DFD Level 0 — Context Diagram

UrbanSphere interacts with three major external actors:

Mobile/Web Users → APIs via HTTPS

IoT GPS Devices → high-frequency telemetry

External Providers → Routing APIs, Push Notification providers

DFD Level 0 Overview
flowchart TB
User[Mobile/Web User]
Device[GPS/IoT Device]
Providers[External Providers<br>(ORS/OSRM, FCM/APNS, SMS)]

subgraph UrbanSphere Cloud
ALB[ALB + WAF]
APIGW[API Gateway]
Services[Microservices]
Kafka[(Kafka / MSK)]
Storage[(RDS | Redis | S3)]
end

User -->|HTTPS| ALB
Device -->|HTTP/gRPC| ALB
ALB --> APIGW

APIGW --> Services
Services --> Kafka
Services --> Storage

Services --> Providers

Purpose for AWS

This diagram clarifies system boundaries and shows ingress points, security layers, and external dependencies, which AWS reviewers require to understand attack surface and data classification zones.

## 2. DFD Level 1 — Internal System Architecture

This diagram shows how requests propagate, where data is stored, and how event-driven components interact.

flowchart LR
subgraph Clients
Mobile[Mobile/Web Client]
GPS[IoT GPS Device]
end

Mobile -->|HTTPS JWT| ALB[ALB/WAF]
GPS -->|HTTP/gRPC| ALB

ALB --> APIGW[API Gateway]

APIGW --> Auth[auth-service]
APIGW --> User[user-service]
APIGW --> Location[location-service]
APIGW --> Routing[routing-service]
APIGW --> Safety[safety-service]
APIGW --> Traffic[traffic-service]
APIGW --> Notification[notification-service]
APIGW --> Society[society-service]

Location -->|device.location.v1| Kafka[(MSK Topics)]
Safety -->|hazard.report.v1| Kafka
Routing -->|routing.request.v1| Kafka

Kafka --> Traffic
Kafka --> Safety
Kafka --> Notification
Kafka --> Analytics[analytics-worker]

subgraph Storage
RDS[(Postgres - Multi AZ)]
Redis[(Redis Cache)]
S3[(Object Storage)]
end

Auth --> RDS
User --> RDS
Safety --> RDS
Traffic --> RDS

Routing --> Redis
AllServices --> S3

Purpose for AWS

AWS officers use DFD-1 to verify:

Microservice boundaries

No cross-database violation

Kafka topic locations & fan-out behavior

Data persistence strategy

Internal traffic patterns & private subnet isolation

## 3. Sequence Flows

Runtime behavior is extremely important for AWS architecture reviewers.
The following diagrams show real traffic, critical paths, and operational characteristics of UrbanSphere.

### 3.1 Sequence: User Login / JWT Authentication
sequenceDiagram
participant Client
participant ALB
participant APIGW
participant Auth
participant RDS
participant Redis

Client->>ALB: POST /auth/login<br>{email, password}
ALB->>APIGW: Forward over TLS
APIGW->>Auth: Login request
Auth->>RDS: Fetch user by email
RDS-->>Auth: user record
Auth->>Auth: Validate password (BCrypt)
Auth->>Redis: Store refresh_token_hash (TTL)
Auth-->>APIGW: access_token + refresh_token
APIGW-->>ALB: 200 OK
ALB-->>Client: return tokens

AWS Evaluation Notes

This flow demonstrates:

Zero trust internal model

Strong cryptography (RS256)

Secure storage (Redis/KMS)

JWT lifecycle with rotation

### 3.2 Sequence: GPS Location Ingestion → Kafka → Processing
sequenceDiagram
participant Device
participant ALB
participant APIGW
participant Location
participant Kafka
participant Traffic
participant Safety
participant Analytics

Device->>ALB: POST /location {lat, lon, speed, ts}
ALB->>APIGW: Forward
APIGW->>Location: ingest telemetry
Location->>Location: validate + enrich
Location->>Kafka: produce device.location.v1
Kafka-->>Traffic: consume
Kafka-->>Safety: consume
Kafka-->>Analytics: consume
Traffic->>RDS: update segment-level aggregates
Safety->>Safety: run detection rules

Why AWS cares

This proves the platform supports:

High-throughput ingestion

Partitioned ordering

Real-time fan-out processing

Scalable analytics consumption

### 3.3 Sequence: Routing Request Flow (Cache + External Compute)
sequenceDiagram
participant User
participant APIGW
participant Routing
participant Redis
participant ExternalORS
participant Kafka

User->>APIGW: GET /routing?origin=...&dest=...
APIGW->>Routing: forward request
Routing->>Redis: GET cached route
alt cache hit
Redis-->>Routing: return cached response
Routing-->>User: 200 OK (fast)
else cache miss
Routing->>ExternalORS: compute optimized route
ExternalORS-->>Routing: route result
Routing->>Redis: SET route cache TTL=60s
Routing->>Kafka: publish routing.request.v1 (analytics)
Routing-->>User: 200 OK
end

Why AWS cares

Shows efficient use of:

Redis caching

External service calls

Async analytics event emission

### 3.4 Sequence: Hazard Detection & Safety Pipeline
sequenceDiagram
participant Device
participant LocationSvc
participant Kafka
participant Safety
participant Notification
participant PushProvider

Device->>LocationSvc: telemetry
LocationSvc->>Kafka: device.location.v1
Kafka-->>Safety: consume location event
Safety->>Safety: detect anomaly (crash/pothole/sudden stop)
alt Hazard detected
Safety->>Kafka: publish hazard.report.v1
Kafka-->>Notification: deliver event
Notification->>PushProvider: send push/SMS/email
PushProvider-->>Notification: receipt
Notification->>RDS: store notification logs
end

## 4. Operational Behavior Requirements (AWS Mandatory Section)

AWS reviewers evaluate how your workloads behave under failures and load.

### 4.1 Retry Strategy

Kafka producers use exponential backoff + idempotence

Consumers retry N times → DLQ

External API calls use circuit breakers + retries

4.2 Idempotency

All Kafka events include event_id

DB writes use unique constraints or upserts

Notification sending uses idempotency keys

4.3 Backpressure Handling

Autoscale consumer pods based on Kafka lag

Gateway returns 429 Retry-After on overload

Processing pipelines degrade gracefully

4.4 Observability Hooks

Trace propagation across HTTP + Kafka

Metrics:

kafka.consumer.lag

http.server.requests

db.query.time

cache.hit.rate

Structured logs include trace_id

## 5. Summary

This document defines all data flows inside UrbanSphere, demonstrating:

Full alignment with microservice boundaries

Real-time, scalable ingestion

Event-driven communication

Reliability and idempotency

Observability and fault tolerance

AWS-ready deployment patterns

This is the level of clarity AWS Solution Architects expect when reviewing a distributed system for production-grade access, credits, or architectural approval.