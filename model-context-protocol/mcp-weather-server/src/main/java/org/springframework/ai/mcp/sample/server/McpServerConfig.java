package org.springframework.ai.mcp.sample.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.ServerMcpTransport;

import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;

@Configuration
public class McpServerConfig {

	// STDIO transport
	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "stdio")
	public StdioServerTransport stdioServerTransport() {
		return new StdioServerTransport();
	}

	// SSE transport
	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	public WebFluxSseServerTransport sseServerTransport() {
		return new WebFluxSseServerTransport(new ObjectMapper(), "/mcp/message");
	}

	// Router function for SSE transport used by Spring WebFlux to start an HTTP
	// server.
	@Bean
	@ConditionalOnProperty(prefix = "transport", name = "mode", havingValue = "sse")
	public RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransport transport) {
		return transport.getRouterFunction();
	}

	@Bean
	public WeatherApiClient weatherApiClient() {
		return new WeatherApiClient();
	}

	@Bean
	public McpSyncServer mcpServer(ServerMcpTransport transport, WeatherApiClient weatherApiClient) { // @formatter:off

		// Configure server capabilities with resource support
		var capabilities = McpSchema.ServerCapabilities.builder()
			.tools(true) // Tool support with list changes notifications
			.logging() // Logging support
			.build();

		// Create the server with both tool and resource capabilities
		McpSyncServer server = McpServer.sync(transport)
			.serverInfo("MCP Demo Weather Server", "1.0.0")
			.capabilities(capabilities)
			.tools(McpToolUtils.toSyncToolRegistration(ToolCallbacks.from(weatherApiClient))) // Add @Tools
			.build();
		
		return server; // @formatter:on
	} // @formatter:on

}
