package com.example.tasks;

import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.beans.factory.ObjectProvider;
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

	// Resolved lazily: injecting McpSyncServer directly would create it while this controller
	// is being wired, before the @McpTool beans have been scanned, and the server would start
	// with an empty tool list.
	private final ObjectProvider<McpSyncServer> server;

	public AdminController(ObjectProvider<McpSyncServer> server) {
		this.server = server;
	}

	@PostMapping("/admin/tools/{name}")
	public String addEchoTool(@PathVariable String name) {
		String toolName = "echo_" + name;
		Tool tool = Tool.builder()
			.name(toolName)
			.description("Echoes its 'text' argument, registered at runtime")
			.inputSchema(Map.of("type", "object", "properties", Map.of("text", Map.of("type", "string"))))
			.build();
		this.server.getObject().addTool(McpServerFeatures.SyncToolSpecification.builder()
			.tool(tool)
			.callHandler((exchange, request) -> CallToolResult.builder()
				.addTextContent(toolName + ": " + request.arguments().get("text"))
				.build())
			.build());
		return "Registered " + toolName + "\n";
	}

}
