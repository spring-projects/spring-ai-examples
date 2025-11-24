# JUnit5 API Design Specification

## Executive Summary

This document provides the complete API specification for the Spring AI JUnit5 integration testing framework. It defines all public interfaces, annotations, classes, and extension points that developers will use to create and maintain AI-validated integration tests.

## Package Structure

```
org.springframework.ai.test
├── annotations/
│   ├── SpringAIIntegrationTest.java
│   ├── AIValidation.java
│   ├── ExpectedBehavior.java
│   ├── SuccessPatterns.java
│   ├── SetupAndTeardown.java
│   ├── TestCost.java
│   └── ConditionalOnAI.java
├── core/
│   ├── SpringAITestExtension.java
│   ├── ValidationContext.java
│   ├── ValidationRequest.java
│   ├── ValidationResult.java
│   └── TestExecutionContext.java
├── validation/
│   ├── AIValidationEngine.java
│   ├── ValidationMode.java
│   ├── ValidatorExecutor.java
│   ├── TemplateManager.java
│   └── CostTracker.java
├── output/
│   ├── OutputCapture.java
│   ├── CapturedOutput.java
│   └── LogManager.java
├── config/
│   ├── TestConfiguration.java
│   ├── ValidationConfig.java
│   ├── CostConfig.java
│   └── OutputConfig.java
├── reporting/
│   ├── TestReporter.java
│   ├── ValidationReport.java
│   ├── CostReport.java
│   └── HtmlReportGenerator.java
├── utils/
│   ├── JsonToAnnotationConverter.java
│   ├── TestDataBuilder.java
│   └── ValidationAssertions.java
└── exceptions/
    ├── ValidationException.java
    ├── ConfigurationException.java
    └── TimeoutException.java
```

## Core Annotations API

### @SpringAIIntegrationTest

Primary test class annotation that enables Spring AI integration testing.

```java
package org.springframework.ai.test.annotations;

import java.lang.annotation.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Marks a class as a Spring AI integration test.
 * Combines Spring Boot test context with AI validation capabilities.
 * 
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(SpringAITestExtension.class)
@SpringBootTest
public @interface SpringAIIntegrationTest {
    
    /**
     * Maximum execution time in seconds.
     * @return timeout value, default 120 seconds
     */
    int timeout() default 120;
    
    /**
     * Required environment variables for test execution.
     * @return array of environment variable names
     */
    String[] requiredEnv() default {};
    
    /**
     * Custom configuration class for test setup.
     * @return configuration class
     */
    Class<? extends TestConfiguration> config() default DefaultTestConfiguration.class;
    
    /**
     * Whether to capture application output.
     * @return true to capture output, default true
     */
    boolean captureOutput() default true;
    
    /**
     * Test execution mode.
     * @return execution mode
     */
    ExecutionMode mode() default ExecutionMode.NORMAL;
    
    enum ExecutionMode {
        /** Normal test execution */
        NORMAL,
        /** Debug mode with verbose logging */
        DEBUG,
        /** Performance testing mode */
        PERFORMANCE,
        /** Dry run without actual execution */
        DRY_RUN
    }
}
```

### @AIValidation

Configures AI-powered validation for test methods or classes.

```java
package org.springframework.ai.test.annotations;

import java.lang.annotation.*;
import org.springframework.ai.test.validation.ValidationMode;

/**
 * Configures AI validation for integration tests.
 * Can be applied at class or method level.
 * 
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(AIValidations.class)
public @interface AIValidation {
    
    /**
     * Enable or disable AI validation.
     * @return true to enable, default true
     */
    boolean enabled() default true;
    
    /**
     * Validation mode determining how AI validation interacts with other validations.
     * @return validation mode
     */
    ValidationMode mode() default ValidationMode.HYBRID;
    
    /**
     * Template name for validation prompt.
     * @return template name
     */
    String template() default "example_validation";
    
    /**
     * Path to README file for additional context.
     * @return readme file path
     */
    String readmeFile() default "";
    
    /**
     * System components to validate.
     * @return array of component names
     */
    String[] components() default {};
    
    /**
     * Minimum confidence threshold for validation to pass.
     * @return confidence threshold between 0.0 and 1.0
     */
    double minConfidence() default 0.8;
    
    /**
     * Maximum number of retry attempts for validation.
     * @return retry count
     */
    int maxRetries() default 3;
    
    /**
     * Custom validation provider class.
     * @return provider class
     */
    Class<? extends ValidationProvider> provider() default DefaultValidationProvider.class;
}
```

### @ExpectedBehavior

Describes expected application behavior for AI validation with automatic README inference.

```java
package org.springframework.ai.test.annotations;

import java.lang.annotation.*;

/**
 * Describes expected behavior of the application under test.
 * Supports automatic inference from README documentation.
 * Used by AI validator to assess correctness.
 * 
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExpectedBehavior {
    
    /**
     * Primary description of expected behavior.
     * Optional when inferFromReadme is true.
     * @return behavior description
     */
    String value() default "";
    
    /**
     * Whether to automatically infer behavior from README.md.
     * @return true to enable inference (default)
     */
    boolean inferFromReadme() default true;
    
    /**
     * Path to README file relative to module root.
     * @return readme path
     */
    String readmePath() default "README.md";
    
    /**
     * Specific README sections to extract for context.
     * @return section names
     */
    String[] readmeSections() default {};
    
    /**
     * Expected execution steps in order.
     * @return array of step descriptions
     */
    String[] steps() default {};
    
    /**
     * Types of outputs expected from the application.
     * @return array of output types
     */
    OutputType[] expectedOutputs() default {};
    
    /**
     * Whether user interaction is required.
     * @return true if user interaction needed
     */
    boolean requiresUserInteraction() default false;
    
    /**
     * Expected execution time range in seconds.
     * @return min and max execution time
     */
    int[] expectedDuration() default {0, Integer.MAX_VALUE};
    
    /**
     * Success criteria for validation.
     * @return success criteria
     */
    SuccessCriteria criteria() default @SuccessCriteria;
    
    enum OutputType {
        /** Conversational output */
        CONVERSATION,
        /** Tool/function usage */
        TOOL_USAGE,
        /** API response */
        API_RESPONSE,
        /** Workflow completion */
        WORKFLOW_COMPLETION,
        /** Protocol communication */
        PROTOCOL_COMMUNICATION,
        /** Structured data */
        STRUCTURED_DATA,
        /** Stream output */
        STREAM_OUTPUT,
        /** File generation */
        FILE_GENERATION
    }
    
    @interface SuccessCriteria {
        int minSteps() default 0;
        int maxSteps() default Integer.MAX_VALUE;
        boolean requiresEvaluation() default false;
        String outputFormat() default "";
        int minResponseLength() default 0;
    }
}
```

### @SuccessPatterns

Defines regex patterns for traditional validation.

```java
package org.springframework.ai.test.annotations;

import java.lang.annotation.*;

/**
 * Defines success patterns for regex-based validation.
 * 
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SuccessPatterns {
    
    /**
     * Regex patterns that must be found in output.
     * @return array of regex patterns
     */
    String[] value() default {};
    
    /**
     * Patterns that must NOT be found in output.
     * @return array of exclusion patterns
     */
    String[] exclude() default {};
    
    /**
     * Whether to ignore case in pattern matching.
     * @return true to ignore case
     */
    boolean ignoreCase() default false;
    
    /**
     * How patterns should be matched.
     * @return match mode
     */
    MatchMode matchMode() default MatchMode.ALL;
    
    /**
     * Timeout for pattern matching in seconds.
     * @return timeout value
     */
    int timeout() default 30;
    
    /**
     * Whether to use multiline matching.
     * @return true for multiline mode
     */
    boolean multiline() default false;
    
    enum MatchMode {
        /** All patterns must match */
        ALL,
        /** At least one pattern must match */
        ANY,
        /** Exact number of patterns must match */
        EXACT,
        /** Patterns must match in order */
        ORDERED,
        /** Custom matching logic */
        CUSTOM
    }
}
```

## Validation API

### ValidationContext

Encapsulates all context needed for validation.

```java
package org.springframework.ai.test.core;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.List;

/**
 * Context object containing all information needed for validation.
 * 
 * @since 1.0.0
 */
public class ValidationContext {
    private final String testName;
    private final String moduleName;
    private final CapturedOutput capturedOutput;
    private final Map<String, Object> metadata;
    private final List<Annotation> annotations;
    private final Duration executionTime;
    private final Path logFile;
    private final TestExecutionStatus status;
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getTestName() { return testName; }
    public String getModuleName() { return moduleName; }
    public CapturedOutput getCapturedOutput() { return capturedOutput; }
    public Map<String, Object> getMetadata() { return metadata; }
    public List<Annotation> getAnnotations() { return annotations; }
    public Duration getExecutionTime() { return executionTime; }
    public Path getLogFile() { return logFile; }
    public TestExecutionStatus getStatus() { return status; }
    
    // Utility methods
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return annotations.stream()
            .filter(annotationType::isInstance)
            .map(annotationType::cast)
            .findFirst()
            .orElse(null);
    }
    
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }
    
    public static class Builder {
        // Builder implementation
    }
}
```

### ValidationResult

Result of AI validation.

```java
package org.springframework.ai.test.core;

import java.util.List;
import java.util.Map;

/**
 * Result of AI validation process.
 * 
 * @since 1.0.0
 */
public class ValidationResult {
    private final boolean success;
    private final double confidence;
    private final String reasoning;
    private final List<String> componentsValidated;
    private final List<String> functionalityDemonstrated;
    private final List<String> issuesFound;
    private final List<String> recommendations;
    private final CostInfo costInfo;
    private final Map<String, Object> additionalData;
    
    // Getters
    public boolean isSuccess() { return success; }
    public double getConfidence() { return confidence; }
    public String getReasoning() { return reasoning; }
    public List<String> getComponentsValidated() { return componentsValidated; }
    public List<String> getFunctionalityDemonstrated() { return functionalityDemonstrated; }
    public List<String> getIssuesFound() { return issuesFound; }
    public List<String> getRecommendations() { return recommendations; }
    public CostInfo getCostInfo() { return costInfo; }
    public Map<String, Object> getAdditionalData() { return additionalData; }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    // Utility methods
    public boolean hasIssues() {
        return issuesFound != null && !issuesFound.isEmpty();
    }
    
    public boolean meetsConfidenceThreshold(double threshold) {
        return confidence >= threshold;
    }
    
    public static class Builder {
        // Builder implementation
    }
}
```

### AIValidationEngine

Core engine for AI validation.

```java
package org.springframework.ai.test.validation;

import java.util.concurrent.CompletableFuture;

/**
 * Core engine for executing AI validations.
 * 
 * @since 1.0.0
 */
public interface AIValidationEngine {
    
    /**
     * Validates test execution using AI.
     * @param context validation context
     * @return validation result
     */
    ValidationResult validate(ValidationContext context);
    
    /**
     * Asynchronously validates test execution.
     * @param context validation context
     * @return future validation result
     */
    CompletableFuture<ValidationResult> validateAsync(ValidationContext context);
    
    /**
     * Validates multiple contexts in batch.
     * @param contexts list of validation contexts
     * @return list of validation results
     */
    List<ValidationResult> validateBatch(List<ValidationContext> contexts);
    
    /**
     * Checks if engine supports given template.
     * @param template template name
     * @return true if supported
     */
    boolean supportsTemplate(String template);
    
    /**
     * Gets cost estimate for validation.
     * @param context validation context
     * @return estimated cost
     */
    CostEstimate estimateCost(ValidationContext context);
    
    /**
     * Clears validation cache.
     */
    void clearCache();
    
    /**
     * Gets engine statistics.
     * @return statistics map
     */
    Map<String, Object> getStatistics();
}
```

## Configuration API

### TestConfiguration

Base configuration interface.

```java
package org.springframework.ai.test.config;

/**
 * Base interface for test configuration.
 * 
 * @since 1.0.0
 */
public interface TestConfiguration {
    
    /**
     * Configures the test environment.
     * @param builder configuration builder
     */
    void configure(TestConfigBuilder builder);
    
    /**
     * Gets validation configuration.
     * @return validation config
     */
    default ValidationConfig validation() {
        return ValidationConfig.defaults();
    }
    
    /**
     * Gets output capture configuration.
     * @return output config
     */
    default OutputConfig output() {
        return OutputConfig.defaults();
    }
    
    /**
     * Gets cost management configuration.
     * @return cost config
     */
    default CostConfig cost() {
        return CostConfig.defaults();
    }
    
    /**
     * Gets reporting configuration.
     * @return reporting config
     */
    default ReportingConfig reporting() {
        return ReportingConfig.defaults();
    }
}
```

### ValidationConfig

Configuration for validation behavior.

```java
package org.springframework.ai.test.config;

import java.time.Duration;

/**
 * Configuration for AI validation.
 * 
 * @since 1.0.0
 */
public class ValidationConfig {
    private boolean enabled = true;
    private ValidationMode defaultMode = ValidationMode.HYBRID;
    private String defaultTemplate = "example_validation";
    private Duration timeout = Duration.ofSeconds(180);
    private int maxRetries = 3;
    private double minConfidence = 0.8;
    private boolean cacheEnabled = true;
    private int cacheSize = 1000;
    private Duration cacheTtl = Duration.ofHours(1);
    
    // Getters and setters
    // Builder pattern
    
    public static ValidationConfig defaults() {
        return new ValidationConfig();
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
```

## Output Capture API

### OutputCapture

Captures application output during test execution.

```java
package org.springframework.ai.test.output;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Captures and manages application output.
 * 
 * @since 1.0.0
 */
public interface OutputCapture {
    
    /**
     * Starts capturing output.
     */
    void startCapture();
    
    /**
     * Stops capturing and returns captured output.
     * @return captured output
     */
    CapturedOutput stopCapture();
    
    /**
     * Gets current captured output without stopping.
     * @return current output
     */
    String getCurrentOutput();
    
    /**
     * Clears captured output.
     */
    void clear();
    
    /**
     * Saves output to file.
     * @param path file path
     */
    void saveToFile(Path path);
    
    /**
     * Gets original stdout stream.
     * @return original stdout
     */
    PrintStream getOriginalOut();
    
    /**
     * Gets original stderr stream.
     * @return original stderr
     */
    PrintStream getOriginalErr();
}
```

### CapturedOutput

Represents captured application output.

```java
package org.springframework.ai.test.output;

import java.time.Instant;
import java.util.List;

/**
 * Represents captured application output.
 * 
 * @since 1.0.0
 */
public class CapturedOutput {
    private final String stdout;
    private final String stderr;
    private final String combined;
    private final Instant captureStart;
    private final Instant captureEnd;
    private final List<LogEntry> logEntries;
    
    // Getters
    public String getStdout() { return stdout; }
    public String getStderr() { return stderr; }
    public String getCombined() { return combined; }
    public Instant getCaptureStart() { return captureStart; }
    public Instant getCaptureEnd() { return captureEnd; }
    public List<LogEntry> getLogEntries() { return logEntries; }
    
    // Utility methods
    public boolean contains(String text) {
        return combined.contains(text);
    }
    
    public boolean containsPattern(String regex) {
        return combined.matches("(?s).*" + regex + ".*");
    }
    
    public List<String> getLines() {
        return combined.lines().toList();
    }
    
    public String getLastLines(int count) {
        List<String> lines = getLines();
        int start = Math.max(0, lines.size() - count);
        return String.join("\n", lines.subList(start, lines.size()));
    }
}
```

## Reporting API

### TestReporter

Interface for test reporting.

```java
package org.springframework.ai.test.reporting;

import java.nio.file.Path;

/**
 * Interface for generating test reports.
 * 
 * @since 1.0.0
 */
public interface TestReporter {
    
    /**
     * Generates report for single test result.
     * @param result test result
     * @param outputPath output file path
     */
    void generateReport(TestResult result, Path outputPath);
    
    /**
     * Generates report for multiple test results.
     * @param results list of test results
     * @param outputPath output file path
     */
    void generateBatchReport(List<TestResult> results, Path outputPath);
    
    /**
     * Gets report format.
     * @return format name
     */
    String getFormat();
    
    /**
     * Gets reporter priority.
     * @return priority value
     */
    int getPriority();
    
    /**
     * Checks if reporter supports given format.
     * @param format format name
     * @return true if supported
     */
    boolean supportsFormat(String format);
}
```

## Utility API

### ValidationAssertions

Custom assertions for validation results.

```java
package org.springframework.ai.test.utils;

import org.junit.jupiter.api.Assertions;

/**
 * Custom assertions for AI validation results.
 * 
 * @since 1.0.0
 */
public class ValidationAssertions {
    
    /**
     * Asserts that validation was successful.
     * @param result validation result
     */
    public static void assertValidationSuccess(ValidationResult result) {
        Assertions.assertTrue(result.isSuccess(), 
            "Validation failed: " + result.getReasoning());
    }
    
    /**
     * Asserts validation confidence meets threshold.
     * @param result validation result
     * @param minConfidence minimum confidence
     */
    public static void assertConfidence(ValidationResult result, double minConfidence) {
        Assertions.assertTrue(result.getConfidence() >= minConfidence,
            String.format("Confidence %.2f below threshold %.2f", 
                result.getConfidence(), minConfidence));
    }
    
    /**
     * Asserts no issues were found.
     * @param result validation result
     */
    public static void assertNoIssues(ValidationResult result) {
        Assertions.assertTrue(result.getIssuesFound().isEmpty(),
            "Issues found: " + String.join(", ", result.getIssuesFound()));
    }
    
    /**
     * Asserts specific functionality was demonstrated.
     * @param result validation result
     * @param functionality expected functionality
     */
    public static void assertFunctionalityDemonstrated(
            ValidationResult result, String functionality) {
        Assertions.assertTrue(
            result.getFunctionalityDemonstrated().contains(functionality),
            "Functionality not demonstrated: " + functionality);
    }
}
```

### TestDataBuilder

Builder for test data and mocks.

```java
package org.springframework.ai.test.utils;

/**
 * Builder for creating test data and mocks.
 * 
 * @since 1.0.0
 */
public class TestDataBuilder {
    
    /**
     * Creates mock validation context.
     * @return mock context
     */
    public static ValidationContext mockContext() {
        return ValidationContext.builder()
            .testName("mock-test")
            .moduleName("mock-module")
            .capturedOutput(mockOutput())
            .build();
    }
    
    /**
     * Creates mock captured output.
     * @return mock output
     */
    public static CapturedOutput mockOutput() {
        return new CapturedOutput(
            "Mock stdout",
            "Mock stderr",
            "Mock combined output",
            Instant.now().minusSeconds(10),
            Instant.now(),
            List.of()
        );
    }
    
    /**
     * Creates mock validation result.
     * @param success whether validation succeeded
     * @return mock result
     */
    public static ValidationResult mockResult(boolean success) {
        return ValidationResult.builder()
            .success(success)
            .confidence(success ? 0.95 : 0.45)
            .reasoning(success ? "All tests passed" : "Validation failed")
            .build();
    }
}
```

## Exception API

### ValidationException

Base exception for validation errors.

```java
package org.springframework.ai.test.exceptions;

/**
 * Base exception for validation errors.
 * 
 * @since 1.0.0
 */
public class ValidationException extends RuntimeException {
    private final ValidationContext context;
    private final ValidationResult result;
    
    public ValidationException(String message) {
        super(message);
        this.context = null;
        this.result = null;
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
        this.context = null;
        this.result = null;
    }
    
    public ValidationException(String message, ValidationContext context, 
                              ValidationResult result) {
        super(message);
        this.context = context;
        this.result = result;
    }
    
    public ValidationContext getContext() { return context; }
    public ValidationResult getResult() { return result; }
}
```

## Extension Points

### Custom Validation Provider

Interface for custom validation providers.

```java
package org.springframework.ai.test.validation;

/**
 * Interface for custom validation providers.
 * 
 * @since 1.0.0
 */
public interface ValidationProvider {
    
    /**
     * Gets provider name.
     * @return provider name
     */
    String getName();
    
    /**
     * Validates using custom logic.
     * @param request validation request
     * @return validation result
     */
    ValidationResult validate(ValidationRequest request);
    
    /**
     * Checks if provider supports template.
     * @param template template name
     * @return true if supported
     */
    boolean supportsTemplate(String template);
    
    /**
     * Gets provider priority.
     * @return priority value
     */
    int getPriority();
}
```

### Test Lifecycle Hooks

Interface for test lifecycle customization.

```java
package org.springframework.ai.test.core;

/**
 * Hooks for customizing test lifecycle.
 * 
 * @since 1.0.0
 */
public interface TestLifecycleHooks {
    
    /**
     * Called before test class execution.
     * @param context test context
     */
    default void beforeAll(TestExecutionContext context) {}
    
    /**
     * Called before each test method.
     * @param context test context
     */
    default void beforeEach(TestExecutionContext context) {}
    
    /**
     * Called after each test method.
     * @param context test context
     */
    default void afterEach(TestExecutionContext context) {}
    
    /**
     * Called after test class execution.
     * @param context test context
     */
    default void afterAll(TestExecutionContext context) {}
    
    /**
     * Called when test fails.
     * @param context test context
     * @param throwable failure cause
     */
    default void onTestFailure(TestExecutionContext context, Throwable throwable) {}
    
    /**
     * Called when test succeeds.
     * @param context test context
     */
    default void onTestSuccess(TestExecutionContext context) {}
}
```

## Usage Examples

### Simple Test Example

```java
@SpringAIIntegrationTest(timeout = 120)
@AIValidation(mode = ValidationMode.PRIMARY)
@ExpectedBehavior()  // Automatically infers from README.md
@SuccessPatterns({"USER:", "ASSISTANT:"})
public class SimpleAITest {
    
    @Test
    public void testJokeGeneration() {
        // Test executes automatically
        // README behavior is inferred automatically
        // Output is captured and validated
    }
}
```

### Complex Test Example

```java
@SpringAIIntegrationTest(
    timeout = 300,
    requiredEnv = {"OPENAI_API_KEY", "BRAVE_API_KEY"}
)
public class ComplexWorkflowTest {
    
    @Test
    @AIValidation(
        mode = ValidationMode.HYBRID,
        template = "workflow_validation",
        minConfidence = 0.9
    )
    @ExpectedBehavior(
        value = "Execute multi-step data processing pipeline",
        steps = {
            "Load data from source",
            "Transform data format",
            "Validate data integrity",
            "Generate output report"
        },
        expectedOutputs = {OutputType.WORKFLOW_COMPLETION, OutputType.STRUCTURED_DATA}
    )
    @SuccessPatterns(
        value = {"Pipeline started", "Step \\d+ completed", "Pipeline finished"},
        matchMode = MatchMode.ORDERED
    )
    @SetupAndTeardown(
        setup = {"./scripts/prepare-data.sh"},
        cleanup = {"./scripts/cleanup-data.sh"}
    )
    public void testDataPipeline() {
        // Complex test with multiple validations
    }
}
```

### Custom Validation Example

```java
public class CustomValidationProvider implements ValidationProvider {
    
    @Override
    public String getName() {
        return "custom-domain-validator";
    }
    
    @Override
    public ValidationResult validate(ValidationRequest request) {
        // Custom validation logic
        return ValidationResult.builder()
            .success(true)
            .confidence(0.95)
            .reasoning("Custom validation passed")
            .build();
    }
    
    @Override
    public boolean supportsTemplate(String template) {
        return template.startsWith("custom_");
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}

@SpringAIIntegrationTest
public class CustomValidationTest {
    
    @Test
    @AIValidation(provider = CustomValidationProvider.class)
    public void testWithCustomValidator() {
        // Uses custom validation provider
    }
}
```

## Version Compatibility

| Framework Version | JUnit Version | Spring Boot Version | Java Version |
|-------------------|---------------|---------------------|--------------|
| 1.0.x | 5.10.x | 3.4.x | 17+ |
| 1.1.x | 5.11.x | 3.5.x | 17+ |
| 2.0.x | 5.11.x | 3.5.x | 21+ |

## Migration Guide

### From JBang to JUnit5

```java
// Old JBang approach
public class RunExample {
    public static void main(String[] args) {
        IntegrationTestUtils.runIntegrationTest("example");
    }
}

// New JUnit5 approach
@SpringAIIntegrationTest
@AIValidation
@ExpectedBehavior()  // Infers from README.md
public class ExampleIntegrationTest {
    @Test
    public void testExample() {
        // Automatic execution and validation
        // Behavior inferred from documentation
    }
}
```

### From JSON to Annotations

```json
// Old ExampleInfo.json
{
  "timeoutSec": 300,
  "successRegex": ["BUILD SUCCESS"],
  "requiredEnv": ["OPENAI_API_KEY"],
  "aiValidation": {
    "enabled": true,
    "validationMode": "hybrid"
  }
}
```

```java
// New annotation-based configuration
@SpringAIIntegrationTest(
    timeout = 300,
    requiredEnv = {"OPENAI_API_KEY"}
)
@AIValidation(mode = ValidationMode.HYBRID)
@SuccessPatterns("BUILD SUCCESS")
public class MigratedTest {
    // Test implementation
}
```

## Best Practices

### 1. Annotation Organization
- Apply class-level annotations for common configuration
- Use method-level annotations for specific test requirements
- Keep annotations readable and well-organized

### 2. Validation Strategy
- Use PRIMARY mode for pure AI outputs
- Use HYBRID mode for mixed deterministic/AI content
- Use FALLBACK mode during migration

### 3. Performance Optimization
- Enable caching for repeated validations
- Use batch validation for test suites
- Configure appropriate timeouts

### 4. Cost Management
- Monitor token usage through cost reports
- Use templates to minimize token consumption
- Enable caching to reduce API calls

## Conclusion

This API design provides a comprehensive, type-safe, and extensible framework for AI-driven integration testing. The annotation-based approach offers excellent IDE support while maintaining flexibility through extension points and custom providers. The design balances ease of use for simple cases with powerful capabilities for complex testing scenarios.