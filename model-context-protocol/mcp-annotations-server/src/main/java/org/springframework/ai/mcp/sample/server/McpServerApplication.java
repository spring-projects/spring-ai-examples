package org.springframework.ai.mcp.sample.server;

import java.util.List;

import com.logaritex.mcp.spring.SpringAiMcpAnnotationProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

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
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}

	@Bean
	public List<SyncResourceSpecification> resourceSpecs(UserProfileResourceProvider userProfileResourceProvider) {
		return SpringAiMcpAnnotationProvider.createSyncResourceSpecifications(List.of(userProfileResourceProvider));
	}

	@Bean
	public List<SyncPromptSpecification> promptSpecs(PromptProvider promptProvider) {
		return SpringAiMcpAnnotationProvider.createSyncPromptSpecifications(List.of(promptProvider));
	}

	@Bean
	public List<SyncCompletionSpecification> completionSpecs(AutocompleteProvider autocompleteProvider) {
		return SpringAiMcpAnnotationProvider.createSyncCompleteSpecifications(List.of(autocompleteProvider));
	}

}
