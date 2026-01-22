///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Integration test launcher for document-forge
 * Tests Claude Skills document generation web application
 */

import java.util.List;
import java.util.Map;

public class RunDocumentForge {

    public static void main(String... args) throws Exception {
        // Check for required environment variable
        if (System.getenv("ANTHROPIC_API_KEY") == null) {
            System.err.println("❌ Missing required environment variable: ANTHROPIC_API_KEY");
            System.exit(1);
        }

        // Define endpoint test for the web UI
        // Pattern uses non-capturing group for proper alternation
        var endpointTest = new WebServerTestUtils.EndpointTest(
            "http://localhost:8080/",
            "GET",
            "(?:Document Forge|document-forge|Generate|Claude)",
            Map.of()
        );

        // Configure web server test
        var config = new WebServerTestUtils.WebServerConfig(
            "http://localhost:8080/",  // Health check URL
            20,  // Startup timeout in seconds
            List.of(endpointTest)  // Endpoints to test
        );

        // Run the test
        WebServerTestUtils.runWebServerTest("document-forge", config);
    }
}
