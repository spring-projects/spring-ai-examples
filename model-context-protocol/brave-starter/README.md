# Spring AI - Model Context Protocol (MCP) Brave Search Example

This example demonstrates how to use the Spring AI Model Context Protocol (MCP) with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search). The application enables natural language interactions with Brave Search, allowing you to perform internet searches through a conversational interface.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- npx package manager
- OpenAI API key
- Brave Search API key

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
   cd model-context-protocol/brave-starter
   ```

3. Set up your API keys:
   ```bash
   export OPENAI_API_KEY='your-openai-api-key-here'
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

The application will demonstrate the integration by asking a sample question about Spring AI and Model Context Protocol, utilizing Brave Search to gather information.

## How it Works

The application integrates Spring AI with the Brave Search MCP server through several components:

### MCP Client Autoconfiguration

1. Add the Spring AI MCP boot-starter to your POM:

```xml
<dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-mcp-spring-boot-starter</artifactId>
</dependency>
```

2. Application properties:

```
spring.ai.mcp.client.stdio.enabled=true
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json
```

The `servers-configuration` should point to Anthropic Desktop-compatible JSON configuration like:

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
      }
    }
  }
}
```

The MCP auto-configuration will use the `servers-configuration` to initialize MCP client connection to the provided servers and create an `McpToolAdapter` bean, providing access to the MCP tools exposed by the configured MCP servers.

### Chat Integration

The ChatClient is configured with the Brave Search function callbacks:

```java
var chatClient = chatClientBuilder
        .defaultTools(mcpToolAdapter.toolCallbacks())
        .build();
```

This setup allows the AI model to do the following:
- Understand when to use Brave Search
- Format queries appropriately
- Process and incorporate search results into responses
