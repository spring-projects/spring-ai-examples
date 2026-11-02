package com.example.tickets;

import java.util.Map;
import java.util.function.Supplier;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.function.SingletonSupplier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adds a tool at runtime. On the session-based server this goes through
 * {@link McpSyncServer#addTool}, which also sends {@code notifications/tools/list_changed}
 * to every connected session.
 */
@RestController
public class AdminController {

	// Resolved lazily, once: injecting McpSyncServer directly would create it while this
	// controller is being wired, before the @McpTool beans have been scanned, and the server
	// would start with an empty tool list. The ObjectProvider defers the lookup; the
	// SingletonSupplier caches its result.
	private final Supplier<McpSyncServer> server;

	public AdminController(ObjectProvider<McpSyncServer> server) {
		this.server = SingletonSupplier.of(server::getObject);
	}

	@PostMapping("/admin/tools/{name}")
	public String addEchoTool(@PathVariable String name) {
		String toolName = "echo_" + name;
		Tool tool = Tool.builder()
			.name(toolName)
			.description("Echoes its 'text' argument, registered at runtime")
			.inputSchema(Map.of("type", "object", "properties", Map.of("text", Map.of("type", "string"))))
			.build();
		this.server.get().addTool(McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler((exchange, request) -> CallToolResult.builder()
				.addTextContent(toolName + ": " + (request.arguments() != null ? request.arguments().get("text") : null))
				.build())
			.build());
		return "Registered " + toolName + "\n";
	}

}
