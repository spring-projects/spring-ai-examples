package org.springframework.ai.mcp.sample.server;

import org.springframework.ai.mcp.sample.server.providers.SpringAiToolProvider;
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

	// Note: this is not MCP Annotations related, but demonstrates how to register a SpringAI tool 
	// callback provider as MCP tools along with the @McpTool such
	@Bean
	public ToolCallbackProvider weatherTools(SpringAiToolProvider weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}
}
