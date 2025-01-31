# Spring AI MCP Quick Weather Server Sample

This sample project demonstrates a simplified implementation of the Spring AI Model Context Protocol (MCP). It shows how to create an MCP server that exposes weather-related tools using the National Weather Service API.

## Overview

The sample provides:
- A Spring Boot application implementing an MCP server
- Two transport mode implementations: Stdio and SSE (Server-Sent Events)
- Two weather-related tools:
  - Get weather forecast by location (latitude/longitude)
  - Get weather alerts by US state

## Building the Project

```bash
./mvnw clean package
```

## Running the Server

The server can be started in two transport modes, controlled by the `transport.mode` property:

### Stdio Mode (Default)

```bash
java -Dspring.ai.mcp.server.transport=STDIO -Dspring.main.web-application-type=none -Dlogging.pattern.console= -jar target/mcp-weather-server-quick-0.0.1-SNAPSHOT.jar
```

The Stdio mode server is automatically started by the client - no explicit server startup is needed.
But you have to build the server jar first: `./mvnw clean install -DskipTests`.

In Stdio mode the server must not emit any messages/logs to the console (e.g. standard out) but the JSON messages produced by the server.

### SSE Mode
```bash
java -Dspring.ai.mcp.server.transport=WEBFLUX -jar target/mcp-weather-server-quick-0.0.1-SNAPSHOT.jar
```

## Sample Clients

The project includes example clients for both transport modes:

### Stdio Client (ClientStdio.java)
```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dspring.ai.mcp.server.transport=STDIO", "-Dspring.main.web-application-type=none",
            "-Dlogging.pattern.console=", "-jar",
            "model-context-protocol/mcp-weather-server-quick/target/mcp-weather-server-quick-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

### SSE Client (ClientSse.java)
```java
var transport = new WebFluxSseClientTransport(WebClient.builder().baseUrl("http://localhost:8080"));
var client = McpClient.using(transport).sync();
```

### Claud Destop

```json
{
  "mcpServers": {
    "spring-ai-mcp-weather": {
      "command": "java",
      "args": [
        "-Dspring.ai.mcp.server.transport=STDIO",
        "-Dspring.main.web-application-type=none",
        "-Dlogging.pattern.console=",
        "-jar",
        "<YOUR ABSOLUTE PATH TO>/mcp-weather-server-quick-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## Available Tools

### Weather Forecast Tool
- Name: `getWeatherForecastByLocation`
- Description: Get weather forecast for a specific latitude/longitude
- Parameters:
  - `latitude`: double - Latitude coordinate
  - `longitude`: double - Longitude coordinate
- Example:
```java
CallToolResult forecastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
        Map.of("latitude", 47.6062, "longitude", -122.3321)));
```

### Weather Alerts Tool
- Name: `getAlerts`
- Description: Get weather alerts for a US state
- Parameters:
  - `state`: String - Two-letter US state code (e.g. CA, NY)
- Example:
```java
CallToolResult alertResult = client.callTool(new CallToolRequest("getAlerts", 
        Map.of("state", "NY")));
```

## Client Usage Example

```java
// Initialize client
client.initialize();

// Test connection
client.ping();

// List available tools
ListToolsResult tools = client.listTools();
System.out.println("Available tools: " + tools);

// Get weather forecast for Seattle
CallToolResult weatherForcastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
        Map.of("latitude", "47.6062", "longitude", "-122.3321")));
System.out.println("Weather Forcast: " + weatherForcastResult);

// Get weather alerts for New York
CallToolResult alertResult = client.callTool(new CallToolRequest("getAlerts", Map.of("state", "NY")));
System.out.println("Alert Response = " + alertResult);

// Close client
client.closeGracefully();
```

## Server Implementation

The server is implemented using Spring Boot and Spring AI's tool annotations:

```java
@SpringBootApplication
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }

    @Bean
    public List<ToolCallback> weatherTools(WeatherService weatherService) {
        return List.of(ToolCallbacks.from(weatherService));
    }
}
```

The `WeatherService` class provides the tool implementations using the `@Tool` annotation:

```java
@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        // Implementation using weather.gov API
    }

    @Tool(description = "Get weather alerts for a US state. Input is Two-letter US state code (e.g. CA, NY)")
    public String getAlerts(String state) {
        // Implementation using weather.gov API
    }
}
```

## Configuration

The application can be configured through `application.properties`:

- `spring.ai.mcp.server.transport`: Transport mode to use (STDIO/WEBFLUX)
- `server.port`: Server port for WEBFLUX mode (default: 8080)
- `spring.main.banner-mode`: Set to 'off' for STDIO mode
- `logging.pattern.console`: Clear this property for STDIO mode
- `logging.file.name`: Path to log file (useful when console logging is disabled)
