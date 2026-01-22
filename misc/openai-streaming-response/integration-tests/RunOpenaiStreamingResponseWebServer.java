///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Web server integration test for openai-streaming-response
 * Tests the streaming endpoint by making HTTP requests
 */

import java.util.List;
import java.util.Map;

public class RunOpenaiStreamingResponseWebServer {
    
    public static void main(String... args) throws Exception {
        // Check for required environment variable
        if (System.getenv("OPENAI_API_KEY") == null) {
            System.err.println("❌ Missing required environment variable: OPENAI_API_KEY");
            System.exit(1);
        }

        // Define the endpoint tests
        // The SSE stream returns ChatResponse objects with joke content word by word
        var endpointTest = new WebServerTestUtils.EndpointTest(
            "http://localhost:8080/ai/generateStream?message=Tell%20me%20a%20short%20joke",
            "GET",
            // Validate we get SSE data events with chat completion and text content
            // Pattern matches: data:{"metadata":...,"id":"chatcmpl-...,"text":"..."...}
            "data:.*chatcmpl.*text",
            Map.of("Accept", "text/event-stream")
        );
        
        // Configure web server test
        var config = new WebServerTestUtils.WebServerConfig(
            null,  // No health check, will just wait for startup time
            15,  // Startup timeout in seconds (give it time to start)
            List.of(endpointTest)  // Endpoints to test
        );
        
        // Run the test
        WebServerTestUtils.runWebServerTest("openai-streaming-response", config);
    }
}