# Architecture Diagram

This Spring Boot microservice implements a RESTful customers service with JPA-based data access, Spring Cloud Config integration, and Eureka service discovery.

## Application Architecture

```mermaid
flowchart TD
    subgraph Client["Client Layer"]
        APIClient["REST API Clients"]
    end
    subgraph App["Application Layer - Spring Boot 3.4.1 / Java 17"]
        Web["Spring MVC REST Controllers"]
        Config["Spring Cloud Config Client"]
    end
    subgraph Domain["Domain / Data Access Layer"]
        JPA["Spring Data JPA"]
        DB[("MySQL Database")]
        HSQL[("HSQLDB - In-Memory")]
    end
    subgraph External["External Services"]
        ConfigServer["Spring Cloud Config Server"]
        Eureka["Netflix Eureka Discovery"]
        Prometheus["Prometheus Metrics"]
        AzureMySQL["Azure MySQL - spring-cloud-azure-starter-jdbc-mysql"]
        ChaosMonkey["Chaos Monkey"]
    end

    APIClient -->|"HTTP REST calls port 8081"| Web
    Web -->|"entity CRUD"| JPA
    JPA -->|"SQL queries"| DB
    JPA -->|"in-memory testing"| HSQL
    DB -->|"Azure-managed connection"| AzureMySQL
    Config -->|"fetch properties"| ConfigServer
    App -->|"register service"| Eureka
    App -->|"expose metrics"| Prometheus
    ChaosMonkey -.->|"fault injection"| App
```

## Component Relationships

```mermaid
flowchart LR
    subgraph Presentation["Presentation Layer"]
        OwnerResource["OwnerResource\nREST Controller"]
        PetResource["PetResource\nREST Controller"]
        PetDetails["PetDetails\nDTO"]
        PetRequest["PetRequest\nDTO"]
        ResourceNotFoundException["ResourceNotFoundException\nException Handler"]
    end
    subgraph Domain["Domain Layer"]
        Owner["Owner\nJPA Entity"]
        Pet["Pet\nJPA Entity"]
        PetType["PetType\nJPA Entity"]
    end
    subgraph DataAccess["Data Access Layer"]
        OwnerRepository["OwnerRepository\nJpaRepository"]
        PetRepository["PetRepository\nJpaRepository"]
    end
    subgraph Infrastructure["Infrastructure"]
        MetricConfig["MetricConfig\nMicrometer Setup"]
        CustomersServiceApplication["CustomersServiceApplication\nEntry Point"]
    end

    OwnerResource -->|"queries"| OwnerRepository
    PetResource -->|"queries"| PetRepository
    PetResource -->|"lookups"| OwnerRepository
    OwnerResource -->|"maps to"| Owner
    PetResource -->|"maps to"| Pet
    PetResource -->|"maps to"| PetType
    PetResource -->|"returns"| PetDetails
    PetResource -->|"accepts"| PetRequest
    OwnerResource -->|"throws"| ResourceNotFoundException
    PetResource -->|"throws"| ResourceNotFoundException
    OwnerRepository -->|"manages"| Owner
    PetRepository -->|"manages"| Pet
    Owner -->|"one-to-many"| Pet
    Pet -->|"many-to-one"| PetType
    MetricConfig -.->|"instruments"| Presentation
    CustomersServiceApplication -.->|"bootstraps"| Infrastructure
```
