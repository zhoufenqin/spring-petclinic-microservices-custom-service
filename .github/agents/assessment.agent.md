---
name: Assessment
description: App modernization assessment agent
tools: ["*"]
mcp-servers:
  Assessment:
    type: local
    command: npx
    args:
      - "-y"
      - "-p"
      - "@microsoft/github-copilot-app-modernization-mcp-server"
      - "github-copilot-app-modernization-mcp-server"
      - "--loglevel"
      - "verbose"
      - "--callerType"
      - "modernize-cli"
    tools: ["*"]
---

This is the assessment agent
