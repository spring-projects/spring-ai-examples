package org.springframework.ai.mcp.samples.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

@Service
public class ClientMcpHandlers {

	private static final Logger logger = LoggerFactory.getLogger(ClientMcpHandlers.class);

	@McpProgress(clientId = "server1")
	public void progressHandler(ProgressNotification progressNotification) {
		logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
				progressNotification.progressToken(), progressNotification.progress(),
				progressNotification.total(), progressNotification.message());
	}

	@McpLogging
	public void loggingHandler(LoggingMessageNotification loggingMessage) {
		logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
	}

	@McpSampling
	public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {

		logger.info("   MCP SAMPLING REQUEST: {}", llmRequest);

		String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();

		String modelHint = llmRequest.modelPreferences().hints().get(0).name();

		return CreateMessageResult.builder()
				.content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
				.build();
	};

	@McpElicitation
	public ElicitResult elicitationHandler(McpSchema.ElicitRequest request) {
		logger.info("MCP ELICITATION REQUEST: {}", request);

		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
	}

}
