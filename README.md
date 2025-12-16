# Spring PetClinic Microservices - Custom Service

This repository contains a custom service for the Spring PetClinic Microservices application.

## Application Assessment

This repository includes automated application assessment using AppCAT (Application Containerization Assessment Tool). The assessment helps identify migration considerations for deploying to Azure services.

### Running the Assessment Workflow

The assessment workflow can be triggered in multiple ways:

#### 1. Using GitHub Web UI (Easiest)
1. Go to the [Actions tab](https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions)
2. Select "Application Assessment" workflow
3. Click "Run workflow" button
4. Confirm and run

#### 2. Using the Included Script
```bash
./trigger-assessment.sh
```

#### 3. Automatic Triggering
The assessment workflow will automatically run when:
- Changes are pushed to `main` branch that affect `.github/workflows/report.json`
- The auto-trigger workflow is manually dispatched

See [TRIGGER_ASSESSMENT.md](./TRIGGER_ASSESSMENT.md) for detailed instructions.

### Assessment Report

The assessment report is stored at `.github/workflows/report.json` and contains detailed analysis of the application for migration to Azure services including:
- Azure Kubernetes Service (AKS)
- Azure Container Apps
- Azure App Service

When the assessment workflow runs, it:
1. Validates the report file
2. Copies the report to the target repository (`zhoufenqin/spring-petclinic-microservices`)
3. Updates the issue tracker with assessment summary

## Workflow Status

View the latest workflow runs:
- [Application Assessment Workflow](https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/appcat-analysis.yml)
- [Auto-trigger Workflow](https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/auto-trigger-assessment.yml)
