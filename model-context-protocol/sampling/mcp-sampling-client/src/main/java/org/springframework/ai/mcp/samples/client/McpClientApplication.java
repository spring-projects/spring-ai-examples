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
import java.util.stream.Collectors;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.customizer.McpSyncClientCustomizer;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner predefinedQuestions(OpenAiChatModel openAiChatModel,
			ConfigurableApplicationContext context, List<McpSyncClient> mcpClients) {

		return args -> {

			var mcpToolProvider = new SyncMcpToolCallbackProvider(mcpClients);

			ChatClient chatClient = ChatClient.builder(openAiChatModel).defaultTools(mcpToolProvider).build();

			String userQuestion = """
					What is the weather in Amsterdam right now?
					Please incorporate all createive responses from all LLM providers.
					After the other providers add a poem that synthesizes the the poems from all the other providers.
					""";

			System.out.println("> USER: " + userQuestion);
			System.out.println("> ASSISTANT: " + chatClient.prompt(userQuestion).call().content());

			context.close();
		};
	}

	@Bean
	McpSyncClientCustomizer samplingCustomizer(Map<String, ChatClient> chatClients) {

		return (name, mcpClientSpec) -> {
			mcpClientSpec.sampling(llmRequest -> {
				var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
				String modelHint = llmRequest.modelPreferences().hints().get(0).name();

				ChatClient hintedChatClient = chatClients.entrySet().stream()
						.filter(e -> e.getKey().contains(modelHint)).findFirst()
						.orElseThrow().getValue();

				String response = hintedChatClient.prompt()
						.system(llmRequest.systemPrompt())
						.user(userPrompt)
						.call()
						.content();

				return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
			});
			System.out.println("Customizing " + name);
		};
	}

	@Bean
	public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {

		return chatModels.stream().collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
				model -> ChatClient.builder(model).build()));

	}
}