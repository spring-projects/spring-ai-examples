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

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import static java.lang.System.*;

public class RunWeatherStarterWebmvcOauth2ServerWebServer {

    public static void main(String... args) throws Exception {
        WebServerTestUtils.runSimpleWebServerTest(
            WebServerTestUtils.WebServerTestConfig.withWait(
                "weather-starter-webmvc-oauth2-server",
                20,
                RunWeatherStarterWebmvcOauth2ServerWebServer::validateOAuth2McpServer
            )
        );
    }

    private static boolean validateOAuth2McpServer() {
        try {
            // Get OAuth2 token
            out.println("🔐 Obtaining OAuth2 token...");
            String token = getOAuth2Token();
            if (token == null || token.isEmpty()) {
                err.println("❌ Failed to obtain OAuth2 token!");
                return false;
            }
            out.println("✅ OAuth2 token obtained: " + token.substring(0, Math.min(20, token.length())) + "...");

            // Test MCP STREAMABLE protocol with OAuth2 Bearer token
            out.println("🧪 Testing MCP STREAMABLE protocol with OAuth2...");

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

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            out.println("📡 MCP endpoint responded with status: " + response.statusCode());

            if (response.statusCode() == 200) {
                out.println("✅ OAuth2 authentication successful!");
                return true;
            } else if (response.statusCode() == 401) {
                err.println("❌ OAuth2 authentication failed (401 Unauthorized)");
                return false;
            } else {
                err.println("❌ Unexpected status code: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            err.println("❌ Exception: " + e.getMessage());
            return false;
        }
    }

    private static String getOAuth2Token() throws Exception {
        Process tokenProcess = new ProcessBuilder(
            "curl", "-s", "-XPOST", "http://localhost:8080/oauth2/token",
            "--data", "grant_type=client_credentials",
            "--user", "mcp-client:secret"
        ).start();

        String output = new String(tokenProcess.getInputStream().readAllBytes());
        tokenProcess.waitFor(5, TimeUnit.SECONDS);

        ObjectMapper mapper = new ObjectMapper();
        var tokenResponse = mapper.readTree(output);
        if (tokenResponse.has("access_token")) {
            return tokenResponse.get("access_token").asText();
        }
        return null;
    }
}
