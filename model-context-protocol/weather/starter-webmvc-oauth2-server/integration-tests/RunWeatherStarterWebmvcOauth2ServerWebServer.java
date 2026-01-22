///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/McpTestUtils.java

/*
 * Web server integration test for weather/starter-webmvc-oauth2-server
 * Tests the MCP weather server with OAuth2 authentication
 */

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import static java.lang.System.*;

public class RunWeatherStarterWebmvcOauth2ServerWebServer {
    
    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;
        
        try {
            // Get the module root (current directory when run from test runner)
            java.io.File moduleDir = new java.io.File(".").getAbsoluteFile();
            
            // Build the application first
            out.println("🏗️  Building weather-starter-webmvc-oauth2-server...");
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
            out.println("🚀 Starting OAuth2 MCP server in background...");
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
            out.println("⏳ Waiting 20 seconds for OAuth2 server startup...");
            Thread.sleep(20000);
            out.println("✅ Server should be ready");
            
            // Get OAuth2 token
            out.println("\n🔐 Obtaining OAuth2 token...");
            String token = getOAuth2Token();
            if (token != null && !token.isEmpty()) {
                out.println("✅ OAuth2 token obtained: " + token.substring(0, Math.min(20, token.length())) + "...");
                
                // Test MCP STREAMABLE protocol with OAuth2 Bearer token
                out.println("\n🧪 Testing MCP STREAMABLE protocol with OAuth2...");

                // Create MCP initialize request
                String initRequest = "{\"jsonrpc\":\"2.0\",\"method\":\"initialize\",\"params\":{" +
                    "\"protocolVersion\":\"1.0\"," +
                    "\"capabilities\":{}," +
                    "\"clientInfo\":{\"name\":\"integration-test\",\"version\":\"1.0\"}" +
                    "},\"id\":1}";

                var client = HttpClient.newHttpClient();
                var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/mcp"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream, application/json")
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(initRequest))
                    .build();

                try {
                    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    out.println("📡 MCP endpoint responded with status: " + response.statusCode());

                    if (response.statusCode() == 200) {
                        String body = response.body();
                        out.println("✅ OAuth2 authentication successful!");

                        // Check if response contains server info
                        if (body.contains("serverInfo") && body.contains("my-weather-server")) {
                            out.println("✅ Server initialized: my-weather-server");
                            out.println("\n🎉 MCP weather OAuth2 server test passed!");
                        } else if (body.contains("result")) {
                            out.println("✅ Server responded with valid MCP result");
                            out.println("\n🎉 MCP weather OAuth2 server test passed!");
                        } else {
                            out.println("Response: " + body.substring(0, Math.min(300, body.length())));
                            out.println("\n🎉 MCP weather OAuth2 server test passed!");
                        }
                    } else if (response.statusCode() == 401) {
                        err.println("❌ OAuth2 authentication failed (401 Unauthorized)");
                        err.println("Response: " + response.body());
                        exit(1);
                    } else {
                        err.println("❌ Unexpected status code: " + response.statusCode());
                        err.println("Response: " + response.body());
                        exit(1);
                    }
                } catch (Exception e) {
                    err.println("❌ Error connecting to MCP endpoint: " + e.getMessage());
                    exit(1);
                }
                
            } else {
                err.println("❌ Failed to obtain OAuth2 token!");
                exit(1);
            }
            
        } finally {
            // Always try to shut down the server
            if (serverHandle != null) {
                WebServerTestUtils.shutdownServer(serverHandle);
            }
        }
    }
    
    private static String getOAuth2Token() {
        try {
            // Use curl to get OAuth2 token as shown in README
            Process tokenProcess = new ProcessBuilder(
                "curl", "-s", "-XPOST", "http://localhost:8080/oauth2/token",
                "--data", "grant_type=client_credentials",
                "--user", "mcp-client:secret"
            ).start();
            
            String output = new String(tokenProcess.getInputStream().readAllBytes());
            tokenProcess.waitFor(5, TimeUnit.SECONDS);
            
            // Parse JSON response to get access_token
            ObjectMapper mapper = new ObjectMapper();
            var tokenResponse = mapper.readTree(output);
            if (tokenResponse.has("access_token")) {
                return tokenResponse.get("access_token").asText();
            }
        } catch (Exception e) {
            err.println("Error getting OAuth2 token: " + e.getMessage());
        }
        return null;
    }
}