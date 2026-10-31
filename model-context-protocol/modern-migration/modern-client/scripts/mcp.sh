#!/usr/bin/env bash
# Usage: mcp.sh <method> '<params-json>' [mcp-name]
# Sends one 2026-07-28 MCP request and prints the response (JSON or SSE frames).
#   ./mcp.sh tools/list '{}'
#   ./mcp.sh tools/call '{"name":"open_ticket","arguments":{"title":"Printer offline"}}' open_ticket
# Environment:
#   MCP_ENDPOINT      default http://localhost:8080/mcp
#   MCP_HEADER        one extra request header, e.g. 'X-Role: admin'
#   MCP_CAPABILITIES  client capabilities JSON for _meta, default '{"elicitation":{"form":{}},"sampling":{}}'
set -euo pipefail
METHOD=$1; PARAMS=${2:-"{}"}; NAME=${3:-}
ENDPOINT=${MCP_ENDPOINT:-http://localhost:8080/mcp}
CAPABILITIES=${MCP_CAPABILITIES:-'{"elicitation":{"form":{}},"sampling":{}}'}
BODY=$(python3 - "$METHOD" "$PARAMS" "$CAPABILITIES" <<'PY'
import json, sys
params = json.loads(sys.argv[2])
params["_meta"] = {"io.modelcontextprotocol/protocolVersion": "2026-07-28",
                   "io.modelcontextprotocol/clientCapabilities": json.loads(sys.argv[3])}
print(json.dumps({"jsonrpc": "2.0", "id": 1, "method": sys.argv[1], "params": params}))
PY
)
ARGS=(-s -N -X POST "$ENDPOINT" -H 'Content-Type: application/json' -H 'Accept: application/json, text/event-stream'
      -H "Mcp-Method: $METHOD" -H 'MCP-Protocol-Version: 2026-07-28')
[ -n "$NAME" ] && ARGS+=(-H "Mcp-Name: $NAME")
[ -n "${MCP_HEADER:-}" ] && ARGS+=(-H "$MCP_HEADER")
curl "${ARGS[@]}" -d "$BODY"; echo
