#!/bin/bash

# Script to trigger the Application Assessment workflow
# This script requires GitHub CLI (gh) to be installed and authenticated
# Minimum required version: gh 2.0.0

set -euo pipefail

REPO="zhoufenqin/spring-petclinic-microservices-custom-service"
WORKFLOW="appcat-analysis.yml"
REF="main"
ISSUE_URL="${1:-https://github.com/zhoufenqin/spring-petclinic-microservices/issues/8}"

echo "============================================"
echo "Triggering Application Assessment Workflow"
echo "============================================"
echo ""
echo "Repository: $REPO"
echo "Workflow: $WORKFLOW"
echo "Branch: $REF"
echo "Issue URL: $ISSUE_URL"
echo ""

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo "❌ Error: GitHub CLI (gh) is not installed."
    echo "Please install it from: https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo "❌ Error: Not authenticated with GitHub CLI."
    echo "Please run: gh auth login"
    exit 1
fi

echo "✅ GitHub CLI is installed and authenticated"
echo ""
echo "Triggering workflow..."
echo ""

# Trigger the workflow
if gh workflow run "$WORKFLOW" \
    --repo "$REPO" \
    --ref "$REF" \
    --field issue_url="$ISSUE_URL"; then
    echo ""
    echo "✅ Workflow triggered successfully!"
    echo ""
    echo "View the workflow run at:"
    echo "https://github.com/$REPO/actions/workflows/$WORKFLOW"
    echo ""
    echo "Check the status with:"
    echo "gh run list --repo $REPO --workflow=$WORKFLOW --limit 5"
else
    echo ""
    echo "❌ Failed to trigger workflow"
    exit 1
fi
