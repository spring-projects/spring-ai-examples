///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Web server integration test for weather/starter-webmvc-server
 * Tests the MCP weather server SSE endpoint
 */

import java.util.List;
import java.util.Map;

public class RunWeatherStarterWebmvcServerWebServer {
    
    public static void main(String... args) throws Exception {
        // Define the endpoint tests
        var endpointTest = new WebServerTestUtils.EndpointTest(
            "http://localhost:8080/mcp/message",
            "GET",
            "text/event-stream",  // Looking for SSE content type
            Map.of("Accept", "text/event-stream")
        );
        
        // Configure web server test
        var config = new WebServerTestUtils.WebServerConfig(
            null,  // No health check endpoint available
            15,  // Startup timeout in seconds
            List.of(endpointTest)  // Endpoints to test
        );
        
        // Run the test
        WebServerTestUtils.runWebServerTest("weather-starter-webmvc-server", config);
    }
}
EOF < /dev/null
