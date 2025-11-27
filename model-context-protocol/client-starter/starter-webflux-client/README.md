# Spring AI - MCP Starter WebFlux Client

This project demonstrates how to use the Spring AI MCP (Model Context Protocol) Client Boot Starter with WebFlux in a Spring Boot application. It showcases how to connect to MCP servers and integrate them with Spring AI's tool execution framework.

Follow the [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) reference documentation.

## Overview

The project uses Spring Boot 4.0.0 and Spring AI 2.0.0-SNAPSHOT to create a command-line application that demonstrates MCP server integration with WebFlux. The application:
- Connects to MCP servers using STDIO and/or SSE (WebFlux-based) transports
- Integrates with Spring AI's chat capabilities
- Demonstrates tool execution through MCP servers
- Takes a user-defined question via the `-Dai.user.input` command-line property, which is mapped to a Spring `@Value` annotation in the code

For example, running the application with `-Dai.user.input="What tools are available?"` will inject this question into the application through Spring's property injection, and the application will use it to query the MCP server using WebFlux's reactive programming model.

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Anthropic API key (Claude) (Get one at https://docs.anthropic.com/en/docs/initial-setup)
- OpenAI API key (optional, commented out by default) (Get one at https://platform.openai.com/api-keys)
- Brave Search API key (for the Brave Search MCP server) (Get one at https://brave.com/search/api/)

## Dependencies

The project uses the following main dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-anthropic</artifactId>
    </dependency>
    <!-- OpenAI dependency is commented out by default but can be enabled -->
    <!-- <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency> -->
</dependencies>
```

## Configuration

### Application Properties

Check the [MCP Client configuration properties](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html#_configuration_properties) documentation.

The application can be configured through `application.properties` or `application.yml`:

#### Common Properties
```properties
# Application Configuration
spring.application.name=mcp
spring.main.web-application-type=none

# AI Provider Configuration
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Enable the MCP client tool-callback auto-configuration
spring.ai.mcp.client.toolcallback.enabled=true
```

#### STDIO Transport Properties

Follow the [STDIO Configuration properties](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html#_stdio_transport_properties) documentation.

Configure a separate, named configuration for each STDIO server you connect to:

```properties
spring.ai.mcp.client.stdio.connections.brave-search.command=npx
spring.ai.mcp.client.stdio.connections.brave-search.args=-y,@modelcontextprotocol/server-brave-search
```

Here, `brave-search` is the name of your connection.

Alternatively, you can configure STDIO connections using an external JSON file in the Claude Desktop format:

```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json
```

Example `mcp-servers-config.json`:

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

#### SSE Transport Properties

You can also connect to Server-Sent Events (SSE) servers using WebFlux.
Follow the [SSE Configuration properties](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html#_sse_transport_properties) documentation.

The properties for SSE transport are prefixed with `spring.ai.mcp.client.sse`:

```properties
spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080
spring.ai.mcp.client.sse.connections.server2.url=http://localhost:8081
```

## How It Works

The application demonstrates a simple command-line interaction with an AI model using MCP tools:

1. The application starts and configures multiple MCP Clients (one for each provided STDIO or SSE connection configuration)
2. It builds a ChatClient with the configured MCP tools
3. Sends a predefined question (set via the `ai.user.input` property) to the AI model
4. Displays the AI's response
5. Automatically closes the application

## Running the Application

1. Set the required environment variables:
   ```bash
   export ANTHROPIC_API_KEY=your-api-key
   # If using OpenAI (uncomment the dependency in pom.xml first)
   # export OPENAI_API_KEY=your-openai-api-key
   # For the Brave Search MCP server
   export BRAVE_API_KEY=your-brave-api-key
   ```

2. Build the application:
   ```bash   
   ./mvnw clean install
   ```

3. Run the application:
   ```bash   
   # Run with the default question from application.properties
   java -jar target/mcp-starter-webflux-client-0.0.1-SNAPSHOT.jar
   
   # Or specify a custom question
   java -Dai.user.input='Does Spring AI support MCP?' -jar target/mcp-starter-webflux-client-0.0.1-SNAPSHOT.jar
   ```

The application will execute the question, use the configured MCP tools to answer it, and display the AI assistant's response.

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
