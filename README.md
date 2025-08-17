# Spring AI Examples

A comprehensive collection of examples demonstrating how to build AI-powered applications using Spring AI. This repository contains a variety of examples showcasing different features, patterns, and integrations available in the Spring AI project.

## Overview

Spring AI is a framework that simplifies the development of AI-powered applications by providing abstractions and integrations for various AI models and services. This repository contains examples that demonstrate how to use Spring AI to build different types of AI applications.

The examples are organized into several categories:

### Agentic Patterns

Examples demonstrating practical implementations of workflow patterns for building effective LLM-based systems, as described in [Anthropic's research on building effective agents](https://www.anthropic.com/research/building-effective-agents).

- [Chain Workflow](agentic-patterns/chain-workflow/) - Sequential processing of tasks
- [Parallelization Workflow](agentic-patterns/parallelization-workflow/) - Concurrent processing of tasks
- [Routing Workflow](agentic-patterns/routing-workflow/) - Classification and routing of inputs
- [Orchestrator-Workers](agentic-patterns/orchestrator-workers/) - Central orchestration with specialized workers
- [Evaluator-Optimizer](agentic-patterns/evaluator-optimizer/) - Iterative refinement with feedback

### Agents

Examples demonstrating how to build AI agents using Spring AI.

- [Reflection](agents/reflection/) - Agents that can reflect on their own reasoning

### Kotlin

Examples demonstrating how to use Spring AI with Kotlin.

- [Kotlin Hello World](kotlin/kotlin-hello-world/) - Basic Spring AI usage with Kotlin
- [Kotlin Function Callback](kotlin/kotlin-function-callback/) - Function callbacks in Kotlin
- [RAG with Kotlin](kotlin/rag-with-kotlin/) - Retrieval Augmented Generation in Kotlin

### Miscellaneous

Various examples demonstrating specific features of Spring AI.

- [Java Function Callback](misc/spring-ai-java-function-callback/) - Function callbacks in Java
- [OpenAI Streaming Response](misc/openai-streaming-response/) - Streaming responses from OpenAI

### Models

Examples demonstrating how to use different AI models with Spring AI.

- [Chat Hello World](models/chat/helloworld/) - Basic chat interaction with AI models

### Model Context Protocol (MCP)

Examples demonstrating how to use the Model Context Protocol to enable natural language interactions with various data sources.

- [SQLite](model-context-protocol/sqlite/) - Interact with SQLite databases
- [Brave](model-context-protocol/brave/) - Interact with the Brave browser
- [Filesystem](model-context-protocol/filesystem/) - Interact with the filesystem
- [Weather](model-context-protocol/weather/) - Interact with weather data
- [Web Search](model-context-protocol/web-search/) - Perform web searches
- And more...

### Prompt Engineering

Examples demonstrating prompt engineering techniques and patterns.

- [Prompt Engineering Patterns](prompt-engineering/prompt-engineering-patterns/) - Various prompt engineering patterns

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- API keys for the AI models you want to use (e.g., OpenAI, Anthropic, etc.)

### Running an Example

1. Clone the repository:
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd spring-ai-examples
   ```

2. Set up your API keys as environment variables:
   ```bash
   # For OpenAI
   export SPRING_AI_OPENAI_API_KEY=your-api-key-here

   # For Anthropic
   export SPRING_AI_ANTHROPIC_API_KEY=your-api-key-here

   # For other providers, check the specific example's README
   ```

3. Run an example using the provided script:
   ```bash
   ./run-example.sh <example-directory>

   # For example:
   ./run-example.sh models/chat/helloworld
   ```

   Or navigate to the example directory and run it directly:
   ```bash
   cd models/chat/helloworld
   ./mvnw spring-boot:run
   ```

### Switching Models

Most examples are model-agnostic and can work with any of the [chat models supported by Spring AI](https://docs.spring.io/spring-ai/reference/1.0/api/chat/comparison.html). To switch between different models:

1. Replace the model-specific starter dependency in the example's `pom.xml`
2. Configure the model-specific properties in the example's `application.properties`

See the [Agentic Patterns README](agentic-patterns/README.md#spring-ai-model-portability) for detailed instructions.

## Documentation

Each example contains its own README.md file with detailed information about:
- What the example demonstrates
- Prerequisites and setup
- How to run the example
- Technical details and code explanations

For more information about Spring AI, visit the [official documentation](https://docs.spring.io/spring-ai/reference/1.0/index.html).

## References

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/1.0/index.html)
- [Spring AI GitHub Repository](https://github.com/spring-projects/spring-ai)
- [Building Effective Agents (Anthropic Research)](https://www.anthropic.com/research/building-effective-agents)
- [Model Context Protocol](https://github.com/modelcontextprotocol/docs)
