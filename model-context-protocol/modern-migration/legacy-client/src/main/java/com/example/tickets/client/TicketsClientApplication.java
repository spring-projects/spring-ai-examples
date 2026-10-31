package com.example.tickets.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Drives the STATELESS and the STREAMABLE tickets servers with the Spring AI MCP client. The
 * interactive tools only exist on the STREAMABLE server, so they are called when present.
 */
@SpringBootApplication
public class TicketsClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketsClientApplication.class, args).close();
	}

	@Bean
	CommandLineRunner demo(List<McpSyncClient> clients, @Value("${demo.admin-url:http://localhost:8080}") String adminUrl) {
		return args -> {
			for (McpSyncClient client : clients) {
				System.out.println(">>> Connected to " + client.getServerInfo().name());
				Set<String> tools = toolNames(client);
				System.out.println("Tools: " + tools);

				System.out.println("open_ticket -> " + client.callTool(CallToolRequest.builder("open_ticket")
					.arguments(Map.of("title", "Printer offline"))
					.build()).content());
				System.out.println("resource tickets://all -> "
						+ client.readResource(ReadResourceRequest.builder("tickets://all").build()).contents());
				System.out.println("prompt daily-summary -> " + client
					.getPrompt(GetPromptRequest.builder("daily-summary").arguments(Map.of("owner", "Ana")).build())
					.messages());

				if (tools.contains("delete_ticket")) {
					// Elicitation and sampling are answered by the handlers in ClientHandlers.
					System.out.println("delete_ticket (elicitation) -> " + client.callTool(CallToolRequest.builder("delete_ticket")
						.arguments(Map.of("id", "t-1"))
						.progressToken("del-1")
						.build()).content());
					System.out.println("summarize_tickets (sampling) -> " + client
						.callTool(CallToolRequest.builder("summarize_tickets").arguments(Map.of()).build())
						.content());

					// Runtime change: the server adds a tool and notifies the session.
					HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(adminUrl + "/admin/tools/hello"))
						.POST(HttpRequest.BodyPublishers.noBody())
						.build(), HttpResponse.BodyHandlers.ofString());
					Thread.sleep(1000); // let notifications/tools/list_changed arrive
					System.out.println("Tools after runtime add: " + toolNames(client));
				}
				else {
					System.out.println("(stateless server: no interactive tools, no notifications)");
				}
			}
		};
	}

	private static Set<String> toolNames(McpSyncClient client) {
		return client.listTools().tools().stream().map(Tool::name).collect(Collectors.toCollection(TreeSet::new));
	}

}
