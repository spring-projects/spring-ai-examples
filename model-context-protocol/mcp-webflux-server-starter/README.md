# Spring AI MCP WebFlux Server Starter

This project demonstrates how to create a Spring AI Model Context Protocol (MCP) server using Spring WebFlux. It showcases how to implement MCP tools, resources, and prompts with Spring Boot's auto-configuration capabilities.

## Overview

The project provides:
- Spring Boot starter for MCP server implementation
- WebFlux-based transport mode
- Server capabilities:
  - Tools support with OpenLibrary integration
  - Resources support with system information
  - Prompts support with greeting functionality

## Building the Project

```bash
./mvnw clean package
```

## Running the Server

The server uses WebFlux transport mode by default:

```bash
java -jar target/mcp-webflux-server-starter-0.0.1-SNAPSHOT.jar
```

## Available Tools

### OpenLibrary Tools
The server integrates with the OpenLibrary API to provide book-related functionality:

#### Get Books by Title
- Description: Get list of Books by title
- Parameters:
  - `title`: String - The book title to search for
- Example:
```java
CallToolResult response = client.callTool(
    new CallToolRequest("getBooks", Map.of("title", "Spring Framework"))
);
```

#### Get Book Titles by Author
- Description: Get book titles by author
- Parameters:
  - `authorName`: String - The author name to search for
- Example:
```java
CallToolResult response = client.callTool(
    new CallToolRequest("getBookTitlesByAuthor", Map.of("authorName", "Craig Walls"))
);
```

### Text Processing Tool
#### toUpperCase
- Description: Converts input text to uppercase
- Parameters:
  - `input`: String - The text to convert
- Example:
```java
CallToolResult response = client.callTool(
    new CallToolRequest("toUpperCase", Map.of("input", "hello world"))
);
```

## Available Resources

### System Information Resource
- URI: `system://info`
- Description: Provides basic system information including Java version, OS, etc.
- MIME Type: application/json
- Returns: JSON object containing:
  - javaVersion
  - osName
  - osVersion
  - osArch
  - processors
  - timestamp

## Available Prompts

### Greeting Prompt
- Name: `greeting`
- Description: A friendly greeting prompt
- Parameters:
  - `name`: String (required) - The name to greet
- Returns: A personalized greeting message

## Configuration

The application can be configured through `application.properties`:

```properties
# Server Configuration
spring.ai.mcp.server.enabled=true
spring.ai.mcp.server.name=my-webflux-server-starter
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.transport=WEBFLUX

# Logging Configuration (Required for proper operation)
spring.main.banner-mode=off
logging.pattern.console=
logging.file.name=./target/mcp.webflux-server-starter.log
```

## Implementation Details

### Server Configuration
```java
@SpringBootApplication
public class McpServerApplication {
    @Bean
    public List<ToolCallback> tools(OpenLibrary openLibrary) {
        List<ToolCallback> tools = new ArrayList<>();
        
        // Add OpenLibrary tools
        tools.addAll(List.of(ToolCallbacks.from(openLibrary)));
        
        // Add toUpperCase tool
        tools.add(FunctionToolCallback
            .builder("toUpperCase", 
                    (Function<ToUpperCaseInput, String>) s -> s.input().toUpperCase())
            .description("To upper case")
            .inputType(ToUpperCaseInput.class)
            .build());
            
        return tools;
    }
}
```

### OpenLibrary Integration
```java
@Service
public class OpenLibrary {
    @Tool(description = "Get list of Books by title")
    public List<Book> getBooks(String title) {
        // Implementation
    }

    @Tool(description = "Get book titles by author")
    public List<String> getBookTitlesByAuthor(String authorName) {
        // Implementation
    }
}
```

### Resource Implementation
```java
@Bean
public List<McpServerFeatures.SyncResourceRegistration> resourceRegistrations() {
    var systemInfoResource = new McpSchema.Resource(
        "system://info",
        "System Information",
        "Provides basic system information including Java version, OS, etc.",
        "application/json", 
        null
    );
    
    // Resource registration implementation
}
```

### Prompt Implementation
```java
@Bean
public List<McpServerFeatures.SyncPromptRegistration> promptRegistrations() {
    var prompt = new McpSchema.Prompt(
        "greeting",
        "A friendly greeting prompt",
        List.of(new McpSchema.PromptArgument("name", "The name to greet", true))
    );
    
    // Prompt registration implementation
}
```

## Key Features

1. **Spring Boot Integration**: Leverages Spring Boot's auto-configuration for easy setup
2. **WebFlux Transport**: Uses reactive programming model for efficient request handling
3. **OpenLibrary Integration**: Demonstrates external API integration
4. **System Resource**: Shows how to expose system information as an MCP resource
5. **Function Tools**: Examples of both service-based and function-based tool implementations
6. **Prompt Support**: Implements basic prompt functionality with parameter handling

## Notes

- The server is configured to use file-based logging to ensure proper operation of the transport layer
- Banner mode is disabled to prevent interference with the transport protocol
- The OpenLibrary integration demonstrates how to integrate external REST APIs with MCP tools
