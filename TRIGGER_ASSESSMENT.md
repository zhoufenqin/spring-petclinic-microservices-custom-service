# How to Trigger the Assessment Workflow

This document explains how to run the `appcat-analysis.yml` workflow to update the assessment report.

## Workflow Overview

The workflow (`/.github/workflows/appcat-analysis.yml`) performs the following actions:
1. Verifies the assessment report exists at `.github/workflows/report.json`
2. Clones the target repository (`zhoufenqin/spring-petclinic-microservices`)
3. Copies the report to `app-modernization/spring-petclinic-microservices-custom-service-report.json`
4. Commits and pushes the report
5. Updates issue #8 in the target repository with the assessment summary

## Triggering the Workflow

### Option 1: GitHub Web UI (Recommended)

1. Navigate to the [Actions tab](https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions)
2. Click on "Application Assessment" workflow in the left sidebar
3. Click the "Run workflow" button (top right)
4. Select the branch (default: `main`)
5. (Optional) Modify the issue URL if needed (default: `https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8`)
6. Click "Run workflow"

### Option 2: GitHub CLI

If you have the GitHub CLI installed and authenticated:

```bash
gh workflow run appcat-analysis.yml \
  --repo zhoufenqin/spring-petclinic-microservices-custom-service \
  --ref main \
  --field issue_url="https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8"
```

### Option 3: GitHub API

Using curl with a Personal Access Token (PAT):

```bash
curl -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer YOUR_PAT_TOKEN" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/repos/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/appcat-analysis.yml/dispatches \
  -d '{"ref":"main","inputs":{"issue_url":"https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8"}}'
```

## Workflow Inputs

- **issue_url** (optional): The GitHub issue URL where the assessment summary will be posted
  - Default: `https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8`
  - Format: `https://github.com/OWNER/REPO/issues/NUMBER`

## Prerequisites

- The assessment report file must exist at `.github/workflows/report.json`
- The `PAT_TOKEN` secret must be configured with permissions to:
  - Clone the target repository
  - Push changes to the target repository
  - Create comments on issues in the target repository

## Workflow Status

View the status of workflow runs at:
https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/appcat-analysis.yml

## Recent Runs

The workflow has been successfully run multiple times:
- Run #8: December 16, 2025 at 08:14:47 UTC - ✅ Success
- Run #7: December 15, 2025 at 06:29:45 UTC - ✅ Success

## Troubleshooting

If the workflow fails, check:
1. The report.json file exists and is valid JSON
2. The PAT_TOKEN secret is properly configured
3. The target repository exists and is accessible
4. The target issue exists and is accessible
