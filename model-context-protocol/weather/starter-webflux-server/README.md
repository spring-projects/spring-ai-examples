# Spring AI MCP Weather Server — WebFlux Starter

MCP server built with the Spring AI WebFlux starter. Exposes weather tools via [Streamable HTTP](https://modelcontextprotocol.io/specification/2025-03-26/basic/transports#streamable-http) (default), SSE, and STDIO transports.

Reference docs: [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)

## Dependency

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webflux</artifactId>
</dependency>
```

## Build

```bash
./mvnw clean install -DskipTests
```

## Run

### Streamable HTTP (default, port 8080)

```bash
java -jar target/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar
```

The default configuration exposes Streamable HTTP at `/mcp`.

### SSE

Select SSE before using an SSE client:

```bash
java -Dspring.ai.mcp.server.protocol=SSE \
     -jar target/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar
```

SSE clients connect to `/sse` in this mode.

### STDIO

```bash
java -Dspring.ai.mcp.server.stdio=true \
     -Dspring.main.web-application-type=none \
     -Dspring.main.banner-mode=off \
     -Dlogging.pattern.console= \
     -jar target/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar
```

## Configuration

```properties
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.protocol=STREAMABLE

# Required when using STDIO transport
# spring.main.banner-mode=off
# logging.pattern.console=
```

## Tools

| Tool | Description |
|------|-------------|
| `getWeatherForecastByLocation` | Forecast for a latitude/longitude (weather.gov) |
| `getAlerts` | Active alerts for a two-letter US state code (e.g. `NY`) |
| `toUpperCase` | Converts input text to upper case |

Weather tools are registered via `@McpTool` on [WeatherService](src/main/java/org/springframework/ai/mcp/sample/server/WeatherService.java). `toUpperCase` is registered programmatically in [McpServerApplication](src/main/java/org/springframework/ai/mcp/sample/server/McpServerApplication.java).

## Sample Clients

Three standalone clients are provided under [src/main/java/.../client/](src/main/java/org/springframework/ai/mcp/sample/client/):

| Client | Transport |
|--------|-----------|
| [StreamableHttpClient.java](src/main/java/org/springframework/ai/mcp/sample/client/StreamableHttpClient.java) | Streamable HTTP (`WebClientStreamableHttpTransport`) |
| [ClientSse.java](src/main/java/org/springframework/ai/mcp/sample/client/ClientSse.java) | SSE (`WebFluxSseClientTransport`) |
| [ClientStdio.java](src/main/java/org/springframework/ai/mcp/sample/client/ClientStdio.java) | STDIO (`StdioClientTransport`) — starts the server automatically |

### Streamable HTTP

```java
var transport = WebClientStreamableHttpTransport.builder(
        WebClient.builder().baseUrl("http://localhost:8080"))
    .build();
var client = McpClient.sync(transport).build();
client.initialize();
```

### SSE

Start the server in SSE mode before running this client.

```java
var transport = new WebFluxSseClientTransport(
        WebClient.builder().baseUrl("http://localhost:8080"),
        McpJsonDefaults.getMapper());
var client = McpClient.sync(transport).build();
client.initialize();
```

### STDIO

```java
var params = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.stdio=true",
          "-Dspring.main.web-application-type=none",
          "-Dspring.main.banner-mode=off",
          "-Dlogging.pattern.console=",
          "-jar", "target/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar")
    .build();
var transport = new StdioClientTransport(params, McpJsonDefaults.getMapper());
var client = McpClient.sync(transport).build();
client.initialize();
```

## Boot Starter Clients

For a higher-level client experience see the [starter-default-client](../../client-starter/starter-default-client) and [starter-webflux-client](../../client-starter/starter-webflux-client) projects. They auto-configure STDIO and/or SSE connections via `mcp-servers-config.json`.

### STDIO via starter client

`mcp-servers-config.json`:
```json
{
  "mcpServers": {
    "weather": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "/absolute/path/to/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

```bash
java -Dspring.ai.mcp.client.stdio.servers-configuration=file:mcp-servers-config.json \
     -Dai.user.input='What is the weather in NY?' \
     -Dlogging.pattern.console= \
     -jar mcp-starter-webflux-client-0.0.1-SNAPSHOT.jar
```

### SSE via starter client

```bash
# Terminal 1 — start the server
java -Dspring.ai.mcp.server.protocol=SSE \
     -jar target/mcp-weather-starter-webflux-server-0.0.1-SNAPSHOT.jar

# Terminal 2 — run the client
java -Dspring.ai.mcp.client.sse.connections.weather-server.url=http://localhost:8080 \
     -Dai.user.input='What is the weather in NY?' \
     -Dlogging.pattern.console= \
     -jar mcp-starter-webflux-client-0.0.1-SNAPSHOT.jar
```
