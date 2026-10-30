#!/usr/bin/env bash
# Opens subscriptions/listen and prints events until interrupted. In another shell run:
#   curl -X POST http://localhost:8080/admin/tools/hello
exec "$(dirname "$0")/mcp.sh" subscriptions/listen '{"notifications":{"toolsListChanged":true}}'
