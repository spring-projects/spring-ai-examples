# Spring AI MCP Sampling Examples - Annotation-Based Implementation

This directory contains annotation-based examples demonstrating the Model Context Protocol (MCP) Sampling capability in Spring AI. These examples showcase the same MCP Sampling functionality as the main sampling examples but use Spring AI's annotation-based approach for simplified development.

## Overview

The annotation-based MCP Sampling examples demonstrate:

- **Simplified Development**: Using annotations (`@McpTool`, `@McpSampling`, `@McpLogging`) instead of manual tool registration
- **Declarative Configuration**: Annotation-driven MCP server and client setup
- **Multi-Model Integration**: Seamless routing between OpenAI and Anthropic models
- **Creative Content Generation**: Generating poems about weather data using multiple LLMs
- **Unified Response Handling**: Combining responses from different models into cohesive results

## What Makes This Different?

The annotation-based approach provides several advantages over the traditional implementation:

### Server-Side Benefits
- **`@McpTool`**: Automatic tool registration without manual `ToolCallbackProvider` configuration
- **`SyncMcpAnnotationProvider`**: Automatic tool specification generation
- **Simplified Dependency Injection**: Direct access to `McpSyncServerExchange` as method parameter

### Client-Side Benefits
- **`@McpSampling`**: Declarative sampling request handling
- **`@McpLogging`**: Built-in logging support for MCP operations
- **`@McpProgress`**: Progress notification handling
- **`AnnotationSyncClientCustomizer`**: Automatic registration of annotation-based handlers

## Projects

### 1. mcp-sampling-server-annotations
An MCP server implementation using Spring AI's annotation-based approach that:
- Provides weather information via the `@McpTool` annotation
- Uses MCP Sampling to generate creative content from multiple LLM providers
- Demonstrates simplified server-side MCP implementation

### 2. mcp-sampling-client-annotations
An MCP client implementation using annotation-based handlers that:
- Handles sampling requests using `@McpSampling`
- Routes requests to different LLM providers based on model hints
- Provides logging and progress tracking capabilities

## How It Works

### Annotation-Based Server Implementation

The server uses the `@McpTool` annotation for automatic tool registration:

```java
@Service
public class WeatherService {

    @McpTool(description = "Get the temperature (in celsius) for a specific location")
    public String getTemperature2(McpSyncServerExchange exchange,
            @McpToolParam(description = "The location latitude") double latitude,
            @McpToolParam(description = "The location longitude") double longitude) {
        
        // Retrieve weather data
        WeatherResponse weatherResponse = restClient
            .get()
            .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                latitude, longitude)
            .retrieve()
            .body(WeatherResponse.class);

        // Use MCP Sampling with model preferences
        if (exchange.getClientCapabilities().sampling() != null) {
            var messageRequestBuilder = McpSchema.CreateMessageRequest.builder()
                .systemPrompt("You are a poet!")
                .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                    new McpSchema.TextContent("Please write a poem about this weather forecast..."))));

            // Request poem from OpenAI
            var openAiRequest = messageRequestBuilder
                .modelPreferences(ModelPreferences.builder().addHint("openai").build())
                .build();
            CreateMessageResult openAiResponse = exchange.createMessage(openAiRequest);

            // Request poem from Anthropic
            var anthropicRequest = messageRequestBuilder
                .modelPreferences(ModelPreferences.builder().addHint("anthropic").build())
                .build();
            CreateMessageResult anthropicResponse = exchange.createMessage(anthropicRequest);
        }

        return combinedResponse;
    }
}
```

Tool registration is handled automatically:

```java
@SpringBootApplication
public class McpServerApplication {
    @Bean
    public List<SyncToolSpecification> toolSpecs(WeatherService weatherService) {
        return SyncMcpAnnotationProvider.createSyncToolSpecifications(List.of(weatherService));
    }
}
```

### Annotation-Based Client Implementation

The client uses annotation-based handlers for MCP operations:

```java
@Service
public class ClientMcpHandlers {

    @Autowired
    Map<String, ChatClient> chatClients;

    @McpSampling
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
        String modelHint = llmRequest.modelPreferences().hints().get(0).name();

        // Find the appropriate chat client based on the model hint
        ChatClient hintedChatClient = chatClients.entrySet().stream()
            .filter(e -> e.getKey().contains(modelHint))
            .findFirst()
            .orElseThrow().getValue();

        // Generate response using the selected model
        String response = hintedChatClient.prompt()
            .system(llmRequest.systemPrompt())
            .user(userPrompt)
            .call()
            .content();

        return CreateMessageResult.builder()
            .content(new McpSchema.TextContent(response))
            .build();
    }

    @McpLogging
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
    }

    @McpProgress(clientId = "server1")
    public void progressHandler(ProgressNotification progressNotification) {
        logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
            progressNotification.progressToken(), progressNotification.progress(),
            progressNotification.total(), progressNotification.message());
    }
}
```

Handler registration is managed through configuration:

```java
@Configuration
public class AppConfiguration {

    @Bean
    List<SyncSamplingSpecification> samplingSpecs(ClientMcpHandlers clientMcpHandlers) {
        return SyncMcpAnnotationProvider.createSyncSamplingSpecifications(List.of(clientMcpHandlers));
    }

    @Bean
    List<SyncLoggingSpecification> loggingSpecs(ClientMcpHandlers clientMcpHandlers) {
        return SyncMcpAnnotationProvider.createSyncLoggingSpecifications(List.of(clientMcpHandlers));
    }

    @Bean
    McpSyncClientCustomizer annotationMcpSyncClientCustomizer(
            List<SyncLoggingSpecification> loggingSpecs,
            List<SyncSamplingSpecification> samplingSpecs,
            List<SyncElicitationSpecification> elicitationSpecs,
            List<SyncProgressSpecification> progressSpecs) {
        return new AnnotationSyncClientCustomizer(samplingSpecs, loggingSpecs, elicitationSpecs, progressSpecs);
    }
}
```

## Key Dependencies

Both projects use the Spring AI MCP Annotations library:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### Server Dependencies
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### Client Dependencies
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-anthropic</artifactId>
</dependency>
```

## Running the Examples

### Prerequisites

- Java 17 or later
- Maven 3.6+
- OpenAI API key
- Anthropic API key

### Step 1: Start the Annotation-Based MCP Weather Server

```bash
cd mcp-sampling-server-annotations
./mvnw clean package -DskipTests
java -jar target/mcp-sampling-server-annotations-0.0.1-SNAPSHOT.jar
```

The server will start on `http://localhost:8080` with stateless-http transport enabled.

### Step 2: Set Environment Variables

```bash
export OPENAI_API_KEY=your-openai-key
export ANTHROPIC_API_KEY=your-anthropic-key
```

### Step 3: Run the Annotation-Based MCP Sampling Client

```bash
cd mcp-sampling-client-annotations
./mvnw clean package
java -jar target/mcp-sampling-client-annotations-0.0.1-SNAPSHOT.jar
```

## Configuration

### Stateless-HTTP Transport Configuration

These examples demonstrate the use of **stateless-http transport** for MCP communication, which provides several advantages over traditional SSE (Server-Sent Events) transport:

- **Stateless Operation**: Each request is independent, making it easier to scale and debug
- **HTTP-Based**: Uses standard HTTP requests/responses, simplifying network configuration
- **Better Error Handling**: More predictable error handling and recovery
- **Firewall Friendly**: Works better with corporate firewalls and proxies

#### Key Configuration Properties

**Server Side:**
- `spring.ai.mcp.server.protocol=STREAMABLE` - Enables the stateless-http server protocol

**Client Side:**
- `spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080` - Configures the stateless-http client to connect to the server

### Server Configuration (`application.properties`)

```properties
# Server identification
spring.ai.mcp.server.name=mcp-sampling-server-annotations
spring.ai.mcp.server.version=0.0.1

# Enable stateless-http server protocol
spring.ai.mcp.server.protocol=STREAMABLE

# Disable banner and configure logging
spring.main.banner-mode=off
logging.file.name=./model-context-protocol/sampling/mcp-sampling-server-annotations/target/server.log

# Uncomment for STDIO transport
# spring.ai.mcp.server.stdio=true
# spring.main.web-application-type=none
```

### Client Configuration (`application.properties`)

```properties
spring.application.name=mcp
spring.main.web-application-type=none

# Disable default chat client auto-configuration for multiple models
spring.ai.chat.client.enabled=false

# API keys
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}

# MCP client connection using stateless-http transport
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080

# Disable tool callback to prevent cyclic dependencies
spring.ai.mcp.client.toolcallback.enabled=false

# Logging configuration
logging.level.io.modelcontextprotocol.client=WARN
logging.level.io.modelcontextprotocol.spec=WARN
```

## Sample Output

When you run the annotation-based client, you'll see output similar to:

```
> USER: What is the weather in Amsterdam right now?
Please incorporate all creative responses from all LLM providers.
After the other providers add a poem that synthesizes the poems from all the other providers.

> ASSISTANT: 
OpenAI poem about the weather:
**Amsterdam's Winter Whisper**
*Temperature: 4.2°C*

In Amsterdam's embrace, where canals reflect the sky,
A gentle chill of 4.2 degrees drifts by...

Anthropic poem about the weather:
**Canal-Side Contemplation**
*Current conditions: 4.2°C*

Along the waterways where bicycles rest,
The winter air puts Amsterdam to test...

Weather Data:
{
  "current": {
    "time": "2025-01-23T11:00",
    "interval": 900,
    "temperature_2m": 4.2
  }
}
```

## Comparison with Traditional Implementation

| Aspect | Traditional Approach | Annotation-Based Approach |
|--------|---------------------|---------------------------|
| **Tool Registration** | Manual `ToolCallbackProvider` | `@McpTool` annotation |
| **Sampling Handler** | `McpSyncClientCustomizer` lambda | `@McpSampling` method |
| **Configuration** | Programmatic bean configuration | Annotation-driven specifications |
| **Code Complexity** | More boilerplate code | Reduced boilerplate |
| **Type Safety** | Manual parameter handling | Automatic parameter injection |
| **Debugging** | Complex stack traces | Clearer annotation-based flow |

## Advantages of Annotation-Based Approach

1. **Reduced Boilerplate**: Less configuration code required
2. **Declarative Style**: Clear intent through annotations
3. **Type Safety**: Automatic parameter validation and injection
4. **Better IDE Support**: Enhanced code completion and navigation
5. **Easier Testing**: Simplified unit testing of individual handlers
6. **Maintainability**: Cleaner separation of concerns

## Related Projects

- **[Main Sampling Examples](../)**: Traditional implementation using manual configuration
- **[MCP Annotations](../../mcp-annotations)**: Core annotation-based MCP examples
- **[Weather Server](../../weather)**: Simple weather MCP server
- **[SQLite Server](../../sqlite)**: Database access MCP server
- **[Filesystem Server](../../filesystem)**: File system operations MCP server

## Additional Resources

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
* [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
* [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* [Spring AI MCP Annotations](https://github.com/spring-projects-experimental/spring-ai-mcp-annotations)
