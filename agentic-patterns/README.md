# Agentic Patterns

This project demonstrates practical implementations of the workflow patterns for building effective LLM-based systems, as described in [Anthropic's research on building effective agents](https://www.anthropic.com/research/building-effective-agents).

## Overview

The project provides concrete implementations of five fundamental workflow patterns that can be used to build effective LLM-based systems. Each pattern is implemented as a separate module with its own specific use cases and benefits.

## Workflow Patterns

### 1. Chain Workflow
[chain-workflow/](chain-workflow/)

Implements prompt chaining to decompose tasks into a sequence of LLM calls where each step processes the output of the previous one. Ideal for tasks that can be cleanly broken down into fixed subtasks.

**When to Use:**
- Tasks with clear sequential steps
- When you want to trade latency for higher accuracy
- When each step builds on the previous step's output

**Example Applications:**
- Data transformation pipelines
- Multi-step text processing
- Document generation with structured steps

### 2. Parallelization Workflow
[parallelization-workflow/](parallelization-workflow/)

Enables concurrent processing of multiple LLM operations with two key variations:
- **Sectioning**: Breaking tasks into independent subtasks run in parallel
- **Voting**: Running the same task multiple times to get diverse outputs

**When to Use:**
- Processing large volumes of similar but independent items
- Tasks requiring multiple independent perspectives
- When processing time is critical and tasks are parallelizable

**Example Applications:**
- Batch processing of documents
- Multi-perspective content analysis
- Parallel validation checks

### 3. Routing Workflow
[routing-workflow/](routing-workflow/)

Implements a classification system that directs input to specialized followup tasks, enabling separation of concerns and optimized processing for different types of input.

**When to Use:**
- Complex tasks with distinct categories of input
- When different inputs require specialized processing
- When classification can be handled accurately

**Example Applications:**
- Customer support ticket routing
- Content moderation systems
- Query optimization based on complexity

### 4. Orchestrator-Workers
[orchestrator-workers/](orchestrator-workers/)

Implements a flexible system where a central LLM orchestrates task decomposition and delegates to specialized worker LLMs.

**When to Use:**
- Complex tasks where subtasks can't be predicted upfront
- Tasks requiring different approaches or perspectives
- Situations needing adaptive problem-solving

**Example Applications:**
- Complex code generation tasks
- Multi-source research tasks
- Adaptive content creation

### 5. Evaluator-Optimizer
[evaluator-optimizer/](evaluator-optimizer/)

Implements an iterative refinement process where one LLM generates solutions while another provides evaluation and feedback.

**When to Use:**
- Clear evaluation criteria exist
- Iterative refinement provides measurable value
- Tasks benefit from multiple rounds of critique

**Example Applications:**
- Code review and improvement
- Content quality optimization
- Translation refinement
- Complex search tasks

## Implementation Details

All workflows are implemented using:
- Spring AI for LLM interactions
- Spring Boot for application framework
- Java 17+ for modern language features

Each workflow module contains:
- Core workflow implementation
- Usage examples
- Customization options
- Unit tests

## Getting Started

1. Clone the repository
2. Choose the appropriate workflow pattern for your use case
3. See the individual module's README for specific implementation details

## Spring AI Features

This project leverages several key features from Spring AI to implement the workflow patterns effectively:

1. **ChatClient Interface**: Core abstraction for interacting with LLM models
   - Consistent API across different LLM providers
   - Fluent interface for prompt construction
   - Built-in retry and error handling

2. **Model Support**: Wide range of supported LLM providers
   - OpenAI (GPT models)
   - Azure OpenAI
   - Anthropic (Claude models)
   - Ollama (local models)
   - And more...

3. **Structured Output**: Type-safe handling of LLM responses
   - Convert JSON responses to Java objects
   - Strongly-typed response handling
   - Validation and error handling

4. **Prompt Management**: Flexible prompt handling
   - Template-based prompts
   - System and user message separation
   - Context management

These features provide a robust foundation for building reliable and maintainable LLM-based applications.

### Spring AI Model Portability

The workflows in this project are model-agnostic and can work with any of the [chat models supported by Spring AI](https://docs.spring.io/spring-ai/reference/1.0/api/chat/comparison.html). To switch between different models:

1. Replace the model-specific starter dependency in your `pom.xml`:

```xml
<!-- OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<!-- Azure OpenAI -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-openai-spring-boot-starter</artifactId>
</dependency>

<!-- Anthropic -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>

<!-- Ollama -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```

2. Configure the model-specific properties in your `application.properties`:

```properties
# OpenAI Configuration
spring.ai.openai.api-key=your-api-key
spring.ai.openai.model=gpt-3.5-turbo

# Azure OpenAI Configuration
spring.ai.azure.openai.api-key=your-api-key
spring.ai.azure.openai.endpoint=your-endpoint
spring.ai.azure.openai.model=gpt-35-turbo

# Anthropic Configuration
spring.ai.anthropic.api-key=your-api-key
spring.ai.anthropic.model=claude-3-opus-20240229

# Ollama Configuration
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.model=llama3.2:latest
```

Detailed configuration options for each model can be found in the [Spring AI Chat Models documentation](https://docs.spring.io/spring-ai/reference/1.0/api/chatmodel.html).


### Spring AI Structured Output

Several patterns in this project use Spring AI's [Structured Output Converter](https://docs.spring.io/spring-ai/reference/1.0/api/structured-output-converter.html) to handle structured responses from LLMs. This feature allows for:
- Converting LLM responses into strongly-typed Java objects
- Ensuring consistent response formats
- Type-safe handling of LLM outputs

#### Example Usage

```java
// Define a record for structured output
public record EvaluationResponse(Evaluation evaluation, String feedback) {
    public enum Evaluation {
        PASS, NEEDS_IMPROVEMENT, FAIL
    }
}

// Use the entity() method to convert response to your type
EvaluationResponse response = chatClient.prompt(prompt)
    .call()
    .entity(EvaluationResponse.class);

// Access typed fields
if (response.evaluation() == Evaluation.PASS) {
    // Handle passing evaluation
}
```

#### Implementation Examples

The feature is used in several workflow patterns:

1. **Evaluator-Optimizer Workflow**: Structures evaluation responses with pass/fail status and feedback
```java
record EvaluationResponse(Evaluation evaluation, String feedback)
```

2. **Routing Workflow**: Structures routing decisions with reasoning and selection
```java
record RoutingResponse(String reasoning, String selection)
```

3. **Chain Workflow**: Structures intermediate transformation results
```java
record TransformationResult(String output, List<String> steps)
```


## References

- [Building Effective Agents (Anthropic Research)](https://www.anthropic.com/research/building-effective-agents)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/1.0/api/chatclient.html)
- [Spring AI Chat Models](https://docs.spring.io/spring-ai/reference/1.0/api/chatmodel.html)
- [Spring AI Structured Output](https://docs.spring.io/spring-ai/reference/1.0/api/structured-output-converter.html)
