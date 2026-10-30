# Migrating Spring AI MCP servers to the 2026-07-28 ("modern") protocol

These samples show the same small task application on three MCP protocols, what changes when a session-based server is moved to the modern one, and how a modern server customises the repositories that serve its tools and resources.

**Versions:** Spring Boot `4.1.1` · Spring AI `2.1.0-SNAPSHOT` (branch `modern-mcp-server-2`) · MCP Java SDK `2.2.0-SNAPSHOT`

IMPORTANT: The modern server integration is not released yet.
Build the SDK `2.2.0-SNAPSHOT` and the Spring AI `modern-mcp-server-2` branch into your local Maven repository first (`./mvnw install -DskipTests` in both checkouts), and run the samples with `./mvnw -o` so that a newer remote snapshot of Spring AI `main` does not replace the local build.

## Modules

| Module | Protocol | What it shows |
|---|---|---|
| [stateless-server](stateless-server/) | `STATELESS` | `@McpTool`, `@McpResource` (static and template) and `@McpPrompt` on a session-less Streamable HTTP server |
| [streamable-server](streamable-server/) | `STREAMABLE` | The same, plus tools that use the session: client logging, progress, elicitation, sampling, and a runtime tool addition that triggers `tools/list_changed` |
| [modern-server-from-stateless](modern-server-from-stateless/) | `MODERN` | The stateless server after migration: a property change and two annotation attributes (`ttlMs`, `cacheScope`) |
| [modern-server-from-streamable](modern-server-from-streamable/) | `MODERN` | The streamable server after migration: elicitation and sampling become multi round-trip requests, logging goes server-side, runtime changes go through the tool repository and `subscriptions/listen` |
| [modern-server-repositories](modern-server-repositories/) | `MODERN` | Customising the repositories: a programmatic `List<McpSyncTool>` bean, a wrapping tool repository that filters per caller, and a resource repository backed by the domain that turns Spring events into `resources/updated` notifications; also an `@McpIcon` and an argument `title` in the definitions |
| [legacy-client](legacy-client/) | Streamable HTTP | Spring AI MCP client with `@McpElicitation`, `@McpSampling`, `@McpLogging`, `@McpProgress` and `@McpToolListChanged` handlers; works against both legacy servers |
| [modern-client](modern-client/) | 2026-07-28 | A minimal JSON-RPC driver (there is no modern Java client in the SDK yet) plus curl scripts: `server/discover`, headers, per-request client capabilities, the `input_required` retry loop, `subscriptions/listen` |

All servers listen on `http://localhost:8080`, so run one at a time.

## Running the pairs

```bash
# 1. stateless
(cd stateless-server && ./mvnw -o spring-boot:run)          # terminal 1
(cd legacy-client    && ./mvnw -o spring-boot:run)          # terminal 2

# 2. stateful streamable: watch the handler log lines for logging, progress, elicitation, sampling, list_changed
(cd streamable-server && ./mvnw -o spring-boot:run)
(cd legacy-client     && ./mvnw -o spring-boot:run)

# 3. modern, from stateless: same calls, now with ttlMs/cacheScope on lists and the resource
(cd modern-server-from-stateless && ./mvnw -o spring-boot:run)
(cd modern-client                && ./mvnw -o spring-boot:run)

# 4. modern, from streamable: watch the "input_required round" lines, the -32021 error and the listen events
(cd modern-server-from-streamable && ./mvnw -o spring-boot:run)
(cd modern-client                 && ./mvnw -o spring-boot:run)

# 5. modern, custom repositories: per-caller tool lists, paged resources, resources/updated events
(cd modern-server-repositories && ./mvnw -o spring-boot:run)
(cd modern-client              && ./mvnw -o spring-boot:run)
```

The modern client runs a common walk against every server and then the scenario that the server supports, detected from its tool list.
Quick checks without Java: `modern-client/scripts/discover.sh`, `tools-list.sh`, `tools-call.sh add_task '{"title":"x"}'` and `listen.sh`.
`MCP_HEADER='X-Role: admin'` adds a header and `MCP_CAPABILITIES='{}'` changes the declared client capabilities.

## What changes in the migration

### Stateless to modern: configuration only

```diff
-spring.ai.mcp.server.protocol=STATELESS
-spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp
+spring.ai.mcp.server.protocol=MODERN
+spring.ai.mcp.server.modern.mcp-endpoint=/mcp
+spring.ai.mcp.server.modern.list-ttl=30s
+spring.ai.mcp.server.modern.discover-ttl=5m
+spring.ai.mcp.server.modern.page-size=50
```

```diff
-@McpResource(uri = "tasks://all", mimeType = "application/json")
+@McpResource(uri = "tasks://all", mimeType = "application/json", ttlMs = 30_000, cacheScope = CacheScope.PUBLIC)
```

`TaskProvider` is otherwise identical in the stateless, streamable and both migrated servers.

### Streamable to modern: rewrite the session-bound methods

| Streamable (`InteractiveTaskProvider`) | Modern (`InteractiveTaskProvider`) |
|---|---|
| `McpSyncRequestContext ctx` | `McpModernSyncRequestContext ctx` |
| `ctx.info("...")` to the client | `logger.info(...)` on the server; client logging is deprecated in the 2026-07-28 revision and not exposed |
| `ctx.progress(p -> p.percentage(10))` | declare the SDK `McpSyncNotifier` and call `notifier.progress(10, 100.0, "...")`; that parameter, and only that one, makes the response a short SSE stream |
| `ctx.elicit(e -> e.message(...), Confirmation.class)` blocks until the client answers | return `InputRequiredResult.builder().elicit("confirm", form).requestState(id).build()`; on the retry read `ctx.inputResponse("confirm", ElicitResponse.class)` |
| `ctx.sample(s -> s.message(...))` | return `InputRequiredResult.builder().createMessage("summary", request).build()`; on the retry read `ctx.inputResponse("summary", Map.class)` |
| returns `String` | returns `CallToolOutcome` (a `CallToolResult` or an `InputRequiredResult`) |
| `McpSyncServer.addTool(SyncToolSpecification)` notifies every session | `InMemorySyncToolRepository.add(McpSyncTool.of(tool, handler))` broadcasts to every `subscriptions/listen` stream |

On the client side the `@McpElicitation` and `@McpSampling` handlers disappear.
The client answers the `inputRequests` of an `input_required` result and retries the same call with `inputResponses` and the sealed `requestState`; see `ModernMcpDriver.callTool`.
There is no handshake either: the client declares its capabilities in `_meta` of every request, and a tool that asks for input the request did not declare is refused with JSON-RPC error `-32021 Missing required client capability`.

### Programmatic specifications and runtime changes

A modern server has no `McpSyncServer` and no `McpServerFeatures.Sync*Specification` records.
Tools, prompts and resources are served by repositories, and the Spring AI integration builds the default in-memory repositories from list beans:

| Session-based server | Modern server |
|---|---|
| `@Bean List<McpServerFeatures.SyncToolSpecification>` | `@Bean List<McpSyncTool>`; the SDK type is a `Tool` definition, optional required client capabilities, and a handler |
| `@Bean List<SyncPromptSpecification>` | `@Bean List<McpSyncPrompt>` |
| `@Bean List<SyncResourceSpecification>` / `SyncResourceTemplateSpecification` | `@Bean List<SyncResourceEntry>` / `List<SyncResourceTemplateEntry>` (`of(definition, handler)`) |
| `McpSyncServer.addTool/removeTool/notifyToolsListChanged` | `InMemorySyncToolRepository.add/remove`, resolved through `ObjectProvider` |
| `McpSyncServer.notifyResourcesUpdated` | `InMemorySyncResourceRepository.resourceUpdated(uri)`, or `McpChangeBroadcaster.broadcast(new ServerChange.ResourceUpdated(uri))` from a custom repository |
| a different tool list per caller | not available; on the modern server, a repository bean that wraps the in-memory one |

The annotation scanner produces the same list beans, so annotated and programmatic primitives end up in the same repository.

## Customising the repositories

[modern-server-repositories](modern-server-repositories/) shows the two ways to replace a repository.
Defining any bean of an SDK repository type (`McpSyncToolRepository`, `McpSyncResourceRepository`, `McpSyncPromptRepository`, `McpSyncCompletionRepository`) makes the auto-configured in-memory repository for that primitive back off.

* **Delegation** - `RoleFilteringRepositoryConfiguration` builds the in-memory tool repository from the `List<McpSyncTool>` beans and wraps it. Callers without `X-Role: admin` do not see the `admin_*` tools in `tools/list`, and calling one answers `-32602 Unknown tool`. Use this when the catalogue stays a fixed list and only its visibility depends on the caller.
* **Implementation** - `TaskResourceRepository` implements `McpSyncResourceRepository` directly. Every task is a resource, listed from the domain service on each request and paged by task id; nothing is registered, added or removed. Task changes arrive as Spring events and leave as `resources/list_changed` and `resources/updated` notifications. Use this when the catalogue lives in a database, a remote service or the domain model.

## Wire format, for the curious

Every modern request is a POST with `Mcp-Method` and `MCP-Protocol-Version` headers, plus `Mcp-Name` for `tools/call`, `prompts/get` and `resources/read`, and `_meta.io.modelcontextprotocol/protocolVersion` and `clientCapabilities` in the body.
There is no `initialize`: `server/discover` is a cacheable request.
Headers that disagree with the body are rejected with JSON-RPC error `-32020`.

## Things to notice

- The stateless server silently skips the two interactive tools: the legacy client prints three tools against it and six against the streamable server.
- `tools/list` on the modern servers carries `ttlMs` and `cacheScope`; `resources/read tasks://all` carries the values from the annotation.
- Both admin controllers resolve the server or repository through `ObjectProvider`, and the wrapping repository is a `@Bean` method that reads the list beans through `ObjectProvider`. Injecting either directly into a component can create it before the annotated beans are scanned, and the server starts with no annotated tools.
- The response shape is the method's choice: `delete_task` declares `McpSyncNotifier` and answers over `text/event-stream`, with its progress notifications before the `input_required` result; `summarize_tasks` declares only the context and answers with plain JSON. The driver reads the last SSE frame as the response.
- The required client capabilities are checked before a programmatic tool's handler runs (`McpSyncTool.of(tool, requiredCapabilities, handler)`), and when an annotated method returns an `InputRequiredResult`.
- A filtered tool list depends on the caller, so the wrapping repository returns it with `cacheScope: private`.
