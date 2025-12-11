# Azure Migration Technical Guide
## Spring PetClinic Customers Service - Implementation Details

---

## Table of Contents
1. [Dockerfile Implementation](#1-dockerfile-implementation)
2. [Kubernetes Manifests](#2-kubernetes-manifests)
3. [Azure Configuration](#3-azure-configuration)
4. [Security Implementation](#4-security-implementation)
5. [CI/CD Pipeline](#5-cicd-pipeline)
6. [Monitoring Setup](#6-monitoring-setup)

---

## 1. Dockerfile Implementation

### 1.1 Production-Ready Dockerfile

Create `Dockerfile` in the project root:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
COPY .mvn .mvn
RUN mvn dependency:resolve -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Extract layers for optimized image
RUN mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="petclinic-team"
LABEL application="customers-service"
LABEL version="3.4.1"

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy extracted layers from build stage
COPY --from=build --chown=spring:spring /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build --chown=spring:spring /app/target/dependency/META-INF /app/META-INF
COPY --from=build --chown=spring:spring /app/target/dependency/BOOT-INF/classes /app

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health/liveness || exit 1

# JVM configuration
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp /app:/app/lib/* org.springframework.samples.petclinic.customers.CustomersServiceApplication"]
```

### 1.2 .dockerignore

Create `.dockerignore`:

```
target/
!target/*.jar
.git
.gitignore
.mvn/wrapper/maven-wrapper.jar
*.md
.idea
.vscode
*.iml
```

---

## 2. Kubernetes Manifests

> **Note on Templating:** The manifests below use placeholder syntax `<VALUE>` for values that need to be replaced. In production, use one of these approaches:
> - **Kustomize**: Use `kustomize edit set image` for image tags and overlays for environment-specific values
> - **Helm**: Use Helm charts with values.yaml for templating
> - **CI/CD**: Replace placeholders during pipeline execution using `envsubst` or similar tools

### 2.1 Directory Structure

```
k8s/
├── base/
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── hpa.yaml
│   └── servicemonitor.yaml
├── overlays/
│   ├── development/
│   ├── staging/
│   └── production/
└── kustomization.yaml
```

### 2.2 deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: customers-service
  labels:
    app: customers-service
    version: "3.4.1"
    component: backend
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: customers-service
  template:
    metadata:
      labels:
        app: customers-service
        version: "3.4.1"
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/path: "/actuator/prometheus"
        prometheus.io/port: "8081"
    spec:
      serviceAccountName: customers-service
      containers:
      - name: customers-service
        # Note: Replace with actual values or use Kustomize/Helm for templating
        # Example: myregistry.azurecr.io/customers-service:1.0.0
        image: <ACR_NAME>.azurecr.io/customers-service:<VERSION>
        imagePullPolicy: Always
        ports:
        - name: http
          containerPort: 8081
          protocol: TCP
        - name: management
          containerPort: 8081
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "azure,production"
        - name: SERVER_PORT
          value: "8081"
        - name: SPRING_APPLICATION_NAME
          value: "customers-service"
        # Azure App Configuration
        - name: SPRING_CLOUD_AZURE_APPCONFIGURATION_STORES_0_CONNECTION_STRING
          valueFrom:
            secretKeyRef:
              name: azure-appconfig-secret
              key: connection-string
        # Database configuration
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            configMapKeyRef:
              name: customers-config
              key: database.url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: mysql-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-credentials
              key: password
        # JVM Options
        - name: JAVA_OPTS
          value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Xlog:gc*:file=/tmp/gc.log"
        # Application Insights
        - name: APPLICATIONINSIGHTS_CONNECTION_STRING
          valueFrom:
            secretKeyRef:
              name: appinsights-secret
              key: connection-string
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
            scheme: HTTP
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 3
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
            scheme: HTTP
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        - name: logs
          mountPath: /app/logs
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
      volumes:
      - name: tmp
        emptyDir: {}
      - name: logs
        emptyDir: {}
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - customers-service
              topologyKey: kubernetes.io/hostname
```

### 2.3 service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: customers-service
  labels:
    app: customers-service
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: http
    protocol: TCP
    name: http
  - port: 8081
    targetPort: management
    protocol: TCP
    name: management
  selector:
    app: customers-service
```

### 2.4 configmap.yaml

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: customers-config
data:
  application.yml: |
    spring:
      application:
        name: customers-service
      jpa:
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            format_sql: false
            use_sql_comments: false
    
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: when-authorized
          probes:
            enabled: true
      metrics:
        export:
          prometheus:
            enabled: true
    
    logging:
      level:
        root: INFO
        org.springframework.samples.petclinic: DEBUG
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  
  database.url: "jdbc:mysql://${MYSQL_HOST:mysql-server.mysql.database.azure.com}:3306/${MYSQL_DATABASE:petclinic}?useSSL=true&requireSSL=true"
```

### 2.5 hpa.yaml

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: customers-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: customers-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 30
      - type: Pods
        value: 2
        periodSeconds: 30
      selectPolicy: Max
```

### 2.6 ingress.yaml

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: customers-service-ingress
  annotations:
    appgw.ingress.kubernetes.io/ssl-redirect: "true"
    appgw.ingress.kubernetes.io/backend-protocol: "http"
    appgw.ingress.kubernetes.io/health-probe-path: "/actuator/health"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: azure-application-gateway
  tls:
  - hosts:
    - api.petclinic.example.com
    secretName: petclinic-tls
  rules:
  - host: api.petclinic.example.com
    http:
      paths:
      - path: /customers
        pathType: Prefix
        backend:
          service:
            name: customers-service
            port:
              number: 8081
```

---

## 3. Azure Configuration

### 3.1 application-azure.yml

Create `src/main/resources/application-azure.yml`:

```yaml
spring:
  cloud:
    azure:
      credential:
        managed-identity-enabled: true
      keyvault:
        secret:
          property-source-enabled: true
          endpoint: ${AZURE_KEYVAULT_ENDPOINT}
      appconfiguration:
        stores:
          - connection-string: ${AZURE_APPCONFIG_CONNECTION_STRING}
            monitoring:
              enabled: true
              refresh-interval: 60s
  
  datasource:
    url: ${MYSQL_URL}
    username: ${MYSQL_USERNAME}
    password: ${MYSQL_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:production}

logging:
  level:
    root: INFO
    org.springframework.samples.petclinic: DEBUG
    com.azure: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 3.2 Azure Resources Terraform

Create `terraform/main.tf`:

```hcl
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

# Resource Group
resource "azurerm_resource_group" "petclinic" {
  name     = "petclinic-rg"
  location = "East US"
}

# AKS Cluster
resource "azurerm_kubernetes_cluster" "petclinic" {
  name                = "petclinic-aks"
  location            = azurerm_resource_group.petclinic.location
  resource_group_name = azurerm_resource_group.petclinic.name
  dns_prefix          = "petclinic"

  default_node_pool {
    name       = "default"
    node_count = 3
    vm_size    = "Standard_D2s_v3"
    enable_auto_scaling = true
    min_count  = 3
    max_count  = 10
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin = "azure"
    network_policy = "calico"
  }

  oms_agent {
    log_analytics_workspace_id = azurerm_log_analytics_workspace.petclinic.id
  }
}

# Container Registry
resource "azurerm_container_registry" "petclinic" {
  name                = "petclinicacr"
  resource_group_name = azurerm_resource_group.petclinic.name
  location            = azurerm_resource_group.petclinic.location
  sku                 = "Standard"
  admin_enabled       = false
}

# MySQL Flexible Server
resource "azurerm_mysql_flexible_server" "petclinic" {
  name                = "petclinic-mysql"
  resource_group_name = azurerm_resource_group.petclinic.name
  location            = azurerm_resource_group.petclinic.location
  
  administrator_login    = "petclinic_admin"
  administrator_password = var.mysql_admin_password
  
  sku_name   = "GP_Standard_D2ds_v4"
  version    = "8.0.21"
  
  backup_retention_days        = 7
  geo_redundant_backup_enabled = true
  
  storage {
    size_gb = 20
    auto_grow_enabled = true
  }
}

# Key Vault
resource "azurerm_key_vault" "petclinic" {
  name                = "petclinic-kv"
  location            = azurerm_resource_group.petclinic.location
  resource_group_name = azurerm_resource_group.petclinic.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  sku_name            = "standard"
}

# Application Insights
resource "azurerm_application_insights" "petclinic" {
  name                = "petclinic-appinsights"
  location            = azurerm_resource_group.petclinic.location
  resource_group_name = azurerm_resource_group.petclinic.name
  application_type    = "java"
  workspace_id        = azurerm_log_analytics_workspace.petclinic.id
}

# Log Analytics Workspace
resource "azurerm_log_analytics_workspace" "petclinic" {
  name                = "petclinic-logs"
  location            = azurerm_resource_group.petclinic.location
  resource_group_name = azurerm_resource_group.petclinic.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
}
```

---

## 4. Security Implementation

### 4.1 Add Spring Security

Update `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

### 4.2 Security Configuration

Create `src/main/java/org/springframework/samples/petclinic/customers/config/SecurityConfig.java`:

```java
package org.springframework.samples.petclinic.customers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> 
                oauth2.jwt(jwt -> jwt.jwkSetUri("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")));
        
        return http.build();
    }
}
```

---

## 5. CI/CD Pipeline

### 5.1 GitHub Actions Workflow

Create `.github/workflows/azure-deploy.yml`:

```yaml
name: Build and Deploy to AKS

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

env:
  AZURE_RESOURCE_GROUP: petclinic-rg
  AKS_CLUSTER_NAME: petclinic-aks
  ACR_NAME: petclinicacr
  IMAGE_NAME: customers-service

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: maven
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Run tests
      run: mvn test
    
    - name: Azure Login
      uses: azure/login@v1
      with:
        creds: ${{ secrets.AZURE_CREDENTIALS }}
    
    - name: Build and push Docker image
      run: |
        az acr login --name ${{ env.ACR_NAME }}
        docker buildx build --push \
          -t ${{ env.ACR_NAME }}.azurecr.io/${{ env.IMAGE_NAME }}:${{ github.sha }} \
          -t ${{ env.ACR_NAME }}.azurecr.io/${{ env.IMAGE_NAME }}:latest \
          --platform linux/amd64 .
    
    - name: Set AKS context
      uses: azure/aks-set-context@v3
      with:
        resource-group: ${{ env.AZURE_RESOURCE_GROUP }}
        cluster-name: ${{ env.AKS_CLUSTER_NAME }}
    
    - name: Deploy to AKS
      uses: azure/k8s-deploy@v4
      with:
        manifests: |
          k8s/base/deployment.yaml
          k8s/base/service.yaml
          k8s/base/hpa.yaml
        images: |
          ${{ env.ACR_NAME }}.azurecr.io/${{ env.IMAGE_NAME }}:${{ github.sha }}
        imagepullsecrets: |
          acr-secret
```

---

## 6. Monitoring Setup

### 6.1 Application Insights Integration

Add to `pom.xml`:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>applicationinsights-spring-boot-starter</artifactId>
    <version>3.4.19</version>
</dependency>
```

### 6.2 Custom Metrics

Create `src/main/java/org/springframework/samples/petclinic/customers/config/MetricsConfig.java`:

```java
package org.springframework.samples.petclinic.customers.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                Tags.of("application", "customers-service")
                    .and("environment", System.getenv().getOrDefault("ENVIRONMENT", "development"))
            );
    }
}
```

### 6.3 Azure Monitor Queries

```kusto
// Average response time
requests
| where timestamp > ago(1h)
| where cloud_RoleName == "customers-service"
| summarize avg(duration) by bin(timestamp, 5m)
| render timechart

// Error rate
requests
| where timestamp > ago(1h)
| where cloud_RoleName == "customers-service"
| summarize 
    total = count(),
    errors = countif(success == false)
  by bin(timestamp, 5m)
| extend errorRate = (errors * 100.0) / total
| render timechart

// Top 10 slowest requests
requests
| where timestamp > ago(1h)
| where cloud_RoleName == "customers-service"
| top 10 by duration desc
| project timestamp, name, url, duration, resultCode
```

---

## Implementation Checklist

### Pre-Migration
- [ ] Review and approve assessment report
- [ ] Setup Azure subscription and billing
- [ ] Create Azure AD app registrations
- [ ] Provision base infrastructure (Terraform)
- [ ] Setup CI/CD pipeline

### Application Preparation
- [ ] Create Dockerfile
- [ ] Add Azure-specific configuration
- [ ] Implement security layer
- [ ] Add Application Insights
- [ ] Update health checks

### Infrastructure Setup
- [ ] Deploy AKS cluster
- [ ] Configure networking
- [ ] Setup ACR
- [ ] Provision MySQL
- [ ] Configure Key Vault

### Deployment
- [ ] Build and push Docker image
- [ ] Deploy Kubernetes manifests
- [ ] Configure ingress
- [ ] Setup monitoring
- [ ] Validate deployment

### Post-Deployment
- [ ] Load testing
- [ ] Security scanning
- [ ] Documentation
- [ ] Team training
- [ ] Production cutover plan

---

**Document Version:** 1.0  
**Last Updated:** December 11, 2024  
**Status:** Ready for Implementation
