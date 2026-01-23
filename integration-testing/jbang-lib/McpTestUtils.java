/*
 * MCP Protocol Testing Utilities
 * Provides comprehensive testing for Model Context Protocol servers
 * including initialization, tool discovery, and invocation
 * Uses HTTP client directly to support the new STREAMABLE protocol
 */

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
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
    
    private static final ObjectMapper mapper = new ObjectMapper();
    private static int requestId = 1;
    private static String sessionId = null;

    /**
     * Test an MCP server over HTTP STREAMABLE transport
     * @param baseUrl The base URL of the MCP server (e.g., "http://localhost:8080/mcp")
     * @param toolTests List of tools to test with their parameters
     * @return Test result with details about the server and tool invocations
     */
    public static McpTestResult testMcpSseServer(String baseUrl, List<ToolTest> toolTests) {
        out.println("🔌 Testing MCP server via HTTP STREAMABLE transport at: " + baseUrl);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        sessionId = null;  // Reset session for each test

        try {
            // Initialize MCP connection
            out.println("🤝 Initializing MCP connection...");
            String initRequest = createJsonRpcRequest("initialize", Map.of(
                "protocolVersion", "1.0",
                "capabilities", Map.of(),
                "clientInfo", Map.of("name", "integration-test", "version", "1.0")
            ));

            JsonNode initResponse = sendMcpRequest(client, baseUrl, initRequest, true);
            JsonNode result = initResponse.get("result");
            JsonNode serverInfoNode = result.get("serverInfo");
            String serverInfo = String.format("Server: %s %s",
                serverInfoNode.get("name").asText(),
                serverInfoNode.get("version").asText());
            out.println("✅ Connected to: " + serverInfo);
            out.println("📋 Session ID: " + sessionId);

            // List available tools
            out.println("🔧 Discovering available tools...");
            String listToolsRequest = createJsonRpcRequest("tools/list", Map.of());
            JsonNode toolsResponse = sendMcpRequest(client, baseUrl, listToolsRequest, false);

            List<String> availableTools = new ArrayList<>();
            JsonNode toolsResult = toolsResponse.get("result");
            if (toolsResult != null && toolsResult.has("tools")) {
                for (JsonNode tool : toolsResult.get("tools")) {
                    String toolName = tool.get("name").asText();
                    String description = tool.has("description") ? tool.get("description").asText() : "No description";
                    availableTools.add(toolName);
                    out.println("  - " + toolName + ": " + description);
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
                    String callRequest = createJsonRpcRequest("tools/call", Map.of(
                        "name", test.toolName(),
                        "arguments", test.parameters()
                    ));
                    JsonNode callResponse = sendMcpRequest(client, baseUrl, callRequest, false);

                    // Extract result content
                    String resultContent = "";
                    JsonNode callResult = callResponse.get("result");
                    if (callResult != null && callResult.has("content")) {
                        JsonNode content = callResult.get("content");
                        if (content.isArray() && content.size() > 0) {
                            JsonNode firstContent = content.get(0);
                            if (firstContent.has("text")) {
                                resultContent = firstContent.get("text").asText();
                            } else {
                                resultContent = firstContent.toString();
                            }
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

            out.println("\n✅ MCP protocol test completed");

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
        }
    }

    private static String createJsonRpcRequest(String method, Map<String, Object> params) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("method", method);
        request.put("params", params);
        request.put("id", requestId++);
        return mapper.writeValueAsString(request);
    }

    private static JsonNode sendMcpRequest(HttpClient client, String baseUrl, String jsonBody, boolean isInit) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream, application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        // Add session ID header for non-init requests
        if (!isInit && sessionId != null) {
            requestBuilder.header("mcp-session-id", sessionId);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Capture session ID from init response
        if (isInit) {
            response.headers().firstValue("mcp-session-id").ifPresent(id -> sessionId = id);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        String body = response.body();

        // Init returns plain JSON, other requests return SSE format
        if (isInit) {
            return mapper.readTree(body);
        } else {
            // Parse SSE format: extract JSON from "data:" lines
            return parseSseResponse(body);
        }
    }

    private static JsonNode parseSseResponse(String sseBody) throws Exception {
        // SSE format: "id: X\ndata: {...}\n\n"
        StringBuilder jsonData = new StringBuilder();
        for (String line : sseBody.split("\n")) {
            if (line.startsWith("data:")) {
                String data = line.substring(5).trim();
                if (!data.isEmpty()) {
                    jsonData.append(data);
                }
            }
        }
        if (jsonData.length() == 0) {
            throw new RuntimeException("No data found in SSE response: " + sseBody);
        }
        return mapper.readTree(jsonData.toString());
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
                "Event"  // Alert responses contain "Event:" followed by alert type (Watch, Warning, etc.)
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

    /**
     * Run a complete MCP weather server integration test.
     * This is a one-liner for testing standard MCP weather servers.
     *
     * @param moduleName Name of the module for logging
     * @param startupSeconds Seconds to wait for server startup
     * @param mcpEndpoint The MCP endpoint URL (e.g., "http://localhost:8080/mcp")
     */
    public static void runMcpWeatherServerTest(String moduleName, int startupSeconds, String mcpEndpoint) throws Exception {
        WebServerTestUtils.runSimpleWebServerTest(
            WebServerTestUtils.WebServerTestConfig.withWait(
                moduleName,
                startupSeconds,
                () -> {
                    try {
                        var toolTests = createWeatherTests();
                        var mcpResult = testMcpSseServer(mcpEndpoint, toolTests);

                        if (mcpResult.success()) {
                            out.println("  Server: " + mcpResult.serverInfo());
                            out.println("  Available tools: " + mcpResult.availableTools());
                            out.println("  Tool tests: " + mcpResult.toolResults().size() + " passed");
                            return true;
                        } else {
                            err.println("  Error: " + mcpResult.errorMessage());
                            return false;
                        }
                    } catch (Exception e) {
                        err.println("  Exception: " + e.getMessage());
                        return false;
                    }
                }
            )
        );
    }
}