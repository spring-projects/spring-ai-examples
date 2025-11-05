# Spring AI Model Context Protocol - Filesystem Demo

A cross-platform demo application showcasing Spring AI integration with the Model Context Protocol (MCP) Filesystem server. This application enables natural language interactions with your local filesystem.

Connects to the [Filesystem MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem) with access to the `target` directory.

## Platform Support

✅ **Windows** - Automatic `cmd.exe` wrapper for npx
✅ **Linux** - Direct npx execution
✅ **macOS** - Direct npx execution

The application **automatically detects** your operating system and configures the MCP client appropriately.

## Features

- ✨ **Cross-platform** - Works on Windows, Linux, and macOS without modification
- 🤖 Natural language querying and updating of local filesystem
- 📝 Predefined question mode for automated analysis
- 🔄 Two configuration approaches: programmatic (default) or JSON-based
- 🚀 Built on Spring AI and Model Context Protocol

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js and npx (npm comes with Node.js)
- OpenAI API key

### Installing Node.js/npx

**Windows:**
```cmd
# Download and install from https://nodejs.org
# npx is included with npm
npx --version
```

**Linux (Ubuntu/Debian):**
```bash
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs
npx --version
```

**macOS:**
```bash
brew install node
npx --version
```

## Quick Start

1. **Clone the repository:**
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd spring-ai-examples/model-context-protocol/filesystem
   ```

2. **Set your OpenAI API key:**

   **Linux/macOS:**
   ```bash
   export OPENAI_API_KEY='your-api-key-here'
   ```

   **Windows (Command Prompt):**
   ```cmd
   set OPENAI_API_KEY=your-api-key-here
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:OPENAI_API_KEY="your-api-key-here"
   ```

3. **Create a sample test file:**

   **Linux/macOS:**
   ```bash
   ./create-text-file.sh
   ```

   **Windows:**
   ```cmd
   mkdir target
   echo Sample content > target\spring-ai-mcp-overview.txt
   ```

4. **Run the application:**

   **Linux/macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

   **Windows:**
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```

## Configuration Approaches

The application supports two ways to configure the MCP client:

### Option 1: Programmatic Configuration (Default - Recommended)

The default approach uses automatic OS detection in `Application.java`:

```java
@Bean(destroyMethod = "close")
@ConditionalOnMissingBean(McpSyncClient.class)
public McpSyncClient mcpClient() {
    ServerParameters stdioParams;

    if (isWindows()) {
        // Windows: cmd.exe /c npx approach
        var winArgs = new ArrayList<>(Arrays.asList(
            "/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"));
        stdioParams = ServerParameters.builder("cmd.exe")
                .args(winArgs)
                .build();
    } else {
        // Linux/Mac: direct npx approach
        stdioParams = ServerParameters.builder("npx")
                .args("-y", "@modelcontextprotocol/server-filesystem", "target")
                .build();
    }

    // Create and initialize client...
}
```

**Advantages:**
- ✅ Works out-of-the-box on all platforms
- ✅ No configuration files needed
- ✅ Automatic OS detection

**Disadvantages:**
- ❌ Configuration is hardcoded in Java
- ❌ Less flexible for different server configurations

### Option 2: JSON Configuration (Optional)

For more flexibility, you can use JSON-based configuration. Edit `src/main/resources/application.properties`:

**For Windows:**
```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config-windows.json
```

**For Linux/macOS:**
```properties
spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config-linux.json
```

**Advantages:**
- ✅ Externalized configuration
- ✅ Easy to modify without recompiling
- ✅ Can configure multiple MCP servers

**Disadvantages:**
- ❌ Must choose the correct OS-specific JSON file
- ❌ Requires manual configuration

**⚠️ Important:** When JSON configuration is enabled, the programmatic `@Bean` is automatically skipped via `@ConditionalOnMissingBean` to avoid conflicts.

## Why Windows Requires Special Handling

On Windows, `npx` is implemented as a **batch file** (.cmd), not a native executable. Java's `ProcessBuilder` (used internally by `StdioClientTransport`) cannot execute batch files directly.

**Solution:** Wrap the command with `cmd.exe /c`:

```java
// Windows
ServerParameters.builder("cmd.exe")
    .args("/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target")

// vs. Linux/macOS
ServerParameters.builder("npx")
    .args("-y", "@modelcontextprotocol/server-filesystem", "target")
```

This pattern applies to other Windows batch files: `npm.cmd`, `node.cmd`, etc.

## Architecture Overview

### Cross-Platform MCP Client Creation

The application uses Spring's `@ConditionalOnMissingBean` to support both configuration approaches:

1. **Programmatic Bean** - Created when JSON config is NOT enabled
2. **Auto-Configuration** - Created when JSON config IS enabled

```java
@Bean(destroyMethod = "close")
@ConditionalOnMissingBean(McpSyncClient.class)
public McpSyncClient mcpClient() {
    // Only created if auto-config doesn't provide a client
}
```

### CommandLineRunner with Dual Support

The `CommandLineRunner` accepts both approaches:

```java
@Bean
public CommandLineRunner predefinedQuestions(
        @Autowired(required = false) List<McpSyncClient> mcpSyncClients,  // From JSON config
        @Autowired(required = false) McpSyncClient mcpClient,              // From programmatic
        ...) {

    // Use whichever is available
    List<McpSyncClient> clients = (mcpSyncClients != null && !mcpSyncClients.isEmpty())
            ? mcpSyncClients
            : (mcpClient != null ? List.of(mcpClient) : List.of());
}
```

### Tool Integration

MCP tools are automatically discovered and integrated with Spring AI:

```java
var chatClient = chatClientBuilder
    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(clients))
    .build();
```

The AI model can then call MCP filesystem tools (read_file, write_file, etc.) through natural language.

## JSON Configuration Files

### mcp-servers-config-windows.json

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "cmd.exe",
      "args": ["/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"],
      "env": {}
    }
  }
}
```

### mcp-servers-config-linux.json

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "target"],
      "env": {}
    }
  }
}
```

## Path Handling

The example uses a **relative path** (`"target"`) instead of absolute paths for cross-platform compatibility:

```java
// ✅ Recommended: Relative path
.args("-y", "@modelcontextprotocol/server-filesystem", "target")

// ❌ Avoid: Absolute path with OS-specific separators
.args("-y", "@modelcontextprotocol/server-filesystem", "/home/user/project/target")
```

The MCP server resolves relative paths based on the current working directory.

## Troubleshooting

### Windows: "Cannot run program 'npx'"

**Cause:** npx is not in PATH or is a batch file that ProcessBuilder can't execute directly.

**Solution:** Ensure the application is using the `cmd.exe` wrapper (should be automatic with the default programmatic approach).

### Bean Conflicts: "Sinks.many().unicast() sinks only allow a single Subscriber"

**Cause:** Both programmatic and JSON configuration are creating MCP clients simultaneously.

**Solution:** Choose ONE approach:
- Comment out JSON config in `application.properties` (use programmatic)
- OR enable JSON config (programmatic will be skipped automatically)

### Linux/macOS: "npx: command not found"

**Cause:** Node.js/npm is not installed or not in PATH.

**Solution:** Install Node.js:
```bash
# Linux
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs

# macOS
brew install node
```

## Example Output

```
Detected Unix-like OS - using npx directly
MCP Initialized: InitializeResult[protocolVersion=2024-11-05, ...]

Running predefined questions with AI model responses:

QUESTION: Can you explain the content of the target/spring-ai-mcp-overview.txt file?
ASSISTANT: The file contains an overview of the Model Context Protocol (MCP) Java SDK...

QUESTION: Please summarize the content... and store it in target/summary.md
ASSISTANT: I've created a summary and saved it to target/summary.md...
```

## Learn More

- [Model Context Protocol Specification](https://modelcontextprotocol.io)
- [MCP Filesystem Server](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI MCP Client Documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
