# Spring PetClinic – Customers Service

This repository contains the **Customers Service** microservice, one of the backend services that make up the [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) sample application. It is responsible for managing **pet owners** and their **pets**.

---

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Domain Model](#domain-model)
- [REST API](#rest-api)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Database](#database)
- [Building and Running](#building-and-running)
- [Testing](#testing)
- [Observability](#observability)

---

## Overview

The Customers Service is a standalone Spring Boot microservice that:

- Stores and retrieves **owners** (pet clinic customers) and their **pets**.
- Exposes a RESTful HTTP API consumed by other services (e.g., the API gateway and the visits service).
- Registers itself with a **Eureka** service-discovery server so other services can locate it dynamically.
- Fetches its runtime configuration from a central **Spring Cloud Config Server**.
- Supports both an in-memory **HSQLDB** database (for local development) and **MySQL** (for production / Docker deployments), with optional Azure managed-identity authentication via the Azure JDBC starter.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Service discovery | Spring Cloud Netflix Eureka Client |
| Centralised config | Spring Cloud Config Client |
| Persistence | Spring Data JPA (Hibernate) |
| Production database | MySQL (with Azure JDBC Managed Identity support) |
| Embedded database | HSQLDB (test / local) |
| Metrics | Micrometer + Prometheus |
| Chaos engineering | Chaos Monkey for Spring Boot |
| Boilerplate reduction | Lombok |
| Build tool | Maven |
| Packaging | Executable JAR / Docker image |

---

## Domain Model

```
┌──────────────────────────────────┐          ┌──────────────────────────────────┐
│             Owner                │  1 ───► * │              Pet                 │
│──────────────────────────────────│          │──────────────────────────────────│
│ id          : Integer (PK)       │          │ id        : Integer (PK)          │
│ firstName   : String (not blank) │          │ name      : String                │
│ lastName    : String (not blank) │          │ birthDate : Date                  │
│ address     : String (not blank) │          │ type      : PetType (FK)          │
│ city        : String (not blank) │          │ owner     : Owner (FK, hidden)    │
│ telephone   : String (≤ 12 digits│          └──────────────────────────────────┘
└──────────────────────────────────┘

┌──────────────────────────────────┐
│            PetType               │
│──────────────────────────────────│
│ id   : Integer (PK)              │
│ name : String                    │
└──────────────────────────────────┘
```

- An **Owner** can have zero or more **Pets** (one-to-many, cascade all, fetched eagerly).
- A **Pet** belongs to one **Owner** and has one **PetType** (e.g., cat, dog, bird).

---

## REST API

The service listens on port **8081** by default (configurable via Config Server).

### Owner endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/owners` | Create a new owner |
| `GET` | `/owners` | List all owners |
| `GET` | `/owners/{ownerId}` | Get a single owner by ID |
| `PUT` | `/owners/{ownerId}` | Update an existing owner |

### Pet endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/petTypes` | List all pet types (cat, dog, …) |
| `POST` | `/owners/{ownerId}/pets` | Add a new pet to an owner |
| `PUT` | `/owners/*/pets/{petId}` | Update an existing pet |
| `GET` | `/owners/*/pets/{petId}` | Get pet details by pet ID |

All endpoints return / accept **JSON**. Validation is applied to request bodies using Jakarta Bean Validation (`@NotBlank`, `@Digits`, etc.). A `404` response with a descriptive message is returned when a requested resource is not found.

---

## Project Structure

```
src/
├── main/
│   ├── java/org/springframework/samples/petclinic/customers/
│   │   ├── CustomersServiceApplication.java   # Spring Boot entry point
│   │   ├── config/
│   │   │   └── MetricConfig.java              # Micrometer custom metrics setup
│   │   ├── model/
│   │   │   ├── Owner.java                     # JPA entity: pet owner
│   │   │   ├── OwnerRepository.java           # Spring Data repository for Owner
│   │   │   ├── Pet.java                       # JPA entity: pet
│   │   │   ├── PetRepository.java             # Spring Data repository for Pet & PetType
│   │   │   └── PetType.java                   # JPA entity: pet type (cat, dog, …)
│   │   └── web/
│   │       ├── OwnerResource.java             # REST controller: /owners
│   │       ├── PetResource.java               # REST controller: /owners/*/pets, /petTypes
│   │       ├── PetDetails.java                # DTO: pet detail response
│   │       ├── PetRequest.java                # DTO: create/update pet request
│   │       └── ResourceNotFoundException.java # Exception → HTTP 404
│   └── resources/
│       ├── application.yml                    # Service name & Config Server URL
│       ├── logback-spring.xml                 # Logging configuration
│       ├── appcat-analysis.yml                # AppCAT assessment metadata
│       └── db/
│           ├── hsqldb/
│           │   ├── schema.sql                 # DDL for embedded HSQLDB
│           │   └── data.sql                   # Seed data for HSQLDB
│           └── mysql/
│               ├── schema.sql                 # DDL for MySQL
│               └── data.sql                   # Seed data for MySQL
└── test/
    ├── java/org/springframework/samples/petclinic/customers/web/
    │   └── PetResourceTest.java               # Unit tests for PetResource
    └── resources/
        └── application-test.yml               # Test-specific configuration
```

---

## Configuration

The service pulls the bulk of its configuration (datasource URL, JPA settings, Eureka server address, etc.) from a **Spring Cloud Config Server**.

| Property | Default | Purpose |
|---|---|---|
| `spring.application.name` | `customers-service` | Service identifier used by Eureka and Config Server |
| `CONFIG_SERVER_URL` | `http://localhost:8888/` | URL of the Config Server (overridable via env var) |

When running with the `docker` Spring profile the Config Server URL switches to `http://config-server:8888` (hostname resolution inside a Docker network).

---

## Database

Two database backends are supported, selected via the Spring profile:

| Profile | Database | Script location |
|---|---|---|
| *(default / local)* | HSQLDB (in-memory) | `db/hsqldb/` |
| `mysql` / Docker | MySQL | `db/mysql/` |

### Schema (MySQL example)

```sql
CREATE TABLE types  (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(80));
CREATE TABLE owners (id INT PRIMARY KEY AUTO_INCREMENT,
                     first_name VARCHAR(30), last_name VARCHAR(30),
                     address VARCHAR(255), city VARCHAR(80), telephone VARCHAR(20));
CREATE TABLE pets   (id INT PRIMARY KEY AUTO_INCREMENT,
                     name VARCHAR(30), birth_date DATE,
                     type_id INT REFERENCES types(id),
                     owner_id INT REFERENCES owners(id));
```

The Azure JDBC Starter (`spring-cloud-azure-starter-jdbc-mysql`) allows the service to authenticate to **Azure Database for MySQL** using managed identity — no passwords in configuration.

---

## Building and Running

### Prerequisites

- Java 17+
- Maven 3.6+
- (Optional) Docker & Docker Compose for a full multi-service run

### Build

```bash
./mvnw clean package
```

### Run locally (HSQLDB, standalone)

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8081`. Because the Config Server import is declared as `optional:` in `application.yml`, the service will boot in standalone mode using embedded HSQLDB even when the Config Server and Eureka discovery server are not running.

### Run with Docker Compose

See the parent [spring-petclinic-microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) repository for the full `docker-compose.yml` that wires this service together with the Config Server, Eureka discovery server, API gateway, visits service, and vets service.

---

## Testing

Unit tests are located in `src/test/` and use JUnit 5, AssertJ, and the Spring Boot Test framework.

```bash
./mvnw test
```

Key test file: **`PetResourceTest`** — validates the `PetResource` REST controller behaviour (create pet, update pet, get pet details, get pet types).

---

## Observability

| Feature | Details |
|---|---|
| Health / Info | Spring Boot Actuator (`/actuator/health`, `/actuator/info`) |
| Prometheus metrics | Exposed at `/actuator/prometheus` via `micrometer-registry-prometheus` |
| Custom metrics | `@Timed("petclinic.owner")` and `@Timed("petclinic.pet")` on REST controllers |
| JMX | Jolokia (`/actuator/jolokia`) for JMX-over-HTTP |
| Chaos testing | Chaos Monkey for Spring Boot (activate via `chaos.monkey.enabled=true`) |
| Logging | Logback with a Spring profile–aware configuration (`logback-spring.xml`) |
