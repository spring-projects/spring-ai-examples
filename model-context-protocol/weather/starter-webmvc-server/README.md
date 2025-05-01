# Spring AI MCP Weather Server Sample with WebMVC Starter

This sample project demonstrates how to create an MCP server using the Spring AI MCP Server Boot Starter with WebMVC transport. It implements a weather service that exposes tools for retrieving weather information using the National Weather Service API.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Overview

The sample showcases:
- Integration with `spring-ai-mcp-server-webmvc-spring-boot-starter`
- Support for both SSE (Server-Sent Events) and STDIO transports
- Automatic tool registration using Spring AI's `@Tool` annotation
- Two weather-related tools:
  - Get weather forecast by location (latitude/longitude)
  - Get weather alerts by US state

## Dependencies

The project requires the Spring AI MCP Server WebMVC Boot Starter:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-server-webmvc-spring-boot-starter</artifactId>
</dependency>
```

This starter provides:
- HTTP-based transport using Spring MVC (`WebMvcSseServerTransport`)
- Auto-configured SSE endpoints
- Optional STDIO transport
- Included `spring-boot-starter-web` and `mcp-spring-webmvc` dependencies

## Building the Project

Build the project using Maven:
```bash
./mvnw clean install -DskipTests
```

## Running the Server

The server supports two transport modes:

### WebMVC SSE Mode (Default)
```bash
java -jar target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar
```

### STDIO Mode
To enable STDIO transport, set the appropriate properties:
```bash
java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -jar target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar
```

## Configuration

Configure the server through `application.properties`:

```properties
# Server identification
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1

# Server type (SYNC/ASYNC)
spring.ai.mcp.server.type=SYNC

# Transport configuration
spring.ai.mcp.server.stdio=false
spring.ai.mcp.server.sse-message-endpoint=/mcp/message

# Change notifications
spring.ai.mcp.server.resource-change-notification=true
spring.ai.mcp.server.tool-change-notification=true
spring.ai.mcp.server.prompt-change-notification=true

# Logging (required for STDIO transport)
spring.main.banner-mode=off
logging.file.name=./target/starter-webmvc-server.log
```

## Available Tools

### Weather Forecast Tool
- Name: `getWeatherForecastByLocation`
- Description: Get weather forecast for a specific latitude/longitude
- Parameters:
  - `latitude`: double - Latitude coordinate
  - `longitude`: double - Longitude coordinate

### Weather Alerts Tool
- Name: `getAlerts`
- Description: Get weather alerts for a US state
- Parameters:
  - `state`: String - Two-letter US state code (e.g., CA, NY)

## Server Implementation

The server uses Spring Boot and Spring AI's tool annotations for automatic tool registration:

```java
@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService){
      return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }
}
```

The `WeatherService` implements the weather tools using the `@Tool` annotation:

```java
@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        // Implementation using weather.gov API
    }

    @Tool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g., CA, NY)")
    public String getAlerts(String state) {
        // Implementation using weather.gov API
    }
}
```

## MCP Clients 

You can connect to the weather server using either STDIO or SSE transport:

### Manual Clients

#### WebMVC SSE Client

For servers using SSE transport:

```java
var transport = new HttpClientSseClientTransport("http://localhost:8080");
var client = McpClient.sync(transport).build();
```

#### STDIO Client

For servers using STDIO transport:

```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.stdio=true",
          "-Dspring.main.web-application-type=none",
          "-Dspring.main.banner-mode=off",
          "-Dlogging.pattern.console=",
          "-jar",
          "target/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

The sample project includes example client implementations:
- [SampleClient.java](src/test/java/org/springframework/ai/mcp/sample/client/SampleClient.java): Manual MCP client implementation
- [ClientStdio.java](src/test/java/org/springframework/ai/mcp/sample/client/ClientStdio.java): STDIO transport connection
- [ClientSse.java](src/test/java/org/springframework/ai/mcp/sample/client/ClientSse.java): SSE transport connection

For a better development experience, consider using the [MCP Client Boot Starters](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html). These starters enable auto-configuration of multiple STDIO and/or SSE connections to MCP servers. See the [starter-default-client](../../client-starter/starter-default-client) project for examples.

### Boot Starter Clients

Let's use the [starter-default-client](../../client-starter/starter-default-client) client to connect to our weather `starter-webmvc-server`.

Follow the `starter-default-client` readme instruction to build a `mcp-starter-default-client-0.0.1-SNAPSHOT.jar` client application.

#### STDIO Transport

1. Create a `mcp-servers-config.json` configuration file with this content:

```json
{
  "mcpServers": {
    "weather-starter-webmvc-server": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "/absolute/path/to/mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

2. Run the client using the configuration file:

```bash
java -Dspring.ai.mcp.client.stdio.servers-configuration=file:mcp-servers-config.json \
 -Dai.user.input='What is the weather in NY?' \
 -Dlogging.pattern.console= \
 -jar mcp-starter-default-client-0.0.1-SNAPSHOT.jar
```

#### SSE (WebMVC) Transport

1. Start the `mcp-weather-starter-webmvc-server`:

```bash
java -jar mcp-weather-starter-webmvc-server-0.0.1-SNAPSHOT.jar
```

starts the MCP server on port 8080.

2. In another console start the client configured with SSE transport:

```bash
java -Dspring.ai.mcp.client.sse.connections.weather-server.url=http://localhost:8080 \
 -Dlogging.pattern.console= \
 -Dai.user.input='What is the weather in NY?' \
 -jar mcp-starter-default-client-0.0.1-SNAPSHOT.jar
```

## Additional Resources

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
* [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
* [Spring Boot Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
