# Spring AI MCP Weather STDIO Server

A Spring Boot application demonstrating an MCP server with STDIO transport that exposes weather tools via the [National Weather Service API](https://api.weather.gov). Uses the `spring-ai-starter-mcp-server` auto-configuration.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Prerequisites

- Java 25+
- Maven 3.6+

## How It Works

Annotate a `@Service` bean's methods with `@McpTool` — the starter auto-registers them as MCP tools:

```java
@Service
public class WeatherService {

    @McpTool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) { ... }

    @McpTool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
    public String getAlerts(@McpToolParam(description = "Two-letter US state code (e.g. CA, NY") String state) { ... }
}
```

No manual bean wiring needed — the starter discovers all `@McpTool`-annotated methods automatically.

## Available Tools

| Tool | Description |
|------|-------------|
| `getWeatherForecastByLocation` | Weather forecast for a latitude/longitude coordinate |
| `getAlerts` | Active weather alerts for a two-letter US state code (e.g. `NY`, `CA`) |

## Build

```bash
./mvnw clean install -DskipTests
```

## Run the Test Client

`ClientStdio` starts the server as a subprocess and exercises both tools:

```bash
./mvnw exec:java -Dexec.mainClass="org.springframework.ai.mcp.sample.client.ClientStdio"
```

Or from the repository root after building:

```bash
java -jar model-context-protocol/weather/starter-stdio-server/target/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar
```

## Claude Desktop Integration

Build the jar, then add to your Claude Desktop `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "spring-ai-mcp-weather": {
      "command": "java",
      "args": [
        "-jar",
        "/absolute/path/to/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## Configuration

Key STDIO requirements in `application.properties`:

```properties
# Disable web server and suppress console output so STDIO transport works
spring.main.web-application-type=none
spring.main.banner-mode=off
logging.pattern.console=

spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1

# Optional: redirect logs to a file for debugging
logging.file.name=./target/mcp-weather-stdio-server.log
```

## Related Examples

- [starter-default-client](../../client-starter/starter-default-client) — MCP client that connects to this server
- [starter-webflux-client](../../client-starter/starter-webflux-client) — reactive MCP client
