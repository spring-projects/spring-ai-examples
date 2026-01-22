///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//DEPS io.modelcontextprotocol.sdk:mcp:0.10.0
//DEPS io.modelcontextprotocol.sdk:mcp-spring-webflux:0.10.0
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Web server integration test for weather/starter-webmvc-server
 * Tests the MCP weather server with full protocol validation
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.lang.System.*;

public class RunWeatherStarterWebmvcServerWebServer {
    
    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;
        
        try {
            // Get the module root (current directory when run from test runner)
            java.io.File moduleDir = new java.io.File(".").getAbsoluteFile();
            
            // Build the application first
            out.println("🏗️  Building weather-starter-webmvc-server...");
            new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(moduleDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(System.out)
                .redirectError(System.err)
                .execute();
            
            // Create log directory after build (since clean might remove it)
            java.io.File logDir = new java.io.File(moduleDir, "target/integration-test-logs");
            logDir.mkdirs();
            
            // Start server in background using zt-exec
            out.println("🚀 Starting web server in background...");
            var logFile = new java.io.File(logDir, "web-server-" + System.currentTimeMillis() + ".log");
            
            Process process = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(moduleDir)
                .redirectOutput(new java.io.FileOutputStream(logFile))
                .redirectError(System.err)
                .start()
                .getProcess();
            
            serverHandle = process.toHandle();
            
            // Wait for server to be ready (no health check, so just wait)
            out.println("⏳ Waiting 15 seconds for server startup...");
            Thread.sleep(15000);
            out.println("✅ Server should be ready");
            
            // Test MCP functionality using McpTestUtils
            out.println("\n🧪 Testing MCP Protocol...");
            var toolTests = McpTestUtils.createWeatherTests();
            var mcpResult = McpTestUtils.testMcpSseServer("http://localhost:8080", toolTests);
            
            // Validate results
            if (mcpResult.success()) {
                out.println("\n🎉 MCP weather server test passed!");
                out.println("  Server: " + mcpResult.serverInfo());
                out.println("  Available tools: " + mcpResult.availableTools());
                out.println("  Tool test results: " + mcpResult.toolResults().size() + " tools tested");
            } else {
                err.println("\n❌ MCP weather server test failed!");
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