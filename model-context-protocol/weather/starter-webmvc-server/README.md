# Spring AI MCP Weather Server Sample with WebMVC Starter

Demonstrates an MCP server using the Spring AI MCP Server Boot Starter with WebMVC transport, exposing weather tools via the National Weather Service API.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Overview

- Integration with `spring-ai-starter-mcp-server-webmvc`
- SSE, Streamable HTTP, and STDIO transport support
- Automatic tool registration via `@McpTool` annotation (no manual bean wiring required)
- Two weather tools: forecast by lat/lon and alerts by US state

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

## Building

```bash
./mvnw clean install -DskipTests
```

## Running the Server

### WebMVC SSE / Streamable HTTP (Default)
```bash
java -jar target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar
```
Server starts on port 8080.

### STDIO Mode
```bash
java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none \
  -jar target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar
```

## Configuration

```properties
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.stdio=false
spring.ai.mcp.server.sse-message-endpoint=/mcp/message

# Required for STDIO transport
spring.main.banner-mode=off
logging.file.name=./target/starter-webmvc-server.log
```

## Tool Registration

Tools are registered automatically via `@McpTool` — no `ToolCallbackProvider` bean needed:

```java
@Service
public class WeatherService {

    @McpTool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) { ... }

    @McpTool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
    public String getAlerts(String state) { ... }
}
```

## Available Tools

| Tool | Parameters | Description |
|------|------------|-------------|
| `getWeatherForecastByLocation` | `latitude: double`, `longitude: double` | Weather forecast for a location |
| `getAlerts` | `state: String` | Active weather alerts for a US state (e.g. `NY`) |

## MCP Clients

The sample includes three client implementations:

- [SampleClient.java](src/main/java/org/springframework/ai/mcp/sample/client/SampleClient.java) — SSE transport
- [ClientStdio.java](src/main/java/org/springframework/ai/mcp/sample/client/ClientStdio.java) — STDIO transport
- [StreamableHttpClient.java](src/main/java/org/springframework/ai/mcp/sample/client/StreamableHttpClient.java) — Streamable HTTP transport

### SSE Client
```java
var transport = HttpClientSseClientTransport.builder("http://localhost:8080").build();
var client = McpClient.sync(transport).build();
```

### Streamable HTTP Client
```java
var transport = HttpClientStreamableHttpTransport.builder("http://localhost:8080").build();
var client = McpClient.sync(transport).build();
```

### STDIO Client
```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.stdio=true",
          "-Dspring.main.web-application-type=none",
          "-Dlogging.pattern.console=",
          "-jar", "target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar")
    .build();
var client = McpClient.sync(new StdioClientTransport(stdioParams)).build();
```

## Using the Boot Starter Client

Build the [starter-default-client](../../client-starter/starter-default-client) and connect via STDIO or SSE.

### STDIO Transport

`mcp-servers-config.json`:
```json
{
  "mcpServers": {
    "weather-starter-webmvc-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar", "/absolute/path/to/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

```bash
java -Dspring.ai.mcp.client.stdio.servers-configuration=file:mcp-servers-config.json \
     -Dai.user.input='What is the weather in NY?' \
     -Dlogging.pattern.console= \
     -jar mcp-starter-default-client-0.0.1-SNAPSHOT.jar
```

### SSE Transport

```bash
java -Dspring.ai.mcp.client.sse.connections.weather-server.url=http://localhost:8080 \
     -Dai.user.input='What is the weather in NY?' \
     -Dlogging.pattern.console= \
     -jar mcp-starter-default-client-0.0.1-SNAPSHOT.jar
```

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
