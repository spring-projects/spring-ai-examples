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

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

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
						.progressToken("test-progress-token")
						.build();

				CallToolResult response = mcpClient.callTool(toolRequest);

				System.out.println("Tool response: " + response);
			}
		};
	}
}