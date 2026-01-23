/*
 * Utilities for testing web server applications
 * Handles server startup, health checks, HTTP requests, and shutdown
 */

import com.fasterxml.jackson.databind.*;
import org.zeroturnaround.exec.*;
import java.nio.file.*;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.function.Supplier;
import java.io.File;
import java.io.FileOutputStream;
import static java.lang.System.*;

public class WebServerTestUtils {

    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    // Configuration for the simplified web server test runner
    public record WebServerTestConfig(
        String moduleName,         // Name for logging
        int startupWaitSeconds,    // Time to wait for server startup
        String healthCheckUrl,     // Optional URL to check (null = just wait)
        Supplier<Boolean> validator // Validation logic returning true on success
    ) {
        // Builder-style factory methods for convenience
        public static WebServerTestConfig withWait(String name, int seconds, Supplier<Boolean> validator) {
            return new WebServerTestConfig(name, seconds, null, validator);
        }

        public static WebServerTestConfig withHealthCheck(String name, String healthUrl, int timeout, Supplier<Boolean> validator) {
            return new WebServerTestConfig(name, timeout, healthUrl, validator);
        }
    }

    // Extended configuration for web server tests (legacy)
    public record WebServerConfig(
        String healthCheck,        // URL to check if server is ready
        int startupTime,          // Seconds to wait for startup
        List<EndpointTest> testEndpoints  // Endpoints to test
    ) {}

    public record EndpointTest(
        String url,
        String method,
        String expectedPattern,
        Map<String, String> headers
    ) {}

    /**
     * Simplified web server integration test runner.
     * Handles all boilerplate: build, start, wait, validate, shutdown.
     *
     * @param config Test configuration with module name, timing, and validator
     * @throws Exception if test fails
     */
    public static void runSimpleWebServerTest(WebServerTestConfig config) throws Exception {
        ProcessHandle serverHandle = null;
        File logFile = null;

        try {
            File moduleDir = new File(".").getAbsoluteFile();

            // Build the application
            out.println("🏗️  Building " + config.moduleName() + "...");
            new ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(moduleDir)
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(out)
                .redirectError(err)
                .execute();

            // Create log directory
            File logDir = new File(moduleDir, "target/integration-test-logs");
            logDir.mkdirs();
            logFile = new File(logDir, "web-server-" + System.currentTimeMillis() + ".log");

            // Start server in background
            out.println("🚀 Starting web server in background...");
            Process process = new ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(moduleDir)
                .redirectOutput(new FileOutputStream(logFile))
                .redirectError(err)
                .start()
                .getProcess();

            serverHandle = process.toHandle();

            // Wait for server to be ready
            if (config.healthCheckUrl() != null) {
                boolean ready = waitForHealthCheck(config.healthCheckUrl(), config.startupWaitSeconds());
                if (!ready) {
                    throw new RuntimeException("Server failed to start - health check failed");
                }
            } else {
                out.println("⏳ Waiting " + config.startupWaitSeconds() + " seconds for server startup...");
                Thread.sleep(config.startupWaitSeconds() * 1000L);
                out.println("✅ Server should be ready");
            }

            // Run validation
            out.println("🧪 Running validation...");
            boolean success = config.validator().get();

            if (success) {
                out.println("🎉 " + config.moduleName() + " test passed!");
            } else {
                err.println("❌ " + config.moduleName() + " test failed!");
                exit(1);
            }

        } finally {
            if (serverHandle != null) {
                shutdownServer(serverHandle);
            }
        }
    }

    /**
     * Read the server log file for validation purposes.
     * Call this from your validator if you need to check log contents.
     */
    public static String readServerLog() throws Exception {
        File logDir = new File("target/integration-test-logs");
        File[] logs = logDir.listFiles((dir, name) -> name.startsWith("web-server-"));
        if (logs != null && logs.length > 0) {
            // Get the most recent log
            File latestLog = logs[0];
            for (File log : logs) {
                if (log.lastModified() > latestLog.lastModified()) {
                    latestLog = log;
                }
            }
            return Files.readString(latestLog.toPath());
        }
        return "";
    }

    /**
     * Simple HTTP GET request helper for validation.
     */
    public static HttpResponse<String> httpGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    
    // Start server in background and return process handle
    public static ProcessHandle startServerInBackground() throws Exception {
        out.println("🚀 Starting web server in background...");
        
        // Create logs directory if it doesn't exist
        Path logDir = Paths.get("target/integration-test-logs");
        Files.createDirectories(logDir);
        
        ProcessBuilder pb = new ProcessBuilder("./mvnw", "spring-boot:run", "-q");
        pb.redirectOutput(ProcessBuilder.Redirect.to(
            logDir.resolve("web-server-" + System.currentTimeMillis() + ".log").toFile()));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        
        Process process = pb.start();
        return process.toHandle();
    }
    
    // Wait for health check endpoint to respond
    public static boolean waitForHealthCheck(String healthCheckUrl, int timeoutSeconds) {
        out.println("⏳ Waiting for server to be ready...");
        long startTime = System.currentTimeMillis();
        long timeout = timeoutSeconds * 1000L;
        
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthCheckUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();
                    
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                    
                if (response.statusCode() == 200) {
                    out.println("✅ Server is ready!");
                    return true;
                }
            } catch (Exception e) {
                // Server not ready yet, keep trying
            }
            
            try {
                Thread.sleep(1000); // Wait 1 second before retry
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        err.println("❌ Server failed to start within " + timeoutSeconds + " seconds");
        return false;
    }
    
    // Test an endpoint and verify response
    public static boolean testEndpoint(EndpointTest test) throws Exception {
        out.println("🔍 Testing endpoint: " + test.url());
        
        // Check if this is an SSE endpoint
        boolean isSSE = test.headers() != null && 
            test.headers().getOrDefault("Accept", "").contains("event-stream");
        
        if (isSSE) {
            return testSSEEndpoint(test);
        }
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(test.url()))
            .timeout(Duration.ofSeconds(30));
        
        // Add headers if provided
        if (test.headers() != null) {
            test.headers().forEach(requestBuilder::header);
        }
        
        // Set HTTP method
        switch (test.method().toUpperCase()) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + test.method());
        }
        
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        String body = response.body();
        out.println("📡 Response status: " + response.statusCode());
        out.println("📝 Response body (first 500 chars): " + 
            body.substring(0, Math.min(500, body.length())));
        
        // Check if response matches expected pattern
        if (body.matches("(?s).*" + test.expectedPattern() + ".*")) {
            out.println("✅ Response matches expected pattern");
            return true;
        } else {
            err.println("❌ Response doesn't match pattern: " + test.expectedPattern());
            return false;
        }
    }
    
    // Test SSE endpoint by capturing a few seconds of stream
    private static boolean testSSEEndpoint(EndpointTest test) throws Exception {
        out.println("🌊 Testing SSE stream endpoint...");
        
        ProcessBuilder pb = new ProcessBuilder(
            "timeout", "10", "curl", "-s", "-N",
            "-H", "Accept: text/event-stream",
            test.url()
        );
        
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        process.waitFor();
        String sseData = output.toString();
        
        out.println("📡 SSE stream sample (first 1000 chars): " + 
            sseData.substring(0, Math.min(1000, sseData.length())));
        
        // Check if SSE data matches expected pattern
        if (test.expectedPattern() == null || test.expectedPattern().isEmpty()) {
            // Empty pattern means we just check if SSE endpoint responds
            out.println("✅ SSE endpoint responded (no pattern check)");
            return true;
        } else if (sseData.matches("(?s).*" + test.expectedPattern() + ".*")) {
            out.println("✅ SSE stream contains expected pattern");
            return true;
        } else {
            err.println("❌ SSE stream doesn't match pattern: " + test.expectedPattern());
            err.println("📝 Full SSE output: " + sseData);
            return false;
        }
    }
    
    // Gracefully shutdown the server
    public static void shutdownServer(ProcessHandle handle) {
        out.println("🛑 Shutting down server...");
        
        if (handle != null && handle.isAlive()) {
            handle.destroy(); // Send SIGTERM
            
            try {
                // Wait up to 10 seconds for graceful shutdown
                boolean terminated = handle.onExit()
                    .get(10, TimeUnit.SECONDS) != null;
                    
                if (!terminated && handle.isAlive()) {
                    out.println("⚠️ Forcing server shutdown...");
                    handle.destroyForcibly();
                }
                
                out.println("✅ Server shut down");
            } catch (Exception e) {
                err.println("⚠️ Error shutting down server: " + e.getMessage());
                if (handle.isAlive()) {
                    handle.destroyForcibly();
                }
            }
        }
    }
    
    // Main test execution for web servers
    public static void runWebServerTest(String moduleName, WebServerConfig config) throws Exception {
        ProcessHandle serverHandle = null;
        
        try {
            // Build the application first
            out.println("🏗️  Building " + moduleName + "...");
            new ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .timeout(300, TimeUnit.SECONDS)
                .redirectOutput(System.out)
                .redirectError(System.err)
                .execute();
            
            // Start server in background
            serverHandle = startServerInBackground();
            
            // Wait for server to be ready
            if (config.healthCheck() != null) {
                boolean ready = waitForHealthCheck(
                    config.healthCheck(),
                    config.startupTime() > 0 ? config.startupTime() : 30
                );
                
                if (!ready) {
                    throw new RuntimeException("Server failed to start");
                }
            } else {
                // No health check, just wait for startup time
                out.println("⏳ Waiting " + config.startupTime() + " seconds for server startup...");
                Thread.sleep(config.startupTime() * 1000L);
                out.println("✅ Assuming server is ready after wait time");
            }
            
            // Test all configured endpoints
            boolean allTestsPassed = true;
            if (config.testEndpoints() != null) {
                for (EndpointTest test : config.testEndpoints()) {
                    if (!testEndpoint(test)) {
                        allTestsPassed = false;
                    }
                }
            }
            
            if (allTestsPassed) {
                out.println("🎉 All web server tests passed!");
            } else {
                err.println("❌ Some tests failed");
                exit(1);
            }
            
        } finally {
            // Always try to shut down the server
            if (serverHandle != null) {
                shutdownServer(serverHandle);
            }
        }
    }
}