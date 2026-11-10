package com.example.tickets.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import com.example.tickets.client.ModernMcpDriver.McpError;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Walks the modern tickets servers. Every server gets the common walk (discover, list, call,
 * resources, prompt, and the ticket id completion where the server has the template); the
 * scenarios after it run only when the server offers what they need: the asynchronous tool
 * of {@code modern-server-async}, the multi round-trip tools of
 * {@code modern-server-from-streamable} and {@code modern-server-async}, and the admin tools
 * and ticket resources of {@code modern-server-repositories}.
 */
@SpringBootApplication
public class ModernTicketsClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModernTicketsClientApplication.class, args).close();
	}

	@Bean
	CommandLineRunner demo(@Value("${demo.mcp-endpoint:http://localhost:8080/mcp}") String endpoint,
			@Value("${demo.admin-url:http://localhost:8080}") String adminUrl) {
		return args -> {
			ModernMcpDriver mcp = new ModernMcpDriver(endpoint);
			List<String> toolNames = common(mcp);
			ModernMcpDriver admin = mcp.withHeader("X-Role", "admin");
			if (toolNames.contains("count_open_tickets")) {
				asynchronous(mcp);
			}
			if (toolNames.contains("delete_ticket")) {
				interactive(mcp, adminUrl);
			}
			else if (ModernMcpDriver.names(admin.listTools(), "tools").size() > toolNames.size()) {
				repositories(mcp, admin);
			}
			else {
				System.out.println("(no interactive or admin tools: this is the stateless migration)");
			}
		};
	}

	/** What every modern server answers, whatever it was migrated from. */
	private static List<String> common(ModernMcpDriver mcp) throws Exception {
		System.out.println(">>> server/discover -> " + mcp.discover());
		Map<String, Object> tools = mcp.listTools();
		List<String> toolNames = ModernMcpDriver.names(tools, "tools");
		System.out.println(">>> tools/list (ttlMs=" + tools.get("ttlMs") + ", cacheScope=" + tools.get("cacheScope") + ") -> " + toolNames);
		System.out.println(">>> open_ticket -> " + mcp.callTool("open_ticket", Map.of("title", "Printer offline"), ModernMcpDriver.autoAnswer(), n -> { }).get("content"));

		Map<String, Object> resources = mcp.listResources(null);
		List<String> uris = ModernMcpDriver.values(resources, "resources", "uri");
		System.out.println(">>> resources/list -> " + uris + (resources.get("nextCursor") != null ? ", nextCursor=" + resources.get("nextCursor") : ""));
		Map<String, Object> resource = mcp.readResource(uris.get(0));
		System.out.println(">>> resources/read " + uris.get(0) + " (ttlMs=" + resource.get("ttlMs") + ", cacheScope=" + resource.get("cacheScope") + ") -> " + resource.get("contents"));
		System.out.println(">>> prompts/get daily-summary -> " + mcp.getPrompt("daily-summary", Map.of("owner", "Ana")).get("messages"));
		if (ModernMcpDriver.values(mcp.listResourceTemplates(), "resourceTemplates", "uriTemplate").contains("tickets://{id}")) {
			Map<String, Object> completion = mcp.complete(Map.of("type", "ref/resource", "uri", "tickets://{id}"), "id", "t-");
			System.out.println(">>> completion/complete tickets://{id} id=\"t-\" -> " + ((Map<?, ?>) completion.get("completion")).get("values"));
		}
		return toolNames;
	}

	/** modern-server-async: the same walk, answered by Mono and CompletableFuture methods. */
	private static void asynchronous(ModernMcpDriver mcp) throws Exception {
		System.out.println(">>> count_open_tickets (a CompletableFuture on the server) -> "
				+ mcp.callTool("count_open_tickets", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }).get("content"));
	}

	/** modern-server-from-streamable: elicitation and sampling as multi round-trip requests, runtime tools. */
	private static void interactive(ModernMcpDriver mcp, String adminUrl) throws Exception {
		System.out.println(">>> delete_ticket (MRTR elicitation, with progress)");
		Map<String, Object> deleted = mcp.callTool("delete_ticket", Map.of("id", "t-1"), ModernMcpDriver.autoAnswer(),
				n -> System.out.println("    notification " + n.get("method") + " " + n.get("params")));
		System.out.println("    -> " + deleted.get("content"));

		System.out.println(">>> delete_ticket from a client that declares no capabilities");
		expectError(() -> mcp.withCapabilities(Map.of()).callTool("delete_ticket", Map.of("id", "t-2"), ModernMcpDriver.autoAnswer(), n -> { }));

		System.out.println(">>> summarize_tickets (MRTR sampling)");
		System.out.println("    -> " + mcp.callTool("summarize_tickets", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }).get("content"));

		System.out.println(">>> summarize_tickets from a client that declares only form elicitation");
		expectError(() -> mcp.withCapabilities(Map.of("elicitation", Map.of("form", Map.of())))
			.callTool("summarize_tickets", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }));

		System.out.println(">>> subscriptions/listen, then add a tool at runtime");
		AutoCloseable listener = mcp.listen(Map.of("toolsListChanged", true), ModernTicketsClientApplication::printEvent);
		Thread.sleep(500);
		HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(adminUrl + "/admin/tools/hello"))
			.POST(HttpRequest.BodyPublishers.noBody())
			.build(), HttpResponse.BodyHandlers.ofString());
		Thread.sleep(1000);
		listener.close();
		System.out.println(">>> tools/list after runtime add -> " + ModernMcpDriver.names(mcp.listTools(), "tools"));
	}

	/** modern-server-repositories: a filtering tool repository and a domain-backed resource repository. */
	@SuppressWarnings("unchecked")
	private static void repositories(ModernMcpDriver mcp, ModernMcpDriver admin) throws Exception {
		System.out.println(">>> definition attributes");
		for (Object tool : (List<?>) mcp.listTools().get("tools")) {
			Map<String, Object> definition = (Map<String, Object>) tool;
			if (definition.containsKey("icons")) {
				System.out.println("    tool " + definition.get("name") + " icons -> " + definition.get("icons"));
			}
		}
		for (Object prompt : (List<?>) mcp.listPrompts().get("prompts")) {
			Map<String, Object> definition = (Map<String, Object>) prompt;
			System.out.println("    prompt " + definition.get("name") + " arguments -> " + definition.get("arguments"));
		}

		System.out.println(">>> tools/list with X-Role: admin -> " + ModernMcpDriver.names(admin.listTools(), "tools"));
		System.out.println(">>> admin_stats without the header");
		expectError(() -> mcp.callTool("admin_stats", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }));
		System.out.println(">>> admin_stats -> " + admin.callTool("admin_stats", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }).get("content"));

		System.out.println(">>> resources/list, following nextCursor");
		String cursor = null;
		do {
			Map<String, Object> page = mcp.listResources(cursor);
			System.out.println("    page -> " + ModernMcpDriver.values(page, "resources", "uri"));
			cursor = (String) page.get("nextCursor");
		}
		while (cursor != null);

		System.out.println(">>> subscriptions/listen to tickets://t-1 and resource list changes, then change tickets");
		AutoCloseable listener = mcp.listen(Map.of("resourcesListChanged", true, "resourceSubscriptions", List.of("tickets://t-1")),
				ModernTicketsClientApplication::printEvent);
		Thread.sleep(500);
		mcp.callTool("resolve_ticket", Map.of("id", "t-1"), ModernMcpDriver.autoAnswer(), n -> { });
		mcp.callTool("open_ticket", Map.of("title", "Plan the next sprint"), ModernMcpDriver.autoAnswer(), n -> { });
		Thread.sleep(1000);
		listener.close();

		System.out.println(">>> admin_purge_resolved from a client that declares no capabilities");
		expectError(() -> admin.withCapabilities(Map.of()).callTool("admin_purge_resolved", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }));
		System.out.println(">>> admin_purge_resolved (MRTR elicitation on a programmatic tool)");
		System.out.println("    -> " + admin.callTool("admin_purge_resolved", Map.of(), ModernMcpDriver.autoAnswer(), n -> { }).get("content"));
	}

	private static void printEvent(Map<String, Object> event) {
		System.out.println("    listen event: " + (event.containsKey("method") ? event.get("method") + " " + event.get("params") : event.get("result")));
	}

	private static void expectError(Call call) throws Exception {
		try {
			System.out.println("    unexpected success -> " + call.run());
		}
		catch (McpError error) {
			System.out.println("    -> error " + error.getMessage());
		}
	}

	@FunctionalInterface
	private interface Call {

		Object run() throws Exception;

	}

}
