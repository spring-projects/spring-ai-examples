# Spring AI MCP Sampling Client

This project demonstrates how to implement a client for the MCP (Model Context Protocol) Sampling capability using Spring AI. It showcases how to route LLM requests to different providers (OpenAI and Anthropic) based on model preferences.

## Overview

The MCP Sampling Client:
- Connects to an MCP server using SSE (Server-Sent Events) transport
- Implements a sampling handler that routes requests to different LLM providers
- Integrates with both OpenAI and Anthropic models
- Demonstrates how to use model hints to select the appropriate LLM
- Combines creative responses from multiple LLMs into a single result

## MCP Sampling Implementation

MCP Sampling is a powerful capability that allows an MCP server to delegate certain requests to LLM providers. This client implements the client-side handling of sampling requests:

1. **Sampling Handler Registration**: The client registers a sampling handler using the `McpSyncClientCustomizer`:

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

2. **Model Routing**: The client uses the model hint from the request to select the appropriate LLM provider:
   - If the hint is "openai", it routes to the OpenAI model
   - If the hint is "anthropic", it routes to the Anthropic model

3. **Chat Client Management**: The client creates and manages multiple chat clients, one for each LLM provider:

```java
@Bean
public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {
    return chatModels.stream().collect(Collectors.toMap(
        model -> model.getClass().getSimpleName().toLowerCase(),
        model -> ChatClient.builder(model).build()));
}
```

4. **Integration with Spring AI**: The client leverages Spring AI's auto-configuration to set up the necessary components:

```java
var mcpToolProvider = new SyncMcpToolCallbackProvider(
    mcpClientsProvider.stream().flatMap(List::stream).toList());

ChatClient chatClient = ChatClient.builder(openAiChatModel)
    .defaultTools(mcpToolProvider)
    .build();
```

## Dependencies

The project requires the following Spring AI dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## Configuration

### Application Properties

The application is configured through `application.properties`:

```properties
spring.application.name=mcp
spring.main.web-application-type=none

# Disable the chat client auto-configuration because we are using multiple chat models
spring.ai.chat.client.enabled=false

# API keys for LLM providers
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}

# MCP server connection
spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080

# Logging configuration
logging.level.io.modelcontextprotocol.client=WARN
logging.level.io.modelcontextprotocol.spec=WARN
```

## How It Works

The application demonstrates MCP Sampling with a weather-related query:

1. The client connects to the MCP Weather Server
2. It sends a weather-related question: "What is the weather in Amsterdam right now?"
3. The server retrieves the weather data and sends sampling requests to the client
4. The client routes each request to the appropriate LLM based on model hints
5. Each LLM generates a creative poem about the weather
6. The server combines the responses and returns them to the user

## Running the Application

1. First, start the MCP Weather Server:
   ```bash
   cd ../mcp-weather-webmvc-server
   ./mvnw clean install -DskipTests
   java -jar target/mcp-sampling-weather-server-0.0.1-SNAPSHOT.jar
   ```

2. Set the required environment variables:
   ```bash
   export OPENAI_API_KEY=your-openai-key
   export ANTHROPIC_API_KEY=your-anthropic-key
   ```

3. Build and run the MCP Sampling Client:
   ```bash
   cd ../mcp-sampling-client
   ./mvnw clean install
   java -Dai.user.input='What is the weather in Amsterdam right now?' -jar target/mcp-sampling-client-0.0.1-SNAPSHOT.jar
   ```

The application will:
1. Connect to the MCP Weather Server
2. Process the weather query
3. Handle sampling requests from the server
4. Route each request to the appropriate LLM
5. Display the combined creative responses

## Sample Output

When you run the application, you'll see output similar to:

```
> USER: What is the weather in Amsterdam right now?
Please incorporate all creative responses from all LLM providers.
After the other providers add a poem that synthesizes the the poems from all the other providers.

> ASSISTANT: I checked the current weather in Amsterdam for you. Here are the creative responses from different AI providers:

OpenAI poem about the weather:
# Amsterdam's Embrace

In Amsterdam, where canals reflect the sky,
A gentle warmth of sixteen degrees goes by.
The autumn air, a crisp and tender touch,
Caresses faces of the Dutch.

Time stands still at this perfect hour,
As sunshine breaks through with gentle power.
The city breathes with calm delight,
In this moment, everything feels right.

Anthropic poem about the weather:
## Amsterdam Today

Sixteen degrees in Amsterdam's embrace,
A perfect autumn day unfolds with grace.
The canals reflect the passing clouds above,
As the city hums with life and love.

Neither cold nor warm, but just between,
The perfect weather for this Dutch scene.
Time captured in this moment's gentle hold,
As Amsterdam's story continues to unfold.

My synthesis of these weather poems:
# Amsterdam's Gentle Harmony

Where canals mirror skies in Dutch design,
Sixteen degrees - a temperature divine.
Neither cold nor warm, but perfectly between,
The autumn air paints Amsterdam's scene.

Time seems suspended in this golden hour,
As gentle sunshine shows its subtle power.
The city breathes, alive with calm delight,
In this moment, everything feels just right.

Amsterdam's story, continuing to unfold,
Embraces all within its gentle hold.
A perfect day that poets can't ignore,
In this beautiful city we all adore.

Current weather data shows the temperature is 16°C in Amsterdam right now.
```

## Additional Resources

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
* [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
