package org.springframework.ai.mcp.samples.brave;

import java.util.Scanner;

import org.springframework.ai.autoconfigure.mcp.client.stdio.McpClientDefinitions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner chatbot(ChatClient.Builder chatClientBuilder, McpClientDefinitions mcpClientDefinitions) {

		return args -> {

			var mcpClients = mcpClientDefinitions.toMcpSyncClients();

			var chatClient = chatClientBuilder
					.defaultSystem("You are useful assistant, expert in AI and Java.")
					.defaultTools(McpToolUtils.getToolCallbacks(mcpClients))
					.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
					.build();

			// Start the chat loop
			System.out.println("\nI am your AI assistant.\n");
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					System.out.print("\nUSER: ");
					System.out.println("\nASSISTANT: " +
							chatClient.prompt(scanner.nextLine()) // Get the user input
									.call()
									.content());
				}
			}

		};
	}
}