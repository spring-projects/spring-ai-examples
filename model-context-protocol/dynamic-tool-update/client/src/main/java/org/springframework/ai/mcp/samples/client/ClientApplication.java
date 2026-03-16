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
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import io.modelcontextprotocol.client.McpClient;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class ClientApplication {

	private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

	CountDownLatch latch = new CountDownLatch(1);

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args).close();
	}

	public static record ToolDescription(String toolName, String toolDescription) {
		@Override
		public final String toString() {			
			return "Tool: " + toolName + " -> " + toolDescription;
		}
	}

	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {

		return args -> {

			logger.info("1) Get Available Tools: ");

			List<ToolDescription> toolDescriptions = chatClientBuilder.build()
					.prompt("What tools are available? Please list them and avoid any additional comments. Only JSON format.")
					.toolCallbacks(tools)
					.call()
					.entity(new ParameterizedTypeReference<List<ToolDescription>>() {
					});

		
			logger.info(toolDescriptions.stream().map(td -> td.toString()).collect(Collectors.joining("\n")));

			// signal the server to update the tools
			String signal = RestClient.builder().build().get()
					.uri("http://localhost:8080/updateTools").retrieve().body(String.class);
			logger.info("Server tool update response: " + signal);

			latch.await();

			logger.info("2) Get Available Tools: ");

			toolDescriptions = chatClientBuilder.build()
					.prompt("What tools are available? Please list them and avoid any additional comments. Only JSON format.")
					.toolCallbacks(tools)
					.call()
					.entity(new ParameterizedTypeReference<List<ToolDescription>>() {
					});
			logger.info(toolDescriptions.stream().map(td -> td.toString()).collect(Collectors.joining("\n")));
		};
	}

	@Bean
	McpClientCustomizer<McpClient.SyncSpec> customizeMcpClient() {
		return (name, mcpClientSpec) -> {
			mcpClientSpec.toolsChangeConsumer(tv -> {
				logger.info("\nMCP TOOLS CHANGE: " + tv);
				latch.countDown();
			});
		};
	}
}