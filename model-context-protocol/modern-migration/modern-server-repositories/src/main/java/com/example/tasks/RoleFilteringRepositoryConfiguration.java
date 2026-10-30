package com.example.tasks;

import java.util.List;

import io.modelcontextprotocol.modern.McpSchema.CacheScope;
import io.modelcontextprotocol.modern.McpSchema.Tool;
import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.feature.McpChangeFeed;
import io.modelcontextprotocol.modern.server.feature.McpSyncTool;
import io.modelcontextprotocol.modern.server.feature.McpSyncToolRepository;
import io.modelcontextprotocol.modern.server.feature.ToolsPage;

import org.springframework.ai.mcp.modern.InMemorySyncToolRepository;
import org.springframework.ai.mcp.modern.McpTransportContexts;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customises the tool repository by delegation. The bean builds the in-memory repository from
 * the same {@code List<McpSyncTool>} beans the auto-configuration would use (the annotated
 * tools and {@link AdminToolsConfiguration}) and wraps it. Because a {@link McpSyncToolRepository} bean now
 * exists, the auto-configured repository backs off.
 * <p>
 * Callers without the {@code X-Role: admin} header neither see the {@code admin_*} tools nor
 * can call them: {@code find} returns {@code null}, which the server answers as an unknown tool.
 */
@Configuration(proxyBeanMethods = false)
public class RoleFilteringRepositoryConfiguration {

	// The lists are resolved through ObjectProvider, and the repository is a @Bean method
	// rather than a @Component, so that it is created after the annotated beans were scanned.
	@Bean
	McpSyncToolRepository toolRepository(ObjectProvider<List<McpSyncTool>> tools, McpChangeFeed changes) {
		InMemorySyncToolRepository inMemory = InMemorySyncToolRepository.builder()
			.tools(tools.stream().flatMap(List::stream).toList())
			.changeBroadcaster(changes)
			.build();
		return new McpSyncToolRepository() {

			@Override
			public ToolsPage list(McpRequestContext ctx, String cursor) {
				ToolsPage page = inMemory.list(ctx, cursor);
				List<Tool> visible = page.tools().stream().filter(tool -> visible(ctx, tool.name())).toList();
				// The listing depends on the caller, so no shared cache may reuse it.
				return new ToolsPage(visible, page.nextCursor(), page.ttlMs(), CacheScope.PRIVATE);
			}

			@Override
			public McpSyncTool find(McpRequestContext ctx, String name) {
				McpSyncTool tool = inMemory.find(ctx, name);
				return tool != null && visible(ctx, name) ? tool : null;
			}

		};
	}

	private static boolean visible(McpRequestContext ctx, String toolName) {
		// The default transport context extractor puts the HTTP request headers in the context.
		return !toolName.startsWith("admin_") || "admin".equals(McpTransportContexts.header(ctx, "X-Role"));
	}

}
