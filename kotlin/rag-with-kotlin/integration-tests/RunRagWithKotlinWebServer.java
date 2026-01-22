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

import java.io.*;
import java.net.http.*;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import static java.lang.System.*;

public class RunRagWithKotlinWebServer {

    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;

        try {
            // Check for required env var
            if (getenv("OPENAI_API_KEY") == null && getenv("SPRING_AI_OPENAI_API_KEY") == null) {
                err.println("Missing required environment variable: OPENAI_API_KEY");
                exit(1);
            }

            File moduleDir = new File(".").getAbsoluteFile();

            // Build the application
            out.println("Building rag-with-kotlin...");
            new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(moduleDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(out)
                .redirectError(err)
                .execute();

            // Create log directory
            File logDir = new File(moduleDir, "target/integration-test-logs");
            logDir.mkdirs();
            var logFile = new File(logDir, "web-server-" + currentTimeMillis() + ".log");

            // Start server in background
            out.println("Starting web server in background...");
            Process process = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(moduleDir)
                .redirectOutput(new FileOutputStream(logFile))
                .redirectError(err)
                .start()
                .getProcess();

            serverHandle = process.toHandle();

            // Wait for server to start and RAG workflow to complete
            out.println("Waiting for server startup and RAG workflow (45s)...");
            Thread.sleep(45000);

            // Read log to verify RAG workflow completed
            String logContent = new String(java.nio.file.Files.readAllBytes(logFile.toPath()));
            out.println("\nLog file: " + logFile.getAbsolutePath());

            boolean hasRagOutput = logContent.contains("DogAdoptionSuggestion");
            boolean hasStarted = logContent.contains("Started RagWithKotlin");
            boolean hasDogsLoaded = logContent.contains("got a dog");

            if (!hasStarted) {
                err.println("Server did not start properly");
                out.println("\n--- Log Output ---\n" + logContent);
                exit(1);
            }
            out.println("Server started successfully");

            if (!hasDogsLoaded) {
                err.println("Dogs were not loaded into vector store");
                exit(1);
            }
            out.println("Dogs loaded into vector store");

            if (!hasRagOutput) {
                err.println("RAG query did not produce DogAdoptionSuggestion");
                exit(1);
            }
            out.println("RAG workflow completed");

            // Test the /dogs endpoint
            out.println("\nTesting /dogs endpoint...");
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .build();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8090/dogs"))
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                err.println("/dogs endpoint returned status " + response.statusCode());
                exit(1);
            }

            String body = response.body();
            if (!body.contains("name") || !body.contains("description")) {
                err.println("/dogs endpoint returned unexpected response: " + body);
                exit(1);
            }
            out.println("/dogs endpoint returned dog data");

            out.println("\nIntegration test completed successfully!");

        } finally {
            if (serverHandle != null) {
                WebServerTestUtils.shutdownServer(serverHandle);
            }
        }
    }
}
