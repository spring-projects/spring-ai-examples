///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//DEPS io.modelcontextprotocol.sdk:mcp:0.11.0
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Integration test launcher for MCP Weather STDIO Server
 * Tests STDIO transport MCP protocol with weather tools
 */

import java.nio.file.*;
import static java.lang.System.*;

public class RunStarterStdioServer {
    
    public static void main(String... args) throws Exception {
        out.println("🚀 Starting MCP Weather STDIO Server Integration Test");
        
        // Build the server JAR first
        out.println("🏗️  Building starter-stdio-server...");
        IntegrationTestUtils.buildApplication("starter-stdio-server");
        
        // Verify JAR exists
        Path jarPath = Paths.get("target/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar");
        if (!Files.exists(jarPath)) {
            err.println("❌ Server JAR not found at: " + jarPath);
            exit(1);
        }
        out.println("✅ Server JAR built successfully");
        
        // Test the STDIO server using McpTestUtils
        out.println("\n🔌 Testing MCP server via STDIO transport");
        var result = McpTestUtils.testMcpStdioServerJar(
            jarPath.toAbsolutePath().toString(),
            McpTestUtils.createWeatherTests()
        );
        
        // Display test results
        out.println("\n📊 Test Results:");
        out.println("  Success: " + result.success());
        out.println("  Server: " + result.serverInfo());
        out.println("  Available tools: " + result.availableTools());
        out.println("  Message: " + result.errorMessage());
        
        if (!result.success()) {
            err.println("\n❌ Integration test failed");
            exit(1);
        }
        
        out.println("\n🎉 Integration test passed!");
    }
}