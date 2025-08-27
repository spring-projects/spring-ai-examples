package org.springframework.ai.mcp.samples.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

@Service
public class McpClientHandlers {

	private static final Logger logger = LoggerFactory.getLogger(McpClientHandlers.class);

	@Autowired
	Map<String, ChatClient> chatClients;

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

		var userPrompt = ((McpSchema.TextContent) llmRequest.messages().get(0).content()).text();
		String modelHint = llmRequest.modelPreferences().hints().get(0).name();

		ChatClient hintedChatClient = chatClients.entrySet().stream()
				.filter(e -> e.getKey().contains(modelHint)).findFirst()
				.orElseThrow().getValue();

		String response = hintedChatClient.prompt()
				.system(llmRequest.systemPrompt())
				.user(userPrompt)
				.call()
				.content();

		return CreateMessageResult.builder().content(new McpSchema.TextContent(response)).build();
	};
}
