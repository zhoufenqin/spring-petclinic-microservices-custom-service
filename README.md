# Spring PetClinic – Customers Service

A Spring Boot microservice that manages **pet owners and their pets** as part of the [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) sample application.

## Overview

The Customers Service is a RESTful API responsible for:

- Creating and updating **pet owners**
- Creating and updating **pets** belonging to owners
- Serving **pet-type reference data** (cat, dog, bird, etc.)

It integrates with the wider microservices platform via Spring Cloud Config (centralised configuration) and Netflix Eureka (service discovery).

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Cloud | Spring Cloud 2024.0.0 |
| Persistence | Spring Data JPA (MySQL / HSQLDB) |
| Service Discovery | Netflix Eureka Client |
| Config Server | Spring Cloud Config Client |
| Observability | Micrometer + Prometheus |
| Cloud DB | Azure JDBC MySQL Connector |
| Build | Maven |

## Project Structure

```
src/
├── main/java/.../customers/
│   ├── CustomersServiceApplication.java   # Spring Boot entry point
│   ├── config/
│   │   └── MetricConfig.java              # Prometheus / @Timed AOP config
│   ├── model/
│   │   ├── Owner.java                     # JPA entity – owner
│   │   ├── Pet.java                       # JPA entity – pet
│   │   ├── PetType.java                   # JPA entity – pet type
│   │   ├── OwnerRepository.java           # Spring Data repository
│   │   └── PetRepository.java             # Spring Data repository
│   └── web/
│       ├── OwnerResource.java             # REST controller – owners
│       ├── PetResource.java               # REST controller – pets
│       ├── PetRequest.java                # Inbound DTO for pet mutations
│       ├── PetDetails.java                # Outbound DTO for pet reads
│       └── ResourceNotFoundException.java # 404 exception
└── main/resources/
    ├── application.yml                    # App config (profiles: docker)
    ├── db/hsqldb/                         # HSQLDB DDL + sample data
    └── db/mysql/                          # MySQL DDL + sample data
```

## REST API

### Owners – `/owners`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/owners` | Create a new owner | `201 Created` – owner JSON |
| `GET` | `/owners` | List all owners | `200 OK` – array of owners |
| `GET` | `/owners/{ownerId}` | Get owner by ID | `200 OK` – owner JSON |
| `PUT` | `/owners/{ownerId}` | Update owner fields | `204 No Content` |

**Owner fields:** `firstName`, `lastName`, `address`, `city`, `telephone`

### Pets – `/petTypes`, `/owners/{ownerId}/pets`, `/owners/*/pets/{petId}`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/petTypes` | List all pet types | `200 OK` – array of types |
| `POST` | `/owners/{ownerId}/pets` | Create a pet for an owner | `201 Created` – pet JSON |
| `PUT` | `/owners/*/pets/{petId}` | Update a pet | `204 No Content` |
| `GET` | `/owners/*/pets/{petId}` | Get pet details | `200 OK` – PetDetails JSON |

**Pet fields:** `name`, `birthDate`, `typeId`

A `404` (`ResourceNotFoundException`) is returned when an owner or pet is not found.

## Database Schema

```
types   (id, name)
owners  (id, first_name, last_name, address, city, telephone)
pets    (id, name, birth_date, type_id → types, owner_id → owners)
```

Sample data (10 owners, 13 pets, 6 pet types) is loaded automatically from SQL scripts in `src/main/resources/db/`.

## Configuration

Configuration is fetched from the Spring Cloud Config Server at startup.

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `customers-service` | Service name used for discovery and config lookup |
| `CONFIG_SERVER_URL` | `http://localhost:8888/` | Config server base URL (env var) |
| Docker profile config server | `http://config-server:8888` | Used when `spring.profiles.active=docker` |
| Server port | `8081` | HTTP port the service listens on |

For local development without a running config server, set `spring.config.import=optional:configserver:...` (already the default) – the service will start with its local `application.yml` only.

## Running Locally

### Prerequisites

- JDK 17+
- Maven 3.6+
- (Optional) a running MySQL instance or use the embedded HSQLDB for tests

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

The service starts on **http://localhost:8081**.

### Run Tests

```bash
mvn test
```

Tests use an embedded HSQLDB database and disable Eureka / Config Server discovery (see `application-test.yml`).

## Observability

- **Health & info:** `GET /actuator/health`, `GET /actuator/info`
- **Prometheus metrics:** `GET /actuator/prometheus`
- All controller methods are timed via the `@Timed("petclinic.owner")` and `@Timed("petclinic.pet")` annotations (wired by `MetricConfig`).

## Part of the PetClinic Microservices Ecosystem

This service is one of several in the Spring PetClinic Microservices reference application:

| Service | Responsibility |
|---------|---------------|
| **customers-service** *(this repo)* | Owners & pets |
| visits-service | Vet visit records |
| vets-service | Veterinarians |
| api-gateway | Single entry-point / BFF |
| config-server | Centralised configuration |
| discovery-server | Eureka service registry |
