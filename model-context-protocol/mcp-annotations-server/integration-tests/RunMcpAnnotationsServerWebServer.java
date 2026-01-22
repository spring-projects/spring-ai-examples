///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Web server integration test for mcp-annotations-server
 * Tests the comprehensive MCP server with tools, resources, prompts, and completions
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.lang.System.*;

public class RunMcpAnnotationsServerWebServer {
    
    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;
        
        try {
            // Get the module root (current directory when run from test runner)
            java.io.File moduleDir = new java.io.File(".").getAbsoluteFile();
            
            // Build the application first
            out.println("🏗️  Building mcp-annotations-server...");
            new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(moduleDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(System.out)
                .redirectError(System.err)
                .execute();
            
            // Create log directory after build
            java.io.File logDir = new java.io.File(moduleDir, "target/integration-test-logs");
            logDir.mkdirs();
            
            // Start server in background
            out.println("🚀 Starting MCP annotations server in background...");
            var logFile = new java.io.File(logDir, "web-server-" + System.currentTimeMillis() + ".log");
            
            Process process = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(moduleDir)
                .redirectOutput(new java.io.FileOutputStream(logFile))
                .redirectError(System.err)
                .start()
                .getProcess();
            
            serverHandle = process.toHandle();
            
            // Wait for server to be ready
            out.println("⏳ Waiting 15 seconds for server startup...");
            Thread.sleep(15000);
            out.println("✅ Server should be ready");
            
            // Test MCP functionality using McpTestUtils
            out.println("\n🧪 Testing MCP Protocol with comprehensive capabilities...");
            
            // Test weather tools (similar to weather server) plus additional tools
            var toolTests = List.of(
                new McpTestUtils.ToolTest(
                    "getWeatherForecastByLocation",
                    Map.of("latitude", 47.6062, "longitude", -122.3321),
                    "Temperature"  // Weather forecast should contain temperature
                ),
                new McpTestUtils.ToolTest(
                    "getAlerts",
                    Map.of("state", "NY"),
                    "alert"  // Should contain alert information
                ),
                // The annotations server might have additional tools
                new McpTestUtils.ToolTest(
                    "addNumbers",
                    Map.of("a", 5, "b", 3),
                    "8",  // 5 + 3 = 8
                    true  // Optional - might not exist
                ),
                new McpTestUtils.ToolTest(
                    "subtractNumbers",
                    Map.of("a", 10, "b", 4),
                    "6",  // 10 - 4 = 6
                    true  // Optional
                )
            );
            
            var mcpResult = McpTestUtils.testMcpSseServer("http://localhost:8080/mcp", toolTests);
            
            // Validate results
            if (mcpResult.success()) {
                out.println("\n🎉 MCP annotations server test passed!");
                out.println("  Server: " + mcpResult.serverInfo());
                out.println("  Available tools: " + mcpResult.availableTools());
                out.println("  Tool test results: " + mcpResult.toolResults().size() + " tools tested");
                
                // Note: Resources, prompts, and completions would require
                // extending McpTestUtils to support those capabilities
                out.println("\n📝 Note: This test validates tools only.");
                out.println("  Full testing of resources, prompts, and completions");
                out.println("  would require extending McpTestUtils.");
            } else {
                err.println("\n❌ MCP annotations server test failed!");
                err.println("  Error: " + mcpResult.errorMessage());
                exit(1);
            }
            
        } finally {
            // Always try to shut down the server
            if (serverHandle != null) {
                WebServerTestUtils.shutdownServer(serverHandle);
            }
        }
    }
}