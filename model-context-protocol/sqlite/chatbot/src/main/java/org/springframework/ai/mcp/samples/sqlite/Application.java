package org.springframework.ai.mcp.samples.sqlite;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner interactiveChat(ChatClient.Builder chatClientBuilder,
			List<McpSyncClient> mcpClients,
			ConfigurableApplicationContext context) {
		return args -> {

			var chatClient = chatClientBuilder
					.defaultTools(new SyncMcpToolCallbackProvider(mcpClients))
					.defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
					.build();

			var scanner = new Scanner(System.in);
			System.out.println("\nStarting interactive chat session. Type 'exit' to quit.");

			try {
				while (true) {
					System.out.print("\nUSER: ");
					String input = scanner.nextLine();

					if (input.equalsIgnoreCase("exit")) {
						System.out.println("Ending chat session.");
						break;
					}

					System.out.print("ASSISTANT: ");
					System.out.println(chatClient.prompt(input).call().content());
				}
			} finally {
				scanner.close();
				context.close();
			}

		};
	}


	@Bean(destroyMethod = "close")
	public McpSyncClient mcpClient() {

		var stdioParams = ServerParameters.builder("uvx")
				.args("mcp-server-sqlite", "--db-path",
						getDbPath())
				.build();

		var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
				.requestTimeout(Duration.ofSeconds(10)).build();

		var init = mcpClient.initialize();

		System.out.println("MCP Initialized: " + init);

		return mcpClient;

	}

	private static String getDbPath() {
		return Paths.get(System.getProperty("user.dir"), "test.db").toString();
	}

}