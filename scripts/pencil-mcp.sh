#!/bin/sh
# Talk to the local OpenPencil MCP server over its HTTP transport.
#
# Delta's MCP client cannot reach a localhost server here, but the server speaks
# streamable HTTP on http://127.0.0.1:7600/mcp, so plain curl works.
# Verified against open-pencil 0.16.0 (MCP protocol 2025-06-18).
#
#   scripts/pencil-mcp.sh tools
#   scripts/pencil-mcp.sh call list_documents
#   scripts/pencil-mcp.sh call query_nodes '{"page_id":"0:3","selector":"//FRAME[@width=412]"}'
#   scripts/pencil-mcp.sh image /tmp/home.png export_image '{"ids":["0:1"],"maxEdge":500}'
#
# Env: PENCIL_MCP_URL (default http://127.0.0.1:7600/mcp), PENCIL_TIMEOUT (seconds, default 120)
set -eu

URL=${PENCIL_MCP_URL:-http://127.0.0.1:7600/mcp}
TIMEOUT=${PENCIL_TIMEOUT:-120}
hdr=$(mktemp)
body=$(mktemp)

# Streamable HTTP sessions are capped by the server ("Too many active MCP
# sessions"); every run must terminate the session it opened.
cleanup() {
  if [ -n "${SESSION:-}" ]; then
    curl -sS -m 5 -o /dev/null -X DELETE "$URL" -H "mcp-session-id: $SESSION" || true
  fi
  rm -f "$hdr" "$body"
}
trap cleanup EXIT INT TERM

INIT_JSON='{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"delta-cli","version":"1"}}}'

retry_after() {
  v=$(awk 'BEGIN{IGNORECASE=1} /^retry-after:/{gsub("\r","");print $2}' "$hdr")
  echo "${v:-2}"
}

init() {
  n=0
  while :; do
    code=$(curl -sS -m 20 -D "$hdr" -o /dev/null -w '%{http_code}' -X POST "$URL" \
      -H 'Content-Type: application/json' \
      -H 'Accept: application/json, text/event-stream' \
      --data-binary "$INIT_JSON" || echo 000)
    [ "$code" = "200" ] && break
    n=$((n + 1))
    if [ "$n" -ge 6 ]; then
      echo "error: could not open an MCP session (HTTP $code) at $URL - is OpenPencil running?" >&2
      exit 1
    fi
    sleep "$(retry_after)"
  done
  SESSION=$(awk 'BEGIN{IGNORECASE=1} /^mcp-session-id:/{gsub("\r","");print $2}' "$hdr")
  if [ -z "$SESSION" ]; then
    echo "error: no mcp-session-id header from $URL" >&2
    exit 1
  fi
  curl -sS -m 20 -o /dev/null -X POST "$URL" \
    -H 'Content-Type: application/json' -H "mcp-session-id: $SESSION" \
    -H 'Accept: application/json, text/event-stream' \
    --data-binary '{"jsonrpc":"2.0","method":"notifications/initialized"}' || true
}

# Leaves the raw JSON-RPC response in $body. Retries while the server reports 503
# (it serialises calls and answers 503 + retry-after while one is in flight).
request() {
  n=0
  while :; do
    code=$(curl -sS -m "$TIMEOUT" -o "$body" -D "$hdr" -w '%{http_code}' -X POST "$URL" \
      -H 'Content-Type: application/json' -H "mcp-session-id: $SESSION" \
      -H 'Accept: application/json, text/event-stream' --data-binary "$1" || echo 000)
    [ "$code" = "503" ] || return 0
    n=$((n + 1))
    if [ "$n" -ge 20 ]; then
      echo "error: server still busy (503) after $n attempts" >&2
      return 1
    fi
    sleep "$(retry_after)"
  done
}

# The server answers single requests as JSON, but may switch to SSE.
json_out() {
  if grep -q '^data: ' "$body"; then
    sed -n 's/^data: //p' "$body" | tail -n 1
  else
    cat "$body"
  fi
}

call() { # $1 tool name, $2 arguments JSON (optional)
  args='{}'
  if [ $# -ge 2 ] && [ -n "$2" ]; then args=$2; fi
  request "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\",\"params\":{\"name\":\"$1\",\"arguments\":$args}}"
}

init

case ${1:-} in
  tools)
    request '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'
    json_out | jq -r '.result.tools[].name' | sort
    ;;
  call)
    shift
    call "$@"
    json_out | jq .
    ;;
  image) # image <outfile> <tool> [args]
    out=$2
    shift 2
    call "$@"
    json_out | jq -r '.result.content[]? | select(.type=="image") | .data' | base64 -D > "$out"
    echo "$out"
    ;;
  *)
    sed -n '2,14p' "$0"
    exit 1
    ;;
esac
