# Spring AI MCP Weather Server

This project demonstrates how to create a Spring AI Model Context Protocol (MCP) server that provides weather data using Spring WebFlux. It showcases how to implement MCP tools with Spring Boot's auto-configuration capabilities.

## Overview

The project provides:
- Spring Boot starter for MCP server implementation
- Multiple transport modes:
  - WebFlux SSE (Server-Sent Events) - default
  - STDIO - for direct communication
- Server capabilities:
  - Weather tool that integrates with the Open-Meteo API
  - Support for both WebFlux SSE and STDIO transport modes

## Building the Project

```bash
./mvnw clean package
```

## Running the Server

The server uses WebFlux SSE transport mode by default:

```bash
./mvnw spring-boot:run
```

## Running the Client Tests

After starting the server, you can run one of the client tests:

### WebFlux SSE Client

The WebFlux SSE client connects to the server running on http://localhost:8080:

```bash
# Ensure the server is running first
java -cp target/mcp-ta-weather-server-0.0.1-SNAPSHOT.jar org.springframework.ai.mcp.sample.client.ClientWebFluxSse
```

### STDIO Client

The STDIO client automatically starts the server process:

```bash
# Build the project first
./mvnw clean install -DskipTests

# Run the STDIO client
java -cp target/mcp-ta-weather-server-0.0.1-SNAPSHOT.jar org.springframework.ai.mcp.sample.client.ClientStdio
```

## Available Tools

### Weather Tool

The server integrates with the Open-Meteo API to provide weather data:

#### Get Temperature
- Description: Get the temperature (in celsius) for a specific location
- Parameters:
  - `latitude`: Double - The location latitude
  - `longitude`: Double - The location longitude
  - `city`: String - The city name (for logging purposes)
- Example:
```java
CallToolResult response = client.callTool(
    new CallToolRequest("getTemperature", Map.of(
        "latitude", "52.377956", 
        "longitude", "4.897070", 
        "city", "Amsterdam"))
);
```

## Configuration

The application can be configured through `application.properties`:

```properties
# Server Configuration
spring.ai.mcp.server.name=my-mcp-server
spring.ai.mcp.server.version=0.0.1

# Logging Configuration (Required for proper operation)
spring.main.banner-mode=off
logging.pattern.console=
logging.file.name=./target/mcp-weather-server.log
```

## Implementation Details

### Server Configuration
```java
@SpringBootApplication
public class McpServerApplication {
    @Bean
    public ToolCallbackProvider openLibraryTools(WeatherTools weatherTools) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherTools)
            .build();
    }
}
```

### Weather Tool Implementation
```java
@Service
public class WeatherTools {
    @Tool(description = "Get the temperature (in celsius) for a specific location")
    public WeatherResponse getTemperature(
            @ToolParam(description = "The location latitude") double latitude,
            @ToolParam(description = "The location longitude") double longitude,
            @ToolParam(description = "The city name") String city) {
        
        // Implementation using Open-Meteo API
        WeatherResponse response = restClient
            .get()
            .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                    latitude, longitude)
            .retrieve()
            .body(WeatherResponse.class);
            
        return response;
    }
}
```

### Anthropic Claude Desktop

```json
{
  "mcpServers": {
    "mcp-weather-server": {
      "command": "java",
      "args": [
        "-Dlogging.file.name=/tmp/mcp-ta-weather-server-2.log",
        "-Dspring.ai.mcp.server.stdio=true",
        "-Dspring.main.web-application-type=none",
        "-jar",
        "/path/to/mcp-ta-weather-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

## Key Features

1. **Spring Boot Integration**: Leverages Spring Boot's auto-configuration for easy setup
2. **Multiple Transport Modes**: Supports both WebFlux SSE and STDIO transport
3. **Open-Meteo Integration**: Demonstrates external API integration with the Open-Meteo weather API
4. **Tool Annotations**: Uses Spring AI's `@Tool` and `@ToolParam` annotations for clean, declarative tool definitions
5. **Client Examples**: Includes sample clients for both transport modes

## Notes

- The server is configured to use file-based logging to ensure proper operation of the transport layer
- Banner mode is disabled to prevent interference with the transport protocol
- When using STDIO transport, the console logging pattern must be empty
- The WebFlux SSE transport runs on port 8080 by default
