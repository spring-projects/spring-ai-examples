# GitHub MCP Server — Java / Spring AI

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?style=flat&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0.0--M3-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-ai)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Lombok](https://img.shields.io/badge/Lombok-red?style=flat&logo=lombok&logoColor=white)](https://projectlombok.org/)

A production-ready MCP server that connects any MCP-compatible AI agent to the GitHub API.
Manage repositories, issues, pull requests, and search — all through natural language.

> **Transport:** HTTP/SSE on port 8080, compatible with Cursor and Claude Desktop out of the box.

---

## Why this server?

| Capability | This server | GitHub REST API |
|---|---|---|
| Repository management | ✅ | ✅ |
| Issue & PR operations | ✅ | ✅ |
| Branch & commit control | ✅ | ✅ |
| Code & user search | ✅ | ✅ |
| Natural language interface | ✅ | ❌ |
| MCP protocol (SSE) | ✅ | ❌ |
| Java / Spring AI | ✅ | ❌ |
| GitHub Enterprise support | ✅ | ✅ |

---

## Tools (38 total)

| Category | Count | Tools |
|---|---|---|
| Repository | 11 | `create_repository`, `fork_repository`, `get_repository`, `list_commits`, `get_commit`, `get_file_contents`, `create_or_update_file`, `delete_file`, `create_branch`, `list_branches`, `merge_branch` |
| Issues | 11 | `create_issue`, `update_issue`, `add_issue_comment`, `list_issues`, `get_issue`, `close_issue`, `reopen_issue`, `assign_issue`, `unassign_issue`, `add_issue_labels`, `remove_issue_label` |
| Pull Requests | 10 | `create_pull_request`, `update_pull_request`, `list_pull_requests`, `get_pull_request`, `merge_pull_request`, `close_pull_request`, `reopen_pull_request`, `add_pull_request_comment`, `create_pull_request_review`, `submit_pull_request_review` |
| Search | 4 | `search_code`, `search_issues`, `search_repositories`, `search_users` |
| Users | 3 | `get_authenticated_user`, `get_user`, `list_user_repositories` |

---

## Quick start

```bash
# 1. Build
mvn clean package

# 2. Set credentials
export GITHUB_PERSONAL_ACCESS_TOKEN=ghp_...

# 3. Run (SSE transport — port 8080)
java -jar target/github-mcp-server-1.0.0.jar

# 4. Verify
curl http://localhost:8080/health

# 5. Inspect all tools
npx @modelcontextprotocol/inspector http://localhost:8080/mcp
```

Get your token from [GitHub Settings](https://github.com/settings/tokens) → Developer settings → Personal access tokens.
For GitHub Enterprise, set `GITHUB_HOST` to your instance URL.

---

## Architecture

```
MCP Client (Cursor / Claude Desktop / other)
    │   HTTP/SSE transport (/mcp + /mcp/message)
    ▼
Tool class  (@McpTool — thin delegation layer, validates required params)
    ▼
Service interface + impl  (business logic, error mapping, pagination)
    ▼
GitHubRestClient  (typed HTTP gateway, PAT auth, exception handling)
    ▼
GitHub REST API
```

The architecture is strictly layered:

- `client/` — GitHub integration boundary (HTTP, Bearer-Auth, error handling)
- `service/` — domain logic (filtering, mapping, pagination)
- `tools/` — MCP-facing surface (descriptions, param validation, delegation)
- Spring Boot — runtime and transport wrapper only

Every tool returns a consistent `ApiResponse<T>` envelope:

```json
{ "success": true,  "data": { ... } }
{ "success": false, "errorCode": "REPO_NOT_FOUND", "errorMessage": "..." }
```

---

## Configuration

| Property | Env var | Required | Default | Description |
|---|---|---|---|---|
| — | `GITHUB_PERSONAL_ACCESS_TOKEN` | ✅ | — | GitHub Personal Access Token |
| — | `GITHUB_HOST` | ❌ | `github.com` | GitHub hostname (for GitHub Enterprise) |
| — | `GITHUB_READ_ONLY` | ❌ | `false` | Restrict to read-only operations |
| — | `GITHUB_TOOLSETS` | ❌ | all | Comma-separated list of toolsets to enable |
| — | `GITHUB_TOOLS` | ❌ | all | Comma-separated list of specific tools to enable |
| — | `GITHUB_EXCLUDE_TOOLS` | ❌ | none | Comma-separated list of tools to exclude |

### Creating a GitHub Personal Access Token

1. Go to **GitHub Settings → Developer settings → Personal access tokens**
2. Click **"Generate new token (classic)"**
3. Select the following scopes:
    - `repo` — Full control of private repositories
    - `workflow` — Update GitHub Action workflows
    - `read:org` — Read org and team membership
    - `gist` — Create gists
    - `notifications` — Access notifications
    - `read:user` — Read user profile data
4. Generate and copy the token

---

## Client config

### Cursor (`.cursor/mcp.json`)

```json
{
  "mcpServers": {
    "github": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

### Claude Desktop (`claude_desktop_config.json`)

```json
{
  "mcpServers": {
    "github": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

On macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`

### VS Code / GitHub Copilot

**URL mode** (if your client supports it):

```json
{
  "github.copilot.chat.mcp.servers": {
    "github": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```

**Command mode** (stdio-only clients):

```json
{
  "github.copilot.chat.mcp.servers": {
    "github": {
      "command": "java",
      "args": ["-jar", "/path/to/github-mcp-server-1.0.0.jar"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "ghp_..."
      }
    }
  }
}
```

---

## Docker

```bash
# Build image
docker build -t github-mcp-server:latest .

# Run
docker run --rm -p 8080:8080 \
  -e GITHUB_PERSONAL_ACCESS_TOKEN=ghp_... \
  github-mcp-server:latest
```

If GitHub Enterprise runs in Docker on the same host, use `host.docker.internal`:

```bash
-e GITHUB_HOST=http://host.docker.internal:3000
```

---

## Running tests

```bash
mvn test
```

---

## Package structure

```
com.github.mcp
├── GithubMcpApplication.java              @SpringBootApplication
├── config/
│   ├── GitHubProperties.java              @ConfigurationProperties — token, host, readOnly, toolsets
│   └── WebConfig.java                     Web configuration
├── client/
│   ├── GitHubRestClient.java              GET/POST/PATCH/DELETE HTTP gateway; typed exceptions
│   └── GitHubGraphqlClient.java           GraphQL client
├── controller/
│   └── HealthController.java              Health check endpoint (GET /health)
├── exception/
│   ├── GitHubMcpException.java            Base exception
│   └── GlobalExceptionHandler.java        Global error handler
├── dto/
│   ├── common/   ApiResponse · PagedResponse
│   ├── request/  *Request DTOs
│   └── response/ *Response DTOs
├── service/      Interfaces + impl — business logic, error mapping, pagination
└── tools/
    ├── RepositoryTools.java   (11 tools)
    ├── IssueTools.java        (11 tools)
    ├── PullRequestTools.java  (10 tools)
    ├── SearchTools.java       (4 tools)
    └── UserTools.java         (3 tools)
```

---

## Troubleshooting

### `401 Unauthorized`

Token issue. Check:

1. `GITHUB_PERSONAL_ACCESS_TOKEN` is set correctly
2. The token has not expired
3. The token has the required scopes (see [Configuration](#configuration))
4. For GitHub Enterprise: confirm `GITHUB_HOST` is set to your instance URL

### `REPO_NOT_FOUND` / `404 Not Found`

The repository may be private and the token lacks `repo` scope, or the owner/name is incorrect.

### Connection timeouts

The server connects to `api.github.com` (or your `GITHUB_HOST`). Ensure the JVM process has outbound network access.

### Copilot / Claude can't see the server

1. Confirm the server is running: `curl http://localhost:8080/health`
2. Confirm the MCP SSE endpoint is alive: `curl http://localhost:8080/mcp`
3. Check that the URL in the client config points to `http://localhost:8080/mcp`

---

## Acknowledgments

This project is a Java / Spring AI port of the official [GitHub MCP Server](https://github.com/github/github-mcp-server) written in Go.

---
