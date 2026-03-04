# Spring PetClinic – Customers Service

This microservice is part of the [Spring PetClinic Microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) sample application. It manages **owners** (customers) and their **pets**, exposing a REST API consumed by other services in the system.

## Features

### Owner Management

Owners represent the customers of the pet clinic. The service exposes the following REST endpoints under `/owners`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/owners` | Create a new owner |
| `GET` | `/owners` | List all owners |
| `GET` | `/owners/{ownerId}` | Get a single owner by ID |
| `PUT` | `/owners/{ownerId}` | Update an existing owner |

Each owner has a first name, last name, street address, city, and telephone number. All fields are validated on write.

### Pet Management

Pets belong to an owner and have an associated pet type (e.g. cat, dog, bird). The following endpoints are available:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/petTypes` | List all available pet types |
| `POST` | `/owners/{ownerId}/pets` | Add a new pet to an owner |
| `PUT` | `/owners/*/pets/{petId}` | Update an existing pet |
| `GET` | `/owners/*/pets/{petId}` | Get details for a specific pet |

A pet has a name, birth date, and a reference to its pet type and owning customer.

### Observability

- **Metrics**: Every REST endpoint is timed and exported via [Micrometer](https://micrometer.io/) with Prometheus support. Metrics are tagged with `application=petclinic` and method-level timings are registered under the `petclinic.owner` and `petclinic.pet` meters.
- **Actuator**: Spring Boot Actuator endpoints (health, info, metrics) are enabled for operational visibility.
- **Logging**: Logback is configured with JMX support so log levels can be changed at runtime via Spring Boot Admin.

### Spring Cloud Integration

- **Service Discovery**: Registers with a Netflix Eureka server so other microservices can locate it by name (`customers-service`).
- **Centralized Configuration**: Fetches its configuration from a Spring Cloud Config Server (defaults to `http://localhost:8888`). In a Docker environment the config server URL is set to `http://config-server:8888` automatically via the `docker` Spring profile.

### Database Support

| Profile | Database | Purpose |
|---------|----------|---------|
| Default / test | HSQLDB (in-memory) | Local development and unit tests |
| Production | MySQL | Persistent storage |

Azure-managed MySQL connectivity is provided through the `spring-cloud-azure-starter-jdbc-mysql` dependency, enabling passwordless authentication in Azure environments.

### Resilience Testing

The [Chaos Monkey for Spring Boot](https://codecentric.github.io/chaos-monkey-spring-boot/latest/) library is included as an optional dependency to enable fault-injection experiments (latency, exceptions) without code changes.

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1 |
| Cloud | Spring Cloud 2024.0.0 |
| Persistence | Spring Data JPA / Hibernate |
| Databases | MySQL (prod), HSQLDB (test) |
| Metrics | Micrometer + Prometheus |
| Service Discovery | Netflix Eureka |
| Config Management | Spring Cloud Config |
| Build | Maven |

## Running Locally

```bash
# Start the service (connects to a local Config Server on port 8888)
./mvnw spring-boot:run

# Run the tests
./mvnw test
```

The service listens on port **8081** by default.

## Project Structure

```
src/
├── main/java/.../customers/
│   ├── CustomersServiceApplication.java   # Application entry point
│   ├── config/MetricConfig.java           # Micrometer configuration
│   ├── model/                             # JPA entities & repositories
│   │   ├── Owner.java / OwnerRepository.java
│   │   ├── Pet.java / PetRepository.java
│   │   └── PetType.java
│   └── web/                               # REST controllers & DTOs
│       ├── OwnerResource.java
│       ├── PetResource.java
│       ├── PetDetails.java
│       ├── PetRequest.java
│       └── ResourceNotFoundException.java
├── main/resources/
│   ├── application.yml                    # Application configuration
│   ├── db/hsqldb/                         # HSQLDB schema & seed data
│   └── db/mysql/                          # MySQL schema & seed data
└── test/
    └── java/.../web/PetResourceTest.java  # Controller unit tests
```
