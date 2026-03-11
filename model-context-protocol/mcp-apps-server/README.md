Spring AI MCP Apps Server
===
This example highlight how to use metadata on an MCP server's tools and
resources to enable [MCP Apps](https://modelcontextprotocol.io/extensions/apps/overview).

Building and Running the Server
---
This example is built with Gradle. There is a Gradle wrapper available in the
project directory, so you can build an executable JAR file like this:

```
./gradlew build
```

Then you can run it like this:

```
java -jar build/libs/mcp-apps-server-0.0.1-SNAPSHOT.jar
```

Optionally, you can use the Spring Boot Gradle plugin to build and run the
app like this:

```
./gradlew bootRun
```

Or, if you want, run the application through your IDE using the facilities
of your favorite IDE.

Once the application starts, the MCP server will be listening on port
3001.

Using the MCP App Server
---
To see the MCP App in action, you'll need to configure the server in an
MCP client that supports MCP Apps. This includes MCP Jam and Claude
Desktop. Goose also supports MCP Apps, but does not (yet, as of version
1.27.2) support the ability for the app to update the context model, so
at least for now you cannot use Goose.

When you configure the MCP server, choose Streamable HTTP as the transport
protocol and set the URL to "http://localhost:3001/mcp".

Claude Desktop (at least as of version 1.1.5749) does not support Streamable
HTTP. But you can still use a Streamable HTTP MCP server by using the
mcp-remote (STDIO transport) MCP server to proxy to the Streamable HTTP
server. Configure it in the Claude Desktop configuration JSON like this:

```
"mcpServers": {
  "dice-tools": {
    "command": "npx",
    "args": [
      "-y",
      "mcp-remote",
      "http://localhost:3001/mcp"
    ]
  }
}
```

After configuring the MCP server in your MCP client, type "Roll the dice"
in the chat. If everything is configured correctly, after a moment you
should see a pair of dice, rolled to some values. Then ask "What was the
result?" to verify that the application was about to update the model
context. Then click the "Roll Dice" button to re-roll the dice and ask
again.
