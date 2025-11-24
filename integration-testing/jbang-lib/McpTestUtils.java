/*
 * MCP Protocol Testing Utilities
 * Provides comprehensive testing for Model Context Protocol servers
 * including initialization, tool discovery, and invocation
 */

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static java.lang.System.*;

public class McpTestUtils {
    
    // Test configuration for a specific tool
    public record ToolTest(
        String toolName,
        Map<String, Object> parameters,
        String expectedContent,
        boolean optional  // Some tools might not be available in all servers
    ) {
        // Constructor with default optional = false
        public ToolTest(String toolName, Map<String, Object> parameters, String expectedContent) {
            this(toolName, parameters, expectedContent, false);
        }
    }
    
    // Result of MCP server testing
    public record McpTestResult(
        boolean success,
        String serverInfo,
        List<String> availableTools,
        Map<String, String> toolResults,
        String errorMessage
    ) {}
    
    /**
     * Test an MCP server over SSE transport
     * @param baseUrl The base URL of the MCP server (e.g., "http://localhost:8080")
     * @param toolTests List of tools to test with their parameters
     * @return Test result with details about the server and tool invocations
     */
    public static McpTestResult testMcpSseServer(String baseUrl, List<ToolTest> toolTests) {
        out.println("🔌 Testing MCP server via SSE transport at: " + baseUrl);
        
        McpSyncClient mcpClient = null;
        try {
            // Create SSE transport and client
            var transport = HttpClientSseClientTransport.builder(baseUrl).build();
            mcpClient = McpClient.sync(transport).build();
            
            // Initialize MCP connection
            out.println("🤝 Initializing MCP connection...");
            var initResult = mcpClient.initialize();
            String serverInfo = String.format("Server: %s %s", 
                initResult.serverInfo().name(), 
                initResult.serverInfo().version());
            out.println("✅ Connected to: " + serverInfo);
            
            // Ping test
            out.println("🏓 Testing ping...");
            mcpClient.ping();
            out.println("✅ Ping successful");
            
            // List available tools
            out.println("🔧 Discovering available tools...");
            ListToolsResult toolsList = mcpClient.listTools();
            List<String> availableTools = new ArrayList<>();
            
            if (toolsList.tools() != null) {
                for (var tool : toolsList.tools()) {
                    availableTools.add(tool.name());
                    out.println("  - " + tool.name() + ": " + tool.description());
                }
            }
            out.println("✅ Found " + availableTools.size() + " tools");
            
            // Test each requested tool
            Map<String, String> toolResults = new HashMap<>();
            boolean allTestsPassed = true;
            
            for (ToolTest test : toolTests) {
                out.println("\n🧪 Testing tool: " + test.toolName());
                
                // Check if tool is available
                if (!availableTools.contains(test.toolName())) {
                    if (test.optional()) {
                        out.println("⚠️ Tool not available (optional): " + test.toolName());
                        continue;
                    } else {
                        err.println("❌ Required tool not found: " + test.toolName());
                        allTestsPassed = false;
                        continue;
                    }
                }
                
                try {
                    // Call the tool
                    out.println("  Parameters: " + test.parameters());
                    CallToolRequest request = new CallToolRequest(test.toolName(), test.parameters());
                    CallToolResult result = mcpClient.callTool(request);
                    
                    // Extract result content
                    String resultContent = "";
                    if (result.content() != null && !result.content().isEmpty()) {
                        var firstContent = result.content().get(0);
                        if (firstContent instanceof TextContent) {
                            resultContent = ((TextContent) firstContent).text();
                        } else {
                            resultContent = firstContent.toString();
                        }
                    }
                    
                    // Store result
                    toolResults.put(test.toolName(), resultContent);
                    out.println("  Response length: " + resultContent.length() + " chars");
                    
                    // Validate expected content if provided
                    if (test.expectedContent() != null && !test.expectedContent().isEmpty()) {
                        if (resultContent.toLowerCase().contains(test.expectedContent().toLowerCase())) {
                            out.println("✅ Tool response contains expected content: " + test.expectedContent());
                        } else {
                            err.println("❌ Tool response missing expected content: " + test.expectedContent());
                            out.println("  Actual response (first 200 chars): " + 
                                resultContent.substring(0, Math.min(200, resultContent.length())));
                            allTestsPassed = false;
                        }
                    } else {
                        out.println("✅ Tool invoked successfully");
                    }
                    
                } catch (Exception e) {
                    err.println("❌ Error calling tool " + test.toolName() + ": " + e.getMessage());
                    toolResults.put(test.toolName(), "ERROR: " + e.getMessage());
                    allTestsPassed = false;
                }
            }
            
            // Graceful shutdown
            out.println("\n🛑 Closing MCP connection...");
            mcpClient.closeGracefully();
            out.println("✅ Connection closed gracefully");
            
            return new McpTestResult(
                allTestsPassed,
                serverInfo,
                availableTools,
                toolResults,
                allTestsPassed ? "All tests passed" : "Some tests failed"
            );
            
        } catch (Exception e) {
            err.println("❌ MCP test failed: " + e.getMessage());
            e.printStackTrace();
            return new McpTestResult(
                false,
                "Unknown",
                List.of(),
                Map.of(),
                "Connection error: " + e.getMessage()
            );
        } finally {
            // Ensure client is closed
            if (mcpClient != null) {
                try {
                    mcpClient.closeGracefully();
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }
    
    /**
     * Test an MCP server over STDIO transport
     * @param command The command to launch the MCP server
     * @param args Arguments for the server command
     * @param toolTests List of tools to test
     * @return Test result
     */
    public static McpTestResult testMcpStdioServer(
            String command, 
            List<String> args, 
            List<ToolTest> toolTests) {
        
        out.println("🔌 Testing MCP server via STDIO transport");
        out.println("  Command: " + command + " " + String.join(" ", args));
        
        McpSyncClient mcpClient = null;
        try {
            // Create STDIO transport
            var serverParams = ServerParameters.builder(command)
                .args(args.toArray(new String[0]))
                .build();
            
            var transport = new StdioClientTransport(serverParams);
            mcpClient = McpClient.sync(transport).build();
            
            // Initialize MCP connection
            out.println("🤝 Initializing MCP connection...");
            var initResult = mcpClient.initialize();
            String serverInfo = String.format("Server: %s %s", 
                initResult.serverInfo().name(), 
                initResult.serverInfo().version());
            out.println("✅ Connected to: " + serverInfo);
            
            // Ping test
            out.println("🏓 Testing ping...");
            mcpClient.ping();
            out.println("✅ Ping successful");
            
            // List available tools
            out.println("🔧 Discovering available tools...");
            ListToolsResult toolsList = mcpClient.listTools();
            List<String> availableTools = new ArrayList<>();
            
            if (toolsList.tools() != null) {
                for (var tool : toolsList.tools()) {
                    availableTools.add(tool.name());
                    out.println("  - " + tool.name() + ": " + tool.description());
                }
            }
            out.println("✅ Found " + availableTools.size() + " tools");
            
            // Test each requested tool
            Map<String, String> toolResults = new HashMap<>();
            boolean allTestsPassed = true;
            
            for (ToolTest test : toolTests) {
                out.println("\n🧪 Testing tool: " + test.toolName());
                
                // Check if tool is available
                if (!availableTools.contains(test.toolName())) {
                    if (test.optional()) {
                        out.println("⚠️ Tool not available (optional): " + test.toolName());
                        continue;
                    } else {
                        err.println("❌ Required tool not found: " + test.toolName());
                        allTestsPassed = false;
                        continue;
                    }
                }
                
                try {
                    // Call the tool
                    out.println("  Parameters: " + test.parameters());
                    CallToolRequest request = new CallToolRequest(test.toolName(), test.parameters());
                    CallToolResult result = mcpClient.callTool(request);
                    
                    // Extract result content
                    String resultContent = "";
                    if (result.content() != null && !result.content().isEmpty()) {
                        var firstContent = result.content().get(0);
                        if (firstContent instanceof TextContent) {
                            resultContent = ((TextContent) firstContent).text();
                        } else {
                            resultContent = firstContent.toString();
                        }
                    }
                    
                    // Store result
                    toolResults.put(test.toolName(), resultContent);
                    out.println("  Response length: " + resultContent.length() + " chars");
                    
                    // Validate expected content if provided
                    if (test.expectedContent() != null && !test.expectedContent().isEmpty()) {
                        if (resultContent.toLowerCase().contains(test.expectedContent().toLowerCase())) {
                            out.println("✅ Tool response contains expected content: " + test.expectedContent());
                        } else {
                            err.println("❌ Tool response missing expected content: " + test.expectedContent());
                            out.println("  Actual response (first 200 chars): " + 
                                resultContent.substring(0, Math.min(200, resultContent.length())));
                            allTestsPassed = false;
                        }
                    } else {
                        out.println("✅ Tool invoked successfully");
                    }
                    
                } catch (Exception e) {
                    err.println("❌ Error calling tool " + test.toolName() + ": " + e.getMessage());
                    toolResults.put(test.toolName(), "ERROR: " + e.getMessage());
                    allTestsPassed = false;
                }
            }
            
            // Return result
            return new McpTestResult(
                allTestsPassed,
                serverInfo,
                availableTools,
                toolResults,
                allTestsPassed ? "All tests passed" : "Some tests failed"
            );
            
        } catch (Exception e) {
            err.println("❌ STDIO MCP test failed: " + e.getMessage());
            e.printStackTrace();
            return new McpTestResult(
                false,
                "Unknown",
                List.of(),
                Map.of(),
                "STDIO error: " + e.getMessage()
            );
        } finally {
            // Ensure client is closed
            if (mcpClient != null) {
                try {
                    out.println("\n🛑 Closing MCP connection...");
                    mcpClient.closeGracefully();
                    out.println("✅ Connection closed gracefully");
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }
    
    /**
     * Test an MCP STDIO server JAR file
     * @param jarPath Path to the server JAR file
     * @param toolTests List of tools to test
     * @return Test result
     */
    public static McpTestResult testMcpStdioServerJar(String jarPath, List<ToolTest> toolTests) {
        List<String> args = Arrays.asList("-jar", jarPath);
        return testMcpStdioServer("java", args, toolTests);
    }
    
    /**
     * Helper method to create common weather service tests
     */
    public static List<ToolTest> createWeatherTests() {
        return List.of(
            new ToolTest(
                "getWeatherForecastByLocation",
                Map.of("latitude", 47.6062, "longitude", -122.3321),
                "Temperature"  // Weather forecast should contain temperature information
            ),
            new ToolTest(
                "getAlerts",
                Map.of("state", "NY"),
                "alert"  // Should contain alert information or "no alerts"
            )
        );
    }
    
    /**
     * Helper method to create common SQLite tests
     */
    public static List<ToolTest> createSqliteTests() {
        return List.of(
            new ToolTest(
                "executeSqlQuery",
                Map.of("query", "SELECT * FROM users LIMIT 1"),
                "user"  // Should return user data
            ),
            new ToolTest(
                "listTables",
                Map.of(),
                "table"  // Should list available tables
            )
        );
    }
    
    /**
     * Helper method to create filesystem tests
     */
    public static List<ToolTest> createFilesystemTests() {
        return List.of(
            new ToolTest(
                "listDirectory",
                Map.of("path", "."),
                "file"  // Should list files
            ),
            new ToolTest(
                "readFile",
                Map.of("path", "README.md"),
                "Spring AI"  // README should mention Spring AI
            )
        );
    }
}