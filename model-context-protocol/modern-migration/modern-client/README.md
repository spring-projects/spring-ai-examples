# Tickets client, 2026-07-28

There is no modern MCP Java client in the SDK yet, so `ModernMcpDriver` shows the wire format directly with the JDK `HttpClient`.
It covers `_meta` with the protocol version and the client capabilities, the `Mcp-Method`, `MCP-Protocol-Version` and `Mcp-Name` headers, JSON and SSE responses, the `input_required` retry loop (`callTool`), `completion/complete` (`complete`), and a closeable `subscriptions/listen` reader.
`withHeader` and `withCapabilities` derive drivers that send an extra header or declare other capabilities.

`ModernTicketsClientApplication` runs a common walk against any modern tickets server, including the completion of a ticket id where the server has the `tickets://{id}` template, then the scenarios that the server's tools allow:

* **modern-server-async** - `count_open_tickets`, a `CompletableFuture` on the server, followed by the interactive scenario below.
* **modern-server-from-streamable** and **modern-server-async** - `delete_ticket` with elicitation and progress, the same call without client capabilities (`-32021`), `summarize_tickets` with sampling and from a client without it (`-32021`), and a runtime tool addition seen on `subscriptions/listen`.
* **modern-server-repositories** - the icon and argument title in the definitions, the tool list with and without `X-Role: admin`, a hidden tool answered as unknown, resource paging with `nextCursor`, `resources/updated` and `resources/list_changed` events, and the programmatic `admin_purge_resolved` with and without capabilities.

`scripts/` holds curl equivalents: `discover.sh`, `tools-list.sh`, `tools-call.sh <tool> '<json>'`, `listen.sh` and the generic `mcp.sh <method> '<params>' [name]`.
Point them elsewhere with `MCP_ENDPOINT=http://host:port/mcp`, add a header with `MCP_HEADER='X-Role: admin'`, and change the declared capabilities with `MCP_CAPABILITIES='{}'`.

Part of [modern-migration](../README.md). Run with `./mvnw -o spring-boot:run` while one of the modern servers is running.
