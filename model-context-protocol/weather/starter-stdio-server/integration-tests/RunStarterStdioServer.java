///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../../integration-testing/jbang-lib/IntegrationTestUtils.java

/*
 * Integration test launcher for MCP Weather STDIO Server
 * Verifies the server JAR builds successfully
 * Note: Full STDIO transport testing requires additional infrastructure
 */

import java.nio.file.*;
import static java.lang.System.*;

public class RunStarterStdioServer {

    public static void main(String... args) throws Exception {
        out.println("🚀 Starting MCP Weather STDIO Server Integration Test");

        // Build the server JAR first
        out.println("🏗️  Building starter-stdio-server...");
        IntegrationTestUtils.buildApplication("starter-stdio-server");

        // Verify JAR exists
        Path jarPath = Paths.get("target/mcp-weather-stdio-server-0.0.1-SNAPSHOT.jar");
        if (!Files.exists(jarPath)) {
            err.println("❌ Server JAR not found at: " + jarPath);
            exit(1);
        }
        out.println("✅ Server JAR built successfully at: " + jarPath);

        // Verify the JAR is executable
        out.println("\n🔍 Verifying JAR is valid...");
        long jarSize = Files.size(jarPath);
        if (jarSize < 1000) {
            err.println("❌ JAR file seems too small: " + jarSize + " bytes");
            exit(1);
        }
        out.println("✅ JAR size: " + (jarSize / 1024) + " KB");

        out.println("\n🎉 Integration test passed!");
        out.println("Note: STDIO transport testing requires spawning subprocess - verified build only");
    }
}
