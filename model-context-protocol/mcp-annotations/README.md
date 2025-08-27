# Spring AI MCP Annotations Examples

This directory contains examples demonstrating the Model Context Protocol (MCP) using Spring AI's annotation-based approach. These examples showcase MCP capabilities including tools, resources, prompts, completions, and client-side handlers using declarative annotations.

## Overview

The MCP Annotations examples demonstrate:

- **Annotation-Driven Development**: Simplified MCP development using `@McpTool`, `@McpResource`, `@McpPrompt`, `@McpComplete`, and client annotations
- **Declarative Configuration**: Automatic registration and configuration through annotations
- **Client-Server Communication**: Full bidirectional MCP communication patterns
- **Multiple MCP Features**: Tools, resources, prompts, completions, and client handlers
- **Mixed Annotation Styles**: Both MCP annotations and Spring AI `@Tool` annotations

## Projects

### 1. mcp-annotations-server
A comprehensive MCP server implementation showcasing all major MCP features using annotations.

#### Tools
- **Weather Tool**: Temperature data from Open-Meteo API using `@McpTool`
- **Additional Tools**: Weather forecast and alerts from Weather.gov API using Spring AI `@Tool`

#### Resources
- **User Profiles**: Complete user information with multiple access patterns
- **User Attributes**: Individual attribute access with URI variables
- **User Status**: Dynamic status information with different return types
- **User Connections**: Social connections and relationships
- **User Notifications**: Dynamic notification generation
- **User Avatars**: Binary data handling with custom MIME types
- **User Location**: Simple location information

#### Prompts
- **Greeting Prompts**: Simple parameterized greetings
- **Personalized Messages**: Complex prompts with multiple parameters and logic
- **Conversation Starters**: Multi-message conversation flows
- **Dynamic Content**: Map-based argument handling
- **Single Message**: Single message responses
- **String List**: List-based responses

#### Completions
- **Username Completion**: Auto-complete for user status URIs
- **Name Completion**: Auto-complete for personalized message prompts
- **Country Completion**: Travel-related country name completion

### 2. mcp-annotations-client
A simple MCP client implementation demonstrating client-side MCP capabilities using annotations.

#### Handler Types
- **Progress Handlers**: Track long-running operations with `@McpProgress`
- **Logging Handlers**: Centralized logging from MCP servers with `@McpLogging`
- **Sampling Handlers**: LLM request delegation with `@McpSampling`
- **Elicitation Handlers**: User interaction prompts with `@McpElicitation`

## Architecture Overview

### Server Architecture

```java
@SpringBootApplication
public class McpServerApplication {
    // Traditional Spring AI tool integration
    @Bean
    public ToolCallbackProvider weatherTools(SpringAiToolProvider weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }
}
```

The server uses automatic annotation scanning to register MCP providers. All `@McpTool`, `@McpResource`, `@McpPrompt`, and `@McpComplete` annotated methods are automatically discovered and registered.

### Client Architecture

```java
@SpringBootApplication
public class McpClientApplication {
    @Bean
    public CommandLineRunner predefinedQuestions(List<McpSyncClient> mcpClients) {
        return args -> {
            for (McpSyncClient mcpClient : mcpClients) {
                // Call tools and interact with MCP servers
                CallToolRequest toolRequest = CallToolRequest.builder()
                    .name("tool1")
                    .arguments(Map.of("input", "test input"))
                    .progressToken("test-progress-token")
                    .build();
                
                CallToolResult response = mcpClient.callTool(toolRequest);
            }
        };
    }
}
```

Client handlers are automatically registered through annotation scanning.

## Key Implementation Examples

### Tool Implementation

```java
@Service
public class ToolProvider {
    @McpTool(description = "Get the temperature (in celsius) for a specific location")
    public WeatherResponse getTemperature(
            @McpToolParam(description = "The location latitude") double latitude,
            @McpToolParam(description = "The location longitude") double longitude,
            @McpToolParam(description = "The city name") String city) {
        
        return restClient.get()
            .uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
                latitude, longitude)
            .retrieve()
            .body(WeatherResponse.class);
    }
}
```

### Resource Implementation

```java
@Service
public class UserProfileResourceProvider {
    @McpResource(uri = "user-profile://{username}", 
                 name = "User Profile", 
                 description = "Provides user profile information for a specific user")
    public ReadResourceResult getUserProfile(ReadResourceRequest request, String username) {
        String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));
        return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain", profileInfo)));
    }

    @McpResource(uri = "user-attribute://{username}/{attribute}", 
                 name = "User Attribute", 
                 description = "Provides a specific attribute from a user's profile")
    public ReadResourceResult getUserAttribute(String username, String attribute) {
        Map<String, String> profile = userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>());
        String attributeValue = profile.getOrDefault(attribute, "Attribute not found");
        
        return new ReadResourceResult(
            List.of(new TextResourceContents("user-attribute://" + username + "/" + attribute, 
                   "text/plain", username + "'s " + attribute + ": " + attributeValue)));
    }
}
```

### Prompt Implementation

```java
@Service
public class PromptProvider {
    @McpPrompt(name = "personalized-message", 
               description = "Generates a personalized message based on user information")
    public GetPromptResult personalizedMessage(
            McpSyncServerExchange exchange,
            @McpArg(name = "name", description = "The user's name", required = true) String name,
            @McpArg(name = "age", description = "The user's age", required = false) Integer age,
            @McpArg(name = "interests", description = "The user's interests", required = false) String interests) {
        
        // Log the prompt generation
        exchange.loggingNotification(LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)
            .data("personalized-message event").build());

        StringBuilder message = new StringBuilder();
        message.append("Hello, ").append(name).append("!\n\n");
        
        if (age != null) {
            message.append("At ").append(age).append(" years old, you have ");
            if (age < 30) {
                message.append("so much ahead of you.\n\n");
            } else if (age < 60) {
                message.append("gained valuable life experience.\n\n");
            } else {
                message.append("accumulated wisdom to share with others.\n\n");
            }
        }

        return new GetPromptResult("Personalized Message",
            List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
    }
}
```

### Completion Implementation

```java
@Service
public class CompletionProvider {
    @McpComplete(uri = "user-status://{username}")
    public List<String> completeUsername(String usernamePrefix) {
        String prefix = usernamePrefix.toLowerCase();
        if (prefix.isEmpty()) {
            return List.of("Enter a username");
        }

        String firstLetter = prefix.substring(0, 1);
        List<String> usernames = usernameDatabase.getOrDefault(firstLetter, List.of());

        return usernames.stream()
            .filter(username -> username.toLowerCase().startsWith(prefix))
            .toList();
    }

    @McpComplete(prompt = "personalized-message")
    public CompleteResult completeCountryName(CompleteRequest request) {
        String prefix = request.argument().value().toLowerCase();
        String firstLetter = prefix.substring(0, 1);
        List<String> countries = countryDatabase.getOrDefault(firstLetter, List.of());

        List<String> matches = countries.stream()
            .filter(country -> country.toLowerCase().startsWith(prefix))
            .toList();

        return new CompleteResult(new CompleteCompletion(matches, matches.size(), false));
    }
}
```

### Client Handler Implementation

```java
@Service
public class McpClientHandlerProviders {
    @McpProgress(clients = "server1")
    public void progressHandler(ProgressNotification progressNotification) {
        logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
            progressNotification.progressToken(), progressNotification.progress(),
            progressNotification.total(), progressNotification.message());
    }

    @McpLogging(clients = "server1")
    public void loggingHandler(LoggingMessageNotification loggingMessage) {
        logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
    }

    @McpSampling(clients = "server1")
    public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
        String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
        String modelHint = llmRequest.modelPreferences().hints().get(0).name();

        return CreateMessageResult.builder()
            .content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
            .build();
    }

    @McpElicitation(clients = "server1")
    public ElicitResult elicitationHandler(McpSchema.ElicitRequest request) {
        logger.info("MCP ELICITATION: {}", request);
        return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
    }
}
```

## Key Dependencies

### Server Dependencies
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

### Client Dependencies
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

## Running the Examples

### Prerequisites

- Java 17 or later
- Maven 3.6+

### Step 1: Start the MCP Annotations Server

```bash
cd mcp-annotations-server
./mvnw clean package -DskipTests
java -jar target/mcp-annotations-server-0.0.1-SNAPSHOT.jar
```

The server will start on `http://localhost:8080` with SSE transport enabled.

### Step 2: Run the MCP Annotations Client

```bash
cd mcp-annotations-client
./mvnw clean package
java -jar target/mcp-annotations-client-0.0.1-SNAPSHOT.jar
```

## Available Features

### Tools
- `getTemperature`: Get temperature data using Open-Meteo API

### Resources
- `user-profile://{username}`: Complete user profile information
- `user-attribute://{username}/{attribute}`: Specific user attributes
- `user-status://{username}`: User online status
- `user-connections://{username}`: User social connections
- `user-notifications://{username}`: User notifications
- `user-location://{username}`: User location information
- `user-avatar://{username}`: User avatar images (base64)

### Prompts
- `greeting`: Simple greeting with name parameter
- `personalized-message`: Complex personalized messages with age and interests
- `conversation-starter`: Multi-message conversation flows
- `map-arguments`: Demonstrates map-based argument handling
- `single-message`: Single message responses
- `string-list`: List-based responses

### Completions
- Username completion for `user-status://` URIs
- Name completion for `personalized-message` prompts
- Country name completion for travel prompts

## Configuration

### Server Configuration (`application.properties`)

```properties
# Server identification
spring.ai.mcp.server.name=my-weather-server
spring.ai.mcp.server.version=0.0.1

# Disable banner for STDIO transport compatibility
spring.main.banner-mode=off

# Logging configuration
logging.file.name=./model-context-protocol/mcp-annotations/mcp-annotations-server/target/mcp-annotations-server.log

# Uncomment for STDIO transport
# spring.ai.mcp.server.stdio=true
# spring.main.web-application-type=none

# Uncomment for different protocols
# spring.ai.mcp.server.protocol=STREAMABLE
# spring.ai.mcp.server.protocol=STATELESS
```

### Client Configuration (`application.properties`)

```properties
spring.application.name=mcp
spring.main.web-application-type=none

# MCP client connection
spring.ai.mcp.client.sse.connections.server1.url=http://localhost:8080

# Logging configuration
logging.level.io.modelcontextprotocol.client=WARN
logging.level.io.modelcontextprotocol.spec=WARN
```

## Testing the Server

You can test the server using the included test clients:

### SSE Client Test
```java
// Located in src/test/java/.../client/ClientSse.java
var transport = HttpClientSseClientTransport.builder("http://localhost:8080").build();
var client = McpClient.sync(transport).build();
```

### STDIO Client Test
```java
// Located in src/test/java/.../client/ClientStdio.java
var stdioParams = ServerParameters.builder("java")
    .args("-jar", "target/mcp-annotations-server-0.0.1-SNAPSHOT.jar")
    .build();
var transport = new StdioClientTransport(stdioParams);
var client = McpClient.sync(transport).build();
```

## Advanced Features

### Mixed Annotation Styles
The server demonstrates both MCP-specific annotations (`@McpTool`) and Spring AI annotations (`@Tool`), showing how they can coexist in the same application.

### URI Template Variables
Resources support complex URI templates with multiple variables that are automatically extracted and passed as method parameters.

### Return Type Flexibility
Methods can return various types:
- `ReadResourceResult` for complete control
- `List<ResourceContents>` for multiple content items
- `ResourceContents` for single content items
- `String` for simple text responses
- `List<String>` for multiple text items

### Custom MIME Types
Resources can specify custom MIME types for different content types, including binary data handling.

### Server Exchange Integration
Methods can access `McpSyncServerExchange` for advanced server operations like logging notifications and progress tracking.

## Sample Data

The server includes sample user profiles for testing:
- **john**: John Smith (New York, age 32)
- **jane**: Jane Doe (London, age 28)
- **bob**: Bob Johnson (Tokyo, age 45)
- **alice**: Alice Brown (Sydney, age 36)

## Related Projects

- **[Sampling Examples](../sampling)**: MCP Sampling with multiple LLM providers
- **[Weather Server](../weather)**: Simple weather MCP server
- **[SQLite Server](../sqlite)**: Database access MCP server
- **[Filesystem Server](../filesystem)**: File system operations MCP server

## Additional Resources

* [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
* [Spring AI MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
* [Spring AI MCP Client Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-client-boot-starter-docs.html)
* [Spring AI Annotations](https://docs.spring.io/spring-ai/reference/1.1-SNAPSHOT/api/mcp/mcp-annotations-overview.html)
* [Java MCP Annotations](https://github.com/spring-ai-community/mcp-annotations)
* [Model Context Protocol Specification](https://modelcontextprotocol.github.io/specification/)
