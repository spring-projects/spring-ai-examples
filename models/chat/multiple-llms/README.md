# Spring AI Multiple LLMs Demo

A Spring Boot application showcasing how to integrate and switch between multiple Large Language Models (LLMs) using Spring AI. This project demonstrates working with both OpenAI and Anthropic models in a command-line interface application.

## Overview

This application serves as a practical example of building a chat interface that can seamlessly switch between different AI providers. It leverages Spring AI's abstraction layer to provide a unified interface for interacting with various LLM providers while maintaining clean and maintainable code.

## Project Requirements

- Java 17 or later
- Spring Boot 3.2 or later
- Valid API keys for:
    - OpenAI
    - Anthropic

## Dependencies

The project uses the following key dependencies:

- Spring Boot
- Spring AI
- Spring AI OpenAI Support
- Spring AI Anthropic Support

## Configuration

The application requires configuration of API keys for both OpenAI and Anthropic. These are specified in `application.properties`:

```properties
spring.application.name=multiple-llms

# OpenAI configuration
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4

# Anthropic configuration
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20240620
```

Set your API keys as environment variables:

```bash
export OPENAI_API_KEY=your_openai_key_here
export ANTHROPIC_API_KEY=your_anthropic_key_here
```

## Getting Started

After setting up your environment variables, build the project using your preferred build tool. The application uses Spring Boot's standard build process.

## Running the Application

Execute the application using:

```bash
./mvnw spring-boot:run
```

Once running, you'll be presented with a model selection prompt:

```
Select your AI model:
1. OpenAI
2. Anthropic
Enter your choice (1 or 2):
```

After selecting a model, you can start chatting with the AI. Type 'exit' to quit the application.

## Code Examples

### Setting up Chat Clients

The application configures separate chat clients for each AI provider:

```java
@Bean
public ChatClient openAIChatClient(OpenAiChatModel chatModel) {
    return ChatClient.create(chatModel);
}

@Bean
public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
    return ChatClient.create(chatModel);
}
```

### Handling User Input

The application uses a simple command-line interface to interact with the selected model:

```java
while (true) {
    System.out.print("\nUSER: ");
    String input = scanner.nextLine();
    if (input.equalsIgnoreCase("exit")) {
        System.out.println("Goodbye!");
        break;
    }
    try {
        String response = chat.prompt(input).call().content();
        System.out.println("ASSISTANT: " + response);
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        System.out.println("Please try again.");
    }
}
```


Built with ❤️ using Spring AI