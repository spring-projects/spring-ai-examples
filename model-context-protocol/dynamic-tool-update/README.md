# Dynamic Tool Update Example

This example demonstrates how a Model Context Protocol (MCP) server can dynamically update its available tools at runtime, and how a client can detect and use these updated tools.

## Overview

The MCP protocol allows AI models to access external tools and resources through a standardized interface. This example showcases a key feature of MCP: the ability to dynamically update the available tools on the server side, with the client detecting and utilizing these changes.

## Key Components

### Server

The server application consists of:

1. **WeatherService**: Initially provides a weather forecast tool that retrieves temperature data for a specific location.
2. **MathTools**: Contains mathematical operations (sum, multiply, divide) that are added dynamically to the server.
3. **ServerApplication**: Manages the server lifecycle and handles the dynamic tool update process.

### Client

The client application:

1. Connects to the MCP server
2. Retrieves the initial list of available tools
3. Triggers the server to update its tools
4. Detects the tool changes
5. Retrieves the updated list of tools

## How It Works

1. **Initial Setup**: When the server starts, it only exposes the weather forecast tool.

2. **Dynamic Update Process**:
   - The client sends a request to the server's `/updateTools` endpoint
   - The server receives this signal and adds the math tools to its available tools
   - The server notifies connected clients about the tool changes
   - The client receives the notification and can now use the new tools

3. **Tool Discovery**: The client can query the available tools at any time, demonstrating that the tool list has been updated.

## Implementation Details

### Server-Side Implementation

The server uses Spring AI's MCP server implementation:

```java
@Bean
public ToolCallbackProvider weatherTools(WeatherService weatherService) {
    return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
}

@Bean
public CommandLineRunner predefinedQuestions(McpSyncServer mcpSyncServer) {
    return args -> {
        logger.info("Server: " + mcpSyncServer.getServerInfo());

        latch.await(); // Wait for update signal

        // Add math tools dynamically
        List<SyncToolSpecification> newTools = McpToolUtils
                .toSyncToolSpecifications(ToolCallbacks.from(new MathTools()));

        mcpSyncServer.addTool(newTools.iterator().next());

        logger.info("Tools updated: ");
    };
}
```

### Client-Side Implementation

The client connects to the server and registers a callback to be notified when tools change:

```java
@Bean
McpSyncClientCustomizer customizeMcpClient() {
    return (name, mcpClientSpec) -> {
        mcpClientSpec.toolsChangeConsumer(tv -> {
            logger.info("\nMCP TOOLS CHANGE: " + tv);
            latch.countDown();
        });
    };
}
```

The client retrieves the available tools before and after the update:

```java
// Get initial tools
List<ToolDescription> toolDescriptions = chatClientBuilder.build()
        .prompt("What tools are available?")
        .tools(tools)
        .call()
        .entity(new ParameterizedTypeReference<List<ToolDescription>>() {});

// Signal the server to update tools
String signal = RestClient.builder().build().get()
        .uri("http://localhost:8080/updateTools").retrieve().body(String.class);

// Wait for tool change notification
latch.await();

// Get updated tools
toolDescriptions = chatClientBuilder.build()
        .prompt("What tools are available?")
        .tools(tools)
        .call()
        .entity(new ParameterizedTypeReference<List<ToolDescription>>() {});
```

## Key Insight

The example demonstrates a crucial aspect of the MCP implementation in Spring AI:

> The client implementation relies on the fact that the `ToolCallbackProvider#getToolCallbacks` implementation for MCP will always get the current list of MCP tools from the server.

This means that whenever a client requests the available tools, it will always get the most up-to-date list from the server, without needing to restart or reinitialize the client.

## Running the Example

1. Start the server application:
   ```
   cd server
   ./mvnw spring-boot:run
   ```

2. In a separate terminal, start the client application:
   ```
   cd client
   ./mvnw spring-boot:run
   ```

3. Observe the console output to see:
   - The initial list of tools (only weather forecast)
   - The tool update notification
   - The updated list of tools (weather forecast + math operations)

## MCP Protocol Specification

For more information about the MCP protocol, refer to the official specification:
[https://modelcontextprotocol.io/specification/2024-11-05/server/tools](https://modelcontextprotocol.io/specification/2024-11-05/server/tools)
