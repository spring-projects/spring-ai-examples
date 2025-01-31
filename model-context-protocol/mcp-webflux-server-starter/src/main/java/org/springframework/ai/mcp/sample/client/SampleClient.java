/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springframework.ai.mcp.sample.client;

import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ListPromptsResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;

/**
 * @author Christian Tzolov
 */

public class SampleClient {

	private final ClientMcpTransport transport;

	public SampleClient(ClientMcpTransport transport) {
		this.transport = transport;
	}

	public void run() {

		var client = McpClient.sync(this.transport).build();

		client.initialize();

		client.ping();

		// List and demonstrate tools
		ListToolsResult toolsList = client.listTools();
		System.out.println("Available Tools = " + toolsList);

		CallToolResult upperCaseResult = client
			.callTool(new CallToolRequest("toUpperCase", Map.of("input", "accountName")));
		System.out.println("Upper case Response = " + upperCaseResult);

		CallToolResult books = client.callTool(new CallToolRequest("getBooks", Map.of("title", "Spring Framework")));
		System.out.println("Books Response = " + books);

		// List and demonstrate resources
		var resourcesList = client.listResources();
		System.out.println("\nAvailable Resources = " + resourcesList);

		// Read the system info resource
		var systemInfo = client.readResource(new ReadResourceRequest("system://info"));
		System.out.println("System Info = " + systemInfo);

		// List and demonstrate prompts
		ListPromptsResult promptsList = client.listPrompts();
		System.out.println("\nAvailable Prompts = " + promptsList);

		// Try the greeting prompt
		var greetingResponse = client.getPrompt(new GetPromptRequest("greeting", Map.of("name", "Spring")));
		System.out.println("Greeting Response = " + greetingResponse);

		client.closeGracefully();

	}

}
