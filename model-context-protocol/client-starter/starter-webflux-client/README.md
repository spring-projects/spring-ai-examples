# Spring AI - MCP Starter WebFlux Client

A command-line Spring Boot app demonstrating the [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html) with WebFlux (reactive) transport.

**Stack:** Spring Boot 4.1.1 · Spring AI 2.1.0-SNAPSHOT · Java 17

## How It Works

1. Connects to one or more MCP servers via STDIO or SSE transports
2. Registers all MCP tools with a `ChatClient`
3. Sends a question (configured via `ai.user.input`) to Claude
4. Prints the response and exits

## Prerequisites

- Java 17+
- Node.js / `npx` (for the Brave Search MCP server)
- `ANTHROPIC_API_KEY`
- `BRAVE_API_KEY`

## Configuration

The app uses `spring.ai.mcp.client.stdio.connections.<name>.*` properties to define STDIO servers:

```properties
spring.ai.mcp.client.stdio.connections.brave-search.command=npx
spring.ai.mcp.client.stdio.connections.brave-search.args=-y,@modelcontextprotocol/server-brave-search

spring.ai.mcp.client.toolcallback.enabled=true
ai.user.input=What tools are available?
```

Alternatively, point to a Claude Desktop–style JSON file:

```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json
```

For SSE servers (WebFlux transport):

```properties
spring.ai.mcp.client.sse.connections.my-server.url=http://localhost:8080
```

## Run

```bash
export ANTHROPIC_API_KEY=your-key
export BRAVE_API_KEY=your-key

./mvnw spring-boot:run

# Custom question
./mvnw spring-boot:run -Dai.user.input="Does Spring AI support MCP?"
```

Or via the packaged jar:

```bash
./mvnw clean package
java -Dai.user.input="What tools are available?" -jar target/mcp-starter-webflux-client-0.0.1-SNAPSHOT.jar
```
