# Spring AI MCP Weather Server Sample with WebMVC Starter and Sampling

This sample project demonstrates how to create an MCP server using the Spring AI MCP Server Boot Starter with WebMVC transport. It implements a weather service that exposes tools for retrieving weather information using the Open-Meteo API and showcases MCP Sampling capabilities.

For more information, see the [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) reference documentation.

## Overview

The sample showcases:
- Integration with `spring-ai-mcp-server-webmvc-spring-boot-starter`
- Support for both SSE (Server-Sent Events) and STDIO transports
- Automatic tool registration using Spring AI's `@Tool` annotation
- MCP Sampling implementation that demonstrates LLM provider routing
- Weather tool that retrieves temperature data and generates creative responses using multiple LLMs

## MCP Sampling Implementation

This project demonstrates the MCP Sampling capability, which allows an MCP server to delegate certain requests to LLM providers. The implementation includes:

1. **Server-side Sampling**: The `WeatherService` class implements a `callMcpSampling` method that:
   - Extracts the `McpSyncServerExchange` from the tool context
   - Creates two separate message requests with different model preferences:
     - One targeting OpenAI models with `ModelPreferences.builder().addHint("openai").build()`
     - One targeting Anthropic models with `ModelPreferences.builder().addHint("anthropic").build()`
   - Sends both requests to generate creative poems about the weather data
   - Combines the responses into a single result

2. **Client-side Sampling**: The companion client project (`mcp-sampling-client`) implements:
   - A `McpSyncClientCustomizer` that handles sampling requests
   - Logic to route requests to the appropriate LLM based on model hints
   - Integration with both OpenAI and Anthropic models

This approach demonstrates how MCP can be used to leverage multiple LLM providers within a single application, allowing for creative content generation and model comparison.

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
java -jar target/mcp-sampling-weather-server-0.0.1-SNAPSHOT.jar
```

### STDIO Mode
To enable STDIO transport, set the appropriate properties:
```bash
java -Dspring.ai.mcp.server.stdio=true -Dspring.main.web-application-type=none -jar target/mcp-sampling-weather-server-0.0.1-SNAPSHOT.jar
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

### Weather Temperature Tool
- Name: `getTemperature`
- Description: Get the temperature (in celsius) for a specific location
- Parameters:
  - `latitude`: double - The location latitude
  - `longitude`: double - The location longitude
  - `toolContext`: ToolContext - Automatically provided by Spring AI

This tool not only retrieves the current temperature from the Open-Meteo API but also uses MCP Sampling to generate creative poems about the weather from both OpenAI and Anthropic models.

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

The `WeatherService` implements the weather tool using the `@Tool` annotation and includes MCP Sampling functionality:

```java
@Service
public class WeatherService {
    @Tool(description = "Get the temperature (in celsius) for a specific location")
    public String getTemperature(double latitude, double longitude, ToolContext toolContext) {
        // Retrieve weather data from Open-Meteo API
        WeatherResponse weatherResponse = restClient
            .get()
            .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                latitude, longitude)
            .retrieve()
            .body(WeatherResponse.class);

        // Use MCP Sampling to generate creative responses
        String responseWithPoems = callMcpSampling(toolContext, weatherResponse);

        return responseWithPoems;
    }

    public String callMcpSampling(ToolContext toolContext, WeatherResponse weatherResponse) {
        // Implementation that calls both OpenAI and Anthropic models
        // to generate poems about the weather
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
          "target/mcp-sampling-weather-server-0.0.1-SNAPSHOT.jar")
    .build();

var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

The sample project includes example client implementations:
- [SampleClient.java](src/test/java/org/springframework/ai/mcp/sample/client/SampleClient.java): Manual MCP client implementation
- [ClientStdio.java](src/test/java/org/springframework/ai/mcp/sample/client/ClientStdio.java): STDIO transport connection

### Sampling Client

The companion project `mcp-sampling-client` demonstrates how to implement a client that handles MCP Sampling requests:

```java
@Bean
McpSyncClientCustomizer samplingCustomizer(Map<String, ChatClient> chatClients) {
    return (name, spec) -> {
        spec.sampling(llmRequest -> {
            var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
            String modelHint = llmRequest.modelPreferences().hints().get(0).name();

            // Find the appropriate chat client based on the model hint
            ChatClient hintedChatClient = chatClients.entrySet().stream()
                    .filter(e -> e.getKey().contains(modelHint)).findFirst()
                    .orElseThrow().getValue();

            // Generate response using the selected model
            String response = hintedChatClient.prompt()
                    .system(llmRequest.systemPrompt())
                    .user(userPrompt)
                    .call()
                    .content();

            return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
        });
    };
}
```

To run the sampling client:

1. Start the MCP server
2. Set the required environment variables:
   ```bash
   export OPENAI_API_KEY=your-openai-key
   export ANTHROPIC_API_KEY=your-anthropic-key
   ```
3. Run the client:
   ```bash
   java -jar target/mcp-sampling-client-0.0.1-SNAPSHOT.jar
   ```

## Additional Resources

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
* [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-client-docs.html)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
* [Spring Boot Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
