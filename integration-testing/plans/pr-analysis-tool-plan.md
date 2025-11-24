# Local Tool for PR Integration Test Setup

## Overview
This document outlines the plan for creating a local command-line tool that maintainers can use when reviewing PRs to automatically generate integration tests for new Spring AI example contributions.

## Problem Statement
When contributors submit new examples to spring-ai-examples, they typically provide:
- Core application code (src/main/java or src/main/kotlin)
- README.md with usage instructions
- pom.xml with dependencies
- application.properties for configuration
- Basic unit tests (sometimes)

What's missing for integration testing:
- Integration test configuration (ExampleInfo.json)
- JBang launcher script (Run*.java)
- Environment variable requirements documentation
- Success validation patterns
- AI validation configuration

## Proposed Solution: Local Maintainer Tool

### Core Concept
Create a local command-line tool that maintainers run when reviewing PRs to automatically generate integration tests for new example contributions. This tool will analyze the contributed code and generate all necessary integration test files.

## Tool Design: `analyze-pr-example.py`

### Usage Workflow
```bash
# When reviewing a PR with a new example
cd spring-ai-examples
git checkout pr-branch

# Run the analysis tool
python3 integration-testing/scripts/analyze-pr-example.py path/to/new-example

# Tool automatically:
# 1. Analyzes the example structure
# 2. Generates integration test files
# 3. Runs the test locally
# 4. Shows results to maintainer
```

### Key Features

#### 1. Auto-Detection from Existing Files
- **README Analysis**: Extract expected behavior, usage examples, prerequisites
- **pom.xml Parsing**: Detect dependencies, determine complexity level
- **application.properties**: Identify required environment variables
- **Source Code**: Find main class, detect Spring AI components used

#### 2. Smart Test Generation
- Automatically determine complexity level (simple/complex/mcp)
- Generate appropriate ExampleInfo.json configuration
- Create JBang launcher using centralized utilities
- Suggest AI validation mode based on example characteristics
- Extract success patterns from README examples

#### 3. Interactive Refinement
- Show proposed configuration to maintainer
- Allow quick edits before finalizing
- Option to run test immediately for validation
- Generate commit-ready files

### Implementation Details

#### Phase 1: Enhanced `scaffold_integration_test.py`
Add new capabilities to the existing scaffolding tool:
```python
--auto-detect     # Analyze existing files to determine configuration
--from-readme     # Extract expected behavior from README
--analyze-deps    # Determine complexity from dependencies
--suggest-patterns # Generate success patterns from README examples
```

#### Phase 2: New Analysis Module
Create `integration-testing/scripts/example_analyzer.py`:

##### README Parser
- Extract "Expected Output" or "Example Output" sections
- Identify key functionality descriptions
- Pull out usage examples for pattern generation
- Find prerequisites and environment requirements

##### POM Analyzer
- Score complexity based on dependencies:
  - Simple: Basic Spring Boot + one Spring AI module
  - Complex: Multiple AI modules, workflows, or agents
  - MCP: Model Context Protocol dependencies
- Identify Spring AI components used
- Detect database or external service requirements

##### Pattern Suggester
- Generate regex patterns from README examples
- Common patterns based on Spring AI component type:
  - Chat models: Response generation patterns
  - Embeddings: Vector operation patterns
  - Functions: Tool calling patterns
  - Agents: Workflow execution patterns

##### Environment Detector
- Parse application.properties/yml for placeholders
- Identify common API key patterns
- Detect database connection requirements
- Find service endpoint configurations

### Tool Output Example
```
🔍 Analyzing: kotlin/new-rag-example
📄 Found README with usage examples
📦 Detected dependencies: spring-ai-openai, spring-ai-vectordb
🔧 Found required env: OPENAI_API_KEY, POSTGRES_CONNECTION
📊 Complexity: complex (vector DB + multi-step workflow)

✅ Generated integration test configuration:
- Timeout: 180s (complex workflow)
- AI Validation: hybrid mode (recommended for RAG)
- Success patterns: 3 extracted from README
- Environment vars: 2 required

📁 Files created:
- integration-tests/ExampleInfo.json
- integration-tests/RunNewRagExample.java

🧪 Run test now? [Y/n]
```

### Configuration Templates

#### Simple Example Template
```json
{
  "timeoutSec": 120,
  "successRegex": ["BUILD SUCCESS", "Started.*Application"],
  "requiredEnv": ["OPENAI_API_KEY"],
  "aiValidation": {
    "enabled": true,
    "validationMode": "hybrid",
    "expectedBehavior": "[Extracted from README]",
    "promptTemplate": "example_validation"
  }
}
```

#### Complex Example Template
```json
{
  "timeoutSec": 300,
  "successRegex": ["BUILD SUCCESS", "Started.*Application", "[Pattern from README]"],
  "requiredEnv": ["OPENAI_API_KEY", "[Additional from properties]"],
  "aiValidation": {
    "enabled": true,
    "validationMode": "primary",
    "readmeFile": "../README.md",
    "expectedBehavior": "[Extracted from README]",
    "promptTemplate": "workflow_validation",
    "successCriteria": {
      "expectedSteps": 3,
      "evaluationRequired": true
    }
  }
}
```

## Benefits
1. **Immediate**: Works during PR review process, no waiting for CI
2. **Local Control**: Maintainer has full visibility and control
3. **Iterative**: Can refine and test immediately
4. **Simple**: One command to analyze and generate
5. **Educational**: Shows maintainer what makes a good test
6. **Consistent**: Ensures all examples have integration tests

## Implementation Phases

### Phase 1: Core Functionality (Week 1)
- [ ] Create example_analyzer.py module
- [ ] Implement README parsing for behavior extraction
- [ ] Add POM dependency analysis
- [ ] Basic pattern suggestion from README

### Phase 2: Enhanced Detection (Week 2)
- [ ] Environment variable detection from properties
- [ ] Complexity scoring algorithm
- [ ] AI validation mode recommendation
- [ ] Success pattern generation

### Phase 3: Integration (Week 3)
- [ ] Integrate with scaffold_integration_test.py
- [ ] Add interactive refinement mode
- [ ] Create analyze-pr-example.py wrapper
- [ ] Test with recent PR examples

### Phase 4: Documentation (Week 4)
- [ ] Create maintainer workflow guide
- [ ] Document pattern extraction logic
- [ ] Add troubleshooting guide
- [ ] Create video walkthrough

## Success Criteria
- Tool can analyze 90% of new PR examples successfully
- Generated tests pass on first run 80% of the time
- Time to add integration test reduced from 15 minutes to 2 minutes
- Maintainers prefer tool over manual creation

## Future Enhancements
- GitHub CLI integration for direct PR checkout
- Batch processing for multiple examples
- Learning from previous test configurations
- Integration with PR review comments
- Automated PR update with generated tests

## Notes
- Keep JBang as primary test execution framework
- JUnit5 framework remains as advanced maintainer tool only
- Focus on zero-friction contributor experience
- Maintain backward compatibility with existing tests

---

*This plan was created on 2025-08-08 and should be reviewed before implementation.*