# Spring AI - MCP Brave Search (Starter)

Demonstrates Spring AI's MCP client with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search) using Spring Boot auto-configuration. On startup it asks one predefined question, prints the answer, and exits.

Unlike the manual approach, the MCP client is fully auto-configured via `application.properties` and `mcp-servers-config.json` — no explicit bean definitions required.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17+
- Maven 3.6+
- npx (via [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm))
- Anthropic API key
- Brave Search API key — [get one here](https://brave.com/search/api/)

## Setup

```bash
npm install -g npx

git clone https://github.com/spring-projects/spring-ai-examples.git
cd spring-ai-examples/model-context-protocol/web-search/brave-starter

export ANTHROPIC_API_KEY='your-anthropic-api-key'
export BRAVE_API_KEY='your-brave-api-key'

./mvnw clean install
```

## Running

```bash
./mvnw spring-boot:run
```

## How It Works

**Dependencies** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-anthropic</artifactId>
</dependency>
```

**MCP configuration** — choose one approach:

Option A — inline in `application.properties`:
```properties
spring.ai.mcp.client.stdio.connections.brave-search.command=npx
spring.ai.mcp.client.stdio.connections.brave-search.args=-y,@modelcontextprotocol/server-brave-search
```

Option B — external `mcp-servers-config.json`:
```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json
```
```json
{
  "mcpServers": {
    "brave-search": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-brave-search"],
      "env": { "BRAVE_API_KEY": "${BRAVE_API_KEY}" }
    }
  }
}
```

**Application code**:
```java
@Bean
public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
        List<McpSyncClient> mcpSyncClients, ConfigurableApplicationContext context) {
    return args -> {
        var chatClient = chatClientBuilder
            .defaultTools(SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClients).build())
            .build();

        String question = "Does Spring AI support the Model Context Protocol? Please provide some references.";
        System.out.println("QUESTION: " + question);
        System.out.println("ASSISTANT: " + chatClient.prompt(question).call().content());

        context.close();
    };
}
```

## Versions

| Dependency | Version |
|---|---|
| Spring Boot | 4.0.7 |
| Spring AI | 2.0.0 |
