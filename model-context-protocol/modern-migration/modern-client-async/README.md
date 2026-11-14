# Tickets client, 2026-07-28, on the Spring AI starter, asynchronous

[modern-client-from-legacy](../modern-client-from-legacy/) with the asynchronous client type, `spring.ai.mcp.client.type=ASYNC`.
The connection, the listen stream and the tool filter are configured the same way.
What changes is how the caller reaches the server, and that the handler methods return a `Mono`.
Works against every modern server of this set.

The tools of the connection arrive as `ToolCallback`s from the `AsyncMcpToolCallbackProvider` bean, the same bean that serves legacy asynchronous clients.
An application with a model registers it with `ChatClient.builder(chatModel).defaultToolCallbacks(provider)`; this sample has no model and calls the callbacks itself.
`WireLog` prints every request the connection sends, as `[http] <method>` lines, and `ListenLog` prints what the listen stream reports, as `[listen]` lines.

`AsyncTicketsClientApplication` runs a common walk and then the scenarios that the server allows:

* **Every server** - resolve the tool callbacks (`tickets_open_ticket`, named `<connection>_<tool>`), call one, and resolve them again to see whether the server was asked again. The asynchronous SDK client of the connection then sends `server/discover`, a resource read, the prompt and the completions as one reactive pipeline, at the same time.
* **Every server** - resolve the tool callbacks on a thread that may not block, `Schedulers.parallel()`, which fails at once with a message that says what to do, and then on `Schedulers.boundedElastic()`, which works.
* **modern-server-from-streamable** and **modern-server-async** - `tickets_delete_ticket`, whose elicitation and progress reach the reactive `@McpElicitation` and `@McpProgress` methods of `ClientHandlers`, and a runtime tool addition that the listen stream reports to the reactive `@McpToolListChanged` method.
* **modern-server-repositories** - the tool callbacks resolved for a caller with the `admin` role and for one without. The caller provider bean reads the role when the tools are resolved, `roleHeader` sends it as `X-Role`, and the admin callbacks still call as admin after the role has left the thread.

The classes of the sample:

* **`ClientHandlers`** - the methods of the legacy client, still on the legacy schema types, but each returns a `Mono`. With the asynchronous client type only reactive methods are called, and a synchronous method that names a connection is reported with a warning at startup. A connection has one `@McpElicitation` method with this type.
* **`ModernClientConfiguration`** - an `AsyncModernMcpTransportContextProvider` that supplies the caller of each request, a `McpHttpRequestCustomizer` that adds the header, the tool filter, and an `AsyncModernMcpClientScope` that keeps one client for each role.
* **`ListenLog`** - an `AsyncModernMcpChangeListener` bean, which hears what the listen stream reports next to the tool callback provider and the annotated methods. Its methods return a `Mono`, and the next listener is told when it completes.

## What changes from the synchronous client

| Synchronous client type | Asynchronous client type |
|---|---|
| `SyncMcpToolCallbackProvider` | `AsyncMcpToolCallbackProvider` |
| `McpClientCustomizer<McpClient.SyncBuilder>` that binds the caller to each client | `AsyncModernMcpTransportContextProvider` bean, asked when the tools are resolved |
| `SyncModernMcpClientScope` | `AsyncModernMcpClientScope` |
| `SyncModernMcpChangeListener` | `AsyncModernMcpChangeListener` |
| Handler methods return their result, or `void` | Handler methods return `Mono<...>`, or `Mono<Void>` |

`McpModernToolFilter`, `McpModernToolNamePrefixGenerator` and the `McpHttpRequestCustomizer` beans serve both types.

## The caller of a request

An asynchronous client holds no caller, so the `AsyncModernMcpTransportContextProvider` bean supplies it.
It is asked each time the tools are resolved, on the thread that resolves them, which a `ChatClient` does once for each request.
The tool listing goes out for that caller, and the tool callbacks keep it, so a tool call that runs later, or on the thread of a streaming response, is still made for that caller.

The bean is the only source of the caller.
A `McpTransportContext` in the Reactor context of the chat call is not used, because it may belong to someone else, such as the request that an MCP server of the same application is serving.

## Blocking calls

The tool callback contract is blocking with either client type: resolving the tools lists them from the server, and a tool call waits for the result.
A reactive application must therefore start its chat call on a thread that may block:

```java
Flux<String> answer = Flux.defer(() -> chatClient.prompt(question).stream().content())
    .subscribeOn(Schedulers.boundedElastic());
```

On a non-blocking thread, such as an event loop of WebFlux, resolving the tools fails at once with an `IllegalStateException`, and nothing is sent.

## The listen stream

`spring.ai.mcp.client.streamable-http.connections.tickets.listen=true` keeps a listen stream open to the server, as in the synchronous sample.
The stream is opened when the application starts, in the background, for the application and not for a caller: the caller provider bean is not asked, and the request carries no `X-Role` header.
A reported tool change drops a kept tool listing at once and calls the reactive `@McpToolListChanged` methods with the tools as the application sees them.
The reports of a connection are told in the order they arrive, so a listener or a method whose `Mono` never completes holds up the later ones: give a call that may hang a timeout.

## Things to try

* **A legacy and a modern server together.** Run a legacy server on port 8081, a modern server on 8080, and the client with the `mixed` profile, which adds a connection without the `protocol` property. With `type=ASYNC` the legacy connection gets the legacy asynchronous client, one provider returns `open_ticket` of the legacy server and `tickets_open_ticket` of the modern one, and the methods of `ClientHandlers` serve both connections.

  ```bash
  (cd ../stateless-server             && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081)
  (cd ../modern-server-from-stateless && ./mvnw spring-boot:run)
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=mixed
  ```

* **An unreachable modern server.** Run the `mixed` profile with only the legacy server up. The listen stream and the provider each log a warning, the `tickets_*` tools are left out, and the server is not asked for its tools again for the `unreachable-cool-down` time; the legacy tools stay usable.
* **A client for each role.** Set `spring.ai.mcp.client.streamable-http.connections.tickets.client-scope=CUSTOM` in `application.properties`. The `AsyncModernMcpClientScope` bean keeps one client for each role, and each client keeps the private listing of its role: the second resolution sends nothing, and against `modern-server-repositories` the admin still gets the admin tools.
* **A shared client.** Set `client-scope=SHARED` instead. A warning is logged at startup, because a shared client keeps a private tool listing for every caller, whoever the caller provider bean returns. Against `modern-server-repositories` the admin tools no longer appear: the listing kept from the first request, which had no role, serves the admin as well.

Part of [modern-migration](../README.md). Run with `./mvnw spring-boot:run` while one of the modern servers is running.
