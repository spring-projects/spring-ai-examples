package org.springframework.ai.mcp.samples.brave;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import io.modelcontextprotocol.client.McpSyncClient;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.advisor.SessionAdvisors;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;
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
	public CommandLineRunner chatbot(ChatClient.Builder chatClientBuilder, List<McpSyncClient> mcpSyncClients) {

		return args -> {

			SessionService sessionService = DefaultSessionService.builder()
				.sessionRepository(InMemorySessionRepository.builder().build())
				.build();

			var chatClient = chatClientBuilder.defaultSystem(
					"You are useful assistant and can perform web searches Brave's search API to reply to your questions.")
				.defaultTools(SyncMcpToolCallbackProvider.builder().mcpClients(mcpSyncClients).build())
				.defaultAdvisors(SessionAdvisors.builder(sessionService)
					// Keep the last 10 turns once the conversation grows past 20 turns
					.compaction(new TurnCountTrigger(20), WindowCompactionStrategy.builder().lastTurns(10).build())
					.build())
				.build();

			var conversationId = UUID.randomUUID().toString();

			// Start the chat loop
			System.out.println("\nI am your AI assistant.\n");
			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					System.out.print("\nUSER: ");
					System.out.println("\nASSISTANT: " + chatClient.prompt(scanner.nextLine())
						.advisors(SessionAdvisors.session(conversationId))
						.call()
						.content());
				}
			}

		};
	}

}