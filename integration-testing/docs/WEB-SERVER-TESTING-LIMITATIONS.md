# Web Server Testing Limitations

**Date**: December 2024  
**Author**: Integration Testing Framework Team

## Problem Statement

Several Spring AI examples are web servers that expose HTTP endpoints rather than batch applications. The current integration testing framework expects applications to:
1. Start up
2. Perform some work
3. Exit with a status code

Web servers violate this assumption - they start and run indefinitely, waiting for HTTP requests.

## Affected Tests (2 currently excluded)

1. **`misc/openai-streaming-response`**
   - Exposes `/ai/generateStream` endpoint
   - Streams AI responses via Server-Sent Events
   - Needs HTTP client to test functionality

2. **`model-context-protocol/weather/starter-webmvc-server`**
   - MCP server exposing weather tools
   - Needs MCP client to connect and invoke tools
   - Part of client-server architecture

## Current Workaround

These tests are excluded from CI with the reason "timeout - web server".

## Proposed Solution: Phase 5 Implementation

The Phase 5 "Integrated Client-Server Testing" enhancement would solve this by:

### For Simple Web Servers (like openai-streaming-response):
```json
{
  "testMode": "web-server",
  "server": {
    "startupTime": 10,
    "healthCheck": "http://localhost:8080/actuator/health",
    "testEndpoints": [
      {
        "url": "http://localhost:8080/ai/generateStream?message=test",
        "method": "GET",
        "expectedResponse": "data.*chatcmpl"
      }
    ]
  }
}
```

### For MCP Client-Server Pairs:
```json
{
  "testMode": "client-server",
  "components": {
    "server": {
      "path": "./server",
      "startupTime": 10,
      "healthCheck": "http://localhost:8080/health"
    },
    "client": {
      "path": "./client",
      "startupDelay": 5,
      "serverUrl": "http://localhost:8080"
    }
  }
}
```

## Implementation Approach

### Option 1: Background Process with HTTP Client
1. Start Spring Boot app in background
2. Wait for health check endpoint
3. Execute HTTP requests using curl or Java HTTP client
4. Verify responses
5. Send shutdown signal (SIGTERM)

### Option 2: Test Application Mode
1. Create a test client application
2. Client starts server, makes requests, validates
3. Client exits with success/failure code

### Option 3: Docker Compose Orchestration
1. Use docker-compose for multi-component tests
2. Server container + client container
3. Client performs tests and reports results

## Temporary Workarounds

Until Phase 5 is implemented, consider:

1. **Separate Manual Testing**: Document manual test procedures
2. **Mock Mode**: Add a test mode that performs self-test and exits
3. **Timeout with Success**: Accept timeout as success if startup patterns found

## Impact

- **2 tests currently excluded** (8% of total tests)
- Would increase CI coverage from 75% to 83% if fixed
- Sets foundation for testing distributed systems

## Next Steps

1. Implement Phase 5 orchestration infrastructure
2. Start with `openai-streaming-response` as simple case
3. Extend to MCP client-server pairs
4. Consider Docker-based orchestration for complex scenarios