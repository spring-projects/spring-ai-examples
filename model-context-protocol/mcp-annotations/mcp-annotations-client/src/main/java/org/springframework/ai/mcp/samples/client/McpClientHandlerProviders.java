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
public class McpClientHandlerProviders {

	private static final Logger logger = LoggerFactory.getLogger(McpClientHandlerProviders.class);

	/**
	 * Handles progress notifications for the client identified by {@code clientId = "server1"}.
	 * <br>
	 * The {@code clientId} is configured via application properties, for example:
	 * <ul>
	 *   <li>{@code spring.ai.mcp.client.sse.connections.server1.url=...}</li>
	 *   <li>{@code spring.ai.mcp.client.streamable-http.connections.server1.url=...}</li>
	 * </ul>
	 * 
	 * The handler is assigned only to the client with ID "server1".
	 *
	 * @param progressNotification the progress notification received from the server
	 */
	@McpProgress(clients = "server1")
	public void progressHandler(ProgressNotification progressNotification) {
		logger.info("MCP PROGRESS: [{}] progress: {} total: {} message: {}",
				progressNotification.progressToken(), progressNotification.progress(),
				progressNotification.total(), progressNotification.message());
	}

	@McpLogging(clients = "server1")
	public void loggingHandler(LoggingMessageNotification loggingMessage) {
		logger.info("MCP LOGGING: [{}] {}", loggingMessage.level(), loggingMessage.data());
	}

	@McpSampling(clients = "server1")
	public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) {
		logger.info("MCP SAMPLING: {}", llmRequest);

		String userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
		String modelHint = llmRequest.modelPreferences().hints().get(0).name();

		return CreateMessageResult.builder()
				.content(new McpSchema.TextContent("Response " + userPrompt + " with model hint " + modelHint))
				.build();
	}

	@McpElicitation(clients = "server1")
	public ElicitResult elicitationHandler(McpSchema.ElicitRequest request) {
		logger.info("MCP ELICITATION: {}", request);
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("message", request.message()));
	}

}
