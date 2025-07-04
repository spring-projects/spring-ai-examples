package io.spring.examples;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ChatClient openAIChatClient(OpenAiChatModel chatModel) {
		return ChatClient.create(chatModel);
	}

	@Bean
	public ChatClient anthropicChatClient(AnthropicChatModel chatModel) {
		return ChatClient.create(chatModel);
	}

	@Bean
	CommandLineRunner cli(@Qualifier("openAIChatClient") ChatClient openAiChatClient,
						  @Qualifier("anthropicChatClient") ChatClient anthropicChatClient) {
		return args -> {
			var scanner = new Scanner(System.in);
			ChatClient chat;

			// Model selection
			while (true) {
				System.out.println("\nSelect your AI model:");
				System.out.println("1. OpenAI");
				System.out.println("2. Anthropic");
				System.out.print("Enter your choice (1 or 2): ");

				String choice = scanner.nextLine().trim();

				if (choice.equals("1")) {
					chat = openAiChatClient;
					System.out.println("Using OpenAI model");
					break;
				} else if (choice.equals("2")) {
					chat = anthropicChatClient;
					System.out.println("Using Anthropic model");
					break;
				} else {
					System.out.println("Invalid choice. Please enter 1 or 2.");
				}
			}

			// Main chat loop
			System.out.println("\nLet's chat! (Type 'exit' to quit)");
			while (true) {
				System.out.print("\nUSER: ");
				String input = scanner.nextLine();

				if (input.equalsIgnoreCase("exit")) {
					System.out.println("Goodbye!");
					break;
				}

				try {
					String response = chat.prompt(input).call().content();
					System.out.println("ASSISTANT: " + response);
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
					System.out.println("Please try again.");
				}
			}

			scanner.close();
		};
	}

}
