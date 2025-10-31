# Spring AI Recursive Advisors Demo

A Spring Boot demonstration project showcasing the new **Recursive Advisors** feature in Spring AI 1.1.0-M4+, which enables looping through advisor chains multiple times for iterative AI workflows.

## Overview

This project demonstrates the **Recursive Advisors** pattern using Spring AI's `ToolCallAdvisor` - a built-in recursive advisor that handles tool calling loops within the advisor chain. Key features shown:

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Anthropic API key

## Key Components

### 1. Main Application (`RecursiveAdvisorDemoApplication.java`)

The main class demonstrates the recursive advisor pattern:

```java
ChatClient chatClient = chatClientBuilder
    .defaultTools(new MyTools())
    .defaultAdvisors(
        ToolCallAdvisor.builder().build(),  // Built-in recursive advisor
        new MyLogAdvisor())                 // Custom logging advisor
    .build();
```

Key aspects:
- **ToolCallAdvisor**: Built-in recursive advisor that loops until all tool calls are completed
    - **User-Controlled Tool Execution**: Tools execute within the advisor chain, not inside the model
- **Advisor Ordering**: Multiple advisors working together in the chain

### 2. Custom Tools (`MyTools` class)

```java
@Tool(description = "Get the current weather for a given location")
public String weather(String location) {
    return "The current weather in " + location + " is sunny with a temperature of 25°C.";
}
```

Demonstrates how to create custom tools that the AI can call during conversations.

### 3. Custom Advisor (`MyLogAdvisor` class)

Implements a non-recursive advisor to demonstrate observability in advisor chain flows:

```java
static class MyLogAdvisor implements BaseAdvisor {
    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        print("REQUEST", request.prompt().getInstructions());
        return request;
    }
    
    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        print("RESPONSE", response.chatResponse().getResults());
        return response;
    }
}
```

This advisor logs each iteration as the `ToolCallAdvisor` loops through tool executions, providing visibility into the recursive process.

## Expected output 

```
REQUEST:[{"messageType":"USER","metadata":{"messageType":"USER"},"media":[],"text":"What is current weather in Paris?"}]

RESPONSE:[{"metadata":{"finishReason":"tool_use","contentFilters":[],"empty":true},"output":{"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[],"media":[],"text":"I'll check the current weather in Paris for you."}},{"metadata":{"finishReason":"tool_use","contentFilters":[],"empty":true},"output":{"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[{"id":"toolu_01HNde1Z7mwtwXh4qiK3tavZ","type":"function","name":"weather","arguments":"{\"location\":\"Paris\"}"}],"media":[],"text":""}}]

REQUEST:[{"messageType":"USER","metadata":{"messageType":"USER"},"media":[],"text":"What is current weather in Paris?"},{"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[{"id":"toolu_01HNde1Z7mwtwXh4qiK3tavZ","type":"function","name":"weather","arguments":"{\"location\":\"Paris\"}"}],"media":[],"text":""},{"messageType":"TOOL","metadata":{"messageType":"TOOL"},"responses":[{"id":"toolu_01HNde1Z7mwtwXh4qiK3tavZ","name":"weather","responseData":"\"The current weather in Paris is sunny with a temperature of 25°C.\""}],"text":""}]

RESPONSE:[{"metadata":{"finishReason":"end_turn","contentFilters":[],"empty":true},"output":{"messageType":"ASSISTANT","metadata":{"messageType":"ASSISTANT"},"toolCalls":[],"media":[],"text":"The current weather in Paris is sunny with a temperature of 25°C."}}]

The current weather in Paris is sunny with a temperature of 25°C.
```
