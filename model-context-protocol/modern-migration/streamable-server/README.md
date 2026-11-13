# Tickets server, STREAMABLE (stateful)

`spring.ai.mcp.server.protocol=STREAMABLE`: sessions over Streamable HTTP.

Adds `InteractiveTicketProvider`, whose tools call back into the client over the session: `delete_ticket` logs, reports progress and asks for confirmation with `ctx.elicit(...)`; `summarize_tickets` asks the client's model with `ctx.sample(...)`. `AdminController` adds a tool at runtime through `McpSyncServer.addTool`, which sends `notifications/tools/list_changed` to connected sessions (`POST /admin/tools/{name}`).

Drive it with [legacy-client](../legacy-client/). Compare with [modern-server-from-streamable](../modern-server-from-streamable/).

Part of [modern-migration](../README.md). Run with `./mvnw spring-boot:run`; the server listens on `http://localhost:8080`.
