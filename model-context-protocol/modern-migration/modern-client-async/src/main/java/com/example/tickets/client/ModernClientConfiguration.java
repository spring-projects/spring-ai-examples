package com.example.tickets.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.modern.client.McpAsyncClient;
import io.modelcontextprotocol.modern.client.transport.McpHttpRequestCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.modern.client.AsyncModernMcpClientScope;
import org.springframework.ai.mcp.modern.client.AsyncModernMcpTransportContextProvider;
import org.springframework.ai.mcp.modern.client.McpModernToolFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * What a modern connection of the asynchronous client type needs next to the reactive
 * handlers of {@link ClientHandlers}: the caller of each request, the header that carries
 * it, the tools to hide, and a scope that keeps a client for each role.
 */
@Configuration(proxyBeanMethods = false)
class ModernClientConfiguration {

	/** The key of the caller's role in the transport context. */
	static final String ROLE = "role";

	private static final Logger logger = LoggerFactory.getLogger(ModernClientConfiguration.class);

	/**
	 * Supplies the caller of each request. An asynchronous client holds no caller, so the
	 * synchronous sample's client customizer has no counterpart: this bean is asked each
	 * time the tools are resolved, on the thread that resolves them. The tool listing goes
	 * out for that caller, and the tool callbacks of that resolution keep it, so a tool call
	 * that runs later, or on another thread, is still made for that caller. It is the only
	 * source of the caller: a transport context in the Reactor context of a chat call is not
	 * used. The listen stream belongs to the application and does not ask it.
	 */
	@Bean
	AsyncModernMcpTransportContextProvider callerProvider() {
		return connection -> {
			String role = CurrentRole.get();
			return role != null ? McpTransportContext.create(Map.of(ROLE, role)) : McpTransportContext.EMPTY;
		};
	}

	/**
	 * Used only when the connection sets {@code client-scope=CUSTOM}: keeps one client for
	 * each role, so that the tool listing a server marks as private is kept for that role
	 * for as long as the server allows. One scope serves all connections, so the key
	 * contains the name of the connection.
	 */
	@Bean
	AsyncModernMcpClientScope perRoleClientScope() {
		Map<String, McpAsyncClient> clients = new ConcurrentHashMap<>();
		return (connection, clientFactory) -> clients.computeIfAbsent(connection.name() + "/" + CurrentRole.get(),
				key -> {
					logger.info("CLIENT SCOPE: a new client for {}", key);
					return clientFactory.get();
				});
	}

	/** Sends the role of the caller to the server, as an access token would be sent. */
	@Bean
	McpHttpRequestCustomizer roleHeader() {
		return McpHttpRequestCustomizer.fromSync((request, headers, context) -> {
			Object role = context.get(ROLE);
			if (role != null) {
				headers.set("X-Role", role.toString());
			}
		});
	}

	@Bean
	WireLog wireLog() {
		return new WireLog();
	}

	@Bean
	ListenLog listenLog() {
		return new ListenLog();
	}

	/**
	 * Hides the tool that needs sampling, which the modern SDK client does not support; the
	 * server would refuse the call with {@code -32021}. The legacy {@code McpToolFilter} does
	 * not apply to modern connections.
	 */
	@Bean
	McpModernToolFilter modernToolFilter() {
		return (connection, tool) -> !tool.name().equals("summarize_tickets");
	}

}
