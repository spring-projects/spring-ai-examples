# Spring AI - Model Context Protocol (MCP) Brave Search Chatbot

This example demonstrates how to build an interactive chatbot that combines Spring AI's Model Context Protocol (MCP) with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search). The application creates a persistent chatbot that maintains conversation history using Spring AI's Memory Advisor, allowing for contextual interactions across multiple exchanges. Users can engage in an ongoing conversation with the bot, which can perform internet searches through Brave Search to provide up-to-date information. The chatbot runs continuously until terminated with Ctrl-C, maintaining conversational state throughout the session.

The application is powered by Anthropic's Claude AI model and can perform internet searches through Brave Search, enabling natural language interactions with real-time web data while maintaining context of the conversation.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- npx package manager
- Anthropic API key (Claude) (Get one at https://docs.anthropic.com/en/docs/initial-setup)
- Brave Search API key (Get one at https://brave.com/search/api/)

## Setup

1. Install npx (Node Package eXecute):
   First, make sure to install [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
   and then run:
   ```bash
   npm install -g npx
   ```

2. Clone the repository:
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd spring-ai-examples/model-context-protocol/web-search/brave-chatbot
   ```

3. Set up your API keys:
   ```bash
   export ANTHROPIC_API_KEY='your-anthropic-api-key-here'
   export BRAVE_API_KEY='your-brave-api-key-here'
   ```

4. Build the application:
   ```bash
   ./mvnw clean install
   ```

## Running the Application

Run the application using Maven:
```bash
./mvnw spring-boot:run
```

The application will start an interactive chat session where you can ask questions. The chatbot will use Brave Search when it needs to find information from the internet to answer your queries.

## How it Works

The application integrates Spring AI with the Brave Search MCP server through several components:

### MCP Client Configuration

1. Required dependencies in pom.xml:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>
```

2. Application properties (application.yml):
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        name: brave-search-client
        version: 1.0.0
        type: SYNC  # or ASYNC for reactive applications
        request-timeout: 20s
        stdio:
          root-change-notification: true
          servers-configuration: classpath:/mcp-servers-config.json
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
```

3. MCP Server Configuration (mcp-servers-config.json):
```json
{
  "mcpServers": {
    "brave-search": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-brave-search"
      ],
      "env": {
        "BRAVE_API_KEY": "${BRAVE_API_KEY}"
      }
    }
  }
}
```

### Client Types

The MCP client supports two types of implementations:

- **Synchronous (default)**: Uses blocking operations, suitable for traditional request-response patterns
- **Asynchronous**: Uses non-blocking operations, suitable for reactive applications

You can switch between these types using the `spring.ai.mcp.client.type` property (SYNC or ASYNC).

### Chat Implementation

The chatbot is implemented using Spring AI's ChatClient with MCP tool integration:

```java
var chatClient = chatClientBuilder
    .defaultSystem("You are a useful assistant, expert in AI and Java.")
    .defaultTools((Object[]) mcpToolAdapter.toolCallbacks())
    .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
    .build();
```

Key features:
- Uses Claude AI model for natural language understanding
- Integrates Brave Search through MCP for real-time web search capabilities
- Maintains conversation memory using InMemoryChatMemory
- Runs as an interactive command-line application

The chatbot can:
- Answer questions using its built-in knowledge
- Perform web searches when needed using Brave Search
- Remember context from previous messages in the conversation
- Combine information from multiple sources to provide comprehensive answers

### Advanced Configuration

The MCP client supports additional configuration options:

- Client customization through `McpSyncClientCustomizer` or `McpAsyncClientCustomizer`
- Multiple transport types: STDIO and SSE (Server-Sent Events)
- Integration with Spring AI's tool execution framework
- Automatic client initialization and lifecycle management

For WebFlux-based applications, you can use the WebFlux starter instead:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-webflux-spring-boot-starter</artifactId>
</dependency>
```

This provides similar functionality but uses a WebFlux-based SSE transport implementation, recommended for production deployments.
