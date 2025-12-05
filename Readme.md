# ⭐ **UrbanSphere**

*Modern, Smart Mobility & Safety Platform — Event-Driven, Cloud-Ready, Scalable*

---

````markdown
# 🚀 UrbanSphere  
### A Smart, Event-Driven Mobility & Safety Platform for Modern Cities

UrbanSphere is a next-generation mobility and public-safety backend platform built using a scalable microservices architecture.  
Designed for **millions of users**, UrbanSphere powers:

- Real-time location updates  
- Smart routing & navigation  
- Traffic analysis  
- Hazard detection & alerts  
- Community safety features  
- Emergency broadcasts  
- Scalable event-driven analytics  

UrbanSphere follows **clean architecture**, **event streaming**, and **cloud-native principles**.

---

## 🧠 Vision

> **To build a unified, intelligent mobility platform that makes transportation safer, faster, and more efficient for cities and citizens.**

UrbanSphere is fully designed on modern distributed-systems principles:
- Microservices  
- Kafka event streaming  
- Postgres + Redis  
- Containerized infrastructure  
- Cloud-native deployment  

---

# 🏗️ Architecture Overview

```mermaid
flowchart LR
    UserApp --> API_Gateway

    API_Gateway --> Auth_Service
    API_Gateway --> User_Service
    API_Gateway --> Location_Service
    API_Gateway --> Routing_Service

    Location_Service -->|device.location.v1| Kafka
    Safety_Service -->|hazard.report.v1| Kafka
    Society_Service -->|community.event.v1| Kafka

    Kafka --> Traffic_Service
    Kafka --> Safety_Service
    Kafka --> Notification_Service
    Kafka --> Analytics_Service

    Traffic_Service --> Postgres
    Auth_Service --> Postgres
    User_Service --> Postgres

    Traffic_Service --> Redis
    Routing_Service --> Redis

    Kafka --> Kafka_UI
````

---

# 🧬 Core Features

### 🔐 **Authentication & User Identity**

* JWT (RS256) authentication
* Refresh tokens
* Role-based access (RBAC)
* Consent and privacy management

### 📍 **Real-Time Location Processing**

* Device → Location Service → Kafka
* Geo-event generation
* Speed & GPS accuracy tracking

### 🚦 **Traffic Intelligence**

* Congestion detection
* Real-time speed averaging
* Heatmap computation
* Traffic pattern analytics

### 🛣️ **Routing Engine**

* Smart city routing
* Hazard-aware navigation
* Cached responses for speed

### ⚠️ **Safety & Hazard Detection**

* Accident detection
* Road hazard ingestion
* Emergency alerts

### 🔔 **Notifications**

* Push notifications
* Community alerts
* Emergency broadcast workflows

---

# 🛠️ Tech Stack

### **Backend**

* Java 21 / Spring Boot 3
* WebFlux (reactive)
* JPA / Hibernate
* OpenAPI documentation
* Micrometer + Prometheus

### **Event Architecture**

* Apache Kafka (Redpanda for local dev)
* Avro (Schema Registry planned)
* Event-driven microservices

### **Database**

* PostgreSQL
* Redis cache

### **DevOps**

* Docker & Docker Compose
* GitHub Actions (CI/CD)
* Multi-module monorepo

---

# 📁 Project Structure (Monorepo)

```
UrbanSphere/
├── common/               # Shared libraries (kafka-lib, security-lib, util-lib)
├── docs/                 # Documentation & architecture
├── infra/
│   └── docker-compose.yml # Local infra: Kafka, Redis, Postgres, Kafka UI
├── scripts/              # Dev & deployment scripts
└── services/
    ├── api-gateway/
    ├── auth-service/
    ├── user-service/
    ├── location-service/
    ├── routing-service/
    ├── safety-service/
    ├── traffic-service/
    ├── society-service/
    └── notification-service/
```

---

# 🚀 Getting Started (Local Development)

### **1. Clone Repo**

```bash
git clone https://github.com/urbansphere-org/urbansphere.git
cd urbansphere
```

### **2. Start Local Infrastructure**

```bash
cd infra
docker compose up -d
```

This boots:

* Redpanda (Kafka)
* Postgres (port 5434)
* Redis
* Kafka UI (localhost:8080)

### **3. Start Any Microservice**

Example (auth-service):

```
cd services/auth-service
./gradlew bootRun
```

---

# 📦 Infra Endpoints

| Service      | URL / Port                                     |
| ------------ | ---------------------------------------------- |
| Kafka UI     | [http://localhost:8080](http://localhost:8080) |
| Postgres     | localhost:5434                                 |
| Redis        | localhost:6379                                 |
| Kafka Broker | localhost:9092                                 |

---

# 📡 Event Topics (Kafka)

| Topic Name           | Description                       |
| -------------------- | --------------------------------- |
| `device.location.v1` | GPS updates from users/devices    |
| `hazard.report.v1`   | Hazard alerts (accidents, issues) |
| `traffic.update.v1`  | Traffic congestion stream         |
| `community.event.v1` | Society/community safety alerts   |

---

# 🤝 Contributing

We welcome contributions!
To propose changes:

1. Fork the repo
2. Create a feature branch
3. Submit a PR with clear description

Coding conventions are inside:

```
docs/conventions.md
```

---

# 🗺️ Roadmap

### Phase 1 — Core Platform (WIP)

* [x] Monorepo setup
* [x] Local infrastructure setup
* [ ] Auth Service (in-progress)
* [ ] API Gateway
* [ ] User Service
* [ ] Location → Kafka pipeline

### Phase 2 — Mobility Intelligence

* [ ] Traffic Service
* [ ] Routing engine integration
* [ ] Safety Service
* [ ] Hazard detection models

### Phase 3 — Community & Emergency Systems

* [ ] Society Service
* [ ] Notification pipelines
* [ ] Citizen alerting

### Phase 4 — Enterprise scaling

* [ ] Kubernetes deployment
* [ ] Observability stack
* [ ] Distributed tracing (OpenTelemetry)
* [ ] Multi-region Kafka

---

# 🏁 License

To be added.

---

# 💬 Contact

**Ashutosh Modanwal**
Creator & Maintainer
mailto: [ashu.modanwal15@gmail.com](mailto:ashu.modanwal15@gmail.com)

