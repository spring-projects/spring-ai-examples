package com.example.tickets.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.modern.client.McpClient;
import io.modelcontextprotocol.modern.client.McpSyncClient;
import io.modelcontextprotocol.modern.client.transport.McpHttpRequestCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.ai.mcp.modern.client.McpModernToolFilter;
import org.springframework.ai.mcp.modern.client.SyncModernMcpClientScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * What a modern connection needs next to the annotated handlers of {@link ClientHandlers}:
 * the caller of each request, the header that carries it, the tools to hide, and a scope
 * that keeps a client for each role.
 */
@Configuration(proxyBeanMethods = false)
class ModernClientConfiguration {

	/** The key of the caller's role in the transport context. */
	static final String ROLE = "role";

	private static final Logger logger = LoggerFactory.getLogger(ModernClientConfiguration.class);

	/**
	 * Customizes each modern client. With the default {@code PER_REQUEST} client scope a
	 * client is created each time the tools are resolved, on the thread that resolves them,
	 * so the caller is read here once and bound to the client. A tool call that runs later,
	 * or on another thread, is still made for that caller. The client of the listen stream
	 * is built at startup, outside of any request, so it has no caller.
	 */
	@Bean
	McpClientCustomizer<McpClient.SyncBuilder> modernClientCustomizer() {
		return (connectionName, builder) -> {
			String role = CurrentRole.get();
			McpTransportContext caller = McpTransportContext.create(role != null ? Map.of(ROLE, role) : Map.of());
			builder.transportContextProvider(() -> caller);
		};
	}

	/**
	 * Used only when the connection sets {@code client-scope=CUSTOM}: keeps one client for
	 * each role, so that the tool listing a server marks as private is kept for that role
	 * for as long as the server allows. The factory builds a client as the customizer above
	 * configures it, on the thread of the caller. One scope serves all connections, so the
	 * key contains the name of the connection.
	 */
	@Bean
	SyncModernMcpClientScope perRoleClientScope() {
		Map<String, McpSyncClient> clients = new ConcurrentHashMap<>();
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
