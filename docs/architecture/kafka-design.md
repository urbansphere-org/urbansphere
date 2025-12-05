# UrbanSphere — Kafka Architecture & Event Streaming Design

Kafka (Amazon MSK) is the real-time backbone of UrbanSphere.
All telemetry, hazard reports, analytics signals, and asynchronous workflows depend on Kafka’s durability, ordering guarantees, and fan-out capabilities.

This document describes:

Kafka architecture & rationale

Topic definitions

Partition strategies

Producer/consumer design

Schema evolution

DLQ & recovery flows

AWS MSK production configuration

Operational considerations

## 1. Why Kafka for UrbanSphere

UrbanSphere ingests millions of GPS location events per minute and requires:

✔ Real-time hazard detection
✔ Parallel processing
✔ Ordered event streams per device
✔ Durable, replayable logs
✔ High availability & multi-AZ architecture
✔ Decoupling between ingestion & downstream computation

Kafka satisfies all of these through:

Append-only commit log

Partition-based parallelism

Consumer groups

Exactly-once processing (idempotence + transactions)

Long-term retention

AWS Equivalent:
Production uses Amazon MSK (Managed Kafka) with multi-AZ replication and IAM-based authentication.

## 2. Kafka Architecture (High-Level)
flowchart LR

subgraph Producers
Location[location-service]
Safety[safety-service]
Routing[routing-service]
UserAct[user-service]
end

subgraph MSK[Amazon MSK Cluster]
DL[(device.location.v1)]
HR[(hazard.report.v1)]
RR[(routing.request.v1)]
UA[(user.activity.v1)]
DLQ[(dead-letter-queues)]
end

subgraph Consumers
TrafficC[traffic-service]
SafetyC[safety-service<br>(anomaly detection)]
NotificationC[notification-service]
AnalyticsC[analytics-worker]
end

Location --> DL
Safety --> HR
Routing --> RR
UserAct --> UA

DL --> TrafficC
DL --> SafetyC
DL --> AnalyticsC
HR --> NotificationC
HR --> AnalyticsC
RR --> AnalyticsC
UA --> AnalyticsC

DLQ --> AnalyticsC

## 3. Topic Definitions (Production-Ready)

This section defines each topic, retention policies, and scaling strategy.

### 3.1 device.location.v1
Field	Value
Purpose	Continuous GPS ingestion (very high volume)
Partition Key	deviceId
Partitions	48–300 (autoscale as user base grows)
Retention	24–72 hours
Rate	10k–500k events/sec

Why deviceId as key?

Strict ordering for each device

Partition workload evenly distributed

Consumer scalability becomes linear

### 3.2 hazard.report.v1
Field	Value
Purpose	Hazard events (accidents, potholes, sudden deceleration)
Partition Key	hazardId
Partitions	6–12
Retention	7 days

This topic is lower volume but critical.

### 3.3 routing.request.v1
Field	Value
Purpose	Log routing requests for analytics and ML
Partition Key	requestId
Partitions	6–12
Retention	7 days

Used for:

Route popularity analysis

Heatmap generation

ML model training

### 3.4 user.activity.v1
Field	Value
Purpose	Audit trail of user actions
Partition Key	userId
Partitions	3–6
Retention	30 days

Low priority but good for insights.

## 4. Partition Strategy (Critical for AWS Review)

Partitioning directly impacts:

Throughput

Consumer scalability

Ordering guarantees

📌 device.location.v1

Key = deviceId → ensures strict ordering
Scaling model:

User Count	Events/sec	Recommended Partitions
10k	~5k/s	12–18
100k	~50k/s	48–72
1M	~500k/s	150–300
📌 hazard.report.v1

Low volume → 6–12 partitions enough.

📌 routing.request.v1

Moderate → 6–12.

📌 user.activity.v1

Low → 3–6.

## 5. Avro Schemas & Schema Registry

UrbanSphere uses Avro + Schema Registry (AWS Glue or Confluent) for all Kafka topics.

Schema evolution follows backward-compatible rules:

Allowed:

Add new optional fields

Add fields with defaults

Add enums

Not Allowed:

Remove fields

Change field type

Rename without alias

Migration Strategy for Breaking Changes:

Introduce device.location.v2

Migrate consumers

Retire old topic

## 6. Producer Design
Key configurations (Java/Spring Boot):
acks=all
enable.idempotence=true
compression.type=snappy
linger.ms=5
batch.size=65536
max.in.flight.requests.per.connection=1

Guarantees:

Exactly-once delivery for events

Minimal network overhead

Controlled batching for throughput

Producer Error Handling:

Retries with exponential backoff

Dead-letter queue handoff if serialization fails

Circuit breaker for downstream backpressure

## 7. Consumer Design

Consumers follow at-least-once processing with idempotent writes.

Consumer config:
max.poll.records=500
enable.auto.commit=false
max.poll.interval.ms=300000

Processing workflow:

Consume batch

Validate & deserialize

Perform idempotent writes (via event_id uniqueness)

Commit offsets only after successful processing

Autoscaling:

Scale based on Kafka lag metrics

HPA or KEDA based on Prometheus metrics

## 8. Dead Letter Queues (DLQ)

Each topic has a DLQ:

Topic Format:
<topic-name>.dlq

DLQ Event Schema:
{
"failed_event": { ... },
"error_reason": "string",
"stacktrace": "string",
"original_topic": "device.location.v1",
"partition": 12,
"offset": 39302,
"timestamp": 1736079903
}

DLQ Workflow:

Consumer retries N times

Moves event to DLQ

Analytics worker periodically processes DLQs

AWS Operators review via dashboards

## 9. AWS MSK Configuration (Production Checklist)
Cluster

Multi-AZ

3–6 brokers minimum

Storage: 500GB–2TB per broker

Encryption at rest enabled

TLS between clients & brokers

Authentication

IAM-based MSK auth (SASL/IAM)

No plaintext auth

Networking

VPC private subnets

Security groups restrict to EKS → MSK only

Monitoring

Enable:

Broker metrics (CloudWatch)

Enhanced Monitoring

Client-level metrics via OpenTelemetry

Autoscaling Behaviors

Add partitions as ingestion increases

Add brokers for increased throughput

## 10. Operational Monitoring & Alerts
Critical Metrics
Metric	Purpose
Consumer Lag	Detect backpressure
BytesInPerSec	Ingestion throughput
BytesOutPerSec	Consumer throughput
UnderReplicatedPartitions	Broker health
ActiveControllerCount	Must always be 1
RequestLatency	Broker performance
Alerts

Lag > threshold for 5+ minutes

URP > 0

Broker CPU > 80%

Error rate spikes

## 11. End-to-End Streaming Summary (Visual)
flowchart LR
GPS-->LocationSvc
LocationSvc-->device.location.v1
device.location.v1-->Traffic
device.location.v1-->Safety
device.location.v1-->Analytics
Safety-->hazard.report.v1
hazard.report.v1-->Notification
hazard.report.v1-->Analytics
Routing-->routing.request.v1
routing.request.v1-->Analytics


Kafka enables:

Real-time decisions

Fan-out processing

Low latency workflows

Replay for analytics

Independent scaling of services

## 12. Final Summary

UrbanSphere’s Kafka architecture is:

Scalable → handles millions of events

Fault tolerant → multi-AZ MSK

Consistent & ordered → partition strategies

Secure → IAM auth + TLS

Observable → metrics, logs, traces

AWS-aligned → matches SA best practices

This document is suitable for:

AWS architecture review

Funding programs requiring tech justification

Investors / auditors

Internal engineering onboarding