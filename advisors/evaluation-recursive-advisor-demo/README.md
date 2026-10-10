# Spring AI LLM-as-a-Judge Demo

Demonstrates the **LLM-as-a-Judge** pattern in Spring AI 2.0 using a custom `SelfRefineEvaluationAdvisor` — a recursive advisor that evaluates AI responses and retries with feedback until a quality threshold is met. Both the response model and judge use a local Ollama model.

## How It Works

`SelfRefineEvaluationAdvisor` implements `CallAdvisor` and loops recursively via `callAdvisorChain.copy(this).nextCall()`:

1. Generate a response with the primary model (Ollama)
2. Evaluate it using the configured local Ollama model on a 1–4 scale
3. If rating < `successRating`, augment the prompt with feedback and retry
4. Return when the rating passes or `maxRepeatAttempts` is exhausted

Tool-call responses are skipped automatically — only final text answers are evaluated.

```java
ChatClient chatClient = ChatClient.builder(ollamaChatModel)
    .defaultTools(new MyTools())
    .defaultAdvisors(
        SelfRefineEvaluationAdvisor.builder()
            .chatClientBuilder(ChatClient.builder(ollamaChatModel))
            .maxRepeatAttempts(15)
            .successRating(4)
            .order(0)
            .build(),
        new MyLoggingAdvisor(2))
    .build();
```

The weather tool returns random temperatures (including physically impossible values like -255°C) to intentionally trigger evaluation failures and retries.

## Sample Output

```
>>> Tool Call responseTemp: -255
Evaluation failed on attempt 1: temperature of -255°C is physically impossible.

>>> Tool Call responseTemp: 15
Evaluation passed on attempt 2: Excellent response with realistic weather data.

The current weather in Paris is sunny with a temperature of 15°C.
```

## Prerequisites

- Java 25+, Maven 3.6+
- [Ollama](https://ollama.com) running locally with a tool-calling-capable model

## Setup

```bash
# Pull a local model that supports tool calling (example)
ollama pull qwen3.5:latest

export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=qwen3.5:latest
./mvnw spring-boot:run
```

`application.properties` (already configured):
```properties
spring.ai.ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
spring.ai.ollama.chat.model=${OLLAMA_MODEL:qwen3.5:latest}
spring.ai.ollama.chat.temperature=0
spring.ai.ollama.chat.think=false
spring.ai.chat.client.enabled=false
```

The default model is used for both the weather response and its evaluation.
Override `OLLAMA_BASE_URL` and `OLLAMA_MODEL` to use another local Ollama
server/model. No Anthropic API key is required.

## Reference

- [Recursive Advisor Demo](../recursive-advisor-demo) — basic recursive patterns
- [Spring AI Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Judge Arena Leaderboard](https://huggingface.co/spaces/AtlaAI/judge-arena)
