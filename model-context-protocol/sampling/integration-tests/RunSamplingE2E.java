///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * MCP Sampling E2E integration test
 * Tests the full MCP Sampling flow with server+client coordination:
 * 1. Start mcp-sampling-server on port 8080
 * 2. Run mcp-sampling-client which connects and triggers weather query
 * 3. Server delegates poem generation to both OpenAI and Anthropic via MCP Sampling
 * 4. Validate output contains poems from both LLM providers
 */

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.zeroturnaround.exec.*;
import static java.lang.System.*;

public class RunSamplingE2E {

    public static void main(String... args) throws Exception {
        ProcessHandle serverHandle = null;
        StringBuilder combinedOutput = new StringBuilder();

        try {
            // Load configuration
            var cfg = IntegrationTestUtils.loadConfig();
            IntegrationTestUtils.verifyEnvironment(cfg);

            // Get module directories
            File samplingDir = new File(".").getAbsoluteFile().getParentFile(); // sampling/
            File serverDir = new File(samplingDir, "mcp-sampling-server");
            File clientDir = new File(samplingDir, "mcp-sampling-client");

            out.println("📁 Sampling directory: " + samplingDir);
            out.println("📁 Server directory: " + serverDir);
            out.println("📁 Client directory: " + clientDir);

            // Step 1: Build the server
            out.println("\n🏗️  Building mcp-sampling-server...");
            new ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(serverDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(System.out)
                .redirectError(System.err)
                .execute();
            out.println("✅ Server built successfully");

            // Step 2: Start server in background
            out.println("\n🚀 Starting mcp-sampling-server in background...");
            File logDir = new File(serverDir, "target/integration-test-logs");
            logDir.mkdirs();
            File serverLogFile = new File(logDir, "server-" + System.currentTimeMillis() + ".log");

            Process serverProcess = new ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(serverDir)
                .redirectOutput(new FileOutputStream(serverLogFile))
                .redirectError(System.err)
                .start()
                .getProcess();

            serverHandle = serverProcess.toHandle();
            out.println("📝 Server log: " + serverLogFile.getAbsolutePath());

            // Step 3: Wait for server to be ready
            out.println("⏳ Waiting 15 seconds for server startup...");
            Thread.sleep(15000);
            out.println("✅ Server should be ready on port 8080");

            // Step 4: Build the client
            out.println("\n🏗️  Building mcp-sampling-client...");
            new ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(clientDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(System.out)
                .redirectError(System.err)
                .execute();
            out.println("✅ Client built successfully");

            // Step 5: Run the client and capture output
            out.println("\n🚀 Running mcp-sampling-client...");
            File clientLogFile = new File(logDir, "client-" + System.currentTimeMillis() + ".log");

            ProcessResult clientResult = new ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(clientDir)
                .timeout(cfg.timeoutSec(), TimeUnit.SECONDS)
                .readOutput(true)
                .redirectErrorStream(true)
                .execute();

            String clientOutput = clientResult.outputUTF8();
            combinedOutput.append(clientOutput);

            // Save client output to log file
            Files.writeString(clientLogFile.toPath(), clientOutput);
            out.println("📝 Client log: " + clientLogFile.getAbsolutePath());

            // Display output
            out.println("\n📋 Client Output:");
            out.println("---");
            out.println(clientOutput);
            out.println("---");

            // Step 6: Validate output
            Path combinedLogFile = logDir.toPath().resolve("combined-" + System.currentTimeMillis() + ".log");
            Files.writeString(combinedLogFile, combinedOutput.toString());

            boolean isPrimaryAiMode = cfg.aiValidation() != null &&
                cfg.aiValidation().enabled() &&
                "primary".equals(cfg.aiValidation().validationMode());

            // Check regex patterns (informational for primary mode, required for hybrid/fallback)
            out.println("\n✅ Verifying output patterns...");
            boolean allPatternsFound = true;

            for (String pattern : cfg.successRegex()) {
                if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(clientOutput).find()) {
                    out.println("  ✅ Found: " + pattern);
                } else {
                    if (isPrimaryAiMode) {
                        out.println("  ℹ️  Pattern not found (AI will validate): " + pattern);
                    } else {
                        err.println("  ❌ Missing: " + pattern);
                    }
                    allPatternsFound = false;
                }
            }

            // For non-primary modes, regex failure is fatal
            if (!allPatternsFound && !isPrimaryAiMode) {
                err.println("\n❌ Not all expected patterns were found in output");
                exit(1);
            }

            // Step 7: AI Validation
            if (cfg.aiValidation() != null && cfg.aiValidation().enabled()) {
                out.println("\n🤖 Using AI validation as primary validation method");
                boolean aiResult = IntegrationTestUtils.performAIValidation(
                    combinedOutput.toString(),
                    combinedLogFile,
                    cfg,
                    "sampling-e2e"
                );

                if (!aiResult) {
                    err.println("\n❌ AI validation failed");
                    exit(1);
                }
            }

            out.println("\n🎉 MCP Sampling E2E test completed successfully!");
            out.println("   - Server started and handled weather query");
            out.println("   - Client connected via MCP SSE");
            out.println("   - Creative weather content generated via MCP Sampling");

        } finally {
            // Always try to shut down the server
            if (serverHandle != null) {
                WebServerTestUtils.shutdownServer(serverHandle);
            }
        }
    }
}
