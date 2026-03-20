# Spring PetClinic Customers Service

A cloud-native Spring Boot microservice that manages customer (owner) and pet data for the Spring PetClinic application. This service is part of the [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) ecosystem and has been enhanced with Azure cloud integration and app modernization tooling.

## Overview

The Customers Service is responsible for:

- Managing **owner** (customer) profiles — name, address, city, telephone
- Managing **pet** records associated with owners — name, birth date, type
- Providing a REST API consumed by other services (e.g., the API Gateway, Visits Service)
- Exposing health, metrics, and Prometheus endpoints for observability

## Technology Stack

| Component | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.4.1 |
| Spring Cloud | 2024.0.0 |
| Spring Cloud Azure | 5.20.1 |
| Database (production) | MySQL |
| Database (test) | HSQLDB (in-memory) |

### Key Dependencies

- **Spring Boot Web** — REST API
- **Spring Boot Data JPA** — ORM and database access
- **Spring Boot Actuator** — Health checks and metrics
- **Spring Cloud Config** — Externalized configuration via Config Server
- **Spring Cloud Netflix Eureka Client** — Service discovery and registration
- **Spring Cloud Azure JDBC MySQL** — Azure Database for MySQL integration
- **Micrometer + Prometheus** — Metrics collection and export
- **Chaos Monkey for Spring Boot** — Resilience and chaos engineering testing
- **Jolokia** — JMX-over-HTTP for remote management
- **Lombok** — Boilerplate code reduction
- **HSQLDB** — Embedded database for tests

## Project Structure

```
src/
├── main/
│   ├── java/org/springframework/samples/petclinic/customers/
│   │   ├── CustomersServiceApplication.java   # Application entry point
│   │   ├── config/
│   │   │   └── MetricConfig.java              # Micrometer metrics configuration
│   │   ├── model/
│   │   │   ├── Owner.java                     # Owner JPA entity
│   │   │   ├── OwnerRepository.java           # Owner Spring Data repository
│   │   │   ├── Pet.java                       # Pet JPA entity
│   │   │   ├── PetRepository.java             # Pet Spring Data repository
│   │   │   └── PetType.java                   # PetType JPA entity
│   │   └── web/
│   │       ├── OwnerResource.java             # Owner REST controller
│   │       ├── PetResource.java               # Pet REST controller
│   │       ├── PetDetails.java                # Pet response DTO
│   │       ├── PetRequest.java                # Pet request DTO
│   │       └── ResourceNotFoundException.java # 404 exception handler
│   └── resources/
│       ├── application.yml                    # Main application configuration
│       ├── db/
│       │   ├── mysql/                         # MySQL schema and seed data
│       │   └── hsqldb/                        # HSQLDB schema and seed data (tests)
│       └── logback-spring.xml                 # Logging configuration
└── test/
    ├── java/.../web/PetResourceTest.java      # Pet REST controller unit tests
    └── resources/application-test.yml         # Test profile configuration
```

## Data Model

The service manages three entities:

```
PetType           Owner              Pet
─────────         ─────────          ──────────────
id (PK)           id (PK)            id (PK)
name              first_name         name
                  last_name          birth_date
                  address            type_id  ──► PetType
                  city               owner_id ──► Owner
                  telephone
```

**Pre-loaded data** includes 6 pet types (cat, dog, lizard, snake, bird, hamster), 10 owners, and 13 pets.

## REST API

The service runs on port **8081** by default.

### Owners

| Method | Path | Description |
|---|---|---|
| `GET` | `/owners` | List all owners |
| `GET` | `/owners/{ownerId}` | Get a single owner (with their pets) |
| `POST` | `/owners` | Create a new owner |
| `PUT` | `/owners/{ownerId}` | Update an existing owner |

**Example — create an owner:**
```http
POST /owners
Content-Type: application/json

{
  "firstName": "Jane",
  "lastName": "Doe",
  "address": "123 Main St",
  "city": "Springfield",
  "telephone": "5551234567"
}
```

### Pets

| Method | Path | Description |
|---|---|---|
| `GET` | `/petTypes` | List all available pet types |
| `GET` | `/owners/{ownerId}/pets/{petId}` | Get pet details |
| `POST` | `/owners/{ownerId}/pets` | Add a pet to an owner |
| `PUT` | `/owners/{ownerId}/pets/{petId}` | Update a pet |

**Example — add a pet:**
```http
POST /owners/1/pets
Content-Type: application/json

{
  "name": "Fluffy",
  "birthDate": "2023-01-15",
  "typeId": 1
}
```

### Actuator / Observability

| Path | Description |
|---|---|
| `/actuator/health` | Application health |
| `/actuator/metrics` | Available metrics |
| `/actuator/prometheus` | Prometheus-format metrics |

## Building the Project

### Prerequisites

- Java 17+
- Maven 3.6+

### Build

```bash
# Compile and package
mvn clean package

# Skip tests during build
mvn clean package -DskipTests

# Build a Docker image (requires Docker)
mvn clean package -P buildDocker
```

The packaged JAR is produced at `target/customers-service-3.4.1.jar`.

## Running the Service

### Standalone (local, no external dependencies)

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8081` using the embedded HSQLDB database. Spring Cloud Config and Eureka are optional in standalone mode and default to `http://localhost:8888` if not configured.

### With a Config Server

Set the `CONFIG_SERVER_URL` environment variable to point at your Config Server:

```bash
CONFIG_SERVER_URL=http://my-config-server:8888 java -jar target/customers-service-3.4.1.jar
```

### Docker (docker profile)

When running inside a Docker network alongside a Config Server container named `config-server`:

```bash
docker run -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=docker \
  customers-service:3.4.1
```

The `docker` Spring profile instructs the service to fetch configuration from `http://config-server:8888`.

### With MySQL

Provide the standard Spring datasource properties (or configure them via the Config Server):

```bash
java -jar target/customers-service-3.4.1.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/petclinic \
  --spring.datasource.username=petclinic \
  --spring.datasource.password=petclinic
```

## Configuration

### application.yml

| Property | Default | Description |
|---|---|---|
| `spring.application.name` | `customers-service` | Service name registered with Eureka |
| `spring.config.import` | `optional:configserver:http://localhost:8888/` | Config Server URL |

### Profiles

| Profile | Description |
|---|---|
| _(default)_ | Standalone mode; Config Server and Eureka are optional |
| `docker` | Connects to `http://config-server:8888` (required) |
| `test` | HSQLDB in-memory database; Config Server and Eureka disabled |

## Running Tests

```bash
mvn test
```

Tests use the `test` Spring profile (defined in `src/test/resources/application-test.yml`), which:
- Uses HSQLDB as an embedded in-memory database
- Disables Spring Cloud Config Server import
- Disables Eureka service discovery

## Monitoring

The service exposes Prometheus-compatible metrics at `/actuator/prometheus`. Custom timers are registered for owner and pet operations, all tagged with `application=petclinic`.

Logging is configured via `logback-spring.xml` with Jolokia JMX support, enabling runtime log-level adjustments through Spring Boot Admin.

## Related Projects

- [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) — The full multi-service architecture (API Gateway, Vets Service, Visits Service, Config Server, Discovery Server)
- [Spring PetClinic](https://github.com/spring-projects/spring-petclinic) — The original monolithic reference application
