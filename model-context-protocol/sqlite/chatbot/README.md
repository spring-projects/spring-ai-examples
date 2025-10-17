# Spring AI Model Context Protocol - SQLite Chatbot

A demo application showcasing the integration of Spring AI with SQLite databases using the Model Context Protocol (MCP). This application enables natural language interactions with your SQLite database through a command-line interface.

It uses the [SQLite MCP-Server](https://pypi.org/project/mcp-server-sqlite/) to enable running SQL queries, analyzing business data, and automatically generating business insight memos.

The demo starts a simple chatbot where your can ask qustions about the data  stored in the database.

For example:
> Can you connect to my SQLite database and tell me what products are available, and their prices?

or perform some data aggreagation on the fly:

> What's the average price of all products in the database?

run annalysis 

> Can you analyze the price distribution and suggest any pricing optimizations?

or even create an new table: 

> Could you help me design and create a new table for storing customer orders?

## Features

- Natural language querying of SQLite databases
- Interactive chat mode for dynamic database interactions
- Seamless integration with OpenAI's language models
- Built on Spring AI and Model Context Protocol

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- uvx package manager
- Git
- OpenAI API key
- SQLite (optional, for database modifications)

## Installation

1. Install uvx (Universal Package Manager):
   ```bash
   # Follow installation instructions at:
   https://docs.astral.sh/uv/getting-started/installation/
   ```

2. Clone the repository:
   ```bash
   git clone https://github.com/spring-projects/spring-ai-examples.git
   cd model-context-protocol/sqlite/chatbot
   ```

3. Set up your OpenAI API key:
   ```bash
   export OPENAI_API_KEY='your-api-key-here'
   ```

## Sample SQLite database

SQLite database files are portable across operating systems.  This repository contains a sample database file named `test.db`.

It has a `PRODUCTS` table and was created using the script `create-database.sh`

## Running the Application

### Interactive Chat
Enables real-time conversation with your database:

```bash
./mvnw spring-boot:run
```

## Architecture Overview

### MCP Client Configuration

The application uses a synchronous MCP client to communicate with the SQLite database:

```java
@Bean(destroyMethod = "close")
public McpSyncClient mcpClient() {

    var stdioParams = ServerParameters.builder("uvx")
            .args("mcp-server-sqlite", "--db-path", getDbPath())
            .build();

   var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.createDefault()))
           .requestTimeout(Duration.ofSeconds(10)).build();

    var init = mcpClient.initialize();

    System.out.println("MCP Initialized: " + init);

    return mcpClient;
}
```

This configuration:
1. Creates a stdio-based transport layer that communicates with the `uvx` MCP server
2. Specifies SQLite as the backend database and its location
3. Sets a 10-second timeout for operations
4. Uses Jackson for JSON serialization
5. Initializes the connection to the MCP server

The `destroyMethod = "close"` annotation ensures proper cleanup when the application shuts down.

#### Usage

The registered callbacks enable the ChatClient to:
- Access MCP tools during conversations
- Handle function calls requested by the AI model
- Execute tools against the MCP server (e.g., SQLite database)


## Documentation references

You can find out more about this sample application following the this quickstart link to a specific verison in github.

Unfortunately, on December 10th 2024, the quickstart was changed from SQLite to be a weather retrieval example.

However, here is the [link](https://github.com/modelcontextprotocol/docs/blob/1024e03f83aa0b8badde9b50dfee4d2e4e7f9446/quickstart.mdx) to the docs before the change if you want to read up on some details.

For example, you may want to create other tables and install SQLite