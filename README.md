# Spring PetClinic – Customers Service

A standalone, cloud-ready Spring Boot microservice that manages **owners** (customers) and their **pets** for the [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) application. This service is extracted from the broader Spring PetClinic microservices architecture and is independently deployable, including to Azure.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [REST API](#rest-api)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Build](#build)
  - [Run](#run)
  - [Test](#test)
- [Configuration](#configuration)
- [Database](#database)
- [Monitoring & Observability](#monitoring--observability)
- [Azure Deployment](#azure-deployment)

---

## Overview

| Property        | Value                    |
|-----------------|--------------------------|
| **Service name**| `customers-service`      |
| **Version**     | 3.4.1                    |
| **Java version**| 17                       |
| **Default port**| 8081                     |
| **Build tool**  | Maven                    |

This service is responsible for:

- **Owner (customer) management** – create, read, and update pet owners.
- **Pet management** – create, read, and update pets associated with an owner.
- **Pet type lookup** – retrieve the list of supported pet types (cat, dog, hamster, etc.).

---

## Architecture

The service is a single deployable unit within a larger microservices ecosystem.

```
┌──────────────────────────────────────┐
│           customers-service          │
│                                      │
│  ┌───────────┐   ┌───────────────┐   │
│  │OwnerResource│  │  PetResource  │   │
│  └─────┬─────┘   └──────┬────────┘   │
│        │                │            │
│  ┌─────▼────────────────▼────────┐   │
│  │   Owner / Pet / PetType JPA   │   │
│  │         Repositories          │   │
│  └──────────────┬────────────────┘   │
│                 │                    │
│  ┌──────────────▼────────────────┐   │
│  │        MySQL / HSQLDB         │   │
│  └───────────────────────────────┘   │
└──────────────────────────────────────┘
         │                  │
  Spring Cloud         Spring Cloud
  Config Server        Eureka (registry)
```

**Outbound dependencies (optional):**

| Service | Purpose | Default URL |
|---------|---------|-------------|
| Spring Cloud Config Server | Centralized configuration | `http://localhost:8888/` |
| Eureka Server | Service discovery & registration | configured by Config Server |

Both dependencies are optional for local development (the service starts without them).

---

## Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Cloud | Spring Cloud 2024.0.0 |
| Service discovery | Spring Cloud Netflix Eureka Client |
| Configuration | Spring Cloud Config |
| Persistence | Spring Data JPA / Hibernate |
| Production DB | MySQL |
| Test DB | HSQLDB (in-memory) |
| Metrics | Micrometer + Prometheus |
| Remote management | Jolokia (JMX over HTTP) |
| Code generation | Lombok |
| Resilience testing | Chaos Monkey for Spring Boot |
| Azure integration | Spring Cloud Azure (v5.20.1) |

---

## REST API

### Owners

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/owners` | Create a new owner | `201 Created` |
| `GET` | `/owners` | List all owners | `200 OK` |
| `GET` | `/owners/{ownerId}` | Get an owner by ID | `200 OK` / `404` |
| `PUT` | `/owners/{ownerId}` | Update an owner | `204 No Content` |

### Pets

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/petTypes` | List all pet types | `200 OK` |
| `POST` | `/owners/{ownerId}/pets` | Add a pet to an owner | `201 Created` |
| `GET` | `/owners/{ownerId}/pets/{petId}` | Get pet details | `200 OK` |
| `PUT` | `/owners/{ownerId}/pets/{petId}` | Update a pet | `204 No Content` |

### Example – Create an owner

```bash
curl -X POST http://localhost:8081/owners \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "address": "123 Main St",
    "city": "Madison",
    "telephone": "6085551234"
  }'
```

### Example – Add a pet

```bash
curl -X POST http://localhost:8081/owners/1/pets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Buddy",
    "birthDate": "2020-01-15",
    "typeId": 1
  }'
```

---

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **MySQL** *(optional – HSQLDB is used by default)*
- **Spring Cloud Config Server** *(optional – falls back gracefully)*

### Build

```bash
# Build (skipping tests)
mvn clean package -DskipTests

# Build and run all tests
mvn clean package
```

The executable JAR is created at `target/customers-service-3.4.1.jar`.

### Run

```bash
# Using the JAR
java -jar target/customers-service-3.4.1.jar

# Using the Maven plugin
mvn spring-boot:run
```

The service starts on **port 8081** by default. It uses an in-memory HSQLDB database when no external database is configured.

#### Docker

```bash
# Build the Docker image
mvn clean package -P buildDocker -DskipTests

# Run the container
docker run -p 8081:8081 customers-service:3.4.1
```

### Test

```bash
# Run unit tests
mvn test
```

Tests use an in-memory HSQLDB database and have Eureka / Config Server disabled, so no external services are needed.

---

## Configuration

The service reads its configuration from **Spring Cloud Config Server** when available.

| Property | Default | Description |
|----------|---------|-------------|
| `spring.application.name` | `customers-service` | Service identifier in Eureka / Config |
| `CONFIG_SERVER_URL` | `http://localhost:8888/` | Config server location |
| `spring.datasource.url` | HSQLDB in-memory | Override for MySQL |
| `spring.datasource.username` | – | Database username |
| `spring.datasource.password` | – | Database password |

Active Spring profiles:

| Profile | Purpose |
|---------|---------|
| *(default)* | Local development; Config Server is optional |
| `docker` | Config Server is required; URL uses Docker network host `config-server` |
| `test` | Config Server and Eureka disabled; HSQLDB schema loaded from SQL scripts |

---

## Database

### Schema

```sql
CREATE TABLE types   (id INT, name VARCHAR(80));
CREATE TABLE owners  (id INT, first_name VARCHAR(30), last_name  VARCHAR(30),
                      address   VARCHAR(255), city VARCHAR(80), telephone VARCHAR(20));
CREATE TABLE pets    (id INT, name VARCHAR(30), birth_date DATE,
                      type_id INT REFERENCES types(id),
                      owner_id INT REFERENCES owners(id));
```

MySQL scripts are located in `src/main/resources/db/mysql/`.  
HSQLDB scripts (for testing) are in `src/main/resources/db/hsqldb/`.

Sample data includes **10 owners** and **13 pets** loaded at startup.

### Using MySQL

```bash
java \
  -Dspring.datasource.url=jdbc:mysql://localhost:3306/petclinic \
  -Dspring.datasource.username=pc \
  -Dspring.datasource.password=pc \
  -jar target/customers-service-3.4.1.jar
```

---

## Monitoring & Observability

Spring Boot Actuator exposes standard management endpoints:

| Endpoint | Description |
|----------|-------------|
| `GET /actuator/health` | Application health status |
| `GET /actuator/metrics` | All available metrics |
| `GET /actuator/prometheus` | Prometheus-formatted metrics |

Jolokia (`/actuator/jolokia`) provides JMX access over HTTP, enabling tools such as Spring Boot Admin to manage log levels at runtime.

---

## Azure Deployment

The service includes first-class support for Azure through **Spring Cloud Azure**:

- **Azure Database for MySQL** via `spring-cloud-azure-starter-jdbc-mysql`
- Compatible with **Azure Kubernetes Service (AKS)**, **Azure Container Apps**, and **Azure App Service**
- App modernization assessment workflow in `.github/appmod/`

For guidance on deploying to Azure, refer to the [Azure Spring Apps documentation](https://learn.microsoft.com/azure/spring-apps/).

---

## Project Structure

```
src/
├── main/
│   ├── java/org/springframework/samples/petclinic/customers/
│   │   ├── CustomersServiceApplication.java   # Entry point
│   │   ├── config/
│   │   │   └── MetricConfig.java              # Metrics bean configuration
│   │   ├── model/
│   │   │   ├── Owner.java                     # Owner entity
│   │   │   ├── OwnerRepository.java           # Owner JPA repository
│   │   │   ├── Pet.java                       # Pet entity
│   │   │   ├── PetRepository.java             # Pet JPA repository
│   │   │   └── PetType.java                   # PetType entity
│   │   └── web/
│   │       ├── OwnerResource.java             # Owner REST controller
│   │       ├── PetResource.java               # Pet REST controller
│   │       ├── PetDetails.java                # Pet response DTO
│   │       ├── PetRequest.java                # Pet request DTO
│   │       └── ResourceNotFoundException.java # 404 exception
│   └── resources/
│       ├── application.yml                    # Main configuration
│       ├── logback-spring.xml                 # Logging configuration
│       └── db/
│           ├── mysql/                         # MySQL schema & seed data
│           └── hsqldb/                        # HSQLDB schema & seed data (tests)
└── test/
    ├── java/.../web/PetResourceTest.java      # Controller unit tests
    └── resources/application-test.yml         # Test profile configuration
```
