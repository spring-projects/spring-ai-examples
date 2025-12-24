# Spring AI - MCP Starter Streamable HTTP Client

This project demonstrates how to use the Spring AI MCP (Model Context Protocol) Client Boot Starter with streamable HTTP transport in a Spring Boot application. It showcases how to connect to MCP servers via streamable HTTP connections and integrate them with Spring AI's tool execution framework.

Follow the [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) reference documentation.

## Overview

The project uses Spring Boot 3.4.5 and Spring AI 1.1.0-SNAPSHOT to create a command-line application that demonstrates MCP server integration. The application:
- Connects to MCP servers using streamable HTTP transport
- Integrates with Spring AI's chat capabilities
- Demonstrates tool execution through MCP servers
- Takes a user-defined question via the `-Dai.user.input` command-line property, which is mapped to a Spring `@Value` annotation in the code

For example, running the application with `-Dai.user.input="Does Spring AI support MCP?"` will inject this question into the application through Spring's property injection, and the application will use it to query the MCP server.

## Prerequisites

- Java 17 or later
- Maven 3.6+
- OpenAI API key (Get one at https://platform.openai.com/api-keys)
- A running MCP server accessible via HTTP (e.g., Chrome MCP server)

## Dependencies

The project uses the following main dependencies:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>
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
spring.ai.openai.api-key=${OPENAI_API_KEY}

# Enable the MCP client tool-callback auto-configuration
spring.ai.mcp.client.toolcallback.enabled=true
```

#### Streamable HTTP Transport Properties

Follow the [Streamable HTTP Configuration properties](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) documentation.

Configure a separate, named configuration for each streamable HTTP server you connect to:

```properties
spring.ai.mcp.client.streamable.connections.chrome.url=http://127.0.0.1:12306
spring.ai.mcp.client.streamable.connections.chrome.endpoint=/mcp
```

Here, `chrome` is the name of your connection. The configuration specifies:
- `url`: The base URL of the MCP server
- `endpoint`: The specific endpoint path for MCP communication


## How It Works

The application demonstrates a simple command-line interaction with an AI model using MCP tools:

1. The application starts and configures MCP Clients using streamable HTTP transport connections
2. It builds a ChatClient with the configured MCP tools
3. Sends a predefined question (set via the `ai.user.input` property) to the AI model
4. Displays the AI's response
5. Automatically closes the application

## Running the Application

1. Set the required environment variables:
   ```bash
   export OPENAI_API_KEY=your-openai-api-key
   ```

2. Build the application:
   ```bash   
   ./mvnw clean install
   ```

3. Run the application:
   ```bash   
   # Run with the default question from application.properties
   java -jar target/mcp-starter-streamable-http-client-0.0.1-SNAPSHOT.jar

   # Or specify a custom question
   java -Dai.user.input='What tools are available?' -jar target/mcp-starter-streamable-http-client-0.0.1-SNAPSHOT.jar
   ```

The application will execute the question, use the configured MCP tools to answer it, and display the AI assistant's response.

## Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
