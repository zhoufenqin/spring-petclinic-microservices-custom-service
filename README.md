# Spring PetClinic – Customers Service

A cloud-native Spring Boot microservice that manages **pet owners and their pets** as part of the [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) ecosystem. This repository is a custom fork focused on Azure cloud assessment and modernisation.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Data Model](#data-model)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run Locally](#run-locally)
  - [Run with Docker](#run-with-docker)
- [Configuration](#configuration)
- [Observability](#observability)
- [Testing](#testing)

---

## Overview

The **Customers Service** is responsible for:

- **Owner management** – create, retrieve, and update pet clinic customers (owners), including their address and contact information.
- **Pet management** – create and update pets associated with owners, including pet type (e.g. Cat, Dog, Hamster).
- **Pet type catalogue** – expose the list of available pet types.

The service exposes a REST API consumed by other microservices (e.g. an API gateway or visits service) in the broader PetClinic ecosystem.

---

## Architecture

```
┌──────────────────────────────────────────┐
│           Customers Service              │
│                                          │
│  ┌─────────────┐   ┌──────────────────┐  │
│  │ REST Layer  │   │  Config Client   │  │
│  │ (Spring MVC)│   │  (Config Server) │  │
│  └──────┬──────┘   └──────────────────┘  │
│         │                                │
│  ┌──────▼──────────────────────────────┐ │
│  │         Spring Data JPA             │ │
│  └──────┬──────────────────────────────┘ │
│         │                                │
│  ┌──────▼──────────────────────────────┐ │
│  │   MySQL / HSQLDB (embedded)         │ │
│  └─────────────────────────────────────┘ │
│                                          │
│  Eureka Client (Service Discovery)       │
│  Micrometer / Prometheus (Metrics)       │
└──────────────────────────────────────────┘
```

**Integration with the wider ecosystem:**

| Concern | Component |
|---|---|
| Service discovery | Registers with **Eureka** at startup; other services resolve it by name `customers-service` |
| Centralised config | Pulls settings from a **Spring Cloud Config Server** (`http://localhost:8888` by default) |
| Metrics | Publishes Prometheus-compatible metrics via **Spring Boot Actuator** |
| JMX | Exposes JMX data over HTTP via **Jolokia** |
| Resilience testing | **Chaos Monkey for Spring Boot** can inject latency/errors at runtime |

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/owners` | Create a new owner |
| `GET` | `/owners` | List all owners |
| `GET` | `/owners/{ownerId}` | Get a single owner by ID |
| `PUT` | `/owners/{ownerId}` | Update an existing owner |
| `GET` | `/petTypes` | List all available pet types |
| `POST` | `/owners/{ownerId}/pets` | Add a pet to an owner |
| `PUT` | `/owners/*/pets/{petId}` | Update a pet |
| `GET` | `/owners/*/pets/{petId}` | Get pet details |

---

## Data Model

```
types
  id          INT (PK, auto-increment)
  name        VARCHAR(80)

owners
  id          INT (PK, auto-increment)
  first_name  VARCHAR(30)
  last_name   VARCHAR(30)
  address     VARCHAR(255)
  city        VARCHAR(80)
  telephone   VARCHAR(20)

pets
  id          INT (PK, auto-increment)
  name        VARCHAR(30)
  birth_date  DATE
  type_id     INT (FK → types.id)
  owner_id    INT (FK → owners.id)
```

---

## Technologies

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Cloud | Spring Cloud 2024.0.0 (Config, Eureka) |
| Azure integration | Spring Cloud Azure 5.20.1 (`spring-cloud-azure-starter-jdbc-mysql`) |
| Persistence | Spring Data JPA / Hibernate |
| Databases | MySQL 8+ (production), HSQLDB (embedded, for local dev/tests) |
| Metrics | Micrometer + Prometheus |
| JMX | Jolokia 1.7.1 |
| Resilience | Chaos Monkey for Spring Boot 3.1.0 |
| Build | Apache Maven |
| Utilities | Lombok, Jackson |
| Testing | JUnit 5, Mockito, AssertJ |

---

## Project Structure

```
src/
└── main/
│   ├── java/org/springframework/samples/petclinic/customers/
│   │   ├── CustomersServiceApplication.java   # Spring Boot entry point
│   │   ├── config/
│   │   │   └── MetricConfig.java              # Micrometer metric tag configuration
│   │   ├── model/
│   │   │   ├── Owner.java                     # Owner JPA entity
│   │   │   ├── OwnerRepository.java           # Owner Spring Data repository
│   │   │   ├── Pet.java                       # Pet JPA entity
│   │   │   ├── PetRepository.java             # Pet Spring Data repository (incl. pet types)
│   │   │   └── PetType.java                   # PetType JPA entity
│   │   └── web/
│   │       ├── OwnerResource.java             # Owner REST controller
│   │       ├── PetResource.java               # Pet REST controller
│   │       ├── PetRequest.java                # Request DTO for pet create/update
│   │       ├── PetDetails.java                # Response DTO for pet details
│   │       └── ResourceNotFoundException.java # 404 exception
│   └── resources/
│       ├── application.yml                    # App configuration
│       ├── logback-spring.xml                 # Logging configuration
│       └── db/
│           ├── mysql/
│           │   ├── schema.sql                 # MySQL DDL
│           │   └── data.sql                   # MySQL seed data
│           └── hsqldb/
│               ├── schema.sql                 # HSQLDB DDL
│               └── data.sql                   # HSQLDB seed data
└── test/
    └── java/.../customers/web/
        └── PetResourceTest.java               # Unit tests for PetResource
```

---

## Getting Started

### Prerequisites

- **JDK 17+**
- **Maven 3.8+**
- (Optional) **MySQL 8.0+** – the service defaults to embedded HSQLDB when no MySQL configuration is provided
- (Optional) **Spring Cloud Config Server** running on port `8888` – the service starts fine without it (`optional:configserver:…`)

### Run Locally

```bash
# Clone the repository
git clone https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service.git
cd spring-petclinic-microservices-custom-service

# Build
mvn clean package -DskipTests

# Run (uses embedded HSQLDB and no Config Server required)
mvn spring-boot:run
```

The service will start on **port 8081**. You can verify it is running:

```bash
curl http://localhost:8081/petTypes
curl http://localhost:8081/owners
```

### Run with Docker

```bash
# Build JAR and Docker image
mvn clean package -PbuildDocker

# Run container
docker run -p 8081:8081 customers-service:3.4.1
```

To connect to an external MySQL database and Config Server, pass environment variables:

```bash
docker run -p 8081:8081 \
  -e CONFIG_SERVER_URL=http://config-server:8888 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/petclinic \
  -e SPRING_DATASOURCE_USERNAME=petclinic \
  -e SPRING_DATASOURCE_PASSWORD=petclinic \
  customers-service:3.4.1
```

---

## Configuration

The service is configured via `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: customers-service
  config:
    # Config Server URL – override with CONFIG_SERVER_URL env var
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
```

When the `docker` Spring profile is active, the Config Server URL defaults to `http://config-server:8888`.

Additional runtime settings (datasource, JPA, Eureka, etc.) are typically supplied by the Config Server or via environment variable overrides following standard Spring Boot [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config) rules.

---

## Observability

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Service health check |
| `GET /actuator/info` | Build info and application metadata |
| `GET /actuator/prometheus` | Prometheus metrics scrape endpoint |
| `GET /actuator/jolokia` | JMX beans exposed as JSON (via Jolokia) |

All custom metrics are tagged with `application=petclinic` and named under the `petclinic.owner.*` and `petclinic.pet.*` namespaces using Micrometer's `@Timed` annotation.

---

## Testing

Run the test suite with:

```bash
mvn test
```

Tests use an embedded HSQLDB database (no external dependencies required). The `test` Spring profile is automatically activated for the test source set.
