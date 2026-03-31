# Dependency Map

The Spring PetClinic Customers Service declares 18 runtime/compile dependencies managed via Maven, with version governance through Spring Boot 3.4.1 BOM and Spring Cloud 2024.0.0 BOM.

## Dependencies

```mermaid
flowchart LR
    App["customers-service v3.4.1"]

    subgraph BOM["BOM / Version Management"]
        SBParent["spring-boot-starter-parent 3.4.1"]
        SCDepMgmt["spring-cloud-dependencies 2024.0.0"]
        AzureDepMgmt["spring-cloud-azure-dependencies 5.20.1"]
    end
    subgraph Web["Web Frameworks"]
        SpringWeb["spring-boot-starter-web"]
        SpringCloud["spring-cloud-starter-bootstrap"]
        SpringConfig["spring-cloud-starter-config"]
        Eureka["spring-cloud-starter-netflix-eureka-client"]
    end
    subgraph DB["Database / ORM"]
        SpringJPA["spring-boot-starter-data-jpa"]
        MySQL["mysql-connector-j runtime"]
        HSQLDB["hsqldb runtime"]
        AzureJDBC["spring-cloud-azure-starter-jdbc-mysql 5.20.1"]
    end
    subgraph Obs["Observability"]
        Actuator["spring-boot-starter-actuator"]
        Prometheus["micrometer-registry-prometheus"]
        Jolokia["jolokia-core 1.7.1"]
    end
    subgraph Resil["Resilience"]
        ChaosMonkey["chaos-monkey-spring-boot 3.1.0"]
    end
    subgraph Util["Utilities"]
        Lombok["lombok provided"]
    end

    App -->|"version governance"| BOM
    App -->|"web"| Web
    App -->|"persistence"| DB
    App -->|"observability"| Obs
    App -->|"resilience"| Resil
    App -->|"utilities"| Util
    SBParent -.->|"governs"| Web
    SBParent -.->|"governs"| DB
    SCDepMgmt -.->|"governs"| Eureka
    SCDepMgmt -.->|"governs"| SpringConfig
    AzureDepMgmt -.->|"governs"| AzureJDBC
```
