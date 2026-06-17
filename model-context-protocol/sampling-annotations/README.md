# MCP Sampling — Annotation-Based

Two-module example showing MCP Sampling with Spring AI annotations. The server exposes a weather tool that uses MCP Sampling to request poems from the client's LLMs; the client routes those sampling requests to OpenAI or Anthropic based on model hints.

```
mcp-sampling-server-annotations/   # MCP server — weather tool + sampling
mcp-sampling-client-annotations/   # MCP client — sampling handler + chat entry point
```

## How it works

1. The client sends: *"What is the weather in Amsterdam right now?"*
2. The server's `@McpTool` fetches live weather from Open-Meteo, then calls `exchange.createMessage()` twice — once hinting `openai`, once hinting `anthropic`.
3. The client's `@McpSampling` handler receives each request, picks the matching `ChatClient`, and returns a poem.
4. The server combines both poems with the raw weather data and returns the result to the client.

## Key annotations

| Annotation | Side | Purpose |
|---|---|---|
| `@McpTool` | Server | Registers a method as an MCP tool |
| `@McpSampling` | Client | Handles sampling requests from the server |
| `@McpLogging` | Client | Receives log notifications from the server |
| `@McpProgress` | Client | Receives progress notifications from the server |

## Prerequisites

- Java 17+
- OpenAI API key
- Anthropic API key

## Run

**1. Start the server**

```bash
cd mcp-sampling-server-annotations
./mvnw spring-boot:run
```

Starts on `http://localhost:8080` using Streamable HTTP transport.

**2. Set API keys**

```bash
export OPENAI_API_KEY=your-openai-key
export ANTHROPIC_API_KEY=your-anthropic-key
```

**3. Run the client**

```bash
cd mcp-sampling-client-annotations
./mvnw spring-boot:run
```

## Configuration

**Server** (`application.properties`):
```properties
spring.ai.mcp.server.name=mcp-sampling-server-annotations
spring.ai.mcp.server.version=0.0.1
spring.ai.mcp.server.protocol=STREAMABLE
spring.main.banner-mode=off
```

**Client** (`application.properties`):
```properties
spring.main.web-application-type=none
spring.ai.chat.client.enabled=false
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080
```

## Key implementation snippets

**Server — weather tool with sampling:**
```java
@Service
public class WeatherService {

    @McpTool(description = "Get the temperature (in celsius) for a specific location")
    public String getTemperature2(McpSyncServerExchange exchange,
            @McpToolParam(description = "The location latitude") double latitude,
            @McpToolParam(description = "The location longitude") double longitude) {

        // fetch weather, then request poems via MCP Sampling
        var request = McpSchema.CreateMessageRequest
            .builder(messages, 1000)
            .systemPrompt("You are a poet!")
            .modelPreferences(ModelPreferences.builder().addHint("openai").build())
            .build();
        CreateMessageResult result = exchange.createMessage(request);
        // ...
    }
}
```

**Client — sampling handler:**
```java
@Service
public class McpClientHandlers {

    @McpSampling(clients = "server1")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        var userPrompt = ((TextContent) llmRequest.messages().get(0).content()).text();
        String modelHint = llmRequest.modelPreferences().hints().get(0).name();

        ChatClient hintedChatClient = chatClients.entrySet().stream()
            .filter(e -> e.getKey().contains(modelHint))
            .findFirst().orElseThrow().getValue();

        String response = hintedChatClient.prompt()
            .system(llmRequest.systemPrompt()).user(userPrompt).call().content();

        return CreateMessageResult.builder(Role.ASSISTANT, TextContent.builder(response).build(), "any").build();
    }
}
```

**Client — entry point:**
```java
@Bean
public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
        ToolCallbackProvider toolCallbackProvider) {
    return args -> {
        ChatClient chatClient = ChatClient.builder(openAiChatModel)
            .defaultTools(toolCallbackProvider).build();
        System.out.println(chatClient.prompt("""
            What is the weather in Amsterdam right now?
            Please incorporate all creative responses from all LLM providers.
            After the other providers add a poem that synthesizes the poems from all the other providers.
            """).call().content());
    };
}
```
