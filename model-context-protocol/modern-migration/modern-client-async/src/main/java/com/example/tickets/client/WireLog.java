package com.example.tickets.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.modern.client.transport.McpHttpRequest;
import io.modelcontextprotocol.modern.client.transport.McpHttpRequestCustomizer;
import io.modelcontextprotocol.modern.client.transport.McpHttpRequestHeaders;
import reactor.core.publisher.Mono;

/**
 * Prints every request that a modern connection sends and counts the requests by MCP method,
 * so that the sample can show when a tool listing is served without asking the server.
 */
class WireLog implements McpHttpRequestCustomizer {

	private final Map<String, AtomicInteger> requests = new ConcurrentHashMap<>();

	@Override
	public Mono<Void> customize(McpHttpRequest request, McpHttpRequestHeaders headers, McpTransportContext context) {
		this.requests.computeIfAbsent(request.mcpMethod(), method -> new AtomicInteger()).incrementAndGet();
		Object role = context.get(ModernClientConfiguration.ROLE);
		System.out.println("    [http] " + request.mcpMethod() + (request.mcpName() != null ? " " + request.mcpName() : "")
				+ (role != null ? " (X-Role: " + role + ")" : ""));
		return Mono.empty();
	}

	/** The number of requests sent so far for an MCP method, such as {@code tools/list}. */
	int count(String mcpMethod) {
		AtomicInteger count = this.requests.get(mcpMethod);
		return count != null ? count.get() : 0;
	}

}
