package org.springframework.ai.mcp.sample.server;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import org.springaicommunity.mcp.spring.SyncMcpAnnotationProvider;
import org.springframework.ai.mcp.sample.server.provider.McpCompletionProvider;
import org.springframework.ai.mcp.sample.server.provider.McpToolProvider;
import org.springframework.ai.mcp.sample.server.provider.McpToolProvider2;
import org.springframework.ai.mcp.sample.server.provider.McpPromptProvider;
import org.springframework.ai.mcp.sample.server.provider.SpringAiToolProvider;
import org.springframework.ai.mcp.sample.server.provider.McpUserProfileResourceProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(SpringAiToolProvider weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}

	@Bean
	public List<SyncResourceSpecification> resourceSpecs(McpUserProfileResourceProvider userProfileResourceProvider) {
		return SyncMcpAnnotationProvider.createSyncResourceSpecifications(List.of(userProfileResourceProvider));
	}

	@Bean
	public List<SyncPromptSpecification> promptSpecs(McpPromptProvider promptProvider) {
		return SyncMcpAnnotationProvider.createSyncPromptSpecifications(List.of(promptProvider));
	}

	@Bean
	public List<SyncCompletionSpecification> completionSpecs(McpCompletionProvider autocompleteProvider) {
		return SyncMcpAnnotationProvider.createSyncCompleteSpecifications(List.of(autocompleteProvider));
	}

	@Bean
	public List<SyncToolSpecification> toolSpecs(McpToolProvider toolProvider, McpToolProvider2 toolProvider2) {
		var toolSpecs = SyncMcpAnnotationProvider.createSyncToolSpecifications(List.of(toolProvider, toolProvider2));
		return toolSpecs;
	}

}
