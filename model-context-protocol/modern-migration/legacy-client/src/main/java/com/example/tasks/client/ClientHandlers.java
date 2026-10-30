package com.example.tasks.client;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.mcp.annotation.McpElicitation;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Component;

/**
 * Client-side handlers for the server-initiated requests of the STREAMABLE server. On the
 * modern protocol these become inputs the client supplies when it retries a call.
 */
@Component
public class ClientHandlers {

	private static final Logger logger = LoggerFactory.getLogger(ClientHandlers.class);

	public record Confirmation(boolean confirmed) {
	}

	@McpElicitation(clients = "server1")
	public StructuredElicitResult<Confirmation> confirm(McpSchema.ElicitRequest request) {
		logger.info("ELICITATION: {}", request.message());
		return new StructuredElicitResult<>(ElicitResult.Action.ACCEPT, new Confirmation(true), null);
	}

	@McpSampling(clients = "server1")
	public CreateMessageResult sample(CreateMessageRequest request) {
		String prompt = ((TextContent) request.messages().get(0).content()).text();
		logger.info("SAMPLING: {}", prompt);
		return CreateMessageResult.builder(Role.ASSISTANT,
				TextContent.builder("Two tasks are open, one is about the quarterly report.").build(), "demo-model")
			.build();
	}

	@McpLogging(clients = "server1")
	public void log(LoggingMessageNotification notification) {
		logger.info("SERVER LOG [{}]: {}", notification.level(), notification.data());
	}

	@McpProgress(clients = "server1")
	public void progress(ProgressNotification notification) {
		logger.info("PROGRESS {}: {} {}", notification.progressToken(), notification.progress(), notification.message());
	}

	@McpToolListChanged(clients = "server1")
	public void toolsChanged(List<McpSchema.Tool> tools) {
		logger.info("TOOLS LIST CHANGED: {}", tools.stream().map(McpSchema.Tool::name).toList());
	}

}
