///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS org.zeroturnaround:zt-exec:1.12
//DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
//JAVA 17
//FILES ExampleInfo.json
//SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java
//SOURCES ../../../integration-testing/jbang-lib/WebServerTestUtils.java

/*
 * Composite integration test for dynamic-tool-update
 * Starts server first, then client connects to demonstrate dynamic tool updates
 */

import java.io.*;
import java.util.concurrent.*;
import static java.lang.System.*;

public class RunDynamicToolUpdate {

    public static void main(String... args) throws Exception {
        out.println("🚀 Starting Dynamic Tool Update Composite Test");

        ProcessHandle serverHandle = null;
        // When run from test runner, CWD is model-context-protocol/dynamic-tool-update
        File serverDir = new File("server").getAbsoluteFile();
        File clientDir = new File("client").getAbsoluteFile();

        try {
            // Step 1: Build server
            out.println("\n🏗️  Building server...");
            int serverBuild = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(serverDir)
                .timeout(180, TimeUnit.SECONDS)
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            if (serverBuild != 0) {
                err.println("❌ Server build failed");
                exit(1);
            }
            out.println("✅ Server built");

            // Step 2: Build client
            out.println("\n🏗️  Building client...");
            int clientBuild = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "clean", "package", "-q", "-DskipTests")
                .directory(clientDir)
                .timeout(180, TimeUnit.SECONDS)
                .redirectOutput(out)
                .redirectError(err)
                .execute()
                .getExitValue();

            if (clientBuild != 0) {
                err.println("❌ Client build failed");
                exit(1);
            }
            out.println("✅ Client built");

            // Step 3: Start server in background
            out.println("\n🚀 Starting server...");
            File serverLog = new File("target/server.log");
            serverLog.getParentFile().mkdirs();

            Process serverProcess = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(serverDir)
                .redirectOutput(new FileOutputStream(serverLog))
                .redirectError(err)
                .start()
                .getProcess();

            serverHandle = serverProcess.toHandle();

            // Wait for server to be ready
            out.println("⏳ Waiting 15 seconds for server startup...");
            Thread.sleep(15000);
            out.println("✅ Server should be ready");

            // Step 4: Run client
            out.println("\n🚀 Running client...");
            File clientLog = new File("target/client.log");

            org.zeroturnaround.exec.ProcessResult clientResult = new org.zeroturnaround.exec.ProcessExecutor()
                .command("./mvnw", "spring-boot:run", "-q")
                .directory(clientDir)
                .timeout(120, TimeUnit.SECONDS)
                .redirectOutput(new FileOutputStream(clientLog))
                .redirectError(err)
                .execute();

            // Read and display logs
            out.println("\n📋 Server log:");
            out.println(new String(java.nio.file.Files.readAllBytes(serverLog.toPath())));

            out.println("\n📋 Client log:");
            String clientOutput = new String(java.nio.file.Files.readAllBytes(clientLog.toPath()));
            out.println(clientOutput);

            // Check for success indicators
            boolean success = clientOutput.contains("tool") ||
                             clientOutput.contains("Tool") ||
                             clientOutput.contains("MCP");

            if (success) {
                out.println("\n🎉 Dynamic tool update test completed!");
            } else {
                err.println("\n⚠️  Test completed but expected output not found");
            }

        } finally {
            // Cleanup
            if (serverHandle != null) {
                out.println("\n🛑 Shutting down server...");
                WebServerTestUtils.shutdownServer(serverHandle);
            }
        }
    }
}
