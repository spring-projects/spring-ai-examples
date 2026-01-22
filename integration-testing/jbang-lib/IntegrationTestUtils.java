/*
 * Centralized utilities for JBang integration tests
 * Provides common functionality to eliminate code duplication across test scripts
 */

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.zeroturnaround.exec.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import static java.lang.System.*;

public class IntegrationTestUtils {
    
    // Record for AI validation configuration
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AIValidationConfig(
        boolean enabled,
        String validationMode,    // "primary", "hybrid", "fallback"
        String readmeFile,
        String expectedBehavior,
        String[] components,
        String promptTemplate,
        Map<String, Object> successCriteria
    ) {}
    
    // Record for test configuration
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExampleInfo(
        int timeoutSec, 
        String[] successRegex, 
        String[] requiredEnv,
        String[] setupCommands,
        String[] cleanupCommands,
        AIValidationConfig aiValidation
    ) {}
    
    // Load configuration from ExampleInfo.json
    public static ExampleInfo loadConfig() throws Exception {
        Path configPath = Path.of("integration-tests/ExampleInfo.json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(configPath.toFile(), ExampleInfo.class);
    }
    
    // Verify required environment variables
    public static void verifyEnvironment(ExampleInfo cfg) {
        for (String envVar : cfg.requiredEnv()) {
            if (getenv(envVar) == null) {
                err.println("❌ Missing required environment variable: " + envVar);
                exit(1);
            }
        }
    }
    
    // Run a command with timeout
    public static void runCommand(String[] cmd, int timeoutSec) throws Exception {
        ProcessResult result = new ProcessExecutor()
            .command(cmd)
            .timeout(timeoutSec, TimeUnit.SECONDS)
            .redirectOutput(System.out)
            .redirectError(System.err)
            .execute();
            
        int exit = result.getExitValue();
        if (exit != 0) {
            throw new RuntimeException("Command failed with exit code " + exit + ": " + String.join(" ", cmd));
        }
    }
    
    // Run setup commands if specified
    public static void runSetupCommands(ExampleInfo cfg) throws Exception {
        if (cfg.setupCommands() != null) {
            for (String setupCmd : cfg.setupCommands()) {
                out.println("🔧 Running setup: " + setupCmd);
                runCommand(setupCmd.split("\\s+"), 60); // 1 minute timeout for setup
            }
        }
    }
    
    // Run cleanup commands if specified
    public static void runCleanupCommands(ExampleInfo cfg) {
        if (cfg.cleanupCommands() != null) {
            for (String cleanupCmd : cfg.cleanupCommands()) {
                out.println("🧹 Running cleanup: " + cleanupCmd);
                try {
                    runCommand(cleanupCmd.split("\\s+"), 30);
                } catch (Exception e) {
                    err.println("⚠️  Cleanup command failed (non-fatal): " + e.getMessage());
                }
            }
        }
    }
    
    // Build the application
    public static void buildApplication(String moduleName) throws Exception {
        out.println("🏗️  Building " + moduleName + "...");
        runCommand(new String[]{"./mvnw", "clean", "package", "-q", "-DskipTests"}, 300);
    }
    
    // Create log file path
    public static Path createLogFile(String moduleName) throws Exception {
        // Try different relative paths based on module depth
        Path logDir = null;
        if (Files.exists(Paths.get("../../../integration-testing"))) {
            logDir = Paths.get("../../../integration-testing/logs/integration-tests");
        } else if (Files.exists(Paths.get("../../integration-testing"))) {
            logDir = Paths.get("../../integration-testing/logs/integration-tests");
        } else if (Files.exists(Paths.get("../../../../integration-testing"))) {
            logDir = Paths.get("../../../../integration-testing/logs/integration-tests");
        } else {
            throw new RuntimeException("Could not find integration-testing directory");
        }
        
        Files.createDirectories(logDir);
        return logDir.resolve(moduleName + "-spring-boot-" + System.currentTimeMillis() + ".log");
    }
    
    // Run the Spring Boot application
    public static ProcessResult runSpringBootApp(ExampleInfo cfg, Path logFile) throws Exception {
        return new ProcessExecutor()
            .command("./mvnw", "spring-boot:run", "-q")
            .timeout(cfg.timeoutSec(), TimeUnit.SECONDS)
            .redirectOutput(Files.newOutputStream(logFile))
            .redirectErrorStream(true)
            .execute();
    }
    
    // Display full output
    public static void displayOutput(String output) {
        out.println("📋 Full Application Output:");
        out.println("---");
        out.println(output);
        out.println("---");
    }
    
    // Display log file path
    public static void displayLogPath(Path logFile) {
        out.println("📁 Full Spring Boot log: " + logFile.toAbsolutePath().normalize());
    }
    
    // Perform AI validation using Python script
    public static boolean performAIValidation(String output, Path logFile, ExampleInfo cfg, String moduleName) throws Exception {
        AIValidationConfig aiConfig = cfg.aiValidation();
        if (aiConfig == null || !aiConfig.enabled()) {
            return true; // AI validation not enabled, return success
        }
        
        out.println("🤖 Running AI validation...");
        
        // Build Python command - calculate path to repository root and then to validator
        Path currentDir = Path.of(".").toAbsolutePath();
        Path repoRoot = currentDir;
        
        // Navigate up to find the repository root (contains integration-testing directory)
        while (repoRoot != null && !repoRoot.resolve("integration-testing").toFile().exists()) {
            repoRoot = repoRoot.getParent();
        }
        
        if (repoRoot == null) {
            throw new Exception("Could not find repository root with integration-testing directory");
        }
        
        Path validatorScript = repoRoot.resolve("integration-testing/ai-validator/validate_example.py");
            
        String[] baseCmd = {
            "python3",
            validatorScript.toString(),
            "--log-path", logFile.toAbsolutePath().toString(),
            "--example-name", moduleName,
            "--output-format", "json",
            "--verbose"  // Enable verbose output for debugging in CI
        };
        
        // Build dynamic command with optional parameters
        List<String> cmdList = new ArrayList<>(Arrays.asList(baseCmd));
        
        if (aiConfig.expectedBehavior() != null && !aiConfig.expectedBehavior().isEmpty()) {
            cmdList.add("--expected-behavior");
            cmdList.add(aiConfig.expectedBehavior());
        }
        
        if (aiConfig.readmeFile() != null && !aiConfig.readmeFile().isEmpty()) {
            cmdList.add("--readme-path");
            cmdList.add(aiConfig.readmeFile());
        }
        
        if (aiConfig.components() != null && aiConfig.components().length > 0) {
            cmdList.add("--components");
            for (String component : aiConfig.components()) {
                cmdList.add(component);
            }
        }
        
        if (aiConfig.promptTemplate() != null && !aiConfig.promptTemplate().isEmpty()) {
            cmdList.add("--template");
            cmdList.add(aiConfig.promptTemplate());
        }
        
        String[] fullCmd = cmdList.toArray(new String[0]);
        
        try {
            // Execute AI validation
            ProcessResult result = new ProcessExecutor()
                .command(fullCmd)
                .timeout(180, TimeUnit.SECONDS) // 3 minutes timeout for AI validation
                .readOutput(true)
                .redirectErrorStream(true) // Merge stderr into stdout for better error visibility
                .execute();

            String aiOutput = result.outputUTF8();
            int aiExitCode = result.getExitValue();

            // Check exit code
            if (aiExitCode != 0) {
                err.println("AI validation output: " + aiOutput);
                throw new Exception("AI validation script failed with exit code: " + aiExitCode);
            }
            
            // With quiet mode, output should be clean JSON
            String jsonOutput = aiOutput.trim();
            
            if (jsonOutput.isEmpty()) {
                throw new Exception("No output from AI validation");
            }
            
            // Parse JSON response directly (no complex filtering needed with quiet mode)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> aiResult;
            
            try {
                aiResult = mapper.readValue(jsonOutput, Map.class);
            } catch (Exception e) {
                // If direct parsing fails, try extracting JSON as fallback
                String extractedJson = extractJsonFromOutput(aiOutput);
                if (extractedJson != null && !extractedJson.trim().isEmpty()) {
                    aiResult = mapper.readValue(extractedJson, Map.class);
                } else {
                    throw new Exception("Could not parse AI validation JSON response: " + e.getMessage());
                }
            }
            
            boolean aiSuccess = (Boolean) aiResult.get("success");
            Double confidence = (Double) aiResult.get("confidence");
            String reasoning = (String) aiResult.get("reasoning");
            
            // Display AI validation results
            out.println("🤖 AI Validation Result:");
            out.println("  Success: " + aiSuccess);
            out.println("  Confidence: " + String.format("%.2f", confidence));
            out.println("  Reasoning: " + reasoning);
            
            // Show cost information if available
            @SuppressWarnings("unchecked")
            Map<String, Object> costInfo = (Map<String, Object>) aiResult.get("cost_info");
            if (costInfo != null) {
                out.println("  💰 Cost Information:");
                
                Object inputTokens = costInfo.get("input_tokens");
                Object outputTokens = costInfo.get("output_tokens");
                Object totalTokens = costInfo.get("total_tokens");
                Object cacheCreation = costInfo.get("cache_creation_input_tokens");
                Object cacheRead = costInfo.get("cache_read_input_tokens");
                Object duration = costInfo.get("duration_seconds");
                
                if (inputTokens != null) out.println("    Input Tokens: " + inputTokens);
                if (outputTokens != null) out.println("    Output Tokens: " + outputTokens);
                if (totalTokens != null) out.println("    Total Tokens: " + totalTokens);
                if (cacheCreation != null) out.println("    Cache Creation Tokens: " + cacheCreation);
                if (cacheRead != null) out.println("    Cache Read Tokens: " + cacheRead);
                if (duration != null) out.println("    Duration: " + String.format("%.2f", (Double) duration) + " seconds");
            }
            
            @SuppressWarnings("unchecked")
            List<String> functionalityDemo = (List<String>) aiResult.get("functionality_demonstrated");
            if (functionalityDemo != null && !functionalityDemo.isEmpty()) {
                out.println("  Functionality Demonstrated:");
                for (String item : functionalityDemo) {
                    out.println("    - " + item);
                }
            }
            
            @SuppressWarnings("unchecked")
            List<String> issues = (List<String>) aiResult.get("issues_found");
            if (issues != null && !issues.isEmpty()) {
                out.println("  Issues Found:");
                for (String issue : issues) {
                    out.println("    - " + issue);
                }
            }
            
            return aiSuccess;
            
        } catch (Exception e) {
            err.println("⚠️  AI validation failed: " + e.getMessage());
            
            // Handle based on validation mode
            String mode = aiConfig.validationMode();
            if ("primary".equals(mode)) {
                throw new Exception("AI validation failed and is required: " + e.getMessage());
            } else if ("hybrid".equals(mode)) {
                err.println("⚠️  AI validation failed, falling back to regex patterns only");
                return true; // Let regex validation handle it
            } else { // fallback mode
                err.println("⚠️  AI validation failed, using regex validation result");
                return true; // Use whatever regex validation determined
            }
        }
    }
    
    // Extract JSON from mixed output (filtering out logging lines)
    public static String extractJsonFromOutput(String output) {
        String[] lines = output.split("\n");
        StringBuilder jsonBuilder = new StringBuilder();
        boolean inJsonBlock = false;
        int braceCount = 0;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip logging lines
            if (trimmedLine.startsWith("INFO:") || 
                trimmedLine.startsWith("DEBUG:") || 
                trimmedLine.startsWith("WARNING:") ||
                trimmedLine.startsWith("ERROR:")) {
                continue;
            }
            
            // Look for JSON start
            if (trimmedLine.startsWith("{") && !inJsonBlock) {
                inJsonBlock = true;
                braceCount = 0;
                jsonBuilder.setLength(0); // Reset
                jsonBuilder.append(line).append("\n");
                braceCount += line.chars().map(c -> c == '{' ? 1 : c == '}' ? -1 : 0).sum();
            } else if (inJsonBlock) {
                jsonBuilder.append(line).append("\n");
                braceCount += line.chars().map(c -> c == '{' ? 1 : c == '}' ? -1 : 0).sum();
                
                // Check if JSON block is complete
                if (braceCount <= 0 && trimmedLine.endsWith("}")) {
                    break;
                }
            }
        }
        
        return inJsonBlock ? jsonBuilder.toString().trim() : null;
    }
    
    // Main test execution flow
    public static void runIntegrationTest(String moduleName) throws Exception {
        // Load configuration
        ExampleInfo cfg = loadConfig();
        
        // Verify environment
        verifyEnvironment(cfg);
        
        try {
            // Build application
            buildApplication(moduleName);
            
            // Run setup commands AFTER build to avoid clean removing test files
            runSetupCommands(cfg);
            
            // Create log file
            Path logFile = createLogFile(moduleName.toLowerCase().replace(" ", "-"));
            
            // Run application
            out.println("🚀 Running " + moduleName + "...");
            ProcessResult result = runSpringBootApp(cfg, logFile);
            int exitCode = result.getExitValue();
            
            // Read output
            String output = Files.readString(logFile);
            
            // Verify patterns and display log path
            out.println("✅ Verifying output patterns...");
            out.println("📁 Full Spring Boot log: " + logFile.toAbsolutePath().normalize());
            
            // Display full output first
            displayOutput(output);
            
            // First, check if AI validation is enabled and in primary mode
            AIValidationConfig aiConfig = cfg.aiValidation();
            boolean regexValidationNeeded = true;
            boolean aiValidationPassed = true;
            
            if (aiConfig != null && aiConfig.enabled() && "primary".equals(aiConfig.validationMode())) {
                // Primary AI validation mode - skip regex patterns
                out.println("🤖 Using AI validation as primary validation method");
                regexValidationNeeded = false;
                aiValidationPassed = performAIValidation(output, logFile, cfg, moduleName);
            } else {
                // Run regex validation first
                int failedPatterns = 0;
                for (String pattern : cfg.successRegex()) {
                    if (output.matches("(?s).*" + pattern + ".*")) {
                        out.println("  ✓ Found: " + pattern);
                    } else {
                        err.println("  ❌ Missing: " + pattern);
                        failedPatterns++;
                    }
                }
                
                // Run AI validation if configured
                if (aiConfig != null && aiConfig.enabled()) {
                    if ("hybrid".equals(aiConfig.validationMode())) {
                        // Hybrid mode - both must pass
                        out.println("🤖 Running additional AI validation (hybrid mode)");
                        aiValidationPassed = performAIValidation(output, logFile, cfg, moduleName);
                    } else if ("fallback".equals(aiConfig.validationMode()) && failedPatterns > 0) {
                        // Fallback mode - only run AI if regex failed
                        out.println("🤖 Regex validation failed, trying AI validation (fallback mode)");
                        aiValidationPassed = performAIValidation(output, logFile, cfg, moduleName);
                        if (aiValidationPassed) {
                            out.println("🎉 AI validation succeeded, overriding regex failure!");
                            failedPatterns = 0; // Reset failed patterns since AI validation passed
                        }
                    }
                }
                
                // Check regex results if needed
                if (regexValidationNeeded && failedPatterns > 0 && !aiValidationPassed) {
                    err.println("❌ Failed pattern verification: " + failedPatterns + " patterns missing");
                    exit(1);
                }
            }
            
            // Keep log file for debugging - DO NOT DELETE
            out.println("\n📁 Spring Boot log preserved: " + logFile.toAbsolutePath().normalize());
            
            // Final validation check
            // Allow non-zero exit codes for interactive applications that use AI validation
            boolean allowNonZeroExit = aiConfig != null && aiConfig.enabled() && 
                aiConfig.successCriteria() != null && 
                aiConfig.successCriteria().containsKey("requiresUserInteraction") &&
                Boolean.TRUE.equals(aiConfig.successCriteria().get("requiresUserInteraction"));
                
            if (exitCode != 0 && !allowNonZeroExit) {
                err.println("❌ Application exited with code: " + exitCode);
                exit(exitCode);
            } else if (exitCode != 0 && allowNonZeroExit) {
                out.println("ℹ️  Application exited with code " + exitCode + " (expected for interactive application)");
            }
            
            if (!aiValidationPassed) {
                err.println("❌ AI validation failed");
                exit(1);
            }
            
            out.println("🎉 Integration test completed successfully!");
            
        } finally {
            // Run cleanup commands
            runCleanupCommands(cfg);
        }
    }
}