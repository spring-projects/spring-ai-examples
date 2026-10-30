#!/usr/bin/env bash
# Usage: tools-call.sh <tool> '<arguments-json>'   e.g. ./tools-call.sh add_task '{"title":"Buy milk"}'
# A streaming tool answers with text/event-stream; an input_required result must be retried with
# inputResponses and requestState (see ModernMcpDriver for the loop).
TOOL=$1; ARGS=${2:-"{}"}
exec "$(dirname "$0")/mcp.sh" tools/call "{\"name\":\"$TOOL\",\"arguments\":$ARGS}" "$TOOL"
