/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.mcp.samples.client;

import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest.CompleteArgument;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args).close();
	}

	@Bean
	public CommandLineRunner predefinedQuestions(
			List<McpSyncClient> mcpClients) {

		return args -> {

			for (McpSyncClient mcpClient : mcpClients) {
				System.out.println(">>> MCP Client: " + mcpClient.getClientInfo());

				// Call a tool that sends progress notifications
				CallToolRequest toolRequest = CallToolRequest.builder()
						.name("tool1")
						.arguments(Map.of("input", "test input"))
						.progressToken(666)
						.build();
				CallToolResult response = mcpClient.callTool(toolRequest);
				System.out.println("Tool response: " + response);

				CompleteResult nameCompletion = mcpClient.completeCompletion(
					new CompleteRequest(
						new PromptReference("personalized-message"), 
						new CompleteArgument("name", "J")));

				System.out.println("Name completions: " + nameCompletion.completion());

				String nameValue = nameCompletion.completion().values().get(3);

				try {
					GetPromptResult promptResponse = mcpClient
						.getPrompt(new GetPromptRequest("personalized-message", Map.of("name", nameValue)));

					System.out.println("Prompt response: " + promptResponse);
				} catch (Exception e) {
					System.err.println("Error getting prompt: " + e.getMessage());
				}

				nameCompletion = mcpClient.completeCompletion(
					new CompleteRequest(
						new ResourceReference("user-status://{username}"), 
						new CompleteArgument("username", "J")));

				System.out.println("Name completions: " + nameCompletion.completion());

				var resourceResponse = mcpClient.readResource(new ReadResourceRequest("user-status://" + nameCompletion.completion().values().get(0)));

				System.out.println("Resource response: " + resourceResponse);
				
			}
		};
	}
}