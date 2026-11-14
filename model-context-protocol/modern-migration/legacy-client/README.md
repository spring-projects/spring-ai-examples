# Tickets client, Streamable HTTP

Spring AI MCP client (`spring-ai-starter-mcp-client-webflux`) configured for `http://localhost:8080`. Works against [stateless-server](../stateless-server/) and [streamable-server](../streamable-server/).

`ClientHandlers` answers the server-initiated requests of the streamable server: `@McpElicitation` accepts the confirmation, `@McpSampling` returns a canned summary, `@McpLogging` and `@McpProgress` print what arrives, `@McpToolListChanged` prints the new tool list after the runtime addition. The runner calls the interactive tools only when the server lists them.

Part of [modern-migration](../README.md). Run with `./mvnw spring-boot:run`; the server listens on `http://localhost:8080`.
