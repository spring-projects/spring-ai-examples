# Tasks server, STATELESS

`spring.ai.mcp.server.protocol=STATELESS`: every POST is independent, no `Mcp-Session-Id`.

`TaskProvider` exposes `list_tasks`, `add_task`, `complete_task`, the resources `tasks://all` and `tasks://{id}`, and the prompt `daily-summary`. Nothing here needs the session, which is why the same class runs unchanged on every protocol.

Drive it with [legacy-client](../legacy-client/).

Part of [modern-migration](../README.md). Run with `./mvnw -o spring-boot:run`; the server listens on `http://localhost:8080`.
