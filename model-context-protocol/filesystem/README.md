# Spring AI Model Context Protocol Demo Application

A demo application showcasing the integration of Spring AI with File syste using the Model Context Protocol (MCP). 
This application enables natural language interactions with predefiend folders in your local files system.

It starts and connects to [Filesystem MCP-Server](https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem) with provided accsss to your `model-context-protocol/filesystem/target` folder

## Features

- Natural language querying and updateing files on your local file system
- Predefined question mode for automated database analysis
- Seamless integration with OpenAI's language models
- Built on Spring AI and Model Context Protocol

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- npx package manager
- Git
- OpenAI API key

## Installation

1. Install npx (Node Package eXecute):
   first make sure to install [npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
   and then run:
   ```bash
   npm install -g npx
   ```

2. Clone the repository:
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd model-context-protocol/filesystem
   ```

3. Set up your OpenAI API key:
   ```bash
   export OPENAI_API_KEY='your-api-key-here'
   ```

4. Build the demo:
   ```bash
   ./mvnw clean install
   ```

## Create a sample text file to explore

Create a sample `spring-ai-mcp-overview.txt` file under your `filesystem/target` directory manually or use the `create-text-file.sh` script


## Running the Application

### Predefined Questions
Runs through a set of preset questions:
```bash
./mvnw spring-boot:run
```

## Architecture Overview

Spring AI's integration with MCP follows a simple chain of components:

1. **MCP Client** provides the base communication layer with your filesystem
2. **Function Callbacks** expose filesystem operations as AI-callable functions
3. **Chat Client** connects these functions to the AI model

The bean definitions are described below, starting with the `ChatClient`

### Chat Client

```java
@Bean
@Profile("!chat")
public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                           List<McpFunctionCallback> functionCallbacks,
                                           ConfigurableApplicationContext context) {
    return args -> {
        var chatClient = chatClientBuilder.defaultFunctions(functionCallbacks)
                .build();
        // Run Predefined Questions
    };
}
```

The chat client setup is remarkably simple - it just needs the function callbacks that were automatically created from the MCP tools. Spring's dependency injection handles all the wiring, making the integration seamless.

Now let's look at the other bean definitions in detail...

### Function Callbacks

The application registers MCP tools with Spring AI using function callbacks:

```java
@Bean
public List<McpFunctionCallback> functionCallbacks(McpSyncClient mcpClient) {
    return mcpClient.listTools(null)
            .tools()
            .stream()
            .map(tool -> new McpFunctionCallback(mcpClient, tool))
            .toList();
}
```

#### Purpose

This bean is responsible for:
1. Discovering available MCP tools from the client
2. Converting each tool into a Spring AI function callback
3. Making these callbacks available for use with the ChatClient


#### How It Works

1. `mcpClient.listTools(null)` queries the MCP server for all available tools
   - The `null` parameter represents a pagination cursor
   - When null, returns the first page of results
   - A cursor string can be provided to get results after that position
2. `.tools()` extracts the tool list from the response
3. Each tool is transformed into a `McpFunctionCallback` using `.map()`
4. These callbacks are collected into an array using `.toList()`

#### Usage

The registered callbacks enable the ChatClient to:
- Access MCP tools during conversations
- Handle function calls requested by the AI model
- Execute tools against the MCP server (e.g., filesystem)


### MCP Client 

The application uses a synchronous MCP client to communicate with the Filesystem MCP Server running locally:

```java
@Bean(destroyMethod = "close")
public McpSyncClient mcpClient() {
    var stdioParams = ServerParameters.builder("npx")
            .args("-y", "@modelcontextprotocol/server-filesystem", getDbPath())
            .build();

    var mcpClient = McpClient.sync(new StdioServerTransport(stdioParams),
            Duration.ofSeconds(10), new ObjectMapper());

    var init = mcpClient.initialize();
    System.out.println("MCP Initialized: " + init);

    return mcpClient;
}
```

This configuration:
1. Creates a stdio-based transport layer that communicates with the `npx` MCP server
2. Specifies the location of folders to be used by the filesystem server.
3. Sets a 10-second timeout for operations
4. Uses Jackson for JSON serialization
5. Initializes the connection to the MCP server

The `destroyMethod = "close"` annotation ensures proper cleanup when the application shuts down.
