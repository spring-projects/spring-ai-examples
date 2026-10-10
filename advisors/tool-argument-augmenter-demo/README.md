# Tool Argument Augmenter Demo

Demonstrates Spring AI's [Tool Argument Augmentation](https://docs.spring.io/spring-ai/reference/api/tools.html#tool-argument-augmentation) — a way to inject extra arguments into tool calls (e.g. LLM reasoning, confidence, memory notes) without modifying the underlying tool implementation.

## How It Works

```
User: "What is the weather in Paris?"
        │
        ▼
Tool schema augmented transparently:
  Original:  { location: string }
  Augmented: { location: string, innerThought: string,
               confidence: string, memoryNotes: string[] }
        │
        ▼
LLM fills all arguments including reasoning fields
        │
        ├─► argumentConsumer processes extra args (logging, observability)
        │
        └─► Original tool receives only: { location: "Paris" }
```

## Key Components

**`AgentThinking`** — defines the extra arguments added to every tool schema:

```java
public record AgentThinking(
    @ToolParam(description = "Step-by-step reasoning for calling this tool", required = true)
    String innerThought,

    @ToolParam(description = "Confidence level (low, medium, high)", required = false)
    String confidence,

    @ToolParam(description = "Key insights to remember for future interactions", required = true)
    List<String> memoryNotes
) {}
```

**`AugmentedToolCallbackProvider`** — wraps tools and processes the extra arguments:

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
        logger.info("Tool: {}", event.toolDefinition().name());
    })
    .removeExtraArgumentsAfterProcessing(true) // strip before calling actual tool
    .build();
```

**Wired into `ChatClient` with memory and logging advisors:**

```java
ChatClient chatClient = chatClientBuilder
    .defaultTools(provider)
    .defaultAdvisors(
        MessageChatMemoryAdvisor.builder(chatMemory).order(Ordered.HIGHEST_PRECEDENCE + 1000).build(),
        new MyLogAdvisor())
    .build();
```

## Prerequisites

- Java 25+
- Spring Boot 4.0.0 / Spring AI 2.0.0
- OpenAI API key

## Run

```bash
export OPENAI_API_KEY=your-api-key

cd advisors/tool-argument-augmenter-demo
./mvnw spring-boot:run
```

## Sample Output

```
LLM Reasoning: The user wants current weather in Paris. I should call the weather tool with location=Paris.
Confidence: high
Memory Notes: [User interested in Paris weather]
Tool: weather

MyResponse[result=The current weather in Paris is sunny with a temperature of 25°C., thinking=...]
```

## Resources

- [Tool Argument Augmentation Docs](https://docs.spring.io/spring-ai/reference/api/tools.html#tool-argument-augmentation)
- [Explainable AI Agents Blog Post](https://spring.io/blog/2025/12/21/explainable-ai-agents-capture-llm-tool-call-reasoning-with-spring-ai)
- [Spring AI Advisors Guide](https://docs.spring.io/spring-ai/reference/api/advisors.html)
