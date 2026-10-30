# Tasks server, MODERN (migrated from STREAMABLE)

`spring.ai.mcp.server.protocol=MODERN` with the session-bound tools rewritten as multi round-trip requests.

`InteractiveTaskProvider.deleteTask` returns an `InputRequiredResult` carrying an elicitation form and the task id as `requestState`; the client retries with `inputResponses`, and the second invocation reads the answer through `ctx.inputResponse("confirm", ElicitResponse.class)`.
`summarizeTasks` does the same with `createMessage`.
`deleteTask` also declares the SDK `McpSyncNotifier` and reports progress through it, which makes its response a short `text/event-stream`; `summarizeTasks` declares only the context and answers with plain JSON.
A client that does not declare form elicitation in its request `_meta` gets JSON-RPC error `-32021` instead of the question.
Client logging has no equivalent and is replaced by server-side logging.

`AdminController` adds an `McpSyncTool` at runtime through `InMemorySyncToolRepository.add`, which broadcasts `notifications/tools/list_changed` to `subscriptions/listen` streams.
It resolves the repository through `ObjectProvider`, so that the repository is created after the annotated tools were scanned.

Drive it with [modern-client](../modern-client/). Compare with [streamable-server](../streamable-server/).

Part of [modern-migration](../README.md). Run with `./mvnw -o spring-boot:run`; the server listens on `http://localhost:8080`.
