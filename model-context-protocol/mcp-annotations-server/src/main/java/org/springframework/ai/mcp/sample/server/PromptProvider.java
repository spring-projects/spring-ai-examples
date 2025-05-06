/* 
* Copyright 2025 - 2025 the original author or authors.
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
package org.springframework.ai.mcp.sample.server;

import java.util.List;
import java.util.Map;

import com.logaritex.mcp.annotation.McpArg;
import com.logaritex.mcp.annotation.McpPrompt;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.springframework.stereotype.Service;

/**
 * @author Christian Tzolov
 */
@Service
public class PromptProvider {
    	/**
		 * A simple greeting prompt that takes a name parameter.
		 * @param name The name to greet
		 * @return A greeting message
		 */
		@McpPrompt(name = "greeting", description = "A simple greeting prompt")
		public GetPromptResult greetingPrompt(
				@McpArg(name = "name", description = "The name to greet", required = true) String name) {
			return new GetPromptResult("Greeting", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello, " + name + "! Welcome to the MCP system."))));
		}

		/**
		 * A more complex prompt that generates a personalized message.
		 * @param exchange The server exchange
		 * @param name The user's name
		 * @param age The user's age
		 * @param interests The user's interests
		 * @return A personalized message
		 */
		@McpPrompt(name = "personalized-message",
				description = "Generates a personalized message based on user information")
		public GetPromptResult personalizedMessage(McpSyncServerExchange exchange,
				@McpArg(name = "name", description = "The user's name", required = true) String name,
				@McpArg(name = "age", description = "The user's age", required = false) Integer age,
				@McpArg(name = "interests", description = "The user's interests", required = false) String interests) {

			exchange.loggingNotification(LoggingMessageNotification.builder()
				.level(LoggingLevel.INFO)	
				.data("personalized-message event").build());

			StringBuilder message = new StringBuilder();
			message.append("Hello, ").append(name).append("!\n\n");

			if (age != null) {
				message.append("At ").append(age).append(" years old, you have ");
				if (age < 30) {
					message.append("so much ahead of you.\n\n");
				}
				else if (age < 60) {
					message.append("gained valuable life experience.\n\n");
				}
				else {
					message.append("accumulated wisdom to share with others.\n\n");
				}
			}

			if (interests != null && !interests.isEmpty()) {
				message.append("Your interest in ")
					.append(interests)
					.append(" shows your curiosity and passion for learning.\n\n");
			}

			message
				.append("I'm here to assist you with any questions you might have about the Model Context Protocol.");

			return new GetPromptResult("Personalized Message",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
		}

		/**
		 * A prompt that returns a list of messages forming a conversation.
		 * @param request The prompt request
		 * @return A list of messages
		 */
		@McpPrompt(name = "conversation-starter", description = "Provides a conversation starter with the system")
		public List<PromptMessage> conversationStarter(GetPromptRequest request) {
			return List.of(
					new PromptMessage(Role.ASSISTANT,
							new TextContent("Hello! I'm the MCP assistant. How can I help you today?")),
					new PromptMessage(Role.USER,
							new TextContent("I'd like to learn more about the Model Context Protocol.")),
					new PromptMessage(Role.ASSISTANT, new TextContent(
							"Great choice! The Model Context Protocol (MCP) is a standardized way for servers "
									+ "to communicate with language models. It provides a structured approach for "
									+ "exchanging information, making requests, and handling responses. "
									+ "What specific aspect would you like to explore first?")));
		}

		/**
		 * A prompt that accepts arguments as a map.
		 * @param arguments The arguments map
		 * @return A prompt result
		 */
		@McpPrompt(name = "map-arguments", description = "Demonstrates using a map for arguments")
		public GetPromptResult mapArguments(Map<String, Object> arguments) {
			StringBuilder message = new StringBuilder("I received the following arguments:\n\n");

			if (arguments != null && !arguments.isEmpty()) {
				for (Map.Entry<String, Object> entry : arguments.entrySet()) {
					message.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
				}
			}
			else {
				message.append("No arguments were provided.");
			}

			return new GetPromptResult("Map Arguments Demo",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
		}

		/**
		 * A prompt that returns a single PromptMessage.
		 * @param name The user's name
		 * @return A single PromptMessage
		 */
		@McpPrompt(name = "single-message", description = "Demonstrates returning a single PromptMessage")
		public PromptMessage singleMessagePrompt(
				@McpArg(name = "name", description = "The user's name", required = true) String name) {
			return new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello, " + name + "! This is a single message response."));
		}

		/**
		 * A prompt that returns a list of strings.
		 * @param topic The topic to provide information about
		 * @return A list of strings with information about the topic
		 */
		@McpPrompt(name = "string-list", description = "Demonstrates returning a list of strings")
		public List<String> stringListPrompt(@McpArg(name = "topic",
				description = "The topic to provide information about", required = true) String topic) {
			if ("MCP".equalsIgnoreCase(topic)) {
				return List.of(
						"The Model Context Protocol (MCP) is a standardized way for servers to communicate with language models.",
						"It provides a structured approach for exchanging information, making requests, and handling responses.",
						"MCP allows servers to expose resources, tools, and prompts to clients in a consistent way.");
			}
			else {
				return List.of("I don't have specific information about " + topic + ".",
						"Please try a different topic or ask a more specific question.");
			}
		}

}
