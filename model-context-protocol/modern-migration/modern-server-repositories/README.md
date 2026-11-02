# Tickets server, MODERN (custom repositories)

`spring.ai.mcp.server.protocol=MODERN` with two of the four repositories replaced by application beans.
The sample shows how a modern server customises what it serves, which a session-based server could only do by adding and removing specifications on `McpSyncServer`.

* **Programmatic tools** - `AdminToolsConfiguration` defines a `List<McpSyncTool>` bean, the same contract the annotation scanner uses. `admin_purge_resolved` is a multi round-trip tool written against the SDK directly: it declares form elicitation as a required client capability (`McpSyncTool.of(tool, requiredCapabilities, handler)`, the programmatic form of the `requiredCapabilities` annotation attribute), asks for confirmation, and seals the ids it asked about into `requestState`.
* **Wrapping tool repository** - `RoleFilteringRepositoryConfiguration` builds the in-memory tool repository from the `List<McpSyncTool>` beans and wraps it. The `admin_*` tools are visible and callable only for requests with the `X-Role: admin` header, which reaches the repository because `application.properties` names it in `spring.ai.mcp.server.modern.context-headers`; nothing else from the request does.
* **Custom resource repository** - `TicketResourceRepository` implements `McpSyncResourceRepository`. It lists one resource per ticket from `TicketService`, pages the list two at a time, serves the `tickets://{id}` template, and turns `TicketChanged` Spring events into `resources/list_changed` and `resources/updated` notifications on `subscriptions/listen`.

* **Definition attributes** - `TicketProvider` puts an `@McpIcon` on `open_ticket` and a display `title` on the `owner` argument of `daily-summary`. They are annotation attributes, not repository features, and appear in `tools/list` and `prompts/list` as they would on any server kind.

The prompt repository and the completion repository stay auto-configured.
Because the server defines its own resource repository, `@McpResource` methods would not be served, so `TicketProvider` has none.

NOTE: `spring.ai.mcp.server.modern.page-size` is left unset.
The wrapper filters a page after the in-memory repository has paged it, so with a page size an anonymous caller could receive a short or empty page with a `nextCursor`.

Drive it with [modern-client](../modern-client/), or with curl:

```bash
../modern-client/scripts/tools-list.sh                                  # three tools
MCP_HEADER='X-Role: admin' ../modern-client/scripts/tools-list.sh       # five tools
MCP_HEADER='X-Role: admin' MCP_CAPABILITIES='{}' \
  ../modern-client/scripts/tools-call.sh admin_purge_resolved '{}'          # -32021
../modern-client/scripts/mcp.sh resources/list '{}'                     # nextCursor=t-2
../modern-client/scripts/mcp.sh subscriptions/listen \
  '{"notifications":{"resourcesListChanged":true,"resourceSubscriptions":["tickets://t-1"]}}'
```

Part of [modern-migration](../README.md). Run with `./mvnw -o -llr spring-boot:run`; the server listens on `http://localhost:8080`.
