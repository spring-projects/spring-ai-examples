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

import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;
import org.springaicommunity.mcp.method.progress.SyncProgressSpecification;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;
import org.springaicommunity.mcp.spring.SyncMcpAnnotationProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.mcp.samples.client.customizers.AnnotationSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
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
	public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
			List<McpSyncClient> mcpClients) {

		return args -> {
			McpSyncClient mcpClient = mcpClients.get(0);

			// Call a tool that sends progress notifications
			CallToolRequest toolRequest = CallToolRequest.builder()
					.name("tool1")
					.arguments(Map.of("input", "test input"))
					.progressToken("test-progress-token")
					.build();

			CallToolResult response = mcpClient.callTool(toolRequest);

			System.out.println("Tool response: " + response);
		};
	}

	@Bean
	List<SyncLoggingSpecification> loggingSpecs(ClientMcpHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncLoggingSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncSamplingSpecification> samplingSpecs(ClientMcpHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncSamplingSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncElicitationSpecification> elicitationSpecs(ClientMcpHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncElicitationSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	List<SyncProgressSpecification> progressSpecs(ClientMcpHandlers clientMcpHandlers) {
		return SyncMcpAnnotationProvider.createSyncProgressSpecifications(List.of(clientMcpHandlers));
	}

	@Bean
	McpSyncClientCustomizer annotationMcpSyncClientCustomizer(List<SyncLoggingSpecification> loggingSpecs,
			List<SyncSamplingSpecification> samplingSpecs, List<SyncElicitationSpecification> elicitationSpecs,
			List<SyncProgressSpecification> progressSpecs) {
		return new AnnotationSyncClientCustomizer(samplingSpecs, loggingSpecs, elicitationSpecs, progressSpecs);
	}
}