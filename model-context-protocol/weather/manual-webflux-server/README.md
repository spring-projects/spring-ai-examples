# MCP Weather Server — Manual WebFlux

A Spring AI MCP server built manually (without the MCP starter) using Spring WebFlux. Supports both **stdio** and **SSE** transports and exposes weather forecast tools via the [National Weather Service API](https://www.weather.gov/documentation/services-web-api).

## Build

```bash
./mvnw clean install -DskipTests
```

## Run the Server

Controlled by the `transport.mode` property:

**SSE mode** (HTTP server on port 8080):
```bash
java -Dtransport.mode=sse -jar target/mcp-weather-server-0.0.1-SNAPSHOT.jar
```

**Stdio mode** (launched automatically by the stdio client):
```bash
java -Dtransport.mode=stdio -Dspring.main.web-application-type=none \
  -Dspring.main.banner-mode=off -Dlogging.pattern.console= \
  -jar target/mcp-weather-server-0.0.1-SNAPSHOT.jar
```

## Sample Clients

Run from the repository root after building. The executable Spring Boot JAR keeps client classes and dependencies under `BOOT-INF`, so use `PropertiesLauncher` to select the client main class:

**SSE client** — connects to a running SSE server:
```bash
# Start server first (SSE mode), then:
java -Dloader.main=org.springframework.ai.mcp.sample.client.ClientSse \
  -cp model-context-protocol/weather/manual-webflux-server/target/mcp-weather-server-0.0.1-SNAPSHOT.jar \
  org.springframework.boot.loader.launch.PropertiesLauncher
```

**Stdio client** — launches the server as a subprocess automatically:
```bash
java -Dloader.main=org.springframework.ai.mcp.sample.client.ClientStdio \
  -cp model-context-protocol/weather/manual-webflux-server/target/mcp-weather-server-0.0.1-SNAPSHOT.jar \
  org.springframework.boot.loader.launch.PropertiesLauncher
```

## Available Tools

| Tool | Description | Parameters |
|------|-------------|------------|
| `getWeatherForecastByLocation` | Weather forecast for coordinates | `latitude`, `longitude` |
| `getAlerts` | Active weather alerts by US state | `state` (e.g. `NY`) |

## Server Configuration

The server is wired manually in `McpServerConfig`:

```java
McpSyncServer server = McpServer.sync(transportProvider)
    .serverInfo("MCP Demo Weather Server", "1.0.0")
    .capabilities(McpSchema.ServerCapabilities.builder().tools(true).logging().build())
    .tools(McpToolUtils.toSyncToolSpecifications(ToolCallbacks.from(weatherApiClient)))
    .build();
```

Transport providers are conditionally activated via `transport.mode`:
- `stdio` → `StdioServerTransportProvider`
- `sse` → `WebFluxSseServerTransportProvider` (endpoint: `/mcp/message`, SSE: `/sse`)
