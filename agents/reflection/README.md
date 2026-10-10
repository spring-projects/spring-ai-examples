# Spring AI Reflection Agent Application

This project demonstrates the use of Spring AI to create a self-improving code generation system. The Reflection Agent uses two ChatClient instances in an iterative loop - one for generation and one for critique - to produce high-quality Java code.

It is based on the code in the repository https://github.com/neural-maze/agentic_patterns

The application implements a reflection-based system where the **Reflection Agent**:

- Uses a **generation ChatClient** to create code based on user prompts
- Uses a **critique ChatClient** to review the generated code
- Iteratively improves the code by feeding critique back to the **generation ChatClient**
- Continues this loop until the **critique ChatClient** is satisfied with the quality


## Prerequisites
- Java 25 or higher
- Maven

This examples uses OpenAI as the model provider.

Before using the AI commands, make sure you have a developer token from OpenAI.

Create an account at [OpenAI Signup](https://platform.openai.com/signup) and generate the token at [API Keys](https://platform.openai.com/account/api-keys).

The Spring AI project defines a configuration property named `spring.ai.openai.api-key` that you should set to the value of the API key obtained from OpenAI.

Exporting an environment variable is one way to set that configuration property:

```shell
export SPRING_AI_OPENAI_API_KEY=<INSERT KEY HERE>
```



## Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run the application using Maven wrapper:
   `./mvnw spring-boot:run`

## Project Structure
### Main Components

* `Application.java`: The main Spring Boot application that provides the command-line interface
* `ReflectionAgent.java`: The core component that manages the iteration between generation and critique


## How It Works

### Initial Setup

The Reflection Agent creates two `ChatClient` instances:

- `generateChatClient`: For generating Java code based on user requests
- `critiqueChatClient`: For reviewing and critiquing the generated code

## Generation Process

- User inputs a request
- The generation `ChatClient` creates initial code
- The critique `ChatClient` reviews the code
- If improvements are needed, the generation `ChatClient` creates a revised version
- This continues for up to `maxIterations` or until the critique `ChatClient` approves (`<OK>`)
- 
## ChatClient Configurations

- **Generation ChatClient** system prompt:

```text
You are a Java programmer tasked with generating high quality
Java code. Your task is to generate the best content possible
for the user's request.
```
- **Critique ChatClient** system prompt:
```text
You are tasked with generating critique and recommendations for
the user's generated content. If the user content has something
wrong or something to be improved, output a list of
recommendations and critiques.
```

## Example Run

In this sample run, the user requested a JUnit 5 test for a `Person` class.  See the file `JacksonTestAgent.md` for the actual output.

### Initial Generation

- The generation `ChatClient` created a basic `Person` class and test class
- Included serialization/deserialization functionality
- Implemented basic test cases

### Critique Phase

The critique `ChatClient identified several improvements:

- Better error handling
- Improved code readability
- Need for edge case testing
- Better test structure
- Expanded test coverage
- Enhanced Java class structure
- Modern Java feature usage

### Final Result

The generation `ChatClient` created improved code with:

- Separated test methods for better modularity
- Enhanced error handling with detailed messages
- Added edge case testing for null values
- Improved code structure and readability
- Better test coverage

## MergeSort

The file `AgentMergeSort.md` shows a similar run to create a merge sort algorithm.  There is also a JUnit test of the code that was generated to show it works.
