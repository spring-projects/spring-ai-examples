#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

# Define the file path in the script's directory
FILE_PATH="$SCRIPT_DIR/target/spring-ai-mcp-overview.txt"

echo "Creating a text file in $FILE_PATH..."

# Create the file
cat << 'EOF' > "$FILE_PATH"

Model Context Protocol (MCP) Java SDK

A Java implementation of the Model Context Protocol specification, providing both synchronous and 
asynchronous clients for MCP server interactions.

Overview: This SDK implements the Model Context Protocol, enabling seamless integration with AI models and tools through 
a standardized interface. It supports both synchronous and asynchronous communication patterns, making it suitable for 
various use cases and integration scenarios.

Features

 Synchronous and Asynchronous client implementations
 Standard MCP operations support:
	 Tool discovery and execution
	 Resource management and templates
	 Prompt handling and management
	 Resource subscription system
	 Server initialization and ping
 Stdio-based server transport
 Reactive programming support using Project Reactor

EOF

echo "The file has been created in $FILE_PATH."
