# Tickets server, MODERN (migrated from STATELESS)

`spring.ai.mcp.server.protocol=MODERN`: the 2026-07-28 protocol, one POST endpoint, `server/discover` instead of `initialize`.

The Java code is the [stateless-server](../stateless-server/) code plus `ttlMs` and `cacheScope` on the `tickets://all` resource. The properties add list and discover cache hints and a page size.

Drive it with [modern-client](../modern-client/) or the curl scripts in `modern-client/scripts`.

Part of [modern-migration](../README.md). Run with `./mvnw spring-boot:run`; the server listens on `http://localhost:8080`.
