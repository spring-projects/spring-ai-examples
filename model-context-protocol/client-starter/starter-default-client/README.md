# MCP Starter Default Client

Spring Boot 4 / Spring AI 2.0 command-line app showing the simplest possible MCP client setup using the [MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html).

The application connects to a Brave Search MCP server over STDIO, injects all discovered tools into a `ChatClient`, sends one question, prints the answer, and exits.

## Prerequisites

- Java 25+
- Node.js / `npx` (for the Brave Search MCP server)
- `ANTHROPIC_API_KEY`
- `BRAVE_API_KEY`

## Key dependencies

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

## Configuration

`application.properties` wires the Brave Search server via STDIO and enables tool callbacks:

```properties
spring.ai.mcp.client.stdio.connections.brave-search.command=npx
spring.ai.mcp.client.stdio.connections.brave-search.args=-y,@modelcontextprotocol/server-brave-search

spring.ai.mcp.client.toolcallback.enabled=true

ai.user.input=What tools are available?
```

Alternatively, point to a Claude Desktop–style JSON config file:

```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config.json
```

SSE (HTTP) connections are supported too:

```properties
spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080
```

## Running

```bash
export ANTHROPIC_API_KEY=<your-key>
export BRAVE_API_KEY=<your-key>

./mvnw spring-boot:run

# Or with a custom question
./mvnw spring-boot:run -Dai.user.input="Does Spring AI support MCP?"
```

## How it works

`ToolCallbackProvider` is auto-configured by the starter and exposes all MCP tools as Spring AI tool callbacks. The application injects it directly into a `ChatClient`:

```java
var chatClient = chatClientBuilder.defaultTools(tools).build();
System.out.println(chatClient.prompt(userInput).call().content());
```

## Resources

- [MCP Client Boot Starter docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
- [Spring AI reference](https://docs.spring.io/spring-ai/reference/)
- [MCP specification](https://modelcontextprotocol.io/specification)
