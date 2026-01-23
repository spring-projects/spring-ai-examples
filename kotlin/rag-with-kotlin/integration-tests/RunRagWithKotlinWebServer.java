///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Web server integration test for kotlin/rag-with-kotlin
 * Tests the RAG-powered dog adoption demo
 */

import static java.lang.System.*;

public class RunRagWithKotlinWebServer {

    public static void main(String... args) throws Exception {
        // Check for required env var
        if (getenv("OPENAI_API_KEY") == null && getenv("SPRING_AI_OPENAI_API_KEY") == null) {
            err.println("Missing required environment variable: OPENAI_API_KEY");
            exit(1);
        }

        WebServerTestUtils.runSimpleWebServerTest(
            WebServerTestUtils.WebServerTestConfig.withWait(
                "rag-with-kotlin",
                45,  // RAG workflow needs time to load dogs into vector store
                RunRagWithKotlinWebServer::validateRagWorkflow
            )
        );
    }

    private static boolean validateRagWorkflow() {
        try {
            // Read server log to verify RAG workflow completed
            String logContent = WebServerTestUtils.readServerLog();

            boolean hasStarted = logContent.contains("Started RagWithKotlin");
            boolean hasDogsLoaded = logContent.contains("got a dog");
            boolean hasRagOutput = logContent.contains("DogAdoptionSuggestion");

            if (!hasStarted) {
                err.println("Server did not start properly");
                return false;
            }
            out.println("✅ Server started successfully");

            if (!hasDogsLoaded) {
                err.println("Dogs were not loaded into vector store");
                return false;
            }
            out.println("✅ Dogs loaded into vector store");

            if (!hasRagOutput) {
                err.println("RAG query did not produce DogAdoptionSuggestion");
                return false;
            }
            out.println("✅ RAG workflow completed");

            // Test the /dogs endpoint
            out.println("Testing /dogs endpoint...");
            var response = WebServerTestUtils.httpGet("http://localhost:8090/dogs");

            if (response.statusCode() != 200) {
                err.println("/dogs endpoint returned status " + response.statusCode());
                return false;
            }

            String body = response.body();
            if (!body.contains("name") || !body.contains("description")) {
                err.println("/dogs endpoint returned unexpected response");
                return false;
            }
            out.println("✅ /dogs endpoint returned dog data");

            return true;
        } catch (Exception e) {
            err.println("Exception: " + e.getMessage());
            return false;
        }
    }
}
