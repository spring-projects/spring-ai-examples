///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Web server integration test for weather/starter-webflux-server
 * Tests the MCP weather server with WebFlux reactive SSE transport
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static java.lang.System.*;

public class RunWeatherStarterWebfluxServerWebServer {
    
    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;
        
        try {
            // Get the module root (current directory when run from test runner)
            java.io.File moduleDir = new java.io.File(".").getAbsoluteFile();
            
            // Build the application first
            out.println("🏗️  Building weather-starter-webflux-server...");
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
            out.println("🚀 Starting WebFlux server in background...");
            var logFile = new java.io.File(logDir, "web-server-" + System.currentTimeMillis() + ".log");
            
            Process process = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(moduleDir)
                .redirectOutput(new java.io.FileOutputStream(logFile))
                .redirectError(System.err)
                .start()
                .getProcess();
            
            serverHandle = process.toHandle();
            
            // Wait for server to be ready (WebFlux servers might take a bit longer)
            out.println("⏳ Waiting 20 seconds for WebFlux server startup...");
            Thread.sleep(20000);
            out.println("✅ Server should be ready");
            
            // Test MCP functionality using McpTestUtils
            out.println("\n🧪 Testing MCP Protocol over WebFlux SSE...");
            var toolTests = McpTestUtils.createWeatherTests();
            var mcpResult = McpTestUtils.testMcpSseServer("http://localhost:8080/mcp", toolTests);
            
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