# Spring AI Recursive Advisors Demo

Demonstrates the **Recursive Advisors** pattern in Spring AI 2.0, using `ToolCallingAdvisor` to handle tool-calling loops within the advisor chain.

## Overview

`ToolCallingAdvisor` is a built-in recursive advisor that iterates through the advisor chain until all tool calls are completed. It is **auto-registered** by default — you only need to add it explicitly when customizing its configuration or controlling its position in the chain.

```java
ChatClient chatClient = chatClientBuilder
    .defaultTools(new MyTools())
    .defaultAdvisors(
        ToolCallingAdvisor.builder().build(), // optional: auto-registered if absent
        new MyLogAdvisor())                   // logs each iteration
    .build();
```

`MyLogAdvisor` implements `BaseAdvisor` and logs requests/responses at each recursive iteration, providing visibility into the tool-calling loop:

```
REQUEST: [user: "What is current weather in Paris?"]
RESPONSE: [assistant tool call → weather("Paris")]

REQUEST: [user + assistant tool call + tool result]
RESPONSE: [assistant: "The current weather in Paris is sunny with a temperature of 25°C."]
```

## Prerequisites

- Java 25+
- Maven 3.6+
- Anthropic API key (`ANTHROPIC_API_KEY`)

## Run

```bash
export ANTHROPIC_API_KEY=your-key
./mvnw spring-boot:run
```

## Reference

- [Spring AI Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Spring AI Tool Calling](https://docs.spring.io/spring-ai/reference/api/tools.html)
