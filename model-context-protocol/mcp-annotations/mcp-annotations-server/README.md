# Spring AI MCP Annotations Server

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Demonstrates how to build an MCP server using Spring AI's annotation-driven model. Covers tools, resources, prompts, and completions with a declarative, annotation-based approach.

**Dependencies:** Spring Boot `4.0.6`, Spring AI `2.0.0`

Reference: [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)

## Capabilities

### Tools

| Provider | Annotation | Tools |
|----------|-----------|-------|
| `SpringAiToolProvider` | `@Tool` | `getWeatherForecastByLocation`, `getAlerts` |
| `McpToolProvider` | `@McpTool` | `getTemperature` (with progress token) |
| `DocumentProvider` | `@McpTool` | `read_doc_contents`, `edit_document` |
| `ToolProvider2` | `@McpTool` | `toolLggingSamplingElicitationProgress` (logging, sampling, elicitation, progress) |

### Resources

| URI | Description |
|-----|-------------|
| `user-profile://{username}` | User profile info |
| `user-attribute://{username}/{attribute}` | Specific profile attribute |
| `user-profile-exchange://{username}` | Profile with server exchange context |
| `user-connections://{username}` | User connections list |
| `user-notifications://{username}` | User notifications |
| `user-status://{username}` | User status |
| `user-location://{username}` | User location |
| `user-avatar://{username}` | Base64-encoded avatar (image/png) |
| `docs://documents` | List all documents (application/json) |
| `docs://documents/{docId}` | Fetch document content |
| `static://hello` | Static "Hello World!" resource |

### Prompts

`greeting`, `personalized-message`, `conversation-starter`, `map-arguments`, `single-message`, `string-list`, `format` (Markdown reformat via `DocumentProvider`)

### Completions

`@McpComplete` on `user-status://{username}` (username suggestions), `personalized-message` prompt (name suggestions), `travel-planner` prompt (country names).

## Build

```bash
./mvnw clean install -DskipTests
```

## Run

### WebMVC (SSE / Streamable-HTTP / Stateless)

```bash
# Streamable-HTTP (default in application.properties)
java -Dspring.ai.mcp.server.protocol=STREAMABLE -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar

# SSE
java -Dspring.ai.mcp.server.protocol=SSE -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

### STDIO

```bash
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

## Configuration

```properties
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.protocol=STREAMABLE   # SSE | STREAMABLE | STATELESS

# STDIO transport (uncomment to enable)
# spring.ai.mcp.server.stdio=true
# spring.main.web-application-type=none
# logging.pattern.console=
```

## Connecting a Client

See the companion `mcp-annotations-client` module. Configure the transport in `application.properties`:

```properties
# Streamable-HTTP
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080

# SSE (alternative)
# spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080
```

For STDIO, use a `mcp-servers-config.json`:

```json
{
  "mcpServers": {
    "annotations-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar", "/absolute/path/to/mcp-annotations-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## Additional Resources

- [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [MCP Annotations Project](https://github.com/spring-ai-community/mcp-annotations)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
