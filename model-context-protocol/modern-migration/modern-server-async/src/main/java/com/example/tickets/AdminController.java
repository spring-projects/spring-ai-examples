package com.example.tickets;

import java.util.Map;

import io.modelcontextprotocol.modern.McpSchema.CallToolResult;
import io.modelcontextprotocol.modern.McpSchema.TextContent;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpAsyncResponse;
import io.modelcontextprotocol.modern.server.feature.McpAsyncTool;
import reactor.core.publisher.Mono;

import org.springframework.ai.mcp.modern.InMemoryAsyncToolRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adds a tool at runtime through the asynchronous in-memory repository, which broadcasts
 * {@code notifications/tools/list_changed} to every open {@code subscriptions/listen}
 * stream. The tool is the SDK's {@link McpAsyncTool}: a definition plus a handler that
 * returns a {@code Mono}.
 */
@RestController
public class AdminController {

	private final ObjectProvider<InMemoryAsyncToolRepository> tools;

	public AdminController(ObjectProvider<InMemoryAsyncToolRepository> tools) {
		this.tools = tools;
	}

	@PostMapping("/admin/tools/{name}")
	public Mono<String> addEchoTool(@PathVariable String name) {
		String toolName = "echo_" + name;
		Tool tool = Tool.builder(toolName, Map.of("type", "object", "properties", Map.of("text", Map.of("type", "string"))))
			.description("Echoes its 'text' argument, registered at runtime")
			.build();
		this.tools.getObject().add(McpAsyncTool.of(tool, (ctx, request) -> Mono.just(McpAsyncResponse.result(CallToolResult.builder()
			.addContent(TextContent.builder(toolName + ": " + (request.arguments() != null ? request.arguments().get("text") : null)).build())
			.build()))));
		return Mono.just("Registered " + toolName + "\n");
	}

}
