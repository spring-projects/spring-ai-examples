# Phase 5: Web Server Testing Infrastructure

**Date**: December 2024  
**Status**: ✅ COMPLETED  
**Impact**: Enabled testing of streaming web servers and SSE endpoints

## Overview

Successfully implemented infrastructure for testing Spring Boot web servers that don't naturally exit, including streaming response servers and MCP servers with SSE transports.

## Problem Solved

Several Spring AI examples are web servers that expose HTTP endpoints rather than batch applications. These servers violated the original testing assumption that applications would:
1. Start up
2. Perform some work  
3. Exit with a status code

Web servers start and run indefinitely, waiting for HTTP requests, which caused timeouts in the original framework.

## Solution Implemented

### 1. WebServerTestUtils.java

Created centralized utility for web server testing with:
- Background process management
- HTTP client for endpoint testing
- SSE (Server-Sent Events) support
- Graceful shutdown handling
- Local log directory management

```java
public static void runWebServerTest(String moduleName, WebServerConfig config) {
    // Start server in background
    ProcessHandle serverHandle = startServerInBackground();
    
    // Wait for server readiness
    waitForHealthCheck(config.healthCheck, config.startupTime);
    
    // Test configured endpoints
    for (EndpointTest test : config.testEndpoints()) {
        testEndpoint(test);
    }
    
    // Graceful shutdown
    shutdownServer(serverHandle);
}
```

### 2. Test Configuration Structure

Extended ExampleInfo.json with web server configuration:

```json
{
  "testMode": "web-server",
  "webServerConfig": {
    "startupTime": 15,
    "testEndpoints": [
      {
        "url": "http://localhost:8080/endpoint",
        "method": "GET",
        "expectedPattern": "response pattern",
        "headers": {
          "Accept": "text/event-stream"
        }
      }
    ]
  }
}
```

## Examples Successfully Tested

### 1. openai-streaming-response
- **Type**: Streaming AI response server
- **Endpoint**: `/ai/generateStream`
- **Challenge**: SSE stream doesn't close naturally
- **Solution**: HTTP client with SSE headers and pattern matching

### 2. weather/starter-webmvc-server
- **Type**: MCP server with SSE transport
- **Endpoint**: `/mcp/message`
- **Challenge**: MCP protocol over SSE
- **Solution**: Proper SSE request handling

## CI Integration

Created separate workflow for web server tests:
- `.github/workflows/web-server-tests.yml`
- Manual trigger with Spring AI version selection
- Isolated testing for web servers
- Background process management in CI environment

## Key Achievements

1. **Background Process Management**: Servers run in background with proper lifecycle management
2. **HTTP Testing**: Full HTTP client implementation for endpoint validation
3. **SSE Support**: Proper handling of Server-Sent Events streams
4. **Log Capture**: Server logs captured to local directory for debugging
5. **Graceful Shutdown**: Clean termination with SIGTERM/SIGKILL fallback
6. **CI Ready**: Workflow ready for GitHub Actions integration

## Fixed Issues

### prompt-engineering-patterns Timeout
- **Problem**: Complex example making multiple AI API calls
- **Solution**: Increased timeout from 120s to 300s
- **Result**: Test now passes successfully

## Metrics

- **Tests Added**: 2 web server tests
- **Coverage Increase**: From 71% to 75% (18/24 tests passing)
- **Code Reuse**: WebServerTestUtils shared across all web server tests
- **Lines of Code**: ~200 lines for complete infrastructure

## Future Enhancements

1. **Health Check Endpoints**: Add actuator endpoints to servers for better readiness detection
2. **Multiple Port Support**: Handle servers on different ports
3. **WebSocket Support**: Extend to WebSocket-based servers
4. **Load Testing**: Add performance testing capabilities
5. **Client-Server Orchestration**: Test MCP client-server pairs together

## Technical Details

### Process Management
```java
// Start server with log redirection
ProcessBuilder pb = new ProcessBuilder("./mvnw", "spring-boot:run", "-q");
pb.redirectOutput(logFile);
Process process = pb.start();

// Graceful shutdown
handle.destroy(); // SIGTERM
if (!terminated) {
    handle.destroyForcibly(); // SIGKILL
}
```

### HTTP Client Configuration
```java
HttpClient httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .build();

HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(url))
    .header("Accept", "text/event-stream")
    .GET()
    .build();
```

## Lessons Learned

1. **SSE Complexity**: SSE streams require special handling as they don't close naturally
2. **Process Lifecycle**: Proper process management crucial for CI environments
3. **Log Management**: Local log directories better than complex path resolution
4. **Timeout Tuning**: Different examples need different timeout values
5. **Health Checks**: Optional health checks provide flexibility

## Files Created/Modified

### New Files
- `integration-testing/jbang-lib/WebServerTestUtils.java`
- `misc/openai-streaming-response/integration-tests/RunOpenaiStreamingResponseWebServer.java`
- `model-context-protocol/weather/starter-webmvc-server/integration-tests/RunWeatherStarterWebmvcServerWebServer.java`
- `.github/workflows/web-server-tests.yml`

### Modified Files
- `misc/openai-streaming-response/integration-tests/ExampleInfo.json`
- `model-context-protocol/weather/starter-webmvc-server/integration-tests/ExampleInfo.json`
- `prompt-engineering/prompt-engineering-patterns/integration-tests/ExampleInfo.json`

## Conclusion

Phase 5 successfully extends the integration testing framework to handle web servers, streaming responses, and SSE endpoints. This infrastructure provides a foundation for testing more complex distributed systems and client-server architectures in future phases.