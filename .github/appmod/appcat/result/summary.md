# App Modernization Assessment Summary

**Target Azure Services**: Azure Kubernetes Service, Azure Container Apps, Azure App Service

## Overall Statistics

**Total Applications**: 1

**Name: customers-service**
- Mandatory: 7 issues
- Potential: 18 issues
- Optional: 4 issues

> **Severity Levels Explained:**
> - **Mandatory**: The issue has to be resolved for the migration to be successful.
> - **Potential**: This issue may be blocking in some situations but not in others. These issues should be reviewed to determine whether a change is required or not.
> - **Optional**: The issue discovered is real issue fixing which could improve the app after migration, however it is not blocking.

## Applications Profile

### Name: customers-service
- **JDK Version**: 17
- **Frameworks**: Spring Boot, Spring Cloud, Spring
- **Languages**: Java, JavaScript
- **Build Tools**: Maven

**Key Findings**:
- **Mandatory Issues (528 locations)**:
  - <!--ruleid=azure-aws-config-secret-manager-05000-->AWS Secrets Manager configuration (2 locations found)
  - <!--ruleid=google-gcr-to-azure-acr-01000-->Google Container Registry (GCR) or Artifact Registry usage detected (4 locations found)
  - <!--ruleid=java-8-deprecate-odbc-00001-->Removal of the JDBC-ODBC Bridge (1 location found)
  - <!--ruleid=dockerfile-00000-->No Dockerfile found (1 location found)
  - <!--ruleid=azure-aws-config-credential-01000-->AWS credential configuration (2 locations found)
  - <!--ruleid=unsecure-network-protocol-00000-->Use of unsecured network protocols or URI libraries (517 locations found)
  - <!--ruleid=embedded-cache-15000-->Caching - Spring Boot Cache library (1 location found)
- **Potential Issues (28 locations)**:
  - <!--ruleid=spring-boot-to-azure-eureka-02000-->Embedded framework - Eureka Client (1 location found)
  - <!--ruleid=azure-tas-binding-01000-->Tanzu Application Service service bindings (2 locations found)
  - <!--ruleid=azure-database-config-mongodb-02000-->MongoDB connection found in configuration file (1 location found)
  - <!--ruleid=azure-database-postgresql-02000-->PostgreSQL database found (5 locations found)
  - <!--ruleid=azure-database-microsoft-sql-03000-->Microsoft SQL database found (4 locations found)
  - <!--ruleid=azure-database-microsoft-oracle-07000-->Oracle database found (11 locations found)
  - <!--ruleid=spring-boot-to-azure-config-server-03000-->Embedded library - Spring Cloud Config (1 location found)
  - <!--ruleid=azure-database-microsoft-mariadb-06000-->MariaDB database found (3 locations found)
- **Optional Issues (3074 locations)**:
  - <!--ruleid=localhost-00004-->Localhost Usage (25 locations found)
  - <!--ruleid=database-reliability-01000-->Consider database reliability when migrating to Azure (1 location found)
  - <!--ruleid=azure-message-queue-amqp-02000-->Spring AMQP dependency found (43 locations found)
  - <!--ruleid=hardcoded-urls-00001-->Avoid using hardcoded URLs (HTTP protocol) in source code (3005 locations found)

## Next Steps

For comprehensive migration guidance and best practices, visit:
- [GitHub Copilot App Modernization](https://aka.ms/ghcp-appmod)

