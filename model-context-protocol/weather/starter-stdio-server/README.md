# Spring AI MCP Weather STDIO Server

A Spring Boot starter project demonstrating how to build a Model Context Protocol (MCP) server that provides weather-related tools using the National Weather Service (weather.gov) API. This project showcases the Spring AI MCP Server Boot Starter capabilities with STDIO transport implementation.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Prerequisites

- Java 17 or later
- Maven 3.6 or later
- Understanding of Spring Boot and Spring AI concepts
- (Optional) Claude Desktop for AI assistant integration

## About Spring AI MCP Server Boot Starter

The `spring-ai-mcp-server-spring-boot-starter` provides:
- Automatic configuration of MCP server components
- Support for both synchronous and asynchronous operation modes
- STDIO transport layer implementation
- Flexible tool registration through Spring beans
- Change notification capabilities

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── org/springframework/ai/mcp/sample/server/
│   │       ├── McpServerApplication.java    # Main application class with tool registration
│   │       └── WeatherService.java          # Weather service implementation with MCP tools
│   └── resources/
│       └── application.properties           # Server and transport configuration
└── test/
    └── java/
        └── org/springframework/ai/mcp/sample/client/
            └── ClientStdio.java             # Test client implementation
```

## Building and Running

The server uses STDIO transport mode and is typically started automatically by the client. To build the server jar:

```bash
./mvnw clean install -DskipTests
```

## Tool Implementation

The project demonstrates how to implement and register MCP tools using Spring's dependency injection and auto-configuration:

```java
@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(
        double latitude,   // Latitude coordinate
        double longitude   // Longitude coordinate
    ) {
        // Implementation
    }

    @Tool(description = "Get weather alerts for a US state")
    public String getAlerts(
        String state  // Two-letter US state code (e.g., CA, NY)
    ) {
        // Implementation
    }
}

@SpringBootApplication
public class McpServerApplication {
    @Bean
    public List<ToolCallback> weatherTools(WeatherService weatherService) {
        return ToolCallbacks.from(weatherService);
    }
}
```

The auto-configuration automatically registers these tools with the MCP server. You can have multiple beans producing lists of ToolCallbacks, and the auto-configuration will merge them.

## Available Tools

### 1. Weather Forecast Tool
```java
@Tool(description = "Get weather forecast for a specific latitude/longitude")
public String getWeatherForecastByLocation(
    double latitude,   // Latitude coordinate
    double longitude   // Longitude coordinate
) {
    // Returns detailed forecast including:
    // - Temperature and unit
    // - Wind speed and direction
    // - Detailed forecast description
}

// Example usage:
CallToolResult forecast = client.callTool(
    new CallToolRequest("getWeatherForecastByLocation",
        Map.of(
            "latitude", 47.6062,    // Seattle coordinates
            "longitude", -122.3321
        )
    )
);
```

### 2. Weather Alerts Tool
```java
@Tool(description = "Get weather alerts for a US state")
public String getAlerts(
    String state  // Two-letter US state code (e.g., CA, NY)
) {
    // Returns active alerts including:
    // - Event type
    // - Affected area
    // - Severity
    // - Description
    // - Safety instructions
}

// Example usage:
CallToolResult alerts = client.callTool(
    new CallToolRequest("getAlerts",
        Map.of("state", "NY")
    )
);
```

## Client Integration

### Java Client Example

#### Create MCP Client Manually

```java
// Create server parameters
ServerParameters stdioParams = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.transport=STDIO",
          "-Dspring.main.web-application-type=none",
          "-jar",
          "target/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar")
    .build();

// Initialize transport and client
var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

The [ClientStdio.java](src/test/java/org/springframework/ai/mcp/sample/client/ClientStdio.java) demonstrates how to implement an MCP client manually.

For a better development experience, consider using the [MCP Client Boot Starters](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html). These starters enable auto-configuration of multiple STDIO and/or SSE connections to MCP servers. See the [starter-default-client](../../client-starter/starter-default-client) and [starter-webflux-client](../../client-starter/starter-webflux-client) projects for examples.

#### Use MCP Client Boot Starter

Use the [starter-default-client](../../client-starter/starter-default-client) to connect to the weather `starter-stdio-server`:

1. Follow the `starter-default-client` readme instructions to build a `mcp-starter-default-client-0.0.1-SNAPSHOT.jar` client application.

2. Run the client using the configuration file:

```bash
java -Dspring.ai.mcp.client.stdio.connections.server1.command=java \
     -Dspring.ai.mcp.client.stdio.connections.server1.args=-jar,/Users/christiantzolov/Dev/projects/spring-ai-examples/model-context-protocol/weather/starter-stdio-server/target/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar \
     -Dai.user.input='What is the weather in NY?' \
     -jar mcp-starter-default-client-0.0.1-SNAPSHOT.jar
```

### Claude Desktop Integration

To integrate with Claude Desktop, add the following configuration to your Claude Desktop settings:

```json
{
  "mcpServers": {
    "spring-ai-mcp-weather": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-jar",
        "/absolute/path/to/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

Replace `/absolute/path/to/` with the actual path to your built jar file.

## Configuration

### Application Properties

All properties are prefixed with `spring.ai.mcp.server`:

```properties
# Required STDIO Configuration
spring.main.web-application-type=none
spring.main.banner-mode=off

# Server Configuration
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1
# SYNC or ASYNC
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.resource-change-notification=true
spring.ai.mcp.server.tool-change-notification=true
spring.ai.mcp.server.prompt-change-notification=true

# Optional file logging
logging.file.name=mcp-weather-stdio-server.log
```

### Logback Configuration

Redirect all console logs to stderr with `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml" />
    <include resource="org/springframework/boot/logging/logback/file-appender.xml" />
    <appender name="CONSOLE_LOG_STDERR" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.err</target>
        <encoder>
            <pattern>${CONSOLE_LOG_PATTERN}</pattern>
            <charset>${CONSOLE_LOG_CHARSET}</charset>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="CONSOLE_LOG_STDERR" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Key Configuration Notes

1. **STDIO Mode Requirements**
   - Disable web application type (`spring.main.web-application-type=none`)
   - Disable Spring banner (`spring.main.banner-mode=off`)
   - Configure Logback to output to stderr (`logback-spring.xml`)

2. **Server Type**
   - `SYNC` (default): Uses `McpSyncServer` for straightforward request-response patterns
   - `ASYNC`: Uses `McpAsyncServer` for non-blocking operations with Project Reactor support

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-client-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [Spring Boot Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
