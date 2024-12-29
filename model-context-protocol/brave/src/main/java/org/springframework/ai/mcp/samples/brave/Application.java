package org.springframework.ai.mcp.samples.brave;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.client.McpClient;
import org.springframework.ai.mcp.client.McpSyncClient;
import org.springframework.ai.mcp.client.transport.ServerParameters;
import org.springframework.ai.mcp.client.transport.StdioClientTransport;
import org.springframework.ai.mcp.spring.McpFunctionCallback;
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
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
			McpSyncClient mcpClient, ConfigurableApplicationContext context) {

		return args -> {

			var chatClient = chatClientBuilder
					.defaultFunctions(mcpClient.listTools(null)
							.tools()
							.stream()
							.map(tool -> new McpFunctionCallback(mcpClient, tool))
							.toArray(McpFunctionCallback[]::new))
					.build();

			String question = "Does Spring AI supports the Model Context Protocol? Please provide some references.";
			System.out.println("QUESTION: " + question);
			System.out.println("ASSISTANT: " + chatClient.prompt(question).call().content());

			context.close();
		};
	}

	@Bean(destroyMethod = "close")
	public McpSyncClient mcpClient() {

		// https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search
		var stdioParams = ServerParameters.builder("npx")
				.args("-y", "@modelcontextprotocol/server-brave-search")
				.addEnvVar("BRAVE_API_KEY", System.getenv("BRAVE_API_KEY"))
				.build();

		var mcpClient = McpClient.using(new StdioClientTransport(stdioParams)).sync();
		var init = mcpClient.initialize();
		System.out.println("MCP Initialized: " + init);
		return mcpClient;
	}

}