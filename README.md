# Spring PetClinic – Customers Service

This repository contains the **Customers Service** microservice for the [Spring PetClinic](https://github.com/spring-petclinic/spring-petclinic-microservices) sample application. It is a standalone Spring Boot service responsible for managing **pet owners** and their **pets** within a distributed microservices architecture.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [REST API](#rest-api)
- [Configuration](#configuration)
- [Database](#database)
- [Building](#building)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Monitoring](#monitoring)

---

## Overview

The **Customers Service** exposes a RESTful JSON API that other services in the PetClinic ecosystem can call to:

- Create, retrieve, and update **pet owners** (name, address, city, telephone).
- Create, retrieve, and update **pets** associated with an owner (name, birth date, type).
- Look up available **pet types** (cat, dog, lizard, snake, bird, hamster).

The service is cloud-native and is designed to run alongside a Spring Cloud Config Server and a Eureka service-discovery server, making it suitable for containerized deployments on Kubernetes, Azure Kubernetes Service (AKS), or Azure Container Apps.

---

## Architecture

```
                     ┌─────────────────────────────┐
                     │  Spring Cloud Config Server  │
                     │  (centralised configuration) │
                     └──────────────┬──────────────┘
                                    │
              ┌─────────────────────▼─────────────────────┐
              │          Eureka Service Registry           │
              │          (service discovery)               │
              └─────────────────────┬─────────────────────┘
                                    │
              ┌─────────────────────▼─────────────────────┐
              │           Customers Service                │
              │   Spring Boot 3.4.1 · Java 17 · Port 8081 │
              │                                           │
              │  OwnerResource  ──►  OwnerRepository      │
              │  PetResource    ──►  PetRepository        │
              │                         │                 │
              └─────────────────────────┼─────────────────┘
                                        │
                              ┌─────────▼────────┐
                              │  MySQL / HSQLDB  │
                              │  (persistence)   │
                              └──────────────────┘
```

The service registers itself with **Eureka** so that other microservices (e.g. the API gateway or the visits service) can discover it dynamically without hard-coded URLs.

---

## Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Runtime |
| Spring Boot | 3.4.1 | Application framework |
| Spring Cloud | 2024.0.0 | Service discovery & config |
| Spring Cloud Netflix Eureka | (managed) | Service registration |
| Spring Cloud Config | (managed) | Centralised configuration |
| Spring Data JPA | (managed) | ORM / database access |
| Lombok | (managed) | Boilerplate code generation |
| Micrometer Prometheus | (managed) | Metrics collection |
| MySQL Connector/J | (managed) | Production database driver |
| HSQLDB | (managed) | Embedded database (dev/test) |
| Jolokia | 1.7.1 | JMX-over-HTTP bridge |
| Chaos Monkey for Spring Boot | 3.1.0 | Resilience testing |
| JUnit 5 / AssertJ | (managed) | Unit & integration testing |
| Maven | 3.6+ | Build tool |

---

## Project Structure

```
.
├── pom.xml
└── src/
    ├── main/
    │   ├── java/org/springframework/samples/petclinic/customers/
    │   │   ├── CustomersServiceApplication.java   # @SpringBootApplication entry point
    │   │   ├── config/
    │   │   │   └── MetricConfig.java              # Micrometer / Prometheus setup
    │   │   ├── model/
    │   │   │   ├── Owner.java                     # JPA entity
    │   │   │   ├── Pet.java                       # JPA entity
    │   │   │   ├── PetType.java                   # JPA entity
    │   │   │   ├── OwnerRepository.java           # Spring Data repository
    │   │   │   └── PetRepository.java             # Spring Data repository
    │   │   └── web/
    │   │       ├── OwnerResource.java             # REST controller – owners
    │   │       ├── PetResource.java               # REST controller – pets
    │   │       ├── PetDetails.java                # Response DTO
    │   │       ├── PetRequest.java                # Request DTO
    │   │       └── ResourceNotFoundException.java # 404 exception
    │   └── resources/
    │       ├── application.yml                    # Boot configuration
    │       ├── logback-spring.xml                 # Logging configuration
    │       └── db/
    │           ├── mysql/
    │           │   ├── schema.sql                 # MySQL DDL
    │           │   └── data.sql                   # MySQL seed data
    │           └── hsqldb/
    │               ├── schema.sql                 # HSQLDB DDL (for tests)
    │               └── data.sql                   # HSQLDB seed data (for tests)
    └── test/
        └── java/…                                 # Unit & slice tests
```

---

## Domain Model

### Owner

Represents a pet clinic customer.

| Field | Type | Constraints |
|---|---|---|
| `id` | `Integer` | Auto-generated primary key |
| `firstName` | `String` | Required |
| `lastName` | `String` | Required |
| `address` | `String` | Required |
| `city` | `String` | Required |
| `telephone` | `String` | Required, max 12 digits |
| `pets` | `List<Pet>` | One-to-many |

### Pet

A pet belonging to an owner.

| Field | Type | Constraints |
|---|---|---|
| `id` | `Integer` | Auto-generated primary key |
| `name` | `String` | Required |
| `birthDate` | `Date` | Required |
| `type` | `PetType` | Foreign key → `types` table |
| `owner` | `Owner` | Foreign key → `owners` table |

### PetType

A category of pet (e.g. `cat`, `dog`, `lizard`, `snake`, `bird`, `hamster`).

| Field | Type |
|---|---|
| `id` | `Integer` |
| `name` | `String` |

---

## REST API

All endpoints accept and return `application/json`.

### Owners

| Method | Path | Description | Status |
|---|---|---|---|
| `POST` | `/owners` | Create a new owner | `201 Created` |
| `GET` | `/owners` | List all owners | `200 OK` |
| `GET` | `/owners/{ownerId}` | Get a single owner by ID | `200 OK` |
| `PUT` | `/owners/{ownerId}` | Update an existing owner | `204 No Content` |

### Pets

| Method | Path | Description | Status |
|---|---|---|---|
| `GET` | `/petTypes` | List all pet types | `200 OK` |
| `POST` | `/owners/{ownerId}/pets` | Add a pet to an owner | `201 Created` |
| `GET` | `/owners/*/pets/{petId}` | Get pet details | `200 OK` |
| `PUT` | `/owners/*/pets/{petId}` | Update a pet | `204 No Content` |

### Actuator (health & metrics)

| Path | Description |
|---|---|
| `GET /actuator/health` | Liveness / readiness probe |
| `GET /actuator/metrics` | Prometheus-compatible metrics |
| `GET /actuator/env` | Exposed environment properties |

---

## Configuration

Configuration is externalised to a **Spring Cloud Config Server**. The service looks for the config server at the URL defined by the `CONFIG_SERVER_URL` environment variable (default: `http://localhost:8888/`).

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: customers-service
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888/}
```

When the `docker` Spring profile is active (set automatically in Docker Compose deployments), the config server URL switches to `http://config-server:8888`.

A separate `application-test.yml` is used by tests to disable the Config Server and Eureka and to use the embedded HSQLDB database instead.

---

## Database

The service supports two databases:

| Database | Usage |
|---|---|
| **MySQL 8** | Production deployments |
| **HSQLDB** | Local development & automated tests |

DDL and seed data are in `src/main/resources/db/{mysql,hsqldb}/`.

The seed data includes 6 pet types, 10 sample owners, and 13 sample pets.

---

## Building

Requires **Java 17+** and **Maven 3.6+**.

```bash
# Compile and package
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build a Docker image (requires Docker)
mvn clean package -P buildDocker
```

The packaged JAR is written to `target/customers-service-3.4.1.jar`.

---

## Running the Application

### Standalone (embedded HSQLDB, no external dependencies)

```bash
java -jar target/customers-service-3.4.1.jar
```

The service starts on port **8081**. The Config Server and Eureka are optional in this mode.

### With a MySQL database

```bash
java -jar target/customers-service-3.4.1.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/petclinic \
  --spring.datasource.username=pc \
  --spring.datasource.password=pc
```

### With the full microservices stack (Docker Compose)

Start the Config Server and Eureka Server first, then run this service with the `docker` profile:

```bash
java -jar target/customers-service-3.4.1.jar \
  --spring.profiles.active=docker \
  --CONFIG_SERVER_URL=http://config-server:8888/
```

---

## Testing

```bash
# Run all tests
mvn test
```

Tests use the `test` Spring profile, which:

- Disables the Config Server and Eureka.
- Uses an embedded HSQLDB database populated with the seed scripts under `db/hsqldb/`.
- Uses Spring's `MockMvc` for controller slice tests.

---

## Monitoring

| Mechanism | Details |
|---|---|
| **Prometheus** | Metrics exposed at `/actuator/metrics` via Micrometer |
| **Jolokia** | JMX beans accessible over HTTP at `/actuator/jolokia` |
| **Chaos Monkey** | Resilience/fault-injection testing support |

The service also emits `@Timed` Micrometer metrics for every owner and pet endpoint, grouped under the `petclinic.owner` and `petclinic.pet` meter names.

---

## License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
