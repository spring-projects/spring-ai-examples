# Migrating Spring AI MCP servers and clients to the 2026-07-28 ("modern") protocol

These samples show the same small ticket application on three MCP protocols, what changes when a session-based server is moved to the modern one, how a modern server customises the repositories that serve its tools and resources, the same server written against the asynchronous API type, and the Spring AI client after its migration.

**Versions:** Spring Boot `4.1.1` · Spring AI `2.1.0-SNAPSHOT` · MCP Java SDK `2.2.0`

IMPORTANT: The modern server shipped in Spring AI 2.1.0-RC2, and spring-projects/spring-ai#7349 added `spring.ai.mcp.server.type=ASYNC` and moved completions onto prompts and resource templates.
The modern connections of the Spring AI client, which [modern-client-from-legacy](modern-client-from-legacy/) uses, were added by spring-projects/spring-ai#7385 on top of the modern client of the MCP Java SDK `2.2.0`.
Both are in the Spring AI `2.1.0-SNAPSHOT` builds, which the samples take from the Spring snapshot repository.

## Modules

| Module | Protocol | What it shows |
|---|---|---|
| [stateless-server](stateless-server/) | `STATELESS` | `@McpTool`, `@McpResource` (static and template) and `@McpPrompt` on a session-less Streamable HTTP server |
| [streamable-server](streamable-server/) | `STREAMABLE` | The same, plus tools that use the session: client logging, progress, elicitation, sampling, and a runtime tool addition that triggers `tools/list_changed` |
| [modern-server-from-stateless](modern-server-from-stateless/) | `MODERN` | The stateless server after migration: a property change and two annotation attributes (`ttlMs`, `cacheScope`) |
| [modern-server-from-streamable](modern-server-from-streamable/) | `MODERN` | The streamable server after migration: elicitation and sampling become multi round-trip requests, logging goes server-side, runtime changes go through the tool repository and `subscriptions/listen` |
| [modern-server-repositories](modern-server-repositories/) | `MODERN` | Customising the repositories: a programmatic `List<McpSyncTool>` bean, a wrapping tool repository that filters per caller, and a resource repository backed by the domain that turns Spring events into `resources/updated` notifications and completes its template; also an `@McpIcon` and an argument `title` in the definitions |
| [modern-server-async](modern-server-async/) | `MODERN`, `type=ASYNC` | The streamable migration written against the asynchronous API type on WebFlux: `Mono`, `CompletableFuture` and plain returns, `McpAsyncNotifier` progress, and runtime changes through `InMemoryAsyncToolRepository` |
| [legacy-client](legacy-client/) | Streamable HTTP | Spring AI MCP client with `@McpElicitation`, `@McpSampling`, `@McpLogging`, `@McpProgress` and `@McpToolListChanged` handlers; works against both legacy servers |
| [modern-client](modern-client/) | 2026-07-28 | A minimal JSON-RPC driver that shows the wire format, plus curl scripts: `server/discover`, headers, per-request client capabilities, the `input_required` retry loop, `completion/complete`, `subscriptions/listen` |
| [modern-client-from-legacy](modern-client-from-legacy/) | 2026-07-28, optionally with a legacy connection | The legacy client after migration, on the Spring AI client starter: a `protocol=MODERN` connection whose tools arrive as tool callbacks, a client for each caller, the unchanged `@McpElicitation`, `@McpProgress` and `@McpToolListChanged` methods, a listen stream, a tool filter, an optional scope with one client for each role, and a legacy and a modern server behind one provider |

All servers listen on `http://localhost:8080`, so run one at a time.

## Running the pairs

```bash
# 1. stateless
(cd stateless-server && ./mvnw spring-boot:run)          # terminal 1
(cd legacy-client    && ./mvnw spring-boot:run)          # terminal 2

# 2. stateful streamable: watch the handler log lines for logging, progress, elicitation, sampling, list_changed
(cd streamable-server && ./mvnw spring-boot:run)
(cd legacy-client     && ./mvnw spring-boot:run)

# 3. modern, from stateless: same calls, now with ttlMs/cacheScope on lists and the resource
(cd modern-server-from-stateless && ./mvnw spring-boot:run)
(cd modern-client                && ./mvnw spring-boot:run)

# 4. modern, from streamable: watch the "input_required round" lines, the -32021 error and the listen events
(cd modern-server-from-streamable && ./mvnw spring-boot:run)
(cd modern-client                 && ./mvnw spring-boot:run)

# 5. modern, custom repositories: per-caller tool lists, paged resources, resources/updated events
(cd modern-server-repositories && ./mvnw spring-boot:run)
(cd modern-client              && ./mvnw spring-boot:run)

# 6. modern, asynchronous API type on WebFlux: the output of pair 4, plus count_open_tickets
(cd modern-server-async && ./mvnw spring-boot:run)
(cd modern-client       && ./mvnw spring-boot:run)

# 7. any modern server with the migrated Spring AI client: tool callbacks, and the scenario the server supports
(cd modern-server-repositories && ./mvnw spring-boot:run)
(cd modern-client-from-legacy  && ./mvnw spring-boot:run)

# 8. a legacy and a modern server behind one tool callback provider
(cd stateless-server             && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081)
(cd modern-server-from-stateless && ./mvnw spring-boot:run)
(cd modern-client-from-legacy    && ./mvnw spring-boot:run -Dspring-boot.run.profiles=mixed)
```

Both modern clients run a common walk against every server and then the scenario that the server supports, detected from its tool list.
Quick checks without Java: `modern-client/scripts/discover.sh`, `tools-list.sh`, `tools-call.sh open_ticket '{"title":"x"}'` and `listen.sh`.
`MCP_HEADER='X-Role: admin'` adds a header and `MCP_CAPABILITIES='{}'` changes the declared client capabilities.

## What changes in the migration

### Stateless to modern: configuration only

```diff
-spring.ai.mcp.server.protocol=STATELESS
-spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp
+spring.ai.mcp.server.protocol=MODERN
+spring.ai.mcp.server.modern.mcp-endpoint=/mcp
+spring.ai.mcp.server.modern.list-ttl=30s
+spring.ai.mcp.server.modern.list-cache-scope=PRIVATE
+spring.ai.mcp.server.modern.discover-ttl=5m
+spring.ai.mcp.server.modern.page-size=50
```

```diff
-@McpResource(uri = "tickets://all", mimeType = "application/json")
+@McpResource(uri = "tickets://all", mimeType = "application/json", ttlMs = 30_000, cacheScope = CacheScope.PUBLIC)
```

`TicketProvider` is otherwise identical in the stateless, streamable and both migrated servers.

### Streamable to modern: rewrite the session-bound methods

| Streamable (`InteractiveTicketProvider`) | Modern (`InteractiveTicketProvider`) |
|---|---|
| `McpSyncRequestContext ctx` | `McpModernSyncRequestContext ctx` |
| `ctx.info("...")` to the client | `logger.info(...)` on the server; client logging is deprecated in the 2026-07-28 revision and not exposed |
| `ctx.progress(p -> p.percentage(10))` | declare the SDK `McpSyncNotifier` and call `notifier.progress(10, 100.0, "...")`; that parameter, and only that one, makes the response a short SSE stream |
| `ctx.elicit(e -> e.message(...), Confirmation.class)` blocks until the client answers | return `InputRequiredResult.builder().elicit("confirm", form).requestState(id).build()`; on the retry read `ctx.inputResponse("confirm", ElicitResponse.class)` and the ticket id from `ctx.requestState()` |
| `ctx.sample(s -> s.message(...))` | return `InputRequiredResult.builder().createMessage("summary", request).build()`; on the retry read `ctx.inputResponse("summary", Map.class)` |
| returns `String` | returns `CallToolOutcome` (a `CallToolResult` or an `InputRequiredResult`) |
| `ctx.elicit` and `ctx.sample` check the capabilities the client sent at `initialize` | `@McpTool(requiredCapabilities = McpClientCapability.ELICITATION_FORM)` declares what the tool cannot work without |
| `McpSyncServer.addTool(SyncToolSpecification)` notifies every session | `InMemorySyncToolRepository.add(McpSyncTool.of(tool, handler))` broadcasts to every `subscriptions/listen` stream |

On the wire the server no longer sends elicitation and sampling requests to the client.
The client answers the `inputRequests` of an `input_required` result and retries the same call with `inputResponses` and the sealed `requestState`; see `ModernMcpDriver.callTool`.
The modern client of the MCP Java SDK runs that loop itself and calls the `@McpElicitation` methods of a Spring AI client for the answers, see [Legacy client to modern](#legacy-client-to-modern-a-property-and-the-handlers-stay).
There is no handshake either: the client declares its capabilities in `_meta` of every request, and a tool whose `requiredCapabilities` the request did not declare is refused with JSON-RPC error `-32021 Missing required client capability` before the method runs.

### Synchronous to asynchronous: the API type

[modern-server-async](modern-server-async/) is [modern-server-from-streamable](modern-server-from-streamable/) with `spring.ai.mcp.server.type=ASYNC` on the WebFlux starter.
The tools, resources, prompt and completion are the same; only the programming model changes.

| Sync (`modern-server-from-streamable`) | Async (`modern-server-async`) |
|---|---|
| `spring-ai-starter-mcp-server-webmvc`, `type=SYNC` | `spring-ai-starter-mcp-server-webflux`, `type=ASYNC` |
| returns `String`, `CallToolOutcome` | returns `Mono<String>`, `Mono<CallToolOutcome>`, or a `CompletableFuture`; a plain return still works and runs on `boundedElastic` |
| `McpModernSyncRequestContext ctx` | `McpModernRequestContext ctx`, the same accessors for both API types |
| `McpSyncNotifier notifier`, `notifier.progress(...)` | `McpAsyncNotifier notifier`, `notifier.progress(...)` returns a `Mono<Void>` composed into the result |
| `InMemorySyncToolRepository.add(McpSyncTool.of(...))` | `InMemoryAsyncToolRepository.add(McpAsyncTool.of(...))`, the handler returns a `Mono` |

A method that returns a `Flux` is rejected at startup, because an MCP request has one result; return a `Mono` of a list instead.
On `type=SYNC` it is the other way round: methods with an asynchronous return type, `CompletableFuture` included, are skipped with a warning.

### Legacy client to modern: a property, and the handlers stay

[modern-client-from-legacy](modern-client-from-legacy/) is [legacy-client](legacy-client/) after migration.
The protocol is a property of the connection, so an application can keep legacy connections next to modern ones, and one `SyncMcpToolCallbackProvider` bean returns the tools of both.

```diff
-spring.ai.mcp.client.streamable-http.connections.server1.url=http://localhost:8080
+spring.ai.mcp.client.streamable-http.connections.tickets.url=http://localhost:8080
+spring.ai.mcp.client.streamable-http.connections.tickets.protocol=MODERN
+spring.ai.mcp.client.streamable-http.connections.tickets.listen=true
```

The connection is renamed because its name becomes the prefix of the tool names.
The annotated handler methods keep their annotations and their legacy schema types: one method serves the legacy and the modern connections that it names.

```diff
-@McpElicitation(clients = "server1")
-public StructuredElicitResult<Confirmation> confirm(McpSchema.ElicitRequest request) {
+@McpElicitation(clients = { "tickets", "legacy" })
+public StructuredElicitResult<Confirmation> confirm(McpSchema.ElicitRequest request, ToolContext toolContext) {
```

| Legacy connection (`legacy-client`) | Modern connection (`modern-client-from-legacy`) |
|---|---|
| the client runs `initialize` at startup | nothing is sent at startup, apart from the listen stream of a connection with `listen=true`; the first request goes out when the tools are first resolved |
| tools from the `SyncMcpToolCallbackProvider` bean, under their own names | tools from the same bean, named `<connection>_<tool>`, for example `tickets_open_ticket` |
| `List<McpSyncClient>` beans for resources, prompts and completions | `List<SyncModernMcpConnection>`; `connection.client()` returns the modern `McpSyncClient` of the SDK for the current request |
| one client for the application, with one session | `client-scope=PER_REQUEST` (the default) creates a client each time the tools are resolved, so each request carries its own caller; `client-scope=SHARED` creates one client; `client-scope=CUSTOM` asks a `SyncModernMcpClientScope` bean, for example for one client for each user |
| `@McpElicitation` | the same method; the SDK client calls it for the input request of the server and retries the tool call. It may take the `ToolContext` of that tool call, to know whom to ask |
| `@McpProgress` | the same method |
| `@McpToolListChanged` | the same method, for a connection with `listen=true`; the tools are listed first, as a modern server sends no list with the change |
| `@McpSampling` | not supported by the modern SDK client; the server refuses a tool that requires it with `-32021`, so a `McpModernToolFilter` bean hides it |
| `@McpLogging` | gone, as client logging is |
| `McpToolFilter`, `McpToolNamePrefixGenerator` | `McpModernToolFilter`, `McpModernToolNamePrefixGenerator`; an application with a `McpToolFilter` bean and a modern connection must define a `McpModernToolFilter` bean too |
| headers from a `McpSyncHttpClientRequestCustomizer` | headers from a `McpHttpRequestCustomizer` bean, which reads the caller from the `McpTransportContext` of the request |

Modern connections support only `spring.ai.mcp.client.type=SYNC`, and both client starters serve them with the JDK `HttpClient` transport of the SDK.

### Programmatic specifications and runtime changes

A modern server has no `McpSyncServer` and no `McpServerFeatures.Sync*Specification` records.
Tools, prompts and resources are served by repositories, and the Spring AI integration builds the default in-memory repositories from list beans:

| Session-based server | Modern server |
|---|---|
| `@Bean List<McpServerFeatures.SyncToolSpecification>` | `@Bean List<McpSyncTool>`; the SDK type is a `Tool` definition, optional required client capabilities, and a handler |
| `@Bean List<SyncPromptSpecification>` | `@Bean List<McpSyncPrompt>` |
| `@Bean List<SyncResourceSpecification>` / `SyncResourceTemplateSpecification` | `@Bean List<SyncResourceRegistration>` / `List<SyncResourceTemplateRegistration>` (`of(definition, handler)`) |
| `@Bean List<SyncCompletionSpecification>` | no list of its own: `withCompletionHandler(...)` on the `McpSyncPrompt` or `SyncResourceTemplateRegistration` it completes |
| `McpSyncServer.addTool/removeTool/notifyToolsListChanged` | `InMemorySyncToolRepository.add/remove`, resolved through `ObjectProvider` |
| `McpSyncServer.notifyResourcesUpdated` | `InMemorySyncResourceRepository.resourceUpdated(uri)`, or `McpChangeBroadcaster.broadcast(new ServerChange.ResourceUpdated(uri))` from a custom repository |
| a different tool list per caller | not available; on the modern server, a repository bean that wraps the in-memory one |

The annotation scanner produces the same list beans, so annotated and programmatic primitives end up in the same repository.
An annotated `@McpComplete` method is attached to the `@McpPrompt` or `@McpResource` template it names: `TicketProvider.completeOwner` completes the `owner` of the `daily-summary` prompt and `completeTicketId` the `id` of `tickets://{id}`, and the application fails to start if no annotated prompt or template has that name or URI.
On `type=ASYNC` the same contract uses the async twins: `List<McpAsyncTool>`, `List<McpAsyncPrompt>`, `List<AsyncResourceRegistration>` and `List<AsyncResourceTemplateRegistration>`.

## Customising the repositories

[modern-server-repositories](modern-server-repositories/) shows the two ways to replace a repository.
Defining any bean of an SDK repository type (`McpSyncToolRepository`, `McpSyncResourceRepository`, `McpSyncPromptRepository`, or an `McpAsync*Repository` twin) makes the auto-configured in-memory repository for that primitive back off.

* **Delegation** - `RoleFilteringRepositoryConfiguration` builds the in-memory tool repository from the `List<McpSyncTool>` beans and wraps it. Callers without `X-Role: admin` do not see the `admin_*` tools in `tools/list`, and calling one answers `-32602 Unknown tool`. Use this when the catalogue stays a fixed list and only its visibility depends on the caller.
* **Implementation** - `TicketResourceRepository` implements `McpSyncResourceRepository` directly. Every ticket is a resource, listed from the domain service on each request and paged by ticket id; nothing is registered, added or removed. Ticket changes arrive as Spring events and leave as `resources/list_changed` and `resources/updated` notifications. It also completes the `id` of its `tickets://{id}` template: `findTemplate` returns the template with a completion handler, and `supportsCompletion()` opts in; both are default methods, so a repository that leaves them out does not complete. Use this when the catalogue lives in a database, a remote service or the domain model.

## Wire format, for the curious

Every modern request is a POST with `Mcp-Method` and `MCP-Protocol-Version` headers, plus `Mcp-Name` for `tools/call`, `prompts/get` and `resources/read`, and `_meta.io.modelcontextprotocol/protocolVersion` and `clientCapabilities` in the body.
There is no `initialize`: `server/discover` is a cacheable request.
Headers that disagree with the body are rejected with JSON-RPC error `-32020`.

## Things to notice

- A STATELESS server skips methods that declare `McpSyncRequestContext` (the provider logs a warning), so `stateless-server` has no `InteractiveTicketProvider`: the legacy client prints three tools against it and five against the streamable server.
- `tools/list` on the modern servers carries `ttlMs` and `cacheScope`; `resources/read tickets://all` carries the values from the annotation.
- Both admin controllers resolve the server or repository through `ObjectProvider`, wrapped in a `SingletonSupplier` so the lookup happens once, on the first request; the wrapping repository is a `@Bean` method that reads the list beans through `ObjectProvider`. Injecting either directly into a component can create it before the annotated beans are scanned, and the server starts with no annotated tools.
- The response shape is the method's choice: `delete_ticket` declares `McpSyncNotifier` and answers over `text/event-stream`, with its progress notifications before the `input_required` result; `summarize_tickets` declares only the context and answers with plain JSON. The driver reads the last SSE frame as the response.
- The required client capabilities are checked before the handler runs, declared with `requiredCapabilities` on the annotation or with `McpSyncTool.of(tool, requiredCapabilities, handler)` on a programmatic tool. A method that asks for undeclared input without them is still refused with `-32021`, but only after it has run, and the server logs a warning recommending the attribute.
- A filtered tool list depends on the caller, so the wrapping repository returns it with `cacheScope: private`.
- `completion/complete` carries no `Mcp-Name` header. The server completes only the arguments that a prompt or template declares and answers `-32602` for any other, and a static resource such as `tickets://all` has nothing to complete.
- On `modern-server-async` the WebFlux transport never blocks an event loop: `Mono` methods run where they are subscribed, and `resolve_ticket`, which returns a plain `String`, runs on `boundedElastic`.
- The Spring AI client keeps a tool listing across requests only when the server allows it for every caller: a `ttlMs` above zero with `cacheScope: public`. The sample servers send a `ttlMs` with `private`, so `modern-client-from-legacy` lists the tools each time it resolves them, and the client logs once that the listing could be kept. Set `spring.ai.mcp.server.modern.list-cache-scope=PUBLIC` on a server whose tools are the same for every caller, or keep a client that outlives the request: `client-scope=SHARED` for an application with a single identity, or `client-scope=CUSTOM` for one client for each user.
- A connection with `listen=true` keeps a listen stream open, for the application and not for a caller. When the server reports a tool change, a kept listing is dropped at once and the `@McpToolListChanged` methods are called. They are also called when the stream is established again after it had ended, as the server may have changed in between.
- A tool callback of a modern connection is bound to the client it was resolved with. `modern-client-from-legacy` reads the caller when that client is built, so a call made later, or on another thread, still carries the caller's `X-Role` header.
- A modern server that cannot be reached does not fail the client: its tools are left out with one warning, and the server is not asked again for `spring.ai.mcp.client.modern.unreachable-cool-down` (30 seconds by default).
