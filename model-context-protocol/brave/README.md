# Spring AI - Model Context Protocol (MCP) Brave Search Example

This example demonstrates how to use the Spring AI Model Context Protocol (MCP) with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search). The application enables natural language interactions with Brave Search, allowing you to perform internet searches through a conversational interface.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- npx package manager
- Git
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
   cd model-context-protocol/brave
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

### MCP Client Setup

```java
@Bean(destroyMethod = "close")
public McpSyncClient mcpClient() {
    var stdioParams = ServerParameters.builder("npx")
            .args("-y", "@modelcontextprotocol/server-brave-search")
            .addEnvVar("BRAVE_API_KEY", System.getenv("BRAVE_API_KEY"))
            .build();

    var mcpClient = McpClient.sync(new StdioServerTransport(stdioParams));
    var init = mcpClient.initialize();
    return mcpClient;
}
```

The MCP client is configured to:
1. Use the Brave Search MCP server via npx
2. Pass the Brave API key from environment variables
3. Initialize a synchronous connection to the server

### Function Callbacks

The application automatically discovers and registers available Brave Search tools:

```java
List<McpFunctionCallback> functionCallbacks = mcpClient.listTools(null)
        .tools()
        .stream()
        .map(tool -> new McpFunctionCallback(mcpClient, tool))
        .toList();
```

These callbacks enable the ChatClient to:
- Access Brave Search tools during conversations
- Handle function calls requested by the AI model
- Execute search queries against the Brave Search API

### Chat Integration

The ChatClient is configured with the Brave Search function callbacks:

```java
var chatClient = chatClientBuilder
        .defaultFunctions(functionCallbacks.toArray(new McpFunctionCallback[0]))
        .build();
```

This setup allows the AI model to:
- Understand when to use Brave Search
- Format queries appropriately
- Process and incorporate search results into responses

## Dependencies

The project uses:
- Spring Boot 3.3.6
- Spring AI 1.0.0-M4
- Spring AI MCP Spring 0.1.0
- OpenAI Spring Boot Starter
