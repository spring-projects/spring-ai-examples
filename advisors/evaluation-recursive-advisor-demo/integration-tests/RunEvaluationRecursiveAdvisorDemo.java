///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java

/*
 * Integration test launcher for evaluation-recursive-advisor-demo
 * Tests LLM-as-a-Judge pattern with self-refining recursive advisors
 * Requires Ollama with flow-judge model running locally
 */

import java.net.http.*;
import java.net.URI;
import java.time.Duration;

public class RunEvaluationRecursiveAdvisorDemo {

    private static final String OLLAMA_MODEL = "avcodes/flowaicom-flow-judge:q4";

    public static void main(String... args) throws Exception {
        // Check for required environment variable
        if (System.getenv("ANTHROPIC_API_KEY") == null) {
            System.err.println("❌ Missing required environment variable: ANTHROPIC_API_KEY");
            System.exit(1);
        }

        // Check if Ollama is running
        if (!isOllamaRunning()) {
            System.err.println("❌ Ollama is not running at http://localhost:11434");
            System.err.println("   Start Ollama with: ollama serve");
            System.exit(1);
        }
        System.out.println("✅ Ollama is running");

        // Check if required model is available
        if (!isModelAvailable(OLLAMA_MODEL)) {
            System.err.println("❌ Required model not found: " + OLLAMA_MODEL);
            System.err.println("   Pull the model with: ollama pull " + OLLAMA_MODEL);
            System.exit(1);
        }
        System.out.println("✅ Required model available: " + OLLAMA_MODEL);

        // Run the integration test
        IntegrationTestUtils.runIntegrationTest("evaluation-recursive-advisor-demo");
    }

    private static boolean isOllamaRunning() {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isModelAvailable(String modelName) {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Simple check - model name should appear in the response
            return response.body().contains(modelName.split(":")[0]);
        } catch (Exception e) {
            return false;
        }
    }
}
