# Remaining Test Failures Analysis

**Date**: December 2024  
**Status**: 7 tests still failing/excluded from CI

## Summary of Issues

### 1. Web Server Tests (Partial Solution)

#### ❌ `misc/openai-streaming-response`
- **Status**: Test created but validation is weak
- **Current validation**: Only checks for SSE data pattern
- **Missing**: Doesn't validate actual joke content or streaming chunks
- **Need**: Better SSE parsing and content validation
- **Not in CI**: Need to add to main workflow or web-server workflow

#### ❌ `model-context-protocol/weather/starter-webmvc-server`  
- **Status**: Test created but validation is minimal
- **Current validation**: Only checks SSE content type
- **Missing**: No actual MCP protocol testing
- **Need**: Send MCP initialize request, validate tools listing
- **Not in CI**: Need to add to main workflow or web-server workflow

### 2. Infrastructure Dependencies

#### ❌ `kotlin/rag-with-kotlin`
- **Issue**: Requires Docker for vector database
- **Solution**: Add Docker support to CI or mock the database
- **Complexity**: High - needs Docker in GitHub Actions

### 3. Connection Issues

#### ❌ `model-context-protocol/client-starter/starter-webflux-client`
- **Issue**: Connection refused when trying to connect to MCP servers
- **Error**: Likely trying to connect to servers that aren't running
- **Solution**: Need to start MCP servers before running client
- **Complexity**: Medium - requires orchestration

### 4. Client-Server Orchestration

#### ❌ `model-context-protocol/dynamic-tool-update/client`
- **Issue**: Needs server running
- **Solution**: Start server, then run client
- **Complexity**: High - requires Phase 5 full implementation

#### ❌ `model-context-protocol/dynamic-tool-update/server`
- **Issue**: Needs client to connect
- **Solution**: Start server, then run client to test
- **Complexity**: High - requires Phase 5 full implementation

### 5. Long Running Tests (Fixed but not in CI)

#### ⚠️ `prompt-engineering/prompt-engineering-patterns`
- **Status**: Fixed timeout (120s → 300s)
- **Issue**: Not added to CI yet
- **Solution**: Add to CI with longer timeout
- **Complexity**: Low - just needs CI update

## Validation Quality Issues

### Current Web Server Test Limitations

1. **openai-streaming-response**:
   - Sends: `message=Tell%20me%20a%20short%20joke`
   - Validates: Basic SSE format
   - **Missing**: Actual joke content, streaming chunks, completion

2. **weather/starter-webmvc-server**:
   - Sends: GET to `/mcp/message` 
   - Validates: Content type only
   - **Missing**: MCP protocol handshake, tool discovery, weather queries

## Recommended Fixes Priority

### Quick Wins (Low Complexity)
1. **Add `prompt-engineering-patterns` to CI** - Just needs workflow update
2. **Add `kotlin-function-callback` to CI** - Already passes locally

### Medium Complexity
3. **Improve web server validations** - Parse SSE properly, validate content
4. **Add web server tests to CI** - Update workflow to include them

### High Complexity  
5. **Client-Server Orchestration** - Full Phase 5 implementation needed
6. **Docker Support** - Add Docker to CI for `kotlin/rag-with-kotlin`
7. **WebFlux Client Fix** - Debug connection issues, may need server setup

## Actual Test Coverage

- **Total Examples**: ~33
- **Tests Created**: 24 (73%)
- **Actually Passing**: 18 (with prompt-engineering fix)
- **In CI**: 17 (missing prompt-engineering and kotlin-function-callback)
- **True Coverage**: 54% (18/33)

## Next Steps

1. **Immediate**: Add the 2 fixed tests to CI (prompt-engineering, kotlin-function-callback)
2. **Short-term**: Improve web server test validation
3. **Medium-term**: Implement proper client-server orchestration
4. **Long-term**: Add Docker support for remaining tests