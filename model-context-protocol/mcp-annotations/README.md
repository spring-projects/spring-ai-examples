# Spring AI MCP Annotations Examples

Demonstrates annotation-driven MCP development with Spring AI. Covers server-side tools, resources, prompts, and completions, plus client-side notification handlers — all using declarative annotations.

**Versions:** Spring Boot `4.0.6` · Spring AI `2.0.0`

## Modules

### [mcp-annotations-server](mcp-annotations-server/)

MCP server with four provider beans:

| Provider | Annotation style | Capabilities |
|----------|-----------------|-------------|
| `SpringAiToolProvider` | `@Tool` | Weather forecast & alerts (weather.gov) |
| `McpToolProvider` | `@McpTool` | Temperature lookup with progress token (open-meteo.com) |
| `DocumentProvider` | `@McpTool` / `@McpResource` / `@McpPrompt` | Read/edit docs from local `docs/` dir; list & fetch doc resources; Markdown `format` prompt |
| `ToolProvider2` | `@McpTool` | Composite tool exercising logging, progress, sampling & elicitation |
| `UserProfileResourceProvider` | `@McpResource` | User profile, attributes, status, connections, notifications, location, avatar |
| `PromptProvider` | `@McpPrompt` | `greeting`, `personalized-message`, `conversation-starter`, `map-arguments`, `single-message`, `string-list` |
| `AutocompleteProvider` | `@McpComplete` | Username, name, and country completions |

Dependency: `spring-ai-starter-mcp-server-webmvc`

### [mcp-annotations-client](mcp-annotations-client/)

MCP client that connects to the server and demonstrates client-side handlers:

| Annotation | Handler |
|------------|---------|
| `@McpProgress` | Logs progress notifications |
| `@McpLogging` | Logs server log messages |
| `@McpSampling` | Delegates LLM requests back to a model |
| `@McpElicitation` | Handles structured elicitation requests (`StructuredElicitResult<Person>`) |

The `CommandLineRunner` exercises tool calls, completions, prompt retrieval, and resource reads using the builder APIs:

```java
CallToolRequest.builder("tool1").arguments(...).progressToken(666).build();
CompleteRequest.builder(new PromptReference("personalized-message"), new CompleteArgument("name", "J")).build();
GetPromptRequest.builder("personalized-message").arguments(Map.of("name", nameValue)).build();
ReadResourceRequest.builder("user-status://alice").build();
```

Dependency: `spring-ai-starter-mcp-client`

## Quick Start

**1. Build and start the server**

```bash
cd mcp-annotations-server
./mvnw clean package -DskipTests
java -Dspring.ai.mcp.server.protocol=STREAMABLE -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

**2. Run the client**

```bash
cd mcp-annotations-client
./mvnw clean package -DskipTests
java -Dspring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080 \
     -jar target/mcp-annotations-client-0.0.1-SNAPSHOT.jar
```

> For SSE replace `STREAMABLE` / `streamable-http` with `SSE` / `sse`.  
> For STDIO see the server README.

## Additional Resources

- [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [MCP Annotations Project](https://github.com/spring-ai-community/mcp-annotations)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
