# Project Facts Report

**Project**: Spring PetClinic Customers Service (`customers-service`)
**Version**: 3.4.1
**Assessment Date**: 2026-03-31
**Language**: Java 17
**Framework**: Spring Boot 3.4.1

---

## Application Identity

| Fact | Value | Confidence |
|------|-------|------------|
| **Application Name** | `customers-service` (Spring PetClinic Customers Service) | High |
| **Application Type** | REST API (Spring MVC microservice) | High |
| **Application Port** | 8081 (HTTP) | Medium |
| **Version** | 3.4.1 (release) | High |
| **Architecture Pattern** | Microservices with Layered internal structure | High |

---

## Runtime & Language

| Fact | Value | Confidence |
|------|-------|------------|
| **Runtime Environment** | Java 17, Spring Boot 3.4.1, Spring Cloud 2024.0.0 | High |
| **Servlet Container** | Embedded Apache Tomcat 10.1.x (Jakarta Servlet 6.0 / Jakarta EE 10) | High |
| **Packaging** | Spring Boot executable JAR | High |
| **Operating System** | Linux (ubuntu-latest CI/CD; container OS unspecified) | Medium |

---

## Container & Deployment

| Fact | Value | Confidence |
|------|-------|------------|
| **Container Engine** | Docker (via Spotify docker-maven-plugin v1.2.0) | Medium |
| **Base Image** | Not determined (Dockerfile in external parent directory) | High |
| **Image Size** | Estimated 300–450 MB (JRE 17 ~180 MB + fat JAR ~80–150 MB) | Low |
| **Image Layers** | Not applicable — no Dockerfile in repository | High |
| **Multi-stage Build** | Not applicable — no Dockerfile in repository | High |
| **Container Version** | Not specified | High |
| **Volume Mounts** | None configured | High |
| **Network Settings** | None configured in repository | High |
| **Resource Limits** | None configured | High |
| **Orchestration Tool** | None in repository (targets AKS / Azure Container Apps) | High |
| **Service Definition** | None in repository | High |

---

## Configuration & Profiles

| Fact | Value | Confidence |
|------|-------|------------|
| **Environment Variables** | `CONFIG_SERVER_URL` (default: `http://localhost:8888/`) | Medium |
| **Profile Settings** | Spring profiles: `docker`, `test`; Maven profile: `buildDocker` | High |
| **XML Configs** | `logback-spring.xml` only; rest is annotation-based | High |

---

## External Services & Dependencies

| Fact | Value | Confidence |
|------|-------|------------|
| **External Services** | MySQL (Azure-managed), HSQLDB (test), Spring Cloud Config Server, Netflix Eureka | Medium |
| **External Dependencies** | MySQL, HSQLDB, Spring Cloud Config Server, Netflix Eureka | Medium |
| **Communication Protocols** | HTTP/REST (Spring MVC), HTTP (Config Server), HTTP (Eureka) | High |
| **Language Dependencies** | 14 runtime/compile Maven dependencies | High |

---

## Security & Compliance

| Fact | Value | Confidence |
|------|-------|------------|
| **Security Implementation** | No authentication/authorization at application layer; Azure Managed Identity for MySQL | High |
| **Data Classification** | **PII** — Owner data: first_name, last_name, address, city, telephone | High |
| **Compliance Requirements** | None declared; potential GDPR consideration for PII | Medium |
| **Licensing Information** | No LICENSE file; derived from Spring PetClinic (Apache 2.0) | Low |

---

## Observability & Instrumentation

| Fact | Value | Confidence |
|------|-------|------------|
| **Health Checks** | Spring Boot Actuator `/actuator/health` (no container-level probe configured) | Medium |
| **Startup Instrumentation** | Logback, Micrometer `@Timed` AOP on controllers, Prometheus export, Jolokia JMX | High |
| **Testing Framework** | JUnit 5 (Jupiter), AssertJ, Spring Boot Test; 1 test file | High |
| **Embedded Language Usage** | None — pure Java implementation | High |

---

## Non-Functional Facts

| Fact | Value | Confidence |
|------|-------|------------|
| **Hardware Requirements** | Estimated: 512 MB–1 GB RAM, 0.5–1 CPU core | Low |
| **System Packages** | Not applicable — no Dockerfile in repository | High |

---

## Key Dependencies Summary

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-web | 3.4.1 | REST API + embedded Tomcat |
| spring-boot-starter-data-jpa | 3.4.1 | JPA / Hibernate ORM |
| spring-boot-starter-actuator | 3.4.1 | Health, metrics endpoints |
| spring-cloud-starter-netflix-eureka-client | 2024.0.0 | Service discovery |
| spring-cloud-starter-config | 2024.0.0 | Centralized configuration |
| spring-cloud-azure-starter-jdbc-mysql | 5.20.1 | Azure MySQL / Managed Identity |
| mysql-connector-j | (managed) | MySQL JDBC driver |
| hsqldb | (managed) | In-memory DB for testing |
| micrometer-registry-prometheus | (managed) | Prometheus metrics |
| jolokia-core | 1.7.1 | JMX-HTTP bridge |
| lombok | (managed) | Boilerplate reduction |
| chaos-monkey-spring-boot | 3.1.0 | Resilience / chaos testing |

---

## Assessment Findings Summary

- ✅ **Modern stack**: Java 17 + Spring Boot 3.4.1 + Spring Cloud 2024.0.0 — cloud-native ready
- ✅ **Azure-ready**: `spring-cloud-azure-starter-jdbc-mysql` for passwordless Azure MySQL via Managed Identity
- ✅ **Observability**: Micrometer + Prometheus + Actuator health endpoint
- ⚠️ **No authentication**: REST endpoints are unauthenticated at the application level
- ⚠️ **PII data stored**: Owner personal data (name, address, telephone) requires protection measures
- ⚠️ **No LICENSE file**: Licensing should be clarified before production deployment
- ⚠️ **Minimal tests**: Only 1 test file (PetResourceTest.java) — test coverage should be improved
- ℹ️ **Dockerfile external**: Container definition resides in a parent repository; image configuration cannot be assessed from this repository alone
- ℹ️ **Config Server dependent**: Runtime configuration (DB URL, credentials) delivered via Spring Cloud Config Server
