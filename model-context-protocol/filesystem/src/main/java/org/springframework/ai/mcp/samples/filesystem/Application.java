package org.springframework.ai.mcp.samples.filesystem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.McpJsonMapper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Cross-platform Spring AI MCP Filesystem Demo Application.
 *
 * <p>This application demonstrates integration with the MCP Filesystem server and works
 * on both Windows and Linux/Mac platforms with automatic OS detection.
 *
 * <h2>Configuration Approaches</h2>
 *
 * <h3>Option 1: Programmatic Configuration (Default)</h3>
 * If no JSON configuration is specified in application.properties, the {@link #mcpClient()}
 * bean creates the MCP client programmatically with automatic OS detection:
 * <ul>
 *   <li><b>Windows:</b> Uses {@code cmd.exe /c npx} wrapper (required for npx to execute)</li>
 *   <li><b>Linux/Mac:</b> Uses {@code npx} directly</li>
 * </ul>
 *
 * <h3>Option 2: JSON Configuration (Auto-Config)</h3>
 * Enable in application.properties:
 * <pre>
 * # For Windows:
 * spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config-windows.json
 *
 * # For Linux/Mac:
 * spring.ai.mcp.client.stdio.servers-configuration=classpath:/mcp-servers-config-linux.json
 * </pre>
 *
 * When JSON configuration is enabled, Spring Boot auto-configuration creates the MCP client,
 * and the {@link #mcpClient()} bean is skipped via {@code @ConditionalOnMissingBean}.
 *
 * <h2>Why Windows Requires cmd.exe Wrapper</h2>
 *
 * On Windows, {@code npx} is not a native executable but a batch file. Java's
 * {@code ProcessBuilder} (used by {@code StdioClientTransport}) cannot execute batch files
 * directly and requires the {@code cmd.exe /c} wrapper to spawn such processes.
 *
 * @see <a href="https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem">
 *      MCP Filesystem Server</a>
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * Runs predefined questions against the MCP filesystem server.
	 *
	 * <p>This bean works with both programmatic and JSON-based client configurations:
	 * <ul>
	 *   <li>Programmatic: Single {@code McpSyncClient} bean</li>
	 *   <li>JSON Config: List of {@code McpSyncClient} beans from auto-configuration</li>
	 * </ul>
	 */
	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
			@Autowired(required = false) List<McpSyncClient> mcpSyncClients,
			@Autowired(required = false) McpSyncClient mcpClient,
			ConfigurableApplicationContext context) {

		return args -> {
			// Determine which client(s) to use: auto-configured list or programmatic single
			List<McpSyncClient> clients = (mcpSyncClients != null && !mcpSyncClients.isEmpty())
					? mcpSyncClients
					: (mcpClient != null ? List.of(mcpClient) : List.of());

			if (clients.isEmpty()) {
				System.err.println("No MCP clients available. Check your configuration.");
				context.close();
				return;
			}

			var chatClient = chatClientBuilder
					.defaultToolCallbacks(new SyncMcpToolCallbackProvider(clients))
					.build();

			System.out.println("Running predefined questions with AI model responses:\n");

			// Question 1
			String question1 = "Can you explain the content of the target/spring-ai-mcp-overview.txt file?";
			System.out.println("QUESTION: " + question1);
			System.out.println("ASSISTANT: " + chatClient.prompt(question1).call().content());

			// Question 2
			String question2 = "Please summarize the content of the target/spring-ai-mcp-overview.txt file and store it in a new target/summary.md as Markdown format?";
			System.out.println("\nQUESTION: " + question2);
			System.out.println("ASSISTANT: " +
					chatClient.prompt(question2).call().content());

			context.close();
		};
	}

	/**
	 * Creates an MCP client with automatic OS detection for cross-platform compatibility.
	 *
	 * <p>This bean is only created when no MCP client is provided by auto-configuration
	 * (i.e., when JSON configuration is not enabled). When JSON config is present,
	 * Spring Boot auto-configuration takes precedence.
	 *
	 * <h3>Platform-Specific Behavior:</h3>
	 * <ul>
	 *   <li><b>Windows:</b> Command: {@code cmd.exe}, Args: {@code /c npx -y @modelcontextprotocol/server-filesystem target}</li>
	 *   <li><b>Linux/Mac:</b> Command: {@code npx}, Args: {@code -y @modelcontextprotocol/server-filesystem target}</li>
	 * </ul>
	 *
	 * <p><b>Path Handling:</b> Uses relative path {@code "target"} for cross-platform compatibility.
	 * The MCP server resolves this relative to the current working directory.
	 *
	 * @return configured MCP sync client
	 * @see <a href="https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem">
	 *      MCP Filesystem Server Documentation</a>
	 */
	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean(McpSyncClient.class)
	public McpSyncClient mcpClient() {

		ServerParameters stdioParams;

		if (isWindows()) {
			// Windows: npx is a batch file and requires cmd.exe to execute
			System.out.println("Detected Windows OS - using cmd.exe wrapper for npx");
			var winArgs = new ArrayList<>(Arrays.asList(
					"/c", "npx", "-y", "@modelcontextprotocol/server-filesystem", "target"));
			stdioParams = ServerParameters.builder("cmd.exe")
					.args(winArgs)
					.build();
		} else {
			// Linux/Mac: npx can be executed directly
			System.out.println("Detected Unix-like OS - using npx directly");
			stdioParams = ServerParameters.builder("npx")
					.args("-y", "@modelcontextprotocol/server-filesystem", "target")
					.build();
		}

		var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams, McpJsonMapper.createDefault()))
				.requestTimeout(Duration.ofSeconds(10))
				.build();

		var init = mcpClient.initialize();
		System.out.println("MCP Initialized: " + init);

		return mcpClient;
	}

	/**
	 * Detects if the current operating system is Windows.
	 *
	 * @return true if running on Windows, false otherwise
	 */
	private static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

}
