package com.example.tickets;

import java.util.Map;

import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpSyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;

import org.springframework.ai.mcp.modern.InMemorySyncToolRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adds a tool at runtime. On the modern server there is no {@code McpSyncServer}; tools live
 * in a repository, and {@link InMemorySyncToolRepository#add} broadcasts
 * {@code notifications/tools/list_changed} to every open {@code subscriptions/listen} stream.
 * The tool itself is the SDK's {@link McpSyncTool}: a definition plus a handler.
 */
@RestController
public class AdminController {

	// Resolved lazily: injecting the repository directly would create it while this controller
	// is being wired, before the @McpTool beans have been scanned, and it would start empty.
	private final ObjectProvider<InMemorySyncToolRepository> tools;

	public AdminController(ObjectProvider<InMemorySyncToolRepository> tools) {
		this.tools = tools;
	}

	@PostMapping("/admin/tools/{name}")
	public String addEchoTool(@PathVariable String name) {
		String toolName = "echo_" + name;
		Tool tool = Tool.builder(toolName, Map.of("type", "object", "properties", Map.of("text", Map.of("type", "string"))))
			.description("Echoes its 'text' argument, registered at runtime")
			.build();
		this.tools.getObject().add(McpSyncTool.of(tool, (ctx, request) -> McpSyncResponse.result(CallToolResult.builder()
			.addContent(TextContent.builder(toolName + ": " + request.arguments().get("text")).build())
			.build())));
		return "Registered " + toolName + "\n";
	}

}
