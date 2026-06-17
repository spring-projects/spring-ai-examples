# Spring AI MCP Sampling

Demonstrates MCP Sampling — a capability that lets an MCP server delegate LLM requests back to the client, enabling multi-model workflows within a single interaction.

## Projects

- **[mcp-sampling-server](./mcp-sampling-server)**: MCP weather server that delegates poem generation to the client via sampling
- **[mcp-sampling-client](./mcp-sampling-client)**: MCP client that routes sampling requests to OpenAI or Anthropic based on model hints

## How It Works

1. The client connects to the MCP Weather Server and registers a sampling handler
2. The user sends a weather query; the server fetches weather data from Open-Meteo
3. The server sends two sampling requests back to the client — one hinted for `openai`, one for `anthropic`
4. The client routes each request to the matching `ChatClient` and returns the generated poem
5. The server combines both poems with the raw weather data and returns the result

![MCP Sampling Sequence Diagram](./mvc-sampling-sq.svg)

## Server Implementation

```java
McpToolUtils.getMcpExchange(toolContext).ifPresent(exchange -> {

    exchange.loggingNotification(LoggingMessageNotification.builder(LoggingLevel.INFO, "Start sampling").build());

    if (exchange.getClientCapabilities().sampling() != null) {
        var messageRequestBuilder = McpSchema.CreateMessageRequest
            .builder(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER,
                TextContent.builder("Please write a poem about this weather forecast (temperature is in Celsius). Use markdown format :\n "
                    + JacksonUtils.getDefaultJsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(weatherResponse))
                    .build())), 2000)
            .systemPrompt("You are a poet!");

        CreateMessageResult openAiResponse = exchange.createMessage(
            messageRequestBuilder.modelPreferences(ModelPreferences.builder().addHint("openai").build()).build());

        CreateMessageResult anthropicResponse = exchange.createMessage(
            messageRequestBuilder.modelPreferences(ModelPreferences.builder().addHint("anthropic").build()).build());
    }

    exchange.loggingNotification(LoggingMessageNotification.builder(LoggingLevel.INFO, "Finish Sampling").build());
});
```

## Client Implementation

```java
@Bean
McpClientCustomizer<McpClient.SyncSpec> samplingCustomizer(Map<String, ChatClient> chatClients) {
    return (name, mcpClientSpec) -> {
        mcpClientSpec = mcpClientSpec.loggingConsumer(msg ->
            System.out.println("MCP LOGGING: [" + msg.level() + "] " + msg.data()));

        mcpClientSpec.sampling(llmRequest -> {
            var userPrompt = ((TextContent) llmRequest.messages().get(0).content()).text();
            String modelHint = llmRequest.modelPreferences().hints().get(0).name();

            ChatClient chatClient = chatClients.entrySet().stream()
                .filter(e -> e.getKey().contains(modelHint))
                .findFirst().orElseThrow().getValue();

            String response = chatClient.prompt()
                .system(llmRequest.systemPrompt())
                .user(userPrompt)
                .call().content();

            return CreateMessageResult.builder(Role.ASSISTANT, TextContent.builder(response).build(), modelHint).build();
        });
    };
}
```

> **Note**: Disable MCP tool callback auto-configuration to avoid cyclic dependencies:
> `spring.ai.mcp.client.toolcallback.enabled=false`

## Running

**Prerequisites**: Java 17+, Maven 3.6+, `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`

```bash
# Terminal 1 — start the server
cd mcp-sampling-server
./mvnw clean package -DskipTests
java -jar target/mcp-sampling-server-0.0.1-SNAPSHOT.jar

# Terminal 2 — run the client
export OPENAI_API_KEY=your-openai-key
export ANTHROPIC_API_KEY=your-anthropic-key
cd mcp-sampling-client
./mvnw clean package
java -Dai.user.input='What is the weather in Amsterdam right now?' -jar target/mcp-sampling-client-0.0.1-SNAPSHOT.jar
```

## Resources

* [Spring AI MCP Client Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
* [Spring AI MCP Server Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
