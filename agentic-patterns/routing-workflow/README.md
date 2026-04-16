# Routing Workflow Pattern

This project implements the Routing workflow pattern as described in [Building Effective Agents](https://www.anthropic.com/research/building-effective-agents) by Anthropic. The pattern enables intelligent routing of inputs to specialized handlers based on content classification.

![Routing Workflow](https://www.anthropic.com/_next/image?url=https%3A%2F%2Fwww-cdn.anthropic.com%2Fimages%2F4zrzovbb%2Fwebsite%2F5c0c0e9fe4def0b584c04d37849941da55e5e71c-2401x1000.png&w=3840&q=75)

## Overview

The Routing workflow pattern is designed for complex tasks where different types of inputs are better handled by specialized processes. It uses an LLM to analyze input content and route it to the most appropriate specialized prompt or handler.

### Key Benefits

- **Separation of Concerns**: Each route can be optimized for specific types of input
- **Improved Accuracy**: Specialized prompts handle what they do best
- **Scalable Architecture**: Easy to add new routes and specialized handlers
- **Better Resource Utilization**: Route simpler tasks to lighter models

### When to Use

This workflow is particularly effective when:
- You have distinct categories of input that require different handling
- Classification can be handled accurately by an LLM
- Different types of input require different expertise or processing approaches

## Technical Requirements

- Java 17 or higher
- Maven 3.6+
- Anthropic API key exported as `ANTHROPIC_API_KEY`

## Getting Started

1. Export your Anthropic API key:

   ```bash
   export ANTHROPIC_API_KEY=<INSERT KEY HERE>
   ```

2. Build the module:

   ```bash
   ./mvnw clean install
   ```

3. Run the example:

   ```bash
   ./mvnw spring-boot:run
   ```

The sample application routes example support tickets to different specialized handlers and prints the selected responses to the console.

## Usage Example

```java
@Autowired
private ChatClient chatClient;

// Create the workflow
RoutingWorkflow workflow = new RoutingWorkflow(chatClient);

// Define specialized prompts for different types of input
Map<String, String> routes = Map.of(
    "billing", "You are a billing specialist. Help resolve billing issues...",
    "technical", "You are a technical support engineer. Help solve technical problems...",
    "general", "You are a customer service representative. Help with general inquiries..."
);

// Process input
String input = "My account was charged twice last week";
String response = workflow.route(input, routes);
```

## Common Use Cases

1. **Customer Support**
   - Route queries to appropriate departments (billing, technical, general)
   - Direct urgent issues to priority handling
   - Forward complex cases to specialist teams

2. **Content Moderation**
   - Route content to appropriate review processes
   - Direct sensitive content to human moderators
   - Send routine content through automated checks

3. **Query Optimization**
   - Route simple questions to smaller, faster models
   - Direct complex queries to more capable models
   - Forward specialized topics to domain-specific handlers

## Implementation Details

The implementation consists of two main components:

1. `RoutingWorkflow`: The main class that implements the routing logic
   - Analyzes input using LLM
   - Selects appropriate route
   - Processes input with specialized prompt

2. `RoutingResponse`: Record class that encapsulates routing decisions
   - Stores reasoning behind route selection
   - Maintains selected route information

## References

- [Building Effective Agents](https://www.anthropic.com/research/building-effective-agents) - Anthropic Research
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/1.0/api/chatclient.html)
