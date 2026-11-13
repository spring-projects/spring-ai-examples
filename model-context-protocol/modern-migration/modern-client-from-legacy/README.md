# Tickets client, 2026-07-28, on the Spring AI starter

[legacy-client](../legacy-client/) after migration: the Spring AI MCP client starter (`spring-ai-starter-mcp-client`) with a connection that speaks the modern protocol.
The migration is one property, `spring.ai.mcp.client.streamable-http.connections.tickets.protocol=MODERN`.
The annotated handler methods stay as they are, apart from the name of the connection.
Works against every modern server of this set.

The tools of the connection arrive as `ToolCallback`s from the `SyncMcpToolCallbackProvider` bean, the same bean that serves legacy connections.
An application with a model registers it with `ChatClient.builder(chatModel).defaultToolCallbacks(provider)`; this sample has no model and calls the callbacks itself.
`WireLog` prints every request the connection sends, as `[http] <method>` lines, and `ListenLog` prints what the listen stream reports, as `[listen]` lines.

`MigratedTicketsClientApplication` runs a common walk and then the scenarios that the server allows:

* **Every server** - resolve the tool callbacks (`tickets_open_ticket`, named `<connection>_<tool>`), call one, resolve them again to see whether the server was asked again, and use the SDK client of the connection for `server/discover`, a resource, the prompt and the completions.
* **modern-server-from-streamable** and **modern-server-async** - `tickets_delete_ticket`, whose elicitation and progress reach the `@McpElicitation` and `@McpProgress` methods of `ClientHandlers`, and a runtime tool addition that the listen stream reports to the `@McpToolListChanged` method. `summarize_tickets` needs sampling, which the modern SDK client does not support, so a `McpModernToolFilter` bean hides it.
* **modern-server-repositories** - the tool callbacks resolved for a caller with the `admin` role and for one without. Each resolution creates a client for its caller, `roleHeader` sends the role as `X-Role`, and the admin callbacks still call as admin after the role has left the thread.

The classes of the sample:

* **`ClientHandlers`** - the class of the legacy client with its `@McpElicitation`, `@McpProgress` and `@McpToolListChanged` methods, still on the legacy schema types. One method serves the legacy and the modern connections that it names. The elicitation method gained an optional `ToolContext` parameter, which is the context of the tool call that the elicitation belongs to. The `@McpSampling` and `@McpLogging` methods are gone, as the modern protocol has neither.
* **`ModernClientConfiguration`** - a `McpClientCustomizer<McpClient.SyncBuilder>` that binds the caller to each client, a `McpHttpRequestCustomizer` that adds the header, the tool filter, and a `SyncModernMcpClientScope` that keeps one client for each role.
* **`ListenLog`** - a `SyncModernMcpChangeListener` bean, which hears what the listen stream reports next to the tool callback provider and the annotated methods.

## The listen stream

`spring.ai.mcp.client.streamable-http.connections.tickets.listen=true` keeps a listen stream open to the server.
The stream is opened when the application starts, in the background, for the application and not for a caller: the request carries no `X-Role` header.
A reported tool change drops a kept tool listing at once and calls the `@McpToolListChanged` methods with the tools as the application sees them.
A stream that ends is opened again with growing pauses, and the methods are then called once more, as the server may have changed in between.

## Things to try

* **A legacy and a modern server together.** Run a legacy server on port 8081, a modern server on 8080, and the client with the `mixed` profile, which adds a connection without the `protocol` property. One provider returns `open_ticket` of the legacy server and `tickets_open_ticket` of the modern one, and the methods of `ClientHandlers` serve both connections.

  ```bash
  (cd ../stateless-server             && ./mvnw -o spring-boot:run -Dspring-boot.run.arguments=--server.port=8081)
  (cd ../modern-server-from-stateless && ./mvnw -o spring-boot:run)
  ./mvnw -o spring-boot:run -Dspring-boot.run.profiles=mixed
  ```

* **An unreachable modern server.** Run the `mixed` profile with only the legacy server up. The listen stream and the provider each log a warning, the `tickets_*` tools are left out, and the server is not asked for its tools again for the `unreachable-cool-down` time; the legacy tools stay usable.
* **A listing that could be kept.** The sample servers send their tool listings with a `ttlMs` but mark them as `private`, so only the client that listed them may keep them. With the default `PER_REQUEST` scope the second resolution sends `tools/list` again, and the client logs once that the listing could be kept.
* **A kept public listing.** Start a server with `-Dspring-boot.run.arguments=--spring.ai.mcp.server.modern.list-cache-scope=PUBLIC` and the provider keeps the listing for the server's `list-ttl`, for all callers: the second resolution sends nothing, while tool calls still go to the server.
* **A client for each role.** Set `spring.ai.mcp.client.streamable-http.connections.tickets.client-scope=CUSTOM` in `application.properties`. The `SyncModernMcpClientScope` bean of `ModernClientConfiguration` keeps one client for each role, and each client keeps the private listing of its role: the second resolution sends nothing, and against `modern-server-repositories` the admin still gets the admin tools.
* **A shared client.** Set `client-scope=SHARED` instead. One client serves every request and keeps even a private listing, which fits an application that calls the server under a single identity. Against `modern-server-repositories` the admin tools no longer appear, because the shared client was built at startup, without a caller.

Part of [modern-migration](../README.md). Run with `./mvnw -o spring-boot:run` while one of the modern servers is running.
