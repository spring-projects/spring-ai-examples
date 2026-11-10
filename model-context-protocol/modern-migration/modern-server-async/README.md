# Tickets server, MODERN with the asynchronous API type

`spring.ai.mcp.server.protocol=MODERN` and `spring.ai.mcp.server.type=ASYNC` on the WebFlux starter: [modern-server-from-streamable](../modern-server-from-streamable/) written against the asynchronous programming model.
The tools, resources, prompt and completion, and what the client sees, are the same; only the method signatures change.

* **Return types** - `TicketProvider` returns `Mono`s from most methods and a `CompletableFuture` from `count_open_tickets`; both are single values, resolved through Spring's `ReactiveAdapterRegistry`. `resolve_ticket` keeps a plain `String`, which the server runs on Reactor's `boundedElastic` scheduler, so it may block. A `Flux` is rejected at startup, because an MCP request has one result.
* **Multi round-trip and progress** - `InteractiveTicketProvider` is the streamable migration's, with `McpModernRequestContext` (the context for both API types) and the SDK `McpAsyncNotifier`, whose `progress(...)` returns a `Mono<Void>` that `delete_ticket` composes into its result. Declaring the notifier still makes the response a short `text/event-stream`.
* **Completion** - `TicketProvider.completeTicketId` returns a `Mono<List<String>>` and is attached to the `tickets://{id}` template it names.
* **Runtime changes** - `AdminController` adds an `McpAsyncTool` through `InMemoryAsyncToolRepository.add` (`POST /admin/tools/{name}`), which broadcasts `notifications/tools/list_changed` to `subscriptions/listen` streams.

On WebFlux the request is served without blocking an event loop; on the WebMvc starter the same code works, and the transport waits for the result on the container thread.

Drive it with [modern-client](../modern-client/). Compare with [modern-server-from-streamable](../modern-server-from-streamable/).

Part of [modern-migration](../README.md). Run with `./mvnw -o spring-boot:run`; the server listens on `http://localhost:8080`.
