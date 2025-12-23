# Recursive Advisor with Memory and Tool Argument Augmentation

This example demonstrates how to build **explainable AI agents** using Spring AI by capturing LLM reasoning during tool calls and integrating with chat memory for enhanced context across conversations.

## Overview

When building AI agents with tool calling capabilities, understanding **why** an LLM chose a particular tool is crucial for debugging, observability, and building trustworthy AI systems. This demo showcases the Spring AI [Tool Argument Augmenter](https://docs.spring.io/spring-ai/reference/2.0-SNAPSHOT/api/tools.html#tool-argument-augmentation) utilities which enables:

- **Capturing LLM Reasoning**: Extract inner thoughts, confidence levels, and memory notes during tool execution
- **Transparent Schema Augmentation**: Dynamically extend tool schemas without modifying underlying tool implementations
- **Memory-Enhanced Reasoning**: Persist reasoning insights in conversation history for improved decision-making across extended interactions

## How It Works

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          Tool Argument Augmenter Flow                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  1. User asks: "What is current weather in Paris?"                      │
│                         │                                               │
│                         ▼                                               │
│  2. Tool Definition Augmentation                                        │
│     Original: { location: string }                                      │
│     Augmented: { location: string, innerThought: string,                │
│                  confidence: string, memoryNotes: string[] }            │
│                         │                                               │
│                         ▼                                               │
│  3. LLM Response with Reasoning                                         │
│     {                                                                   │
│       "location": "Paris",                                              │
│       "innerThought": "User wants weather info for Paris...",           │
│       "confidence": "high",                                             │
│       "memoryNotes": ["User interested in Paris weather"]               │
│     }                                                                   │
│                         │                                               │
│                         ▼                                               │
│  4. Argument Consumer processes reasoning (logging, memory storage)     │
│                         │                                               │
│                         ▼                                               │
│  5. Original tool receives only: { "location": "Paris" }                │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Key Components

### AgentThinking Record

Defines the additional arguments to capture from the LLM:

```java
public record AgentThinking(
    @ToolParam(description = "Your step-by-step reasoning for why you're calling this tool", 
               required = true) 
    String innerThought,
    
    @ToolParam(description = "Confidence level (low, medium, high) in this tool choice", 
               required = false) 
    String confidence,
    
    @ToolParam(description = "Key insights to remember for future interactions", 
               required = true) 
    List<String> memoryNotes
) {}
```

### AugmentedToolCallbackProvider

Wraps existing tools to transparently augment their schemas:

```java
AugmentedToolCallbackProvider<AgentThinking> provider = AugmentedToolCallbackProvider
    .<AgentThinking>builder()
    .toolObject(new MyTools())
    .argumentType(AgentThinking.class)
    .argumentConsumer(event -> {
        AgentThinking thinking = event.arguments();
        logger.info("LLM Reasoning: {}", thinking.innerThought());
        logger.info("Confidence: {}", thinking.confidence());
        logger.info("Memory Notes: {}", thinking.memoryNotes());
    })
    .removeExtraArgumentsAfterProcessing(true)
    .build();
```

### Integration with Advisors

Combines tool augmentation with Spring AI's advisor chain:

```java
ChatClient chatClient = chatClientBuilder
    .defaultToolCallbacks(provider)
    .defaultAdvisors(
        ToolCallAdvisor.builder()
            .conversationHistoryEnabled(false).build(),
        MessageChatMemoryAdvisor.builder(chatMemory).build(),
        new MyLogAdvisor())
    .build();
```

## Running the Example

### Prerequisites

- Java 17 or higher
- OpenAI API key (or Anthropic API key)

### Configuration

Set your API key as an environment variable:

```bash
export OPENAI_API_KEY=your-api-key
# or
export ANTHROPIC_API_KEY=your-api-key
```

### Build and Run

```bash
cd advisors/recursive-advisor-with-memory
./mvnw spring-boot:run
```

## Sample Output

When running the example, you'll see the LLM's reasoning captured before each tool call:

```
LLM Reasoning: The user is asking about the current weather in Paris. I need to call the weather tool...
Confidence: high
Memory Notes: [User interested in Paris weather, May need follow-up about activities]
Tool: weather

REQUEST: [{"type":"USER","text":"What is current weather in Paris?"}]

RESPONSE: [{"output":{"text":"The current weather in Paris is sunny with a temperature of 25°C."}}]
```

## Use Cases

- **Debugging**: Understand why your AI agent made specific tool choices
- **Observability**: Log and monitor agent reasoning in production
- **Memory Enhancement**: Store insights for improved context in future conversations
- **Multi-Agent Coordination**: Pass coordination signals between agents
- **Analytics**: Track tool usage patterns and decision quality

## Resources

- [Explainable AI Agents Blog Post](https://spring.io/blog/2025/12/21/explainable-ai-agents-capture-llm-tool-call-reasoning-with-spring-ai)
- [Spring AI Tool Calling Documentation](https://docs.spring.io/spring-ai/reference/api/tools.html)
- [Spring AI Advisors Guide](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Tool Argument Augmenter](https://docs.spring.io/spring-ai/reference/2.0-SNAPSHOT/api/tools.html#tool-argument-augmentation)
