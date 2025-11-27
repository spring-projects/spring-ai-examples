# Spring AI - Model Context Protocol (MCP) Brave Search Example

This example demonstrates how to create a Spring AI Model Context Protocol (MCP) client that communicates with the [Brave Search MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search). The application shows how to build an MCP client that enables natural language interactions with Brave Search, allowing you to perform internet searches through a conversational interface. This example uses Spring Boot autoconfiguration to set up the MCP client through configuration files.

When run, the application demonstrates the MCP client's capabilities by asking a specific question: "Does Spring AI support the Model Context Protocol? Please provide some references." The MCP client uses Brave Search to find relevant information and returns a comprehensive answer. After providing the response, the application exits.

<img src="spring-ai-mcp-brave.jpg" width="600"/>

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker Desktop 4.41 or later
- Git
- OpenAI API key
- Brave Search API key (Get one at https://brave.com/search/api/)

## Setup

1.  **Start the Dockerized Services:**
    Make sure Docker (e.g., Docker Desktop 4.41 or later, or Docker Engine with Docker Compose CLI) is installed and running. Then, in the project's root directory (where `compose.yml` is located), start the necessary services:
    ```bash
    docker compose up
    ```

2. Clone the repository:
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd model-context-protocol/brave-docker-agents-gateway
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

The application will execute a single query asking about Spring AI's support for the Model Context Protocol. It uses the Brave Search MCP server to search the internet for relevant information, processes the results through the MCP client, and provides a detailed response before exiting.

## How it Works

The application integrates Spring AI with the Brave Search MCP server through Spring Boot autoconfiguration:

### MCP Client Configuration

The MCP client is configured using configuration files:

1. `application.properties`:
```properties
spring.ai.mcp.client.see.gateway.url=http://localhost:8811
```

This configuration:
1. Uses the Brave Search MCP server via Docker MCP Gateway
2. The Brave API key is passed from environment variables
3. Initializes a synchronous connection to the server

### Chat Integration

The ChatClient is configured with the MCP tool callbacks in the Application class:

```java
var chatClient = chatClientBuilder
        .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
        .build();
```

This setup allows the AI model to:
- Understand when to use Brave Search
- Format queries appropriately
- Process and incorporate search results into responses

## Dependencies

The project uses:
- Spring Boot 4.0.0
- Spring AI 2.0.0-SNAPSHOT
- spring-ai-starter-model-openai
- spring-ai-starter-mcp-client
