package com.example.tickets.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.modern.McpSchema.PromptReference;
import io.modelcontextprotocol.modern.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.modern.McpSchema.ResourceReference;
import io.modelcontextprotocol.modern.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.modern.client.McpSyncClient;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.modern.client.SyncModernMcpConnection;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * The legacy tickets client after migration to the modern protocol, on the Spring AI MCP
 * client starter. The tools of the modern connection arrive as {@link ToolCallback}s from the
 * same {@link SyncMcpToolCallbackProvider} bean that serves legacy connections, and a
 * {@code ChatClient} would take that bean with {@code defaultToolCallbacks(provider)}. This
 * sample has no model, so it calls the callbacks itself.
 * <p>
 * Every server gets the common walk. The scenarios after it run only when the server offers
 * what they need: the interactive tools of {@code modern-server-from-streamable} and
 * {@code modern-server-async}, and the per-caller tool list of
 * {@code modern-server-repositories}.
 */
@SpringBootApplication
public class MigratedTicketsClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(MigratedTicketsClientApplication.class, args).close();
	}

	@Bean
	CommandLineRunner demo(SyncMcpToolCallbackProvider tools, List<SyncModernMcpConnection> connections, WireLog wire,
			ListenLog listen, @Value("${demo.admin-url:http://localhost:8080}") String adminUrl) {
		return args -> {
			// listen=true: the stream is opened in the background, for the application and
			// not for a caller. Waiting for it keeps the output below in order.
			System.out.println(">>> wait for the listen stream of the modern connection");
			if (!listen.awaitEstablished(Duration.ofSeconds(3))) {
				System.out.println("    -> not established: the server cannot be reached, it is tried again");
			}
			ToolCallback[] callbacks = common(tools, wire);
			for (SyncModernMcpConnection connection : connections) {
				if (has(callbacks, connection.info().name() + "_open_ticket")) {
					sdkClient(connection);
				}
				else {
					// The provider logged a warning and will not ask this server again
					// before the unreachable-cool-down time has passed.
					System.out.println(">>> the server of the connection '" + connection.info().name()
							+ "' cannot be reached: its tools are left out, the other tools stay usable");
				}
			}
			if (has(callbacks, "tickets_count_open_tickets")) {
				System.out.println(">>> tickets_count_open_tickets (a CompletableFuture on the server) -> "
						+ call(callbacks, "tickets_count_open_tickets", "{}"));
			}
			if (has(callbacks, "tickets_delete_ticket")) {
				interactive(tools, callbacks, adminUrl);
			}
			System.out.println(">>> resolve the tool callbacks for a caller with the admin role, and for one without");
			ToolCallback[] adminCallbacks = CurrentRole.callAs("admin", tools::getToolCallbacks);
			ToolCallback[] userCallbacks = tools.getToolCallbacks();
			if (adminCallbacks.length > userCallbacks.length) {
				roles(userCallbacks, adminCallbacks);
			}
			else {
				System.out.println("    -> the same tools: this server does not filter its tools by caller");
			}
		};
	}

	/** The tools of every connection, legacy and modern, from the one provider bean. */
	private static ToolCallback[] common(SyncMcpToolCallbackProvider tools, WireLog wire) {
		System.out.println(">>> resolve the tool callbacks, as a ChatClient does once for each request");
		ToolCallback[] callbacks = tools.getToolCallbacks();
		System.out.println("    -> " + names(callbacks));

		for (ToolCallback callback : callbacks) {
			// tickets_open_ticket on the modern connection, open_ticket on a legacy one
			if (callback.getToolDefinition().name().endsWith("open_ticket")) {
				System.out.println(">>> " + callback.getToolDefinition().name() + " -> "
						+ callback.call("{\"title\":\"Printer offline\"}"));
			}
		}

		System.out.println(">>> resolve the tool callbacks again");
		int listRequests = wire.count("tools/list");
		tools.getToolCallbacks();
		// 1 with the default of the sample servers, a private listing without a ttlMs. 0 when
		// the listing is kept: by the provider for a ttlMs with cacheScope=PUBLIC, or by a
		// client that outlives the request (client-scope=SHARED or CUSTOM) for a ttlMs.
		System.out.println("    -> " + (wire.count("tools/list") - listRequests) + " tools/list request(s) sent");
		return callbacks;
	}

	/**
	 * What is not a tool goes through the modern SDK client of a connection: there are no
	 * modern client beans, a connection hands out the client of the current request.
	 */
	private static void sdkClient(SyncModernMcpConnection connection) {
		McpSyncClient client = connection.client();
		System.out.println(">>> SDK client of the connection '" + connection.info().name() + "'");
		System.out.println("    server/discover -> " + client.discover().supportedVersions());

		String uri = client.resources().list().resources().get(0).uri();
		ReadResourceResult resource = client.resource(uri).read();
		System.out.println("    resources/read " + uri + " (ttlMs=" + resource.ttlMs() + ", cacheScope="
				+ resource.cacheScope() + ") -> " + resource.contents());
		System.out.println("    prompts/get daily-summary -> "
				+ client.prompt("daily-summary").arguments(Map.of("owner", "Ana")).get().messages());
		System.out.println("    completion/complete daily-summary owner=\"A\" -> "
				+ client.completion(new PromptReference("daily-summary"), "owner", "A").complete().completion().values());
		List<String> templates = client.resourceTemplates()
			.list()
			.resourceTemplates()
			.stream()
			.map(ResourceTemplate::uriTemplate)
			.toList();
		if (templates.contains("tickets://{id}")) {
			System.out.println("    completion/complete tickets://{id} id=\"t-\" -> " + client
				.completion(new ResourceReference("tickets://{id}"), "id", "t-")
				.complete()
				.completion()
				.values());
		}
	}

	/**
	 * modern-server-from-streamable and modern-server-async: elicitation and progress through
	 * the annotated methods of ClientHandlers, and a runtime tool addition that the listen
	 * stream of the connection reports.
	 */
	private static void interactive(SyncMcpToolCallbackProvider tools, ToolCallback[] callbacks, String adminUrl)
			throws Exception {
		System.out.println(">>> tickets_delete_ticket (@McpElicitation answers, the SDK client retries the call)");
		// The tool context of the call reaches the handler method, which then knows the user.
		ToolContext toolContext = new ToolContext(Map.of("user", "ana"));
		System.out.println("    -> " + callback(callbacks, "tickets_delete_ticket").call("{\"id\":\"t-1\"}", toolContext));

		System.out.println(">>> add a tool at runtime: the listen stream reports it to @McpToolListChanged");
		HttpClient.newHttpClient()
			.send(HttpRequest.newBuilder(URI.create(adminUrl + "/admin/tools/hello"))
				.POST(HttpRequest.BodyPublishers.noBody())
				.build(), HttpResponse.BodyHandlers.ofString());
		Thread.sleep(1500);
		System.out.println(">>> tool callbacks after the runtime add -> " + names(tools.getToolCallbacks()));
	}

	/**
	 * modern-server-repositories: the server shows the admin tools only to callers that send
	 * {@code X-Role: admin}. Each resolution of the tools creates a client for its caller.
	 */
	private static void roles(ToolCallback[] callbacks, ToolCallback[] adminCallbacks) {
		System.out.println("    -> admin:   " + names(adminCallbacks));
		System.out.println("    -> no role: " + names(callbacks));

		// The role is no longer set on this thread: the callbacks kept the caller they were
		// resolved for, which is what makes them usable on the thread of a streaming response.
		System.out.println(">>> tickets_admin_stats, called after the admin request has ended -> "
				+ call(adminCallbacks, "tickets_admin_stats", "{}"));
		System.out.println(">>> tickets_resolve_ticket -> " + call(callbacks, "tickets_resolve_ticket", "{\"id\":\"t-1\"}"));
		System.out.println(">>> tickets_admin_purge_resolved (elicitation on a programmatic tool, @McpElicitation)");
		System.out.println("    -> " + call(adminCallbacks, "tickets_admin_purge_resolved", "{}"));
	}

	private static String call(ToolCallback[] callbacks, String name, String arguments) {
		return callback(callbacks, name).call(arguments);
	}

	private static ToolCallback callback(ToolCallback[] callbacks, String name) {
		return Arrays.stream(callbacks)
			.filter(callback -> callback.getToolDefinition().name().equals(name))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No tool named " + name));
	}

	private static boolean has(ToolCallback[] callbacks, String name) {
		return names(callbacks).contains(name);
	}

	private static List<String> names(ToolCallback[] callbacks) {
		return Arrays.stream(callbacks).map(callback -> callback.getToolDefinition().name()).sorted().toList();
	}

}
