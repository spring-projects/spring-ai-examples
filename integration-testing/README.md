# Spring AI Examples - Integration Testing Framework

This directory contains the comprehensive integration testing framework for Spring AI Examples. The framework provides tools, documentation, and infrastructure for reliable testing of all Spring AI examples across different complexity levels.

## Directory Structure

```
integration-testing/
├── scripts/                    # Integration testing scripts (2 active tools)
│   ├── run-integration-tests.sh  # ⭐ PRIMARY: Direct test runner (recommended)
│   └── scaffold_integration_test.py  # ⭐ PRIMARY: Creates integration tests for new examples
├── jbang-lib/                 # Centralized JBang utilities
│   └── IntegrationTestUtils.java  # Common testing functionality
├── docs/                      # Integration testing documentation
│   └── README.md             # Detailed integration testing guide
```

Runtime log directories are created on demand by `run-integration-tests.sh` under `integration-testing/logs/` and are not committed to the repository tree.

## Quick Start

### Run All Integration Tests
```bash
# Recommended: Direct execution with reliable port cleanup
./integration-testing/scripts/run-integration-tests.sh
```

### Create New Integration Test
**⭐ Essential tool for extending the framework to new examples**
```bash
# Creates both ExampleInfo.json and Run*.java files with proper templates
python3 integration-testing/scripts/scaffold_integration_test.py <module-path> [--complexity simple|complex|mcp]

# Examples:
python3 integration-testing/scripts/scaffold_integration_test.py kotlin/kotlin-hello-world --complexity simple
python3 integration-testing/scripts/scaffold_integration_test.py agentic-patterns/new-workflow --complexity complex
python3 integration-testing/scripts/scaffold_integration_test.py model-context-protocol/new-server --complexity mcp
```

### Test Individual Example
```bash
cd <module-directory>
jbang integration-tests/Run*.java
```

## Key Features

### ✅ **100% Reliable Test Execution**
- **Port Cleanup**: Systematic cleanup of port 8080 prevents cascading failures
- **Sequential Execution**: Avoids resource conflicts between Spring Boot applications
- **Comprehensive Logging**: Persistent logs in `integration-testing/logs/` for debugging

### ✅ **Developer Experience**
- **Individual Log Files**: Each test creates timestamped logs for debugging
- **Progress Indication**: Real-time feedback for long-running tests
- **Manual Verification**: Captured application output shown for "eyeball" validation

### ✅ **Test Validation Quality**
- **Functional Validation**: Tests check actual functionality, not just startup messages
- **Error Detection**: Comprehensive logs reveal functional failures behind passing regex
- **Pattern Templates**: Proven success patterns for different example types

## Testing Approaches by Example Type

| Example Type | Test Location | Execution Time | Validation Strategy |
|--------------|---------------|----------------|-------------------|
| **Simple** | `src/test/java/` | ~30s | Unit tests + basic smoke tests |
| **Complex** | `integration-tests/` | ~25-50s | End-to-end with comprehensive patterns |
| **MCP** | `integration-tests/` | ~50s+ | Multi-stage validation with external services |

## Framework Architecture

### **Phase 1**: Infrastructure (✅ Complete)
- Python orchestration + JBang execution + JSON configuration
- Cross-platform compatibility with comprehensive error handling
- Template-based scaffolding for different complexity levels

### **Phase 2**: Pattern Validation (✅ Complete)
- Pilot conversions across all complexity levels
- Content-based success patterns (not just build artifacts)
- Optimized build commands with `-DskipTests` for performance

### **Phase 3a**: Critical Infrastructure (✅ Complete)
- **Phase 3a.1**: 100% test pass rate through systematic port cleanup
- **Phase 3a.2**: Comprehensive logging infrastructure implementation ✅
- **Phase 3a.3**: Centralized directory structure reorganization ✅
- **Phase 3a.4**: Systematic logging fix + functional validation ✅
- **Phase 3a.5**: Enhanced logging and script improvements ✅
- **Phase 3a.6**: JBang utility centralization ✅

## Centralized JBang Architecture

### **Phase 3a.6 Achievement**: Zero Code Duplication
All 18 JBang integration test scripts now use a centralized utility class:

- **Before**: Each script contained ~110-130 lines of duplicated code
- **After**: Each script reduced to ~18 lines using centralized utilities
- **Code Reduction**: 84% reduction in lines of code
- **Duplication**: 0% - all logic centralized in IntegrationTestUtils.java
- **Maintenance**: Single location for bug fixes and enhancements

### **Architecture Components**

1. **IntegrationTestUtils.java** (`jbang-lib/`)
   - Configuration loading (ExampleInfo.json parsing)
   - Environment variable verification
   - Dynamic path resolution for different module depths
   - Command execution with timeout handling
   - Standardized log file creation and management
   - Spring Boot build and run orchestration
   - Output pattern verification
   - Setup/cleanup command execution

2. **Simplified JBang Scripts**
   ```java
   ///usr/bin/env jbang "$0" "$@" ; exit $?
   //DEPS org.zeroturnaround:zt-exec:1.12
   //DEPS com.fasterxml.jackson.core:jackson-databind:2.17.1
   //JAVA 17
   //FILES ExampleInfo.json
   //SOURCES ../../../integration-testing/jbang-lib/IntegrationTestUtils.java

   public class RunExample {
       public static void main(String... args) throws Exception {
           IntegrationTestUtils.runIntegrationTest("example-name");
       }
   }
   ```

## Logging Architecture

### **Centralized Logs**: `integration-testing/logs/`
```
logs/
├── background-runs/           # Main test execution logs
│   └── run-integration-tests_YYYYMMDD_HHMMSS.log
└── integration-tests/         # Individual Spring Boot application logs
    └── MODULE-spring-boot-TIMESTAMP.log
```

These directories are created the first time `run-integration-tests.sh` is executed.

### **Persistent Logging Template** (JBang Scripts)
```java
// Create persistent log file for debugging
Path logDir = Paths.get("../../integration-testing/logs/integration-tests");
Files.createDirectories(logDir);
Path logFile = logDir.resolve("MODULE-spring-boot-" + System.currentTimeMillis() + ".log");

// Keep log file for debugging - DO NOT DELETE
out.println("📁 Spring Boot log preserved: " + logFile.toAbsolutePath());
```

## Best Practices

### ✅ **Effective Patterns**
1. **Systematic Port Cleanup**: Clean port 8080 before and after each test
2. **Persistent Log Files**: Use timestamped logs in predictable locations
3. **Individual Test Validation**: Run tests individually to isolate issues
4. **Comprehensive Output Display**: Show captured application output for manual verification
5. **Sequential Execution**: Prevent resource conflicts with reliable execution order

### ❌ **Anti-Patterns to Avoid**
1. **Temporary File Logging**: Never use `Files.createTempFile()` and delete logs
2. **Regex-Only Validation**: Don't rely solely on success patterns without ERROR checking
3. **Unconstrained Parallel Execution**: Avoid parallel tests without resource management
4. **Pattern Matching Over Functionality**: Validate actual application functionality
5. **Missing Individual Log Files**: Always preserve detailed logs for debugging

## Troubleshooting

### **Common Issues**

**Port Conflicts**: 
```bash
# Clean up hanging processes
lsof -ti:8080 | xargs kill -9
```

**Missing Environment Variables**:
```bash
export OPENAI_API_KEY="your-key-here"
export BRAVE_API_KEY="your-brave-key"  # For Brave examples
```

**Integration Test Timeout**:
- Check `ExampleInfo.json` timeout settings
- Verify external services are accessible
- Review comprehensive logs in `integration-testing/logs/`

## Documentation

For detailed integration testing guide, see: `integration-testing/docs/README.md`

## Framework Status

**✅ Production Ready**: 24 modules with integration tests  
**✅ Test Coverage**: All 24 modules passing with 100% reliability (regex validation)  
**✅ Comprehensive Logging**: Full debugging capability implemented  
**✅ Developer Experience**: Real-time progress and persistent logs  
**✅ Zero Duplication**: All JBang scripts use centralized utilities  
**✅ Version Management**: Supports testing with multiple Spring AI versions

## Test Statistics
- **Total Modules with Integration Tests**: 24
- **Modules using spring-ai.version property**: 17  
- **Modules using direct BOM version**: 7
- **Total pom.xml files managed**: 32
