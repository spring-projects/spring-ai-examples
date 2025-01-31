# Spring AI MCP Sample Weather MCP Server

This sample project demonstrates the usage of the Spring AI Model Context Protocol (MCP) implementation. It showcases how to create and use MCP servers and clients with different transport modes and capabilities.

## Overview

The sample provides:
- Two transport mode implementations: Stdio and SSE (Server-Sent Events)
- Server capabilities:
  - Tools support with list changes notifications
  - Resources support with list changes notifications (no subscribe support)
  - Prompts support with list changes notifications
- Sample implementations:
  - Two MCP tools: Weather and Calculator
  - One MCP resource: System Information
  - One MCP prompt: Greeting

## Building the Project

```bash
./mvnw clean package
```

## Running the Server

The server can be started in two transport modes, controlled by the `transport.mode` property:

### Stdio Mode (Default)

```bash
java -Dtransport.mode=stdio -jar model-context-protocol/mcp-weather-server/target/mcp-weather-server-0.0.1-SNAPSHOT.jar
```

The Stdio mode server is automatically started by the client - no explicit server startup is needed.
But you have to build the server jar first: `./mvnw clean install -DskipTests`.

In Stdio mode the server must not emit any messages/logs to the console (e.g. standard out) but the JSON messages produced by the server.

### SSE Mode
```bash
java -Dtransport.mode=sse -jar model-context-protocol/mcp-weather-server/target/mcp-weather-server-0.0.1-SNAPSHOT.jar
```

## Sample Clients

The project includes example clients for both transport modes:

### Stdio Client (ClientStdio.java)
```java
var stdioParams = ServerParameters.builder("java")
    .args("-Dtransport.mode=stdio", "-Dspring.main.web-application-type=none", "-jar",
            "model-context-protocol/mcp-weather-server/target/mcp-weather-server-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.using(transport).sync();
```

### SSE Client (ClientWebFluxSse.java)
```java
var transport = new SseClientTransport(WebClient.builder().baseUrl("http://localhost:8080"));
var client = McpClient.using(transport).sync();
```

## Available Tools

### Weather Tool
- Name: `getWeatherForecastByLocation`
- Description: Weather forecast tool by location
- Parameters:
  - `lat, long`: String - latitude, longitude of the locaiton
- Example:
```java
CallToolResult weatherForcastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
        Map.of("latitude", "47.6062", "longitude", "-122.3321")));
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

CallToolResult weatherForcastResult = client.callTool(new CallToolRequest("getWeatherForecastByLocation",
        Map.of("latitude", "47.6062", "longitude", "-122.3321")));
System.out.println("Weather Forcast: " + weatherForcastResult);

CallToolResult alertResult = client.callTool(new CallToolRequest("getAlerts", Map.of("state", "NY")));
System.out.println("Alert Response = " + alertResult);


// Close client
client.closeGracefully();
```

## Server Usage Example

```java
@Configuration
public class CustomMcpServerConfig {

    @Bean
	public WeatherApiClient weatherApiClient() {
		return new WeatherApiClient();
	}

	@Bean
	public McpSyncServer mcpServer(ServerMcpTransport transport, WeatherApiClient weatherApiClient) { // @formatter:off

		// Configure server capabilities with resource support
		var capabilities = McpSchema.ServerCapabilities.builder()
			.tools(true) // Tool support with list changes notifications
			.logging() // Logging support
			.build();

		// Create the server with both tool and resource capabilities
		McpSyncServer server = McpServer.sync(transport)
			.serverInfo("MCP Demo Weather Server", "1.0.0")
			.capabilities(capabilities)
			.tools(ToolHelper.toSyncToolRegistration(ToolCallbacks.from(weatherApiClient))) // Add @Tools
			.build();
		
		return server; // @formatter:on
	} // @formatter:on
}
```

## Configuration

The application can be configured through `application.properties`:

- `transport.mode`: Transport mode to use (stdio/sse)
- `server.port`: Server port for SSE mode (default: 8080)
- Various logging configurations are available for debugging

