UrbanSphere — High-Level Architecture Overview

UrbanSphere is a cloud-native, event-driven Smart Mobility & City Safety platform designed to handle real-time telemetry ingestion, routing, traffic modeling, hazard detection, and user/community interactions at scale.
The platform is built using Java 21, Spring Boot 3, Kafka (MSK), Redis, Postgres, S3, Kubernetes (EKS) and follows strict microservice/domain boundaries.

## 1. Architecture Goals
Functional Goals

User authentication & RBAC

Real-time GPS ingestion

Routing & traffic computation

Hazard detection & safety workflow

Notifications (push/SMS/email)

Community (“society”) features

Analytics & historical insights

Non-Functional Goals

High availability: 99.9% for critical pathways

Low latency: p95 < 300ms for synchronous APIs

Scalable: tens/hundreds of thousands of GPS events/sec

Durable: multi-AZ Postgres + Kafka + S3

Secure: KMS encryption, IAM isolation, mTLS inside cluster

Observable: metrics, logs, traces with OpenTelemetry

## 2. System Architecture (High-Level)
flowchart LR
subgraph Clients
C[Mobile/Web Clients]
IoT[IoT Devices / GPS Trackers]
end

C -->|HTTPS / WebSocket| ALB[Amazon ALB + WAF]
IoT -->|HTTP / MQTT / gRPC| ALB

ALB --> APIGW[API Gateway (EKS)]

subgraph EKS[Microservices on EKS]
Auth[auth-service]
User[user-service]
Location[location-service]
Routing[routing-service]
Traffic[traffic-service]
Safety[safety-service]
Notification[notification-service]
Society[society-service]
Analytics[analytics-worker]
end

APIGW --> Auth
APIGW --> User
APIGW --> Location
APIGW --> Routing
APIGW --> Safety
APIGW --> Traffic
APIGW --> Notification
APIGW --> Society

subgraph MSK[Kafka / Amazon MSK]
DL[(device.location.v1)]
HR[(hazard.report.v1)]
RU[(routing.request.v1)]
UA[(user.activity.v1)]
end

Location --> DL
Safety --> HR
Routing --> RU
User --> UA

DL --> Traffic
DL --> Safety
DL --> Analytics
HR --> Notification
HR --> Analytics

subgraph Storage[Persistent Storage Layer]
RDS[(Postgres - Multi AZ)]
Redis[(Redis Cache)]
S3[(Object Storage)]
end

Auth --> RDS
User --> RDS
Traffic --> RDS
Safety --> RDS
Routing --> Redis
AllServices --> S3

## 3. Architectural Layers

UrbanSphere is divided into distinct layers for scalability, security, and clarity.

### 3.1 Edge Layer

Components:

AWS ALB

AWS WAF

ACM Certificates (TLS termination)

Responsibilities:

First entry point

HTTPS enforcement

Layer-7 routing

Bot/DDoS mitigation

Optional IP filtering

### 3.2 API Gateway Layer

A lightweight gateway deployed inside EKS.

Responsibilities:

Validate JWT (public keys from Auth)

Enforce rate limits

Request routing

API versioning

Fail-fast timeouts

Non-responsibilities:

❌ No business logic

❌ No storage

❌ No external API calls

### 3.3 Microservices Layer

Each service:

Is a Spring Boot 3 app

Runs in its own pod

Owns its database schema

Emits/consumes Kafka events

Uses OpenTelemetry instrumentation

Services list (Domain-driven)
Service	Responsibilities
auth-service	Login, register, JWT, refresh tokens, RBAC
user-service	Profile, privacy settings, device metadata
location-service	GPS ingestion, normalization, publish telemetry
traffic-service	Real-time congestion & segment speed modeling
routing-service	Route calculation + caching
safety-service	Hazard reporting & classification
notification-service	Push/SMS/email delivery
society-service	Community management
analytics-worker	Offline analytics & ETL
## 4. Event-Driven Backbone (Kafka / MSK)

Kafka provides:

High-throughput processing

Ordering per device

Fan-out to many consumers

Replay for analytics & ML

Loose coupling between services

Kafka Topics Used
Topic	Purpose	Key
device.location.v1	Real-time GPS stream	deviceId
hazard.report.v1	Hazard events	hazardId
routing.request.v1	Route request logs	requestId
user.activity.v1	Audit trail	userId
flowchart LR
LocationSvc --> DL[device.location.v1]
DL --> Traffic
DL --> Safety
DL --> Analytics

SafetySvc --> HR[hazard.report.v1]
HR --> Notification
HR --> Analytics

RoutingSvc --> RR[routing.request.v1]
UserSvc --> UA[user.activity.v1]

## 5. Storage Architecture
🔵 Postgres (RDS Multi-AZ)

Used for durable, relational workloads:

Users

Hazards

Traffic aggregates

Society data

Refresh tokens (optional — Redis recommended)

🔴 Redis Cache

Used for:

Route caching

Throttling counters

Recent location snapshots

Session-like transient values

🟡 S3

Stores:

Telemetry dumps

Analytics snapshots

ML datasets

File attachments

## 6. Security Architecture (High-Level)
flowchart TB
User --> ALB[ALB + TLS + WAF]

subgraph AuthLayer[Authentication & IAM]
JWT[RS256 JWT Validation]
RBAC[Role-Based Access Control]
KMS[KMS - Private Key Storage]
end

ALB --> JWT
JWT --> Services[Microservices]

Services --> RDS
Services --> Kafka
Services --> Redis

Services --> IRSA[IAM Roles for Service Accounts]

Security Principles

TLS everywhere (internal + external)

RS256 JWT with rotating keys

IRSA to restrict AWS permissions

Kafka SASL/IAM & encryption

RDS/MSK encrypted at rest

WAF for common OWASP threats

## 7. Observability Architecture
Tracing

OpenTelemetry SDK

trace_id propagated via HTTP + Kafka headers

Exported to AWS X-Ray / Jaeger

Metrics

Collected via Micrometer → Prometheus → Grafana

HTTP latency

Kafka lag

DB query time

Cache hit rate

Error spikes

Logging

JSON structured logs

Include trace_id for correlation

Centralized in CloudWatch / ELK

## 8. Deployment & Infrastructure Overview
Infrastructure Components

Amazon EKS (cluster for microservices)

Amazon MSK (Kafka)

Amazon RDS (Postgres)

ElastiCache Redis

S3 buckets

CloudWatch + Prometheus + Grafana

GitHub Actions → ECR → ArgoCD

CI/CD Flow (High-Level)
flowchart LR
Dev[Developer Commit] --> GH[GitHub Actions]
GH --> Build[Build + Test]
Build --> ECR[ECR (Docker Images)]
ECR --> Argo[ArgoCD GitOps]
Argo --> EKS[EKS Deployment]

## 9. Summary

UrbanSphere is a distributed, real-time, event-driven system that combines:

Microservices for separation of responsibilities

Kafka for high-throughput, scalable streaming

EKS for container orchestration

Postgres + Redis + S3 for reliable storage

WAF + ALB + KMS + IAM for strong security

OpenTelemetry + Prometheus + Grafana for observability

This architecture enables scalability, reliability, and maintainability while supporting the complex needs of smart mobility & city safety.