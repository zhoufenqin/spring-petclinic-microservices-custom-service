# How to Update the Cloud Modernization Assessment Report

## Overview
This guide explains how to trigger the GitHub Actions workflow that will update the assessment report on the target issue.

## Prerequisites
✅ Assessment report exists: `.github/workflows/report.json`  
✅ Workflow is configured: `.github/workflows/appcat-analysis.yml`  
✅ Most recent workflow run was successful (Run #8, completed at 2025-12-16 08:14:56 UTC)

## Steps to Update the Assessment Report

### Method 1: Using GitHub UI (Recommended)

1. **Navigate to GitHub Actions**
   - Go to: https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions

2. **Select the Workflow**
   - Find and click on "Application Assessment" in the workflow list

3. **Trigger the Workflow**
   - Click the "Run workflow" button (dropdown on the right side)
   - Select the branch: `copilot/assess-application-cloud-modernization` (or `main`)
   - (Optional) Enter a custom issue URL, or leave the default: 
     ```
     https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8
     ```
   - Click "Run workflow" to start

4. **Monitor Progress**
   - Watch the workflow run in real-time
   - The workflow typically completes in 10-30 seconds

5. **Verify Results**
   - Check the target issue for the updated assessment report
   - The report will be posted as a comment with the marker: `<!--assessment-report-overview-->`

### Method 2: Using GitHub CLI (gh)

If you have the GitHub CLI installed and authenticated:

```bash
gh workflow run "Application Assessment" \
  --repo zhoufenqin/spring-petclinic-microservices-custom-service \
  --ref copilot/assess-application-cloud-modernization \
  --field issue_url="https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8"
```

### Method 3: Using GitHub API

```bash
curl -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer YOUR_GITHUB_TOKEN" \
  https://api.github.com/repos/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/appcat-analysis.yml/dispatches \
  -d '{
    "ref": "copilot/assess-application-cloud-modernization",
    "inputs": {
      "issue_url": "https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8"
    }
  }'
```

## What the Workflow Does

1. **Verify Report**: Checks that the assessment report exists at `.github/workflows/report.json`
2. **Clone Target Repo**: Clones the repository where the issue is located
3. **Copy Report**: Copies the report to `app-modernization/spring-petclinic-microservices-custom-service-report.json`
4. **Commit Changes**: Commits and pushes the report to the target repository
5. **Update Issue**: Posts or updates a comment on the specified GitHub issue with:
   - Assessment summary
   - Overall statistics
   - Application profile
   - Key findings organized by severity
   - Links to resources and documentation

## Expected Results

After the workflow completes successfully, you will see:

1. ✅ A new commit in the target repository with the assessment report
2. ✅ A comment on the specified issue with the assessment summary
3. ✅ The comment will include:
   - Total issue count: 61
   - Total incidents: 3,698
   - Severity breakdown (528 Mandatory, 39 Potential, 3,074 Optional)
   - Category breakdown (Remote Communication, Database Migration, etc.)

## Troubleshooting

### Permission Issues
If you see a 403 error, ensure:
- You have write access to the repository
- The `GITHUB_TOKEN` or `PAT_TOKEN` has appropriate permissions
- The workflow has `contents: write` and `issues: write` permissions

### Report Not Found
If the workflow fails with "Report file not found":
- Verify the report exists at `.github/workflows/report.json`
- Check that you're running the workflow from the correct branch

### Issue Not Updated
If the issue doesn't show the update:
- Verify the issue URL is correct
- Check the workflow logs for any errors
- Ensure the token has `issues: write` permission on the target repository

## Additional Resources

- **Assessment Status**: See `ASSESSMENT_STATUS.md` for detailed findings
- **Workflow File**: `.github/workflows/appcat-analysis.yml`
- **Report File**: `.github/workflows/report.json`
- **GitHub Copilot App Modernization**: https://aka.ms/ghcp-appmod
- **Workflow Runs**: View all workflow runs in the [Actions tab](https://github.com/zhoufenqin/spring-petclinic-microservices-custom-service/actions/workflows/appcat-analysis.yml)

## Support

For issues or questions:
1. Check the workflow logs for detailed error messages
2. Review the assessment report at `.github/workflows/report.json`
3. Consult the [GitHub Copilot App Modernization documentation](https://aka.ms/ghcp-appmod)
