# Spring AI Structured Output Demo

Transform AI responses into type-safe Java objects effortlessly with Spring AI's structured output capabilities. This project demonstrates how to convert natural language responses from Large Language Models (LLMs) into strongly-typed Java objects without writing complex parsing logic.

## Why Structured Output Matters

Working with AI models typically means dealing with unstructured text responses. Converting these responses into usable data structures traditionally requires:

- Writing custom parsing logic
- Handling edge cases and malformed responses
- Maintaining complex regex patterns or string manipulation code
- Implementing error handling for parsing failures

Spring AI eliminates these challenges by providing built-in conversion capabilities that transform AI responses directly into Java objects. This project showcases this functionality through a practical example: converting information about technical authors into structured data.

## Project Overview

This application demonstrates structured output conversion by:
1. Taking user input for a technical author's name
2. Querying an AI model for information about the author
3. Automatically converting the response into a strongly-typed `AuthorInfo` object
4. Displaying the structured information to the user

## Requirements

- Java 17 or higher
- OpenAI API key
- Spring Boot 3.2 or higher
- Spring AI dependencies

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── io/spring/examples/
│   │       ├── Application.java       # Main application class
│   │       └── AuthorInfo.java       # Data structure for author information
│   └── resources/
│       └── application.properties    # Configuration properties
```

## Key Features

### Structured Data Model

The `AuthorInfo` record defines our structured output format:

```java
public record AuthorInfo(
    String name,
    String primaryGenre,
    List<String> notableBooks,
    int yearsActive,
    String specialization
) {}
```

### Automatic Conversion

Spring AI handles the conversion from AI response to Java object automatically:

```java
var authorInfo = chat.prompt()
    .user(u -> {
        u.text("Please give me information about ${author}");
        u.param("author", authorName);
    })
    .call()
    .entity(AuthorInfo.class);  // Automatic conversion to AuthorInfo
```

## Getting Started

1. Set your OpenAI API key:
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

3. Enter author names when prompted:
   ```
   Who is your favorite technical author? (e.g., 'Rod Johnson', or type 'exit' to quit)
   USER: Rod Johnson
   ```

## Response Format

The application displays structured information about the author:

```
=== Author Analysis ===
Name: Rod Johnson
Primary Genre: Technical Writing
Notable Books: Expert One-on-One J2EE Design and Development, Expert One-on-One J2EE Development without EJB
Years Active: 20
Specialization: Enterprise Java Development and Spring Framework
```

## Technical Details

### Configuration

The application uses Spring Boot's configuration properties:

```properties
spring.application.name=structured-output
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
```

### Error Handling

Spring AI's structured output converter handles various edge cases:
- Malformed AI responses
- Missing fields in the response
- Type conversion errors

## Benefits of Using Spring AI's Structured Output

1. **Type Safety**: Get compile-time checking for your AI response structures
2. **Clean Code**: Eliminate boilerplate parsing code
3. **Reliability**: Robust handling of edge cases and malformed responses
4. **Maintainability**: Changes to response structure only require updating the data model
5. **Integration**: Seamless integration with Spring's ecosystem

## Learn More

For more information about Spring AI and structured output conversion, visit:
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Spring AI GitHub Repository](https://github.com/spring-projects/spring-ai)

---

Built with ❤️ using Spring AI