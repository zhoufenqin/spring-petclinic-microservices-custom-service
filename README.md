# Spring PetClinic Customers Service

This is the Customers microservice for the Spring PetClinic application, built as part of a microservices architecture demonstration.

## Overview

The Customers Service manages pet owners and their pets. It provides REST APIs for:
- Creating, reading, and updating owner information
- Managing pets associated with owners
- Querying available pet types

## Technology Stack

- **Java 17**: Modern Java with enhanced features
- **Spring Boot 3.4.1**: Latest stable Spring Boot version
- **Spring Cloud 2024.0.0**: For microservices patterns and service discovery
- **Spring Data JPA**: For data persistence
- **MySQL/HSQLDB**: Database support (MySQL for production, HSQLDB for development/testing)
- **Lombok**: To reduce boilerplate code
- **Azure Spring Cloud**: For Azure integration capabilities

## Key Features

### Modern Java Practices
- ✅ Uses `java.time.LocalDate` instead of deprecated `java.util.Date`
- ✅ Modern Optional handling with `orElseThrow()` pattern
- ✅ Clean REST API design with proper HTTP status codes
- ✅ Comprehensive Javadoc documentation

### Monitoring & Resilience
- Spring Boot Actuator for health checks and metrics
- Prometheus integration for metrics collection
- Chaos Monkey for Spring Boot - resilience testing support
- Jolokia for JMX over HTTP

### Service Discovery
- Netflix Eureka client for service registration and discovery
- Spring Cloud Config for centralized configuration

## REST API Endpoints

### Owner Management
- `POST /owners` - Create a new owner
- `GET /owners` - List all owners
- `GET /owners/{ownerId}` - Get owner by ID
- `PUT /owners/{ownerId}` - Update owner information

### Pet Management
- `GET /petTypes` - List available pet types
- `POST /owners/{ownerId}/pets` - Create a pet for an owner
- `GET /owners/*/pets/{petId}` - Get pet details
- `PUT /owners/*/pets/{petId}` - Update pet information

## Building and Running

### Prerequisites
- JDK 17 or later
- Maven 3.6+
- MySQL (for production) or HSQLDB (auto-configured for dev/test)

### Build
```bash
mvn clean package
```

### Run Tests
```bash
mvn test
```

### Run Locally
```bash
mvn spring-boot:run
```

The service will start on port 8081 by default (configured via `docker.image.exposed.port`).

## Configuration

The service uses Spring Cloud Config for external configuration. Set the `CONFIG_SERVER_URL` environment variable to point to your config server:

```bash
CONFIG_SERVER_URL=http://config-server:8888
```

For Docker deployments, use the `docker` profile which automatically configures the config server URL.

## Database

### Development/Testing
Uses HSQLDB in-memory database (no setup required).

### Production
Configure MySQL connection:
- Database schemas available in `src/main/resources/db/mysql/`
- Azure MySQL integration via `spring-cloud-azure-starter-jdbc-mysql`

## Dependencies

Recent updates:
- HSQLDB: 2.7.4
- Jolokia: 1.7.2
- Lombok: 1.18.42
- Chaos Monkey: 3.2.2

## Development Notes

### Code Quality
- Uses Lombok `@Data`, `@Getter`, `@Setter` to minimize boilerplate
- Follows REST best practices with proper HTTP status codes
- Implements validation with Jakarta Bean Validation
- Exception handling via `@ResponseStatus` and custom exceptions

### Monitoring
Access actuator endpoints at `/actuator/*` for:
- Health checks
- Metrics
- Application info

## Contributing

This service follows Spring Boot and Spring Cloud best practices. When contributing:
1. Maintain minimal, focused changes
2. Add tests for new functionality
3. Update documentation
4. Follow existing code style and patterns

## License

Licensed under the Apache License, Version 2.0
