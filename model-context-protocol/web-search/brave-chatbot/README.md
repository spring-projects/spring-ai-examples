# Spring AI - MCP Brave Search Chatbot

Interactive chatbot combining Spring AI's Model Context Protocol (MCP) with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search). Maintains conversation history via `MessageChatMemoryAdvisor` with an explicit conversation ID, and performs real-time web searches through Brave Search.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17+
- Maven 3.6+
- npx (via [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm))
- Anthropic API key — [get one here](https://docs.anthropic.com/en/docs/initial-setup)
- Brave Search API key — [get one here](https://brave.com/search/api/)

## Setup

```bash
npm install -g npx

git clone https://github.com/spring-projects/spring-ai-examples.git
cd spring-ai-examples/model-context-protocol/web-search/brave-chatbot

export ANTHROPIC_API_KEY='your-anthropic-api-key'
export BRAVE_API_KEY='your-brave-api-key'

./mvnw clean install
```

## Running

```bash
./mvnw spring-boot:run
```

An interactive chat session starts. Ask anything — the bot uses Brave Search for real-time web data and remembers prior turns in the session.

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

**MCP server config** (`mcp-servers-config.json`):
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

**Chat client setup**:
```java
var chatClient = chatClientBuilder
    .defaultSystem("You are a useful assistant that can perform web searches via Brave's search API.")
    .defaultTools(SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClients).build())
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())
    .build();

var conversationId = UUID.randomUUID().toString();

chatClient.prompt(userInput)
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
    .call()
    .content();
```

Key points:
- Uses `SyncMcpToolCallbackProvider.builder()` (Spring AI 2.0.0 API)
- A stable `conversationId` keeps memory scoped to the current session
- Runs until Ctrl-C

## Versions

| Dependency | Version |
|---|---|
| Spring Boot | 4.0.7 |
| Spring AI | 2.0.0 |
