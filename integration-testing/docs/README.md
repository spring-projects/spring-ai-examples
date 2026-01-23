# Integration Testing Guide for Spring AI Examples

This guide explains how to create, run, and maintain integration tests for Spring AI examples using our JBang-based testing framework.

## Quick Start

### Prerequisites
- Java 17+
- JBang (`curl -Ls https://sh.jbang.dev | bash -s - app setup`)
- Required API keys (e.g., `OPENAI_API_KEY`, `ANTHROPIC_API_KEY`)
- Claude Code CLI (for AI validation): `npm install -g @anthropic-ai/claude-code`

### Running Tests

```bash
# Run all integration tests
./integration-testing/scripts/run-integration-tests.sh

# Run specific test by name filter
./integration-testing/scripts/run-integration-tests.sh "kotlin-hello-world"
./integration-testing/scripts/run-integration-tests.sh "weather"
./integration-testing/scripts/run-integration-tests.sh "chain-workflow"

# Clean old logs before running
./integration-testing/scripts/run-integration-tests.sh --clean-logs

# Run a single test directly with JBang
cd kotlin/kotlin-hello-world
jbang integration-tests/RunKotlinHelloWorld.java
```

## Test Architecture

### Overview

The integration testing framework uses:
- **Bash runner script** - Discovers and executes tests
- **JBang test scripts** - Lightweight Java scripts that launch tests
- **Centralized utilities** - Shared Java libraries for common test patterns
- **AI validation** - Claude-powered intelligent output analysis

### Directory Structure

```
spring-ai-examples/
├── integration-testing/
│   ├── scripts/
│   │   └── run-integration-tests.sh    # Main test runner
│   ├── jbang-lib/
│   │   ├── IntegrationTestUtils.java   # Core test utilities
│   │   ├── WebServerTestUtils.java     # Web server test helpers
│   │   └── McpTestUtils.java           # MCP protocol test helpers
│   ├── docs/
│   │   ├── README.md                   # This file
│   │   ├── AI_VALIDATION.md            # AI validation details
│   │   └── TROUBLESHOOTING.md          # Common issues
│   └── logs/
│       └── background-runs/            # Test execution logs
│
└── <module>/
    └── integration-tests/
        ├── ExampleInfo.json            # Test configuration
        └── Run<Module>.java            # JBang test launcher
```

## Creating Integration Tests

### Step 1: Create Test Directory

```bash
mkdir -p your-module/integration-tests
```

### Step 2: Create ExampleInfo.json

```json
{
  "complexity": "simple",
  "timeoutSec": 120,
  "successRegex": [
    "Started.*Application",
    "Your expected output pattern"
  ],
  "requiredEnv": ["OPENAI_API_KEY"],
  "aiValidation": {
    "enabled": true,
    "validationMode": "hybrid",
    "expectedBehavior": "Application should demonstrate X functionality"
  }
}
```

### Step 3: Create JBang Launcher

For **simple applications** (console output, then exit):

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java

/*
 * Integration test for your-module
 */
public class RunYourModule {
    public static void main(String... args) throws Exception {
        IntegrationTestUtils.runIntegrationTest("your-module");
    }
}
```

For **web server applications** (start server, test endpoints, shutdown):

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Web server integration test for your-module
 */
public class RunYourModuleWebServer {
    public static void main(String... args) throws Exception {
        WebServerTestUtils.runSimpleWebServerTest(
            WebServerTestUtils.WebServerTestConfig.withWait(
                "your-module",
                15,  // startup wait seconds
                () -> {
                    // Your validation logic here
                    var response = WebServerTestUtils.httpGet("http://localhost:8080/api/test");
                    return response.statusCode() == 200;
                }
            )
        );
    }
}
```

For **MCP weather servers** (one-liner):

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * MCP weather server integration test
 */
public class RunWeatherServer {
    public static void main(String... args) throws Exception {
        McpTestUtils.runMcpWeatherServerTest("weather-server", 15, "http://localhost:8080/mcp");
    }
}
```

### Path Resolution

Adjust the `//SOURCES` path based on module depth:

| Module Depth | Example | SOURCES Path Prefix |
|--------------|---------|---------------------|
| 2 levels | `kotlin/kotlin-hello-world` | `../../../` |
| 3 levels | `models/chat/helloworld` | `../../../../` |
| 4 levels | `model-context-protocol/weather/starter-webmvc-server` | `../../../../` |

## Configuration Reference

### ExampleInfo.json Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `complexity` | string | No | `"simple"`, `"complex"`, `"web-server"`, `"mcp"` |
| `timeoutSec` | number | No | Maximum execution time (default: 120) |
| `successRegex` | string[] | No | Patterns that must appear in output |
| `requiredEnv` | string[] | No | Required environment variables |
| `aiValidation` | object | No | AI validation configuration |
| `setupCommands` | string[] | No | Commands to run before test |
| `cleanupCommands` | string[] | No | Commands to run after test |

### AI Validation Configuration

```json
{
  "aiValidation": {
    "enabled": true,
    "validationMode": "hybrid",
    "expectedBehavior": "Description of expected application behavior",
    "promptTemplate": "chat_example_validation"
  }
}
```

**Validation Modes:**
- `primary` - AI validation only (for unpredictable AI outputs)
- `hybrid` - Both regex AND AI must pass (recommended)
- `fallback` - AI validates only if regex fails

See [AI_VALIDATION.md](AI_VALIDATION.md) for detailed documentation.

## Centralized Utilities

### IntegrationTestUtils

Core utilities for all tests:
- `runIntegrationTest(moduleName)` - Run a complete integration test
- `loadConfig()` - Load ExampleInfo.json configuration
- `verifyEnvironment(config)` - Check required environment variables
- `buildApplication(moduleName)` - Build with Maven
- `performAIValidation(output, logFile, config, moduleName)` - Run AI validation

### WebServerTestUtils

Utilities for web server tests:
- `runSimpleWebServerTest(config)` - Build, start, validate, shutdown
- `waitForHealthCheck(url, timeout)` - Wait for server readiness
- `httpGet(url)` - Simple HTTP GET request
- `readServerLog()` - Read the server log file
- `shutdownServer(handle)` - Gracefully stop the server

### McpTestUtils

Utilities for MCP protocol tests:
- `runMcpWeatherServerTest(name, timeout, endpoint)` - One-liner for weather servers
- `testMcpSseServer(url, toolTests)` - Test MCP SSE server
- `createWeatherTests()` - Standard weather tool test cases
- `createSqliteTests()` - Standard SQLite tool test cases

## CI/CD Integration

### GitHub Actions Workflow

Tests run in GitHub Actions with grouped execution:

| Group | Tests | API Keys Required |
|-------|-------|-------------------|
| `mcp-servers` | Weather servers (webmvc, webflux, oauth2, stdio) | Anthropic (AI validation) |
| `agentic` | Chain, evaluator, orchestrator, parallelization, routing, reflection | OpenAI, Anthropic |
| `openai-2` | Kotlin examples, misc examples, chat/helloworld | OpenAI, Anthropic |
| `openai-3` | MCP clients, filesystem, sqlite, dynamic-tool-update | OpenAI, Anthropic, Brave |
| `anthropic-multi` | Recursive advisor, document-forge, brave, sampling | OpenAI, Anthropic, Brave |
| `docker-tests` | rag-with-kotlin, evaluation-recursive-advisor-demo | OpenAI, Anthropic + Docker |

### Triggering CI

```bash
# Run all tests
gh workflow run integration-tests.yml

# Run specific group
gh workflow run integration-tests.yml -f test_group=mcp-servers

# Run specific test
gh workflow run integration-tests.yml -f test_filter=kotlin-hello-world
```

### Required Secrets

Configure these in GitHub repository settings:
- `OPENAI_API_KEY` - OpenAI API access
- `ANTHROPIC_API_KEY` - Anthropic API (for AI validation and Anthropic examples)
- `BRAVE_API_KEY` - Brave Search API (for web search examples)

## Troubleshooting

### Common Issues

#### "JBang not found"
```bash
curl -Ls https://sh.jbang.dev | bash -s - app setup
export PATH="$HOME/.jbang/bin:$PATH"
```

#### "Missing environment variable"
```bash
export OPENAI_API_KEY="your-key"
export ANTHROPIC_API_KEY="your-key"
```

#### "Test timeout"
- Increase `timeoutSec` in ExampleInfo.json
- Check if external services are accessible
- Review logs in `integration-testing/logs/background-runs/`

#### "Pattern verification failed"
- Check actual output in log files
- Update `successRegex` patterns
- Consider using AI validation for unpredictable outputs

### Viewing Logs

```bash
# List recent test logs
ls -lt integration-testing/logs/background-runs/*.log | head -10

# View specific test log
cat integration-testing/logs/background-runs/test_kotlin_kotlin-hello-world_*.log

# Search for errors
grep -r "ERROR\|Exception\|FAILED" integration-testing/logs/background-runs/
```

## Best Practices

### Test Scripts
- Keep JBang scripts minimal (17-20 lines)
- Use centralized utilities for all logic
- Verify SOURCES path matches module depth

### Configuration
- Start with `hybrid` validation mode
- Use specific success patterns over generic ones
- Set realistic timeout values

### Maintenance
- Fix bugs in centralized utilities, not individual scripts
- Keep success patterns current with application changes
- Monitor CI for flaky tests

## Examples by Pattern

### Simple Console Apps
- `kotlin/kotlin-hello-world` - 18 lines
- `models/chat/helloworld` - 17 lines
- `agentic-patterns/chain-workflow` - 18 lines

### Web Server Apps
- `weather/starter-webmvc-server` - 17 lines (uses McpTestUtils)
- `kotlin/rag-with-kotlin` - 83 lines (custom validation)

### Composite Apps (Server + Client)
- `model-context-protocol/sampling` - 173 lines
- `model-context-protocol/dynamic-tool-update` - 122 lines

## Getting Help

- Check existing tests for patterns
- Review [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Check [AI_VALIDATION.md](AI_VALIDATION.md) for AI validation issues
- View test logs for detailed error information
